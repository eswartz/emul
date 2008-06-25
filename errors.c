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

#include "mdep.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "errors.h"
#include "events.h"
#include "trace.h"

#define ERR_SYSTEM  STD_ERR_BASE

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
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
        (LPTSTR) &msg_buf,
        0,
        NULL))
    {
        snprintf(msg, sizeof(msg), "System Error Code %d", errno_win32);
    }
    else {
        int l;
        snprintf(msg, sizeof(msg), "System Error %d: %s", errno_win32, msg_buf, sizeof(msg));
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
    errno = ERR_SYSTEM;
    errno_win32 = win32_error_code;
    return errno;
}

#else

static char * system_strerror(void) {
    assert(0);
}

#endif

char * errno_to_str(int err) {
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
    case ERR_SYSTEM:
        return system_strerror();
    default:
        return strerror(err);
    }
}

#ifdef NDEBUG

void check_error(int error) {
    if (error == 0) return;
    trace(LOG_ALWAYS, "Fatal error %d: %s", error, errno_to_str(error));
    trace(LOG_ALWAYS, "  Exiting agent...");
    exit(1);
}

#else

void check_error_debug(char * file, int line, int error) {
    if (error == 0) return;
#if ENABLE_Trace
    if (log_file != stderr) {
        trace(LOG_ALWAYS, "Fatal error %d: %s", error, errno_to_str(error));
        trace(LOG_ALWAYS, "  At %s:%d", file, line);
        trace(LOG_ALWAYS, "  Exiting agent...");
    }
#endif
    fprintf(stderr, "Fatal error %d: %s", error, errno_to_str(error));
    fprintf(stderr, "  At %s:%d", file, line);
    fprintf(stderr, "  Exiting agent...");
    exit(1);
}

#endif
