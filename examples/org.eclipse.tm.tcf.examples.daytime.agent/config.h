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
 * This file contains "define" statements that control agent configuration.
 * SERVICE_* definitions control which service implementations are included into the agent.
 * 
 * This is example agent configuration. It includes only few standard services,
 * and one example service: Day Time.
 */

#ifndef D_config
#define D_config

#if defined(WIN32) || defined(__CYGWIN__)
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

#define SERVICE_FileSystem      1
#define SERVICE_SysMonitor      TARGET_UNIX

#endif

#ifdef CONFIG_MAIN
/*
 * This part of config.h contains services initialization code,
 * which is executed during agent startup.
 */

#include "filesystem.h"
#include "sysmon.h"
#include "diagnostics.h"
#include "daytime.h"

static void ini_services(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
#if SERVICE_FileSystem
    ini_file_system_service(proto);
#endif
#if SERVICE_SysMonitor
    ini_sys_mon_service(proto);
#endif
    ini_diagnostics_service(proto);
    ini_daytime_service(proto);
}

#endif

