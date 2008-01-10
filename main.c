/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
#include "mdep.h"
#include "events.h"
#include "trace.h"
#include "expressions.h"
#include "cmdline.h"
#include "context.h"
#include "channel.h"
#include "protocol.h"
#include "runctrl.h"
#include "registers.h"
#include "stacktrace.h"
#include "memory.h"
#include "breakpoints.h"
#include "diagnostics.h"
#include "filesystem.h"
#include "processes.h"
#include "symbols.h"
#include "linenumbers.h"
#include "proxy.h"
#include "sysmon.h"

static char * progname;

#if defined(_WRS_KERNEL)
int tcf(void) {
#else	
int main(int argc, char **argv) {
#endif
    int c;
    int ind;
    int interactive = 0;
    char *s;
    char *log_name = 0;
    int port = 1534;

    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_expression_library();

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
            case 'i':
                interactive = 1;
                break;

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

    if (interactive) ini_cmdline_handler();

    ini_protocol();
#if SERVICE_RunControl
    ini_run_ctrl_service();
#endif
#if SERVICE_Breakpoints
    ini_breakpoints_service();
#endif
#if SERVICE_Memory
    ini_memory_service();
#endif
#if SERVICE_Registers
    ini_registers_service();
#endif
#if SERVICE_StackTrace
    ini_stack_trace_service();
#endif
#if SERVICE_Symbols
    ini_symbols_service();
#endif
#if SERVICE_LineNumbers
    ini_line_numbers_service();
#endif
#if SERVICE_Processes
    ini_processes_service();
#endif
#if SERVICE_FileSystem
    ini_file_system_service();
#endif
#if SERVICE_SysMonitor
    ini_sys_mon_service();
#endif
    ini_diagnostics_service();
    ini_proxy_service();
    ini_contexts();
    ini_channel_manager(port);

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
