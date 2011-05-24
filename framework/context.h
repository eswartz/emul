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

typedef struct ContextBreakpoint ContextBreakpoint;

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
    int                 stopped_by_bp;      /* stopped by breakpoint instruction */
    ContextBreakpoint** stopped_by_cb;      /* stopped by ContextBreakpoint - NULL terminated list of triggered ContextBreakpoint's */
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
 * Debug context suspend reason.
 */
extern const char * REASON_USER_REQUEST;
extern const char * REASON_STEP;
extern const char * REASON_BREAKPOINT;
extern const char * REASON_EXCEPTION;
extern const char * REASON_CONTAINER;
extern const char * REASON_WATCHPOINT;
extern const char * REASON_SIGNAL;
extern const char * REASON_SHAREDLIB;
extern const char * REASON_ERROR;

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

typedef struct MemoryMap MemoryMap;
typedef struct MemoryRegion MemoryRegion;
typedef struct MemoryRegionAttribute MemoryRegionAttribute;

struct MemoryMap {
    unsigned region_cnt;
    unsigned region_max;
    MemoryRegion * regions;
};

struct MemoryRegion {
    ContextAddress addr;            /* Region address in context memory */
    ContextAddress size;            /* Region size */
    uint64_t file_offs;             /* File offset of the region */
    int bss;                        /* 1 if the region is BSS segment */
    dev_t dev;                      /* Region file device ID */
    ino_t ino;                      /* Region file inode */
    char * file_name;               /* Region file name */
    char * sect_name;               /* Region file section name, can be NULL */
    unsigned flags;                 /* Region flags, see MM_FLAG* */
    char * id;                      /* Region ID, not NULL only if the region info is submitted by a client */
    MemoryRegionAttribute * attrs;  /* Additional memory region attributes */
};

struct MemoryRegionAttribute {
    MemoryRegionAttribute * next;
    char * name;
    char * value;
};

#define MM_FLAG_R   1
#define MM_FLAG_W   2
#define MM_FLAG_X   4

/* Context resume modes */
#define RM_RESUME                   0 /* Resume normal execution of the context */
#define RM_STEP_OVER                1 /* Step over a single instruction */
#define RM_STEP_INTO                2 /* Step a single instruction */
#define RM_STEP_OVER_LINE           3 /* Step over a single source code line */
#define RM_STEP_INTO_LINE           4 /* Step a single source code line */
#define RM_STEP_OUT                 5 /* Run until control returns from current function */
#define RM_REVERSE_RESUME           6 /* Start running backwards */
#define RM_REVERSE_STEP_OVER        7 /* Reverse of RM_STEP_OVER - run backwards over a single instruction */
#define RM_REVERSE_STEP_INTO        8 /* Reverse of RM_STEP_INTO: "un-execute" the previous instruction */
#define RM_REVERSE_STEP_OVER_LINE   9 /* Reverse of RM_STEP_OVER_LINE */
#define RM_REVERSE_STEP_INTO_LINE  10 /* Reverse of RM_STEP_INTO_LINE */
#define RM_REVERSE_STEP_OUT        11 /* Reverse of RM_STEP_OUT */
#define RM_STEP_OVER_RANGE         12 /* Step over instructions until PC is outside the specified range */
#define RM_STEP_INTO_RANGE         13 /* Step instruction until PC is outside the specified range for any reason */
#define RM_REVERSE_STEP_OVER_RANGE 14 /* Reverse of RM_STEP_OVER_RANGE */
#define RM_REVERSE_STEP_INTO_RANGE 15 /* Reverse of RM_STEP_INTO_RANGE */
#define RM_UNTIL_ACTIVE            16 /* Run until the context becomes active - scheduled to run on a target CPU */
#define RM_REVERSE_UNTIL_ACTIVE    17 /* Run reverse until the context becomes active */
/* RM_TERMINATE must be last */
#define RM_TERMINATE               18 /* Terminate the context */

/* Mode flags for context_attach() */
#define CONTEXT_ATTACH_SELF      0x01 /* The process is forked child - it will attach itself */
#define CONTEXT_ATTACH_CHILDREN  0x02 /* Enabel auto-attaching of children of the process*/

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
 * 'mode' - attach mode flags, see CONTEXT_ATTACH_*.
 */
extern int context_attach(pid_t pid, ContextAttachCallBack * done, void * client_data, int mode);

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
 * Get context state properties.
 * Return -1 and set errno if cannot access the properties.
 */
extern int context_get_state_properties(Context * ctx, const char *** names, const char *** values, int * cnt);

/*
 * Stop execution of the context.
 * Execution can be resumed by calling context_resume()
 * Return -1 and set errno if the context cannot be stopped.
 */
extern int context_stop(Context * ctx);

/*
 * Resume execution of the context using give execution mode.
 * See RM_* for mode definitions.
 * Return -1 and set errno if the context cannot be resumed.
 */
extern int context_resume(Context * ctx, int mode, ContextAddress range_start, ContextAddress range_end);

/*
 * Check if given resume mode is supported.
 * See RM_* for mode definitions.
 * Return 0 if not supported, 1 if supported.
 */
extern int context_can_resume(Context * ctx, int mode);

/*
 * Resume normal execution of the context.
 * Return -1 and set errno if the context cannot be resumed.
 * Deprecated: use context_resume(ctx, RM_RESUME, 0, 0).
 */
extern int context_continue(Context * ctx);

/*
 * Perform single instruction step on the context.
 * Return -1 and set errno if the context cannot be single stepped.
 */
extern int context_single_step(Context * ctx);

/*
 * Retrieve context memory map.
 * Return -1 and set errno if the map cannot be retrieved.
 * Note: the caller owns MemoryMap object and all its contents, it can call loc_free() on it at any time.
 * When context_get_memory_map() is called, 'map' is empty: region_cnt == 0,
 * but 'map->regions' can be pre-allocated: 'map->regions' can be not NULL and 'map->region_max' > 0.
 * The function adds memory region descriptions into it, doing loc_strdup() for things like file names.
 * The function implementation should not retain references to 'map' or its contents.
 */
extern int context_get_memory_map(Context * ctx, MemoryMap * map);

/*
 * Clear a memory map - dispose all entries.
 */
extern void context_clear_memory_map(MemoryMap * map);

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
 * Map an address in given address space to a unique canonical memory location.
 * A target system can have same block (page) of memory mapped to different address spaces at different addresses.
 * This function should return memory space and address that uniquely identify memory block.
 * 'block_addr' and 'block_size' are optional arguments that clients can use to retrieve
 * the memory block start address and size. Clients can use this information to map a range of addresses
 * with a single function call.
 * Return -1 and set errno if canonical address cannot be resolved.
 */
extern int context_get_canonical_addr(Context * ctx, ContextAddress addr,
        Context ** canonical_ctx, ContextAddress * canonical_addr,
        ContextAddress * block_addr, ContextAddress * block_size);

/*
 * Get a context that represents a group of related contexts.
 * Implementation can choose a member of a group to represent the group,
 * or it can create a separate context object for that.
 * Clients can use this function to check if two or more contexts belong to same group,
 * for example:
 *     if (context_get_group(ctx1, group) == context_get_group(ctx2, group) ...
 *
 * See CONTEXT_GROUP_* for possible values of 'group' argument.
 */
extern Context * context_get_group(Context * ctx, int group);

/*
 * "stop" context group - all contexts that need to be stopped to make sure
 * memory contents in a particular address space is stable.
 */
#define CONTEXT_GROUP_STOP          1

/*
 * "breakpoint" context group - all contexts for which evaluation of breakpoint
 * location should produce same list of addresses.
 * context_get_group(ctx, CONTEXT_GROUP_BREAKPOINT) == NULL means the context
 * does not support breakpoints.
 */
#define CONTEXT_GROUP_BREAKPOINT    2

/*
 * "intercept" context group - all contexts which should be intercepted
 * when any member of the group is intercepted for any reason.
 */
#define CONTEXT_GROUP_INTERCEPT     3

/*
 * "process" context group - all contexts that share same memory address space,
 * memory map and symbol files.
 */
#define CONTEXT_GROUP_PROCESS       4

/*
 * "CPU" context group - all contexts that belong to same CPU.
 * On SMP systems, all CPUs (cores) of same type should be members of same group.
 */
#define CONTEXT_GROUP_CPU           5

/*
 * Debug context implementation can support low-level breakpoints.
 * The support is optional, if not implemented, the agent will use generic code
 * to plant software breakpoints. Hardware breakpoints is an example of breakpoints
 * that can be implemented by debug context.
 *
 * ContextBreakpoint struct is used by the agent to communicate breakpoint properties to
 * debug context implementation. The implementation needs to handle only small subset of
 * breakpoint properties, the rest is handles by generic code in Breakpoints service.
 */
struct ContextBreakpoint {
    Context * ctx;              /* breakpoint context, one of returned by
                                 * context_get_group(..., CONTEXT_GROUP_BREAKPOINT) */
    ContextAddress address;     /* breakpoint address */
    ContextAddress length;      /* length of the breakpoint address range */
    unsigned access_types;      /* memory access type, bit set of CTX_BP_ACCESS_* */
    unsigned id;                /* to be used by debug context implementation */
};

#define CTX_BP_ACCESS_DATA_READ      0x01
#define CTX_BP_ACCESS_DATA_WRITE     0x02
#define CTX_BP_ACCESS_INSTRUCTION    0x04

/*
 * Return bitmask of supported CTX_BP_ACCESS_* values.
 */
extern int context_get_supported_bp_access_types(Context * ctx);

/*
 * Plant a breakpoint.
 * Return 0 on success, or return -1 and set errno on error.
 * If error code is ERR_UNSUPPORTED, Breakpoints service will
 * try to plant the breakpoint as a generic software breakpoint.
 */
extern int context_plant_breakpoint(ContextBreakpoint * bp);

/*
 * Un-plant (remove) a breakpoint.
 * Return 0 on success, or return -1 and set errno on error.
 * 'bp' must be a breakpoint that was planted by context_plant_breakpoint().
 */
extern int context_unplant_breakpoint(ContextBreakpoint * bp);

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
