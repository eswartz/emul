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
 * This file contains "define" statements that control agent configuration.
 * SERVICE_* definitions control which service implementations are included into the agent.
 */

#ifndef D_config
#define D_config

#include "mdep.h"

#if defined(WIN32)
#  define TARGET_WINDOWS    1
#  define TARGET_VXWORKS    0
#  define TARGET_UNIX       0
#elif defined(_WRS_KERNEL)
#  define TARGET_WINDOWS    0
#  define TARGET_VXWORKS    1
#  define TARGET_UNIX       0
#else
#  define TARGET_WINDOWS    0
#  define TARGET_VXWORKS    0
#  define TARGET_UNIX       1
#endif

#define SERVICE_RunControl      TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_Breakpoints     TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_Memory          TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_Registers       TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_StackTrace      TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_Symbols         TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_LineNumbers     TARGET_UNIX || TARGET_WINDOWS
#define SERVICE_Processes       TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_FileSystem      TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_SysMonitor      TARGET_UNIX
#define SERVICE_Expressions     TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS

#define ENABLE_Trace        1
#define ENABLE_Discovery    1
#define ENABLE_Cmdline      1

#define ENABLE_ELF          (((SERVICE_LineNumbers) || (SERVICE_Symbols)) && !TARGET_WINDOWS)

#ifdef CONFIG_MAIN
/*
 * This part of config.h contains services initialization code,
 * which is executed during agent startup.
 */

#include "runctrl.h"
#include "breakpoints.h"
#include "memoryservice.h"
#include "registers.h"
#include "stacktrace.h"
#include "symbols.h"
#include "linenumbers.h"
#include "processes.h"
#include "filesystem.h"
#include "sysmon.h"
#include "diagnostics.h"
#include "expressions.h"
#include "proxy.h"

static void ini_services(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
#if SERVICE_RunControl
    ini_run_ctrl_service(proto, bcg, spg);
#endif
#if SERVICE_Breakpoints
    ini_breakpoints_service(proto, bcg);
#endif
#if SERVICE_Memory
    ini_memory_service(proto, bcg);
#endif
#if SERVICE_Registers
    ini_registers_service(proto);
#endif
#if SERVICE_StackTrace
    ini_stack_trace_service(proto, bcg);
#endif
#if SERVICE_Symbols
    ini_symbols_service(proto);
#endif
#if SERVICE_LineNumbers
    ini_line_numbers_service(proto);
#endif
#if SERVICE_Processes
    ini_processes_service(proto);
#endif
#if SERVICE_FileSystem
    ini_file_system_service(proto);
#endif
#if SERVICE_SysMonitor
    ini_sys_mon_service(proto);
#endif
#if SERVICE_Expressions
    ini_expressions_service(proto);
#endif
    ini_diagnostics_service(proto);
}

#endif /* CONFIG_MAIN */

#endif /* D_config */
