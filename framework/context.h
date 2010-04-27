/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * This module handles process/thread OS contexts and their state machine.
 */

#ifndef D_context
#define D_context

#include "config.h"
#include "cpudefs.h"
#include "errors.h"
#include "link.h"

extern LINK context_root;

#define ctxl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, ctxl)))
#define cldl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, cldl)))

typedef void ContextAttachCallBack(int, Context *, void *);

struct Context {
    LINK                cldl;               /* Link that used to form a list of context chidren */
    LINK                children;           /* Context children double linked list */
    Context *           parent;             /* if this is not main thread in a process, parent points to main thread */
    unsigned int        ref_count;          /* reference count, see context_lock() and context_unlock() */
    pid_t               pid;                /* process or thread identifier */
    pid_t               mem;                /* context memory space identifier */
    int                 stopped;            /* OS kernel has stopped this context */
    int                 intercepted;        /* context is reported to a host as suspended */
    int                 exited;             /* context exited */
    int                 event_notification; /* set to 1 when calling one of ContextEventListener call-backs for this context */
#if ENABLE_DebugContext && !ENABLE_ContextProxy
    LINK                ctxl;               /* link that used to form a list of all contexts */
    LINK                pidl;               /* link that used to form a list of contexts with same hash code */
    int                 stopped_by_bp;      /* stopped by breakpoint */
    int                 stopped_by_exception;/* stopped by runtime exception (like SIGSEGV, etc.) */
    void *              stepping_over_bp;   /* if not NULL context is stepping over a breakpoint */
    int                 exiting;            /* context is about to exit */
    int                 pending_step;       /* context is executing single instruction step */
    int                 pending_intercept;  /* host is waiting for this context to be suspended */
    unsigned long       pending_signals;    /* bitset of signals that were received, but not handled yet */
    unsigned long       sig_dont_stop;      /* bitset of signals that should not be intercepted by the debugger */
    unsigned long       sig_dont_pass;      /* bitset of signals that should not be delivered to the context */
    int                 signal;             /* signal that stopped this context */
    RegisterData *      regs;               /* copy of context registers, updated when context stops */
    size_t              regs_size;          /* size of data pointed by "regs" */
    ErrorReport *       regs_error;         /* if not NULL, 'regs' is invalid */
    int                 regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
#endif /* ENABLE_DebugContext */
};

/*
 * Convert PID to TCF Context ID
 */
extern char * pid2id(pid_t pid, pid_t parent);

/*
 * Get context ID
 */
extern char * ctx2id(Context * ctx);

/*
 * Convert TCF Context ID to PID
 */
extern pid_t id2pid(const char * id, pid_t * parent);

/*
 * Search Context record by TCF Context ID
 */
extern Context * id2ctx(const char * id);

#if ENABLE_DebugContext

/*
 * Register an extension of struct Context.
 * Return offset of extension data area.
 * Additional memory of given size will be allocated in each context struct.
 * Client are allowed to call this function only during initialization.
 */
extern size_t context_extension(size_t size);

/*
 * Get human redable name of current state of a context.
 */
extern const char * context_state_name(Context * ctx);

/*
 * Get state change reason of a context.
 * Reason can be any text, but if it is one of predefined strings,
 * a generic client might be able to handle it better.
 */
extern const char * context_suspend_reason(Context * ctx);

/*
 * Find a context by PID
 * Both process and main thread can have same PID.
 * 'thread' = 0: search for process, otherwise search for a thread.
 */
extern Context * context_find_from_pid(pid_t pid, int thread);

/*
 * Trigger self attachment e.g. of forked child
 * Only available on Linux/Unix.
 */
extern int context_attach_self(void);

/*
 * Start tracing of a process.
 * Client provides a call-back function that will be called when context is attached.
 * The callback function args are error code, the context and client data.
 */
extern int context_attach(pid_t pid, ContextAttachCallBack * done, void * client_data, int selfattach);

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

/*
 * Return 1 if the context has running/stopped state, return 0 othewise
 */
extern int context_has_state(Context * ctx);

/*
 * Stop execution of the context.
 * Execution can be resumed by calling context_continue()
 * Return -1 and set errno if the context cannot be stopped.
 */
extern int context_stop(Context * ctx);

/*
 * Resume execution of the context.
 * Return -1 and set errno if the context cannot be resumed.
 */
extern int context_continue(Context * ctx);

/*
 * Perform single instruction step on the context.
 * Return -1 and set errno if the context cannot be single stepped.
 */
extern int context_single_step(Context * ctx);

/*
 * Write context memory.
 * Implementation calls check_breakpoints_on_memory_write() before writing to context memory,
 * which can change contents of the buffer.
 * Return -1 and set errno if the context memory cannot be written.
 */
extern int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Read context memory.
 * Implementation calls check_breakpoints_on_memory_read() after reading context memory.
 * Return -1 and set errno if the context cannot be read.
 */
extern int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Return context memory word size in bytes.
 */
extern unsigned context_word_size(Context * ctx);

/*
 * Functions that notify listeners of various context event.
 * They are not supposed to be called by clients.
 */
extern void send_context_created_event(Context * ctx);
extern void send_context_changed_event(Context * ctx);
extern void send_context_stopped_event(Context * ctx);
extern void send_context_started_event(Context * ctx);
extern void send_context_exited_event(Context * ctx);

/*
 * Functions that are used to create a Context.
 * They are not supposed to be called by clients.
 */
extern Context * create_context(pid_t pid, size_t regs_size);
extern void link_context(Context * ctx);

extern void ini_contexts(void);
extern void init_contexts_sys_dep(void);

#endif /* ENABLE_DebugContext */

typedef struct ContextEventListener {
    void (*context_created)(Context * ctx, void * client_data);
    void (*context_exited )(Context * ctx, void * client_data);
    void (*context_stopped)(Context * ctx, void * client_data);
    void (*context_started)(Context * ctx, void * client_data);
    void (*context_changed)(Context * ctx, void * client_data);
    void (*context_disposed)(Context * ctx, void * client_data);
} ContextEventListener;

extern void add_context_event_listener(ContextEventListener * listener, void * client_data);

#endif
