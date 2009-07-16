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

#include "config.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "errors.h"
#include "events.h"
#include "trace.h"

#define ERR_SYSTEM  (ERR_EXCEPTION + 1)
#define ERR_GAI     (ERR_EXCEPTION + 2)

static char * exception_msg;
static int exception_no;

static int errno_gai;

#ifdef WIN32

static DWORD errno_win32 = 0;

static char * system_strerror(void) {
    static char msg[256];
    LPVOID msg_buf;
    assert(is_dispatch_thread());
    if (!FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS,
        NULL,
        errno_win32,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), /* Default language */
        (LPTSTR) &msg_buf,
        0,
        NULL))
    {
        snprintf(msg, sizeof(msg), "System Error Code %lu", (unsigned long)errno_win32);
    }
    else {
        int l;
        strncpy(msg, msg_buf, sizeof(msg) - 1);
        msg[sizeof(msg) - 1] = 0;
        LocalFree(msg_buf);
        l = strlen(msg);
        while (l > 0 && (msg[l - 1] == '\n' || msg[l - 1] == '\r')) l--;
        msg[l] = 0;
    }
    return msg;
}

int set_win32_errno(DWORD win32_error_code) {
    assert(is_dispatch_thread());
    /* For WIN32 errors we always set errno to ERR_SYSTEM and
     * store actual error code in errno_win32, which is used later
     * when anyone calls errno_to_str() to get actual error message string.
     */
    if (win32_error_code) {
        errno = ERR_SYSTEM;
        errno_win32 = win32_error_code;
    }
    else {
        errno = 0;
    }
    return errno;
}

DWORD get_win32_errno(int no) {
    return no == ERR_SYSTEM ? errno_win32 : 0;
}

#endif

const char * errno_to_str(int err) {
    static char buf[256];
    switch (err) {
    case ERR_ALREADY_STOPPED:
        return "Already stopped";
    case ERR_ALREADY_EXITED:
        return "Already exited";
    case ERR_ALREADY_RUNNING:
        return "Already running";
    case ERR_JSON_SYNTAX:
        return "JSON syntax error";
    case ERR_PROTOCOL:
        return "Protocol format error";
    case ERR_INV_CONTEXT:
        return "Invalid context ID";
    case ERR_INV_ADDRESS:
        return "Invalid address";
    case ERR_EOF:
        return "End of file";
    case ERR_BASE64:
        return "Invalid BASE64 string";
    case ERR_INV_EXPRESSION:
        return "Invalid expression";
    case ERR_SYM_NOT_FOUND:
        return "Symbol not found";
    case ERR_ALREADY_ATTACHED:
        return "Already attached";
    case ERR_BUFFER_OVERFLOW:
        return "Buffer overflow";
    case ERR_INV_FORMAT:
        return "Format is not supported";
    case ERR_INV_NUMBER:
        return "Invalid number";
    case ERR_IS_RUNNING:
        return "Execution context is running";
    case ERR_INV_DWARF:
        return "Error reading DWARF data";
    case ERR_UNSUPPORTED:
        return "Unsupported command";
    case ERR_CHANNEL_CLOSED:
        return "Channel closed";
    case ERR_COMMAND_CANCELLED:
        return "Command cancelled";
    case ERR_UNKNOWN_PEER:
        return "Unknown peer ID";
    case ERR_INV_DATA_SIZE:
        return "Invalid data size";
    case ERR_INV_DATA_TYPE:
        return "Invalid data type";
    case ERR_INV_COMMAND:
        return "Command is not recognized";
    case ERR_INV_TRANSPORT:
        return "Invalid transport name";
    case ERR_EXCEPTION:
        snprintf(buf, sizeof(buf), "%s: %s", exception_msg, errno_to_str(exception_no));
        return buf;
#ifdef WIN32
    case ERR_SYSTEM:
        return system_strerror();
#endif
    case ERR_GAI:
        return loc_gai_strerror(errno_gai);
    default:
        return strerror(err);
    }
}

void set_exception_errno(int no, char * msg) {
    assert(is_dispatch_thread());
    if (msg == NULL) {
        errno = no;
    }
    else {
        errno = ERR_EXCEPTION;
        exception_no = no;
        exception_msg = msg;
    }
}

int get_exception_errno(int no) {
    return no == ERR_EXCEPTION ? exception_no : no;
}

int set_gai_errno(int n) {
    assert(is_dispatch_thread());
    errno = ERR_GAI;
    errno_gai = n;
    return errno;
}

#ifdef NDEBUG

void check_error(int error) {
    if (error == 0) return;
#if ENABLE_Trace
    trace(LOG_ALWAYS, "Fatal error %d: %s", error, errno_to_str(error));
    trace(LOG_ALWAYS, "  Exiting agent...");
    if (log_file == stderr) exit(1);
#endif
    fprintf(stderr, "Fatal error %d: %s", error, errno_to_str(error));
    fprintf(stderr, "  Exiting agent...");
    exit(1);
}

#else /* NDEBUG */

void check_error_debug(char * file, int line, int error) {
    if (error == 0) return;
#if ENABLE_Trace
    trace(LOG_ALWAYS, "Fatal error %d: %s", error, errno_to_str(error));
    trace(LOG_ALWAYS, "  At %s:%d", file, line);
    trace(LOG_ALWAYS, "  Exiting agent...");
    if (log_file == stderr) exit(1);
#endif
    fprintf(stderr, "Fatal error %d: %s", error, errno_to_str(error));
    fprintf(stderr, "  At %s:%d", file, line);
    fprintf(stderr, "  Exiting agent...");
    exit(1);
}

#endif /* NDEBUG */
