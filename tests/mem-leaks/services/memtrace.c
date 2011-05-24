/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
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
 * Memory Trace service.
 */

#include "config.h"

#include <stdio.h>
#include <assert.h>
#include <framework/link.h>
#include <framework/myalloc.h>
#include <framework/protocol.h>
#include <framework/context.h>
#include <services/breakpoints.h>
#include <services/stacktrace.h>
#include <services/linenumbers.h>
#include <services/memtrace.h>
#include <system/Windows/context-win32.h>

#define FUNC_CREATE     1
#define FUNC_ALLOC      2
#define FUNC_REALLOC    3
#define FUNC_EXPAND     4
#define FUNC_FREE       5
#define FUNC_DESTROY    6

typedef struct EventPoint {
    char * name;
    int heap_type;
    int func_type;
} EventPoint;

static EventPoint points[] = {
    { "_malloc",        1, FUNC_ALLOC },
    { "_realloc",       1, FUNC_REALLOC },
    { "_expand",        1, FUNC_EXPAND },
    { "_free",          1, FUNC_FREE },

    { "malloc",         1, FUNC_ALLOC },
    { "realloc",        1, FUNC_REALLOC },
    { "free",           1, FUNC_FREE },

    { "_malloc_dbg",    2, FUNC_ALLOC },
    { "_realloc_dbg",   2, FUNC_REALLOC },
    { "_expand_dbg",    2, FUNC_EXPAND },
    { "_free_dbg",      2, FUNC_FREE },

    { "HeapCreate",     3, FUNC_CREATE },
    { "HeapAlloc",      3, FUNC_ALLOC },
    { "HeapReAlloc",    3, FUNC_REALLOC },
    { "HeapFree",       3, FUNC_FREE },
    { "HeapDestroy",    3, FUNC_DESTROY },
    { NULL, 0, 0 }
};

#define STK_TRACE_SIZE 8
#define STK_HASH_SIZE 511
#define MEM_HASH_SIZE 111

typedef struct StackTrace {
    LINK link_all;
    Context * mem;
    int heap_type;
    int func_type;
    int frame_cnt;
    ContextAddress frames[STK_TRACE_SIZE];

    int call_cnt;
    uint64_t size_total;
    uint64_t size_current;
} StackTrace;
/* TODO: 'frames' must be represented as module plus offset to support DLL loading/unloading */

typedef struct MemBlock {
    int heap_type;
    ContextAddress addr;
    ContextAddress size;
    StackTrace * trace;
    struct MemBlock * l;
    struct MemBlock * r;
    int bal;
} MemBlock;

typedef struct MemorySpace {
    LINK link_all;
    LINK link_rtn;
    LINK stk_hash[STK_HASH_SIZE];
    Context * mem;
    MemBlock * table;
    time_t report_time;
} MemorySpace;

typedef struct ReturnPoint {
    LINK link_mem;
    Context * ctx;
    MemorySpace * mem;
    StackTrace * trace;
    ContextAddress addr;
    ContextAddress args[2];
} ReturnPoint;

static LINK mem_hash[MEM_HASH_SIZE];
static RegisterDefinition * reg_def_eax = NULL;
static RegisterDefinition * reg_def_esp = NULL;
static RegisterDefinition * reg_def_eip = NULL;

#define link_mem2trace(x)  ((StackTrace *)((char *)(x) - offsetof(StackTrace, link_all)))
#define link_mem2ret(x)    ((ReturnPoint *)((char *)(x) - offsetof(ReturnPoint, link_mem)))
#define link_all2mem(x)    ((MemorySpace *)((char *)(x) - offsetof(MemorySpace, link_all)))

static unsigned calc_trace_hash(StackTrace * t) {
    int i;
    unsigned h = ((uintptr_t)t->mem >> 4) + t->heap_type + t->func_type;
    for (i = 0; i < t->frame_cnt; i++) {
        h += (unsigned)t->frames[i];
    }
    return h % STK_HASH_SIZE;
}

static unsigned calc_mem_hash(Context * ctx) {
    return (unsigned)ctx->mem % MEM_HASH_SIZE;
}

static MemorySpace * get_mem_space(Context * ctx, int alloc) {
    MemorySpace * m = NULL;
    unsigned h = calc_mem_hash(ctx);
    LINK * l = mem_hash[h].next;
    while (l != mem_hash + h) {
        MemorySpace * x = link_all2mem(l);
        l = l->next;
        if (x->mem == ctx->mem) {
            m = x;
            break;
        }
    }
    if (m == NULL && alloc) {
        int i;
        m = (MemorySpace *)loc_alloc_zero(sizeof(MemorySpace));
        m->mem = ctx->mem;
        list_init(&m->link_rtn);
        for (i = 0; i < STK_HASH_SIZE; i++) list_init(m->stk_hash + i);
        list_add_first(&m->link_all, mem_hash + h);
    }
    return m;
}

static void free_mem_block(MemBlock * x) {
    if (x->l) free_mem_block(x->l);
    if (x->r) free_mem_block(x->r);
    loc_free(x);
}

static void free_mem_space(MemorySpace * m) {
    int i;

    assert(list_is_empty(&m->link_rtn));
    list_remove(&m->link_all);
    for (i = 0; i < STK_HASH_SIZE; i++) {
        LINK * l = m->stk_hash[i].next;
        while (l != m->stk_hash + i) {
            StackTrace * x = link_mem2trace(l);
            l = l->next;
            assert(x->mem == m->mem);
            list_remove(&x->link_all);
            loc_free(x);
        }
    }
    if (m->table) free_mem_block(m->table);
    loc_free(m);
}

static void free_return_point(ReturnPoint * r) {
    int error = 0;
    CONTEXT regs;

    memset(&regs, 0, sizeof(regs));
    regs.ContextFlags = CONTEXT_DEBUG_REGISTERS;
    if (GetThreadContext(get_context_handle(r->ctx), &regs) == 0) error = set_win32_errno(GetLastError());
    if (!error) {
        regs.Dr7 &= ~0x03030003;
        if (SetThreadContext(get_context_handle(r->ctx), &regs) == 0) error = set_win32_errno(GetLastError());
    }
    if (error) {
        printf("free_return_point: %s\n", errno_to_str(error));
    }

    context_unlock(r->ctx);
    list_remove(&r->link_mem);
    loc_free(r);
}

static void app_bal(MemBlock * x, MemBlock ** p, int * h) {
    MemBlock * p1, * p2;
    if (*p == NULL) {
        *p = x;
        x->l = NULL;
        x->r = NULL;
        *h = 1;
        x->bal = 0;
        return;
    }
    if ((*p)->addr > x->addr) {
        app_bal(x, &(*p)->l, h);
        if (*h) {
            switch((*p)->bal) {
            case +1: (*p)->bal = 0; (*h) = 0; break;
            case  0: (*p)->bal =-1; break;
            case -1:
                p1 = (*p)->l;
                if (p1->bal == -1) {
                    (*p)->l = p1->r; p1->r = (*p);
                    (*p)->bal = 0; (*p) = p1;
                }
                else {
                    p2 = p1->r; p1->r = p2->l;
                    p2->l = p1; (*p)->l = p2->r; p2->r = (*p);
                    if (p2->bal == -1) (*p)->bal = +1; else (*p)->bal = 0;
                    if (p2->bal == +1) p1->bal = -1; else p1->bal = 0;
                    (*p) = p2;
                }
                (*p)->bal = 0;
                (*h) = 0;
                break;
            default:
                assert(0);
            }
        }
    }
    else if ((*p)->addr < x->addr) {
        app_bal(x, &(*p)->r, h);
        if (*h) {
            switch ((*p)->bal) {
            case -1: (*p)->bal = 0; (*h) = 0; break;
            case  0: (*p)->bal =+1; break;
            case +1:
                p1 = (*p)->r;
                if (p1->bal == +1) {
                    (*p)->r = p1->l; p1->l = *p;
                    (*p)->bal = 0; *p = p1;
                }
                else {
                    p2 = p1->l; p1->l = p2->r;
                    p2->r = p1; (*p)->r = p2->l; p2->l = *p;
                    if (p2->bal == +1) (*p)->bal = -1; else (*p)->bal = 0;
                    if (p2->bal == -1) p1->bal = +1; else p1->bal = 0;
                    (*p) = p2;
                }
                (*p)->bal = 0;
                (*h) = 0;
                break;
            default:
                assert(0);
            }
        }
    }
}

static MemBlock * rem_lost = NULL;

/*
 * remove entry x from table p
 * h == 1  tree length decreased
 */
static void rem_bal(MemBlock * x, MemBlock ** p, int * h) {
    MemBlock * p1 = NULL;
    MemBlock * p2 = NULL;
    assert(*p != NULL);
    assert(x != NULL);
    if ((*p)->addr > x->addr) {
        if ((*p)->l == NULL) return;
        rem_bal(x, &(*p)->l, h);
        if (*h) {
            switch((*p)->bal) {
            case -1: /* left was longer */
                (*p)->bal = 0; break;
            case  0:
                (*p)->bal = +1; (*h) = 0; break;
            case +1: /* right was longer */
                p1 = (*p)->r;
                assert(p1 != NULL);
                if (p1->bal == +1) {
                    assert(p1->r != NULL);
                    (*p)->r = p1->l;
                    p1->l = *p;
                    (*p)->bal = 0;
                    *p = p1;
                    (*p)->bal = 0;
                    (*h) = 1;
                }
                else if (p1->bal == 0 && p1->l->bal == -1) {
                    (*p)->r = p1->l;
                    p1->l = *p;
                    (*p)->bal = +1;
                    *p = p1;
                    (*p)->bal = -1;
                    (*h) = 0;
                }
                else {
                    int i = p1->bal;
                    p2 = p1->l;
                    p1->l = p2->r;
                    p2->r = p1;
                    (*p)->r = p2->l;
                    p2->l = *p;
                    (*p)->bal = (p2->bal == +1 ? -1 : 0);
                    p1->bal = (i < 0 && p2->bal >= 0 ? 0 : +1);
                    p2->bal = (i < 0 ? 0 : +1);
                    (*p) = p2;
                    (*h) = i < 0;
                }
                break;
            default:
                assert(0);
            }
        }
    }
    else if ((*p)->addr < x->addr) {
        if ((*p)->r == NULL) return;
        rem_bal(x, &(*p)->r, h);
        if (*h) {
            switch ((*p)->bal) {
            case +1:
                (*p)->bal = 0; break;
            case  0:
                (*p)->bal =-1; (*h) = 0; break;
            case -1:
                p1 = (*p)->l;
                assert(p1 != NULL);
                if (p1->bal == -1) {
                    (*p)->l = p1->r;
                    p1->r = *p;
                    (*p)->bal = 0;
                    (*p) = p1;
                    (*p)->bal = 0;
                    (*h) = 1;
                }
                else if (p1->bal == 0 && p1->r->bal == +1) {
                    (*p)->l = p1->r;
                    p1->r = *p;
                    (*p)->bal = -1;
                    (*p) = p1;
                    (*p)->bal = +1;
                    (*h) = 0;
                }
                else {
                    int i = p1->bal;
                    p2 = p1->r;
                    p1->r = p2->l;
                    p2->l = p1;
                    (*p)->l = p2->r;
                    p2->r = (*p);
                    (*p)->bal = (p2->bal == -1 ? +1 : 0);
                    p1->bal = (i > 0 && p2->bal <= 0 ? 0 : -1);
                    p2->bal = (i > 0 ? 0 : -1);
                    (*p) = p2;
                    (*h) = i > 0;
                }
                break;
            default:
                assert(0);
            }
        }
    }
    else if ((*p)->l == NULL) { /* found it, no left link... */
        *p = (*p)->r;           /* unlink right */
        *h = 1;                 /* set flag */
    }
    else if ((*p)->bal < 0) {
        rem_lost = (*p)->r;     /* save right side in rem_lost */
        *p = (*p)->l;           /* unlink left */
        *h = 1;                 /* set flag */
    }
    else {
        assert((*p)->r != NULL);
        rem_lost = (*p)->l;     /* save left side in rem_lost */
        *p = (*p)->r;           /* unlink right */
        *h = 1;                 /* set flag */
    }
}

static void add_mem_block(MemorySpace * m, MemBlock * x) {
    int h = 0;
    app_bal(x, &m->table, &h);
}

static void append_tree(MemorySpace * m, MemBlock * x) {
    if (x->l) append_tree(m, x->l);
    if (x->r) append_tree(m, x->r);
    add_mem_block(m, x);
}

static void rem_mem_block(MemorySpace * m, MemBlock * x) {
    int h = 0;
    rem_lost = NULL;
    rem_bal(x, &m->table, &h);
    if (rem_lost) append_tree(m, rem_lost);
}

static MemBlock * find_mem_block(MemorySpace * m, ContextAddress p, ContextAddress e) {
    MemBlock * x = m->table;
    while (x != NULL) {
        if (x->addr <= e && x->addr + x->size >= p) return x;
        x = x->addr < p ? x->r : x->l;
    }
    return NULL;
}

static ReturnPoint * find_pending_return_point(Context * ctx) {
    LINK * l;
    MemorySpace * m = NULL;

    m = get_mem_space(ctx, 0);
    if (m == NULL) return NULL;
    l = m->link_rtn.next;
    while (l != &m->link_rtn) {
        ReturnPoint * r = link_mem2ret(l);
        l = l->next;
        if (r->ctx == ctx) return r;
    }
    return NULL;
}

static ContextAddress read_reg(Context * ctx, RegisterDefinition * r) {
    size_t i;
    ContextAddress n = 0;
    uint8_t buf[8];
    assert(r->size <= sizeof(buf));
    if (context_read_reg(ctx, r, 0, r->size, buf) < 0) return 0;
    for (i = 0; i < r->size; i++) {
        n = n << 8;
        n |= buf[r->big_endian ? i : r->size - i - 1];
    }
    return n;
}

static void return_point(Context * ctx, ReturnPoint * r) {
    MemBlock * b = NULL;
    ContextAddress eax = read_reg(ctx, reg_def_eax);

    switch (r->trace->func_type) {
    case FUNC_ALLOC:
        if (eax != 0) {
            b = (MemBlock *)loc_alloc_zero(sizeof(MemBlock));
            b->heap_type = r->trace->heap_type;
            b->addr = eax;
            b->size = r->args[0];
            b->trace = r->trace;
            b->trace->size_current += b->size;
            b->trace->size_total += b->size;
            add_mem_block(r->mem, b);
        }
        break;
    case FUNC_REALLOC:
    case FUNC_EXPAND:
        if (eax != 0) {
            b = find_mem_block(r->mem, r->args[0], r->args[0] + 1);
            if (b != NULL) {
                rem_mem_block(r->mem, b);
                b->trace->size_current -= b->size;
            }
            else {
                b = (MemBlock *)loc_alloc_zero(sizeof(MemBlock));
            }
            b->heap_type = r->trace->heap_type;
            b->addr = eax;
            b->size = r->args[1];
            b->trace = r->trace;
            b->trace->size_current += b->size;
            b->trace->size_total += b->size;
            add_mem_block(r->mem, b);
        }
        break;
    }
    free_return_point(r);
}

static int sort_func(const void * x, const void * y) {
    StackTrace * tx = *(StackTrace **)x;
    StackTrace * ty = *(StackTrace **)y;
    if (tx->size_current > ty->size_current) return -1;
    if (tx->size_current < ty->size_current) return +1;
    return 0;
}

static int print_text_pos_cnt = 0;

static void print_text_pos(CodeArea * area, void * args) {
    if (print_text_pos_cnt == 0) {
        printf("    %s %d\n", area->file, area->start_line);
    }
    print_text_pos_cnt++;
}

static void event_point(Context * ctx, void * args) {
    EventPoint * p = (EventPoint *)args;
    int top_frame = STACK_NO_FRAME;
    StackFrame * info = NULL;
    uint64_t esp = 0;
    uint64_t eip = 0;
    ContextAddress buf[4];
    MemorySpace * m = NULL;
    static StackTrace trace;
    StackTrace * t = NULL;
    int error = 0;

    if (find_pending_return_point(ctx) != NULL) return;

    if (p->heap_type == 3) {
        printf("%s\n", p->name);
        return;
    }

    if ((top_frame = get_top_frame(ctx)) < 0) error = errno;
    if (!error && get_frame_info(ctx, top_frame, &info) < 0) error = errno;
    if (!error && read_reg_value(info, reg_def_esp, &esp) < 0) error = errno;
    if (!error && read_reg_value(info, reg_def_eip, &eip) < 0) error = errno;
    if (!error && context_read_mem(ctx, (ContextAddress)esp, buf, sizeof(buf)) < 0) error = errno;
    memset(&trace, 0, sizeof(trace));
    if (!error) {
        trace.mem = ctx->mem;
        trace.heap_type = p->heap_type;
        trace.func_type = p->func_type;
        trace.frame_cnt = 0;
        while (trace.frame_cnt < STK_TRACE_SIZE && trace.frame_cnt < top_frame) {
            if (get_frame_info(ctx, top_frame - trace.frame_cnt - 1, &info) < 0) {
                error = errno;
                break;
            }
            if (read_reg_value(info, reg_def_eip, &eip) < 0) {
                error = errno;
                break;
            }
            trace.frames[trace.frame_cnt++] = (ContextAddress)eip;
        }
    }
    if (!error) {
        LINK * l;
        unsigned h = calc_trace_hash(&trace);
        m = get_mem_space(ctx, 1);
        for (l = m->stk_hash[h].next; l != m->stk_hash + h; l = l->next) {
            int i;
            StackTrace * x = (StackTrace *)link_mem2trace(l);
            if (x->mem != trace.mem) continue;
            if (x->heap_type != trace.heap_type) continue;
            if (x->func_type != trace.func_type) continue;
            if (x->frame_cnt != trace.frame_cnt) continue;
            for (i = 0; i < trace.frame_cnt; i++) {
                if (x->frames[i] != trace.frames[i]) break;
            }
            if (i == trace.frame_cnt) {
                t = x;
                break;
            }
        }
        if (t == NULL) {
            *(t = (StackTrace *)loc_alloc(sizeof(StackTrace))) = trace;
            list_add_first(&t->link_all, m->stk_hash + h);
        }
        t->call_cnt++;
    }
    if (!error) {
        if (p->func_type == FUNC_FREE) {
            MemBlock * b = find_mem_block(m, buf[1], buf[1] + 1);
            if (b != NULL) {
                rem_mem_block(m, b);
                b->trace->size_current -= b->size;
                loc_free(b);
            }
        }
        else if (p->func_type == FUNC_DESTROY) {
        }
        else {
            CONTEXT regs;
            ReturnPoint * r = (ReturnPoint *)loc_alloc_zero(sizeof(ReturnPoint));

            r->trace = t;
            r->ctx = ctx;
            r->mem = m;
            r->addr = buf[0];
            r->args[0] = buf[1];
            r->args[1] = buf[2];
            list_add_first(&r->link_mem, &m->link_rtn);
            context_lock(r->ctx);

            memset(&regs, 0, sizeof(regs));
            regs.ContextFlags = CONTEXT_DEBUG_REGISTERS;
            if (GetThreadContext(get_context_handle(ctx), &regs) == 0) error = set_win32_errno(GetLastError());
            if (!error && (regs.Dr7 & 0x03) != 0) error = set_errno(ERR_OTHER, "HW breakpoint not available");
            if (!error) {
                regs.Dr0 = r->addr;
                regs.Dr7 &= ~0x03030003;
                regs.Dr7 |=  0x00000001;
                if (SetThreadContext(get_context_handle(ctx), &regs) == 0) error = set_win32_errno(GetLastError());
            }
            if (error) free_return_point(r);
        }
    }
    if (error) {
        printf("%s: %s\n", p->name, errno_to_str(error));
    }
    if (m != NULL && m->report_time + 30 < time(NULL)) {
        int i;
        int cnt = 0;
        m->report_time = time(NULL);
        for (i = 0; i < STK_HASH_SIZE; i++) {
            LINK * l = m->stk_hash[i].next;
            while (l != m->stk_hash + i) {
                l = l->next;
                cnt++;
            }
        }
        if (cnt > 0) {
            int pos = 0;
            StackTrace ** buf = (StackTrace **)loc_alloc(sizeof(StackTrace *) * cnt);
            for (i = 0; i < STK_HASH_SIZE; i++) {
                LINK * l = m->stk_hash[i].next;
                while (l != m->stk_hash + i) {
                    buf[pos++] = link_mem2trace(l);
                    l = l->next;
                }
            }
            assert(pos == cnt);
            qsort(buf, cnt, sizeof(StackTrace *), sort_func);
            printf("\nPID %d, total traces %d\n", m->mem, cnt);
            for (i = 0; i < 8 && i < cnt; i++) {
                int j;
                StackTrace * t = buf[i];
                printf("  curr %lld, total %lld, calls %d\n",
                    (long long)t->size_current, (long long)t->size_total, t->call_cnt);
                for (j = 0; j < t->frame_cnt; j++) {
                    print_text_pos_cnt = 0;
                    address_to_line(ctx, t->frames[j], t->frames[j] + 1, print_text_pos, NULL);
                    if (print_text_pos_cnt == 0) {
                        printf("    0x%08x\n", t->frames[j]);
                    }
                }
            }
            loc_free(buf);
        }
    }
}

static void event_context_created(Context * ctx, void * args) {
    if (ctx->parent != NULL && ctx->parent->mem == ctx->mem) return;

}

static int contex_exception_handler(Context * ctx, EXCEPTION_DEBUG_INFO * info) {
    if (info->ExceptionRecord.ExceptionCode == EXCEPTION_SINGLE_STEP) {
        MemorySpace * m = get_mem_space(ctx, 0);
        if (m != NULL) {
            LINK * l = m->link_rtn.next;
            while (l != &m->link_rtn) {
                ReturnPoint * r = link_mem2ret(l);
                l = l->next;
                if (r->ctx == ctx) {
                    ContextAddress addr = read_reg(ctx, reg_def_eip);
                    if (r->addr == addr) {
                        return_point(ctx, r);
                        return 1;
                    }
                }
            }
        }
    }
    return 0;
}

static void event_context_stopped(Context * ctx, void * args) {
}

static void event_context_exited(Context * ctx, void * args) {
    MemorySpace * m = get_mem_space(ctx, 0);
    if (m != NULL) {
        LINK * l = m->link_rtn.next;
        while (l != &m->link_rtn) {
            ReturnPoint * r = link_mem2ret(l);
            l = l->next;
            if (r->ctx == ctx) {
                free_return_point(r);
                break;
            }
        }
        if (ctx->parent == NULL || ctx->parent->mem != ctx->mem) {
            free_mem_space(m);
        }
    }
}

void ini_mem_trace_service(Protocol * proto) {
    int i;
    EventPoint * p = points;
    RegisterDefinition * r = get_reg_definitions(NULL);

    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        event_context_stopped,
        NULL,
        NULL
    };

    while (p->name) {
        create_eventpoint(p->name, NULL, event_point, p);
        p++;
    }

    for (i = 0; i < MEM_HASH_SIZE; i++) list_init(mem_hash + i);

    while (r->name != NULL) {
        if (strcmp(r->name, "eax") == 0) reg_def_eax = r;
        if (strcmp(r->name, "esp") == 0) reg_def_esp = r;
        if (strcmp(r->name, "eip") == 0) reg_def_eip = r;
        r++;
    }

    add_context_event_listener(&listener, NULL);
    add_context_exception_handler(contex_exception_handler);
}
