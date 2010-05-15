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
 * Services initialization code.
 */

#include <config.h>

#include <framework/proxy.h>
#include <framework/plugins.h>
#include <services/discovery.h>
#include <services/runctrl.h>
#include <services/breakpoints.h>
#include <services/memoryservice.h>
#include <services/memorymap.h>
#include <services/registers.h>
#include <services/stacktrace.h>
#include <services/symbols.h>
#include <services/linenumbers.h>
#include <services/processes.h>
#include <services/filesystem.h>
#include <services/sysmon.h>
#include <services/diagnostics.h>
#include <services/expressions.h>
#include <services/streamsservice.h>
#include <services/pathmap.h>
#include <services/tcf_elf.h>
#include <main/services.h>

#include <main/services-ext.h>

void ini_services(Protocol * proto, TCFBroadcastGroup * bcg) {
#if SERVICE_Locator
    ini_locator_service(proto, bcg);
#endif
#if SERVICE_RunControl
    ini_run_ctrl_service(proto, bcg);
#endif
#if SERVICE_Breakpoints
    ini_breakpoints_service(proto, bcg);
#endif
#if SERVICE_Memory
    ini_memory_service(proto, bcg);
#endif
#if SERVICE_MemoryMap
    ini_memory_map_service(proto, bcg);
#endif
#if SERVICE_Registers
    ini_registers_service(proto, bcg);
#endif
#if SERVICE_StackTrace
    ini_stack_trace_service(proto, bcg);
#endif
#if SERVICE_Symbols
    ini_symbols_service(proto);
#elif ENABLE_SymbolsProxy
    ini_symbols_lib();
#endif
#if SERVICE_LineNumbers
    ini_line_numbers_service(proto);
#elif ENABLE_LineNumbersProxy
    ini_line_numbers_lib();
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
#if SERVICE_PathMap
    ini_path_map_service(proto);
#endif
#if ENABLE_DebugContext
    ini_contexts();
#endif
#if ENABLE_ELF
    ini_elf();
#endif
#if ENABLE_Plugins
    plugins_load(proto, bcg);
#endif

    ini_diagnostics_service(proto);
    ini_ext_services(proto, bcg);
}

