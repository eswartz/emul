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
 * This module handles process/thread OS contexts and their state machine.
 */

#ifndef D_context
#define D_context

#include <config.h>
#include <framework/cpudefs.h>
#include <framework/errors.h>
#include <framework/link.h>

extern LINK context_root;

#define ctxl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, ctxl)))
#define cldl2ctxp(A)    ((Context *)((char *)(A) - offsetof(Context, cldl)))

typedef void ContextAttachCallBack(int, Context *, void *);

/*
 * A context corresponds to an execution thread, process, address space, etc.
 * A context can belong to a parent context. Contexts hierarchy can be simple
 * plain list or it can form a tree. It is up to target agent developers to choose
 * layout that is most descriptive for a given target.
 */
struct Context {
    char                id[256];            /* context ID */
    char *              name;               /* human readable context name */
    LINK                cldl;               /* link that used to form a list of context children */
    LINK                ctxl;               /* link that used to form a list of all contexts */
    LINK                children;           /* context children double linked list */
    Context *           parent;             /* context parent */
    Context *           creator;            /* context creator */
    Context *           mem;                /* context memory space */
    int                 big_endian;         /* 0 - little endian, 1 -  big endian */
    unsigned int        mem_access;         /* bit set of memory access types represented by this context */
    unsigned int        ref_count;          /* reference count, see context_lock() and context_unlock() */
    int                 stopped;            /* OS kernel has stopped this context */
    int                 stopped_by_bp;      /* stopped by breakpoint */
    int                 stopped_by_exception;/* stopped by runtime exception (like SIGSEGV, etc.) */
    char *              exception_description;/* description of exception if stopped by runtime exception */
    int                 exiting;            /* context is about to exit */
    int                 exited;             /* context exited */
    int                 event_notification; /* set to 1 when calling one of ContextEventListener call-backs for this context */
    int                 pending_intercept;  /* host is waiting for this context to be suspended */
    unsigned long       pending_signals;    /* bit set of signals that were received, but not handled yet */
    unsigned long       sig_dont_stop;      /* bit set of signals that should not be intercepted by the debugger */
    unsigned long       sig_dont_pass;      /* bit set of signals that should not be delivered to the context */
    int                 signal;             /* signal that stopped this context */
};

/*
 * Values of "mem_access".
 * Target system can support multiple different memory access types, like instruction and data access.
 * Different access types can use different logic for address translation and memory mapping, so they can
 * end up accessing different data bits, even if address is the same.
 * Each distinct access type should be represented by separate memory context.
 * A memory context can represent multiple access types if they are equivalent - all access same memory bits.
 * Same data bits can be exposed through multiple memory contexts.
 */
#define MEM_ACCESS_INSTRUCTION  0x0001      /* Context represent instructions fetch access */
#define MEM_ACCESS_DATA         0x0002      /* Context represents data access */
#define MEM_ACCESS_IO           0x0004      /* Context represents IO peripherals */
#define MEM_ACCESS_USER         0x0008      /* Context represents a user (e.g. application running in Linux) view to memory */
#define MEM_ACCESS_SUPERVISOR   0x0010      /* Context represents a supervisor (e.g. Linux kernel) view to memory */
#define MEM_ACCESS_HYPERVISOR   0x0020      /* Context represents a hypervisor view to memory */
#define MEM_ACCESS_VIRTUAL      0x0040      /* Context uses virtual addresses */
#define MEM_ACCESS_PHYSICAL     0x0080      /* Context uses physical addresses */
#define MEM_ACCESS_CACHE        0x0100      /* Context is a cache */
#define MEM_ACCESS_TLB          0x0200      /* Context is a TLB memory */

/*
 * Convert PID to TCF Context ID
 */
extern char * pid2id(pid_t pid, pid_t parent);

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
 * Return -1 and set errno if the context memory cannot be read.
 */
extern int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Write 'size' byte into context register starting at offset 'offs'.
 * Return -1 and set errno if the register cannot be written.
 */
extern int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf);

/*
 * Read 'size' bytes from context register starting at offset 'offs'.
 * Return -1 and set errno if the register cannot be read.
 */
extern int context_read_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf);

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
 * Create a Context object.
 * It is not supposed to be called by clients.
 */
extern Context * create_context(const char * id);

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
