/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * TCF Processes - process control service.
 * Processes service provides access to the target OS's process information,
 * allows to start and terminate a process, and allows to attach and
 * detach a process for debugging. Debug services, like Memory and Run Control,
 * require a process to be attached before they can access it.
 */

#include "mdep.h"
#include "config.h"

#if SERVICE_Processes

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <assert.h>
#include "myalloc.h"
#include "protocol.h"
#include "trace.h"
#include "context.h"
#include "json.h"
#include "exceptions.h"
#include "runctrl.h"
#include "processes.h"

static const char * PROCESSES = "Processes";

#if defined(WIN32)
#  include "tlhelp32.h"
#elif defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#else
#  include <sys/stat.h>
#  include <fcntl.h>
#  include <unistd.h>
#  include <dirent.h>
#endif

typedef struct AttachDoneArgs AttachDoneArgs;

struct AttachDoneArgs {
    Channel * c;
    char token[256];
};

static void write_context(OutputStream * out, int pid) {
    Context * ctx = NULL;

    write_stream(out, '{');

#if defined(WIN32)
#elif defined(_WRS_KERNEL)
#else
    {
        char dir[FILE_PATH_SIZE];
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (chdir(dir) >= 0) {
            int sz;
            char fnm[FILE_PATH_SIZE + 1];

            json_write_string(out, "CanTerminate");
            write_stream(out, ':');
            json_write_boolean(out, 1);
            write_stream(out, ',');

            if ((sz = readlink("exe", fnm, FILE_PATH_SIZE)) > 0) {
                fnm[sz] = 0;
                json_write_string(out, "Name");
                write_stream(out, ':');
                json_write_string(out, fnm);
                write_stream(out, ',');
            }
        }
    }
#endif
    
    ctx = context_find_from_pid(pid);
    if (ctx != NULL) {
        json_write_string(out, "Attached");
        write_stream(out, ':');
        json_write_boolean(out, 1);
        write_stream(out, ',');
    }

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, pid2id(pid, 0));

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
#if defined(WIN32)
#elif defined(_WRS_KERNEL)
        if (TASK_ID_VERIFY(pid) == ERROR) err = ERR_INV_CONTEXT;
#else
        struct_stat st;
        char dir[FILE_PATH_SIZE];
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
#endif
    }

    write_errno(&c->out, err);
    
    if (err == 0 && pid != 0 && parent == 0) {
        write_context(&c->out, pid);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    int attached_only;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    attached_only = json_read_boolean(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (id[0] != 0) {
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
#if defined(WIN32)
    DWORD err = 0;
    HANDLE snapshot;
    PROCESSENTRY32 pe32;

    snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (snapshot == INVALID_HANDLE_VALUE) err = GetLastError();
    pe32.dwSize = sizeof(PROCESSENTRY32);
    if (!err && !Process32First(snapshot, &pe32)) {
        err = GetLastError();
        CloseHandle(snapshot);
    }
    if (err) {
        /* TODO need better translation of WIN32 error codes to errno */
        write_errno(&c->out, EINVAL);
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        write_errno(&c->out, 0);
        write_stream(&c->out, '[');
        do {
            if (!attached_only || context_find_from_pid(pe32.th32ProcessID) != NULL) {
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, pid2id(pe32.th32ProcessID, 0));
                cnt++;
            }
        }
        while (Process32Next(snapshot, &pe32));
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    if (snapshot != INVALID_HANDLE_VALUE) CloseHandle(snapshot);
#elif defined(_WRS_KERNEL)
        int i = 0;
        int cnt = 0;
        int ids_cnt = 0;
        int ids_max = 500;
        int * ids = (int *)loc_alloc(ids_max * sizeof(int));
        while (1) {
            ids_cnt = taskIdListGet(ids, ids_max);
            if (ids_cnt < ids_max) break;
            loc_free(ids);
            ids_max *= 2;
            ids = (int *)loc_alloc(ids_max * sizeof(int));
        }
        write_errno(&c->out, 0);
        write_stream(&c->out, '[');
        for (i = 0; i < ids_cnt; i++) {
            if (!attached_only || context_find_from_pid(ids[i]) != NULL) {
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, pid2id(ids[i], 0));
                cnt++;
            }
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
#else
        DIR * proc = opendir("/proc");
        if (proc == NULL) {
            write_errno(&c->out, errno);
            write_stringz(&c->out, "null");
        }
        else {
            int cnt = 0;
            write_errno(&c->out, 0);
            write_stream(&c->out, '[');
            for (;;) {
                struct dirent * ent = readdir(proc);
                if (ent == NULL) break;
                if (ent->d_name[0] >= '1' && ent->d_name[0] <= '9') {
                    pid_t pid = atol(ent->d_name);
                    if (!attached_only || context_find_from_pid(pid) != NULL) {
                        if (cnt > 0) write_stream(&c->out, ',');
                        json_write_string(&c->out, pid2id(pid, 0));
                        cnt++;
                    }
                }
            }
            write_stream(&c->out, ']');
            write_stream(&c->out, 0);
            closedir(proc);
        }
#endif
    }

    write_stream(&c->out, MARKER_EOM);
}

static void attach_done(int error, Context * ctx, void * arg) {
    AttachDoneArgs * data = arg;
    Channel * c = data->c;

    if (!is_stream_closed(c)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, data->token);
        write_errno(&c->out, error);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
    }
    stream_unlock(c);
    loc_free(data);
}

static void command_attach(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);

    if (parent != 0) {
        err = ERR_INV_CONTEXT;
    }
    else if (context_find_from_pid(pid) != NULL) {
        err = ERR_ALREADY_ATTACHED;
    }
    else {
        AttachDoneArgs * data = loc_alloc_zero(sizeof *data);
        data->c = c;
        strcpy(data->token, token);
        stream_lock(c);
        if (context_attach(pid, attach_done, data, 0) == 0) return;
        err = errno;
        stream_unlock(c);
        loc_free(data);
    }
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_detach(char * token, Channel * c) {
    /* TODO: implement command_detach() */
    exception(ERR_PROTOCOL);
}

static void command_terminate(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (parent != 0) {
        err = ERR_INV_CONTEXT;
    }
    else {
#if defined(WIN32)
        HANDLE h = OpenProcess(PROCESS_TERMINATE, FALSE, pid);
        if (h == NULL) {
            err = ERR_INV_CONTEXT;
        }
        else {
            TerminateProcess(h, 1);
            CloseHandle(h);
        }
#elif defined(_WRS_KERNEL)
        if (kill(pid, SIGTERM) < 0) err = errno;
#else
        if (kill(pid, SIGTERM) < 0) err = errno;
#endif
    }

    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_signal(char * token, Channel * c) {
    int err = 0;
    char id[256];
    int signal = 0;
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    signal = (int)json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (parent != 0) {
        err = ERR_INV_CONTEXT;
    }
    else {
#if defined(WIN32)
        err = ENOSYS;
#elif defined(_WRS_KERNEL)
        if (kill(pid, signal) < 0) err = errno;
#else
        if (kill(pid, signal) < 0) err = errno;
#endif
    }

    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_environment(char * token, Channel * c) {
    char ** p = environ;

    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, '[');
    if (p != NULL) {
        while (*p != NULL) {
            if (p != environ) write_stream(&c->out, ',');
            json_write_string(&c->out, *p++);
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void start_done(int error, Context * ctx, void * arg) {
    AttachDoneArgs * data = arg;
    Channel * c = data->c;

    if (!is_stream_closed(c)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, data->token);
        write_errno(&c->out, error);
        if (ctx == NULL) write_string(&c->out, "null");
        else write_context(&c->out, ctx->pid);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
    }
    stream_unlock(c);
    loc_free(data);
}

static void command_start(char * token, Channel * c) {
    int pid = 0;
    int err = 0;
    char dir[FILE_PATH_SIZE];
    char exe[FILE_PATH_SIZE];
    char ** args = NULL;
    char ** envp = NULL;
    int args_len = 0;
    int envp_len = 0;
    int attach = 0;
    int selfattach = 0;
    int pending = 0;
    Trap trap;

    if (set_trap(&trap)) {
        json_read_string(&c->inp, dir, sizeof(dir));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        json_read_string(&c->inp, exe, sizeof(exe));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        args = json_read_alloc_string_array(&c->inp, &args_len);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        envp = json_read_alloc_string_array(&c->inp, &envp_len);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        attach = json_read_boolean(&c->inp);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

        if (dir[0] != 0 && chdir(dir) < 0) err = errno;
        if (err == 0) {
#if defined(WIN32)
            STARTUPINFO si;
            PROCESS_INFORMATION prs;
            char * cmd = NULL;
            if (args != NULL) {
                int i = 0;
                int cmd_size = 0;
                int cmd_pos = 0;
#               define cmd_append(ch) { \
                    if (!cmd) { \
                        cmd_size = 0x1000; \
                        cmd = (char *)loc_alloc(cmd_size); \
                    } \
                    else if (cmd_pos >= cmd_size) { \
                        char * tmp = (char *)loc_alloc(cmd_size * 2); \
                        memcpy(tmp, cmd, cmd_pos); \
                        loc_free(cmd); \
                        cmd = tmp; \
                        cmd_size *= 2; \
                    }; \
                    cmd[cmd_pos++] = (ch); \
                }
                while (args[i] != NULL) {
                    char * p = args[i++];
                    if (cmd_pos > 0) cmd_append(' ');
                    cmd_append('"');
                    while (*p) {
                        if (*p == '"') cmd_append('\\');
                        cmd_append(*p);
                        p++;
                    }
                    cmd_append('"');
                }
                cmd_append(0);
#               undef cmd_append
            }
            memset(&si, 0, sizeof(si));
            memset(&prs, 0, sizeof(prs));
            si.cb = sizeof(si);
            if (CreateProcess(exe, cmd, NULL, NULL, FALSE, (attach ? CREATE_SUSPENDED : 0),
                (envp ? envp[0] : NULL), (dir[0] ? dir : NULL), &si, &prs) == 0)
            {
                trace(LOG_ALWAYS, "Can't start process '%s': error %d", exe, GetLastError());
                err = EINVAL;
            }
            if (!err) {
                pid = prs.dwProcessId;
                CloseHandle(prs.hThread);
                CloseHandle(prs.hProcess);
            }
            loc_free(cmd);
#elif defined(_WRS_KERNEL)
            char * ptr;
            SYM_TYPE type;
            if (symFindByName(sysSymTbl, exe, &ptr, &type) != OK) {
                err = errno;
                if (err == S_symLib_SYMBOL_NOT_FOUND) err = ERR_SYM_NOT_FOUND;
                assert(err != 0);
            }
            else {
                /* TODO: arguments, environment */
                pid = taskCreate("tTcf", 100, 0, 0x4000, (FUNCPTR)ptr, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                if (attach) {
                    taskStop(pid);
                    taskActivate(pid);
                    assert(taskIsStopped(pid));
                }
                else {
                    taskActivate(pid);
                }
            }
#else
            pid = fork();
            if (pid < 0) err = errno;
            if (pid == 0) {
                int fd;

                if (attach && context_attach_self() < 0) exit(errno);
                fd = sysconf(_SC_OPEN_MAX);
                if (fd < 0) exit(errno);
                while (fd-- > 0) close(fd);
                if (open("/dev/null", O_RDONLY) != 0) exit(errno);
                if (open("/dev/null", O_WRONLY) != 1) exit(errno);
                if (dup(1) != 2) exit(5);
                execve(exe, args, envp);
                exit(errno);
            }
            selfattach = 1;
#endif            
        }
        if (attach && err == 0) {
            AttachDoneArgs * data = loc_alloc_zero(sizeof *data);
            data->c = c;
            strcpy(data->token, token);
            stream_lock(c);
            pending = context_attach(pid, start_done, data, selfattach) == 0;
            if (!pending) {
                err = errno;
                stream_unlock(c);
                loc_free(data);
            }
        }
        if (!pending) {
            write_stringz(&c->out, "R");
            write_stringz(&c->out, token);
            write_errno(&c->out, err);
            if (err || pid == 0) {
                write_stringz(&c->out, "null");
            }
            else {
                write_context(&c->out, pid);
                write_stream(&c->out, 0);
            }
            write_stream(&c->out, MARKER_EOM);
        }
        clear_trap(&trap);
    }

    loc_free(args);
    loc_free(envp);

    if (trap.error) exception(trap.error);
}

void ini_processes_service(Protocol * proto) {
    add_command_handler(proto, PROCESSES, "getContext", command_get_context);
    add_command_handler(proto, PROCESSES, "getChildren", command_get_children);
    add_command_handler(proto, PROCESSES, "attach", command_attach);
    add_command_handler(proto, PROCESSES, "detach", command_detach);
    add_command_handler(proto, PROCESSES, "terminate", command_terminate);
    add_command_handler(proto, PROCESSES, "signal", command_signal);
    add_command_handler(proto, PROCESSES, "getEnvironment", command_get_environment);
    add_command_handler(proto, PROCESSES, "start", command_start);
}

#endif


