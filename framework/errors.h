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
 * This module defines agent error codes in addition to system codes defined in errno.h
 */

#ifndef D_errors
#define D_errors

#include <errno.h>

#define STD_ERR_BASE    0x20000

#define ERR_OTHER               (STD_ERR_BASE + 1)
#define ERR_JSON_SYNTAX         (STD_ERR_BASE + 2)
#define ERR_PROTOCOL            (STD_ERR_BASE + 3)
#define ERR_BUFFER_OVERFLOW     (STD_ERR_BASE + 4)
#define ERR_CHANNEL_CLOSED      (STD_ERR_BASE + 5)
#define ERR_COMMAND_CANCELLED   (STD_ERR_BASE + 6)
#define ERR_UNKNOWN_PEER        (STD_ERR_BASE + 7)
#define ERR_BASE64              (STD_ERR_BASE + 8)
#define ERR_EOF                 (STD_ERR_BASE + 9)
#define ERR_ALREADY_STOPPED     (STD_ERR_BASE + 10)
#define ERR_ALREADY_EXITED      (STD_ERR_BASE + 11)
#define ERR_ALREADY_RUNNING     (STD_ERR_BASE + 12)
#define ERR_ALREADY_ATTACHED    (STD_ERR_BASE + 13)
#define ERR_IS_RUNNING          (STD_ERR_BASE + 14)
#define ERR_INV_DATA_SIZE       (STD_ERR_BASE + 15)
#define ERR_INV_CONTEXT         (STD_ERR_BASE + 16)
#define ERR_INV_ADDRESS         (STD_ERR_BASE + 17)
#define ERR_INV_EXPRESSION      (STD_ERR_BASE + 18)
#define ERR_INV_FORMAT          (STD_ERR_BASE + 19)
#define ERR_INV_NUMBER          (STD_ERR_BASE + 20)
#define ERR_INV_DWARF           (STD_ERR_BASE + 21)
#define ERR_SYM_NOT_FOUND       (STD_ERR_BASE + 22)
#define ERR_UNSUPPORTED         (STD_ERR_BASE + 23)
#define ERR_INV_DATA_TYPE       (STD_ERR_BASE + 24)
#define ERR_INV_COMMAND         (STD_ERR_BASE + 25)
#define ERR_INV_TRANSPORT       (STD_ERR_BASE + 26)

#define ERR_EXCEPTION           (STD_ERR_BASE + 100)

/*
 * Convert error code to human readable string
 */
extern const char * errno_to_str(int no);

extern void set_exception_errno(int no, char * msg);
extern int get_exception_errno(int no);

extern int set_gai_errno(int gai_error_code);

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
extern int set_win32_errno(DWORD win32_error_code);
extern DWORD get_win32_errno(int no);
#endif


#endif /* D_errors */
