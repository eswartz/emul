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
#include <fcntl.h>
#include <signal.h>
#include <assert.h>
#include "myalloc.h"
#include "protocol.h"
#include "trace.h"
#include "context.h"
#include "json.h"
#include "asyncreq.h"
#include "exceptions.h"
#include "runctrl.h"
#include "filesystem.h"
#include "processes.h"

static const char * PROCESSES = "Processes";

#if defined(WIN32)
#  include "tlhelp32.h"
#  ifdef _MSC_VER
#    include "winternl.h"
#  else
#    include "ntdef.h"
#  endif
#  ifndef STATUS_INFO_LENGTH_MISMATCH
#   define STATUS_INFO_LENGTH_MISMATCH      ((NTSTATUS)0xC0000004L)
#  endif
#  ifndef SystemHandleInformation
#    define SystemHandleInformation 16
#  endif
#elif defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#  include <ioLib.h>
#  include <ptyDrv.h>
#  include <taskHookLib.h>
#else
#  include <sys/stat.h>
#  include <fcntl.h>
#  include <unistd.h>
#  include <dirent.h>
#endif

#define PIPE_SIZE 0x1000

typedef struct AttachDoneArgs {
    Channel * c;
    char token[256];
} AttachDoneArgs;

typedef struct ChildProcess {
    LINK link;
    int pid;
    int refs;
    TCFBroadcastGroup * bcg;
    int inp;
    int out;
    int err;
    unsigned exit_code;
} ChildProcess;

typedef struct ProcessOutput {
    int stream_id;
    ChildProcess * prs;
    AsyncReqInfo req;
    char buf[0x400];
} ProcessOutput;

#define link2prs(A)  ((ChildProcess *)((char *)(A) - offsetof(ChildProcess, link)))

static LINK prs_list;
#if defined(_WRS_KERNEL)
static SEM_ID prs_list_lock = NULL;
#endif

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

static void send_event_process_output(OutputStream * out, ProcessOutput * buf) {
    ChildProcess * prs = buf->prs;
    JsonWriteBinaryState state;

    assert(buf->req.u.fio.rval > 0);
    assert(buf->req.u.fio.bufp == buf->buf);

    write_stringz(out, "E");
    write_stringz(out, PROCESSES);
    write_stringz(out, "output");

    json_write_string(out, pid2id(prs->pid, 0));
    write_stream(out, 0);

    json_write_long(out, buf->stream_id);
    write_stream(out, 0);

    json_write_binary_start(&state, out);
    json_write_binary_data(&state, buf->buf, buf->req.u.fio.rval);
    json_write_binary_end(&state);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_process_exited(OutputStream * out, ChildProcess * prs) {
    write_stringz(out, "E");
    write_stringz(out, PROCESSES);
    write_stringz(out, "exited");

    json_write_string(out, pid2id(prs->pid, 0));
    write_stream(out, 0);

    json_write_long(out, prs->exit_code);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
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
    if (snapshot == INVALID_HANDLE_VALUE) err = set_win32_errno(GetLastError());
    pe32.dwSize = sizeof(PROCESSENTRY32);
    if (!err && !Process32First(snapshot, &pe32)) {
        err = set_win32_errno(GetLastError());
        CloseHandle(snapshot);
    }
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
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

#if defined(WIN32)
    err = ENOSYS;
#elif defined(_WRS_KERNEL)
    if (kill(pid, signal) < 0) err = errno;
#else
    if (parent == 0) {
        if (kill(pid, signal) < 0) err = errno;
    }
    else {
        if (tkill(pid, signal) < 0) err = errno;
    }
#endif

    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_signal_list(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, NULL);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    /* pid is ignored, same signal list for all */

    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        int i = 0;
        int n = 0;
        write_stream(&c->out, '[');
        for (i = 0; i < 32; i++) {
            char * name = signal_name(i);
            if (name != NULL) {
                if (n > 0) write_stream(&c->out, ',');
                write_stream(&c->out, '{');
                json_write_string(&c->out, "Name");
                write_stream(&c->out, ':');
                json_write_string(&c->out, name);
                write_stream(&c->out, ',');
                json_write_string(&c->out, "Code");
                write_stream(&c->out, ':');
                json_write_long(&c->out, i);
                write_stream(&c->out, '}');
                n++;
            }
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_signal_mask(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid;
    Context * ctx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, NULL);
    ctx = context_find_from_pid(pid);
    if (ctx == NULL) err = ERR_INV_CONTEXT;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (ctx == NULL) {
        write_stringz(&c->out, "null");
        write_stringz(&c->out, "null");
        write_stringz(&c->out, "null");
    }
    else {
        json_write_long(&c->out, ctx->sig_dont_stop);
        write_stream(&c->out, 0);
        json_write_long(&c->out, ctx->sig_dont_pass);
        write_stream(&c->out, 0);
        json_write_long(&c->out, ctx->pending_signals);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_set_signal_mask(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid;
    Context * ctx = NULL;
    int dont_stop;
    int dont_pass;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    dont_stop = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    dont_pass = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, NULL);
    ctx = context_find_from_pid(pid);
    if (ctx == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else {
        ctx->sig_dont_stop = dont_stop;
        ctx->sig_dont_pass = dont_pass;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
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

static ChildProcess * find_process(int pid) {
    LINK * qhp = &prs_list;
    LINK * qp = qhp->next;

    while (qp != qhp) {
        ChildProcess * prs = link2prs(qp);
        if (prs->pid == pid) return prs;
        qp = qp->next;
    }
    return NULL;
}

static void process_exited(ChildProcess * prs) {
    assert(prs->refs == 0);
    /* TODO: process exit code */
    send_event_process_exited(&prs->bcg->out, prs);
    flush_stream(&prs->bcg->out);
#if defined(_WRS_KERNEL)
    semTake(prs_list_lock, WAIT_FOREVER);
#endif
    list_remove(&prs->link);
    close(prs->inp);
    close(prs->out);
    close(prs->err);
    loc_free(prs);
#if defined(_WRS_KERNEL)
    semGive(prs_list_lock);
#endif
}

static void read_process_output_done(void * x) {
    AsyncReqInfo * req = x;
    ProcessOutput * out = (ProcessOutput *)req->client_data;
    ChildProcess * prs = out->prs;

    if ((int)req->u.fio.rval > 0) {
        send_event_process_output(&prs->bcg->out, out);
        flush_stream(&prs->bcg->out);
        async_req_post(req);
    }
    else {
        prs->refs--;
        if (prs->refs == 0) process_exited(prs);
        loc_free(out);
    }
}

static void read_process_output(ChildProcess * prs, int stream_id) {
    ProcessOutput * out = loc_alloc_zero(sizeof(ProcessOutput));
    out->prs = prs;
    out->stream_id = stream_id;
    out->req.client_data = out;
    out->req.done = read_process_output_done;
    out->req.type = AsyncReqRead;
    out->req.u.fio.bufp = out->buf;
    out->req.u.fio.bufsz = sizeof(out->buf);
    out->req.u.fio.fd = stream_id == 0 ? prs->out : prs->err;
    async_req_post(&out->req);
    prs->refs++;
}

#if defined(WIN32)

static int start_process(Channel * c, char ** envp, char * dir, char * exe, char ** args, int attach,
                int * pid, int * selfattach, ChildProcess ** prs) {
    typedef struct _SYSTEM_HANDLE_INFORMATION {
        ULONG Count;
        struct HANDLE_INFORMATION {
            USHORT ProcessId;
            USHORT CreatorBackTraceIndex;
            UCHAR ObjectTypeNumber;
            UCHAR Flags;
            USHORT Handle;
            PVOID Object;
            ACCESS_MASK GrantedAccess;
        } Handles[1];
    } SYSTEM_HANDLE_INFORMATION;
    FARPROC QuerySystemInformationProc = GetProcAddress(GetModuleHandle("NTDLL.DLL"), "NtQuerySystemInformation");
    DWORD size;
    NTSTATUS status;
    SYSTEM_HANDLE_INFORMATION * hi = NULL;
    int fpipes[3][2];
    HANDLE hpipes[3][2];
    char * cmd = NULL;
    int err = 0;
    int i;

    if (args != NULL) {
        int i = 0;
        int cmd_size = 0;
        int cmd_pos = 0;
#           define cmd_append(ch) { \
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
#       undef cmd_append
    }

    size = sizeof(SYSTEM_HANDLE_INFORMATION) * 16;
    hi = loc_alloc(size);
    while (1) {
        status = QuerySystemInformationProc(SystemHandleInformation, hi, size, &size);
        if (status != STATUS_INFO_LENGTH_MISMATCH) break;
        hi = loc_realloc(hi, size);
    }
    if (status == 0) {
        ULONG i;
        DWORD id = GetCurrentProcessId();
        for (i = 0; i < hi->Count; i++) {
            if (hi->Handles[i].ProcessId != id) continue;
            SetHandleInformation((HANDLE)(int)hi->Handles[i].Handle, HANDLE_FLAG_INHERIT, FALSE);
        }
    }
    else {
        err = set_win32_errno(status);
        trace(LOG_ALWAYS, "Can't start process '%s': %s", exe, errno_to_str(err));
    }
    loc_free(hi);

    memset(hpipes, 0, sizeof(hpipes));
    for (i = 0; i < 3; i++) fpipes[i][0] = fpipes[i][1] = -1;
    if (!err) {
#if defined(__CYGWIN__)
        for (i = 0; i < 3; i++) {
            if (pipe(fpipes[i]) < 0) {
                err = errno;
                break;
            }
            hpipes[i][0] = (HANDLE)get_osfhandle(fpipes[i][0]);
            hpipes[i][1] = (HANDLE)get_osfhandle(fpipes[i][1]);
        }
#else
        for (i = 0; i < 3; i++) {
            if (!CreatePipe(&hpipes[i][0], &hpipes[i][1], NULL, PIPE_SIZE)) {
                err = set_win32_errno(GetLastError());
                break;
            }
            fpipes[i][0] = _open_osfhandle((intptr_t)hpipes[i][0], O_TEXT);
            fpipes[i][1] = _open_osfhandle((intptr_t)hpipes[i][1], O_TEXT);
        }
#endif
    }
    if (!err) {
        STARTUPINFO si;
        PROCESS_INFORMATION prs_info;
        SetHandleInformation(hpipes[0][0], HANDLE_FLAG_INHERIT, TRUE);
        SetHandleInformation(hpipes[1][1], HANDLE_FLAG_INHERIT, TRUE);
        SetHandleInformation(hpipes[2][1], HANDLE_FLAG_INHERIT, TRUE);
        memset(&si, 0, sizeof(si));
        memset(&prs_info, 0, sizeof(prs_info));
        si.cb = sizeof(si);
        si.dwFlags |= STARTF_USESTDHANDLES;
        si.hStdInput  = hpipes[0][0];
        si.hStdOutput = hpipes[1][1];
        si.hStdError  = hpipes[2][1];
        if (CreateProcess(exe, cmd, NULL, NULL, TRUE, (attach ? CREATE_SUSPENDED : 0),
                (envp ? envp[0] : NULL), (dir[0] ? dir : NULL), &si, &prs_info) == 0)
        {
            err = set_win32_errno(GetLastError());
        }
        else {
            *pid = prs_info.dwProcessId;
            CloseHandle(prs_info.hThread);
            CloseHandle(prs_info.hProcess);
        }
    }
    close(fpipes[0][0]);
    close(fpipes[1][1]);
    close(fpipes[2][1]);
    if (!err) {
        *prs = loc_alloc_zero(sizeof(ChildProcess));
        (*prs)->inp = fpipes[0][1];
        (*prs)->out = fpipes[1][0];
        (*prs)->err = fpipes[2][0];
        (*prs)->pid = *pid;
        (*prs)->bcg = c->bcg;
        list_add_first(&(*prs)->link, &prs_list);
    }
    else {
        close(fpipes[0][1]);
        close(fpipes[1][0]);
        close(fpipes[2][0]);
    }
    loc_free(cmd);
    if (!err) return 0;
    trace(LOG_ALWAYS, "Can't start process '%s': %s", exe, errno_to_str(err));
    errno = err;
    return -1;
}

#elif defined(_WRS_KERNEL)

static void task_create_hook(WIND_TCB * tcb) {
    ChildProcess * prs;

    semTake(prs_list_lock, WAIT_FOREVER);
    prs = find_process(taskIdSelf());
    /* TODO: vxWork: standard IO inheritance */
    semGive(prs_list_lock);
}

static void task_delete_hook(WIND_TCB * tcb) {
    int i;
    ChildProcess * prs;

    semTake(prs_list_lock, WAIT_FOREVER);
    prs = find_process((UINT32)tcb);
    if (prs != NULL) {
        close(ioTaskStdGet(prs->pid, 1));
        close(ioTaskStdGet(prs->pid, 2));
        for (i = 0; i < 2; i++) {
            char pnm[32];
            snprintf(pnm, sizeof(pnm), "/pty/tcf-%08x-%d", prs->pid, i);
            ptyDevRemove(pnm);
        }
    }
    semGive(prs_list_lock);
}

static int start_process(Channel * c, char ** envp, char * dir, char * exe, char ** args, int attach,
                int * pid, int * selfattach, ChildProcess ** prs) {
    int err = 0;
    char * ptr;
    SYM_TYPE type;

    if (symFindByName(sysSymTbl, exe, &ptr, &type) != OK) {
        err = errno;
        if (err == S_symLib_SYMBOL_NOT_FOUND) err = ERR_SYM_NOT_FOUND;
        assert(err != 0);
    }
    else {
        int i;
        int pipes[2][2];
        /* TODO: arguments, environment */
        *pid = taskCreate("tTcf", 100, 0, 0x4000, (FUNCPTR)ptr, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        for (i = 0; i < 2; i++) {
            char pnm[32];
            char pnm_m[32];
            char pnm_s[32];
            snprintf(pnm, sizeof(pnm), "/pty/tcf-%08x-%d", *pid, i);
            snprintf(pnm_m, sizeof(pnm_m), "%sM", pnm);
            snprintf(pnm_s, sizeof(pnm_m), "%sS", pnm);
            if (ptyDevCreate(pnm, PIPE_SIZE, PIPE_SIZE) == ERROR) {
                err = errno;
                break;
            }
            pipes[i][0] = open(pnm_m, O_RDWR, 0);
            pipes[i][1] = open(pnm_s, O_RDWR, 0);
            if (pipes[i][0] < 0 || pipes[i][1] < 0) {
                err = errno;
                break;
            }
        }
        if (err) {
            taskDelete(*pid);
            *pid = 0;
        }
        else {
            semTake(prs_list_lock, WAIT_FOREVER);
            ioTaskStdSet(*pid, 0, pipes[0][1]);
            ioTaskStdSet(*pid, 1, pipes[0][1]);
            ioTaskStdSet(*pid, 2, pipes[1][1]);
            *prs = loc_alloc_zero(sizeof(ChildProcess));
            (*prs)->inp = pipes[0][0];
            (*prs)->out = pipes[0][0];
            (*prs)->err = pipes[1][0];
            (*prs)->pid = *pid;
            (*prs)->bcg = c->bcg;
            list_add_first(&(*prs)->link, &prs_list);
            if (attach) {
                taskStop(*pid);
                taskActivate(*pid);
                assert(taskIsStopped(*pid));
            }
            else {
                taskActivate(*pid);
            }
            semGive(prs_list_lock);
        }
    }
    if (!err) return 0;
    errno = err;
    return -1;
}

#else

static int start_process(Channel * c, char ** envp, char * dir, char * exe, char ** args, int attach,
                int * pid, int * selfattach, ChildProcess ** prs) {
    int err = 0;
    int p_inp[2];
    int p_out[2];
    int p_err[2];

    if (pipe(p_inp) < 0 || pipe(p_out) < 0 || pipe(p_err) < 0) err = errno;

    if (err == 0 && (p_inp[0] < 3 || p_out[1] < 3 || p_err[1] < 3)) {
        int fd0 = p_inp[0];
        int fd1 = p_out[1];
        int fd2 = p_err[1];
        if ((p_inp[0] = dup(p_inp[0])) < 0 ||
            (p_out[1] = dup(p_out[1])) < 0 ||
            (p_err[1] = dup(p_err[1])) < 0 ||
            close(fd0) < 0 ||
            close(fd1) < 0 ||
            close(fd2) < 0) err = errno;
    }

    if (!err) {
        *pid = fork();
        if (*pid < 0) err = errno;
        if (*pid == 0) {
            int fd = -1;
            int err = 0;

            if (err == 0) {
                fd = sysconf(_SC_OPEN_MAX);
                if (fd < 0) err = errno;
            }
            if (!err && dup2(p_inp[0], 0) < 0) err = errno;
            if (!err && dup2(p_out[1], 1) < 0) err = errno;
            if (!err && dup2(p_err[1], 2) < 0) err = errno;
            if (!err) {
                while (fd > 3) close(--fd);
            }
            if (!err && attach && context_attach_self() < 0) err = errno;
            if (!err) {
                execve(exe, args, envp);
                err = errno;
            }
            if (!attach) err = 1;
            else if (err < 1) err = EINVAL;
            else if (err > 0xff) err = EINVAL;
            exit(err);
        }
    }
    if (!err) {
        if (close(p_inp[0]) < 0 || close(p_out[1]) < 0 || close(p_err[1]) < 0) err = errno;
    }
    if (!err) {
        *prs = loc_alloc_zero(sizeof(ChildProcess));
        (*prs)->inp = p_inp[1];
        (*prs)->out = p_out[0];
        (*prs)->err = p_err[0];
        (*prs)->pid = *pid;
        (*prs)->bcg = c->bcg;
        list_add_first(&(*prs)->link, &prs_list);
    }

    *selfattach = 1;

    if (!err) return 0;
    errno = err;
    return -1;
}

#endif

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
    ChildProcess * prs = NULL;
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
        if (err == 0 && start_process(c, envp, dir, exe, args, attach, &pid, &selfattach, &prs) < 0) err = errno;
        if (prs != NULL) {
            read_process_output(prs, 0);
            read_process_output(prs, 1);
        }
        if (attach && !err) {
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
#if defined(_WRS_KERNEL)
    prs_list_lock = semMCreate(SEM_Q_PRIORITY);
    if (prs_list_lock == NULL) check_error(errno);
    if (taskCreateHookAdd((FUNCPTR)task_create_hook) != OK) check_error(errno);
    if (taskDeleteHookAdd((FUNCPTR)task_delete_hook) != OK) check_error(errno);
#endif
    list_init(&prs_list);
    add_command_handler(proto, PROCESSES, "getContext", command_get_context);
    add_command_handler(proto, PROCESSES, "getChildren", command_get_children);
    add_command_handler(proto, PROCESSES, "attach", command_attach);
    add_command_handler(proto, PROCESSES, "detach", command_detach);
    add_command_handler(proto, PROCESSES, "terminate", command_terminate);
    add_command_handler(proto, PROCESSES, "signal", command_signal);
    add_command_handler(proto, PROCESSES, "getSignalList", command_get_signal_list);
    add_command_handler(proto, PROCESSES, "getSignalMask", command_get_signal_mask);
    add_command_handler(proto, PROCESSES, "setSignalMask", command_set_signal_mask);
    add_command_handler(proto, PROCESSES, "getEnvironment", command_get_environment);
    add_command_handler(proto, PROCESSES, "start", command_start);
}

#endif


