/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 *******************************************************************************/

/*
 * Agent main module.
 */

#include <config.h>

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include <framework/asyncreq.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/channel_tcp.h>
#include <framework/plugins.h>
#include <services/discovery.h>
#include <main/test.h>
#include <main/cmdline.h>
#include <main/services.h>
#include <main/server.h>

static const char * progname;

static void shutdown_event(void * args) {
    discovery_stop();
    cancel_event_loop();
}

static void signal_handler(int sig) {
    if (is_dispatch_thread()) {
        discovery_stop();
        signal(sig, SIG_DFL);
        raise(sig);
    }
    else {
        post_event(shutdown_event, NULL);
    }
}

#if defined(WIN32)
static BOOL CtrlHandler(DWORD ctrl) {
    switch(ctrl) {
    case CTRL_C_EVENT:
    case CTRL_CLOSE_EVENT:
    case CTRL_BREAK_EVENT:
    case CTRL_SHUTDOWN_EVENT:
        post_event(shutdown_event, NULL);
        return TRUE;
    }
    return FALSE;
}
#endif

#if !defined(_WRS_KERNEL)
static const char * help_text[] = {
    "Usage: agent [OPTION]...",
    "Start Target Communication Framework agent.",
    "  -d               run in daemon mode",
#if ENABLE_Cmdline
    "  -i               run in interactive mode",
#endif
#if ENABLE_RCBP_TEST
    "  -t               run in diagnostic mode",
#endif
    "  -L<file>         enable logging, use -L- to send log to stderr",
    "  -l<number>       set log level, the value is bitwise OR of:",
    "       0x0001          memory allocation and deallocation",
    "       0x0002          main event queue",
    "       0x0004          waitpid() events",
    "       0x0008          low-level debugger events",
    "       0x0020          communication protocol",
    "       0x0040          debugger actions",
    "       0x0080          discovery",
    "       0x0100          async I/O",
    "       0x0200          proxy state",
    "       0x0400          proxy traffic",
    "       0x0800          ELF reader",
    "       0x1000          LUA interpreter",
    "       0x2000          stack trace service",
    "       0x4000          plugins",
    "  -s<url>          set agent listening port and protocol, default is TCP::1534",
#if ENABLE_Plugins
    "  -P<dir>          set agent plugins directory name",
#endif
#if ENABLE_SSL
    "  -g               generate SSL certificate and exit",
#endif
    NULL
};

static void show_help(void) {
    const char ** p = help_text;
    while (*p != NULL) fprintf(stderr, "%s\n", *p++);
}
#endif

#if defined(_WRS_KERNEL)
int tcf(void) {
#else
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    int daemon = 0;
    int interactive = 0;
    const char * log_name = NULL;
    const char * url = "TCP:";
    Protocol * proto = NULL;
    TCFBroadcastGroup * bcg = NULL;

    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_asyncreq();

#if defined(_WRS_KERNEL)

    progname = "tcf";
    open_log_file("-");
    log_mode = 0;

#else

    progname = argv[0];

    /* Parse arguments */
    for (ind = 1; ind < argc; ind++) {
        const char * s = argv[ind];
        if (*s != '-') {
            break;
        }
        s++;
        while ((c = *s++) != '\0') {
            switch (c) {
            case 'i':
                interactive = 1;
                break;

            case 't':
#if ENABLE_RCBP_TEST
                test_proc();
#endif
                exit(0);
                break;

            case 'd':
                daemon = 1;
                break;

            case 'c':
                generate_ssl_certificate();
                exit(0);
                break;

            case 'l':
            case 'L':
            case 's':
#if ENABLE_Plugins
            case 'P':
#endif
                if (*s == '\0') {
                    if (++ind >= argc) {
                        fprintf(stderr, "%s: error: no argument given to option '%c'\n", progname, c);
                        exit(1);
                    }
                    s = argv[ind];
                }
                switch (c) {
                case 'l':
                    log_mode = strtol(s, 0, 0);
                    break;

                case 'L':
                    log_name = s;
                    break;

                case 's':
                    url = s;
                    break;

#if ENABLE_Plugins
                case 'P':
                    plugins_path = s;
                    break;
#endif
                }
                s = "";
                break;

            default:
                fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                show_help();
                exit(1);
            }
        }
    }

    if (daemon) become_daemon();
    open_log_file(log_name);

#endif

    bcg = broadcast_group_alloc();
    proto = protocol_alloc();

    /* The static services must be initialised before the plugins */
#if ENABLE_Cmdline
    if (interactive) ini_cmdline_handler(interactive, proto);
#else
    if (interactive) fprintf(stderr, "Warning: This version does not support interactive mode.\n");
#endif

    ini_services(proto, bcg);
    if (ini_server(url, proto, bcg) < 0) {
        fprintf(stderr, "Cannot create TCF server: %s\n", errno_to_str(errno));
        exit(1);
    }
    discovery_start();

    signal(SIGABRT, signal_handler);
    signal(SIGILL, signal_handler);
    signal(SIGINT, signal_handler);
    signal(SIGTERM, signal_handler);

#if defined(WIN32)
    SetConsoleCtrlHandler((PHANDLER_ROUTINE)CtrlHandler, TRUE);
#endif

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();

#if ENABLE_Plugins
    plugins_destroy();
#endif /* ENABLE_Plugins */

    return 0;
}
