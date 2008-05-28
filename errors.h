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
 * This module defines agent error codes in addition to system codes defined in errno.h
 */

#ifndef D_errors
#define D_errors

#include <errno.h>

#define ERR_ALREADY_STOPPED     0x1000
#define ERR_ALREADY_EXITED      0x1001
#define ERR_ALREADY_RUNNING     0x1002
#define ERR_JSON_SYNTAX         0x1003
#define ERR_PROTOCOL            0x1004
#define ERR_INV_CONTEXT         0x1005
#define ERR_INV_ADDRESS         0x1006
#define ERR_EOF                 0x1007
#define ERR_BASE64              0x1008
#define ERR_INV_EXPRESSION      0x1009
#define ERR_SYM_NOT_FOUND       0x100a
#define ERR_ALREADY_ATTACHED    0x100b
#define ERR_BUFFER_OVERFLOW     0x100c
#define ERR_INV_FORMAT          0x100d
#define ERR_INV_NUMBER          0x100e
#define ERR_IS_RUNNING          0x100f
#define ERR_DWARF               0x1010
#define ERR_UNSUPPORTED         0x1011
#define ERR_CHANNEL_CLOSED      0x1012
#define ERR_COMMAND_CANCELLED   0x1013
#define ERR_UNKNOWN_PEER        0x1014
#define ERR_INV_DATA_SIZE       0x1015

/*
 * Convert error code to human readable string
 */
extern char * errno_to_str(int error);

/*
 * check_error(): Check error code.
 * If the code is not zero, add error report into trace log and call exit(1)
 */
#ifdef NDEBUG
extern void check_error(int error);
#else
extern void check_error_debug(char * file, int line, int error);
#define check_error(error) check_error_debug(__FILE__, __LINE__, error)
#endif

#ifdef WIN32
/*
 * Set errno to WIN32 error code.
 */
extern void set_win32_errno(DWORD win32_error_code);
#endif


#endif
