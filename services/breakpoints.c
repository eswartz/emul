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

#if defined(_WRS_KERNEL)
#  include <private/vxdbgLibP.h>
#  include <system/VxWorks/context-vxworks.h>
#endif

typedef struct BreakpointClient BreakpointClient;
typedef struct BreakpointAttribute BreakpointAttribute;
typedef struct BreakInstruction BreakInstruction;
typedef struct EvaluationArgs EvaluationArgs;
typedef struct EvaluationRequest EvaluationRequest;
typedef struct ConditionEvaluationRequest ConditionEvaluationRequest;
typedef struct ContextExtensionBP ContextExtensionBP;

struct BreakpointClient {
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
    LINK link_all;
    LINK link_id;
    LINK link_clients;
    char id[256];
    int enabled;
    int client_cnt;
    int instruction_cnt;
    ErrorReport * error;
    char * address;
    char * condition;
    char ** context_ids;
    char ** context_ids_prev;
    char ** stop_group;
    char * file;
    int line;
    int column;
    int ignore_count;
    int hit_count;
    BreakpointAttribute * unsupported;

    EventPointCallBack * event_callback;
    void * event_callback_args;

    /* Last status report contents: */
    ErrorReport * status_error;
    int status_unsupported;
    int status_planted;
};

struct BreakInstruction {
    LINK link_all;
    LINK link_adr;
    Context * ctx;
    ContextAddress address;
#if defined(_WRS_KERNEL)
    VXDBG_CTX vxdbg_ctx;
    VXDBG_BP_ID vxdbg_id;
#else
    char saved_code[16];
    size_t saved_size;
#endif
    ErrorReport * error;
    int stepping_over_bp;
    BreakpointInfo ** refs;
    int ref_size;
    int ref_cnt;
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
    LINK link_posted;
    LINK link_active;
    int location;
    int bp_cnt;
    int bp_max;
    ConditionEvaluationRequest * bp_arr;
};

struct ContextExtensionBP {
    int                 bp_eval_started;
    BreakInstruction *  stepping_over_bp;   /* if not NULL context is stepping over a breakpoint instruction */
    char **             bp_ids;             /* if stopped by breakpoint, contains NULL-terminated list of breakpoint IDs */
    EvaluationRequest * req;
};

static const char * BREAKPOINTS = "Breakpoints";

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionBP *)((char *)(ctx) + context_extension_offset))

#define is_readable(ctx) (!(ctx)->exited && !(ctx)->exiting && ((ctx)->stopped || !context_has_state(ctx)))
#define is_disabled(bp) (bp->enabled == 0 || bp->client_cnt == 0 || bp->unsupported != NULL)

#define ADDR2INSTR_HASH_SIZE 1023
#define addr2instr_hash(addr) ((unsigned)((addr) + ((addr) >> 8)) % ADDR2INSTR_HASH_SIZE)

#define link_all2bi(A)  ((BreakInstruction *)((char *)(A) - offsetof(BreakInstruction, link_all)))
#define link_adr2bi(A)  ((BreakInstruction *)((char *)(A) - offsetof(BreakInstruction, link_adr)))

#define ID2BP_HASH_SIZE 1023

#define link_all2bp(A)  ((BreakpointInfo *)((char *)(A) - offsetof(BreakpointInfo, link_all)))
#define link_id2bp(A)   ((BreakpointInfo *)((char *)(A) - offsetof(BreakpointInfo, link_id)))

#define INP2BR_HASH_SIZE 127

#define link_inp2br(A)  ((BreakpointClient *)((char *)(A) - offsetof(BreakpointClient, link_inp)))
#define link_bp2br(A)   ((BreakpointClient *)((char *)(A) - offsetof(BreakpointClient, link_bp)))

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

static int select_valid_context(Context ** ctx) {
    Context * x = *ctx;
    if (!is_readable(x)) {
        LINK * qp = context_root.next;
        while (qp != &context_root) {
            Context * y = ctxl2ctxp(qp);
            if (y->mem == x->mem && is_readable(y)) {
                assert(x != y);
                context_unlock(x);
                context_lock(y);
                *ctx = y;
                return 0;
            }
            qp = qp->next;
        }
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    return 0;
}

static void plant_instruction(BreakInstruction * bi) {
    int i;
    assert(!bi->stepping_over_bp);
    assert(!bi->planted);
    if (bi->error) {
        release_error_report(bi->error);
        bi->error = NULL;
    }
    assert(is_all_stopped(bi->ctx->mem));
#if defined(_WRS_KERNEL)
    bi->vxdbg_ctx.ctxId = 0;
    bi->vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    if (vxdbgBpAdd(vxdbg_clnt_id,
            &bi->vxdbg_ctx, 0, BP_ACTION_STOP | BP_ACTION_NOTIFY,
            0, 0, (INSTR *)bi->address, 0, 0, &bi->vxdbg_id) != OK) {
        bi->error = get_error_report(errno);
        assert(bi->error != NULL);
    }
#else
    if (select_valid_context(&bi->ctx) < 0) {
        bi->error = get_error_report(errno);
    }
    else {
        uint8_t * break_inst = get_break_instruction(bi->ctx, &bi->saved_size);
        assert(sizeof(bi->saved_code) >= bi->saved_size);
        planting_instruction = 1;
        if (context_read_mem(bi->ctx, bi->address, bi->saved_code, bi->saved_size) < 0) {
            bi->error = get_error_report(errno);
        }
        else if (context_write_mem(bi->ctx, bi->address, break_inst, bi->saved_size) < 0) {
            bi->error = get_error_report(errno);
        }
        planting_instruction = 0;
    }
#endif
    bi->planted = bi->error == NULL;
    if (bi->planted) {
        for (i = 0; i < bi->ref_cnt; i++) {
            BreakpointInfo * bp = bi->refs[i];
            bp->instruction_cnt++;
            if (bp->error) {
                release_error_report(bp->error);
                bp->error = NULL;
            }
        }
    }
}

static void remove_instruction(BreakInstruction * bi) {
    int i;
    assert(bi->planted);
    assert(bi->error == NULL);
    assert(is_all_stopped(bi->ctx->mem));
#if defined(_WRS_KERNEL)
    {
        VXDBG_BP_DEL_INFO info;
        memset(&info, 0, sizeof(info));
        info.pClnt = vxdbg_clnt_id;
        info.type = BP_BY_ID_DELETE;
        info.info.id.bpId = bi->vxdbg_id;
        if (vxdbgBpDelete(info) != OK) {
            bi->error = get_error_report(errno);
            assert(bi->error != NULL);
        }
    }
#else
    if (select_valid_context(&bi->ctx) == 0) {
        planting_instruction = 1;
        if (context_write_mem(bi->ctx, bi->address, bi->saved_code, bi->saved_size) < 0) {
            bi->error = get_error_report(errno);
        }
        planting_instruction = 0;
    }
#endif
    bi->planted = 0;
    for (i = 0; i < bi->ref_cnt; i++) {
        bi->refs[i]->instruction_cnt--;
    }
}

static BreakInstruction * add_instruction(Context * ctx, ContextAddress address) {
    int hash = addr2instr_hash(address);
    BreakInstruction * bi = (BreakInstruction *)loc_alloc_zero(sizeof(BreakInstruction));
    list_add_last(&bi->link_all, &instructions);
    list_add_last(&bi->link_adr, addr2instr + hash);
    context_lock(ctx);
    bi->ctx = ctx;
    bi->address = address;
    return bi;
}

static BreakInstruction * find_instruction(Context * ctx, ContextAddress address) {
    int hash = addr2instr_hash(address);
    LINK * l = addr2instr[hash].next;
    while (l != addr2instr + hash) {
        BreakInstruction * bi = link_adr2bi(l);
        if (bi->ctx->mem == ctx->mem && bi->address == address) return bi;
        l = l->next;
    }
    return NULL;
}

static void clear_instruction_refs(Context * mem) {
    int i;
    LINK * l = instructions.next;
    while (l != &instructions) {
        BreakInstruction * bi = link_all2bi(l);
        if (bi->ctx->mem == mem) {
            if (bi->planted) {
                for (i = 0; i < bi->ref_cnt; i++) {
                    bi->refs[i]->instruction_cnt--;
                }
            }
            bi->ref_cnt = 0;
        }
        l = l->next;
    }
}

static void flush_instructions(void) {
    LINK * l = instructions.next;
    while (l != &instructions) {
        BreakInstruction * bi = link_all2bi(l);
        l = l->next;
        if (bi->stepping_over_bp) continue;
        if (bi->ref_cnt == 0) {
            list_remove(&bi->link_all);
            list_remove(&bi->link_adr);
            if (bi->planted) remove_instruction(bi);
            context_unlock(bi->ctx);
            release_error_report(bi->error);
            loc_free(bi->refs);
            loc_free(bi);
        }
        else if (!bi->planted) {
            plant_instruction(bi);
        }
    }
}

void check_breakpoints_on_memory_read(Context * ctx, ContextAddress address, void * p, size_t size) {
#if !defined(_WRS_KERNEL)
    if (!planting_instruction) {
        size_t i;
        char * buf = (char *)p;
        LINK * l = instructions.next;
        while (l != &instructions) {
            BreakInstruction * bi = link_all2bi(l);
            l = l->next;
            if (!bi->planted) continue;
            if (bi->ctx->mem != ctx->mem) continue;
            if (bi->address + bi->saved_size <= address) continue;
            if (bi->address >= address + size) continue;
            for (i = 0; i < bi->saved_size; i++) {
                if (bi->address + i < address) continue;
                if (bi->address + i >= address + size) continue;
                buf[bi->address + i - address] = bi->saved_code[i];
            }
        }
    }
#endif
}

void check_breakpoints_on_memory_write(Context * ctx, ContextAddress address, void * p, size_t size) {
#if !defined(_WRS_KERNEL)
    if (!planting_instruction) {
        char * buf = (char *)p;
        LINK * l = instructions.next;
        while (l != &instructions) {
            BreakInstruction * bi = link_all2bi(l);
            l = l->next;
            if (!bi->planted) continue;
            if (bi->ctx->mem != ctx->mem) continue;
            if (bi->address + bi->saved_size <= address) continue;
            if (bi->address >= address + size) continue;
            {
                size_t i;
                uint8_t * break_inst = get_break_instruction(bi->ctx, &i);
                assert(i == bi->saved_size);
                for (i = 0; i < bi->saved_size; i++) {
                    if (bi->address + i < address) continue;
                    if (bi->address + i >= address + size) continue;
                    bi->saved_code[i] = buf[bi->address + i - address];
                    buf[bi->address + i - address] = break_inst[i];
                }
            }
        }
    }
#endif
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
            while (i < bi->ref_cnt && bi->refs[i] != bp) i++;
            if (i >= bi->ref_cnt) continue;
            if (cnt > 0) write_stream(out, ',');
            write_stream(out, '{');
            json_write_string(out, "LocationContext");
            write_stream(out, ':');
            json_write_string(out, bi->ctx->id);
            write_stream(out, ',');
            if (bi->error != NULL) {
                json_write_string(out, "Error");
                write_stream(out, ':');
                json_write_string(out, errno_to_str(set_error_report_errno(bi->error)));
            }
            else {
                json_write_string(out, "Address");
                write_stream(out, ':');
                json_write_ulong(out, bi->address);
            }
            write_stream(out, '}');
            cnt++;
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

static void address_expression_error(BreakpointInfo * bp, int error) {
    /* TODO: per-context address expression error report */
    assert(error != 0);
    assert(bp->instruction_cnt == 0 || bp->error == NULL);
    if (bp->instruction_cnt == 0) {
        release_error_report(bp->error);
        bp->error = get_error_report(error);
        assert(bp->error != NULL);
    }
}

static void plant_breakpoint_at_address(BreakpointInfo * bp, Context * ctx, ContextAddress address) {
    BreakInstruction * bi = NULL;
    if (address == 0) return;
    bi = find_instruction(ctx, address);
    if (bi == NULL) {
        bi = add_instruction(ctx, address);
    }
    else {
        int i = 0;
        while (i < bi->ref_cnt) {
            if (bi->refs[i++] == bp) return;
        }
    }
    if (bi->ref_cnt >= bi->ref_size) {
        bi->ref_size = bi->ref_size == 0 ? 8 : bi->ref_size * 2;
        bi->refs = (BreakpointInfo **)loc_realloc(bi->refs, sizeof(BreakpointInfo *) * bi->ref_size);
    }
    bi->refs[bi->ref_cnt++] = bp;
    if (bi->planted) {
        bp->instruction_cnt++;
        if (bp->error) {
            release_error_report(bp->error);
            bp->error = NULL;
        }
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
    if (bp_cnt > 0) {
        assert(req->bp_cnt == 0);
        assert(req->bp_max >= bp_cnt);
        memset(req->bp_arr, 0, sizeof(ConditionEvaluationRequest) * bp_cnt);
        req->bp_cnt = bp_cnt;
    }
    return req;
}

static EvaluationRequest * post_evaluation_request(EvaluationRequest * req) {
    if (list_is_empty(&req->link_posted)) {
        context_lock(req->ctx);
        list_add_last(&req->link_posted, &evaluations_posted);
        post_safe_event(req->ctx->mem, event_replant_breakpoints, (void *)++generation_posted);
    }
    return req;
}

static void post_location_evaluation_request(Context * ctx) {
    post_evaluation_request(create_evaluation_request(ctx->mem, 0))->location = 1;
}

static void expr_cache_enter(CacheClient * client, BreakpointInfo * bp, Context * ctx) {
    LINK * l = NULL;
    EvaluationArgs args;

    args.bp = bp;
    args.ctx = ctx;

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
    if (&bp->id) list_remove(&bp->link_id);
    release_error_report(bp->error);
    loc_free(bp->address);
    loc_free(bp->context_ids);
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

static void done_locations_evaluation(void) {
    LINK * l = NULL;
    assert(cache_enter_cnt == 0);
    assert(generation_done != generation_active);
    flush_instructions();
    for (l = breakpoints.next; l != &breakpoints;) {
        BreakpointInfo * bp = link_all2bp(l);
        l = l->next;
#ifndef NDEBUG
        {
            /* Verify breakpoints data structure */
            LINK * m = NULL;
            int instruction_cnt = 0;
            for (m = instructions.next; m != &instructions; m = m->next) {
                BreakInstruction * bi = link_all2bi(m);
                assert(bi->ref_cnt <= bi->ref_size);
                assert(bi->ctx->ref_count > 0);
                if (bi->planted) {
                    int i;
                    for (i = 0; i < bi->ref_cnt; i++) {
                        if (bi->refs[i] == bp) instruction_cnt++;
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
                        BreakpointClient * br = link_inp2br(m);
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
        if (*bp->id) {
            if (bp->client_cnt == 0) {
                assert(list_is_empty(&bp->link_clients));
                assert(bp->instruction_cnt == 0);
                free_bp(bp);
            }
            else if (bp->status_unsupported != (bp->unsupported != NULL) ||
                    bp->status_error != bp->error ||
                    bp->status_planted != bp->instruction_cnt) {
                send_event_breakpoint_status(&broadcast_group->out, bp);
                bp->status_unsupported = bp->unsupported != NULL;
                bp->status_error = bp->error;
                bp->status_planted = bp->instruction_cnt;
            }
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
            assert(req->ctx->stopped_by_bp);
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
                    if (c != NULL) {
                        suspend_debug_context(c);
                    }
                }
            }
        }

        req->bp_cnt = 0;
        list_remove(&req->link_active);
        context_unlock(ctx);
    }

    if (list_is_empty(&evaluations_posted)) {
        done_locations_evaluation();
        generation_done = generation_active;
    }
}

static void done_evaluation(void) {
    assert(cache_enter_cnt > 0);
    cache_enter_cnt--;
    if (cache_enter_cnt == 0) {
        done_all_evaluations();
        if (!list_is_empty(&evaluations_posted)) {
            EvaluationRequest * req = link_posted2erl(evaluations_posted.next);
            post_safe_event(req->ctx->mem, event_replant_breakpoints, (void *)++generation_posted);
        }
    }
}

static void expr_cache_exit(EvaluationArgs * args) {
    cache_exit();
    done_evaluation();
    run_ctrl_unlock();
}

static void evaluate_address_expression(void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    BreakpointInfo * bp = args->bp;
    Value v;

    assert(cache_enter_cnt > 0);
    if (select_valid_context(&args->ctx) < 0) {
        address_expression_error(bp, errno);
    }
    else if (evaluate_expression(args->ctx, STACK_NO_FRAME, bp->address, 1, &v) < 0) {
        address_expression_error(bp, errno);
    }
    else if (v.type_class != TYPE_CLASS_INTEGER && v.type_class != TYPE_CLASS_CARDINAL && v.type_class != TYPE_CLASS_POINTER) {
        address_expression_error(bp, ERR_INV_DATA_TYPE);
    }
    else {
        plant_breakpoint_at_address(bp, args->ctx, value_to_address(&v));
    }
    expr_cache_exit(args);
}

#if ENABLE_LineNumbers
static void plant_breakpoint_address_iterator(CodeArea * area, void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    plant_breakpoint_at_address(args->bp, args->ctx, area->start_address);
}

static void evaluate_text_location(void * x) {
    EvaluationArgs * args = (EvaluationArgs *)x;
    BreakpointInfo * bp = args->bp;

    assert(cache_enter_cnt > 0);
    if (select_valid_context(&args->ctx) < 0) {
        address_expression_error(bp, errno);
    }
    else if (line_to_address(args->ctx, bp->file, bp->line, bp->column, plant_breakpoint_address_iterator, args) < 0) {
        address_expression_error(bp, errno);
    }
    expr_cache_exit(args);
}
#endif

static int check_context_ids(BreakpointInfo * bp, Context * ctx, int location) {
    if (bp->context_ids != NULL) {
        int ok = 0;
        char ** ids = bp->context_ids;
        while (!ok && *ids != NULL) {
            Context * c = id2ctx(*ids++);
            if (c == NULL) continue;
            ok = location ? c->mem == ctx->mem : c == ctx || c == ctx->parent;
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
    assert(ctx->stopped_by_bp);
    assert(cache_enter_cnt > 0);

    for (i = 0; i < req->bp_cnt; i++) {
        BreakpointInfo * bp = req->bp_arr[i].bp;

        if (is_disabled(bp)) continue;
        if (!check_context_ids(bp, ctx, 0)) continue;

        if (bp->condition != NULL) {
            Value v;
            if (evaluate_expression(ctx, STACK_TOP_FRAME, bp->condition, 1, &v) < 0) {
                if (get_error_code(errno) == ERR_CACHE_MISS) continue;
                trace(LOG_ALWAYS, "%s: %s", errno_to_str(errno), bp->condition);
                req->bp_arr[i].condition_ok = 1;
            }
            else if (value_to_boolean(&v)) {
                req->bp_arr[i].condition_ok = 1;
            }
            continue;
        }

        req->bp_arr[i].condition_ok = 1;
    }

    expr_cache_exit(args);
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
        q = q->next;
        list_remove(&req->link_posted);
        list_add_first(&req->link_active, &evaluations_active);
        if (req->location) {
            Context * ctx = req->ctx;
            req->location = 0;
            clear_instruction_refs(ctx->mem);
            context_lock(ctx);
            if (select_valid_context(&ctx) == 0) {
                LINK * l = breakpoints.next;
                while (l != &breakpoints) {
                    BreakpointInfo * bp = link_all2bp(l);
                    l = l->next;
                    if (is_disabled(bp)) continue;
                    if (!check_context_ids(bp, ctx, 1)) continue;
                    if (bp->address != NULL) {
                        expr_cache_enter(evaluate_address_expression, bp, ctx);
                    }
                    else if (bp->file != NULL) {
#if ENABLE_LineNumbers
                        expr_cache_enter(evaluate_text_location, bp, ctx);
#else
                        set_errno(ERR_UNSUPPORTED, "LineNumbers service not available");
                        address_expression_error(bp, errno);
#endif
                    }
                    else {
                        address_expression_error(bp, ERR_INV_EXPRESSION);
                    }
                }
            }
            context_unlock(ctx);
        }
        if (req->bp_cnt > 0) {
            int i;
            for (i = 0; i < req->bp_cnt; i++) req->bp_arr[i].condition_ok = 0;
            expr_cache_enter(evaluate_condition, NULL, req->ctx);
        }
    }
    done_evaluation();
}

static void replant_breakpoint(BreakpointInfo * bp) {
    char ** ids = bp->context_ids;
    if (list_is_empty(&context_root)) return;
    if (ids == NULL || ids != bp->context_ids_prev) {
        LINK * l = context_root.next;
        while (l != &context_root) {
            Context * ctx = ctxl2ctxp(l);
            l = l->next;
            if (ctx->exited) continue;
            post_location_evaluation_request(ctx);
        }
        bp->context_ids_prev = ids;
    }
    else {
        while (*ids != NULL) {
            Context * ctx = id2ctx(*ids++);
            if (ctx == NULL) continue;
            if (ctx->exited) continue;
            post_location_evaluation_request(ctx);
        }
    }
}

static int str_equ(char * x, char * y) {
    if (x == y) return 1;
    if (x == NULL) return 0;
    if (y == NULL) return 0;
    return strcmp(x, y) == 0;
}

static int str_arr_equ(char ** x, char ** y) {
    int i = 0;
    if (x == y) return 1;
    if (x == NULL) return 0;
    if (y == NULL) return 0;
    for (;;) {
        if (!str_equ(x[i], y[i])) return 0;
        if (x[i] == NULL) break;
        i++;
    }
    return 1;
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

static BreakpointClient * find_breakpoint_ref(BreakpointInfo * bp, Channel * channel) {
    LINK * l;
    if (bp == NULL) return NULL;
    l = bp->link_clients.next;
    while (l != &bp->link_clients) {
        BreakpointClient * br = link_bp2br(l);
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
    BreakpointClient * r = NULL;
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
    if (list_is_empty(&bp->link_clients)) added = 1;
    else r = find_breakpoint_ref(p, c);
    if (r == NULL) {
        unsigned inp_hash = (unsigned)(uintptr_t)c / 16 % INP2BR_HASH_SIZE;
        r = (BreakpointClient *)loc_alloc_zero(sizeof(BreakpointClient));
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

static void remove_ref(Channel * c, BreakpointClient * br) {
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
        BreakpointClient * br = link_inp2br(l);
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
                BreakpointClient * br;
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

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

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
    BreakInstruction * bi = find_instruction(ctx, address);
    return bi != NULL && bi->planted;
}

void evaluate_breakpoint(Context * ctx) {
    int i;
    int bp_cnt = 0;
    int need_to_post = 0;
    BreakInstruction * bi = find_instruction(ctx, get_regs_PC(ctx));
    EvaluationRequest * req = NULL;

    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(ctx->stopped_by_bp);
    assert(ctx->exiting == 0);
    assert(EXT(ctx)->bp_ids == NULL);

    EXT(ctx)->bp_eval_started = 1;
    if (bi == NULL || !bi->planted || bi->ref_cnt == 0) return;

    bp_cnt = bi->ref_cnt;
    req = create_evaluation_request(ctx, bp_cnt);
    for (i = 0; i < bp_cnt; i++) {
        BreakpointInfo * bp = bi->refs[i];
        assert(bp->instruction_cnt);
        assert(bp->error == NULL);
        req->bp_arr[i].bp = bp;

        if (need_to_post) continue;
        if (is_disabled(bp)) continue;
        if (bp->condition != NULL || bp->stop_group != NULL) {
            need_to_post = 1;
            continue;
        }
        if (!check_context_ids(bp, ctx, 0)) continue;
        req->bp_arr[i].condition_ok = 1;
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

#ifndef _WRS_KERNEL

static void safe_restore_breakpoint(void * arg) {
    Context * ctx = (Context *)arg;
    BreakInstruction * bi = EXT(ctx)->stepping_over_bp;

    assert(bi->stepping_over_bp > 0);
    assert(find_instruction(ctx, bi->address) == bi);
    if (!ctx->exiting && ctx->stopped && get_regs_PC(ctx) == bi->address) {
        trace(LOG_ALWAYS, "Skip breakpoint error: wrong PC %#lx", get_regs_PC(ctx));
    }
    EXT(ctx)->stepping_over_bp = NULL;
    bi->stepping_over_bp--;
    if (!ctx->exited && generation_done == generation_posted &&
            bi->stepping_over_bp == 0 && bi->ref_cnt > 0 && !bi->planted) {
        plant_instruction(bi);
    }
    context_unlock(ctx);
}

static void safe_skip_breakpoint(void * arg) {
    Context * ctx = (Context *)arg;
    BreakInstruction * bi = EXT(ctx)->stepping_over_bp;
    int error = 0;

    assert(bi != NULL);
    assert(bi->stepping_over_bp > 0);
    assert(find_instruction(ctx, bi->address) == bi);

    post_safe_event(ctx->mem, safe_restore_breakpoint, ctx);

    if (ctx->exited || ctx->exiting) return;

    assert(ctx->stopped);
    assert(ctx->stopped_by_bp);
    assert(bi->address == get_regs_PC(ctx));

    if (bi->planted) remove_instruction(bi);
    if (bi->error) error = set_error_report_errno(bi->error);
    if (error == 0 && context_single_step(ctx) < 0) error = errno;
    if (error) trace(LOG_ALWAYS, "Skip breakpoint error: %d %s", error, errno_to_str(error));
}

#endif /* ifndef _WRS_KERNEL */

/*
 * When a context is stopped by breakpoint, it is necessary to disable
 * the breakpoint temporarily before the context can be resumed.
 * This function function removes break instruction, then does single step
 * over breakpoint location, then restores break intruction.
 * Return: 0 if it is OK to resume context from current state,
 * return 1 if context needs to step over a breakpoint.
 */
int skip_breakpoint(Context * ctx, int single_step) {
    BreakInstruction * bi;

    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ctx->pending_step);
    assert(single_step || EXT(ctx)->stepping_over_bp == NULL);

    if (EXT(ctx)->stepping_over_bp != NULL) return 0;
    if (ctx->exited || ctx->exiting || !ctx->stopped_by_bp) return 0;

#ifdef _WRS_KERNEL
    /* VxWork debug library can skip breakpoint when neccesary, no code is needed here */
    return 0;
#else
    bi = find_instruction(ctx, get_regs_PC(ctx));
    if (bi == NULL || bi->error) return 0;
    bi->stepping_over_bp++;
    EXT(ctx)->stepping_over_bp = bi;
    assert(bi->stepping_over_bp > 0);
    context_lock(ctx);
    post_safe_event(ctx->mem, safe_skip_breakpoint, ctx);
    return 1;
#endif
}

BreakpointInfo * create_eventpoint(const char * location, EventPointCallBack * callback, void * callback_args) {
    BreakpointInfo * bp = (BreakpointInfo *)loc_alloc_zero(sizeof(BreakpointInfo));
    bp->client_cnt = 1;
    bp->enabled = 1;
    bp->address = loc_strdup(location);
    bp->event_callback = callback;
    bp->event_callback_args = callback_args;
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

static void event_context_changed(Context * ctx, void * args) {
    if (ctx->mem != ctx) {
        /* Optimization: ignore a thread created/exited/changed event
         * if the thread ID is never used in a BP context ID list.
         */
        int cnt = 0;
        LINK * l = breakpoints.next;
        while (cnt == 0 && l != &breakpoints) {
            BreakpointInfo * bp = link_all2bp(l);
            char ** ids = bp->context_ids;
            if (ids != NULL && !is_disabled(bp)) {
                while (*ids != NULL) {
                    if (strcmp(*ids++, ctx->id) == 0) cnt++;
                }
            }
            l = l->next;
        }
        if (cnt == 0) return;
    }
    post_location_evaluation_request(ctx);
}

static void event_context_started(Context * ctx, void * args) {
    ContextExtensionBP * ext = EXT(ctx);
    ext->bp_eval_started = 0;
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
    LINK * l = instructions.next;
    while (l != &instructions) {
        int i;
        BreakInstruction * bi = link_all2bi(l);
        l = l->next;
        if (bi->ctx->mem != ctx->mem) continue;
        if (!bi->planted) continue;
        if (bi->address < addr || bi->address >= addr + size) continue;
        for (i = 0; i < bi->ref_cnt; i++) bi->refs[i]->instruction_cnt--;
        bi->planted = 0;
    }
}

static void channel_close_listener(Channel * c) {
    delete_breakpoint_refs(c);
}

#if !defined(_WRS_KERNEL)
static void eventpoint_at_main(Context * ctx, void * args) {
#if defined(__linux__)
    send_context_changed_event(ctx->mem);
#endif
    suspend_debug_context(ctx);
}
#endif

void ini_breakpoints_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    int i;
    broadcast_group = bcg;

    {
        static ContextEventListener listener = {
            event_context_changed,
            event_context_changed,
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
#if !defined(_WRS_KERNEL)
    create_eventpoint("main", eventpoint_at_main, NULL);
#endif
}

#endif /* SERVICE_Breakpoints */
