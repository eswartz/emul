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
 * This module handles process/thread OS contexts and their state machine.
 */

#ifndef D_context
#define D_context

#include "mdep.h"
#include <sys/types.h>
#include "link.h"

extern LINK context_root;

#define ctxl2ctxp(A)    ((Context *)((char *)(A) - (int)&((Context *)0)->ctxl))
#define pidl2ctxp(A)    ((Context *)((char *)(A) - (int)&((Context *)0)->pidl))
#define cldl2ctxp(A)    ((Context *)((char *)(A) - (int)&((Context *)0)->cldl))

typedef struct Context Context;

struct Context {
    LINK                ctxl;
    LINK                pidl;
    LINK                cldl;
    LINK                children;
    Context *           parent;             /* if this is not main thread in a process, parent points to main thread */
    unsigned int        ref_count;          /* reference count, see context_lock() and context_unlock() */
    pid_t               pid;                /* process or thread identifier */
    pid_t               mem;                /* context memory space identifier */
    int                 stopped;            /* OS kernel has stopped this context */
    int                 stopped_by_bp;      /* stopped by breakpoint */
    int                 exiting;            /* context is about to exit */
    int                 exited;             /* context exited */
    int                 intercepted;        /* context is reported to a host as suspended */
    int                 pending_step;       /* context is executing single instruction step */
    int                 pending_intercept;  /* host is waiting for this context to be suspended */
    int                 pending_safe_event; /* safe events are waiting for this context to be stopped */
    int                 pending_attach;     /* waiting for this context to be attached */
    void *              pending_clone;      /* waiting for clone or fork to bind this to parent */
    unsigned long       pending_signals;    /* bitset of signals that were received, but not handled yet */
    int                 signal;             /* signal that stopped this context */
    int                 event;              /* tracing event code when signal is SIGTRAP */
    REG_SET             regs;               /* copy of context registers, updated when context stops */
    int                 regs_error;         /* if not 0, 'regs' is invalid */
    int                 regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
    void *              stack_trace;
    int                 trace_flags;
#if defined(_WRS_KERNEL)
    VXDBG_BP_INFO       bp_info;            /* breakpoint information */
    pid_t               bp_pid;             /* process or thread that hit breakpoint */
#endif
#if defined(WIN32)
    HANDLE              handle;
    DWORD               suspend_cnt;
    DWORD               context_exception_code;
    DWORD               pending_exception_code;
#endif
};

extern void ini_contexts(void);

extern char * event_name(int event);
extern char * signal_name(int signal);
extern char * context_state_name(Context * ctx);

/*
 * Convert PID to TCF Context ID
 */
extern char * pid2id(pid_t pid, pid_t parent);

/*
 * Convert Context to TCF Context ID
 */
extern char * ctx2id(Context * ctx);

/*
 * Get context thread ID
 */
extern char * thread_id(Context * ctx);

/*
 * Get context container ID
 */
extern char * container_id(Context * ctx);

/*
 * Convert TCF Context ID to PID
 */
extern pid_t id2pid(char * id, pid_t * parent);

/* 
 * Search Context record by TCF Context ID
 */
extern Context * id2ctx(char * id);

/*
 * Find a context by PID
 */
extern Context * context_find_from_pid(pid_t pid);

/*
 * Trigger self attachment e.g. of forked child
 */
extern int context_attach_self(void);

/*
 * Start tracing of a process.
 */
extern int context_attach(pid_t pid, Context ** ctx, int selfattach);

/*
 * Increment reference counter of Context object.
 * While ref count > 0 object will not be deleted even when context exits.
 */
extern void context_lock(Context * ctx);

/*
 * Decrement reference counter.
 * If ref count == 0, delete Context object.
 */
extern void context_unlock(Context * ctx);

extern int context_has_state(Context * ctx);
extern int context_stop(Context * ctx);
extern int context_continue(Context * ctx);
extern int context_single_step(Context * ctx);
extern int context_write_mem(Context * ctx, unsigned long address, void * buf, size_t size);
extern int context_read_mem(Context * ctx, unsigned long address, void * buf, size_t size);

typedef struct ContextEventListener {
    void (*context_created)(Context * ctx, void * client_data);
    void (*context_exited)(Context * ctx, void * client_data);
    void (*context_stopped)(Context * ctx, void * client_data);
    void (*context_started)(Context * ctx, void * client_data);
    void (*context_changed)(Context * ctx, void * client_data);
    void * client_data;
    struct ContextEventListener * next;
} ContextEventListener;

extern void add_context_event_listener(ContextEventListener * listener, void * client_data);

#ifdef _WRS_KERNEL
extern VXDBG_CLNT_ID vxdbg_clnt_id;
#endif

#endif
