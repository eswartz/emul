/*******************************************************************************
 * Copyright (q) 2007, 2010 Wind River Systems, Inc. and others.
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

#include <config.h>
#include <assert.h>
#include <string.h>
#include <framework/outputbuf.h>
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/errors.h>

#define link2buf(A) ((OutputBuffer *)((char *)(A) - offsetof(OutputBuffer, link)))

void output_queue_ini(OutputQueue * q) {
    list_init(&q->queue);
    list_init(&q->pool);
}

void output_queue_add(OutputQueue * q, const void * buf, size_t size) {
    if (q->error) return;
    if (q->queue.next != q->queue.prev) {
        /* Append data to the last pending buffer */
        size_t gap = 0;
        OutputBuffer * bf = link2buf(q->queue.prev);
        assert(bf->buf_pos == 0);
        gap = sizeof(bf->buf) - bf->buf_len;
        if (gap > 0) {
            size_t len = size;
            if (len > gap) len = gap;
            memcpy(bf->buf + bf->buf_len, buf, len);
            bf->buf_len += len;
            buf = (const char *)buf + len;
            size -= len;
        }
    }
    while (size > 0) {
        size_t len = size;
        OutputBuffer * bf = NULL;
        if (list_is_empty(&q->pool)) {
            bf = (OutputBuffer *)loc_alloc_zero(sizeof(OutputBuffer));
            bf->queue = q;
        }
        else {
            bf = link2buf(q->pool.next);
            list_remove(&bf->link);
            q->pool_size--;
        }
        if (len > sizeof(bf->buf)) len = sizeof(bf->buf);
        bf->buf_pos = 0;
        bf->buf_len = len;
        memcpy(bf->buf, buf, len);
        list_add_last(&bf->link, &q->queue);
        if (q->queue.next == &bf->link) {
            q->post_io_request(bf);
        }
        buf = (const char *)buf + len;
        size -= len;
    }
}

void output_queue_done(OutputQueue * q, int error, int size) {
    OutputBuffer * bf = link2buf(q->queue.next);

    assert(q->error == 0);
    if (error) {
        q->error = error;
        trace(LOG_PROTOCOL, "Can't write() on output queue %#lx: %s", q, errno_to_str(q->error));
        output_queue_clear(q);
    }
    else {
        bf->buf_pos += size;
        if (bf->buf_pos < bf->buf_len) {
            /* Nothing */
        }
        else if (q->pool_size < 8) {
            list_remove(&bf->link);
            list_add_last(&bf->link, &q->pool);
            q->pool_size++;
        }
        else {
            list_remove(&bf->link);
            loc_free(bf);
        }
    }
    if (!list_is_empty(&q->queue)) {
        bf = link2buf(q->queue.next);
        q->post_io_request(bf);
    }
}

void output_queue_clear(OutputQueue * q) {
    while (!list_is_empty(&q->queue)) {
        OutputBuffer * bf = link2buf(q->queue.next);
        list_remove(&bf->link);
        loc_free(bf);
    }
    while (!list_is_empty(&q->pool)) {
        OutputBuffer * bf = link2buf(q->pool.next);
        list_remove(&bf->link);
        loc_free(bf);
    }
}
