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
#include "trace.h"

#define SYM_SEARCH_PATH "http://msdl.microsoft.com/download/symbols"

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

int find_symbol(Context * ctx, char * name, Symbol * sym) {

#if defined(__CYGWIN__)

    /* TODO SymGetSymFromName() is not working in CYGWIN */

    extern void tcf_test_func0(void);
    extern void tcf_test_func1(void);
    extern void tcf_test_func2(void);
    extern char * tcf_test_array;

    int error = 0;

    memset(sym, 0, sizeof(Symbol));
    sym->section = ".text";
    sym->storage = "GLOBAL";
    sym->abs = 1;
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

    ULONG64 sym_buf[(sizeof(IMAGEHLP_SYMBOL) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    PIMAGEHLP_SYMBOL symbol = (PIMAGEHLP_SYMBOL)sym_buf;

    if (ctx->parent != NULL) ctx = ctx->parent;

    memset(sym, 0, sizeof(Symbol));
    symbol->SizeOfStruct = sizeof(IMAGEHLP_SYMBOL);
    symbol->MaxNameLength = MAX_SYM_NAME;

    if (!SymGetSymFromName(ctx->handle, name, symbol)) {
        set_win32_errno(GetLastError());
        return -1;
    }

    sym->value = (ContextAddress)symbol->Address;
    sym->abs = 1;

    sym->storage = "GLOBAL";

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

