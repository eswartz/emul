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

/*
 * Agent main module.
 */

#include "config.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include "mdep.h"
#include "asyncreq.h"
#include "events.h"
#include "trace.h"
#include "cmdline.h"
#include "channel.h"
#include "protocol.h"
#include "proxy.h"
#include "discovery_help.h"

static char * progname;

#if defined(_WRS_KERNEL)
int tcf_client(void) {
#else   
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    int error;
    char * s;
    char * log_name = 0;
    char * node = NULL;
    Protocol * proto;

#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif
    ini_mdep();
    ini_trace();
    ini_asyncreq();
    ini_events_queue();

#if defined(_WRS_KERNEL)
    
    progname = "tcf";
    log_file = stdout;
    log_mode = 0;
    
#else
    
    progname = argv[0];

    /* Parse arguments */
    for (ind = 1; ind < argc; ind++) {
        s = argv[ind];
        if (*s != '-') {
            break;
        }
        s++;
        while ((c = *s++) != '\0') {
            switch (c) {
            case 'l':
            case 'L':
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
    
    /* Create log file */
    if (log_name == 0) {
        log_file = NULL;
    }
    else if (strcmp(log_name, "-") == 0) {
        log_file = stderr;
    }
    else if ((log_file = fopen(log_name, "a")) == NULL) {
        fprintf(stderr, "%s: error: cannot create log file %s\n", progname, log_name);
        exit(1);
    }
    
#endif

    ini_proxy_service();
    if (discovery_start(discovery_default_master_notifier)) {
            discovery_default_master_notifier();
    }
    ini_cmdline_handler();

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
