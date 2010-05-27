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

#include <config.h>
#include <framework/trace.h>

int log_mode = LOG_EVENTS | LOG_CHILD | LOG_WAITPID | LOG_CONTEXT | LOG_PROTOCOL;

#if ENABLE_Trace

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <errno.h>

#if defined(WIN32)
#elif defined(_WRS_KERNEL)
#elif defined(__SYMBIAN32__)
#else
#include <syslog.h>
#endif

FILE * log_file = NULL;

static pthread_mutex_t mutex;

int print_trace(int mode, const char * fmt, ...) {
    va_list ap;

    if (log_file == NULL) return 0;
    if (mode != LOG_ALWAYS && (log_mode & mode) == 0) return 0;

    va_start(ap, fmt);
    if (is_daemon()) {
#if defined(WIN32)
#elif defined(_WRS_KERNEL)
#elif defined(__SYMBIAN32__)
#else
        vsyslog(LOG_MAKEPRI(LOG_DAEMON, LOG_INFO), fmt, ap);
#endif
    }
    else {
        struct timespec timenow;

        if (clock_gettime(CLOCK_REALTIME, &timenow)) {
            perror("clock_gettime");
            exit(1);
        }

        if (pthread_mutex_lock(&mutex) != 0) {
            perror("pthread_mutex_lock");
            exit(1);
        }

        fprintf(log_file, "TCF %02d:%02d.%03d: ",
            (int)(timenow.tv_sec / 60 % 60),
            (int)(timenow.tv_sec % 60),
            (int)(timenow.tv_nsec / 1000000));
        vfprintf(log_file, fmt, ap);
        fprintf(log_file, "\n");
        fflush(log_file);

        if (pthread_mutex_unlock(&mutex) != 0) {
            perror("pthread_mutex_unlock");
            exit(1);
        }
    }
    va_end(ap);
    return 1;
}

#endif /* ENABLE_Trace */

void open_log_file(const char * log_name) {
#if ENABLE_Trace
    if (log_name == NULL) {
        log_file = NULL;
    }
    else if (strcmp(log_name, "-") == 0) {
        log_file = stderr;
    }
    else if ((log_file = fopen(log_name, "a")) == NULL) {
        fprintf(stderr, "TCF: error: cannot create log file %s\n", log_name);
        exit(1);
    }
#endif /* ENABLE_Trace */
}

void ini_trace(void) {
#if ENABLE_Trace
    if (pthread_mutex_init(&mutex, NULL) != 0) {
        perror("pthread_mutex_init");
        exit(1);
    }
#endif /* ENABLE_Trace */
}


