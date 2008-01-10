/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Diagnostics service.
 * This service is used for framework and agents testing.
 */

#include "config.h"
#if defined(_WRS_KERNEL)
#  include <vxWorks.h>
#endif
#include <signal.h>
#include <assert.h>
#include <stdio.h>
#include "diagnostics.h"
#include "protocol.h"
#include "json.h"
#include "exceptions.h"
#include "runctrl.h"
#include "symbols.h"
#include "test.h"

static const char * DIAGNOSTICS = "Diagnostics";

static void command_echo(char * token, InputStream * inp, OutputStream * out) {
    char str[0x1000];
    int len = json_read_string(inp, str, sizeof(str));
    if (len >= sizeof(str)) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(out, "R");
    write_stringz(out, token);
    json_write_string_len(out, str, len);
    out->write(out, 0);
    out->write(out, MARKER_EOM);
}

static void command_get_test_list(char * token, InputStream * inp, OutputStream * out) {
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, 0);
#if defined(WIN32)
    write_stringz(out, "[]");
#elif defined(_WRS_KERNEL)
    write_stringz(out, "[\"RCBP1\"]");
#else
    write_stringz(out, "[\"RCBP1\"]");
#endif
    out->write(out, MARKER_EOM);
}

static void command_run_test(char * token, InputStream * inp, OutputStream * out) {
    int err = 0;
    char id[256];
    pid_t pid = 0;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (strcmp(id, "RCBP1") == 0) {
        if (run_test_process(&pid) < 0) err = errno;
    }
    else {
        err = EINVAL;
    }

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    json_write_string(out, err ? NULL : pid2id(pid, 0));
    out->write(out, 0);
    out->write(out, MARKER_EOM);
}

static void event_terminate(void * arg) {
    Context * ctx = arg;
    LINK * qp = ctx->children.next;
    while (qp != &ctx->children) {
        cldl2ctxp(qp)->pending_signals |= 1 << SIGKILL;
        qp = qp->next;
    }
    ctx->pending_signals |= 1 << SIGKILL;
    context_unlock(ctx);
}

static void command_cancel_test(char * token, InputStream * inp, OutputStream * out) {
    char id[256];
    Context * ctx = 0;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_RunControl
    ctx = id2ctx(id);
    if (ctx != NULL && !ctx->exited) {
        context_lock(ctx);
        post_safe_event(event_terminate, ctx);
    }
#endif

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, 0);
    out->write(out, MARKER_EOM);
}

static void command_get_symbol(char * token, InputStream * inp, OutputStream * out) {
    char id[256];
    char name[0x1000];
    Context * ctx;
    Symbol sym;
    int error = 0;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(inp, name, sizeof(name));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    
#if SERVICE_RunControl && SERVICE_Symbols
    ctx = id2ctx(id);
    if (ctx == NULL || ctx->exited) {
        error = ERR_INV_CONTEXT;
    }
    else if (find_symbol(ctx, name, &sym) < 0) {
        error = errno;
    }
#else
    ctx = NULL;
    error = EINVAL;
#endif

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, error);
    if (error != 0) {
        write_stringz(out, "null");
    }
    else {
        out->write(out, '{');
        json_write_string(out, "Abs");
        out->write(out, ':');
        json_write_boolean(out, sym.abs);
        out->write(out, ',');
        json_write_string(out, "Value");
        out->write(out, ':');
        json_write_ulong(out, sym.value);
        if (sym.section != NULL) {
            out->write(out, ',');
            json_write_string(out, "Section");
            out->write(out, ':');
            json_write_string(out, sym.section);
        }
        if (sym.storage != NULL) {
            out->write(out, ',');
            json_write_string(out, "Storage");
            out->write(out, ':');
            json_write_string(out, sym.storage);
        }
        out->write(out, '}');
        out->write(out, 0);
    }
    out->write(out, MARKER_EOM);
}

void ini_diagnostics_service(void) {
    add_command_handler(DIAGNOSTICS, "echo", command_echo);
    add_command_handler(DIAGNOSTICS, "getTestList", command_get_test_list);
    add_command_handler(DIAGNOSTICS, "runTest", command_run_test);
    add_command_handler(DIAGNOSTICS, "cancelTest", command_cancel_test);
    add_command_handler(DIAGNOSTICS, "getSymbol", command_get_symbol);
}

