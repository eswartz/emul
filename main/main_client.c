/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 * Agent main module.
 */

#include <config.h>

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <framework/asyncreq.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <framework/channel.h>
#include <framework/protocol.h>
#include <framework/proxy.h>
#include <framework/plugins.h>
#include <services/discovery.h>
#include <main/cmdline.h>

static const char * progname;
static Protocol * proto;

/*
 * main entry point for TCF client
 *
 * The client is a simple shell permitting communication with the TCF agent.
 * By default the client will run in interactive mode. The client accepts
 * 3 command line options:
 * -L <log_file>        : specify a log file
 * -l <log_mode>        : logging level see trace.c for more details
 * -S <script_file>     : script of commands to run - non-interactive mode
 */

#if defined(_WRS_KERNEL)
int tcf_client(void) {
#else
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    int keep_alive = 0;
    int mode = 1; /* interactive */
    const char * host_name = "localhost";
    const char * command = NULL;
    const char * log_name = "-";
    const char * script_name = NULL;

    log_mode = 0;

    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_asyncreq();

#if defined(_WRS_KERNEL)

    progname = "tcf";
    open_log_file("-");

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
            case 'd':
                keep_alive = 1;
                break;

            case 'l':
            case 'L':
            case 'S':
            case 'h':
            case 'c':
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

                case 'S':
                    script_name = s;
                    mode = 0;
                    break;

                case 'h':
                    host_name = s;
                    break;

                case 'c':
                    /* TODO: allow multiple -c options */
                    command = s;
                    mode = 2;
                    break;

#if ENABLE_Plugins
                case 'P':
                    plugins_path = s;
                    break;
#endif

                default:
                    fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                    exit(1);
                }
                s = "";
                break;

            default:
                fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                exit(1);
            }
        }
    }

    if (script_name != NULL && command != NULL) {
        fprintf(stderr, "%s: error: illegal option -S and -c are mutually exclusive\n", progname);
        exit(1);
    }

    open_log_file(log_name);

#endif

    discovery_start();

    proto = protocol_alloc();

#if ENABLE_Cmdline
    if (script_name != NULL) open_script_file(script_name);
    if (command != NULL) set_single_command(keep_alive, host_name, command);
    ini_cmdline_handler(mode, proto);
#else
    if (script_name != NULL) fprintf(stderr, "Warning: This version does not support script file as input.\n");
#endif

#if ENABLE_Plugins
    plugins_load(proto, NULL);
#endif

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
