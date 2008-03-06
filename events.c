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
 * Event queue manager.
 * Event is a data pointer plus a function pointer (a.k.a. event handler).
 *
 * Posting event means placing event into event queue.
 * Dispatching event means removing event from the queue and then calling
 * event function with event data as argument.
 *
 * All events are dispatched by single thread - dispatch thread. This makes it safe
 * to access global data structures from event handlers without further synchronization,
 * while allows for high level of concurrency.
 */

#include <time.h>
#include <assert.h>
#include "mdep.h"
#include "myalloc.h"
#include "trace.h"
#include "events.h"

typedef struct event_node event_node;

struct event_node {
    event_node *        next;
    struct timespec     runtime;
    void                (*handler)(void *);
    void                *arg;
};

pthread_t event_thread = 0;

static pthread_mutex_t event_lock;
static pthread_cond_t event_cond;
static pthread_cond_t cancel_cond;

static event_node * event_queue = NULL;
static event_node * event_last = NULL;
static event_node * timer_queue = NULL;
static event_node * free_queue = NULL;
static int free_queue_size = 0;
static void (*cancel_handler)(void *);
static void *cancel_arg;

static int time_cmp(const struct timespec *tv1, const struct timespec *tv2) {
    if (tv1->tv_sec < tv2->tv_sec) return -1;
    if (tv1->tv_sec > tv2->tv_sec) return 1;
    if (tv1->tv_nsec < tv2->tv_nsec) return -1;
    if (tv1->tv_nsec > tv2->tv_nsec) return 1;
    return 0;
}

/*
 * Add microsecond value to timeval.
 */
static void time_add_usec(struct timespec *tv, unsigned long usec) {
    tv->tv_sec += usec / 1000000;
    tv->tv_nsec += (usec % 1000000) * 1000;
    if (tv->tv_nsec >= 1000000000) {
        tv->tv_sec++;
        tv->tv_nsec -= 1000000000;
    }
}

static event_node * alloc_node(void (*handler)(void *), void *arg) {
    event_node * node;
    if (free_queue != NULL) {
        node = free_queue;
        free_queue = node->next;
        free_queue_size--;
    }
    else {
        node = (event_node *)loc_alloc(sizeof(event_node));
    }
    memset(node, 0, sizeof(event_node));
    node->handler = handler;
    node->arg = arg;
    return node;
}

static void free_node(event_node * node) {
    if (free_queue_size < 500) {
        node->next = free_queue;
        free_queue = node;
        free_queue_size++;
    }
    else {
        loc_free(node);
    }
}

void post_event_with_delay(void (*handler)(void *), void *arg, unsigned long delay) {
    event_node * ev;
    event_node * qp;
    event_node ** qpp;

    pthread_mutex_lock(&event_lock);
    if (cancel_handler == handler && cancel_arg == arg) {
        cancel_handler = NULL;
        pthread_cond_signal(&cancel_cond);
        pthread_mutex_unlock(&event_lock);
        return;
    }
    ev = alloc_node(handler, arg);
    if (clock_gettime(CLOCK_REALTIME, &ev->runtime)) {
        perror("clock_gettime");
        exit(1);
    }
    time_add_usec(&ev->runtime, delay);

    qpp = &timer_queue;
    while ((qp = *qpp) != 0 && time_cmp(&ev->runtime, &qp->runtime) >= 0) {
        qpp = &qp->next;
    }
    ev->next = qp;
    *qpp = ev;
    if (timer_queue == ev) {
        pthread_cond_signal(&event_cond);
    }
    trace(LOG_EVENTCORE, "post_event: event %#x handler %#x arg %#x runtime %02d%02d.%03d",
        ev, ev->handler, ev->arg, ev->runtime.tv_sec/60%60, ev->runtime.tv_sec%60, ev->runtime.tv_nsec/1000000);
    pthread_mutex_unlock(&event_lock);
}

void post_event(void (*handler)(void *), void *arg) {
    event_node * ev;

    pthread_mutex_lock(&event_lock);
    if (cancel_handler == handler && cancel_arg == arg) {
        cancel_handler = NULL;
        pthread_cond_signal(&cancel_cond);
        pthread_mutex_unlock(&event_lock);
        return;
    }
    ev = alloc_node(handler, arg);

    if (event_queue == NULL) {
        assert(event_last == NULL);
        event_last = event_queue = ev;
        pthread_cond_signal(&event_cond);
    }
    else {
        event_last->next = ev;
        event_last = ev;
    }
    trace(LOG_EVENTCORE, "post_event: event %#x handler %#x arg %#x", ev, ev->handler, ev->arg);
    pthread_mutex_unlock(&event_lock);
}

int cancel_event(void (*handler)(void *), void *arg, int wait) {
    event_node * ev;
    event_node * prev;

    assert(is_dispatch_thread());
    assert(handler != NULL);
    assert(cancel_handler == NULL);

    trace(LOG_EVENTCORE, "cancel_event: handler %#x, arg %#x, wait %d", handler, arg, wait);
    pthread_mutex_lock(&event_lock);
    prev = NULL;
    ev = event_queue;
    while (ev != NULL) {
        if (ev->handler == handler && ev->arg == arg) {
            if (prev == NULL) {
                event_queue = ev->next;
                if (event_queue == NULL) {
                    assert(event_last == ev);
                    event_last = NULL;
                }
            }
            else {
                prev->next = ev->next;
                if (ev->next == NULL) {
                    assert(event_last == ev);
                    event_last = prev;
                }
            }
            free_node(ev);
            pthread_mutex_unlock(&event_lock);
            return 1;
        }
        prev = ev;
        ev = ev->next;
    }

    prev = NULL;
    ev = timer_queue;
    while (ev != NULL) {
        if (ev->handler == handler && ev->arg == arg) {
            if (prev == NULL) {
                timer_queue = ev->next;
            }
            else {
                prev->next = ev->next;
            }
            free_node(ev);
            pthread_mutex_unlock(&event_lock);
            return 1;
        }
        prev = ev;
        ev = ev->next;
    }

    if (!wait) {
        pthread_mutex_unlock(&event_lock);
        return 0;
    }

    cancel_handler = handler;
    cancel_arg = arg;
    do {
        pthread_cond_wait(&cancel_cond, &event_lock);
    } while (cancel_handler != NULL);
    pthread_mutex_unlock(&event_lock);
    return 1;
}

int is_dispatch_thread(void) {
    return event_thread == pthread_self();
}

void ini_events_queue(void) {
    /* Initial thread is event dispatcher. */
    event_thread = pthread_self();
    pthread_mutex_init(&event_lock, NULL);
    pthread_cond_init(&event_cond, NULL);
    pthread_cond_init(&cancel_cond, NULL);
}

void run_event_loop(void) {
    assert(is_dispatch_thread());
    pthread_mutex_lock(&event_lock);
    for (;;) {
        event_node * ev = NULL;
        if (event_queue != NULL) {
            ev = event_queue;
            event_queue = ev->next;
            if (event_queue == NULL) {
                assert(event_last == ev);
                event_last = NULL;
            }
        }
        else if (timer_queue != NULL) {
            struct timespec timenow;
            if (clock_gettime(CLOCK_REALTIME, &timenow)) {
                perror("clock_gettime");
                exit(1);
            }
            ev = timer_queue;
            if (time_cmp(&timer_queue->runtime, &timenow) > 0) {
                pthread_cond_timedwait(&event_cond, &event_lock, &ev->runtime);
                continue;
            }
            timer_queue = ev->next;
        }
        else {
            pthread_cond_wait(&event_cond, &event_lock);
            continue;
        }

        pthread_mutex_unlock(&event_lock);
        trace(LOG_EVENTCORE, "run_event_loop: event %#x handler %#x arg %#x", ev, ev->handler, ev->arg);
        ev->handler(ev->arg);
        pthread_mutex_lock(&event_lock);
        free_node(ev);
    }
}


