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
 * Symbols service.- Windows version
 */
#include "mdep.h"
#include "config.h"

#if (SERVICE_Symbols) && defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <imagehlp.h>
#include "errors.h"
#include "elf.h"
#include "myalloc.h"
#include "symbols.h"
#include "stacktrace.h"
#include "trace.h"

#define SYM_SEARCH_PATH "http://msdl.microsoft.com/download/symbols"

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

static int get_stack_frame(Context * ctx, int frame, IMAGEHLP_STACK_FRAME * stack_frame) {
    memset(stack_frame, 0, sizeof(IMAGEHLP_STACK_FRAME));
    if (frame != STACK_NO_FRAME && ctx->parent != NULL) {
        ContextAddress ip = 0;
        if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) return -1;
        stack_frame->InstructionOffset = ip;
    }
    return 0;
}

static void syminfo2symbol(SYMBOL_INFO * info, Symbol * symbol) {
    memset(symbol, 0, sizeof(Symbol));

    symbol->value = (ContextAddress)info->Address;

    if (info->Flags & SYMFLAG_FRAMEREL) {
        symbol->base = SYM_BASE_FP;
    }
    else if (info->Flags & SYMFLAG_REGREL) {
        symbol->base = SYM_BASE_FP;
    }
    else {
        symbol->base = SYM_BASE_ABS;
    }

    if (info->Flags & SYMFLAG_LOCAL) {
        symbol->storage = "LOCAL";
    }
    else {
        symbol->storage = "GLOBAL";
    }

    symbol->size = info->Size;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {

#if defined(__CYGWIN__)

    /* TODO SymFromName() is not working in CYGWIN */

    extern void tcf_test_func0(void);
    extern void tcf_test_func1(void);
    extern void tcf_test_func2(void);
    extern char * tcf_test_array;

    int error = 0;

    memset(sym, 0, sizeof(Symbol));
    sym->section = ".text";
    sym->storage = "GLOBAL";
    sym->base = SYM_BASE_ABS;
    if (strcmp(name, "tcf_test_func0") == 0) {
        sym->value = (ContextAddress)tcf_test_func0;
    }
    else if (strcmp(name, "tcf_test_func1") == 0) {
        sym->value = (ContextAddress)tcf_test_func1;
    }
    else if (strcmp(name, "tcf_test_func2") == 0) {
        sym->value = (ContextAddress)tcf_test_func2;
    }
    else if (strcmp(name, "tcf_test_array") == 0) {
        sym->value = (ContextAddress)&tcf_test_array;
    }
    else {
        error = EINVAL;
    }
    if (error) {
        errno = error;
        return -1;
    }
    return 0;

#else

    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;

    symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
    symbol->MaxNameLen = MAX_SYM_NAME;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) {
        return -1;
    }

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err != ERROR_SUCCESS) {
            if (err == ERROR_MOD_NOT_FOUND && frame != STACK_NO_FRAME) {
                /* No local symbols data, search global scope */
                if (get_stack_frame(ctx, STACK_NO_FRAME, &stack_frame) < 0) {
                    return -1;
                }
                if (!SymSetContext(process, &stack_frame, NULL)) {
                    DWORD err = GetLastError();
                    if (err != ERROR_SUCCESS) {
                        set_win32_errno(err);
                        return -1;
                    }
                }
            }
            else {
                set_win32_errno(err);
                return -1;
            }
        }
    }

    if (!SymFromName(process, name, symbol)) {
        set_win32_errno(GetLastError());
        return -1;
    }

    syminfo2symbol(symbol, sym);
    return 0;

#endif
}

typedef struct EnumerateSymbolsContext {
    EnumerateSymbolsCallBack * call_back;
    void * args;
} EnumerateSymbolsContext;

static BOOL CALLBACK enumerate_symbols_proc(SYMBOL_INFO * info, ULONG symbol_size, VOID * user_context) {
    EnumerateSymbolsContext * enum_context = (EnumerateSymbolsContext *)user_context;
    Symbol symbol;
    syminfo2symbol(info, &symbol);
    enum_context->call_back(enum_context->args, info->Name, &symbol);
    return TRUE;
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
#if defined(__CYGWIN__)

    /* TODO SymEnumSymbols() is not working in CYGWIN */

    return 0;

#else

    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    EnumerateSymbolsContext enum_context;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;

    symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
    symbol->MaxNameLen = MAX_SYM_NAME;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) {
        return -1;
    }

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err != ERROR_SUCCESS) {
            set_win32_errno(err);
            return -1;
        }
    }

    enum_context.call_back = call_back;
    enum_context.args = args;

    if (!SymEnumSymbols(process, 0, NULL, enumerate_symbols_proc, &enum_context)) {
        set_win32_errno(GetLastError());
        return -1;
    }

    return 0;

#endif
}


ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    return 0;
}

static void event_context_created(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->pid == ctx->mem);
    if (!SymInitialize(ctx->handle, SYM_SEARCH_PATH, FALSE)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymInitialize() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymLoadModule(ctx->handle, ctx->file_handle, NULL, NULL, (DWORD)ctx->base_address, 0)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_exited(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->handle != NULL);
    if (!SymUnloadModule(ctx->handle, (DWORD)ctx->base_address)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymCleanup(ctx->handle)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymCleanup() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_changed(Context * ctx, void * client_data) {
    if (ctx->module_loaded) {
        assert(ctx->pid == ctx->mem);
        if (!SymLoadModule(ctx->handle, ctx->module_handle, NULL, NULL, (DWORD)ctx->module_address, 0)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
    if (ctx->module_unloaded) {
        assert(ctx->pid == ctx->mem);
        assert(ctx->handle != NULL);
        if (!SymUnloadModule(ctx->handle, (DWORD)ctx->module_address)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
}

void ini_symbols_service(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed
    };
    add_context_event_listener(&listener, NULL);
    SymSetOptions(SYMOPT_UNDNAME | SYMOPT_LOAD_LINES | SYMOPT_DEFERRED_LOADS);
}


#endif

