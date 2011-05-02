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
 * Utility module that implements an abstarct output queue.
 */

#ifndef D_outputbuf
#define D_outputbuf

#include <config.h>
#include <framework/link.h>

typedef struct OutputQueue OutputQueue;
typedef struct OutputBuffer OutputBuffer;

struct OutputQueue {
    int error;
    LINK queue;
    LINK pool;
    int pool_size;
    void (*post_io_request)(OutputBuffer *);
};

struct OutputBuffer {
    LINK link;
    OutputQueue * queue;
    char buf[128 * MEM_USAGE_FACTOR];
    size_t buf_len;
    size_t buf_pos;
};

#define output_queue_is_empty(q) (list_is_empty(&(q)->queue))

extern void output_queue_ini(OutputQueue * q);
extern void output_queue_add(OutputQueue * q, const void * buf, size_t size);
extern void output_queue_done(OutputQueue * q, int error, int size);
extern void output_queue_clear(OutputQueue * q);

#endif /* D_outputbuf */
