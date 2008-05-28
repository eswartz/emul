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

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

static HANDLE current_process = NULL;
static ULONG current_base = 0;

/*
static BOOL CALLBACK EnumSymProc1( 
    LPSTR SymbolName,
    ULONG SymbolAddress,
    ULONG SymbolSize,
    PVOID UserContext)
{
    printf("%08X %4u %s\n", (long)SymbolAddress, SymbolSize, SymbolName);
    return TRUE;
}

static void print_symbols(void) {
    printf("process 0x%08x, base address 0x%08x\n", current_process, current_base);
    if (!SymEnumerateSymbols(current_process, current_base, EnumSymProc1, NULL)) {
        printf("SymEnumerateSymbols failed: %d\n", GetLastError());
    }
}
*/

int set_symbol_context(Context * ctx) {
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (ctx->handle != current_process) {
        if (current_process != NULL) {
            if (!SymCleanup(current_process)) {
                set_win32_errno(GetLastError());
                trace(LOG_ALWAYS, "SymCleanup() error: %d: %s",
                    errno, errno_to_str(errno));
            }
            current_process = NULL;
        }
        if (!SymInitialize(ctx->handle, NULL, FALSE)) {
            set_win32_errno(GetLastError());
            return -1;
        }
        if (!SymLoadModule(ctx->handle, ctx->file_handle, NULL, NULL, (DWORD)ctx->base_address, 0)) {
            set_win32_errno(GetLastError());
            return -1;
        }
        current_process = ctx->handle;
        current_base = (ULONG)ctx->base_address;
    }
    return 0;
}

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
        sym->value = (unsigned long)tcf_test_func0;
    }
    else if (strcmp(name, "tcf_test_func1") == 0) {
        sym->value = (unsigned long)tcf_test_func1;
    }
    else if (strcmp(name, "tcf_test_func2") == 0) {
        sym->value = (unsigned long)tcf_test_func2;
    }
    else if (strcmp(name, "tcf_test_array") == 0) {
        sym->value = (unsigned long)&tcf_test_array;
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
    if (set_symbol_context(ctx) != 0) return -1;

    memset(sym, 0, sizeof(Symbol));
    symbol->SizeOfStruct = sizeof(IMAGEHLP_SYMBOL);
    symbol->MaxNameLength = MAX_SYM_NAME;

    if (!SymGetSymFromName(ctx->handle, name, symbol)) {
        set_win32_errno(GetLastError());
        return -1;
    }

    sym->value = (unsigned long)symbol->Address;
    sym->abs = 1;

    sym->storage = "GLOBAL";

    return 0;

#endif
}

static void event_context_exited(Context * ctx, void * client_data) {
    assert(ctx->handle != NULL);
    if (ctx->handle == current_process) {
        if (!SymCleanup(current_process)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymCleanup() error: %d: %s",
                errno, errno_to_str(errno));
        }
        current_process = NULL;
    }
}

void ini_symbols_service(void) {
    static ContextEventListener listener = {
        NULL,
        event_context_exited,
        NULL,
        NULL,
        NULL
    };
    add_context_event_listener(&listener, NULL);
    SymSetOptions(SYMOPT_UNDNAME | SYMOPT_LOAD_LINES);
}


#endif

