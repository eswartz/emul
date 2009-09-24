/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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

#if !defined(SERVICE_Locator)
#define SERVICE_Locator         (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_RunControl)
#define SERVICE_RunControl      (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Breakpoints)
#define SERVICE_Breakpoints     (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Memory)
#define SERVICE_Memory          (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_MemoryMap)
#define SERVICE_MemoryMap       (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Registers)
#define SERVICE_Registers       (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_StackTrace)
#define SERVICE_StackTrace      (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Symbols)
#define SERVICE_Symbols         (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_LineNumbers)
#define SERVICE_LineNumbers     (TARGET_UNIX || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Processes)
#define SERVICE_Processes       (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_FileSystem)
#define SERVICE_FileSystem      (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_SysMonitor)
#define SERVICE_SysMonitor      (TARGET_UNIX || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Expressions)
#define SERVICE_Expressions     (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif
#if !defined(SERVICE_Streams)
#define SERVICE_Streams         (TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS)
#endif

#ifndef ENABLE_Plugins
#define ENABLE_Plugins          ((TARGET_UNIX) && defined(PATH_Plugins))
#endif
#if !defined(ENABLE_ZeroCopy)
#define ENABLE_ZeroCopy         1
#endif
#if !defined(ENABLE_Splice)
#define ENABLE_Splice           ((ENABLE_ZeroCopy) && defined(SPLICE_F_MOVE))
#endif
#if !defined(ENABLE_Trace)
#define ENABLE_Trace            1
#endif
#if !defined(ENABLE_Discovery)
#define ENABLE_Discovery        1
#endif
#if !defined(ENABLE_Cmdline)
#define ENABLE_Cmdline          1
#endif
#if !defined(ENABLE_DebugContext)
#define ENABLE_DebugContext     (SERVICE_RunControl || SERVICE_Breakpoints || SERVICE_Memory || SERVICE_Registers || SERVICE_StackTrace)
#endif
#if !defined(ENABLE_ELF)
#define ENABLE_ELF              ((TARGET_UNIX || TARGET_VXWORKS) && (SERVICE_Symbols || SERVICE_LineNumbers))
#endif
#if !defined(ENABLE_SSL)
#define ENABLE_SSL              ((TARGET_UNIX) && !defined(__APPLE__))
#endif
#if !defined(ENABLE_RCBP_TEST)
#define ENABLE_RCBP_TEST        (SERVICE_RunControl && SERVICE_Breakpoints)
#endif
#if !defined(ENABLE_AIO)
#define ENABLE_AIO              defined(_POSIX_ASYNCHRONOUS_IO)
#endif
#if !defined(ENABLE_LUA)
#define ENABLE_LUA              defined(PATH_LUA)
#endif

#ifdef CONFIG_MAIN
/*
 * This part of config.h contains services initialization code,
 * which is executed during agent startup.
 */

#include "discovery.h"
#include "runctrl.h"
#include "breakpoints.h"
#include "memoryservice.h"
#include "memorymap.h"
#include "registers.h"
#include "stacktrace.h"
#include "symbols.h"
#include "linenumbers.h"
#include "processes.h"
#include "filesystem.h"
#include "sysmon.h"
#include "diagnostics.h"
#include "expressions.h"
#include "streamsservice.h"
#include "proxy.h"
#include "tcf_elf.h"
#include "plugins.h"

static void ini_services(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
#if SERVICE_Locator
    ini_locator_service(proto, bcg);
#endif
#if SERVICE_RunControl
    ini_run_ctrl_service(proto, bcg, spg);
#endif
#if SERVICE_Breakpoints
    ini_breakpoints_service(proto, bcg);
#endif
#if SERVICE_Memory
    ini_memory_service(proto, bcg);
#endif
#if SERVICE_MemoryMap
    ini_memory_map_service();
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
#if SERVICE_Streams
    ini_streams_service(proto);
#endif
#if ENABLE_DebugContext
    ini_contexts();
#endif
#if ENABLE_ELF
    ini_elf();
#endif
#if ENABLE_Plugins
    plugins_load(proto, bcg, spg);
#endif

    ini_diagnostics_service(proto);
}

#endif /* CONFIG_MAIN */

#endif /* D_config */

