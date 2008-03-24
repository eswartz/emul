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
 * This module implements Breakpoints service.
 * The service maintains a list of breakpoints.
 * Each breakpoint consists of one or more conditions that determine
 * when a program's execution should be interrupted.
 */

#include "mdep.h"
#include "config.h"
#if SERVICE_Breakpoints

// TODO: breakpoint status reports
// TODO: replant breakpoints when shared lib is loaded or unloaded

#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <assert.h>
#include "breakpoints.h"
#include "expressions.h"
#include "channel.h"
#include "protocol.h"
#include "errors.h"
#include "trace.h"
#include "runctrl.h"
#include "context.h"
#include "myalloc.h"
#include "exceptions.h"
#include "symbols.h"
#include "json.h"
#include "link.h"

#ifdef BREAK_INST
#  undef BREAK_INST
#endif

#if defined(_WRS_KERNEL)

#include <private/vxdbgLibP.h>

#elif defined(__i386__)

static unsigned char BREAK_INST[] = { 0xcc };   /* breakpoint instruction */
#define BREAK_SIZE 1    /* breakpoint instruction size */

#else

#error "Unknown CPU"

#endif

typedef struct BreakpointRef BreakpointRef;
typedef struct BreakpointInfo BreakpointInfo;
typedef struct BreakInstruction BreakInstruction;

struct BreakpointRef {
    LINK link_inp;
    LINK link_bp;
    InputStream * inp;
    BreakpointInfo * bp;
};

struct BreakpointInfo {
    LINK link_all;
    LINK link_id;
    LINK refs;
    char id[64];
    int enabled;
    int unsupported;
    int planted;
    int deleted;
    int error;
    char * err_msg;
    char * address;
    char * condition;
};

struct BreakInstruction {
    LINK link_all;
    LINK link_adr;
    Context * ctx;
    int ctx_cnt;
    unsigned long address;
#if defined(_WRS_KERNEL)
    VXDBG_CTX vxdbg_ctx;
    VXDBG_BP_ID vxdbg_id;
#else
    char saved_code[BREAK_SIZE];
#endif    
    int error;
    int skip;
    BreakpointInfo ** refs;
    int ref_size;
    int ref_cnt;
    int planted;
};

static const char * BREAKPOINTS = "Breakpoints";

#define ADDR2INSTR_HASH_SIZE 1023
#define addr2instr_hash(addr) (((addr) + ((addr) >> 8)) % ADDR2INSTR_HASH_SIZE)

#define link_all2bi(A)  ((BreakInstruction *)((char *)(A) - (int)&((BreakInstruction *)0)->link_all))
#define link_adr2bi(A)  ((BreakInstruction *)((char *)(A) - (int)&((BreakInstruction *)0)->link_adr))

#define ID2BP_HASH_SIZE 1023

#define link_all2bp(A)  ((BreakpointInfo *)((char *)(A) - (int)&((BreakpointInfo *)0)->link_all))
#define link_id2bp(A)   ((BreakpointInfo *)((char *)(A) - (int)&((BreakpointInfo *)0)->link_id))

#define INP2BR_HASH_SIZE 127

#define link_inp2br(A)  ((BreakpointRef *)((char *)(A) - (int)&((BreakpointRef *)0)->link_inp))
#define link_bp2br(A)   ((BreakpointRef *)((char *)(A) - (int)&((BreakpointRef *)0)->link_bp))

static LINK breakpoints;
static LINK id2bp[ID2BP_HASH_SIZE];

static LINK instructions;
static LINK addr2instr[ADDR2INSTR_HASH_SIZE];

static LINK inp2br[INP2BR_HASH_SIZE];

static int replanting = 0;

static Context * expression_context = NULL;
static int address_expression_identifier(char * name, Value * v);
static int condition_expression_identifier(char * name, Value * v);
static ExpressionContext bp_address_ctx = { address_expression_identifier, NULL };
static ExpressionContext bp_condition_ctx = { condition_expression_identifier, NULL };

static int id2bp_hash(char * id) {
    unsigned hash = 0;
    while (*id) hash = (hash >> 16) + hash + (unsigned char)*id++;
    return hash % ID2BP_HASH_SIZE;
}

static void plant_instruction(BreakInstruction * bi) {
    assert(!bi->planted);
    bi->error = 0;
#if defined(_WRS_KERNEL)
    bi->vxdbg_ctx.ctxId = bi->ctx_cnt == 1 ? bi->ctx->pid : 0;
    bi->vxdbg_ctx.ctxId = 0;
    bi->vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    if (vxdbgBpAdd(vxdbg_clnt_id,
            &bi->vxdbg_ctx, 0, BP_ACTION_STOP | BP_ACTION_NOTIFY,
            0, 0, (INSTR *)bi->address, 0, 0, &bi->vxdbg_id) != OK) {
        bi->error = errno;
        assert(bi->error != 0);
    }
#else
    if (context_read_mem(bi->ctx, bi->address, bi->saved_code, BREAK_SIZE) < 0) {
        bi->error = errno;
    }
    else if (context_write_mem(bi->ctx, bi->address, &BREAK_INST, BREAK_SIZE) < 0) {
        bi->error = errno;
    }
#endif
    bi->planted = bi->error == 0;
}

static int verify_instruction(BreakInstruction * bi) {
    assert(bi->planted);
#if defined(_WRS_KERNEL)
    return bi->vxdbg_ctx.ctxId == (bi->ctx_cnt == 1 ? bi->ctx->pid : 0) &&
           bi->vxdbg_ctx.ctxType == VXDBG_CTX_TASK;
#else
    return 1;
#endif
}

static void remove_instruction(BreakInstruction * bi) {
    assert(bi->planted);
    assert(!bi->error);
#if defined(_WRS_KERNEL)
    {
        VXDBG_BP_DEL_INFO info;
        memset(&info, 0, sizeof(info));
        info.pClnt = vxdbg_clnt_id;
        info.type = BP_BY_ID_DELETE;
        info.info.id.bpId = bi->vxdbg_id;
        if (vxdbgBpDelete(info) != OK) {
            bi->error = errno;
            assert(bi->error != 0);
        }
    }
#else
    if (!bi->ctx->exited && bi->ctx->stopped) {
        if (context_write_mem(bi->ctx, bi->address, bi->saved_code, BREAK_SIZE) < 0) {
            bi->error = errno;
        }
    }
#endif
    bi->planted = 0;
}

static BreakInstruction * add_instruction(Context * ctx, unsigned long address) {
    int hash = addr2instr_hash(address);
    BreakInstruction * bi = (BreakInstruction *)loc_alloc_zero(sizeof(BreakInstruction));
    list_add_last(&bi->link_all, &instructions);
    list_add_last(&bi->link_adr, addr2instr + hash);
    context_lock(ctx);
    bi->ctx = ctx;
    bi->address = address;
    return bi;
}

static void clear_instruction_refs(void) {
    LINK * l = instructions.next;
    while (l != &instructions) {
        BreakInstruction * bi = link_all2bi(l);
        bi->ctx_cnt = 1;
        bi->ref_cnt = 0;
        l = l->next;
    }
}

static void delete_unused_instructions(void) {
    LINK * l = instructions.next;
    while (l != &instructions) {
        BreakInstruction * bi = link_all2bi(l);
        l = l->next;
        if (bi->skip) continue;
        if (bi->ref_cnt == 0) {
            list_remove(&bi->link_all);
            list_remove(&bi->link_adr);
            if (bi->planted) {
                if (bi->ctx->exited || !bi->ctx->stopped) {
                    LINK * qp = context_root.next;
                    while (qp != &context_root) {
                        Context * ctx = ctxl2ctxp(qp);
                        qp = qp->next;
                        if (ctx->mem == bi->ctx->mem && !ctx->exited && ctx->stopped) {
                            assert(bi->ctx != ctx);
                            context_unlock(bi->ctx);
                            context_lock(ctx);
                            bi->ctx = ctx;
                            break;
                        }
                    }
                }
                remove_instruction(bi);
            }
            context_unlock(bi->ctx);
            loc_free(bi->refs);
            loc_free(bi);
        }
        else if (!bi->planted) {
            plant_instruction(bi);
        }
        else if (!verify_instruction(bi)) {
            remove_instruction(bi);
            plant_instruction(bi);
        }
    }
}

static BreakInstruction * find_instruction(Context * ctx, unsigned long address) {
    int hash = addr2instr_hash(address);
    LINK * l = addr2instr[hash].next;
    assert(!ctx->exited && (ctx->stopped || !context_has_state(ctx)));
    while (l != addr2instr + hash) {
        BreakInstruction * bi = link_adr2bi(l);
        l = l->next;
        if (bi->ctx->mem == ctx->mem && bi->address == address) {
            if (bi->ctx->exited || !bi->ctx->stopped) {
                assert(bi->ctx != ctx);
                context_unlock(bi->ctx);
                context_lock(ctx);
                bi->ctx = ctx;
            }
            return bi;
        }
    }
    return NULL;
}

static int address_expression_identifier(char * name, Value * v) {
    if (v == NULL) return 0;
    memset(v, 0, sizeof(Value));
    if (expression_context == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (strcmp(name, "$thread") == 0) {
        if (context_has_state(expression_context)) {
            string_value(v, thread_id(expression_context));
        }
        else {
            string_value(v, container_id(expression_context));
        }
        return 0;
    }
#if SERVICE_Symbols
    {
        Symbol sym;
        if (find_symbol(expression_context, name, &sym) < 0) {
            if (errno != ERR_SYM_NOT_FOUND) return -1;
        }
        else {
            v->type = VALUE_UNS;
            v->value = sym.value;
            return 0;
        }
    }
#endif
    errno = ERR_SYM_NOT_FOUND;
    return -1;
}

static void address_expression_error(BreakpointInfo * bp, char * msg) {
    // TODO: per-context address expression error report
    int size;
    if (bp->error) return;
    bp->error = errno;
    if (msg == NULL) msg = get_expression_error_msg();
    assert(bp->err_msg == NULL);
    size = strlen(msg) + strlen(bp->address) + 64;
    bp->err_msg = loc_alloc(size);
    snprintf(bp->err_msg, size, "Invalid breakpoint address '%s': %s", bp->address, msg);
}

static void plant_breakpoint(BreakpointInfo * bp) {
    LINK * qp;
    char * p = NULL;
    Value v;
    int context_sensitive = 0;

    assert(!bp->planted);
    assert(bp->enabled);
    bp->error = 0;
    if (bp->err_msg != NULL) loc_free(bp->err_msg);

    if (bp->address == NULL) {
        bp->error = ERR_INV_EXPRESSION;
        trace(LOG_ALWAYS, "No breakpoint address");
        return;
    }
    expression_context = NULL;
    if (evaluate_expression(&bp_address_ctx, bp->address, &v) < 0) {
        if (errno != ERR_INV_CONTEXT) {
            address_expression_error(bp, NULL);
            trace(LOG_ALWAYS, "Error: %s", bp->err_msg);
            return;
        }
        context_sensitive = 1;
    }
    if (!context_sensitive && v.type != VALUE_INT && v.type != VALUE_UNS) {
        errno = ERR_INV_EXPRESSION;
        address_expression_error(bp, "Must be integer number");
        trace(LOG_ALWAYS, "Error: %s", bp->err_msg);
        return;
    }

    for (qp = context_root.next; qp != &context_root; qp = qp->next) {
        BreakInstruction * bi = NULL;
        Context * ctx = ctxl2ctxp(qp);

        if (ctx->exited || ctx->exiting) continue;
        assert(ctx->stopped || !context_has_state(ctx));
        if (context_sensitive) {
            expression_context = ctx;
            if (evaluate_expression(&bp_address_ctx, bp->address, &v) < 0) {
                address_expression_error(bp, NULL);
                trace(LOG_ALWAYS, "Error: %s", bp->err_msg);
                continue;
            }
            if (v.type != VALUE_INT && v.type != VALUE_UNS) {
                errno = ERR_INV_EXPRESSION;
                address_expression_error(bp, "Must be integer number");
                continue;
            }
        }
        if (bp->condition != NULL) {
            /* Optimize away the breakpoint if condition is always false for given context */
            Value c;
            expression_context = ctx;
            if (evaluate_expression(&bp_address_ctx, bp->condition, &c) == 0) {
                switch (c.type) {
                case VALUE_INT:
                case VALUE_UNS:
                    if (c.value == 0) continue;
                    break;
                case VALUE_STR:
                    if (c.str == NULL) continue;
                    break;
                }
            }
        }
        bi = find_instruction(ctx, v.value);
        if (bi == NULL) {
            bi = add_instruction(ctx, v.value);
        }
        else if (bp->planted) {
            int i = 0;
            while (i < bi->ref_cnt && bi->refs[i] != bp) i++;
            if (i < bi->ref_cnt) continue;
        }
        if (bi->ref_cnt >= bi->ref_size) {
            bi->ref_size = bi->ref_size == 0 ? 8 : bi->ref_size * 2;
            bi->refs = (BreakpointInfo **)loc_realloc(bi->refs, sizeof(BreakpointInfo *) * bi->ref_size);
        }
        bi->refs[bi->ref_cnt++] = bp;
        if (bi->ctx != ctx) bi->ctx_cnt++;
        if (bi->error) {
            if (!bp->error) bp->error = bi->error;
        }
        else {
            bp->planted = 1;
        }
    }
    if (bp->planted) bp->error = 0;
}

static void event_replant_breakpoints(void *arg) {
    LINK * l = breakpoints.next;
    replanting = 0;
    clear_instruction_refs();
    while (l != &breakpoints) {
        BreakpointInfo * bp = link_all2bp(l);
        l = l->next;
        if (bp->deleted) {
            list_remove(&bp->link_all);
            list_remove(&bp->link_id);
            loc_free(bp->err_msg);
            loc_free(bp->address);
            loc_free(bp->condition);
            loc_free(bp);
            continue;
        }
        bp->planted = 0;
        if (bp->enabled && !bp->unsupported) {
            plant_breakpoint(bp);
        }
    }
    delete_unused_instructions();
}

static void replant_breakpoints(void) {
    if (list_is_empty(&breakpoints) && list_is_empty(&instructions)) return;
    if (replanting) return;
    replanting = 1;
    post_safe_event(event_replant_breakpoints, NULL);
}

static int str_equ(char * x, char * y) {
    if (x == y) return 1;
    if (x == NULL) return 0;
    if (y == NULL) return 0;
    return strcmp(x, y) == 0;
}

static int copy_breakpoint_info(BreakpointInfo * dst, BreakpointInfo * src) {
    int res = 0;
    if (strcmp(dst->id, src->id) != 0) {
        strcpy(dst->id, src->id);
        res = 1;
    }
    if (dst->enabled != src->enabled) {
        dst->enabled = src->enabled;
        res = 1;
    }
    if (dst->unsupported != src->unsupported) {
        dst->unsupported = src->unsupported;
        res = 1;
    }
    if (!str_equ(dst->address, src->address)) {
        loc_free(dst->address);
        dst->address = src->address;
        res = 1;
    }
    else {
        loc_free(src->address);
    }
    src->address = NULL;
    if (!str_equ(dst->condition, src->condition)) {
        loc_free(dst->condition);
        dst->condition = src->condition;
        res = 1;
    }
    else {
        loc_free(src->condition);
    }
    src->condition = NULL;
    return res;
}

static BreakpointInfo * find_breakpoint(char * id) {
    int hash = id2bp_hash(id);
    LINK * l = id2bp[hash].next;
    while (l != id2bp + hash) {
        BreakpointInfo * bp = link_id2bp(l);
        l = l->next;
        if (strcmp(bp->id, id) == 0) return bp;
    }
    return NULL;
}

static BreakpointRef * find_breakpoint_ref(BreakpointInfo * bp, InputStream * inp) {
    LINK * l;
    if (bp == NULL) return NULL;
    l = bp->refs.next;
    while (l != &bp->refs) {
        BreakpointRef * br = link_bp2br(l);
        assert(br->bp == bp);
        if (br->inp == inp) return br;
        l = l->next;
    }
    return NULL;
}

static void add_breakpoint(InputStream * inp, BreakpointInfo * bp) {
    BreakpointRef * r = NULL;
    BreakpointInfo * i = NULL;
    int chng = 0;
    i = find_breakpoint(bp->id);
    if (i == NULL) {
        int hash = id2bp_hash(bp->id);
        i = (BreakpointInfo *)loc_alloc_zero(sizeof(BreakpointInfo));
        list_init(&i->refs);
        list_add_last(&i->link_all, &breakpoints);
        list_add_last(&i->link_id, id2bp + hash);
    }
    chng = copy_breakpoint_info(i, bp);
    if (i->deleted) {
        i->deleted = 0;
        chng = 1;
    }
    r = find_breakpoint_ref(i, inp);
    if (r == NULL) {
        int inp_hash = (int)inp / 16 % INP2BR_HASH_SIZE;
        r = (BreakpointRef *)loc_alloc_zero(sizeof(BreakpointRef));
        list_add_last(&r->link_inp, inp2br + inp_hash);
        list_add_last(&r->link_bp, &i->refs);
        r->inp = inp;
        r->bp = i;
    }
    else {
        assert(r->bp == i);
        assert(!list_is_empty(&i->refs));
    }
    if (chng) {
        if (i->planted || i->enabled && !i->unsupported) replant_breakpoints();
    }
}

static void remove_breakpoint(BreakpointInfo * bp) {
    assert(list_is_empty(&bp->refs));
    if (bp->planted) {
        bp->deleted = 1;
        replant_breakpoints();
    }
    else {
        list_remove(&bp->link_all);
        list_remove(&bp->link_id);
        loc_free(bp->address);
        loc_free(bp->condition);
        loc_free(bp);
    }
}

static void remove_ref(BreakpointRef * br) {
    BreakpointInfo * bp = br->bp;
    list_remove(&br->link_inp);
    list_remove(&br->link_bp);
    loc_free(br);
    if (list_is_empty(&bp->refs)) remove_breakpoint(bp);
}

static void delete_breakpoint_refs(InputStream * inp) {
    int hash = (int)inp / 16 % INP2BR_HASH_SIZE;
    LINK * l = inp2br[hash].next;
    while (l != &inp2br[hash]) {
        BreakpointRef * br = link_inp2br(l);
        l = l->next;
        if (br->inp == inp) remove_ref(br);
    }
}

static void read_breakpoint_properties(InputStream * inp, BreakpointInfo * bp) {
    memset(bp, 0, sizeof(BreakpointInfo));
    if (inp->read(inp) != '{') exception(ERR_JSON_SYNTAX);
    if (inp->peek(inp) == '}') {
        inp->read(inp);
    }
    else {
        while (1) {
            int ch;
            char name[256];
            json_read_string(inp, name, sizeof(name));
            if (inp->read(inp) != ':') exception(ERR_JSON_SYNTAX);
            if (strcmp(name, "ID") == 0) {
                json_read_string(inp, bp->id, sizeof(bp->id));
            }
            else if (strcmp(name, "Address") == 0) {
                bp->address = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "Condition") == 0) {
                bp->condition = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "Enabled") == 0) {
                bp->enabled = json_read_boolean(inp);
            }
            else {
                bp->unsupported = 1;
                json_skip_object(inp);
            }
            ch = inp->read(inp);
            if (ch == ',') continue;
            if (ch == '}') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
}

static void command_ini_bps(char * token, Channel * c) {
    delete_breakpoint_refs(&c->inp);
    if (c->inp.read(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (c->inp.peek(&c->inp) == ']') {
        c->inp.read(&c->inp);
    }
    else {
        while (1) {
            int ch;
            BreakpointInfo bp;
            read_breakpoint_properties(&c->inp, &bp);
            add_breakpoint(&c->inp, &bp);
            ch = c->inp.read(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_bp_ids(char * token, Channel * c) {
    // TODO: implement command_get_bp_ids()
    exception(ERR_PROTOCOL);
}

static void command_get_properties(char * token, Channel * c) {
    // TODO: implement command_get_properties()
    exception(ERR_PROTOCOL);
}

static void command_get_status(char * token, Channel * c) {
    // TODO: implement command_get_status()
    exception(ERR_PROTOCOL);
}

static void command_bp_add(char * token, Channel * c) {
    BreakpointInfo bp;
    read_breakpoint_properties(&c->inp, &bp);
    add_breakpoint(&c->inp, &bp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_bp_change(char * token, Channel * c) {
    BreakpointInfo bp;
    BreakpointInfo * p;
    read_breakpoint_properties(&c->inp, &bp);
    p = find_breakpoint(bp.id);
    if (p != NULL && copy_breakpoint_info(p, &bp)) {
        if (p->planted || p->enabled && !p->unsupported) replant_breakpoints();
    }
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_bp_enable(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (c->inp.peek(&c->inp) == ']') {
        c->inp.read(&c->inp);
    }
    else {
        while (1) {
            int ch;
            char id[256];
            BreakpointInfo * bp;
            json_read_string(&c->inp, id, sizeof(id));
            bp = find_breakpoint(id);
            if (bp != NULL && !bp->enabled) {
                bp->enabled = 1;
                if (!bp->deleted && !bp->unsupported) replant_breakpoints();
            }
            ch = c->inp.read(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_bp_disable(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (c->inp.peek(&c->inp) == ']') {
        c->inp.read(&c->inp);
    }
    else {
        while (1) {
            int ch;
            char id[256];
            BreakpointInfo * bp;
            json_read_string(&c->inp, id, sizeof(id));
            bp = find_breakpoint(id);
            if (bp != NULL && bp->enabled) {
                bp->enabled = 0;
                if (bp->planted) replant_breakpoints();
            }
            ch = c->inp.read(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_bp_remove(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (c->inp.peek(&c->inp) == ']') {
        c->inp.read(&c->inp);
    }
    else {
        while (1) {
            int ch;
            char id[256];
            BreakpointRef * br;
            json_read_string(&c->inp, id, sizeof(id));
            br = find_breakpoint_ref(find_breakpoint(id), &c->inp);
            if (br != NULL) remove_ref(br);
            ch = c->inp.read(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

int is_stopped_by_breakpoint(Context * ctx) {
    BreakInstruction * bi;

    assert(!ctx->exited);
    assert(ctx->stopped);
#if defined(_WRS_KERNEL) || defined(WIN32)
    if (ctx->stopped_by_bp == 1) {
        bi = find_instruction(ctx, get_regs_PC(ctx->regs));
        if (bi == NULL || bi->skip || bi->error) {
            // Break instruction that is not planted by us
            ctx->stopped_by_bp = 0;
            ctx->pending_intercept = 1;
            return 0;
        }
        ctx->stopped_by_bp = 2;
    }
    return ctx->stopped_by_bp != 0;
#else
    if (ctx->signal != SIGTRAP) return 0;
    if (ctx->event != 0) return 0;
    if (ctx->regs_error) return 0;
    if (ctx->stopped_by_bp) return 1;
    bi = find_instruction(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE);
    if (bi == NULL || bi->skip || bi->error) return 0;
    set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
    ctx->regs_dirty = 1;
    ctx->stopped_by_bp = 1;
    return 1;
#endif    
}

static int condition_expression_identifier(char * name, Value * v) {
    return address_expression_identifier(name, v);
}

int evaluate_breakpoint_condition(Context * ctx) {
    int i;
    BreakInstruction * bi = find_instruction(ctx, get_regs_PC(ctx->regs));
    if (bi == NULL) return 0;
    expression_context = ctx;
    for (i = 0; i < bi->ref_cnt; i++) {
        Value v;
        BreakpointInfo * bp = bi->refs[i];
        assert(bp->planted);
        assert(bp->error == 0);
        if (bp->deleted) continue;
        if (bp->unsupported) continue;
        if (!bp->enabled) continue;
        if (bp->condition == NULL) return 1;
        if (evaluate_expression(&bp_condition_ctx, bp->condition, &v) < 0) {
            trace(LOG_ALWAYS, "%s: %s", get_expression_error_msg(), bp->condition);
            return 1;
        }
        switch (v.type) {
        case VALUE_INT:
        case VALUE_UNS:
            if (v.value) return 1;
            break;
        case VALUE_STR:
            if (v.str != NULL) return 1;
            break;
        }
    }
    return 0;
}

#ifndef _WRS_KERNEL

static void safe_restore_breakpoint(void * arg) {
    SkipBreakpointInfo * sb = (SkipBreakpointInfo *)arg;
    BreakInstruction * bi = find_instruction(sb->ctx, sb->address);

    if (bi != NULL && bi->skip) {
        assert(bi->error == 0);
        bi->skip = 0;
        plant_instruction(bi);
    }
    if (sb->done) sb->done(sb);
    if (sb->c) stream_unlock(sb->c);
    context_unlock(sb->ctx);
    loc_free(sb);
}

static void safe_skip_breakpoint(void * arg) {
    SkipBreakpointInfo * sb = (SkipBreakpointInfo *)arg;

    assert(!sb->ctx->exited);
    assert(sb->ctx->stopped);
    assert(!sb->ctx->intercepted);
    assert(!sb->ctx->regs_error);
    assert(sb->address == get_regs_PC(sb->ctx->regs));
    
    if (sb->error == 0) {
        BreakInstruction * bi = find_instruction(sb->ctx, sb->address);
        if (bi != NULL && !bi->skip) {
            if (bi->error) {
                sb->error = bi->error;
            }
            else {
                remove_instruction(bi);
                bi->skip = 1;
            }
        }
    }
    if (sb->error == 0) {
        post_safe_event(safe_restore_breakpoint, sb);
        if (context_single_step(sb->ctx) < 0) {
            sb->error = errno;
        }
        else if (sb->pending_intercept) {
            sb->ctx->pending_intercept = 1;
        }
    }
    else {
        if (sb->done) sb->done(sb);
        if (sb->c) stream_unlock(sb->c);
        context_unlock(sb->ctx);
        loc_free(sb);
    }
}

#endif

/*
 * When a context is stopped by breakpoint, it is necessary to disable
 * the breakpoint temporarily before the context can be resumed.
 * This function function removes break instruction, then does single step
 * over breakpoint location, then restores break intruction.
 * Return: NULL if it is OK to resume context from current state,
 * SkipBreakpointInfo pointer if context needs to step over a breakpoint.
 */
SkipBreakpointInfo * skip_breakpoint(Context * ctx) {
    BreakInstruction * bi;
    SkipBreakpointInfo * sb;

    assert(!ctx->exited);
    assert(ctx->stopped);

#ifdef _WRS_KERNEL
    /* VxWork debug library can skip breakpoint when neccesary, no code is needed here */
    return NULL;
#else
    if (ctx->exited || ctx->exiting) return NULL;
    assert(!ctx->regs_error);
    bi = find_instruction(ctx, get_regs_PC(ctx->regs));
    if (bi == NULL || bi->error) return NULL;
    assert(!bi->skip);
    sb = (SkipBreakpointInfo *)loc_alloc_zero(sizeof(SkipBreakpointInfo));
    context_lock(ctx);
    sb->ctx = ctx;
    sb->address = get_regs_PC(ctx->regs);
    post_safe_event(safe_skip_breakpoint, sb);
    return sb;
#endif
}

static void event_context_created_or_exited(Context * ctx, void * client_data) {
    if (ctx->parent == NULL) replant_breakpoints();
}

static void channel_close_listener(Channel * c) {
    delete_breakpoint_refs(&c->inp);
}

void ini_breakpoints_service(Protocol *proto, TCFBroadcastGroup *bcg) {
    int i;
    static ContextEventListener listener = {
        event_context_created_or_exited,
        event_context_created_or_exited,
        NULL,
        NULL,
        NULL
    };
    add_context_event_listener(&listener, bcg);
    list_init(&breakpoints);
    list_init(&instructions);
    for (i = 0; i < ADDR2INSTR_HASH_SIZE; i++) list_init(addr2instr + i);
    for (i = 0; i < ID2BP_HASH_SIZE; i++) list_init(id2bp + i);
    for (i = 0; i < INP2BR_HASH_SIZE; i++) list_init(inp2br + i);
    add_channel_close_listener(channel_close_listener);
    add_command_handler(proto, BREAKPOINTS, "set", command_ini_bps);
    add_command_handler(proto, BREAKPOINTS, "add", command_bp_add);
    add_command_handler(proto, BREAKPOINTS, "change", command_bp_change);
    add_command_handler(proto, BREAKPOINTS, "enable", command_bp_enable);
    add_command_handler(proto, BREAKPOINTS, "disable", command_bp_disable);
    add_command_handler(proto, BREAKPOINTS, "remove", command_bp_remove);
    add_command_handler(proto, BREAKPOINTS, "getBreakpointIDs", command_get_bp_ids);
    add_command_handler(proto, BREAKPOINTS, "getProperties", command_get_properties);
    add_command_handler(proto, BREAKPOINTS, "getStatus", command_get_status);
}

#endif
