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

#ifndef D_config
#define D_config

#include <framework/mdep.h>

#if !defined(SERVICE_Locator)
#define SERVICE_Locator         1
#endif
#if !defined(SERVICE_FileSystem)
#define SERVICE_FileSystem      1
#endif
#if !defined(SERVICE_LineNumbers)
#define SERVICE_LineNumbers     1
#endif
#if !defined(SERVICE_Symbols)
#define SERVICE_Symbols         1
#endif
#if !defined(SERVICE_PathMap)
#define SERVICE_PathMap         1
#endif
#if !defined(SERVICE_MemoryMap)
#define SERVICE_MemoryMap       1
#endif
#if !defined(SERVICE_StackTrace)
#define SERVICE_StackTrace      0
#endif

#if !defined(ENABLE_ZeroCopy)
#define ENABLE_ZeroCopy         1
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

#if !defined(ENABLE_SymbolsProxy)
#  define ENABLE_SymbolsProxy   0
#endif

#if !defined(ENABLE_LineNumbersProxy)
#  define ENABLE_LineNumbersProxy   0
#endif

#if !defined(ENABLE_Symbols)
#  define ENABLE_Symbols        (ENABLE_SymbolsProxy || SERVICE_Symbols)
#endif

#if !defined(ENABLE_LineNumbers)
#  define ENABLE_LineNumbers    (ENABLE_LineNumbersProxy || SERVICE_LineNumbers)
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

#if !defined(ENABLE_Unix_Domain)
/* Using UNIX:/path/to/socket for local TCP communication */
#  if defined(__linux__) || defined(__FreeBSD__) || defined(__NetBSD__) || defined(__APPLE__)
#    define ENABLE_Unix_Domain    1
#  else
#    define ENABLE_Unix_Domain    0
#  endif
#endif

#if !defined(ENABLE_STREAM_MACROS)
#define ENABLE_STREAM_MACROS    1
#endif

#endif /* D_config */
