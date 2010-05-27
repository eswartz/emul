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
#include <assert.h>
#include <stddef.h>
#if defined(WIN32)
#elif defined(_WRS_KERNEL)
#else
#  include <sys/wait.h>
#endif
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/events.h>
#include <framework/link.h>
#include <framework/asyncreq.h>
#include <framework/errors.h>

#define MAX_WORKER_THREADS 32

static LINK wtlist;
static int wtlist_size = 0;
static pthread_mutex_t wtlock;

typedef struct WorkerThread {
    LINK wtlink;
    AsyncReqInfo * req;
    pthread_cond_t cond;
    pthread_t thread;
} WorkerThread;

#define wtlink2wt(A)  ((WorkerThread *)((char *)(A) - offsetof(WorkerThread, wtlink)))

static void * worker_thread_handler(void * x) {
    WorkerThread * wt = (WorkerThread *)x;

    for (;;) {
        AsyncReqInfo * req = wt->req;

        assert(req != NULL);
        req->error = 0;
        switch(req->type) {
        case AsyncReqRead:              /* File read */
            req->u.fio.rval = read(req->u.fio.fd, req->u.fio.bufp, req->u.fio.bufsz);
            if (req->u.fio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqWrite:             /* File write */
            req->u.fio.rval = write(req->u.fio.fd, req->u.fio.bufp, req->u.fio.bufsz);
            if (req->u.fio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqSeekRead:          /* File read at offset */
            req->u.fio.rval = pread(req->u.fio.fd, req->u.fio.bufp, req->u.fio.bufsz, req->u.fio.offset);
            if (req->u.fio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqSeekWrite:         /* File write at offset */
            req->u.fio.rval = pwrite(req->u.fio.fd, req->u.fio.bufp, req->u.fio.bufsz, req->u.fio.offset);
            if (req->u.fio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqRecv:              /* Socket recv */
            req->u.sio.rval = recv(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, req->u.sio.flags);
            if (req->u.sio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqSend:              /* Socket send */
            req->u.sio.rval = send(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, req->u.sio.flags);
            if (req->u.sio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqRecvFrom:          /* Socket recvfrom */
            req->u.sio.rval = recvfrom(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, req->u.sio.flags, req->u.sio.addr, &req->u.sio.addrlen);
            if (req->u.sio.rval == -1) {
                req->error = errno;
                trace(LOG_ASYNCREQ, "AsyncReqRecvFrom: req %p, type %d, error %d", req, req->type, req->error);
                assert(req->error);
            }
            break;

        case AsyncReqSendTo:            /* Socket sendto */
            req->u.sio.rval = sendto(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, req->u.sio.flags, req->u.sio.addr, req->u.sio.addrlen);
            if (req->u.sio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;

        case AsyncReqAccept:            /* Accept socket connections */
            req->u.acc.rval = accept(req->u.acc.sock, req->u.acc.addr, req->u.acc.addr ? &req->u.acc.addrlen : NULL);
            if (req->u.acc.rval == -1) {
                req->error = errno;
                trace(LOG_ASYNCREQ, "AsyncReqAccept: req %p, type %d, error %d", req, req->type, req->error);
                assert(req->error);
            }
            break;

        case AsyncReqConnect:           /* Connect to socket */
            req->u.con.rval = connect(req->u.con.sock, req->u.con.addr, req->u.con.addrlen);
            if (req->u.con.rval == -1) {
                req->error = errno;
                trace(LOG_ASYNCREQ, "AsyncReqConnect: req %p, type %d, error %d", req, req->type, req->error);
                assert(req->error);
            }
            break;

/* Platform dependant IO methods */
#if defined(WIN32)
#elif defined(_WRS_KERNEL)
#else
        case AsyncReqWaitpid:           /* Wait for process change */
            req->u.wpid.rval = waitpid(req->u.wpid.pid, &req->u.wpid.status, req->u.wpid.options);
            if (req->u.wpid.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;
#endif
        case AsyncReqSelect:
        {
            struct timeval tv;
            tv.tv_sec = (long)req->u.select.timeout.tv_sec;
            tv.tv_usec = req->u.select.timeout.tv_nsec / 1000;
            req->u.select.rval = select(req->u.select.nfds, &req->u.select.readfds,
                        &req->u.select.writefds, &req->u.select.errorfds, &tv);
            if (req->u.select.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;
        }
        case AsyncReqClose:
            req->u.fio.rval = close(req->u.fio.fd);
            if (req->u.fio.rval == -1) {
                req->error = errno;
                assert(req->error);
            }
            break;
        default:
            req->error = ENOSYS;
            break;
        }
        trace(LOG_ASYNCREQ, "async_req_complete: req %p, type %d, error %d", req, req->type, req->error);
        check_error(pthread_mutex_lock(&wtlock));
        /* Post event inside lock to make sure a new worker thread is
         * not created unnecessarily */
        post_event(req->done, req);
        wt->req = NULL;
        if (wtlist_size >= MAX_WORKER_THREADS) {
            check_error(pthread_cond_destroy(&wt->cond));
            loc_free(wt);
            check_error(pthread_mutex_unlock(&wtlock));
            break;
        }
        list_add_last(&wt->wtlink, &wtlist);
        wtlist_size++;
        for (;;) {
            check_error(pthread_cond_wait(&wt->cond, &wtlock));
            if (wt->req != NULL) break;
        }
        check_error(pthread_mutex_unlock(&wtlock));
    }
    return NULL;
}

#if ENABLE_AIO
static void aio_done(union sigval arg) {
    AsyncReqInfo * req = (AsyncReqInfo *)arg.sival_ptr;
    req->u.fio.rval = aio_return(&req->u.fio.aio);
    if (req->u.fio.rval < 0) req->error = aio_error(&req->u.fio.aio);
    post_event(req->done, req);
}
#endif

void async_req_post(AsyncReqInfo * req) {
    WorkerThread * wt;

    trace(LOG_ASYNCREQ, "async_req_post: req %p, type %d", req, req->type);
    assert(req->done != NULL);

#if ENABLE_AIO
    {
        int res = 0;
        switch (req->type) {
        case AsyncReqSeekRead:
        case AsyncReqSeekWrite:
            memset(&req->u.fio.aio, 0, sizeof(req->u.fio.aio));
            req->u.fio.aio.aio_fildes = req->u.fio.fd;
            req->u.fio.aio.aio_offset = req->u.fio.offset;
            req->u.fio.aio.aio_buf = req->u.fio.bufp;
            req->u.fio.aio.aio_nbytes = req->u.fio.bufsz;
            req->u.fio.aio.aio_sigevent.sigev_notify = SIGEV_THREAD;
            req->u.fio.aio.aio_sigevent.sigev_notify_function = aio_done;
            req->u.fio.aio.aio_sigevent.sigev_value.sival_ptr = req;
            res = req->type == AsyncReqSeekWrite ?
                aio_write(&req->u.fio.aio) :
                aio_read(&req->u.fio.aio);
            if (res < 0) {
                req->u.fio.rval = -1;
                req->error = errno;
                post_event(req->done, req);
            }
            return;
        }
    }
#endif
    check_error(pthread_mutex_lock(&wtlock));
    if (list_is_empty(&wtlist)) {
        assert(wtlist_size == 0);
        wt = (WorkerThread *)loc_alloc_zero(sizeof *wt);
        wt->req = req;
        check_error(pthread_cond_init(&wt->cond, NULL));
        check_error(pthread_create(&wt->thread, &pthread_create_attr, worker_thread_handler, wt));
    }
    else {
        wt = wtlink2wt(wtlist.next);
        list_remove(&wt->wtlink);
        wtlist_size--;
        assert(wt->req == NULL);
        wt->req = req;
        check_error(pthread_cond_signal(&wt->cond));
    }
    check_error(pthread_mutex_unlock(&wtlock));
}

void ini_asyncreq(void) {
    list_init(&wtlist);
    wtlist_size = 0;
    check_error(pthread_mutex_init(&wtlock, NULL));
}
