/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

#include "config.h"
#if SERVICE_SysMonitor

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>
#include <sys/types.h>
#include "mdep.h"
#include "sysmon.h"
#include "protocol.h"
#include "json.h"
#include "context.h"
#include "errors.h"

static const char SYS_MON[] = "SysMonitor";

#if defined(WIN32) || defined(__CYGWIN__)
#  error "SysMonitor service is not supported for Windows"
#elif defined(_WRS_KERNEL)
#  error "SysMonitor service is not supported for VxWorks"
#endif

#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <pwd.h>
#include <grp.h>
#include <linux/param.h>

#define BUF_EOF (-1)

static char buf[1024];
static int buf_fd = -1;
static int buf_pos = 0;
static int buf_len = 0;
static int buf_ch = 0;

static void next_ch(void) {
    while (buf_len >= 0 && buf_pos >= buf_len) {
        buf_pos = 0;
        buf_len = read(buf_fd, buf, sizeof(buf));
        if (buf_len == 0) buf_len = -1;
    }
    if (buf_len < 0) {
        buf_ch = BUF_EOF;
    }
    else {
        buf_ch = buf[buf_pos++];
    }
}

static void first_ch(int fd) {
    buf_fd = fd;
    buf_pos = 0;
    buf_len = 0;
    next_ch();
}

static void write_string_array(OutputStream * out, int f) {
    int cnt = 0;
    first_ch(f);
    out->write(out, '[');
    while (buf_ch != BUF_EOF && buf_ch != 0) {
        if (cnt > 0) out->write(out, ',');
        out->write(out, '"');
        do {
            json_write_char(out, buf_ch);
            next_ch();
        }
        while (buf_ch != BUF_EOF && buf_ch != 0);
        next_ch();
        out->write(out, '"');
        cnt++;
    }
    out->write(out, ']');
}

static void write_context(OutputStream * out, char * id, char * parent_id, char * dir) {
    char fnm[FILE_PATH_SIZE + 1];
    int sz;
    int f;

    out->write(out, '{');

    if (chdir(dir) >= 0) {
        if ((sz = readlink("cwd", fnm, FILE_PATH_SIZE)) > 0) {
            fnm[sz] = 0;
            json_write_string(out, "CWD");
            out->write(out, ':');
            json_write_string(out, fnm);
            out->write(out, ',');
        }

        if ((sz = readlink("root", fnm, FILE_PATH_SIZE)) > 0) {
            fnm[sz] = 0;
            json_write_string(out, "Root");
            out->write(out, ':');
            json_write_string(out, fnm);
            out->write(out, ',');
        }

        f = open("stat", O_RDONLY);
        if (f >= 0) {
            struct_stat st;
            if (fstat(f, &st) == 0) {
                struct passwd * pwd;
                struct group * grp;

                json_write_string(out, "UID");
                out->write(out, ':');
                json_write_long(out, st.st_uid);
                out->write(out, ',');

                json_write_string(out, "UGID");
                out->write(out, ':');
                json_write_long(out, st.st_gid);
                out->write(out, ',');

                pwd = getpwuid(st.st_uid);
                if (pwd != NULL) {
                    json_write_string(out, "UserName");
                    out->write(out, ':');
                    json_write_string(out, pwd->pw_name);
                    out->write(out, ',');
                }

                grp = getgrgid(st.st_gid);
                if (grp != NULL) {
                    json_write_string(out, "GroupName");
                    out->write(out, ':');
                    json_write_string(out, grp->gr_name);
                    out->write(out, ',');
                }
            }

            memset(buf, 0, sizeof(buf));
            if ((sz = read(f, buf, sizeof(buf))) > 0) {
                char * str = buf;
                int pid = 0;                // The process ID.
                char * comm = fnm;          // The  filename  of  the  executable,  in parentheses.  This is visible
                                            // whether or not the executable is swapped out.
                char state = 0;             // One character from the string "RSDZTW"  where  R  is  running,  S  is
                                            // sleeping  in  an  interruptible wait, D is waiting in uninterruptible
                                            // disk sleep, Z is zombie, T is traced or stopped (on a signal), and  W
                                            // is paging.
                int ppid = 0;               // The PID of the parent.
                int pgrp = 0;               // The process group ID of the process.
                int session = 0;            // The session ID of the process.
                int tty_nr = 0;             // The tty the process uses.
                int tpgid = 0;              // The process group ID of the process which currently owns the tty that
                                            // the process is connected to.
                unsigned long flags = 0;    // The kernel flags word of the process. For bit meanings, see the  PF_*
                                            // defines in <linux/sched.h>.  Details depend on the kernel version.
                unsigned long minflt = 0;   // The  number  of  minor  faults  the  process  has made which have not
                                            // required loading a memory page from disk.
                unsigned long cminflt = 0;  // The number of minor faults that  the  process's  waited-for  children
                                            // have made.
                unsigned long majflt = 0;   // The  number  of major faults the process has made which have required
                                            // loading a memory page from disk.
                unsigned long cmajflt = 0;  // The number of major faults that  the  process's  waited-for  children
                                            // have made.
                unsigned long utime = 0;    // The  number  of  jiffies that this process has been scheduled in user
                                            // mode.
                unsigned long stime = 0;    // The number of jiffies that this process has been scheduled in  kernel
                                            // mode.
                long cutime = 0;            // The  number  of  jiffies that this process's waited-for children have
                                            // been scheduled in user mode. (See also times(2).)
                long cstime = 0;            // The number of jiffies that this process's  waited-for  children  have
                                            // been scheduled in kernel mode.
                long priority = 0;          // The  standard  nice value, plus fifteen.  The value is never negative
                                            // in the kernel.
                long nice = 0;              // The nice value ranges from 19 (nicest) to -19 (not nice to others).
                long dummy = 0;             // This value is hard coded to 0 as a placeholder for a removed field.
                long itrealvalue = 0;       // The time in jiffies before the next SIGALRM is sent  to  the  process
                                            // due to an interval timer.
                unsigned long starttime = 0;// The time in jiffies the process started after system boot.
                unsigned long vsize = 0;    // Virtual memory size in bytes.
                long rss = 0;               // Resident  Set  Size:  number of pages the process has in real memory,
                                            // minus 3 for administrative purposes. This is  just  the  pages  which
                                            // count  towards  text,  data,  or  stack space.  This does not include
                                            // pages which have not been demand-loaded in, or which are swapped out.
                unsigned long rlim = 0;     // Current  limit in bytes on the rss of the process (usually 4294967295
                                            // on i386).
                unsigned long startcode = 0;// The address above which program text can run.
                unsigned long endcode = 0;  // The address below which program text can run.
                unsigned long startstack =0;// The address of the start of the stack.
                unsigned long kstkesp = 0;  // The current value of esp (stack pointer),  as  found  in  the  kernel
                                            // stack page for the process.
                unsigned long kstkeip = 0;  // The current EIP (instruction pointer).
                unsigned long signal = 0;   // The bitmap of pending signals.
                unsigned long blocked = 0;  // The bitmap of blocked signals.
                unsigned long sigignore = 0;// The bitmap of ignored signals.
                unsigned long sigcatch = 0; // The bitmap of caught signals.
                unsigned long wchan = 0;    // This  is  the  "channel"  in which the process is waiting.  It is the
                                            // address of a system call, and can be looked up in a namelist  if  you
                                            // need  a  textual  name.   (If you have an up-to-date /etc/psdatabase,
                                            // then try ps -l to see the WCHAN field in action.)
                unsigned long nswap = 0;    // Number of pages swapped (not maintained).
                unsigned long cnswap = 0;   // Cumulative nswap for child processes (not maintained).
                int exit_signal = 0;        // Signal to be sent to parent when we die.
                int processor = 0;          // CPU number last executed on.
                unsigned long rt_priority=0;// Real-time scheduling priority (see sched_setscheduler(2)).
                unsigned long policy = 0;   // Scheduling policy (see sched_setscheduler(2)).

                assert(sz < sizeof(buf));
                buf[sz] = 0;

                pid = (int)strtol(str, &str, 10);
                while (*str == ' ') str++;
                if (*str == '(') str++;
                sz = 0;
                while (*str && *str != ')') comm[sz++] = *str++;
                comm[sz] = 0;
                if (*str == ')') str++;
                while (*str == ' ') str++;

                sscanf(str,
                    "%c %d %d %d %d %d %lu %lu %lu %lu %lu %lu %lu %ld %ld %ld %ld %ld %ld %lu %lu %ld %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %d %d %lu %lu",
                    &state, &ppid, &pgrp, &session, &tty_nr, &tpgid, &flags,
                    &minflt, &cminflt, &majflt, &cmajflt, &utime, &stime, &cutime, &cstime,
                    &priority, &nice, &dummy, &itrealvalue, &starttime, &vsize, &rss, &rlim,
                    &startcode, &endcode, &startstack, &kstkesp, &kstkeip, &signal, &blocked, &sigignore, &sigcatch,
                    &wchan, &nswap, &cnswap, &exit_signal, &processor, &rt_priority, &policy);

                json_write_string(out, "PID");
                out->write(out, ':');
                json_write_long(out, pid);
                out->write(out, ',');

                json_write_string(out, "File");
                out->write(out, ':');
                json_write_string(out, comm);
                out->write(out, ',');

                json_write_string(out, "State");
                out->write(out, ':');
                out->write(out, '"');
                json_write_char(out, state);
                out->write(out, '"');
                out->write(out, ',');

                if (ppid > 0) {
                    json_write_string(out, "PPID");
                    out->write(out, ':');
                    json_write_long(out, ppid);
                    out->write(out, ',');
                }

                json_write_string(out, "PGRP");
                out->write(out, ':');
                json_write_long(out, pgrp);
                out->write(out, ',');

                json_write_string(out, "Session");
                out->write(out, ':');
                json_write_long(out, session);
                out->write(out, ',');

                if (tty_nr > 0) {
                    json_write_string(out, "TTY");
                    out->write(out, ':');
                    json_write_long(out, tty_nr);
                    out->write(out, ',');
                }

                if (tpgid > 0) {
                    json_write_string(out, "TGID");
                    out->write(out, ':');
                    json_write_long(out, tpgid);
                    out->write(out, ',');
                }

                json_write_string(out, "Flags");
                out->write(out, ':');
                json_write_ulong(out, flags);
                out->write(out, ',');

                json_write_string(out, "MinFlt");
                out->write(out, ':');
                json_write_ulong(out, minflt);
                out->write(out, ',');

                json_write_string(out, "CMinFlt");
                out->write(out, ':');
                json_write_ulong(out, cminflt);
                out->write(out, ',');

                json_write_string(out, "MajFlt");
                out->write(out, ':');
                json_write_ulong(out, majflt);
                out->write(out, ',');

                json_write_string(out, "CMajFlt");
                out->write(out, ':');
                json_write_ulong(out, cmajflt);
                out->write(out, ',');

                json_write_string(out, "UTime");
                out->write(out, ':');
                json_write_int64(out, (int64)utime * 1000 / HZ);
                out->write(out, ',');

                json_write_string(out, "STime");
                out->write(out, ':');
                json_write_int64(out, (int64)stime * 1000 / HZ);
                out->write(out, ',');

                json_write_string(out, "CUTime");
                out->write(out, ':');
                json_write_int64(out, (int64)cutime * 1000 / HZ);
                out->write(out, ',');

                json_write_string(out, "CSTime");
                out->write(out, ':');
                json_write_int64(out, (int64)cstime * 1000 / HZ);
                out->write(out, ',');

                json_write_string(out, "Priority");
                out->write(out, ':');
                json_write_long(out, (long)priority - 15);
                out->write(out, ',');

                if (nice != 0) {
                    json_write_string(out, "Nice");
                    out->write(out, ':');
                    json_write_long(out, nice);
                    out->write(out, ',');
                }

                if (itrealvalue != 0) {
                    json_write_string(out, "ITRealValue");
                    out->write(out, ':');
                    json_write_int64(out, (int64)itrealvalue * 1000 / HZ);
                    out->write(out, ',');
                }

                json_write_string(out, "StartTime");
                out->write(out, ':');
                json_write_int64(out, (int64)starttime * 1000 / HZ);
                out->write(out, ',');

                json_write_string(out, "VSize");
                out->write(out, ':');
                json_write_ulong(out, vsize);
                out->write(out, ',');

                json_write_string(out, "PSize");
                out->write(out, ':');
                json_write_ulong(out, getpagesize());
                out->write(out, ',');

                json_write_string(out, "RSS");
                out->write(out, ':');
                json_write_long(out, rss);
                out->write(out, ',');

                json_write_string(out, "RLimit");
                out->write(out, ':');
                json_write_ulong(out, rlim);
                out->write(out, ',');

                if (startcode != 0) {
                    json_write_string(out, "CodeStart");
                    out->write(out, ':');
                    json_write_ulong(out, startcode);
                    out->write(out, ',');
                }

                if (endcode != 0) {
                    json_write_string(out, "CodeEnd");
                    out->write(out, ':');
                    json_write_ulong(out, endcode);
                    out->write(out, ',');
                }

                if (startstack != 0) {
                    json_write_string(out, "StackStart");
                    out->write(out, ':');
                    json_write_ulong(out, startstack);
                    out->write(out, ',');
                }

                json_write_string(out, "Signals");
                out->write(out, ':');
                json_write_ulong(out, signal);
                out->write(out, ',');

                json_write_string(out, "SigBlock");
                out->write(out, ':');
                json_write_ulong(out, blocked);
                out->write(out, ',');

                json_write_string(out, "SigIgnore");
                out->write(out, ':');
                json_write_ulong(out, sigignore);
                out->write(out, ',');

                json_write_string(out, "SigCatch");
                out->write(out, ':');
                json_write_ulong(out, sigcatch);
                out->write(out, ',');

                if (wchan != 0) {
                    json_write_string(out, "WChan");
                    out->write(out, ':');
                    json_write_ulong(out, wchan);
                    out->write(out, ',');
                }

                json_write_string(out, "NSwap");
                out->write(out, ':');
                json_write_ulong(out, nswap);
                out->write(out, ',');

                json_write_string(out, "CNSwap");
                out->write(out, ':');
                json_write_ulong(out, cnswap);
                out->write(out, ',');

                json_write_string(out, "ExitSignal");
                out->write(out, ':');
                json_write_long(out, exit_signal);
                out->write(out, ',');

                json_write_string(out, "Processor");
                out->write(out, ':');
                json_write_long(out, processor);
                out->write(out, ',');

                json_write_string(out, "RTPriority");
                out->write(out, ':');
                json_write_ulong(out, rt_priority);
                out->write(out, ',');

                json_write_string(out, "Policy");
                out->write(out, ':');
                json_write_ulong(out, policy);
                out->write(out, ',');
            }
            close(f);
        }
    }

    if (parent_id != NULL && parent_id[0] != 0) {
        json_write_string(out, "ParentID");
        out->write(out, ':');
        json_write_string(out, parent_id);
        out->write(out, ',');
    }

    json_write_string(out, "ID");
    out->write(out, ':');
    json_write_string(out, id);

    out->write(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[FILE_PATH_SIZE];

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0) {
        struct_stat st;
        if (parent != 0) {
            snprintf(dir, sizeof(dir), "/proc/%d/task/%d", parent, pid);
        }
        else {
            snprintf(dir, sizeof(dir), "/proc/%d", pid);
        }
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }

    write_errno(&c->out, err);
    
    if (err == 0 && pid != 0) {
        char bf[256];
        write_context(&c->out, id, parent == 0 ? NULL : strcpy(bf, pid2id(parent, 0)), dir);
        c->out.write(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    DIR * proc = NULL;
    char dir[FILE_PATH_SIZE];
    pid_t pid = 0;
    pid_t parent = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid == 0) strcpy(dir, "/proc");
    else snprintf(dir, sizeof(dir), "/proc/%d/task", pid);

    if (parent != 0) {
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
        proc = opendir(dir);
        if (proc == NULL) {
            write_errno(&c->out, errno);
            write_stringz(&c->out, "null");
        }
        else {
            int cnt = 0;
            write_errno(&c->out, 0);
            c->out.write(&c->out, '[');
            for (;;) {
                struct dirent * ent = readdir(proc);
                if (ent == NULL) break;
                if (ent->d_name[0] >= '1' && ent->d_name[0] <= '9') {
                    if (cnt > 0) c->out.write(&c->out, ',');
                    json_write_string(&c->out, pid2id(atol(ent->d_name), pid));
                    cnt++;
                }
            }
            c->out.write(&c->out, ']');
            c->out.write(&c->out, 0);
            closedir(proc);
        }
    }

    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_command_line(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[256];
    int f;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        struct_stat st;
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    if (err == 0 && chdir(dir) < 0) err = errno;
    if (err == 0 && (f = open("cmdline", O_RDONLY)) < 0) err = errno;

    write_errno(&c->out, err);
    
    if (err == 0) {
        write_string_array(&c->out, f);
        close(f);
        c->out.write(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_environment(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[256];
    int f;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        struct_stat st;
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    if (err == 0 && chdir(dir) < 0) err = errno;
    if (err == 0 && (f = open("environ", O_RDONLY)) < 0) err = errno;

    write_errno(&c->out, err);
    
    if (err == 0) {
        write_string_array(&c->out, f);
        close(f);
        c->out.write(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    c->out.write(&c->out, MARKER_EOM);
}

extern void ini_sys_mon_service(Protocol * proto) {
    add_command_handler(proto, SYS_MON, "getContext", command_get_context);
    add_command_handler(proto, SYS_MON, "getChildren", command_get_children);
    add_command_handler(proto, SYS_MON, "getCommandLine", command_get_command_line);
    add_command_handler(proto, SYS_MON, "getEnvironment", command_get_environment);
}

#endif

