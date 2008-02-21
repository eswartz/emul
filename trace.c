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

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <errno.h>
#include "mdep.h"
#include "trace.h"

FILE * log_file = NULL;
int log_mode = LOG_EVENTS | LOG_CHILD | LOG_WAITPID | LOG_CONTEXT | LOG_PROTOCOL;

static pthread_mutex_t mutex;

int print_trace(int mode, char *fmt, ...) {
    va_list ap;
    struct timespec timenow;
    char tmpbuf[1000];

    if (log_file == NULL || mode != LOG_ALWAYS && (log_mode & mode) == 0) {
        return 0;
    }

    if (clock_gettime(CLOCK_REALTIME, &timenow)) {
        perror("clock_gettime");
        exit(1);
    }

    va_start(ap, fmt);
    vsnprintf(tmpbuf, sizeof(tmpbuf), fmt, ap);
    va_end(ap);

    pthread_mutex_lock(&mutex);

    fprintf(log_file, "TCF %02d%02d.%03d: %s\n",
        timenow.tv_sec / 60 % 60,
        timenow.tv_sec % 60,
        timenow.tv_nsec / 1000000,
        tmpbuf);
    fflush(log_file);

    pthread_mutex_unlock(&mutex);

    return 1;
}

void ini_trace(void) {
    pthread_mutex_init(&mutex, NULL);
}


