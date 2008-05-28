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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

#include "mdep.h"
#include "config.h"

#if SERVICE_StackTrace

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "myalloc.h"
#include "protocol.h"
#include "trace.h"
#include "context.h"
#include "json.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "breakpoints.h"

static const char * STACKTRACE = "StackTrace";

struct StackFrame {
    unsigned long fp;   /* frame address */
    unsigned long pc;   /* return address */
    unsigned long fn;   /* address of function */
    int arg_cnt;        /* number of function arguments */
    unsigned long args; /* address of function arguments */
};

struct StackTrace {
    int error;
    int frame_cnt;
    int top_first;
    struct StackFrame frames[1];
};

typedef struct StackFrame StackFrame;
typedef struct StackTrace StackTrace;

typedef void (*STACK_TRACE_CALLBAK)(
    void *,    /* address from which function was called */
    int   ,    /* address of function called */
    int   ,    /* number of arguments in function call */
    int * ,    /* pointer to function args */
    int   ,    /* thread ID */
    int        /* TRUE if Kernel addresses */
);

static int stack_trace_max = 0;
static StackTrace * stack_trace = NULL;
static Context dump_stack_ctx;

static void stack_trace_callback(
    void *     callAdrs,       /* address from which function was called */
    int        funcAdrs,       /* address of function called */
    int        nargs,          /* number of arguments in function call */
    int *      args,           /* pointer to function args */
    int        taskId,         /* task's ID */
    int        isKernelAdrs    /* TRUE if Kernel addresses */
)
{
    StackFrame * f;
    if (stack_trace == NULL) {
        stack_trace_max = 64;
        stack_trace = (StackTrace *)loc_alloc(sizeof(StackTrace) + (stack_trace_max - 1) * sizeof(StackFrame));
        memset(stack_trace, 0, sizeof(StackTrace));
    }
    else if (stack_trace->frame_cnt >= stack_trace_max) {
        stack_trace_max *= 2;
        stack_trace = (StackTrace *)loc_realloc(stack_trace, sizeof(StackTrace) + (stack_trace_max - 1) * sizeof(StackFrame));
    }
    f = stack_trace->frames + stack_trace->frame_cnt++;
    memset(f, 0, sizeof(StackFrame));
    f->pc = (unsigned long)callAdrs;
    f->fn = (unsigned long)funcAdrs;
    f->arg_cnt = nargs;
    f->args = (unsigned long)args;
}

#if defined(_WRS_KERNEL)

#include <trcLib.h>

static void trace_stack(Context * ctx, STACK_TRACE_CALLBAK callback) {
    trcStack(&ctx->regs, (FUNCPTR)stack_trace_callback, ctx->pid);
}

#else

static int read_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
    if (ctx == &dump_stack_ctx) {
        /* Tracing current thread stack */
        memmove(buf, (void *)address, size);
        return 0;
    }
    else {
        int err = 0;
        if (context_read_mem(ctx, address, buf, size) < 0) err = errno;
        check_breakpoints_on_memory_read(ctx, address, buf, size);
        return err;
    }
}

#define MAX_FRAMES  1000

#define JMPD08      0xeb
#define JMPD32      0xe9
#define PUSH_EBP    0x55
#define MOV_ESP0    0x89
#define MOV_ESP1    0xe5
#define ENTER       0xc8
#define RET         0xc3
#define RETADD      0xc2

/*
 * trace_jump - resolve any JMP instructions to final destination
 *
 * This routine returns a pointer to the next non-JMP instruction to be
 * executed if the pc were at the specified <adrs>.  That is, if the instruction
 * at <adrs> is not a JMP, then <adrs> is returned.  Otherwise, if the
 * instruction at <adrs> is a JMP, then the destination of the JMP is
 * computed, which then becomes the new <adrs> which is tested as before.
 * Thus we will eventually return the address of the first non-JMP instruction
 * to be executed.
 *
 * The need for this arises because compilers may put JMPs to instructions
 * that we are interested in, instead of the instruction itself.  For example,
 * optimizers may replace a stack pop with a JMP to a stack pop.  Or in very
 * UNoptimized code, the first instruction of a subroutine may be a JMP to
 * a PUSH %EBP MOV %ESP %EBP, instead of a PUSH %EBP MOV %ESP %EBP (compiler
 * may omit routine "post-amble" at end of parsing the routine!).  We call
 * this routine anytime we are looking for a specific kind of instruction,
 * to help handle such cases.
 *
 * RETURNS: The address that a chain of branches points to.
 */
static unsigned long trace_jump(Context * ctx, unsigned long addr) {
    int cnt = 0;
    /* while instruction is a JMP, get destination adrs */
    while (cnt < 100) {
        unsigned char instr;            /* instruction opcode at <addr> */
        unsigned long dest;     /* Jump destination address */
        if (read_mem(ctx, addr, &instr, 1) < 0) return addr;

        /* If instruction is a JMP, get destination adrs */
        if (instr == JMPD08) {
            signed char disp08;
            if (read_mem(ctx, addr + 1, &disp08, 1) < 0) return addr;
            dest = addr + 2 + disp08;
        }
        else if (instr == JMPD32) {
            int disp32;
            assert(sizeof(disp32) == 4);
            if (read_mem(ctx, addr + 1, &disp32, 4) < 0) return addr;
            dest = addr + 5 + disp32;
        }
        else {
            break;
        }
        if (dest == addr) break;
        addr = dest;
        cnt++;
    }
    return addr;
}

static int trace_stack(Context * ctx, STACK_TRACE_CALLBAK callback) {
    unsigned long pc = get_regs_PC(ctx->regs);
    unsigned long fp = get_regs_BP(ctx->regs);
    unsigned long fp_prev = 0;

    unsigned long addr = trace_jump(ctx, pc);
    unsigned char code[4];
    unsigned cnt = 0;

    /*
     * we don't have a stack frame in a few restricted but useful cases:
     *  1) we are at a PUSH %EBP MOV %ESP %EBP or RET or ENTER instruction,
     *  2) we are the first instruction of a subroutine (this may NOT be
     *     a PUSH %EBP MOV %ESP %EBP instruction with some compilers)
     */
    if (read_mem(ctx, addr - 1, code, sizeof(code)) < 0) return -1;

    if (code[1] == PUSH_EBP && code[2] == MOV_ESP0 && code[3] == MOV_ESP1 ||
        code[1] == ENTER || code[1] == RET || code[1] == RETADD) {
        fp_prev = fp;
        fp = get_regs_SP(ctx->regs) - 4;
    }
    else if (code[0] == PUSH_EBP && code[1] == MOV_ESP0 && code[2] == MOV_ESP1) {
        fp_prev = fp;
        fp = get_regs_SP(ctx->regs);
    }

    assert(stack_trace == NULL || stack_trace->frame_cnt == 0);
    while (fp != 0 && cnt < MAX_FRAMES) {
        unsigned long frame[2];
        unsigned long fp_next;
        if (read_mem(ctx, fp, frame, sizeof(frame)) < 0) return -1;
        callback((void *)frame[1], 0, 0, 0, ctx->pid, 0);
        if (stack_trace != NULL) {
            stack_trace->frames[stack_trace->frame_cnt - 1].fp = fp;
            stack_trace->top_first = 1;
        }
        cnt++;
        fp_next = fp_prev != 0 ? fp_prev : frame[0];
        fp_prev = 0;
        if (fp_next <= fp) break;
        fp = fp_next;
    }

    return 0;
}

#endif

static void create_stack_trace(Context * ctx) {
    stack_trace = NULL;
    stack_trace_max = 0;
    if (ctx->regs_error != 0) {
        stack_trace = (StackTrace *)loc_alloc_zero(sizeof(StackTrace));
        stack_trace->error = ctx->regs_error;
    }
    else {
        trace_stack(ctx, stack_trace_callback);
    }
    ctx->stack_trace = stack_trace;
    stack_trace = NULL;
}

static int id2frame(char * id, Context ** ctx, int * idx) {
    int i;
    char pid[64];
    int frame;
    StackTrace * s = NULL;

    *ctx = NULL;
    *idx = 0;
    if (*id++ != 'F') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (*id++ != 'P') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    i = 0;
    while (*id != '.') {
        if (*id == 0) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        pid[i++] = *id++;
    }
    pid[i++] = 0;
    id++;
    *ctx = context_find_from_pid(strtol(pid, NULL, 10));
    if (*ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    s = (*ctx)->stack_trace;
    if (s == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    frame = strtol(id, NULL, 10);
    *idx = s->top_first ? s->frame_cnt - frame - 1 : frame;
    return 0;
}

static void write_context(OutputStream * out, char * id, Context * ctx, int level, StackFrame * frame) {
    out->write(out, '{');

    json_write_string(out, "ID");
    out->write(out, ':');
    json_write_string(out, id);

    out->write(out, ',');
    json_write_string(out, "ParentID");
    out->write(out, ':');
    json_write_string(out, thread_id(ctx));

#if !defined(_WRS_KERNEL)
    out->write(out, ',');
    json_write_string(out, "ProcessID");
    out->write(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    if (frame->fp) {
        out->write(out, ',');
        json_write_string(out, "FP");
        out->write(out, ':');
        json_write_ulong(out, frame->fp);
    }

    if (frame->pc) {
        out->write(out, ',');
        json_write_string(out, "RP");
        out->write(out, ':');
        json_write_ulong(out, frame->pc);
    }

    if (frame->arg_cnt) {
        out->write(out, ',');
        json_write_string(out, "ArgsCnt");
        out->write(out, ':');
        json_write_ulong(out, frame->arg_cnt);
    }

    if (frame->args) {
        out->write(out, ',');
        json_write_string(out, "ArgsAddr");
        out->write(out, ':');
        json_write_ulong(out, frame->args);
    }

    out->write(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char ** ids;
    int id_cnt = 0;
    int i;

    ids = json_read_alloc_string_array(&c->inp, &id_cnt);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    c->out.write(&c->out, '[');
    for (i = 0; i < id_cnt; i++) {
        StackTrace * s = NULL;
        Context * ctx = NULL;
        int idx = 0;
        if (i > 0) c->out.write(&c->out, ',');
        if (id2frame(ids[i], &ctx, &idx) < 0) {
            err = errno;
        }
        else if (!ctx->intercepted) {
            err = ERR_IS_RUNNING;
        }
        else {
            if (ctx->stack_trace == NULL) create_stack_trace(ctx);
            s = (StackTrace *)ctx->stack_trace;
        }
        if (s == NULL || idx < 0 || idx >= s->frame_cnt) {
            write_string(&c->out, "null");
        }
        else {
            int level = s->top_first ? s->frame_cnt - idx - 1 : idx;
            write_context(&c->out, ids[i], ctx, level, s->frames + idx);
        }
    }
    c->out.write(&c->out, ']');
    c->out.write(&c->out, 0);
    write_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
    loc_free(ids);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    int err = 0;
    pid_t pid, parent;
    Context * ctx = NULL;
    StackTrace * s = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent != 0) {
        ctx = context_find_from_pid(pid);
        if (ctx != NULL) {
            if (!ctx->intercepted) {
                err = ERR_IS_RUNNING;
            }
            else {
                if (ctx->stack_trace == NULL) create_stack_trace(ctx);
                s = (StackTrace *)ctx->stack_trace;
            }
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, s != NULL ? s->error : err);

    if (s == NULL) {
        write_stringz(&c->out, "null");
    }
    else {
        int i;
        char frame_id[64];
        c->out.write(&c->out, '[');
        for (i = 0; i < s->frame_cnt; i++) {
            if (i > 0) c->out.write(&c->out, ',');
            snprintf(frame_id, sizeof(frame_id), "FP%d.%d", ctx->pid, i);
            json_write_string(&c->out, frame_id);
        }
        c->out.write(&c->out, ']');
        c->out.write(&c->out, 0);
    }

    c->out.write(&c->out, MARKER_EOM);
}

static void dump_stack_callback(
    void *     callAdrs,       /* address from which function was called */
    int        funcAdrs,       /* address of function called */
    int        nargs,          /* number of arguments in function call */
    int *      args,           /* pointer to function args */
    int        taskId,         /* task's ID */
    int        isKernelAdrs    /* TRUE if Kernel addresses */
    )
{
    trace(LOG_ALWAYS, "  0x%08x", callAdrs);
}

void dump_stack_trace(void) {
    stack_trace = NULL;
    stack_trace_max = 0;
#ifdef WIN32
    dump_stack_ctx.handle = OpenThread(THREAD_GET_CONTEXT, FALSE, GetCurrentThreadId());
    memset(&dump_stack_ctx.regs, 0, sizeof(dump_stack_ctx.regs));
    dump_stack_ctx.regs.ContextFlags = CONTEXT_CONTROL | CONTEXT_INTEGER;
    if (GetThreadContext(dump_stack_ctx.handle, &dump_stack_ctx.regs) == 0) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "dump_stack_trace: Can't read thread registers: %d: %s",
            errno, errno_to_str(errno));
    }
    else {
        trace(LOG_ALWAYS, "Stack trace dumped:");
        trace_stack(&dump_stack_ctx, dump_stack_callback);
    }
    CloseHandle(dump_stack_ctx.handle);
#else
    trace(LOG_ALWAYS, "dump_stack_trace: not implemented");
#endif
}

static void delete_stack_trace(Context * ctx, void * client_data) {
    if (ctx->stack_trace != NULL) {
        loc_free(ctx->stack_trace);
        ctx->stack_trace = NULL;
    }
}

void ini_stack_trace_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        NULL,
        delete_stack_trace,
        delete_stack_trace,
        delete_stack_trace,
        delete_stack_trace,
        NULL
    };
    add_context_event_listener(&listener, bcg);
    add_command_handler(proto, STACKTRACE, "getContext", command_get_context);
    add_command_handler(proto, STACKTRACE, "getChildren", command_get_children);
    memset(&dump_stack_ctx, 0, sizeof(dump_stack_ctx));
}

#endif

