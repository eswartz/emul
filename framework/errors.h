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
#define ERR_CACHE_MISS          (STD_ERR_BASE + 27)

typedef struct ErrorReportItem {
    char * name;
    char * value;
    struct ErrorReportItem * next;
} ErrorReportItem;

typedef struct ErrorReport {
    int code;
    char * format;
    uint64_t time_stamp;
    ErrorReportItem * props;
} ErrorReport;

/*
 * Convert error code to human readable string
 */
extern const char * errno_to_str(int no);

/*
 * Set errno to indicate given error code and additional error message.
 * The message will be concatenated with normal error text by errno_to_str().
 * The function creates a copy of the message and puts it into a queue of limited size.
 * Clients should not rely on messages being kept in the queue longer then one dispatch cycle.
 * Persistent error report can be obtained by calling get_error_report().
 * Return new error code that designates both original code and the message.
 */
extern int set_errno(int no, const char * msg);

/*
 * Set errno to indicate getaddrinfo() error code.
 * Return new value of errno.
 */
extern int set_gai_errno(int gai_error_code);

#ifdef WIN32
/*
 * Set errno to indicate WIN32 error code.
 * Return new value of errno.
 */
extern int set_win32_errno(DWORD win32_error_code);
#endif

/*
 * Set errno to indicate TCF standard error report.
 * Report objects are kept in a queue of limited size, and old reports are
 * disposed by calling release_error_report().
 * Clients should not rely on reports being kept in the queue longer then one dispatch cycle.
 * Persistent error report can be obtained by calling get_error_report().
 * Return new value of errno.
 */
extern int set_error_report_errno(ErrorReport * report);

/*
 * Return POSIX error code or one of ERR_* values for given errno value.
 */
extern int get_error_code(int no);

/*
 * Return TCF error report that describes given error code 'no'.
 * Clients should call release_error_report() when done using it.
 * Return NULL if 'no' = 0.
 */
extern ErrorReport * get_error_report(int no);

/*
 * Create new instance of TCF error report.
 * Clients should call release_error_report() when done using it.
 */
extern ErrorReport * create_error_report(void);

/*
 * Release error report that was obtained by get_error_report() or create_error_report().
 */
extern void release_error_report(ErrorReport * report);

/*
 * check_error(): Check error code.
 * If the code is not zero, add error report into trace log and call exit(1)
 */
#ifdef NDEBUG
extern void check_error(int error);
#else
extern void check_error_debug(const char * file, int line, int error);
#define check_error(error) check_error_debug(__FILE__, __LINE__, error)
#endif

#endif /* D_errors */
