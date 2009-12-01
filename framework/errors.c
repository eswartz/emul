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

#define ERR_MESSAGE_MIN         (STD_ERR_BASE + 100)
#define ERR_MESSAGE_MAX         (STD_ERR_BASE + 107)

#define MESSAGE_CNT             (ERR_MESSAGE_MAX - ERR_MESSAGE_MIN + 1)

#define SRC_SYSTEM  1
#define SRC_GAI     2
#define SRC_MESSAGE 3

typedef struct ErrorMessage {
    int source;
    int error;
    char text[128];
} ErrorMessage;

static ErrorMessage msgs[MESSAGE_CNT];
static int msgs_pos = 0;

static ErrorMessage * alloc_msg(int source) {
    ErrorMessage * m;
    assert(is_dispatch_thread());
    m = msgs + msgs_pos;
    errno = ERR_MESSAGE_MIN + msgs_pos++;
    if (msgs_pos >= MESSAGE_CNT) msgs_pos = 0;
    m->source = source;
    return m;
}

#ifdef WIN32

static char * system_strerror(DWORD errno_win32) {
    static char msg[512];
    WCHAR * buf = NULL;
    assert(is_dispatch_thread());
    if (!FormatMessageW(
        FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS |
        FORMAT_MESSAGE_MAX_WIDTH_MASK,
        NULL,
        errno_win32,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), /* Default language */
        (LPWSTR)&buf, 0, NULL) ||
        !WideCharToMultiByte(CP_UTF8, 0, buf, -1, msg, sizeof(msg), NULL, NULL))
    {
        snprintf(msg, sizeof(msg), "System Error Code %lu", (unsigned long)errno_win32);
    }
    if (buf != NULL) LocalFree(buf);
    return msg;
}

int set_win32_errno(DWORD win32_error_code) {
    if (win32_error_code) {
        ErrorMessage * m = alloc_msg(SRC_SYSTEM);
        m->error = win32_error_code;
    }
    else {
        errno = 0;
    }
    return errno;
}

DWORD get_win32_errno(int no) {
    if (no >= ERR_MESSAGE_MIN && no <= ERR_MESSAGE_MAX) {
        ErrorMessage * m = msgs + (no - ERR_MESSAGE_MIN);
        if (m->source == SRC_SYSTEM) return m->error;
    }
    return 0;
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
    default:
        if (err >= ERR_MESSAGE_MIN && err <= ERR_MESSAGE_MAX) {
            ErrorMessage * m = msgs + (err - ERR_MESSAGE_MIN);
            switch (m->source) {
#ifdef WIN32
            case SRC_SYSTEM:
                return system_strerror(m->error);
#endif
            case SRC_GAI:
                return loc_gai_strerror(m->error);
            case SRC_MESSAGE:
                snprintf(buf, sizeof(buf), "%s: %s", m->text, errno_to_str(m->error));
                return buf;
            }
        }
        return strerror(err);
    }
}

int set_errno(int no, char * msg) {
    errno = no;
    if (no != 0 && msg != NULL) {
        ErrorMessage * m = alloc_msg(SRC_MESSAGE);
        m->error = get_errno(no);
        memset(m->text, 0, sizeof(m->text));
        strncpy(m->text, msg, sizeof(m->text) - 1);
    }
    return errno;
}

int get_errno(int no) {
    if (no >= ERR_MESSAGE_MIN && no <= ERR_MESSAGE_MAX) {
        ErrorMessage * m = msgs + (no - ERR_MESSAGE_MIN);
        if (m->source == SRC_MESSAGE) return m->error;
        return ERR_OTHER;
    }
    return no;
}

int set_gai_errno(int no) {
    errno = no;
    if (no != 0) {
        ErrorMessage * m = alloc_msg(SRC_GAI);
        m->error = no;
    }
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
