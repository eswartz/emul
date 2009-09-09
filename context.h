/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
#include "link.h"

extern LINK context_root;

#define ctxl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, ctxl)))
#define pidl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, pidl)))
#define cldl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, cldl)))

typedef uintptr_t ContextAddress; /* Type to represent byted address inside context memory */

typedef struct Context Context;

typedef void ContextAttachCallBack(int, Context *, void *);

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
    void *              stepping_over_bp;   /* if not NULL context is stepping over a breakpoint */
    char **             bp_ids;             /* if stopped by breakpoint, contains NULL-terminated list of breakpoint IDs */
    int                 exiting;            /* context is about to exit */
    int                 exited;             /* context exited */
    int                 intercepted;        /* context is reported to a host as suspended */
    int                 pending_step;       /* context is executing single instruction step */
    int                 pending_intercept;  /* host is waiting for this context to be suspended */
    int                 pending_safe_event; /* safe events are waiting for this context to be stopped */
    unsigned long       pending_signals;    /* bitset of signals that were received, but not handled yet */
    unsigned long       sig_dont_stop;      /* bitset of signals that should not be intercepted by the debugger */
    unsigned long       sig_dont_pass;      /* bitset of signals that should not be delivered to the context */
    int                 signal;             /* signal that stopped this context */
    REG_SET             regs;               /* copy of context registers, updated when context stops */
    int                 regs_error;         /* if not 0, 'regs' is invalid */
    int                 regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
    void *              stack_trace;        /* pointer to StackTrace service data cache */
    void *              memory_map;         /* pointer to MemoryMap service data cache */
#if ENABLE_RCBP_TEST
    int                 test_process;       /* if not 0, the process is test process started by Diagnostics service */
#endif

/* OS dependant context attributes */
#if defined(_WRS_KERNEL)
    VXDBG_BP_INFO       bp_info;            /* breakpoint information */
    pid_t               bp_pid;             /* process or thread that hit breakpoint */
    int                 event;
#elif defined(WIN32)
    HANDLE              handle;
    HANDLE              file_handle;
    DWORD64             base_address;
    int                 module_loaded;
    int                 module_unloaded;
    HANDLE              module_handle;
    DWORD64             module_address;
    int                 debug_started;
    EXCEPTION_DEBUG_INFO pending_event;
    EXCEPTION_DEBUG_INFO suspend_reason;
    int                 context_stopped_async_pending;
#else /* Linux/Unix */
    ContextAttachCallBack * attach_callback;
    void *              attach_data;
    void *              pending_events;  /* waiting for clone or fork to bind this to parent */
    int                 ptrace_flags;
    int                 ptrace_event;
    int                 syscall_enter;
    int                 syscall_exit;
    int                 syscall_id;
    ContextAddress      syscall_pc;
    int                 end_of_step;
#endif
#if ENABLE_ELF
    int                 debug_structure_searched;
    ContextAddress      debug_structure_address;
    ContextAddress      loader_state;
#endif
};

extern void ini_contexts(void);

extern char * signal_name(int signal);
extern char * signal_description(int signal);
extern unsigned signal_code(int signal);

extern char * context_state_name(Context * ctx);
extern char * context_suspend_reason(Context * ctx);

/*
 * Convert PID to TCF Context ID
 */
extern char * pid2id(pid_t pid, pid_t parent);

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

#if ENABLE_DebugContext

/*
 * Find a context by PID
 */
extern Context * context_find_from_pid(pid_t pid);

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
 * Make sure check_breakpoints_on_memory_write() has been called before writing to context memory.
 * Return -1 and set errno if the context memory cannot be written.
 */
extern int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Read context memory.
 * Make sure check_breakpoints_on_memory_read() will be called after reading context memory.
 * Return -1 and set errno if the context cannot be read.
 */
extern int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Return context memory word size in bytes.
 */
extern unsigned context_word_size(Context * ctx);

#else /* ENABLE_DebugContext */

#define context_find_from_pid(pid) NULL
#define context_attach(pid, done, client_data, selfattach) (errno = ERR_UNSUPPORTED, -1)
#define context_attach_self() (errno = ERR_UNSUPPORTED, -1)
#define context_has_state(ctx) 0
#define context_read_mem(ctx, address, buf, size) (errno = ERR_INV_CONTEXT, -1)
#define context_write_mem(ctx, address, buf, size) (errno = ERR_INV_CONTEXT, -1)
#define context_word_size(ctx) sizeof(void *)

#endif /* ENABLE_DebugContext */

typedef struct ContextEventListener {
    void (*context_created)(Context * ctx, void * client_data);
    void (*context_exited )(Context * ctx, void * client_data);
    void (*context_stopped)(Context * ctx, void * client_data);
    void (*context_started)(Context * ctx, void * client_data);
    void (*context_changed)(Context * ctx, void * client_data);
    /* Private: */
    void * client_data;
    struct ContextEventListener * next;
} ContextEventListener;

extern void add_context_event_listener(ContextEventListener * listener, void * client_data);

#ifdef _WRS_KERNEL
extern VXDBG_CLNT_ID vxdbg_clnt_id;
#endif

#endif
