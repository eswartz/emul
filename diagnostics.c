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

static void command_echo(char * token, Channel * c) {
    char str[0x1000];
    int len = json_read_string(&c->inp, str, sizeof(str));
    if (len >= sizeof(str)) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    json_write_string_len(&c->out, str, len);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_test_list(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
#if defined(WIN32) || defined(__CYGWIN__)
    write_stringz(&c->out, "[]");
#elif defined(_WRS_KERNEL)
    write_stringz(&c->out, "[\"RCBP1\"]");
#else
    write_stringz(&c->out, "[\"RCBP1\"]");
#endif
    c->out.write(&c->out, MARKER_EOM);
}

static void command_run_test(char * token, Channel * c) {
    int err = 0;
    char id[256];
    pid_t pid = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (strcmp(id, "RCBP1") == 0) {
        if (run_test_process(&pid) < 0) err = errno;
    }
    else {
        err = EINVAL;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    json_write_string(&c->out, err ? NULL : pid2id(pid, 0));
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
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

static void command_cancel_test(char * token, Channel * c) {
    char id[256];
    Context * ctx = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_RunControl
    ctx = id2ctx(id);
    if (ctx != NULL && !ctx->exited) {
        context_lock(ctx);
        post_safe_event(event_terminate, ctx);
    }
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_symbol(char * token, Channel * c) {
    char id[256];
    char name[0x1000];
    Context * ctx;
    Symbol sym;
    int error = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(&c->inp, name, sizeof(name));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    
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

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, error);
    if (error != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        c->out.write(&c->out, '{');
        json_write_string(&c->out, "Abs");
        c->out.write(&c->out, ':');
        json_write_boolean(&c->out, sym.abs);
        c->out.write(&c->out, ',');
        json_write_string(&c->out, "Value");
        c->out.write(&c->out, ':');
        json_write_ulong(&c->out, sym.value);
        if (sym.section != NULL) {
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "Section");
            c->out.write(&c->out, ':');
            json_write_string(&c->out, sym.section);
        }
        if (sym.storage != NULL) {
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "Storage");
            c->out.write(&c->out, ':');
            json_write_string(&c->out, sym.storage);
        }
        c->out.write(&c->out, '}');
        c->out.write(&c->out, 0);
    }
    c->out.write(&c->out, MARKER_EOM);
}

void ini_diagnostics_service(Protocol *proto) {
    add_command_handler(proto, DIAGNOSTICS, "echo", command_echo);
    add_command_handler(proto, DIAGNOSTICS, "getTestList", command_get_test_list);
    add_command_handler(proto, DIAGNOSTICS, "runTest", command_run_test);
    add_command_handler(proto, DIAGNOSTICS, "cancelTest", command_cancel_test);
    add_command_handler(proto, DIAGNOSTICS, "getSymbol", command_get_symbol);
}

