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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

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
#include "symbols.h"

static const char * STACKTRACE = "StackTrace";

struct StackFrame {
    ContextAddress fp;  /* frame address */
    ContextAddress ip;  /* istruction pointer */
    ContextAddress rp;  /* return address */
    ContextAddress fn;  /* address of function */
    int arg_cnt;        /* number of function arguments */
    ContextAddress args;/* address of function arguments */
};

struct StackTrace {
    int error;
    int frame_cnt;
    int frame_max;
    struct StackFrame frames[1];
};

typedef struct StackFrame StackFrame;
typedef struct StackTrace StackTrace;

static Context dump_stack_ctx;

static void add_frame(Context * ctx, StackFrame * frame) {
    StackTrace * stack_trace = (StackTrace *)ctx->stack_trace;
    if (stack_trace->frame_cnt >= stack_trace->frame_max) {
        stack_trace->frame_max *= 2;
        stack_trace = (StackTrace *)loc_realloc(stack_trace,
            sizeof(StackTrace) + (stack_trace->frame_max - 1) * sizeof(StackFrame));
        ctx->stack_trace = stack_trace;
    }
    stack_trace->frames[stack_trace->frame_cnt++] = *frame;
}

#if defined(_WRS_KERNEL)

#include <trcLib.h>

static Context * client_ctx;

static void vxworks_stack_trace_callback(
    void *     callAdrs,       /* address from which function was called */
    int        funcAdrs,       /* address of function called */
    int        nargs,          /* number of arguments in function call */
    int *      args,           /* pointer to function args */
    int        taskId,         /* task's ID */
    int        isKernelAdrs    /* TRUE if Kernel addresses */
)
{
    StackFrame f;
    memset(&f, 0, sizeof(f));
    f.rp = (ContextAddress)callAdrs;
    f.fn = (ContextAddress)funcAdrs;
    f.arg_cnt = nargs;
    f.args = (ContextAddress)args;
    add_frame(client_ctx, &f);
}

static int trace_stack(Context * ctx) {
    int i;
    StackTrace * s;
    client_ctx = ctx;
    trcStack(&ctx->regs, (FUNCPTR)vxworks_stack_trace_callback, ctx->pid);
    /* VxWorks stack trace is in reverse order - from bottom to top */
    s = (StackTrace *)ctx->stack_trace;
    for (i = 0; i < s->frame_cnt / 2; i++) {
        StackFrame f = s->frames[i];
        s->frames[i] = s->frames[s->frame_cnt - i - 1];
        s->frames[s->frame_cnt - i - 1] = f;
    }
    for (i =0; i < s->frame_cnt; i++) {
        StackFrame f = s->frames[i];
        if (i == 0) {
            f.ip = get_regs_PC(ctx->regs);
        }
        else {
            f.ip = s->frames[i - 1].rp;
        }
    }
    return 0;
}

#else

static int read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    if (ctx == &dump_stack_ctx) {
        /* Tracing current thread stack */
        memmove(buf, (void *)address, size);
    }
    else {
        if (context_read_mem(ctx, address, buf, size) < 0) return -1;
        check_breakpoints_on_memory_read(ctx, address, buf, size);
    }
    return 0;
}

#if defined(__i386__) || defined(__x86_64__)

#define MAX_FRAMES  1000

#define JMPD08      0xeb
#define JMPD32      0xe9
#define GRP5        0xff
#define JMPN        0x25
#define PUSH_EBP    0x55
#define MOV_ESP00   0x89
#define MOV_ESP01   0xe5
#define MOV_ESP10   0x8b
#define MOV_ESP11   0xec
#define ENTER       0xc8
#define RET         0xc3
#define RETADD      0xc2
#define REXW        0x48

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
static ContextAddress trace_jump(Context * ctx, ContextAddress addr) {
    int cnt = 0;
    /* while instruction is a JMP, get destination adrs */
    while (cnt < 100) {
        unsigned char instr;    /* instruction opcode at <addr> */
        ContextAddress dest;    /* Jump destination address */
        if (read_mem(ctx, addr, &instr, 1) < 0) break;

        /* If instruction is a JMP, get destination adrs */
        if (instr == JMPD08) {
            signed char disp08;
            if (read_mem(ctx, addr + 1, &disp08, 1) < 0) break;
            dest = addr + 2 + disp08;
        }
        else if (instr == JMPD32) {
            int disp32;
            assert(sizeof(disp32) == 4);
            if (read_mem(ctx, addr + 1, &disp32, 4) < 0) break;
            dest = addr + 5 + disp32;
        }
        else if (instr == GRP5) {
            ContextAddress ptr;
            if (read_mem(ctx, addr + 1, &instr, 1) < 0) break;
            if (instr != JMPN) break;
            if (read_mem(ctx, addr + 2, &ptr, sizeof(ptr)) < 0) break;
            if (read_mem(ctx, ptr, &dest, sizeof(dest)) < 0) break;
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

static int func_entry(unsigned char * code) {
    if (*code != PUSH_EBP) return 0;
    code++;
    if (*code == REXW) code++;
    if (code[0] == MOV_ESP00 && code[1] == MOV_ESP01) return 1;
    if (code[0] == MOV_ESP10 && code[1] == MOV_ESP11) return 1;
    return 0;
}

static int trace_stack(Context * ctx) {
    ContextAddress pc = get_regs_PC(ctx->regs);
    ContextAddress fp = get_regs_BP(ctx->regs);
    ContextAddress fp_prev = 0;

    ContextAddress addr = trace_jump(ctx, pc);
    ContextAddress plt = is_plt_section(ctx, addr);
    unsigned char code[5];
    unsigned cnt = 0;

    /*
     * we don't have a stack frame in a few restricted but useful cases:
     *  1) we are at a PUSH %EBP MOV %ESP %EBP or RET or ENTER instruction,
     *  2) we are the first instruction of a subroutine (this may NOT be
     *     a PUSH %EBP MOV %ESP %EBP instruction with some compilers)
     *  3) we are inside PLT entry
     */

    if (plt) {
        fp_prev = fp;
        if (addr - plt == 0) {
            fp = get_regs_SP(ctx->regs);
        }
        else if (addr - plt < sizeof(ContextAddress) * 4) {
            fp = get_regs_SP(ctx->regs) + sizeof(ContextAddress);
        }
        else if ((addr - plt) % (sizeof(ContextAddress) * 4) < sizeof(ContextAddress) * 2) {
            fp = get_regs_SP(ctx->regs) - sizeof(ContextAddress);
        }
        else {
            fp = get_regs_SP(ctx->regs);
        }
    }
    else {
        if (read_mem(ctx, addr - 1, code, sizeof(code)) < 0) return -1;

        if (func_entry(code + 1) || code[1] == ENTER || code[1] == RET || code[1] == RETADD) {
            fp_prev = fp;
            fp = get_regs_SP(ctx->regs) - sizeof(ContextAddress);
        }
        else if (func_entry(code)) {
            fp_prev = fp;
            fp = get_regs_SP(ctx->regs);
        }
    }

    while (cnt < MAX_FRAMES) {
        ContextAddress frame[2];
        ContextAddress fp_next;
        StackFrame f;
        memset(&f, 0, sizeof(f));
        f.ip = pc;
        if (fp == 0) {
            add_frame(ctx, &f);
            break;
        }
        if (read_mem(ctx, fp, frame, sizeof(frame)) < 0) {
            memset(frame, 0, sizeof(frame));
        }
        f.fp = fp;
        f.rp = frame[1];
        add_frame(ctx, &f);
        cnt++;
        fp_next = fp_prev != 0 ? fp_prev : frame[0];
        fp_prev = 0;
        if (fp_next <= fp) break;
        fp = fp_next;
        pc = f.rp;
    }

    return 0;
}

#else

#error "Unknown CPU"

#endif

#endif

static StackTrace * create_stack_trace(Context * ctx) {
    StackTrace * stack_trace = (StackTrace *)ctx->stack_trace;
    if (stack_trace != NULL) return stack_trace;

    stack_trace = (StackTrace *)loc_alloc_zero(sizeof(StackTrace) + 31 * sizeof(StackFrame));
    stack_trace->frame_max = 32;
    ctx->stack_trace = stack_trace;
    if (ctx->regs_error != 0) {
        stack_trace->error = ctx->regs_error;
    }
    else if (trace_stack(ctx) < 0) {
        stack_trace = (StackTrace *)ctx->stack_trace;
        stack_trace->error = errno;
    }
    else {
        stack_trace = (StackTrace *)ctx->stack_trace;
    }
    return stack_trace;
}

static int id2frame(char * id, Context ** ctx, int * frame) {
    int i;
    char pid[64];

    *ctx = NULL;
    *frame = 0;
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
    *frame = strtol(id, NULL, 10);
    return 0;
}

static int id2frame_index(char * id, Context ** ctx, int * idx) {
    int frame = 0;
    StackTrace * s = NULL;

    if (id2frame(id, ctx, &frame) < 0) return -1;

    if (!(*ctx)->stopped) {
        errno = ERR_IS_RUNNING;
        return -1;
    }
    s = create_stack_trace(*ctx);
    if (s->error != 0) {
        errno = s->error;
        return -1;
    }
    if (frame < 0 || frame >= s->frame_cnt) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }

    *idx = s->frame_cnt - frame - 1;
    return 0;
}

static void write_context(OutputStream * out, char * id, Context * ctx, int level, StackFrame * frame) {
    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');
    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, thread_id(ctx));

#if !defined(_WRS_KERNEL)
    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    if (frame->fp) {
        write_stream(out, ',');
        json_write_string(out, "FP");
        write_stream(out, ':');
        json_write_ulong(out, frame->fp);
    }

    if (frame->rp) {
        write_stream(out, ',');
        json_write_string(out, "RP");
        write_stream(out, ':');
        json_write_ulong(out, frame->rp);
    }

    if (frame->ip) {
        write_stream(out, ',');
        json_write_string(out, "IP");
        write_stream(out, ':');
        json_write_ulong(out, frame->ip);
    }

    if (frame->arg_cnt) {
        write_stream(out, ',');
        json_write_string(out, "ArgsCnt");
        write_stream(out, ':');
        json_write_ulong(out, frame->arg_cnt);
    }

    if (frame->args) {
        write_stream(out, ',');
        json_write_string(out, "ArgsAddr");
        write_stream(out, ':');
        json_write_ulong(out, frame->args);
    }

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char ** ids;
    int id_cnt = 0;
    int i;

    ids = json_read_alloc_string_array(&c->inp, &id_cnt);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_stream(&c->out, '[');
    for (i = 0; i < id_cnt; i++) {
        StackTrace * s = NULL;
        Context * ctx = NULL;
        int idx = 0;
        if (i > 0) write_stream(&c->out, ',');
        if (id2frame_index(ids[i], &ctx, &idx) < 0) {
            err = errno;
        }
        else if (!ctx->intercepted) {
            err = ERR_IS_RUNNING;
        }
        else {
            s = create_stack_trace(ctx);
        }
        if (s == NULL || idx < 0 || idx >= s->frame_cnt) {
            write_string(&c->out, "null");
        }
        else {
            int level = s->frame_cnt - idx - 1;
            write_context(&c->out, ids[i], ctx, level, s->frames + idx);
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
    loc_free(ids);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    int err = 0;
    pid_t pid, parent;
    Context * ctx = NULL;
    StackTrace * s = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent != 0) {
        ctx = context_find_from_pid(pid);
        if (ctx != NULL) {
            if (!ctx->intercepted) {
                err = ERR_IS_RUNNING;
            }
            else {
                s = create_stack_trace(ctx);
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
        write_stream(&c->out, '[');
        for (i = 0; i < s->frame_cnt; i++) {
            if (i > 0) write_stream(&c->out, ',');
            snprintf(frame_id, sizeof(frame_id), "FP%d.%d", ctx->pid, i);
            json_write_string(&c->out, frame_id);
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void delete_stack_trace(Context * ctx, void * client_data) {
    if (ctx->stack_trace != NULL) {
        loc_free(ctx->stack_trace);
        ctx->stack_trace = NULL;
    }
}

void dump_stack_trace(void) {
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
        int i;
        StackTrace * s;
        trace(LOG_ALWAYS, "Stack trace:");
        s = create_stack_trace(&dump_stack_ctx);
        for (i = 0; i < s->frame_cnt; i++) {
            StackFrame * f = s->frames + i;
            trace(LOG_ALWAYS, "  0x%0*lx 0x%0*lx", sizeof(f->ip) * 2, f->ip, sizeof(f->fp) * 2, f->fp);
        }
    }
    CloseHandle(dump_stack_ctx.handle);
#else
    trace(LOG_ALWAYS, "dump_stack_trace: not implemented");
#endif
}

int is_stack_frame_id(char * id, Context ** ctx, int * frame) {
    return id2frame(id, ctx, frame) == 0;
}

char * get_stack_frame_id(Context * ctx, int frame) {
    static char id[256];

    assert(context_has_state(ctx));
    assert(frame != STACK_NO_FRAME);

    if (frame == STACK_TOP_FRAME) {
        StackTrace * s;

        if (!ctx->stopped) {
            errno = ERR_IS_RUNNING;
            return NULL;
        }

        s = create_stack_trace(ctx);
        if (s->error != 0) {
            errno = s->error;
            return NULL;
        }

        frame = s->frame_cnt - 1;
    }
    snprintf(id, sizeof(id), "FP%d.%d", ctx->pid, frame);
    return id;
}

int get_frame_info(Context * ctx, int frame, ContextAddress * ip, ContextAddress * rp, ContextAddress * fp) {
    StackFrame * f;
    StackTrace * s;

    if (ctx == NULL || !context_has_state(ctx)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (!ctx->stopped) {
        errno = ERR_IS_RUNNING;
        return -1;
    }

    if (frame == STACK_TOP_FRAME && !rp && !fp && !ctx->regs_error) {
        /* Optimization: no need to perform stack trace */
        if (ip) *ip = get_regs_PC(ctx->regs);
        return 0;
    }

    s = create_stack_trace(ctx);
    if (s->error != 0) {
        errno = s->error;
        return -1;
    }

    if (frame == STACK_TOP_FRAME) {
        frame = s->frame_cnt - 1;
    }
    else if (frame < 0 || frame >= s->frame_cnt) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }

    f = s->frames + (s->frame_cnt - frame - 1);
    if (ip) *ip = f->ip;
    if (rp) *rp = f->rp;
    if (fp) *fp = f->fp;
    return 0;
}

int is_top_frame(Context * ctx, int frame) {
    StackTrace * s;

    if (ctx == NULL || !context_has_state(ctx)) return 0;
    if (!ctx->stopped) return 0;
    if (frame == STACK_TOP_FRAME) return 1;
    s = create_stack_trace(ctx);
    if (s->error != 0) return 0;
    return frame == s->frame_cnt - 1;
}

void ini_stack_trace_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        NULL,
        delete_stack_trace,
        delete_stack_trace,
        delete_stack_trace,
        delete_stack_trace
    };
    add_context_event_listener(&listener, bcg);
    add_command_handler(proto, STACKTRACE, "getContext", command_get_context);
    add_command_handler(proto, STACKTRACE, "getChildren", command_get_children);
    memset(&dump_stack_ctx, 0, sizeof(dump_stack_ctx));
}

#endif

