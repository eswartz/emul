/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Intel - implemented terminals service
 *******************************************************************************/

/*
 * TCF Terminals service implementation.
 */

#include <config.h>

#if SERVICE_Terminals

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <assert.h>
#include <framework/myalloc.h>
#include <framework/protocol.h>
#include <framework/trace.h>
#include <framework/context.h>
#include <framework/json.h>
#include <framework/asyncreq.h>
#include <framework/exceptions.h>
#include <framework/waitpid.h>
#include <framework/signames.h>
#include <services/streamsservice.h>
#include <services/terminals.h>

#ifndef TERMINALS_NO_LOGIN
#define TERMINALS_NO_LOGIN 1
#endif

static const char * TERMINALS = "Terminals";

#if defined(WIN32)
#  include <tlhelp32.h>
#  ifdef _MSC_VER
#    pragma warning(disable:4201) /* nonstandard extension used : nameless struct/union (in winternl.h) */
#    include <winternl.h>
#  else
#    include <ntdef.h>
#  endif
#  ifndef STATUS_INFO_LENGTH_MISMATCH
#   define STATUS_INFO_LENGTH_MISMATCH      ((NTSTATUS)0xC0000004L)
#  endif
#  ifndef SystemHandleInformation
#    define SystemHandleInformation 16
#  endif
#  define TERM_LAUNCH_EXEC "cmd"
#  define TERM_LAUNCH_ARGS {TERM_LAUNCH_EXEC, NULL}
    struct winsize {
        unsigned ws_col;
        unsigned ws_row;
    };
#else
#  include <termios.h>
#  ifndef TIOCGWINSZ
#    include <sys/ioctl.h>
#  endif
#  include <sys/stat.h>
#  include <unistd.h>
#  include <dirent.h>
# if TERMINALS_NO_LOGIN
#  define TERM_LAUNCH_EXEC "/bin/bash"
#  define TERM_LAUNCH_ARGS {TERM_LAUNCH_EXEC, "-l", NULL}
#  define TERM_EXIT_SIGNAL SIGHUP
# else
#  define TERM_LAUNCH_EXEC "/bin/login"
#  define TERM_LAUNCH_ARGS {TERM_LAUNCH_EXEC, "-p", NULL}
#  define TERM_EXIT_SIGNAL SIGTERM
# endif
#endif

#define PIPE_SIZE 0x1000
#define TERM_PROP_DEF_SIZE 256

typedef struct Terminal {
    LINK link;
    int pid; /* pid of the login process of the terminal */
    TCFBroadcastGroup * bcg;
    struct TerminalInput * inp_struct;
    struct TerminalOutput * out_struct;
    struct TerminalOutput * err_struct;

    char pty_type[TERM_PROP_DEF_SIZE];
    char encoding[TERM_PROP_DEF_SIZE];
    unsigned long width;
    unsigned long height;
    int terminated;
    long exit_code;

    Channel * channel;
} Terminal;

typedef struct TerminalOutput {
    int fd;
    char id[256];
    Terminal * prs;
    AsyncReqInfo req;
    int req_posted;
    char buf[PIPE_SIZE];
    size_t buf_pos;
    int eos;
    VirtualStream * vstream;
} TerminalOutput;

typedef struct TerminalInput {
    int fd;
    char id[256];
    Terminal * prs;
    AsyncReqInfo req;
    int req_posted;
    char buf[PIPE_SIZE];
    size_t buf_pos;
    size_t buf_len;
    int eos;
    VirtualStream * vstream;
} TerminalInput;

#define link2term(A)  ((Terminal *)((char *)(A) - offsetof(Terminal, link)))

static LINK terms_list;

static Terminal * find_terminal(int pid) {
    LINK * qhp = &terms_list;
    LINK * qp = qhp->next;

    while (qp != qhp) {
        Terminal * prs = link2term(qp);
        if (prs->pid == pid) return prs;
        qp = qp->next;
    }
    return NULL;
}

static char * tid2id(int tid) {
    static char s[64];
    char * p = s + sizeof(s);
    unsigned long n = (long) tid;
    *(--p) = 0;
    do {
        *(--p) = (char) (n % 10 + '0');
        n = n / 10;
    }
    while (n != 0);

    *(--p) = 'T';
    return p;
}

static int id2tid(const char * id) {
    int tid = 0;
    if (id == NULL) return 0;
    if (id[0] != 'T') return 0;
    if (id[1] == 0) return 0;
    tid = (unsigned) strtol(id + 1, (char **) &id, 10);
    if (id[0] != 0) return 0;
    return tid;
}

static void write_context(OutputStream * out, int tid) {
    Terminal * prs = find_terminal(tid);

    write_stream(out, '{');

    if (prs != NULL) {
        json_write_string(out, "ProcessID");
        write_stream(out, ':');
        json_write_string(out, pid2id(prs->pid, 0));
        write_stream(out, ',');

        if (*prs->pty_type) {
            json_write_string(out, "PtyType");
            write_stream(out, ':');
            json_write_string(out, prs->pty_type);
            write_stream(out, ',');
        }

        if (*prs->encoding) {
            json_write_string(out, "Encoding");
            write_stream(out, ':');
            json_write_string(out, prs->encoding);
            write_stream(out, ',');
        }

        json_write_string(out, "Width");
        write_stream(out, ':');
        json_write_ulong(out, prs->width);
        write_stream(out, ',');

        json_write_string(out, "Height");
        write_stream(out, ':');
        json_write_ulong(out, prs->height);
        write_stream(out, ',');

        if (prs->inp_struct) {
            json_write_string(out, "StdInID");
            write_stream(out, ':');
            json_write_string(out, prs->inp_struct->id);
            write_stream(out, ',');
        }
        if (prs->out_struct) {
            json_write_string(out, "StdOutID");
            write_stream(out, ':');
            json_write_string(out, prs->out_struct->id);
            write_stream(out, ',');
        }
        if (prs->err_struct) {
            json_write_string(out, "StdErrID");
            write_stream(out, ':');
            json_write_string(out, prs->err_struct->id);
            write_stream(out, ',');
        }
    }

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, tid2id(tid));

    write_stream(out, '}');
}

static void send_event_terminal_exited(OutputStream * out, Terminal * prs) {
    write_stringz(out, "E");
    write_stringz(out, TERMINALS);
    write_stringz(out, "exited");

    json_write_string(out, tid2id(prs->pid));
    write_stream(out, 0);

    json_write_ulong(out, prs->exit_code);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_terminal_win_size_changed(OutputStream * out, Terminal * prs) {
    write_stringz(out, "E");
    write_stringz(out, TERMINALS);
    write_stringz(out, "winSizeChanged");

    json_write_string(out, tid2id(prs->pid));
    write_stream(out, 0);

    json_write_long(out, prs->width);
    write_stream(out, 0);

    json_write_long(out, prs->height);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static int kill_term(Terminal * term) {
    int err = 0;

#if defined(WIN32)
    HANDLE h = OpenProcess(PROCESS_TERMINATE, FALSE, term->pid);
    if (h == NULL) {
        err = set_win32_errno(GetLastError());
    }
    else {
        if (!TerminateProcess(h, 1)) err = set_win32_errno(GetLastError());
        if (!CloseHandle(h) && !err) err = set_win32_errno(GetLastError());
    }
#else
    if (kill(term->pid, TERM_EXIT_SIGNAL) < 0) err = errno;
#endif
    term->terminated = 1;
    return err;
}

static void command_exit(char * token, Channel * c) {
    int err = 0;
    char id[256];
    unsigned tid;
    Terminal * term = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    tid = id2tid(id);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (tid == 0 || (term = find_terminal(tid)) == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else {
        err = kill_term(term);
    }

    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void terminal_exited(Terminal * prs) {
    Trap trap;

    if (set_trap(&trap)) {
        send_event_terminal_exited(&prs->bcg->out, prs);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception sending terminal exited event: %d %s", trap.error,
                errno_to_str(trap.error));
    }

    list_remove(&prs->link);
    if (prs->inp_struct) {
        TerminalInput * inp = prs->inp_struct;
        if (!inp->req_posted) {
            virtual_stream_delete(inp->vstream);
            close(inp->fd);
            loc_free(inp);
        }
        else {
            inp->prs = NULL;
        }
    }
    if (prs->out_struct) prs->out_struct->prs = NULL;
    if (prs->err_struct) prs->err_struct->prs = NULL;
    channel_unlock(prs->channel);
    loc_free(prs);
}

static void terminal_input_streams_callback(VirtualStream * stream, int event_code, void * args) {
    TerminalInput * inp = (TerminalInput *) args;

    assert(inp->vstream == stream);
    if (!inp->req_posted) {
        if (inp->buf_pos >= inp->buf_len && !inp->eos) {
            inp->buf_pos = inp->buf_len = 0;
            virtual_stream_get_data(stream, inp->buf, sizeof(inp->buf), &inp->buf_len, &inp->eos);
        }
        if (inp->buf_pos < inp->buf_len) {
            inp->req.u.fio.bufp = inp->buf + inp->buf_pos;
            inp->req.u.fio.bufsz = inp->buf_len - inp->buf_pos;
            inp->req_posted = 1;
            async_req_post(&inp->req);
        }
    }
}

static void write_terminal_input_done(void * x) {
    AsyncReqInfo * req = (AsyncReqInfo *) x;
    TerminalInput * inp = (TerminalInput *) req->client_data;

    inp->req_posted = 0;
    if (inp->prs == NULL) {
        /* Process has exited */
        virtual_stream_delete(inp->vstream);
        close(inp->fd);
        loc_free(inp);
    }
    else {
        int wr = inp->req.u.fio.rval;

        if (wr < 0) {
            int err = inp->req.error;
            trace(LOG_ALWAYS, "Can't write terminal input stream: %d %s", err, errno_to_str(err));
            inp->buf_pos = inp->buf_len = 0;
        }
        else {
            inp->buf_pos += wr;
        }

        terminal_input_streams_callback(inp->vstream, 0, inp);
    }
}

static TerminalInput * write_terminal_input(Terminal * prs, int fd) {
    TerminalInput * inp = (TerminalInput *) loc_alloc_zero(sizeof(TerminalInput));
    inp->fd = fd;
    inp->prs = prs;
    inp->req.client_data = inp;
    inp->req.done = write_terminal_input_done;
    inp->req.type = AsyncReqWrite;
    inp->req.u.fio.fd = inp->fd;
    virtual_stream_create(TERMINALS, tid2id(prs->pid), PIPE_SIZE, VS_ENABLE_REMOTE_WRITE,
            terminal_input_streams_callback, inp, &inp->vstream);
    virtual_stream_get_id(inp->vstream, inp->id, sizeof(inp->id));
    return inp;
}

static void terminal_output_streams_callback(VirtualStream * stream, int event_code, void * args) {
    TerminalOutput * out = (TerminalOutput *) args;

    assert(out->vstream == stream);
    if (!out->req_posted) {
        int buf_len = out->req.u.fio.rval;
        int err = 0;
        int eos = 0;

        if (buf_len < 0) {
            buf_len = 0;
            err = out->req.error;
        }
        if (buf_len == 0) eos = 1;
        if (out->prs == NULL) {
            eos = 1;
            err = 0;
        }

        assert(buf_len <= (int) sizeof(out->buf));
        assert(out->buf_pos <= (size_t) buf_len);
        assert(out->req.u.fio.bufp == out->buf);
#ifdef __linux__
        if (err == EIO)
        err = 0;
#endif
        if (err) trace(LOG_ALWAYS, "Can't read terminal output stream: %d %s", err, errno_to_str(
                err));

        if (out->buf_pos < (size_t) buf_len || out->eos != eos) {
            size_t done = 0;
            virtual_stream_add_data(stream, out->buf + out->buf_pos, buf_len - out->buf_pos, &done, eos);
            out->buf_pos += done;
            if (eos) out->eos = 1;
        }

        if (out->buf_pos >= (size_t) buf_len) {
            if (!eos) {
                out->req_posted = 1;
                async_req_post(&out->req);
            }
            else if (virtual_stream_is_empty(stream)) {
                if (out->prs != NULL) {
                    if (out == out->prs->out_struct) out->prs->out_struct = NULL;
                    if (out == out->prs->err_struct) out->prs->err_struct = NULL;
                }
                virtual_stream_delete(stream);
                close(out->fd);
                loc_free(out);
            }
        }
    }
}

static void read_terminal_output_done(void * x) {
    AsyncReqInfo * req = (AsyncReqInfo *) x;
    TerminalOutput * out = (TerminalOutput *) req->client_data;

    out->buf_pos = 0;
    out->req_posted = 0;
    terminal_output_streams_callback(out->vstream, 0, out);
}

static TerminalOutput * read_terminal_output(Terminal * prs, int fd) {
    TerminalOutput * out = (TerminalOutput *)loc_alloc_zero(sizeof(TerminalOutput));
    out->fd = fd;
    out->prs = prs;
    out->req.client_data = out;
    out->req.done = read_terminal_output_done;
    out->req.type = AsyncReqRead;
    out->req.u.fio.bufp = out->buf;
    out->req.u.fio.bufsz = sizeof(out->buf);
    out->req.u.fio.fd = fd;
    virtual_stream_create(TERMINALS, tid2id(prs->pid), PIPE_SIZE, VS_ENABLE_REMOTE_READ,
            terminal_output_streams_callback, out, &out->vstream);
    virtual_stream_get_id(out->vstream, out->id, sizeof(out->id));
    out->req_posted = 1;
    async_req_post(&out->req);
    return out;
}

#if !defined(WIN32)
/*
 * Set the environment variable "name" to the value "value". If the variable
 * exists already, override it or just skip.
 */
static void envp_add(char *** envp, int * env_len, const char * name, const char * value, int env_override) {
    char **env;
    size_t len;
    int i;

    assert(*envp || *env_len == 0);
    assert(name);
    assert(value);

    if (*envp == NULL && *env_len == 0) {
        *envp = (char **)loc_alloc(sizeof(char *));
        *envp[0] = NULL;
        *env_len = 1;
    }

    for (env = *envp, i = 0, len = strlen(name); env[i]; i++)
        if (strncmp(env[i], name, len) == 0 && env[i][len] == '=') break;
    if (env[i]) {
        /* override */
        if (env_override) loc_free(env[i]);
        else return;
    }
    else {
        /* new variable */
        if (i >= *env_len - 1) {
            *env_len += 10;
            env = *envp = (char **)loc_realloc(env, *env_len * sizeof(char *));
        }
        env[i + 1] = NULL;
    }
    env[i] = (char *)loc_alloc_zero(len + 1 + strlen(value) + 1);
    snprintf(env[i], len + 1 + strlen(value) + 1, "%s=%s", name, value);
}

static void set_terminal_env(char *** envp, int * env_len, const char * pty_type,
        const char * encoding, const char * exe) {
#if TERMINALS_NO_LOGIN
    char * value;
    const char *env_array[] = { "USER", "LOGNAME", "HOME", "PATH", NULL };
#endif
    int i = 0;
    char ** new_envp = NULL;

    /* convert the envp memory layout */
    new_envp = (char **)loc_alloc((*env_len + 1) * sizeof(char *));
    for (i = 0; i < *env_len; i++) {
        new_envp[i] = (char *)loc_alloc(strlen((*envp)[i]) + 1);
        memcpy(new_envp[i], (*envp)[i], strlen((*envp)[i]) + 1);
    }
    new_envp[i] = NULL;
    loc_free(*envp);

    *envp = new_envp;
    *env_len = i + 1;

    if (*pty_type) envp_add(envp, env_len, "TERM", pty_type, 1);
    if (*encoding) envp_add(envp, env_len, "LANG", encoding, 1);
    envp_add(envp, env_len, "SHELL", exe, 1);

#if TERMINALS_NO_LOGIN
    i = 0;
    while (env_array[i]) {
        value = getenv(env_array[i]);
        if (value) envp_add(envp, env_len, env_array[i], value, 0);
        ++i;
    }
#endif
}

static void env_free(char ** envp, int envp_len) {
    if (envp) {
        int i;
        for (i = 0; i < envp_len && envp[i]; i++) loc_free(envp[i]);
        loc_free(envp);
    }
}

#endif

#if defined(WIN32)

static int start_terminal(Channel * c, const char * pty_type, const char * encoding, char ** envp,
        int envp_len, const char * exe, const char ** args, int * pid, Terminal ** prs) {
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
    typedef NTSTATUS (FAR WINAPI * QuerySystemInformationTypedef)(int, PVOID, ULONG, PULONG);
    QuerySystemInformationTypedef QuerySystemInformationProc = (QuerySystemInformationTypedef)GetProcAddress(
        GetModuleHandle("NTDLL.DLL"), "NtQuerySystemInformation");
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
            const char * p = args[i++];
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
    hi = (SYSTEM_HANDLE_INFORMATION *)loc_alloc(size);
    for (;;) {
        status = QuerySystemInformationProc(SystemHandleInformation, hi, size, &size);
        if (status != STATUS_INFO_LENGTH_MISMATCH) break;
        hi = (SYSTEM_HANDLE_INFORMATION *)loc_realloc(hi, size);
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
        if (CreateProcess(NULL, cmd, NULL, NULL, TRUE, 0,
                (envp ? envp[0] : NULL), NULL, &si, &prs_info) == 0)
        {
            err = set_win32_errno(GetLastError());
        }
        else {
            *pid = prs_info.dwProcessId;
            if (!CloseHandle(prs_info.hThread)) err = set_win32_errno(GetLastError());
            if (!CloseHandle(prs_info.hProcess)) err = set_win32_errno(GetLastError());
        }
    }
    if (close(fpipes[0][0]) < 0 && !err) err = errno;
    if (close(fpipes[1][1]) < 0 && !err) err = errno;
    if (close(fpipes[2][1]) < 0 && !err) err = errno;
    if (!err) {
        *prs = (Terminal *)loc_alloc_zero(sizeof(Terminal));
        (*prs)->pid = *pid;
        (*prs)->bcg = c->bcg;
        (*prs)->channel = c;
        if (*pty_type) snprintf((*prs)->pty_type, sizeof((*prs)->pty_type), "%s", pty_type);
        if (*encoding) snprintf((*prs)->encoding, sizeof((*prs)->encoding), "%s", encoding);
        (*prs)->inp_struct = write_terminal_input(*prs, fpipes[0][1]);
        (*prs)->out_struct = read_terminal_output(*prs, fpipes[1][0]);
        (*prs)->err_struct = read_terminal_output(*prs, fpipes[2][0]);
        list_add_first(&(*prs)->link, &terms_list);
        channel_lock(c);
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

#else

static int start_terminal(Channel * c, const char * pty_type, const char * encoding, char ** envp,
        int envp_len, const char * exe, const char ** args, int * pid, Terminal ** prs) {
    int err = 0;
    int fd_tty_master = -1;
    int fd_tty_out = -1;
    char * tty_slave_name = NULL;
    struct winsize size;

    memset(&size, 0, sizeof(struct winsize));
    fd_tty_master = posix_openpt(O_RDWR | O_NOCTTY);
    if (fd_tty_master < 0 || grantpt(fd_tty_master) < 0 || unlockpt(fd_tty_master) < 0) err = errno;
    if (!err) {
        tty_slave_name = ptsname(fd_tty_master);
        if (tty_slave_name == NULL) err = EINVAL;
    }

    if (ioctl(fd_tty_master, TIOCGWINSZ, &size) < 0 || size.ws_col <= 0 || size.ws_row <= 0) {
        size.ws_col = 80;
        size.ws_row = 24;
    }

    if (!err && fd_tty_master < 3) {
        int fd0 = fd_tty_master;
        if ((fd_tty_master = dup(fd_tty_master)) < 0 || close(fd0)) err = errno;
    }

    if (!err) {
        *pid = fork();
        if (*pid < 0) err = errno;
        if (*pid == 0) {
            int fd = -1;
            int fd_tty_slave = -1;
            char *path;

            set_terminal_env(&envp, &envp_len, pty_type, encoding, exe);
            path=getenv("HOME");
            if (path && chdir(path) < 0) err = errno;
            setsid();

            if (!err && (fd = sysconf(_SC_OPEN_MAX)) < 0) err = errno;
            if (!err && (fd_tty_slave = open(tty_slave_name, O_RDWR)) < 0) err = errno;
#if defined(TIOCSCTTY)
            if (!err && (ioctl(fd_tty_slave, TIOCSCTTY, NULL)) < 0) err = errno;
#endif
            if (!err && dup2(fd_tty_slave, 0) < 0) err = errno;
            if (!err && dup2(fd_tty_slave, 1) < 0) err = errno;
            if (!err && dup2(fd_tty_slave, 2) < 0) err = errno;
            while (!err && fd > 3) close(--fd);
            if (!err) {
                execve(exe, (char **)args, envp);
                err = errno;
            }
            if (envp) env_free(envp,envp_len);
            fprintf(stderr, "Cannot start %s: %s\n", exe, errno_to_str(err));
            exit(1);
        }
    }

    if ((fd_tty_out = dup(fd_tty_master)) < 0) err = errno;

    if (!err) {
        *prs = (Terminal *)loc_alloc_zero(sizeof(Terminal));
        (*prs)->pid = *pid;
        (*prs)->bcg = c->bcg;
        (*prs)->channel = c;
        if (*pty_type) snprintf((*prs)->pty_type, sizeof((*prs)->pty_type), "%s", pty_type);
        if (*encoding) snprintf((*prs)->encoding, sizeof((*prs)->encoding), "%s", encoding);
        (*prs)->width = size.ws_row;
        (*prs)->height = size.ws_col;
        (*prs)->inp_struct = write_terminal_input(*prs, fd_tty_master);
        (*prs)->out_struct = read_terminal_output(*prs, fd_tty_out);
        list_add_first(&(*prs)->link, &terms_list);
        channel_lock(c);
    }
    else if (fd_tty_master >= 0) {
        close(fd_tty_master);
    }

    if (!err) return 0;
    errno = err;
    return -1;
}

#endif

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    int tid;
    Terminal * term = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    tid = id2tid(id);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (tid == 0 || (term = find_terminal(tid)) == NULL) {
        err = ERR_INV_CONTEXT;
    }

    write_errno(&c->out, err);
    if (term != NULL) {
        write_context(&c->out, tid);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_launch(char * token, Channel * c) {
    int pid = 0;
    int err = 0;
    char encoding[TERM_PROP_DEF_SIZE];
    char pty_type[TERM_PROP_DEF_SIZE];
    const char * args[] = TERM_LAUNCH_ARGS;
    const char * exec = TERM_LAUNCH_EXEC;

    char ** envp = NULL;
    int envp_len = 0;

    Terminal * prs = NULL;
    Trap trap;

    if (set_trap(&trap)) {
        json_read_string(&c->inp, pty_type, sizeof(pty_type));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        json_read_string(&c->inp, encoding, sizeof(encoding));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        envp = json_read_alloc_string_array(&c->inp, &envp_len);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if !defined(WIN32)
        {
            char fnm[FILE_PATH_SIZE];
            struct stat st;
            if (err == 0 && stat(exec, &st) != 0) {
                int n = errno;
                /* On some systems (e.g. Free DSB) bash is installed under /usr/local */
                assert(exec[0] == '/');
                snprintf(fnm, sizeof(fnm), "/usr/local%s", exec);
                if (stat(fnm, &st) == 0) {
                    args[0] = exec = fnm;
                }
                else {
                    err = n;
                }
            }
        }
#endif

        if (err == 0 && start_terminal(
                c, pty_type, encoding, envp, envp_len, exec,
                args, &pid, &prs) < 0) err = errno;

        if (!err) add_waitpid_process(pid);

        /* write result back */
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
        clear_trap(&trap);
    }

    loc_free(envp);

    if (trap.error) exception(trap.error);
}

static void command_set_win_size(char * token, Channel * c) {
    int err = 0;
    struct winsize size;
    char id[256];
    unsigned tid;
    Terminal * term = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    size.ws_col = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    size.ws_row = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    tid = id2tid(id);

    if (tid == 0 || (term = find_terminal(tid)) == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else if (term->width != size.ws_col || term->height != size.ws_row) {
#if defined(WIN32)
#else
        if (ioctl(term->inp_struct->fd, TIOCSWINSZ, &size) < 0) err = errno;
#endif
        if (!err) {
            term->width = size.ws_col;
            term->height = size.ws_row;
            send_event_terminal_win_size_changed(&term->bcg->out, term);
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);

}

static void waitpid_listener(int pid, int exited, int exit_code, int signal, int event_code,
        int syscall, void * args) {
    if (exited) {
        Terminal * prs = find_terminal(pid);
        if (prs) {
            if (signal != 0) prs->exit_code = -signal;
            else prs->exit_code = exit_code;
            terminal_exited(prs);
        }
    }
}

static void channel_close_listener(Channel * c) {
    LINK * l = NULL;

    for (l = terms_list.next; l != &terms_list;) {
        Terminal * term = link2term(l);
        l = l->next;
        if (term->channel == c && !term->terminated) {
            trace(LOG_ALWAYS, "Terminal is left launched: T%d", term->pid);
            kill_term(term);
        }
    }
}

void ini_terminals_service(Protocol * proto) {
    list_init(&terms_list);

    add_waitpid_listener(waitpid_listener, NULL);
    add_channel_close_listener(channel_close_listener);

    add_command_handler(proto, TERMINALS, "getContext", command_get_context);
    add_command_handler(proto, TERMINALS, "launch", command_launch);
    add_command_handler(proto, TERMINALS, "exit", command_exit);
    add_command_handler(proto, TERMINALS, "setWinSize", command_set_win_size);
}

#endif /* SERVICE_Terminals */
