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
 */

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

#define SERVICE_RunControl      TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_Breakpoints     TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_Memory          TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_Registers       TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_StackTrace      TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_Symbols         TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_LineNumbers     TARGET_UNIX
#define SERVICE_Processes       TARGET_UNIX || TARGET_VXWORKS
#define SERVICE_FileSystem      TARGET_UNIX || TARGET_VXWORKS || TARGET_WINDOWS
#define SERVICE_SysMonitor      TARGET_UNIX



