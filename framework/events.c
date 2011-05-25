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

#include <config.h>
#include <time.h>
#include <assert.h>
#include <string.h>
#include <framework/myalloc.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/events.h>

typedef struct event_node event_node;

struct event_node {
    event_node *        next;
    struct timespec     runtime;
    EventCallBack *     handler;
    void *              arg;
};

#if defined(WIN32)
   static DWORD event_thread;
#  define current_thread GetCurrentThreadId()
#  define is_event_thread (event_thread == current_thread)
#else
   static pthread_t event_thread;
#  define current_thread pthread_self()
#  define is_event_thread pthread_equal(event_thread, current_thread)
#endif

#if ENABLE_Trace
#  undef trace
#  define trace if ((log_mode & LOG_EVENTCORE) && log_file) print_trace
#endif

#define EVENT_BUF_SIZE 0x200
static event_node event_buf[EVENT_BUF_SIZE];

static pthread_mutex_t event_lock;
static pthread_cond_t event_cond;
static pthread_cond_t cancel_cond;

static event_node * event_queue = NULL;
static event_node * event_last = NULL;
static event_node * timer_queue = NULL;
static event_node * free_queue = NULL;
static EventCallBack * cancel_handler = NULL;
static void * cancel_arg = NULL;
static int process_events = 0;

static int time_cmp(const struct timespec * tv1, const struct timespec * tv2) {
    if (tv1->tv_sec < tv2->tv_sec) return -1;
    if (tv1->tv_sec > tv2->tv_sec) return 1;
    if (tv1->tv_nsec < tv2->tv_nsec) return -1;
    if (tv1->tv_nsec > tv2->tv_nsec) return 1;
    return 0;
}

/*
 * Add microsecond value to timespec.
 */
static void time_add_usec(struct timespec * tv, unsigned long usec) {
    tv->tv_sec += usec / 1000000;
    tv->tv_nsec += (usec % 1000000) * 1000;
    if (tv->tv_nsec >= 1000000000) {
        tv->tv_sec++;
        tv->tv_nsec -= 1000000000;
    }
}

static void post_from_bg_thread(EventCallBack * handler, void * arg, unsigned long delay) {
    event_node * ev;
    event_node * next;
    event_node * prev;

    check_error(pthread_mutex_lock(&event_lock));
    if (cancel_handler == handler && cancel_arg == arg) {
        cancel_handler = NULL;
        check_error(pthread_cond_signal(&cancel_cond));
        check_error(pthread_mutex_unlock(&event_lock));
        return;
    }
    ev = (event_node *)loc_alloc_zero(sizeof(event_node));
    if (clock_gettime(CLOCK_REALTIME, &ev->runtime)) check_error(errno);
    time_add_usec(&ev->runtime, delay);
    ev->handler = handler;
    ev->arg = arg;

    prev = NULL;
    next = timer_queue;
    while (next != NULL && time_cmp(&ev->runtime, &next->runtime) >= 0) {
        prev = next;
        next = next->next;
    }
    ev->next = next;
    if (prev == NULL) {
        timer_queue = ev;
        check_error(pthread_cond_signal(&event_cond));
    }
    else {
        prev->next = ev;
    }
    trace(LOG_EVENTCORE, "post_event: event %#lx, handler %#lx, arg %#lx, runtime %02d%02d.%03d",
        ev, ev->handler, ev->arg,
        ev->runtime.tv_sec / 60 % 60, ev->runtime.tv_sec % 60, ev->runtime.tv_nsec / 1000000);
    check_error(pthread_mutex_unlock(&event_lock));
}

void post_event_with_delay(EventCallBack * handler, void * arg, unsigned long delay) {
    if (is_event_thread) {
        event_node * ev;
        event_node * next;
        event_node * prev;

        ev = free_queue;
        if (ev != NULL) free_queue = ev->next;
        else ev = (event_node *)loc_alloc(sizeof(event_node));
        if (clock_gettime(CLOCK_REALTIME, &ev->runtime)) check_error(errno);
        time_add_usec(&ev->runtime, delay);
        ev->handler = handler;
        ev->arg = arg;

        check_error(pthread_mutex_lock(&event_lock));
        prev = NULL;
        next = timer_queue;
        while (next != NULL && time_cmp(&ev->runtime, &next->runtime) >= 0) {
            prev = next;
            next = next->next;
        }
        ev->next = next;
        if (prev == NULL) {
            timer_queue = ev;
        }
        else {
            prev->next = ev;
        }
        check_error(pthread_mutex_unlock(&event_lock));

        trace(LOG_EVENTCORE, "post_event: event %#lx, handler %#lx, arg %#lx, runtime %02d%02d.%03d",
            ev, ev->handler, ev->arg,
            ev->runtime.tv_sec / 60 % 60, ev->runtime.tv_sec % 60, ev->runtime.tv_nsec / 1000000);
    }
    else {
        post_from_bg_thread(handler, arg, delay);
    }
}

void post_event(EventCallBack * handler, void * arg) {
    if (is_event_thread) {
        event_node * ev = free_queue;
        if (ev != NULL) free_queue = ev->next;
        else ev = (event_node *)loc_alloc(sizeof(event_node));
        ev->handler = handler;
        ev->arg = arg;
        ev->next = NULL;
        if (event_queue == NULL) {
            assert(event_last == NULL);
            event_last = event_queue = ev;
        }
        else {
            event_last->next = ev;
            event_last = ev;
        }
        trace(LOG_EVENTCORE, "post_event: event %#lx, handler %#lx, arg %#lx", ev, ev->handler, ev->arg);
    }
    else {
        post_from_bg_thread(handler, arg, 0);
    }
}

int cancel_event(EventCallBack * handler, void * arg, int wait) {
    event_node * ev;
    event_node * prev;

    assert(is_dispatch_thread());
    assert(handler != NULL);
    assert(cancel_handler == NULL);

    trace(LOG_EVENTCORE, "cancel_event: handler %#lx, arg %#lx, wait %d", handler, arg, wait);
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
            loc_free(ev);
            return 1;
        }
        prev = ev;
        ev = ev->next;
    }

    check_error(pthread_mutex_lock(&event_lock));
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
            loc_free(ev);
            check_error(pthread_mutex_unlock(&event_lock));
            return 1;
        }
        prev = ev;
        ev = ev->next;
    }

    if (!wait) {
        check_error(pthread_mutex_unlock(&event_lock));
        return 0;
    }

    cancel_handler = handler;
    cancel_arg = arg;
    do check_error(pthread_cond_wait(&cancel_cond, &event_lock));
    while (cancel_handler != NULL);
    check_error(pthread_mutex_unlock(&event_lock));
    return 1;
}

int is_dispatch_thread(void) {
    return is_event_thread;
}

void ini_events_queue(void) {
    int i;
    /* Initial thread is event dispatcher. */
    event_thread = current_thread;
    check_error(pthread_mutex_init(&event_lock, NULL));
    check_error(pthread_cond_init(&event_cond, NULL));
    check_error(pthread_cond_init(&cancel_cond, NULL));
    for (i = 0; i < EVENT_BUF_SIZE; i++) {
        event_node * ev = event_buf + i;
        ev->next = free_queue;
        free_queue = ev;
    }
}

void cancel_event_loop(void) {
    process_events = 0;
}

void run_event_loop(void) {
    unsigned event_cnt = 0;
    assert(is_dispatch_thread());

    process_events = 1;
    while (process_events) {

        event_node * ev = NULL;

        if (event_queue == NULL || (event_cnt & 0x3fu) == 0) {
            check_error(pthread_mutex_lock(&event_lock));
            if (timer_queue != NULL) {
                struct timespec timenow;
                if (clock_gettime(CLOCK_REALTIME, &timenow)) {
                    check_error(errno);
                }
                if (time_cmp(&timer_queue->runtime, &timenow) <= 0) {
                    ev = timer_queue;
                    timer_queue = ev->next;
                }
            }
            check_error(pthread_mutex_unlock(&event_lock));
        }

        if (ev == NULL && event_queue != NULL) {
            ev = event_queue;
            event_queue = ev->next;
            if (event_queue == NULL) {
                assert(event_last == ev);
                event_last = NULL;
            }
        }

        if (ev == NULL) {
            check_error(pthread_mutex_lock(&event_lock));
            if (timer_queue != NULL) {
                int error = pthread_cond_timedwait(&event_cond, &event_lock, &timer_queue->runtime);
                if (error && error != ETIMEDOUT) check_error(error);
            }
            else {
                check_error(pthread_cond_wait(&event_cond, &event_lock));
            }
            check_error(pthread_mutex_unlock(&event_lock));
        }
        else {
            trace(LOG_EVENTCORE, "run_event_loop: event %#lx, handler %#lx, arg %#lx", ev, ev->handler, ev->arg);
            ev->handler(ev->arg);
            if (ev >= event_buf && ev < event_buf + EVENT_BUF_SIZE) {
                ev->next = free_queue;
                free_queue = ev;
            }
            else {
                loc_free(ev);
            }
            event_cnt++;
        }
    }
}
