/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 * This module implements Breakpoints service.
 * The service maintains a bp_arr of breakpoints.
 * Each breakpoint consists of one or more conditions that determine
 * when a program's execution should be interrupted.
 */

#include <config.h>

#if SERVICE_Breakpoints

#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <framework/channel.h>
#include <framework/protocol.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/context.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/cache.h>
#include <framework/json.h>
#include <framework/link.h>
#include <services/symbols.h>
#include <services/runctrl.h>
#include <services/breakpoints.h>
#include <services/expressions.h>
#include <services/linenumbers.h>
#include <services/stacktrace.h>
#include <services/memorymap.h>

typedef struct BreakpointRef BreakpointRef;
typedef struct BreakpointAttribute BreakpointAttribute;
typedef struct InstructionRef InstructionRef;
typedef struct BreakInstruction BreakInstruction;
typedef struct EvaluationArgs EvaluationArgs;
typedef struct EvaluationRequest EvaluationRequest;
typedef struct ConditionEvaluationRequest ConditionEvaluationRequest;
typedef struct ContextExtensionBP ContextExtensionBP;

struct BreakpointRef {
    LINK link_inp;
    LINK link_bp;
    Channel * channel;
    BreakpointInfo * bp;
};

struct BreakpointAttribute {
    BreakpointAttribute * next;
    char * name;
    char * value;
};

struct BreakpointInfo {
    Context * ctx; /* NULL means all contexts */
    LINK link_all;
    LINK link_id;
    LINK link_clients;
    char id[256];
    int enabled;
    int client_cnt;
    int instruction_cnt;
    ErrorReport * error;
    char * address;
    char * type;
    char * condition;
    char ** context_ids;
    char ** context_ids_prev;
    char ** stop_group;
    char * file;
    int access_mode;
    int access_size;
    int line;
    int column;
    int ignore_count;
    int hit_count;
    BreakpointAttribute * unsupported;

    EventPointCallBack * event_callback;
    void * event_callback_args;

    int status_changed;
};

struct InstructionRef {
    BreakpointInfo * bp;
    Context * ctx;
    ContextAddress addr;
    ContextAddress size;
    ErrorReport * address_error;
    int cnt;
};

struct BreakInstruction {
    LINK link_all;
    LINK link_adr;
    ContextBreakpoint cb;
    char saved_code[16];
    size_t saved_size;
    ErrorReport * planting_error;
    int stepping_over_bp;
    InstructionRef * refs;
    int ref_size;
    int ref_cnt;
    int valid;
    int planted;
};

struct EvaluationArgs {
    BreakpointInfo * bp;
    Context * ctx;
};

struct ConditionEvaluationRequest {
    BreakpointInfo * bp;
    int condition_ok;
    int triggered;
};

struct EvaluationRequest {
    Context * ctx;
    BreakpointInfo * bp; /* NULL means all breakpoints */
    LINK link_posted;
    LINK link_active;
    int location;
    int bp_cnt;
    int bp_max;
    ConditionEvaluationRequest * bp_arr;
};

struct ContextExtensionBP {
    int                 step_over_bp_cnt;
    BreakInstruction *  stepping_over_bp;   /* if not NULL, the context is stepping over a breakpoint instruction */
    char **             bp_ids;             /* if stopped by breakpoint, contains NULL-terminated list of breakpoint IDs */
    EvaluationRequest * req;
    Context *           bp_grp;
    int                 empty_bp_grp;
};

static const char * BREAKPOINTS = "Breakpoints";

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionBP *)((char *)(ctx) + context_extension_offset))

#define is_disabled(bp) (bp->enabled == 0 || bp->client_cnt == 0 || bp->unsupported != NULL)

#define ADDR2INSTR_HASH_SIZE (32 * MEM_USAGE_FACTOR - 1)
#define addr2instr_hash(ctx, addr) ((unsigned)((uintptr_t)(ctx) + (uintptr_t)(addr) + ((uintptr_t)(addr) >> 8)) % ADDR2INSTR_HASH_SIZE)

#define link_all2bi(A)  ((BreakInstruction *)((char *)(A) - offsetof(BreakInstruction, link_all)))
#define link_adr2bi(A)  ((BreakInstruction *)((char *)(A) - offsetof(BreakInstruction, link_adr)))

#define ID2BP_HASH_SIZE (32 * MEM_USAGE_FACTOR - 1)

#define link_all2bp(A)  ((BreakpointInfo *)((char *)(A) - offsetof(BreakpointInfo, link_all)))
#define link_id2bp(A)   ((BreakpointInfo *)((char *)(A) - offsetof(BreakpointInfo, link_id)))

#define INP2BR_HASH_SIZE (4 * MEM_USAGE_FACTOR - 1)

#define link_inp2br(A)  ((BreakpointRef *)((char *)(A) - offsetof(BreakpointRef, link_inp)))
#define link_bp2br(A)   ((BreakpointRef *)((char *)(A) - offsetof(BreakpointRef, link_bp)))

#define link_posted2erl(A)  ((EvaluationRequest *)((char *)(A) - offsetof(EvaluationRequest, link_posted)))
#define link_active2erl(A)  ((EvaluationRequest *)((char *)(A) - offsetof(EvaluationRequest, link_active)))
#define link_bcg2chnl(A) ((Channel *)((char *)(A) - offsetof(Channel, bclink)))

static LINK breakpoints;
static LINK id2bp[ID2BP_HASH_SIZE];

static LINK instructions;
static LINK addr2instr[ADDR2INSTR_HASH_SIZE];

static LINK inp2br[INP2BR_HASH_SIZE];

static LINK evaluations_posted;
static LINK evaluations_active;
static uintptr_t generation_posted = 0;
static uintptr_t generation_active = 0;
static uintptr_t generation_done = 0;
static int planting_instruction = 0;
static int cache_enter_cnt = 0;

static TCFBroadcastGroup * broadcast_group = NULL;

static unsigned id2bp_hash(char * id) {
    unsigned hash = 0;
    while (*id) hash = (hash >> 16) + hash + (unsigned char)*id++;
    return hash % ID2BP_HASH_SIZE;
}

static void get_bi_access_types(BreakInstruction * bi, unsigned * access_types, ContextAddress * access_size) {
    int i;
    unsigned t = 0;
    ContextAddress sz = 0;
    for (i = 0; i < bi->ref_cnt; i++) {
        if (bi->refs[i].cnt) {
            int md = bi->refs[i].bp->access_mode;
            if (md == 0) {
                t |= CTX_BP_ACCESS_INSTRUCTION;
            }
            else {
                t |= md;
            }
            if (sz < bi->refs[i].size) sz = bi->refs[i].size;
            /* TODO: parse type (soft|hw) */
        }
    }
    *access_types = t;
    *access_size = sz;
}

static void plant_instruction(BreakInstruction * bi) {
    int i;
    int error = 0;
    size_t saved_size = bi->saved_size;
    ErrorReport * rp = NULL;

    assert(!bi->stepping_over_bp);
    assert(!bi->planted);
    assert(!bi->cb.ctx->exited);
    assert(bi->valid);
    if (bi->cb.address == 0) return;
    assert(is_all_stopped(bi->cb.ctx));

    get_bi_access_types(bi, &bi->cb.access_types, &bi->cb.length);

    bi->saved_size = 0;
    if (context_plant_breakpoint(&bi->cb) < 0) {
        if (bi->cb.access_types == CTX_BP_ACCESS_INSTRUCTION && get_error_code(errno) == ERR_UNSUPPORTED) {
            uint8_t * break_inst = get_break_instruction(bi->cb.ctx, &bi->saved_size);
            assert(sizeof(bi->saved_code) >= bi->saved_size);
            planting_instruction = 1;
            if (context_read_mem(bi->cb.ctx, bi->cb.address, bi->saved_code, bi->saved_size) < 0) {
                error = errno;
            }
            else if (context_write_mem(bi->cb.ctx, bi->cb.address, break_inst, bi->saved_size) < 0) {
                error = errno;
            }
            planting_instruction = 0;
        }
        else {
            error = errno;
        }
    }
    rp = get_error_report(error);
    if (saved_size != bi->saved_size || !compare_error_reports(bi->planting_error, rp)) {
        release_error_report(bi->planting_error);
        bi->planting_error = rp;
        for (i = 0; i < bi->ref_cnt; i++) {
            bi->refs[i].bp->status_changed = 1;
        }
    }
    else {
        release_error_report(rp);
    }
    bi->planted = bi->planting_error == NULL;
}

static void remove_instruction(BreakInstruction * bi) {
    assert(bi->planted);
    assert(bi->planting_error == NULL);
    assert(is_all_stopped(bi->cb.ctx));
    if (bi->saved_size) {
        if (!bi->cb.ctx->exited) {
            planting_instruction = 1;
            if (context_write_mem(bi->cb.ctx, bi->cb.address, bi->saved_code, bi->saved_size) < 0) {
                bi->planting_error = get_error_report(errno);
            }
            planting_instruction = 0;
        }
    }
    else if (context_unplant_breakpoint(&bi->cb) < 0) {
        bi->planting_error = get_error_report(errno);
    }
    bi->planted = 0;
}

#ifndef NDEBUG
static int is_canonical_addr(Context * ctx, ContextAddress address) {
    Context * mem = NULL;
    ContextAddress mem_addr = 0;
    if (context_get_canonical_addr(ctx, address, &mem, &mem_addr, NULL, NULL) < 0) return 0;
    return mem == ctx && address == mem_addr;
}
#endif

static BreakInstruction * find_instruction(Context * ctx, ContextAddress address) {
    int hash = addr2instr_hash(ctx, address);
    LINK * l = addr2instr[hash].next;
    if (address == 0) return NULL;
    assert(is_canonical_addr(ctx, address));
    while (l != addr2instr + hash) {
        BreakInstruction * bi = link_adr2bi(l);
        if (bi->cb.ctx == ctx && bi->cb.address == address) return bi;
        l = l->next;
    }
    return NULL;
}

static BreakInstruction * add_instruction(Context * ctx, ContextAddress address) {
    int hash = addr2instr_hash(ctx, address);
    BreakInstruction * bi = (BreakInstruction *)loc_alloc_zero(sizeof(BreakInstruction));
    assert(find_instruction(ctx, address) == NULL);
    list_add_last(&bi->link_all, &instructions);
    list_add_last(&bi->link_adr, addr2instr + hash);
    context_lock(ctx);
    bi->cb.ctx = ctx;
    bi->cb.address = address;
    return bi;
}

static void clear_instruction_refs(Context * ctx, BreakpointInfo * bp) {
    LINK * l = instructions.next;
    while (l != &instructions) {
        int i;
        BreakInstruction * bi = link_all2bi(l);
        for (i = 0; i < bi->ref_cnt; i++) {
            InstructionRef * ref = bi->refs + i;
            if (ref->ctx != ctx) continue;
            if (bp != NULL && ref->bp != bp) continue;
            ref->size = 0;
            ref->cnt = 0;
            bi->valid = 0;
        }
        l = l->next;
    }
}

static void flush_instructions(void) {
    LINK * l = instructions.next;
    while (l != &instructions) {
        int i = 0;
        BreakInstruction * bi = link_all2bi(l);
        l = l->next;
        if (bi->valid) continue;
        while (i < bi->ref_cnt) {
            if (bi->refs[i].cnt == 0) {
                bi->refs[i].bp->instruction_cnt--;
                bi->refs[i].bp->status_changed = 1;
                context_unlock(bi->refs[i].ctx);
                release_error_report(bi->refs[i].address_error);
                memmove(bi->refs + i, bi->refs + i + 1, sizeof(InstructionRef) * (bi->ref_cnt - i - 1));
                bi->ref_cnt--;
            }
            else {
                i++;
            }
        }
        bi->valid = 1;
        if (!bi->stepping_over_bp) {
            if (bi->ref_cnt == 0) {
                if (bi->planted) remove_instruction(bi);
                list_remove(&bi->link_all);
                list_remove(&bi->link_adr);
                context_unlock(bi->cb.ctx);
                release_error_report(bi->planting_error);
                loc_free(bi->refs);
                loc_free(bi);
            }
            else if (!bi->planted) {
                plant_instruction(bi);
            }
            else {
                unsigned type = 0;
                ContextAddress size = 0;
                get_bi_access_types(bi, &type, &size);
                if (bi->cb.access_types != type || bi->cb.length != size) {
                    remove_instruction(bi);
                    plant_instruction(bi);
                }
            }
        }
    }
}

void clone_breakpoints_on_process_fork(Context * parent, Context * child) {
    Context * mem = context_get_group(parent, CONTEXT_GROUP_PROCESS);
    LINK * l = instructions.next;
    while (l != &instructions) {
        int i;
        BreakInstruction * ci = NULL;
        BreakInstruction * bi = link_all2bi(l);
        l = l->next;
        if (!bi->planted) continue;
        if (!bi->saved_size) continue;
        if (bi->cb.ctx != mem) continue;
        ci = add_instruction(child, bi->cb.address);
        ci->cb.length = bi->cb.length;
        ci->cb.access_types = bi->cb.access_types;
        memcpy(ci->saved_code, bi->saved_code, bi->saved_size);
        ci->saved_size = bi->saved_size;
        ci->ref_size = bi->ref_size;
        ci->ref_cnt = bi->ref_cnt;
        ci->refs = (InstructionRef *)loc_alloc_zero(sizeof(InstructionRef) * ci->ref_size);
        for (i = 0; i < bi->ref_cnt; i++) {
            BreakpointInfo * bp = bi->refs[i].bp;
            ci->refs[i] = bi->refs[i];
            ci->refs[i].ctx = child;
            context_lock(child);
            bp->instruction_cnt++;
            bp->status_changed = 1;
        }
        ci->valid = 1;
        ci->planted = 1;
    }
}

int check_breakpoints_on_memory_read(Context * ctx, ContextAddress address, void * p, size_t size) {
    if (!planting_instruction) {
        while (size > 0) {
            size_t sz = size;
            uint8_t * buf = (uint8_t *)p;
            LINK * l = instructions.next;
            Context * mem = NULL;
            ContextAddress mem_addr = 0;
            ContextAddress mem_base = 0;
            ContextAddress mem_size = 0;
            if (context_get_canonical_addr(ctx, address, &mem, &mem_addr, &mem_base, &mem_size) < 0) return -1;
            if (mem_base + mem_size - mem_addr < sz) sz = mem_base + mem_size - mem_addr;
            while (l != &instructions) {
                BreakInstruction * bi = link_all2bi(l);
                size_t i;
                l = l->next;
                if (!bi->planted) continue;
                if (!bi->saved_size) continue;
                if (bi->cb.ctx != mem) continue;
                if (bi->cb.address + bi->saved_size <= mem_addr) continue;
                if (bi->cb.address >= mem_addr + sz) continue;
                for (i = 0; i < bi->saved_size; i++) {
                    if (bi->cb.address + i < mem_addr) continue;
                    if (bi->cb.address + i >= mem_addr + sz) continue;
                    buf[bi->cb.address + i - mem_addr] = bi->saved_code[i];
                }
            }
            p = (uint8_t *)p + sz;
            address += sz;
            size -= sz;
        }
    }
    return 0;
}

int check_breakpoints_on_memory_write(Context * ctx, ContextAddress address, void * p, size_t size) {
    if (!planting_instruction) {
        while (size > 0) {
            size_t sz = size;
            uint8_t * buf = (uint8_t *)p;
            LINK * l = instructions.next;
            Context * mem = NULL;
            ContextAddress mem_addr = 0;
            ContextAddress mem_base = 0;
            ContextAddress mem_size = 0;
            if (context_get_canonical_addr(ctx, address, &mem, &mem_addr, &mem_base, &mem_size) < 0) return -1;
            if (mem_base + mem_size - mem_addr < sz) sz = mem_base + mem_size - mem_addr;
            while (l != &instructions) {
                BreakInstruction * bi = link_all2bi(l);
                l = l->next;
                if (!bi->planted) continue;
                if (!bi->saved_size) continue;
                if (bi->cb.ctx != mem) continue;
                if (bi->cb.address + bi->saved_size <= mem_addr) continue;
                if (bi->cb.address >= mem_addr + sz) continue;
                {
                    size_t i;
                    uint8_t * break_inst = get_break_instruction(bi->cb.ctx, &i);
                    assert(i == bi->saved_size);
                    for (i = 0; i < bi->saved_size; i++) {
                        if (bi->cb.address + i < mem_addr) continue;
                        if (bi->cb.address + i >= mem_addr + sz) continue;
                        bi->saved_code[i] = buf[bi->cb.address + i - mem_addr];
                        buf[bi->cb.address + i - mem_addr] = break_inst[i];
                    }
                }
            }
            p = (uint8_t *)p + sz;
            address += sz;
            size -= sz;
        }
    }
    return 0;
}

static void write_breakpoint_status(OutputStream * out, BreakpointInfo * bp) {
    BreakpointAttribute * u = bp->unsupported;

    assert(*bp->id);
    write_stream(out, '{');

    if (u != NULL) {
        const char * msg = "Unsupported breakpoint properties: ";
        json_write_string(out, "Error");
        write_stream(out, ':');
        write_stream(out, '"');
        while (*msg) json_write_char(out, *msg++);
        while (u != NULL) {
            msg = u->name;
            while (*msg) json_write_char(out, *msg++);
            u = u->next;
            if (u != NULL) {
                json_write_char(out, ',');
                json_write_char(out, ' ');
            }
        }
        write_stream(out, '"');
    }
    else if (bp->instruction_cnt) {
        int cnt = 0;
        LINK * l = instructions.next;
        json_write_string(out, "Instances");
        write_stream(out, ':');
        write_stream(out, '[');
        while (l != &instructions) {
            int i = 0;
            BreakInstruction * bi = link_all2bi(l);
            l = l->next;
            for (i = 0; i < bi->ref_cnt; i++) {
                if (bi->refs[i].bp != bp) continue;
                if (cnt > 0) write_stream(out, ',');
                write_stream(out, '{');
                json_write_string(out, "LocationContext");
                write_stream(out, ':');
                json_write_string(out, bi->refs[i].ctx->id);
                write_stream(out, ',');
                if (bi->refs[i].address_error != NULL) {
                    json_write_string(out, "Error");
                    write_stream(out, ':');
                    json_write_string(out, errno_to_str(set_error_report_errno(bi->refs[i].address_error)));
                }
                else {
                    json_write_string(out, "Address");
                    write_stream(out, ':');
                    json_write_uint64(out, bi->refs[i].addr);
                    write_stream(out, ',');
                    json_write_string(out, "Size");
                    write_stream(out, ':');
                    json_write_uint64(out, bi->refs[i].size);
                    if (bi->planting_error != NULL) {
                        write_stream(out, ',');
                        json_write_string(out, "Error");
                        write_stream(out, ':');
                        json_write_string(out, errno_to_str(set_error_report_errno(bi->planting_error)));
                    }
                    else if (bi->planted) {
                        write_stream(out, ',');
                        json_write_string(out, "BreakpointType");
                        write_stream(out, ':');
                        json_write_string(out, bi->saved_size ? "Software" : "Hardware");
                    }
                }
                write_stream(out, '}');
                cnt++;
            }
        }
        write_stream(out, ']');
        assert(cnt > 0);
    }
    else if (bp->error) {
        json_write_string(out, "Error");
        write_stream(out, ':');
        json_write_string(out, errno_to_str(set_error_report_errno(bp->error)));
    }

    write_stream(out, '}');
}

static void send_event_breakpoint_status(OutputStream * out, BreakpointInfo * bp) {
    write_stringz(out, "E");
    write_stringz(out, BREAKPOINTS);
    write_stringz(out, "status");

    json_write_string(out, bp->id);
    write_stream(out, 0);
    write_breakpoint_status(out, bp);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static InstructionRef * link_breakpoint_instruction(BreakpointInfo * bp, Context * ctx,
        ContextAddress ctx_addr, ContextAddress size, Context * mem, ContextAddress mem_addr) {
    BreakInstruction * bi = NULL;
    InstructionRef * ref = NULL;
    bi = find_instruction(mem, mem_addr);
    if (bi == NULL) {
        bi = add_instruction(mem, mem_addr);
    }
    else {
        int i = 0;
        while (i < bi->ref_cnt) {
            ref = bi->refs + i;
            if (ref->bp == bp && ref->ctx == ctx) {
                assert(!bi->valid);
                if (ref->size < size) ref->size = size;
                ref->addr = ctx_addr;
                ref->cnt++;
                return ref;
            }
            i++;
        }
    }
    if (bi->ref_cnt >= bi->ref_size) {
        bi->ref_size = bi->ref_size == 0 ? 8 : bi->ref_size * 2;
        bi->refs = (InstructionRef *)loc_realloc(bi->refs, sizeof(InstructionRef) * bi->ref_size);
    }
    ref = bi->refs + bi->ref_cnt++;
    context_lock(ctx);
    memset(ref, 0, sizeof(InstructionRef));
    ref->bp = bp;
    ref->ctx = ctx;
    ref->addr = ctx_addr;
    ref->size = size;
    ref->cnt = 1;
    bi->valid = 0;
    bp->instruction_cnt++;
    bp->status_changed = 1;
    return ref;
}

static void address_expression_error(Context * ctx, BreakpointInfo * bp, int error) {
    ErrorReport * rp = NULL;
    if (get_error_code(errno) == ERR_CACHE_MISS) return;
    assert(error != 0);
    assert(bp->instruction_cnt == 0 || bp->error == NULL);
    rp = get_error_report(error);
    assert(rp != NULL);
    if (ctx != NULL) {
        InstructionRef * ref = link_breakpoint_instruction(bp, ctx, 0, 0, ctx, 0);
        if (!compare_error_reports(rp, ref->address_error)) {
            release_error_report(ref->address_error);
            ref->address_error = rp;
            bp->status_changed = 1;
        }
        else {
            release_error_report(rp);
        }
    }
    else if (!compare_error_reports(rp, bp->error)) {
        release_error_report(bp->error);
        bp->error = rp;
        bp->status_changed = 1;
    }
    else {
        release_error_report(rp);
    }
}

static void plant_breakpoint(Context * ctx, BreakpointInfo * bp, ContextAddress addr, ContextAddress size) {
    Context * mem = NULL;
    ContextAddress mem_addr;
    if (context_get_canonical_addr(ctx, addr, &mem, &mem_addr, NULL, NULL) < 0) {
        address_expression_error(ctx, bp, errno);
    }
    else {
        link_breakpoint_instruction(bp, ctx, addr, size, mem, mem_addr);
    }
}

static void event_replant_breakpoints(void * arg);

static EvaluationRequest * create_evaluation_request(Context * ctx, int bp_cnt) {
    EvaluationRequest * req = EXT(ctx)->req;
    if (req == NULL) {
        req = (EvaluationRequest *)loc_alloc_zero(sizeof(EvaluationRequest));
        req->ctx = ctx;
        list_init(&req->link_posted);
        list_init(&req->link_active);
        EXT(ctx)->req = req;
    }
    if (req->bp_max < bp_cnt) {
        req->bp_max = bp_cnt;
        req->bp_arr = (ConditionEvaluationRequest *)loc_realloc(req->bp_arr, sizeof(ConditionEvaluationRequest) * req->bp_max);
    }
    assert(req->ctx == ctx);
    assert(req->bp_cnt == 0);
    if (bp_cnt > 0) {
        memset(req->bp_arr, 0, sizeof(ConditionEvaluationRequest) * bp_cnt);
        req->bp_cnt = bp_cnt;
    }
    return req;
}

static void post_evaluation_request(EvaluationRequest * req) {
    if (list_is_empty(&req->link_posted)) {
        context_lock(req->ctx);
        list_add_last(&req->link_posted, &evaluations_posted);
        post_safe_event(req->ctx, event_replant_breakpoints, (void *)++generation_posted);
    }
}

static void post_location_evaluation_request(Context * ctx, BreakpointInfo * bp) {
    ContextExtensionBP * ext = EXT(ctx);
    Context * grp = context_get_group(ctx, CONTEXT_GROUP_BREAKPOINT);
    if (ext->bp_grp != NULL && ext->bp_grp != grp && !ext->bp_grp->exited) {
        /* The context has migrated into another breakpoint group.
         * If the old group became empty, we need to remove breakpoints in it.
         */
        int cnt = 0;
        LINK * l = context_root.next;
        while (l != &context_root) {
            Context * c = ctxl2ctxp(l);
            l = l->next;
            if (c->exited) continue;
            if (context_get_group(c, CONTEXT_GROUP_BREAKPOINT) == ext->bp_grp) cnt++;
        }
        if (cnt == 0) {
            EvaluationRequest * req = create_evaluation_request(ext->bp_grp, 0);
            req->bp = NULL;
            req->location = 1;
            post_evaluation_request(req);
            EXT(ext->bp_grp)->empty_bp_grp = 1;
        }
    }
    ext->bp_grp = grp;
    if (grp != NULL) {
        EvaluationRequest * req = create_evaluation_request(grp, 0);
        if (!req->location) {
            req->bp = bp;
            req->location = 1;
            post_evaluation_request(req);
        }
        else if (req->bp != bp) {
            req->bp = NULL;
        }
        post_evaluation_request(req);
        EXT(grp)->empty_bp_grp = 0;
    }
}

static void expr_cache_enter(CacheClient * client, BreakpointInfo * bp, Context * ctx) {
    LINK * l = NULL;
    EvaluationArgs args;

    args.bp = bp;
    args.ctx = ctx;

    if (bp != NULL && bp->error) {
        release_error_report(bp->error);
        bp->error = NULL;
        bp->status_changed = 1;
    }

    l = broadcast_group->channels.next;
    while (l != &broadcast_group->channels) {
        Channel * c = link_bcg2chnl(l);
        if (!is_channel_closed(c)) {
            cache_enter_cnt++;
            run_ctrl_lock();
            cache_enter(client, c, &args, sizeof(args));
        }
        l = l->next;
    }
}

static void free_bp(BreakpointInfo * bp) {
    assert(bp->instruction_cnt == 0);
    assert(bp->client_cnt == 0);
    list_remove(&bp->link_all);
    if (*bp->id) list_remove(&bp->link_id);
    if (bp->ctx) context_unlock(bp->ctx);
    release_error_report(bp->error);
    loc_free(bp->address);
    loc_free(bp->type);
    loc_free(bp->context_ids);
    loc_free(bp->context_ids_prev);
    loc_free(bp->stop_group);
    loc_free(bp->file);
    loc_free(bp->condition);
    while (bp->unsupported != NULL) {
        BreakpointAttribute * u = bp->unsupported;
        bp->unsupported = u->next;
        loc_free(u->name);
        loc_free(u->value);
        loc_free(u);
    }
    assert(list_is_empty(&bp->link_clients));
    loc_free(bp);
}

static void notify_breakpoints_status(void) {
    LINK * l = NULL;
    assert(generation_done == generation_active);
    for (l = breakpoints.next; l != &breakpoints;) {
        BreakpointInfo * bp = link_all2bp(l);
        l = l->next;
#ifndef NDEBUG
        {
            /* Verify breakpoints data structure */
            LINK * m = NULL;
            int instruction_cnt = 0;
            for (m = instructions.next; m != &instructions; m = m->next) {
                int i;
                BreakInstruction * bi = link_all2bi(m);
                assert(bi->valid);
                assert(bi->ref_cnt <= bi->ref_size);
                assert(bi->cb.ctx->ref_count > 0);
                for (i = 0; i < bi->ref_cnt; i++) {
                    assert(bi->refs[i].cnt > 0);
                    if (bi->refs[i].bp == bp) {
                        instruction_cnt++;
                        assert(bp->client_cnt > 0);
                    }
                }
            }
            assert(bp->enabled || instruction_cnt == 0);
            assert(bp->unsupported == NULL || instruction_cnt == 0);
            assert(bp->instruction_cnt == instruction_cnt);
            if (*bp->id) {
                int i;
                int client_cnt = 0;
                for (i = 0; i < INP2BR_HASH_SIZE; i++) {
                    for (m = inp2br[i].next; m != &inp2br[i]; m = m->next) {
                        BreakpointRef * br = link_inp2br(m);
                        if (br->bp == bp) client_cnt++;
                    }
                }
                assert(bp->client_cnt == client_cnt);
            }
            else {
                assert(list_is_empty(&bp->link_clients));
            }
        }
#endif
        if (bp->client_cnt == 0) {
            assert(list_is_empty(&bp->link_clients));
            assert(bp->instruction_cnt == 0);
            free_bp(bp);
        }
        else if (bp->status_changed) {
            if (*bp->id) send_event_breakpoint_status(&broadcast_group->out, bp);
            bp->status_changed = 0;
        }
    }
}

static void done_condition_evaluation(EvaluationRequest * req) {
    Context * ctx = req->ctx;
    size_t size = 0;
    int i;

    for (i = 0; i < req->bp_cnt; i++) {
        BreakpointInfo * bp = req->bp_arr[i].bp;
        if (!req->bp_arr[i].condition_ok) continue;
        bp->hit_count++;
        if (bp->hit_count <= bp->ignore_count) continue;
        bp->hit_count = 0;
        if (bp->event_callback != NULL) {
            bp->event_callback(ctx, bp->event_callback_args);
        }
        else {
            assert(bp->id[0] != 0);
            req->bp_arr[i].triggered = 1;
            size += sizeof(char *) + strlen(bp->id) + 1;
        }
    }

    if (size > 0) {
        /* Create bp_arr of triggered breakpoint IDs */
        size_t mem_size = size + sizeof(char *);
        char ** bp_arr = (char **)loc_alloc(mem_size);
        char * pool = (char *)bp_arr + mem_size;
        assert(ctx->stopped);
        assert(EXT(ctx)->bp_ids == NULL);
        EXT(ctx)->bp_ids = bp_arr;
        for (i = 0; i < req->bp_cnt; i++) {
            BreakpointInfo * bp = req->bp_arr[i].bp;
            if (req->bp_arr[i].triggered) {
                size_t n = strlen(bp->id) + 1;
                pool -= n;
                memcpy(pool, bp->id, n);
                *bp_arr++ = pool;
            }
        }
        *bp_arr++ = NULL;
        assert((char *)bp_arr == pool);
        for (i = 0; i < req->bp_cnt; i++) {
            BreakpointInfo * bp = req->bp_arr[i].bp;
            if (req->bp_arr[i].triggered && bp->stop_group == NULL) {
                suspend_debug_context(ctx);
            }
        }
    }
}

static void done_all_evaluations(void) {
    LINK * l = evaluations_active.next;

    while (l != &evaluations_active) {
        EvaluationRequest * req = link_active2erl(l);
        l = l->next;
        if (req->bp_cnt) {
            assert(req->ctx->stopped_by_bp || req->ctx->stopped_by_cb);
            done_condition_evaluation(req);
        }
    }

    l = evaluations_active.next;
    while (l != &evaluations_active) {
        EvaluationRequest * req = link_active2erl(l);
        Context * ctx = req->ctx;
        int i;

        l = l->next;

        /* Intercept contexts in BP stop groups */
        for (i = 0; i < req->bp_cnt; i++) {
            BreakpointInfo * bp = req->bp_arr[i].bp;
            if (req->bp_arr[i].triggered && bp->stop_group != NULL) {
                char ** ids = bp->stop_group;
                while (*ids) {
                    Context * c = id2ctx(*ids++);
                    if (c != NULL) suspend_debug_context(c);
                }
            }
        }

        req->bp_cnt = 0;
        list_remove(&req->link_active);
        context_unlock(ctx);
    }

    if (list_is_empty(&evaluations_posted)) {
        assert(cache_enter_cnt == 0);
        assert(generation_done != generation_active);
        flush_instructions();
        generation_done = generation_active;
        notify_breakpoints_status();
    }
}

static void done_evaluation(void) {
    assert(cache_enter_cnt > 0);
    cache_enter_cnt--;
    if (cache_enter_cnt == 0) {
        done_all_evaluations();
        if (!list_is_empty(&evaluations_posted)) {
            EvaluationRequest * req = link_posted2erl(evaluations_posted.next);
            post_safe_event(req->ctx, event_replant_breakpoints, (void *)++generation_posted);
        }
    }
}

static void expr_cache_exit(EvaluationArgs * args) {
    cache_exit();
    done_evaluation();
    run_ctrl_unlock();
}

static void plant_at_address_expression(Context * ctx, ContextAddress ip, BreakpointInfo * bp) {
    ContextAddress addr = 0;
    ContextAddress size = 1;
    int error = 0;
    Value v;

    if (evaluate_expression(ctx, STACK_NO_FRAME, ip, bp->address, 1, &v) < 0) error = errno;
    if (!error && value_to_address(&v, &addr) < 0) error = errno;
    if (bp->access_mode & (CTX_BP_ACCESS_DATA_READ | CTX_BP_ACCESS_DATA_WRITE)) {
        if (bp->access_size > 0) {
            size = bp->access_size;
        }
        else {
            size = context_word_size(ctx);
#if ENABLE_Symbols
            {
                Symbol * type = v.type;
                if (type != NULL) {
                    int type_class = 0;
                    Symbol * base_type = NULL;
                    if (!error && get_symbol_type_class(type, &type_class) < 0) error = errno;
                    if (!error && type_class != TYPE_CLASS_POINTER) error = set_errno(ERR_INV_DATA_TYPE, "Pointer expected");
                    if (!error && get_symbol_base_type(type, &base_type) < 0) error = errno;
                    if (!error && base_type != NULL && get_symbol_size(base_type, &size) < 0) error = errno;
                }
            }
#endif
        }
    }
    if (error) address_expression_error(ctx, bp, error);
    else plant_breakpoint(ctx, bp, addr, size);
}

static void evaluate_address_expression(void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    assert(cache_enter_cnt > 0);
    plant_at_address_expression(args->ctx, 0, args->bp);
    expr_cache_exit(args);
}

#if ENABLE_LineNumbers
static void plant_breakpoint_address_iterator(CodeArea * area, void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    if (args->bp->address == NULL) {
        plant_breakpoint(args->ctx, args->bp, area->start_address, 1);
    }
    else {
        plant_at_address_expression(args->ctx, area->start_address, args->bp);
    }
}

static void evaluate_text_location(void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    BreakpointInfo * bp = args->bp;

    assert(cache_enter_cnt > 0);
    if (line_to_address(args->ctx, bp->file, bp->line, bp->column, plant_breakpoint_address_iterator, args) < 0) {
        address_expression_error(args->ctx, bp, errno);
    }
    expr_cache_exit(args);
}
#endif

static int check_context_ids_location(BreakpointInfo * bp, Context * ctx) {
    /* Check context IDs attribute and return 1 if the breakpoint should be planted in 'ctx' */
    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_BREAKPOINT));
    if (bp->ctx != NULL) {
        return context_get_group(bp->ctx, CONTEXT_GROUP_BREAKPOINT) == ctx;
    }
    if (bp->context_ids != NULL) {
        int ok = 0;
        char ** ids = bp->context_ids;
        while (!ok && *ids != NULL) {
            Context * c = id2ctx(*ids++);
            if (c == NULL) continue;
            ok = context_get_group(c, CONTEXT_GROUP_BREAKPOINT) == ctx;
        }
        return ok;
    }
    return 1;
}

static int check_context_ids_condition(BreakpointInfo * bp, Context * ctx) {
    /* Check context IDs attribute and return 1 if the breakpoint should be triggered by 'ctx' */
    assert(context_has_state(ctx));
    if (bp->ctx != NULL) {
        return bp->ctx == ctx;
    }
    if (bp->context_ids != NULL) {
        int ok = 0;
        char ** ids = bp->context_ids;
        while (!ok && *ids != NULL) {
            Context * c = id2ctx(*ids++);
            if (c == NULL) continue;
            ok = c == ctx || c == ctx->parent;
        }
        return ok;
    }
    return 1;
}

static void evaluate_condition(void * x) {
    int i;
    EvaluationArgs * args = (EvaluationArgs *)x;
    Context * ctx = args->ctx;
    EvaluationRequest * req = EXT(ctx)->req;

    assert(req != NULL);
    assert(req->bp_cnt > 0);
    assert(ctx->stopped);
    assert(ctx->stopped_by_bp || ctx->stopped_by_cb);
    assert(cache_enter_cnt > 0);

    for (i = 0; i < req->bp_cnt; i++) {
        BreakpointInfo * bp = req->bp_arr[i].bp;

        if (is_disabled(bp)) continue;
        if (!check_context_ids_condition(bp, ctx)) continue;

        if (bp->condition != NULL) {
            Value v;
            int b = 0;
            if (evaluate_expression(ctx, STACK_TOP_FRAME, 0, bp->condition, 1, &v) < 0 || value_to_boolean(&v, &b) < 0) {
                int no = get_error_code(errno);
                if (no == ERR_CACHE_MISS) continue;
                if (no == ERR_CHANNEL_CLOSED) continue;
                trace(LOG_ALWAYS, "%s: %s", errno_to_str(errno), bp->condition);
                req->bp_arr[i].condition_ok = 1;
            }
            else if (b) {
                req->bp_arr[i].condition_ok = 1;
            }
            continue;
        }

        req->bp_arr[i].condition_ok = 1;
    }

    expr_cache_exit(args);
}

static void evaluate_bp_location(BreakpointInfo * bp, Context * ctx) {
    if (is_disabled(bp)) return;
    if (!check_context_ids_location(bp, ctx)) return;
    if (bp->file != NULL) {
#if ENABLE_LineNumbers
        expr_cache_enter(evaluate_text_location, bp, ctx);
#else
        set_errno(ERR_UNSUPPORTED, "LineNumbers service not available");
        address_expression_error(NULL, bp, errno);
#endif
    }
    else if (bp->address != NULL) {
        expr_cache_enter(evaluate_address_expression, bp, ctx);
    }
    else {
        address_expression_error(NULL, bp, ERR_INV_EXPRESSION);
    }
}

static void event_replant_breakpoints(void * arg) {
    LINK * q;

    assert(!list_is_empty(&evaluations_posted));
    if ((uintptr_t)arg != generation_posted) return;
    if (cache_enter_cnt > 0) return;

    assert(list_is_empty(&evaluations_active));
    cache_enter_cnt++;
    generation_active = generation_posted;
    q = evaluations_posted.next;
    while (q != &evaluations_posted) {
        EvaluationRequest * req = link_posted2erl(q);
        Context * ctx = req->ctx;
        q = q->next;
        list_remove(&req->link_posted);
        list_add_first(&req->link_active, &evaluations_active);
        if (req->location) {
            BreakpointInfo * bp = req->bp;
            req->location = 0;
            req->bp = NULL;
            clear_instruction_refs(ctx, bp);
            if (!ctx->exiting && !ctx->exited && !EXT(ctx)->empty_bp_grp) {
                context_lock(ctx);
                if (bp != NULL) {
                    evaluate_bp_location(bp, ctx);
                }
                else {
                    LINK * l = breakpoints.next;
                    while (l != &breakpoints) {
                        evaluate_bp_location(link_all2bp(l), ctx);
                        l = l->next;
                    }
                }
                context_unlock(ctx);
            }
        }
        if (req->bp_cnt > 0) {
            int i;
            for (i = 0; i < req->bp_cnt; i++) req->bp_arr[i].condition_ok = 0;
            expr_cache_enter(evaluate_condition, NULL, ctx);
        }
    }
    done_evaluation();
}

static int str_equ(char * x, char * y) {
    if (x == y) return 1;
    if (x == NULL) return 0;
    if (y == NULL) return 0;
    return strcmp(x, y) == 0;
}

static char ** str_arr_dup(char ** x) {
    int n = 0;
    int l = 0;
    int i = 0;
    int offs = 0;
    char ** y = NULL;
    if (x == NULL) return NULL;
    while (x[n] != NULL) l += strlen(x[n++]) + 1;
    offs = sizeof(char *) * (n + 1);
    l += offs;
    y = (char **)loc_alloc_zero(l);
    while (i < n) {
        y[i] = strcpy((char *)y + offs, x[i]);
        offs += strlen(x[i++]) + 1;
    }
    assert(offs == l);
    return y;
}

static int str_arr_equ(char ** x, char ** y) {
    if (x == y) return 1;
    if (x == NULL || y == NULL) return 0;
    while (*x != NULL && *y != NULL) {
        if (strcmp(*x++, *y++) != 0) return 0;
    }
    return *x == *y;
}

static void replant_breakpoint(BreakpointInfo * bp) {
    if (bp->client_cnt == 0 && bp->instruction_cnt == 0) {
        free_bp(bp);
        return;
    }
    if (list_is_empty(&context_root)) return;
    if (bp->ctx != NULL) {
        if (!bp->ctx->exited) post_location_evaluation_request(bp->ctx, bp);
    }
    else if (bp->context_ids == NULL || bp->context_ids_prev == NULL) {
        LINK * l = context_root.next;
        while (l != &context_root) {
            Context * ctx = ctxl2ctxp(l);
            l = l->next;
            if (ctx->exited) continue;
            post_location_evaluation_request(ctx, bp);
        }
        bp->context_ids_prev = str_arr_dup(bp->context_ids);
    }
    else {
        char ** ids = bp->context_ids;
        while (*ids != NULL) {
            Context * ctx = id2ctx(*ids++);
            if (ctx == NULL) continue;
            if (ctx->exited) continue;
            post_location_evaluation_request(ctx, bp);
        }
        if (!str_arr_equ(bp->context_ids, bp->context_ids_prev)) {
            ids = bp->context_ids_prev;
            while (*ids != NULL) {
                Context * ctx = id2ctx(*ids++);
                if (ctx == NULL) continue;
                if (ctx->exited) continue;
                post_location_evaluation_request(ctx, bp);
            }
            bp->context_ids_prev = str_arr_dup(bp->context_ids);
        }
    }
}

static int copy_breakpoint_info(BreakpointInfo * dst, BreakpointInfo * src) {
    int res = 0;

    if (strcmp(dst->id, src->id) != 0) {
        strcpy(dst->id, src->id);
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

    if (!str_equ(dst->type, src->type)) {
        loc_free(dst->type);
        dst->type = src->type;
        res = 1;
    }
    else {
        loc_free(src->type);
    }
    src->type = NULL;

    if (dst->access_mode != src->access_mode) {
        dst->access_mode = src->access_mode;
        res = 1;
    }

    if (dst->access_size != src->access_size) {
        dst->access_size = src->access_size;
        res = 1;
    }

    if (!str_equ(dst->condition, src->condition)) {
        loc_free(dst->condition);
        dst->condition = src->condition;
        res = 1;
    }
    else {
        loc_free(src->condition);
    }
    src->condition = NULL;

    if (!str_arr_equ(dst->context_ids, src->context_ids)) {
        loc_free(dst->context_ids);
        dst->context_ids = src->context_ids;
        res = 1;
    }
    else {
        loc_free(src->context_ids);
    }
    src->context_ids = NULL;

    if (!str_arr_equ(dst->stop_group, src->stop_group)) {
        loc_free(dst->stop_group);
        dst->stop_group = src->stop_group;
        res = 1;
    }
    else {
        loc_free(src->stop_group);
    }
    src->stop_group = NULL;

    if (!str_equ(dst->file, src->file)) {
        loc_free(dst->file);
        dst->file = src->file;
        res = 1;
    }
    else {
        loc_free(src->file);
    }
    src->file = NULL;

    if (dst->line != src->line) {
        dst->line = src->line;
        res = 1;
    }

    if (dst->column != src->column) {
        dst->column = src->column;
        res = 1;
    }

    if (dst->ignore_count != src->ignore_count) {
        dst->ignore_count = src->ignore_count;
        res = 1;
    }

    if (dst->enabled != src->enabled) {
        dst->enabled = src->enabled;
        res = 1;
    }

    if (dst->unsupported != src->unsupported) {
        while (dst->unsupported != NULL) {
            BreakpointAttribute * u = dst->unsupported;
            dst->unsupported = u->next;
            loc_free(u->name);
            loc_free(u->value);
            loc_free(u);
        }
        dst->unsupported = src->unsupported;
        res = 1;
    }
    src->unsupported = NULL;

    if (res) dst->status_changed = 1;

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

static BreakpointRef * find_breakpoint_ref(BreakpointInfo * bp, Channel * channel) {
    LINK * l;
    if (bp == NULL) return NULL;
    l = bp->link_clients.next;
    while (l != &bp->link_clients) {
        BreakpointRef * br = link_bp2br(l);
        assert(br->bp == bp);
        if (br->channel == channel) return br;
        l = l->next;
    }
    return NULL;
}

static void read_breakpoint_properties(InputStream * inp, BreakpointInfo * bp) {
    memset(bp, 0, sizeof(BreakpointInfo));
    if (read_stream(inp) != '{') exception(ERR_JSON_SYNTAX);
    if (peek_stream(inp) == '}') {
        read_stream(inp);
    }
    else {
        for (;;) {
            int ch;
            char name[256];
            json_read_string(inp, name, sizeof(name));
            if (read_stream(inp) != ':') exception(ERR_JSON_SYNTAX);
            if (strcmp(name, "ID") == 0) {
                json_read_string(inp, bp->id, sizeof(bp->id));
            }
            else if (strcmp(name, "Location") == 0) {
                bp->address = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "Type") == 0) {
                bp->type = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "AccessMode") == 0) {
                bp->access_mode = json_read_long(inp);
            }
            else if (strcmp(name, "Size") == 0) {
                bp->access_size = json_read_long(inp);
            }
            else if (strcmp(name, "Condition") == 0) {
                bp->condition = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "ContextIds") == 0) {
                bp->context_ids = json_read_alloc_string_array(inp, NULL);
            }
            else if (strcmp(name, "StopGroup") == 0) {
                bp->stop_group = json_read_alloc_string_array(inp, NULL);
            }
            else if (strcmp(name, "File") == 0) {
                bp->file = json_read_alloc_string(inp);
            }
            else if (strcmp(name, "Line") == 0) {
                bp->line = json_read_long(inp);
            }
            else if (strcmp(name, "Column") == 0) {
                bp->column = json_read_long(inp);
            }
            else if (strcmp(name, "IgnoreCount") == 0) {
                bp->ignore_count = json_read_long(inp);
            }
            else if (strcmp(name, "Enabled") == 0) {
                bp->enabled = json_read_boolean(inp);
            }
            else {
                BreakpointAttribute * u = (BreakpointAttribute *)loc_alloc(sizeof(BreakpointAttribute));
                u->name = loc_strdup(name);
                u->value = json_read_object(inp);
                u->next = bp->unsupported;
                bp->unsupported = u;
            }
            ch = read_stream(inp);
            if (ch == ',') continue;
            if (ch == '}') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
}

static void write_breakpoint_properties(OutputStream * out, BreakpointInfo * bp) {
    BreakpointAttribute * u = bp->unsupported;

    write_stream(out, '{');

    assert(*bp->id);
    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, bp->id);

    if (bp->address != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Location");
        write_stream(out, ':');
        json_write_string(out, bp->address);
    }

    if (bp->type != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Type");
        write_stream(out, ':');
        json_write_string(out, bp->type);
    }

    if (bp->access_mode != 0) {
        write_stream(out, ',');
        json_write_string(out, "AccessMode");
        write_stream(out, ':');
        json_write_long(out, bp->access_mode);
    }

    if (bp->access_size != 0) {
        write_stream(out, ',');
        json_write_string(out, "Size");
        write_stream(out, ':');
        json_write_long(out, bp->access_size);
    }

    if (bp->condition != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Condition");
        write_stream(out, ':');
        json_write_string(out, bp->condition);
    }

    if (bp->context_ids != NULL) {
        char ** ids = bp->context_ids;
        write_stream(out, ',');
        json_write_string(out, "ContextIds");
        write_stream(out, ':');
        write_stream(out, '[');
        while (*ids != NULL) {
            if (ids != bp->context_ids) write_stream(out, ',');
            json_write_string(out, *ids++);
        }
        write_stream(out, ']');
    }

    if (bp->stop_group != NULL) {
        char ** ids = bp->stop_group;
        write_stream(out, ',');
        json_write_string(out, "StopGroup");
        write_stream(out, ':');
        write_stream(out, '[');
        while (*ids != NULL) {
            if (ids != bp->stop_group) write_stream(out, ',');
            json_write_string(out, *ids++);
        }
        write_stream(out, ']');
    }

    if (bp->file != NULL) {
        write_stream(out, ',');
        json_write_string(out, "File");
        write_stream(out, ':');
        json_write_string(out, bp->file);
    }

    if (bp->line > 0) {
        write_stream(out, ',');
        json_write_string(out, "Line");
        write_stream(out, ':');
        json_write_long(out, bp->line);
    }

    if (bp->column > 0) {
        write_stream(out, ',');
        json_write_string(out, "Column");
        write_stream(out, ':');
        json_write_long(out, bp->column);
    }

    if (bp->ignore_count > 0) {
        write_stream(out, ',');
        json_write_string(out, "IgnoreCount");
        write_stream(out, ':');
        json_write_long(out, bp->ignore_count);
    }

    if (bp->enabled) {
        write_stream(out, ',');
        json_write_string(out, "Enabled");
        write_stream(out, ':');
        json_write_boolean(out, bp->enabled);
    }

    while (u != NULL) {
        write_stream(out, ',');
        json_write_string(out, u->name);
        write_stream(out, ':');
        write_string(out, u->value);
        u = u->next;
    }

    write_stream(out, '}');
}

static void send_event_context_added(OutputStream * out, BreakpointInfo * bp) {
    write_stringz(out, "E");
    write_stringz(out, BREAKPOINTS);
    write_stringz(out, "contextAdded");

    write_stream(out, '[');
    write_breakpoint_properties(out, bp);
    write_stream(out, ']');
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void send_event_context_changed(BreakpointInfo * bp) {
    OutputStream * out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, BREAKPOINTS);
    write_stringz(out, "contextChanged");

    write_stream(out, '[');
    write_breakpoint_properties(out, bp);
    write_stream(out, ']');
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void send_event_context_removed(BreakpointInfo * bp) {
    OutputStream * out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, BREAKPOINTS);
    write_stringz(out, "contextRemoved");

    write_stream(out, '[');
    json_write_string(out, bp->id);
    write_stream(out, ']');
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void add_breakpoint(Channel * c, BreakpointInfo * bp) {
    BreakpointRef * r = NULL;
    BreakpointInfo * p = NULL;
    int added = 0;
    int chng = 0;

    assert(*bp->id);
    p = find_breakpoint(bp->id);
    if (p == NULL) {
        int hash = id2bp_hash(bp->id);
        p = (BreakpointInfo *)loc_alloc_zero(sizeof(BreakpointInfo));
        list_init(&p->link_clients);
        list_add_last(&p->link_all, &breakpoints);
        list_add_last(&p->link_id, id2bp + hash);
    }
    chng = copy_breakpoint_info(p, bp);
    if (list_is_empty(&p->link_clients)) added = 1;
    else r = find_breakpoint_ref(p, c);
    if (r == NULL) {
        unsigned inp_hash = (unsigned)(uintptr_t)c / 16 % INP2BR_HASH_SIZE;
        r = (BreakpointRef *)loc_alloc_zero(sizeof(BreakpointRef));
        list_add_last(&r->link_inp, inp2br + inp_hash);
        list_add_last(&r->link_bp, &p->link_clients);
        r->channel = c;
        r->bp = p;
        p->client_cnt++;
    }
    assert(r->bp == p);
    assert(!list_is_empty(&p->link_clients));
    if (chng || added) replant_breakpoint(p);
    if (added) send_event_context_added(&broadcast_group->out, p);
    else if (chng) send_event_context_changed(p);
}

static void remove_ref(Channel * c, BreakpointRef * br) {
    BreakpointInfo * bp = br->bp;
    bp->client_cnt--;
    list_remove(&br->link_inp);
    list_remove(&br->link_bp);
    loc_free(br);
    if (list_is_empty(&bp->link_clients)) {
        send_event_context_removed(bp);
        assert(bp->client_cnt == 0);
        replant_breakpoint(bp);
    }
}

static void delete_breakpoint_refs(Channel * c) {
    unsigned hash = (unsigned)(uintptr_t)c / 16 % INP2BR_HASH_SIZE;
    LINK * l = inp2br[hash].next;
    while (l != &inp2br[hash]) {
        BreakpointRef * br = link_inp2br(l);
        l = l->next;
        if (br->channel == c) remove_ref(c, br);
    }
}

static void command_ini_bps(char * token, Channel * c) {
    int ch;
    LINK * l = NULL;

    /* Delete all breakpoints of this channel */
    delete_breakpoint_refs(c);

    /* Report breakpoints from other channels */
    l = breakpoints.next;
    while (l != &breakpoints) {
        BreakpointInfo * bp = link_all2bp(l);
        l = l->next;
        if (list_is_empty(&bp->link_clients)) continue;
        assert(*bp->id);
        send_event_context_added(&c->out, bp);
        send_event_breakpoint_status(&c->out, bp);
    }

    /* Add breakpoints for this channel */
    ch = read_stream(&c->inp);
    if (ch == 'n') {
        if (read_stream(&c->inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else {
        if (ch != '[') exception(ERR_PROTOCOL);
        if (peek_stream(&c->inp) == ']') {
            read_stream(&c->inp);
        }
        else {
            for (;;) {
                int ch;
                BreakpointInfo bp;
                read_breakpoint_properties(&c->inp, &bp);
                add_breakpoint(c, &bp);
                ch = read_stream(&c->inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_bp_ids(char * token, Channel * c) {
    LINK * l = breakpoints.next;
    int cnt = 0;

    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, '[');

    while (l != &breakpoints) {
        BreakpointInfo * bp = link_all2bp(l);
        l = l->next;
        if (list_is_empty(&bp->link_clients)) continue;
        assert(*bp->id);
        if (cnt > 0) write_stream(&c->out, ',');
        json_write_string(&c->out, bp->id);
        cnt++;
    }

    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_properties(char * token, Channel * c) {
    char id[256];
    BreakpointInfo * bp = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    bp = find_breakpoint(id);
    if (bp == NULL || list_is_empty(&bp->link_clients)) err = ERR_INV_CONTEXT;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_breakpoint_properties(&c->out, bp);
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_status(char * token, Channel * c) {
    char id[256];
    BreakpointInfo * bp = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    bp = find_breakpoint(id);
    if (bp == NULL || list_is_empty(&bp->link_clients)) err = ERR_INV_CONTEXT;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_breakpoint_status(&c->out, bp);
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_bp_add(char * token, Channel * c) {
    BreakpointInfo bp;
    read_breakpoint_properties(&c->inp, &bp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    add_breakpoint(c, &bp);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_bp_change(char * token, Channel * c) {
    BreakpointInfo bp;
    read_breakpoint_properties(&c->inp, &bp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    add_breakpoint(c, &bp);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_bp_enable(char * token, Channel * c) {
    int ch = read_stream(&c->inp);
    if (ch == 'n') {
        if (read_stream(&c->inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else {
        if (ch != '[') exception(ERR_PROTOCOL);
        if (peek_stream(&c->inp) == ']') {
            read_stream(&c->inp);
        }
        else {
            for (;;) {
                int ch;
                char id[256];
                BreakpointInfo * bp;
                json_read_string(&c->inp, id, sizeof(id));
                bp = find_breakpoint(id);
                if (bp != NULL && !list_is_empty(&bp->link_clients) && !bp->enabled) {
                    bp->enabled = 1;
                    bp->hit_count = 0;
                    replant_breakpoint(bp);
                    send_event_context_changed(bp);
                }
                ch = read_stream(&c->inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_bp_disable(char * token, Channel * c) {
    int ch = read_stream(&c->inp);
    if (ch == 'n') {
        if (read_stream(&c->inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else {
        if (ch != '[') exception(ERR_PROTOCOL);
        if (peek_stream(&c->inp) == ']') {
            read_stream(&c->inp);
        }
        else {
            for (;;) {
                int ch;
                char id[256];
                BreakpointInfo * bp;
                json_read_string(&c->inp, id, sizeof(id));
                bp = find_breakpoint(id);
                if (bp != NULL && !list_is_empty(&bp->link_clients) && bp->enabled) {
                    bp->enabled = 0;
                    replant_breakpoint(bp);
                    send_event_context_changed(bp);
                }
                ch = read_stream(&c->inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_bp_remove(char * token, Channel * c) {
    int ch = read_stream(&c->inp);
    if (ch == 'n') {
        if (read_stream(&c->inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else {
        if (ch != '[') exception(ERR_PROTOCOL);
        if (peek_stream(&c->inp) == ']') {
            read_stream(&c->inp);
        }
        else {
            for (;;) {
                int ch;
                char id[256];
                BreakpointRef * br;
                json_read_string(&c->inp, id, sizeof(id));
                br = find_breakpoint_ref(find_breakpoint(id), c);
                if (br != NULL) remove_ref(c, br);
                ch = read_stream(&c->inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_capabilities(char * token, Channel * c) {
    char id[256];
    Context * ctx;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);

    write_stream(&c->out, '{');
    json_write_string(&c->out, "ID");
    write_stream(&c->out, ':');
    json_write_string(&c->out, id);
    write_stream(&c->out, ',');
    json_write_string(&c->out, "Location");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, 1);
    write_stream(&c->out, ',');
    json_write_string(&c->out, "FileLine");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, ENABLE_LineNumbers);
    write_stream(&c->out, ',');
    json_write_string(&c->out, "IgnoreCount");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, 1);
    write_stream(&c->out, ',');
    json_write_string(&c->out, "Condition");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, 1);
    if (ctx != NULL) {
        int md = CTX_BP_ACCESS_INSTRUCTION;
        md |= context_get_supported_bp_access_types(ctx);
        write_stream(&c->out, ',');
        json_write_string(&c->out, "AccessMode");
        write_stream(&c->out, ':');
        json_write_long(&c->out, md);
    }
    write_stream(&c->out, ',');
    json_write_string(&c->out, "ContextIds");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, 1);
    write_stream(&c->out, ',');
    json_write_string(&c->out, "StopGroup");
    write_stream(&c->out, ':');
    json_write_boolean(&c->out, 1);
    write_stream(&c->out, '}');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

int is_breakpoint_address(Context * ctx, ContextAddress address) {
    Context * mem = NULL;
    ContextAddress mem_addr = 0;
    BreakInstruction * bi = NULL;
    if (context_get_canonical_addr(ctx, address, &mem, &mem_addr, NULL, NULL) < 0) return 0;
    bi = find_instruction(mem, mem_addr);
    return bi != NULL && bi->planted;
}

void evaluate_breakpoint(Context * ctx) {
    int i;
    int bp_cnt = 0;
    int need_to_post = 0;
    Context * mem = NULL;
    ContextAddress mem_addr = 0;
    BreakInstruction * bi = NULL;
    EvaluationRequest * req = NULL;

    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(ctx->stopped_by_bp || ctx->stopped_by_cb);
    assert(ctx->exited == 0);
    assert(EXT(ctx)->bp_ids == NULL);

    if (ctx->stopped_by_bp) {
        if (context_get_canonical_addr(ctx, get_regs_PC(ctx), &mem, &mem_addr, NULL, NULL) < 0) return;
        bi = find_instruction(mem, mem_addr);
        if (bi == NULL || !bi->planted || bi->ref_cnt == 0) return;

        assert(bi->valid);
        bp_cnt = bi->ref_cnt;
        req = create_evaluation_request(ctx, bp_cnt);
        for (i = 0; i < bp_cnt; i++) {
            BreakpointInfo * bp = bi->refs[i].bp;
            assert(bp->instruction_cnt);
            assert(bp->unsupported == NULL);
            assert(bp->error == NULL);
            req->bp_arr[i].bp = bp;

            if (need_to_post) continue;
            if (is_disabled(bp)) continue;
            if (bp->condition != NULL || bp->stop_group != NULL) {
                need_to_post = 1;
                continue;
            }
            if (!check_context_ids_condition(bp, ctx)) continue;
            req->bp_arr[i].condition_ok = 1;
        }
    }
    if (ctx->stopped_by_cb) {
        int j;
        assert(ctx->stopped_by_cb[0] != NULL);
        for (j = 0; ctx->stopped_by_cb[j]; j++) {
            int k = 0;
            bi = (BreakInstruction *)((char *)ctx->stopped_by_cb[j] - offsetof(BreakInstruction, cb));
            assert(bi->planted);
            bp_cnt += bi->ref_cnt;
            if (req == NULL) {
                req = create_evaluation_request(ctx, bp_cnt);
            }
            else {
                k = req->bp_cnt;
                if (req->bp_max < bp_cnt) {
                    req->bp_max = bp_cnt;
                    req->bp_arr = (ConditionEvaluationRequest *)loc_realloc(req->bp_arr, sizeof(ConditionEvaluationRequest) * req->bp_max);
                }
                if (bp_cnt > k) {
                    memset(req->bp_arr + k, 0, sizeof(ConditionEvaluationRequest) * bp_cnt - k);
                    req->bp_cnt = bp_cnt;
                }
            }
            for (i = 0; i < bi->ref_cnt; i++) {
                BreakpointInfo * bp = bi->refs[i].bp;
                assert(bp->instruction_cnt);
                assert(bp->unsupported == NULL);
                assert(bp->error == NULL);
                assert(bi->refs[i].cnt > 0);

                req->bp_arr[k + i].bp = bp;

                if (need_to_post) continue;
                if (is_disabled(bp)) continue;
                if (bp->condition != NULL || bp->stop_group != NULL) {
                    need_to_post = 1;
                    continue;
                }
                if (!check_context_ids_condition(bp, ctx)) continue;
                req->bp_arr[k + i].condition_ok = 1;
            }
        }
    }

    if (need_to_post) {
        post_evaluation_request(req);
    }
    else {
        done_condition_evaluation(req);
        req->bp_cnt = 0;
    }
}

char ** get_context_breakpoint_ids(Context * ctx) {
    return EXT(ctx)->bp_ids;
}

static void safe_skip_breakpoint(void * arg);

static void safe_restore_breakpoint(void * arg) {
    Context * ctx = (Context *)arg;
    ContextExtensionBP * ext = EXT(ctx);
    BreakInstruction * bi = ext->stepping_over_bp;

    assert(bi->stepping_over_bp > 0);
    assert(find_instruction(bi->cb.ctx, bi->cb.address) == bi);
    if (!ctx->exiting && ctx->stopped && !ctx->stopped_by_exception && get_regs_PC(ctx) == bi->cb.address) {
        if (ext->step_over_bp_cnt < 100) {
            ext->step_over_bp_cnt++;
            safe_skip_breakpoint(arg);
            return;
        }
        trace(LOG_ALWAYS, "Skip breakpoint error: wrong PC %#lx", get_regs_PC(ctx));
    }
    ext->stepping_over_bp = NULL;
    ext->step_over_bp_cnt = 0;
    bi->stepping_over_bp--;
    if (bi->stepping_over_bp == 0) {
        if (generation_done != generation_posted) {
            bi->valid = 0;
        }
        else if (!ctx->exited && bi->ref_cnt > 0 && !bi->planted) {
            plant_instruction(bi);
        }
    }
    context_unlock(ctx);
}

static void safe_skip_breakpoint(void * arg) {
    Context * ctx = (Context *)arg;
    ContextExtensionBP * ext = EXT(ctx);
    BreakInstruction * bi = ext->stepping_over_bp;
    int error = 0;

    assert(bi != NULL);
    assert(bi->stepping_over_bp > 0);
    assert(find_instruction(bi->cb.ctx, bi->cb.address) == bi);

    post_safe_event(ctx, safe_restore_breakpoint, ctx);

    if (ctx->exited || ctx->exiting) return;

    assert(ctx->stopped);
    assert(bi->cb.address == get_regs_PC(ctx));

    if (bi->planted) remove_instruction(bi);
    if (bi->planting_error) error = set_error_report_errno(bi->planting_error);
    if (error == 0 && safe_context_single_step(ctx) < 0) error = errno;
    if (error) {
        error = set_errno(error, "Cannot step over breakpoint");
        ctx->signal = 0;
        ctx->stopped = 1;
        ctx->stopped_by_bp = 0;
        ctx->stopped_by_cb = NULL;
        ctx->stopped_by_exception = 1;
        ctx->pending_intercept = 1;
        loc_free(ctx->exception_description);
        ctx->exception_description = loc_strdup(errno_to_str(error));
        send_context_changed_event(ctx);
    }
}

/*
 * When a context is stopped by breakpoint, it is necessary to disable
 * the breakpoint temporarily before the context can be resumed.
 * This function function removes break instruction, then does single step
 * over breakpoint location, then restores break intruction.
 * Return: 0 if it is OK to resume context from current state,
 * return 1 if context needs to step over a breakpoint.
 */
int skip_breakpoint(Context * ctx, int single_step) {
    ContextExtensionBP * ext = EXT(ctx);
    Context * mem = NULL;
    ContextAddress mem_addr = 0;
    BreakInstruction * bi;

    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(single_step || ext->stepping_over_bp == NULL);

    if (ext->stepping_over_bp != NULL) return 0;
    if (ctx->exited || ctx->exiting || !ctx->stopped_by_bp) return 0;

    if (context_get_canonical_addr(ctx, get_regs_PC(ctx), &mem, &mem_addr, NULL, NULL) < 0) return -1;
    bi = find_instruction(mem, mem_addr);
    if (bi == NULL || bi->planting_error) return 0;
    bi->stepping_over_bp++;
    ext->stepping_over_bp = bi;
    ext->step_over_bp_cnt = 1;
    assert(bi->stepping_over_bp > 0);
    context_lock(ctx);
    post_safe_event(ctx, safe_skip_breakpoint, ctx);
    return 1;
}

BreakpointInfo * create_eventpoint(const char * location, Context * ctx, EventPointCallBack * callback, void * callback_args) {
    BreakpointInfo * bp = (BreakpointInfo *)loc_alloc_zero(sizeof(BreakpointInfo));
    bp->client_cnt = 1;
    bp->enabled = 1;
    bp->address = loc_strdup(location);
    bp->event_callback = callback;
    bp->event_callback_args = callback_args;
    if (ctx != NULL) context_lock(bp->ctx = ctx);
    list_init(&bp->link_clients);
    assert(breakpoints.next != NULL);
    list_add_last(&bp->link_all, &breakpoints);
    replant_breakpoint(bp);
    return bp;
}

void destroy_eventpoint(BreakpointInfo * bp) {
    assert(bp->client_cnt == 1);
    assert(list_is_empty(&bp->link_clients));
    bp->client_cnt = 0;
    replant_breakpoint(bp);
}

static void event_context_created_or_exited(Context * ctx, void * args) {
    post_location_evaluation_request(ctx, NULL);
}

static void event_context_changed(Context * ctx, void * args) {
    if (ctx->mem_access && context_get_group(ctx, CONTEXT_GROUP_PROCESS) == ctx) {
        /* If the context is a memory space, we need to update
         * breakpoints on all members of the group */
        LINK * l = context_root.next;
        while (l != &context_root) {
            Context * x = ctxl2ctxp(l);
            l = l->next;
            if (x->exited) continue;
            if (context_get_group(x, CONTEXT_GROUP_PROCESS) != ctx) continue;
            post_location_evaluation_request(x, NULL);
        }
    }
    else {
        post_location_evaluation_request(ctx, NULL);
    }
}

static void event_context_started(Context * ctx, void * args) {
    ContextExtensionBP * ext = EXT(ctx);
    if (ext->bp_ids != NULL) {
        loc_free(ext->bp_ids);
        ext->bp_ids = NULL;
    }
}

static void event_context_disposed(Context * ctx, void * args) {
    ContextExtensionBP * ext = EXT(ctx);
    EvaluationRequest * req = ext->req;
    if (req != NULL) {
        loc_free(req->bp_arr);
        loc_free(req);
        ext->req = NULL;
    }
    if (ext->bp_ids != NULL) {
        loc_free(ext->bp_ids);
        ext->bp_ids = NULL;
    }
}

static void event_code_unmapped(Context * ctx, ContextAddress addr, ContextAddress size, void * args) {
    /* Unmapping a code section unplants all breakpoint instructions in that section as side effect.
     * This function udates service data structure to reflect that.
     */
    int cnt = 0;
    while (size > 0) {
        size_t sz = size;
        LINK * l = instructions.next;
        Context * mem = NULL;
        ContextAddress mem_addr = 0;
        ContextAddress mem_base = 0;
        ContextAddress mem_size = 0;
        if (context_get_canonical_addr(ctx, addr, &mem, &mem_addr, &mem_base, &mem_size) < 0) break;
        if (mem_base + mem_size - mem_addr < sz) sz = mem_base + mem_size - mem_addr;
        while (l != &instructions) {
            int i;
            BreakInstruction * bi = link_all2bi(l);
            l = l->next;
            if (bi->cb.ctx != mem) continue;
            if (!bi->planted) continue;
            if (bi->cb.address < mem_addr || bi->cb.address >= mem_addr + sz) continue;
            for (i = 0; i < bi->ref_cnt; i++) {
                bi->refs[i].bp->status_changed = 1;
                cnt++;
            }
            bi->planted = 0;
        }
        addr += sz;
        size -= sz;
    }
    if (cnt > 0 && generation_done == generation_active) notify_breakpoints_status();
}

static void channel_close_listener(Channel * c) {
    delete_breakpoint_refs(c);
}

void ini_breakpoints_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    int i;
    broadcast_group = bcg;

    {
        static ContextEventListener listener = {
            event_context_created_or_exited,
            event_context_created_or_exited,
            NULL,
            event_context_started,
            event_context_changed,
            event_context_disposed
        };
        add_context_event_listener(&listener, NULL);
    }
    {
        static MemoryMapEventListener listener = {
            event_context_changed,
            event_code_unmapped,
            event_context_changed
        };
        add_memory_map_event_listener(&listener, NULL);
    }
    list_init(&breakpoints);
    list_init(&instructions);
    list_init(&evaluations_posted);
    list_init(&evaluations_active);
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
    add_command_handler(proto, BREAKPOINTS, "getIDs", command_get_bp_ids);
    add_command_handler(proto, BREAKPOINTS, "getProperties", command_get_properties);
    add_command_handler(proto, BREAKPOINTS, "getStatus", command_get_status);
    add_command_handler(proto, BREAKPOINTS, "getCapabilities", command_get_capabilities);
    context_extension_offset = context_extension(sizeof(ContextExtensionBP));
}

#endif /* SERVICE_Breakpoints */
