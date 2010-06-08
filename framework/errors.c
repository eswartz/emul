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

#include <config.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <framework/errors.h>
#include <framework/events.h>
#include <framework/streams.h>
#include <framework/myalloc.h>
#include <framework/json.h>
#include <framework/trace.h>

#define ERR_MESSAGE_MIN         (STD_ERR_BASE + 100)
#define ERR_MESSAGE_MAX         (STD_ERR_BASE + 199)

#define MESSAGE_CNT             (ERR_MESSAGE_MAX - ERR_MESSAGE_MIN + 1)

#define SRC_SYSTEM  1
#define SRC_GAI     2
#define SRC_MESSAGE 3
#define SRC_REPORT  4

typedef struct ReportBuffer {
    ErrorReport pub; /* public part of error report */
    int refs;
    int gets;
} ReportBuffer;

typedef struct ErrorMessage {
    int source;
    int error;
    char * text;
    ReportBuffer * report;
} ErrorMessage;

static ErrorMessage msgs[MESSAGE_CNT];
static int msgs_pos = 0;

static void release_report(ReportBuffer * report) {
    if (report == NULL) return;
    assert(report->refs > report->gets);
    report->refs--;
    if (report->refs == 0) {
        while (report->pub.props != NULL) {
            ErrorReportItem * i = report->pub.props;
            report->pub.props = i->next;
            loc_free(i->name);
            loc_free(i->value);
            loc_free(i);
        }
        loc_free(report->pub.format);
        loc_free(report);
    }
}

static ErrorMessage * alloc_msg(int source) {
    ErrorMessage * m = msgs + msgs_pos;
    assert(is_dispatch_thread());
    errno = ERR_MESSAGE_MIN + msgs_pos++;
    if (msgs_pos >= MESSAGE_CNT) msgs_pos = 0;
    release_report(m->report);
    loc_free(m->text);
    m->source = source;
    m->error = 0;
    m->report = NULL;
    m->text = NULL;
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

typedef struct EventArgs {
    HANDLE done;
    int win32_code;
    int error_code;
} EventArgs;

static void set_win32_errno_event(void * args) {
    ErrorMessage * m = NULL;
    EventArgs * e = (EventArgs *)args;

    m = alloc_msg(SRC_SYSTEM);
    m->error = e->win32_code;
    e->error_code = errno;
    SetEvent(e->done);
}

int set_win32_errno(DWORD win32_error_code) {
    if (win32_error_code) {
        if (is_dispatch_thread()) {
            ErrorMessage * m = alloc_msg(SRC_SYSTEM);
            m->error = win32_error_code;
        }
        else {
            /* Called on background thread */
            int error = 0;
            EventArgs * e = (EventArgs *)loc_alloc_zero(sizeof(EventArgs));
            e->done = CreateEvent(NULL, TRUE, FALSE, NULL);
            e->win32_code = win32_error_code;
            post_event(set_win32_errno_event, e);
            WaitForSingleObject(e->done, INFINITE);
            CloseHandle(e->done);
            error = e->error_code;
            loc_free(e);
            errno = error;
        }
    }
    else {
        errno = 0;
    }
    return errno;
}

#elif defined(__SYMBIAN32__)

#include <e32err.h>

static char * system_strerror(int err) {
    static char static_error[32];
    switch (err) {
    case KErrNotFound:
        return "item not found";
    case KErrNotSupported:
        return "functionality is not supported";
    case KErrBadHandle:
        return "an invalid handle";
    case KErrAccessDenied:
        return "access to a file is denied";
    case KErrAlreadyExists:
        return "an object already exists";
    case KErrWrite:
        return "error in write operation";
    case KErrPermissionDenied:
        return "permission denied";
    case KErrBadDescriptor:
        return "bad descriptor";
    default:
        snprintf(static_error, sizeof(static_error), "Error code %d", err);
        return static_error;
    }
}

#endif

const char * errno_to_str(int err) {
    switch (err) {
    case ERR_ALREADY_STOPPED:   return "Already stopped";
    case ERR_ALREADY_EXITED:    return "Already exited";
    case ERR_ALREADY_RUNNING:   return "Already running";
    case ERR_JSON_SYNTAX:       return "JSON syntax error";
    case ERR_PROTOCOL:          return "Protocol format error";
    case ERR_INV_CONTEXT:       return "Invalid context ID";
    case ERR_INV_ADDRESS:       return "Invalid address";
    case ERR_EOF:               return "End of file";
    case ERR_BASE64:            return "Invalid BASE64 string";
    case ERR_INV_EXPRESSION:    return "Invalid expression";
    case ERR_SYM_NOT_FOUND:     return "Symbol not found";
    case ERR_ALREADY_ATTACHED:  return "Already attached";
    case ERR_BUFFER_OVERFLOW:   return "Buffer overflow";
    case ERR_INV_FORMAT:        return "Format is not supported";
    case ERR_INV_NUMBER:        return "Invalid number";
    case ERR_IS_RUNNING:        return "Execution context is running";
    case ERR_INV_DWARF:         return "Error reading DWARF data";
    case ERR_UNSUPPORTED:       return "Unsupported command";
    case ERR_CHANNEL_CLOSED:    return "Channel closed";
    case ERR_COMMAND_CANCELLED: return "Command canceled";
    case ERR_UNKNOWN_PEER:      return "Unknown peer ID";
    case ERR_INV_DATA_SIZE:     return "Invalid data size";
    case ERR_INV_DATA_TYPE:     return "Invalid data type";
    case ERR_INV_COMMAND:       return "Command is not recognized";
    case ERR_INV_TRANSPORT:     return "Invalid transport name";
    case ERR_CACHE_MISS:        return "Invalid data cache state";
    default:
        if (err >= ERR_MESSAGE_MIN && err <= ERR_MESSAGE_MAX) {
            ErrorMessage * m = msgs + (err - ERR_MESSAGE_MIN);
            if (m->report != NULL && m->report->pub.format != NULL) {
                /* TODO: error report args */
                return m->report->pub.format;
            }
            switch (m->source) {
#ifdef WIN32
            case SRC_SYSTEM:
                return system_strerror(m->error);
#endif
            case SRC_GAI:
                return loc_gai_strerror(m->error);
            case SRC_MESSAGE:
                return m->text;
            case SRC_REPORT:
                return errno_to_str(m->error);
            }
        }
#ifdef __SYMBIAN32__
        if (err < 0) {
            return system_strerror(err);
        }
#endif
        return strerror(err);
    }
}

int set_errno(int no, const char * msg) {
    errno = no;
    if (no != 0 && msg != NULL) {
        const char * text0 = errno_to_str(no);
        int len = strlen(msg) + strlen(text0) + 4;
        char * text1 = (char *)loc_alloc(len);
        ErrorMessage * m = NULL;
        snprintf(text1, len, "%s. %s", msg, text0);
        m = alloc_msg(SRC_MESSAGE);
        m->error = get_error_code(no);
        m->text = text1;
    }
    return errno;
}

int set_gai_errno(int no) {
    errno = no;
    if (no != 0) {
        ErrorMessage * m = alloc_msg(SRC_GAI);
        m->error = no;
    }
    return errno;
}

int set_error_report_errno(ErrorReport * r) {
    errno = 0;
    if (r != NULL) {
        ReportBuffer * report = (ReportBuffer *)((char *)r - offsetof(ReportBuffer, pub));
        ErrorMessage * m = alloc_msg(SRC_REPORT);
        m->error = report->pub.code + STD_ERR_BASE;
        m->report = report;
        report->refs++;
    }
    return errno;
}

int get_error_code(int no) {
    while (no >= ERR_MESSAGE_MIN && no <= ERR_MESSAGE_MAX) {
        ErrorMessage * m = msgs + (no - ERR_MESSAGE_MIN);
        switch (m->source) {
        case SRC_REPORT:
        case SRC_MESSAGE:
            no = m->error;
            continue;
        }
        return ERR_OTHER;
    }
    return no;
}

static void add_report_prop(ReportBuffer * report, const char * name, ByteArrayOutputStream * buf) {
    ErrorReportItem * i = (ErrorReportItem *)loc_alloc(sizeof(ErrorReportItem));
    i->name = loc_strdup(name);
    get_byte_array_output_stream_data(buf, &i->value, NULL);
    i->next = report->pub.props;
    report->pub.props = i;
}

static void add_report_prop_int(ReportBuffer * report, const char * name, unsigned long n) {
    ByteArrayOutputStream buf;
    OutputStream * out = create_byte_array_output_stream(&buf);
    json_write_ulong(out, n);
    write_stream(out, 0);
    add_report_prop(report, name, &buf);
}

static void add_report_prop_str(ReportBuffer * report, const char * name, const char * str) {
    ByteArrayOutputStream buf;
    OutputStream * out = create_byte_array_output_stream(&buf);
    json_write_string(out, str);
    write_stream(out, 0);
    add_report_prop(report, name, &buf);
}

ErrorReport * get_error_report(int err) {
    ErrorMessage * m = NULL;
    if (err >= ERR_MESSAGE_MIN && err <= ERR_MESSAGE_MAX) {
        m = msgs + (err - ERR_MESSAGE_MIN);
        if (m->report != NULL) {
            m->report->refs++;
            m->report->gets++;
            return &m->report->pub;
        }
    }
    if (err != 0) {
        ReportBuffer * report = (ReportBuffer *)loc_alloc_zero(sizeof(ReportBuffer));
        struct timespec timenow;

        if (clock_gettime(CLOCK_REALTIME, &timenow) == 0) {
            report->pub.time_stamp = (uint64_t)timenow.tv_sec * 1000 + timenow.tv_nsec / 1000000;
        }

        report->pub.format = loc_strdup(errno_to_str(err));

        if (m != NULL) {
            if (m->source == SRC_MESSAGE) {
                err = m->error;
            }
#ifdef WIN32
            else if (m->source == SRC_SYSTEM) {
                add_report_prop_int(report, "AltCode", m->error);
                add_report_prop_str(report, "AltOrg", "WIN32");
                err = ERR_OTHER;
            }
#endif
            else {
                err = ERR_OTHER;
            }
        }

        if (err < STD_ERR_BASE || err > ERR_MESSAGE_MAX) {
            add_report_prop_int(report, "AltCode", err);
#if defined(_MSC_VER)
            add_report_prop_str(report, "AltOrg", "MSC");
#elif defined(_WRS_KERNEL)
            add_report_prop_str(report, "AltOrg", "VxWorks");
#elif defined(__CYGWIN__)
            add_report_prop_str(report, "AltOrg", "CygWin");
#elif defined(__linux__)
            add_report_prop_str(report, "AltOrg", "Linux");
#elif defined(__SYMBIAN32__)
            add_report_prop_str(report, "AltOrg", "Symbian");
#else
            add_report_prop_str(report, "AltOrg", "POSIX");
#endif
            err = ERR_OTHER;
        }

        assert(err >= STD_ERR_BASE);
        assert(err < ERR_MESSAGE_MIN);

        report->pub.code = err - STD_ERR_BASE;
        report->refs = 1;
        report->gets = 1;
        if (m != NULL) {
            assert(m->report == NULL);
            m->report = report;
            report->refs++;
        }
        return &report->pub;
    }
    return NULL;
}

ErrorReport * create_error_report(void) {
    ReportBuffer * report = (ReportBuffer *)loc_alloc_zero(sizeof(ReportBuffer));
    report->refs = 1;
    report->gets = 1;
    return &report->pub;
}

void release_error_report(ErrorReport * r) {
    if (r != NULL) {
        ReportBuffer * report = (ReportBuffer *)((char *)r - offsetof(ReportBuffer, pub));
        assert(is_dispatch_thread());
        assert(report->gets > 0);
        report->gets--;
        release_report(report);
    }
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

void check_error_debug(const char * file, int line, int error) {
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
