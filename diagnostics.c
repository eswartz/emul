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

#include "mdep.h"
#include "config.h"
#include <signal.h>
#include <assert.h>
#include <stdio.h>
#include "diagnostics.h"
#include "protocol.h"
#include "json.h"
#include "exceptions.h"
#include "runctrl.h"
#include "symbols.h"
#include "stacktrace.h"
#include "test.h"
#include "myalloc.h"

static const char * DIAGNOSTICS = "Diagnostics";

typedef struct RunTestDoneArgs RunTestDoneArgs;

struct RunTestDoneArgs {
    Channel * c;
    Context * ctx;
    char token[256];
};

static void command_echo(char * token, Channel * c) {
    char str[0x1000];
    int len = json_read_string(&c->inp, str, sizeof(str));
    if (len >= sizeof(str)) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    json_write_string_len(&c->out, str, len);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_test_list(char * token, Channel * c) {
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stringz(&c->out, "[\"RCBP1\"]");
    write_stream(&c->out, MARKER_EOM);
}

static void run_test_done(int error, Context * ctx, void * arg) {
    RunTestDoneArgs * data = arg;
    Channel * c = data->c;

    if (!is_stream_closed(c)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, data->token);
        write_errno(&c->out, error);
        json_write_string(&c->out, ctx ? ctx2id(ctx) : NULL);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
    }
    stream_unlock(c);
    loc_free(data);
}

static void command_run_test(char * token, Channel * c) {
    int err = 0;
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (strcmp(id, "RCBP1") == 0) {
        RunTestDoneArgs * data = loc_alloc_zero(sizeof(RunTestDoneArgs));
        data->c = c;
        strcpy(data->token, token);
        stream_lock(c);
        if (run_test_process(run_test_done, data) == 0) return;
        err = errno;
        stream_unlock(c);
        loc_free(data);
    }
    else {
        err = EINVAL;
    }
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    json_write_string(&c->out, NULL);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_cancel_test(char * token, Channel * c) {
    char id[256];
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_RunControl
    if (terminate_debug_context(c->bcg, id2ctx(id)) != 0) err = errno;
#else
    err = ERR_UNSUPPORTED;
#endif
    
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_symbol(char * token, Channel * c) {
    char id[256];
    char name[0x1000];
    Context * ctx;
    Symbol sym;
    int error = 0;
    memset(&sym, 0, sizeof(sym));

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(&c->inp, name, sizeof(name));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    
#if SERVICE_Symbols
    ctx = id2ctx(id);
    if (ctx == NULL || ctx->exited) {
        error = ERR_INV_CONTEXT;
    }
    else if (find_symbol(ctx, STACK_NO_FRAME, name, &sym) < 0) {
        error = errno;
    }
#else
    ctx = NULL;
    error = ERR_UNSUPPORTED;
#endif

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, error);
    if (error != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        write_stream(&c->out, '{');
        json_write_string(&c->out, "Abs");
        write_stream(&c->out, ':');
        json_write_boolean(&c->out, sym.base == SYM_BASE_ABS);
        write_stream(&c->out, ',');
        json_write_string(&c->out, "Value");
        write_stream(&c->out, ':');
        json_write_ulong(&c->out, sym.value);
        if (sym.section != NULL) {
            write_stream(&c->out, ',');
            json_write_string(&c->out, "Section");
            write_stream(&c->out, ':');
            json_write_string(&c->out, sym.section);
        }
        if (sym.storage != NULL) {
            write_stream(&c->out, ',');
            json_write_string(&c->out, "Storage");
            write_stream(&c->out, ':');
            json_write_string(&c->out, sym.storage);
        }
        write_stream(&c->out, '}');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

void ini_diagnostics_service(Protocol * proto) {
    add_command_handler(proto, DIAGNOSTICS, "echo", command_echo);
    add_command_handler(proto, DIAGNOSTICS, "getTestList", command_get_test_list);
    add_command_handler(proto, DIAGNOSTICS, "runTest", command_run_test);
    add_command_handler(proto, DIAGNOSTICS, "cancelTest", command_cancel_test);
    add_command_handler(proto, DIAGNOSTICS, "getSymbol", command_get_symbol);
}



