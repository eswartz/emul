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
#include <services/processes.h>
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

#define TERM_PROP_DEF_SIZE 256

typedef struct Terminal {
    LINK link;
    TCFBroadcastGroup * bcg;
    ChildProcess * prs;

    char pty_type[TERM_PROP_DEF_SIZE];
    char encoding[TERM_PROP_DEF_SIZE];
    unsigned long width;
    unsigned long height;
    int terminated;

    Channel * channel;
} Terminal;

#define link2term(A)  ((Terminal *)((char *)(A) - offsetof(Terminal, link)))

static LINK terms_list;

static Terminal * find_terminal(int pid) {
    LINK * qhp = &terms_list;
    LINK * qp = qhp->next;

    while (qp != qhp) {
        Terminal * term = link2term(qp);
        if (get_process_pid(term->prs) == pid) return term;
        qp = qp->next;
    }
    return NULL;
}

static char * tid2id(int tid) {
    static char s[64];
    char * p = s + sizeof(s);
    unsigned long n = (long)tid;
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
    Terminal * term = find_terminal(tid);
    const char * id = NULL;

    write_stream(out, '{');

    if (term != NULL) {
        json_write_string(out, "ProcessID");
        write_stream(out, ':');
        json_write_string(out, pid2id(get_process_pid(term->prs), 0));
        write_stream(out, ',');

        if (*term->pty_type) {
            json_write_string(out, "PtyType");
            write_stream(out, ':');
            json_write_string(out, term->pty_type);
            write_stream(out, ',');
        }

        if (*term->encoding) {
            json_write_string(out, "Encoding");
            write_stream(out, ':');
            json_write_string(out, term->encoding);
            write_stream(out, ',');
        }

        json_write_string(out, "Width");
        write_stream(out, ':');
        json_write_ulong(out, term->width);
        write_stream(out, ',');

        json_write_string(out, "Height");
        write_stream(out, ':');
        json_write_ulong(out, term->height);
        write_stream(out, ',');

        id = get_process_stream_id(term->prs, 0);
        if (id) {
            json_write_string(out, "StdInID");
            write_stream(out, ':');
            json_write_string(out, id);
            write_stream(out, ',');
        }
        id = get_process_stream_id(term->prs, 1);
        if (id) {
            json_write_string(out, "StdOutID");
            write_stream(out, ':');
            json_write_string(out, id);
            write_stream(out, ',');
        }
        id = get_process_stream_id(term->prs, 2);
        if (id) {
            json_write_string(out, "StdErrID");
            write_stream(out, ':');
            json_write_string(out, id);
            write_stream(out, ',');
        }
    }

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, tid2id(tid));

    write_stream(out, '}');
}

static void send_event_terminal_exited(OutputStream * out, Terminal * term) {
    write_stringz(out, "E");
    write_stringz(out, TERMINALS);
    write_stringz(out, "exited");

    json_write_string(out, tid2id(get_process_pid(term->prs)));
    write_stream(out, 0);

    json_write_ulong(out, get_process_exit_code(term->prs));
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_terminal_win_size_changed(OutputStream * out, Terminal * term) {
    write_stringz(out, "E");
    write_stringz(out, TERMINALS);
    write_stringz(out, "winSizeChanged");

    json_write_string(out, tid2id(get_process_pid(term->prs)));
    write_stream(out, 0);

    json_write_long(out, term->width);
    write_stream(out, 0);

    json_write_long(out, term->height);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static int kill_term(Terminal * term) {
    int err = 0;
    int pid = get_process_pid(term->prs);

#if defined(WIN32)
    HANDLE h = OpenProcess(PROCESS_TERMINATE, FALSE, pid);
    if (h == NULL) {
        err = set_win32_errno(GetLastError());
    }
    else {
        if (!TerminateProcess(h, 1)) err = set_win32_errno(GetLastError());
        if (!CloseHandle(h) && !err) err = set_win32_errno(GetLastError());
    }
#else
    if (kill(pid, get_process_out_state(term->prs) ? TERM_EXIT_SIGNAL : SIGKILL) < 0) err = errno;
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

static void terminal_exited(void * args) {
    Terminal * term = (Terminal *)args;
    Trap trap;

    if (set_trap(&trap)) {
        send_event_terminal_exited(&term->bcg->out, term);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception sending terminal exited event: %d %s",
            trap.error, errno_to_str(trap.error));
    }

    list_remove(&term->link);
    channel_unlock(term->channel);
    loc_free(term);
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
    int err = 0;
    char encoding[TERM_PROP_DEF_SIZE];
    char pty_type[TERM_PROP_DEF_SIZE];
    const char * args[] = TERM_LAUNCH_ARGS;
    const char * exec = TERM_LAUNCH_EXEC;

    int envp_len = 0;

    int selfattach = 0;
    ProcessStartParams prms;
    Terminal * term = (Terminal *)loc_alloc_zero(sizeof(Terminal));
    Trap trap;

    if (set_trap(&trap)) {
        memset(&prms, 0, sizeof(prms));
        json_read_string(&c->inp, pty_type, sizeof(pty_type));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        json_read_string(&c->inp, encoding, sizeof(encoding));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        prms.envp = json_read_alloc_string_array(&c->inp, &envp_len);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if !defined(WIN32)
        {
            struct stat st;
            if (err == 0 && stat(exec, &st) != 0) {
                int n = errno;
                static char fnm[FILE_PATH_SIZE];
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
        set_terminal_env(&prms.envp, &envp_len, pty_type, encoding, exec);
        prms.dir = getenv("HOME");
#else
        {
            const char * home_drv = getenv("HOMEDRIVE");
            const char * home_dir = getenv("HOMEPATH");
            if (home_drv && home_dir) {
                static char fnm[FILE_PATH_SIZE];
                snprintf(fnm, sizeof(fnm), "%s%s", home_drv, home_dir);
                prms.dir = fnm;
            }
        }
#endif

        prms.exe = exec;
        prms.args = (char **)args;
        prms.service = TERMINALS;
        prms.use_terminal = 1;
        prms.exit_cb = terminal_exited;
        prms.exit_args = term;

        if (err == 0 && start_process(c, &prms, &selfattach, &term->prs) < 0) err = errno;

        if (!err) {
#if !defined(WIN32)
            struct winsize size;
            int tty = get_process_tty(term->prs);
            memset(&size, 0, sizeof(struct winsize));
            if (tty < 0 || ioctl(tty, TIOCGWINSZ, &size) < 0 || size.ws_col <= 0 || size.ws_row <= 0) {
                size.ws_col = 80;
                size.ws_row = 24;
            }
            term->width = size.ws_row;
            term->height = size.ws_col;
#endif
            term->bcg = c->bcg;
            channel_lock(term->channel = c);
            strlcpy(term->pty_type, pty_type, sizeof(term->pty_type));
            strlcpy(term->encoding, encoding, sizeof(term->encoding));
            list_add_first(&term->link, &terms_list);
            assert(find_terminal(get_process_pid(term->prs)) == term);
        }
        else {
            assert(term->prs == NULL);
            loc_free(term);
        }

        /* write result back */
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_errno(&c->out, err);
        if (err) {
            write_stringz(&c->out, "null");
        }
        else {
            write_context(&c->out, get_process_pid(term->prs));
            write_stream(&c->out, 0);
        }
        write_stream(&c->out, MARKER_EOM);
        clear_trap(&trap);
    }

    loc_free(prms.envp);

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
        int tty = get_process_tty(term->prs);
        if (ioctl(tty, TIOCSWINSZ, &size) < 0) err = errno;
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

static void channel_close_listener(Channel * c) {
    LINK * l = NULL;

    for (l = terms_list.next; l != &terms_list;) {
        Terminal * term = link2term(l);
        l = l->next;
        if (term->channel == c && !term->terminated) {
            trace(LOG_ALWAYS, "Terminal is left launched: %s", tid2id(get_process_pid(term->prs)));
            kill_term(term);
        }
    }
}

void ini_terminals_service(Protocol * proto) {
    list_init(&terms_list);

    add_channel_close_listener(channel_close_listener);

    add_command_handler(proto, TERMINALS, "getContext", command_get_context);
    add_command_handler(proto, TERMINALS, "launch", command_launch);
    add_command_handler(proto, TERMINALS, "exit", command_exit);
    add_command_handler(proto, TERMINALS, "setWinSize", command_set_win_size);
}

#endif /* SERVICE_Terminals */
