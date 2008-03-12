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

#include <assert.h>
#include <stddef.h>
#include "mdep.h"
#include "myalloc.h"
#include "trace.h"
#include "events.h"
#include "link.h"
#include "asyncreq.h"
#include "errors.h"

static LINK wtlist;
static pthread_mutex_t wtlock;

typedef struct WorkerThread {
    LINK wtlink;
    AsyncReqInfo * req;
    pthread_cond_t cond;
    pthread_t thread;
} WorkerThread;

#define wtlink2wt(A)  ((WorkerThread *)((char *)(A) - offsetof(WorkerThread, wtlink)))

static void * worker_thread_handler(void * x) {
    WorkerThread * wt = x;

    for (;;) {
        AsyncReqInfo * req = wt->req;

        assert(req != NULL);
        req->error = 0;
        switch(req->type) {
        case AsyncReqReadSock:          /* Read from socket */
            req->u.sio.rval = recv(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, 0);
            if (req->u.sio.rval == -1) {
                req->error = errno;
            }
            break;

        case AsyncReqWriteSock:         /* Write to socket */
            req->u.sio.rval = send(req->u.sio.sock, req->u.sio.bufp, req->u.sio.bufsz, 0);
            if (req->u.sio.rval == -1) {
                req->error = errno;
            }
            break;

        case AsyncReqAccept:            /* Accept socket connections */
            req->u.acc.rval = accept(req->u.acc.sock, req->u.acc.addr, req->u.acc.addrlen);
            if (req->u.acc.rval == -1) {
                req->error = errno;
            }
            break;

        case AsyncReqConnect:           /* Connect to socket */
            req->u.acc.rval = connect(req->u.con.sock, req->u.con.addr, req->u.con.addrlen);
            if (req->u.con.rval == -1) {
                req->error = errno;
            }
            break;

// Platform dependant IO methods
#if defined(WIN32) || defined(__CYGWIN__)
#elif defined(_WRS_KERNEL)
#else
        case AsyncReqWaitpid:           /* Wait for process change */
            req->u.wpid.rval = waitpid(req->u.wpid.pid, &req->u.wpid.status, req->u.wpid.options);
            if (req->u.con.rval == -1) {
                req->error = errno;
            }
            break;
#endif

        default:
            req->error = ENOSYS;
            break;
        }
        trace(LOG_ASYNCREQ, "async_req_complete: req %p, type %d, error %d", req, req->type, req->error);
        pthread_mutex_lock(&wtlock);
        /* Post event inside lock to make sure a new worker thread is
         * not created unnecessarily */
        post_event(req->done, req);
        wt->req = NULL;
        list_add_last(&wt->wtlink, &wtlist);
        for (;;) {
            LINK *link;

            pthread_cond_wait(&wt->cond, &wtlock);
            if (wt->req != NULL) break;
            for (link = wtlist.next; link != &wtlist; link = link->next) {
                if (wtlink2wt(link) == wt) break;
            }
            assert(link != &wtlist);
        }
        pthread_mutex_unlock(&wtlock);
    }
    return NULL;
}

void async_req_post(AsyncReqInfo *req) {
    WorkerThread * wt;

    trace(LOG_ASYNCREQ, "async_req_post: req %p, type %d", req, req->type);
    if (wtlist.next == NULL) list_init(&wtlist);
    pthread_mutex_lock(&wtlock);
    if (list_is_empty(&wtlist)) {
        int error;

        wt = loc_alloc_zero(sizeof *wt);
        pthread_cond_init(&wt->cond, NULL);
        wt->req = req;
        error = pthread_create(&wt->thread, &pthread_create_attr, worker_thread_handler, wt);
        if (error) {
            trace(LOG_ALWAYS, "Can't create a worker thread: %d %s", error, errno_to_str(error));
            loc_free(wt);
            req->error = error;
            post_event(req->done, req);
        }
    }
    else {
        wt = wtlink2wt(wtlist.next);
        list_remove(&wt->wtlink);
        assert(wt->req == NULL);
        wt->req = req;
        pthread_cond_signal(&wt->cond);
    }
    pthread_mutex_unlock(&wtlock);
}

void ini_asyncreq(void) {
    pthread_mutex_init(&wtlock, NULL);
}
