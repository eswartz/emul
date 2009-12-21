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

#ifndef D_config
#define D_config

#include "mdep.h"

#if !defined(SERVICE_Locator)
#define SERVICE_Locator         1
#endif
#if !defined(SERVICE_FileSystem)
#define SERVICE_FileSystem      1
#endif
#if !defined(SERVICE_LineNumbers)
#define SERVICE_LineNumbers     1
#endif

#if !defined(ENABLE_ZeroCopy)
#define ENABLE_ZeroCopy         1
#endif

#if !defined(ENABLE_Splice)
#  if (ENABLE_ZeroCopy) && defined(SPLICE_F_MOVE)
#    define ENABLE_Splice       1
#  else
#    define ENABLE_Splice       0
#  endif
#endif

#if !defined(ENABLE_Trace)
#  define ENABLE_Trace          1
#endif

#if !defined(ENABLE_Discovery)
#  define ENABLE_Discovery      1
#endif

#if !defined(ENABLE_ContextProxy)
#  define ENABLE_ContextProxy   1
#endif

#if !defined(ENABLE_DebugContext)
#  define ENABLE_DebugContext   1
#endif

#if !defined(ENABLE_ELF)
#  define ENABLE_ELF            1
#endif

#if !defined(ENABLE_SSL)
#  if defined(__linux__)
#    define ENABLE_SSL          1
#  else
#    define ENABLE_SSL          0
#  endif
#endif

#ifdef CONFIG_MAIN
/*
 * This part of config.h contains services initialization code,
 * which is executed during agent startup.
 */

#include "discovery.h"
#include "filesystem.h"
#include "linenumbers.h"
#include "diagnostics.h"
#include "tcf_elf.h"

static void ini_services(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
#if SERVICE_Locator
    ini_locator_service(proto, bcg);
#endif
#if SERVICE_FileSystem
    ini_file_system_service(proto);
#endif
#if SERVICE_LineNumbers
    ini_line_numbers_service(proto);
#endif
#if ENABLE_DebugContext
    ini_contexts();
#endif
#if ENABLE_ELF
    ini_elf();
#endif

    ini_diagnostics_service(proto);
}

#endif /* CONFIG_MAIN */

#endif /* D_config */
