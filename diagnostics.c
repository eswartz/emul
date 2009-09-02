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
 * Diagnostics service.
 * This service is used for framework and agents testing.
 */

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
#include "streamsservice.h"
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

static void command_echo_fp(char * token, Channel * c) {
    double x = json_read_double(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    json_write_double(&c->out, x);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_test_list(char * token, Channel * c) {
    char * arr = "[]";
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
#if ENABLE_RCBP_TEST
    if (context_root.next != NULL) arr = "[\"RCBP1\"]";
#endif
    write_stringz(&c->out, arr);
    write_stream(&c->out, MARKER_EOM);
}

#if ENABLE_RCBP_TEST
static void run_test_done(int error, Context * ctx, void * arg) {
    RunTestDoneArgs * data = arg;
    Channel * c = data->c;

    ctx->test_process = 1;
    if (!is_stream_closed(c)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, data->token);
        write_errno(&c->out, error);
        json_write_string(&c->out, ctx ? container_id(ctx) : NULL);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
    }
    stream_unlock(c);
    loc_free(data);
}
#endif

static void command_run_test(char * token, Channel * c) {
    int err = 0;
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (strcmp(id, "RCBP1") == 0) {
#if ENABLE_RCBP_TEST
        RunTestDoneArgs * data = loc_alloc_zero(sizeof(RunTestDoneArgs));
        data->c = c;
        strcpy(data->token, token);
        if (run_test_process(run_test_done, data) == 0) {
            stream_lock(c);
            return;
        }
        err = errno;
        loc_free(data);
#else
        err = EINVAL;
#endif
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

#if ENABLE_RCBP_TEST
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

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(&c->inp, name, sizeof(name));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_Symbols
    {
        Context * ctx;
        int error = 0;
        Symbol sym;

        memset(&sym, 0, sizeof(sym));
        ctx = id2ctx(id);
        if (ctx == NULL || ctx->exited) {
            error = ERR_INV_CONTEXT;
        }
        else if (find_symbol(ctx, STACK_NO_FRAME, name, &sym) < 0) {
            error = errno;
        }
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_errno(&c->out, error);
        if (error != 0) {
            write_stringz(&c->out, "null");
        }
        else {
            ContextAddress addr = 0;
            write_stream(&c->out, '{');
            if (get_symbol_address(&sym, STACK_NO_FRAME, &addr) >= 0) {
                json_write_string(&c->out, "Abs");
                write_stream(&c->out, ':');
                json_write_boolean(&c->out, 1);
                write_stream(&c->out, ',');
                json_write_string(&c->out, "Value");
                write_stream(&c->out, ':');
                json_write_ulong(&c->out, addr);
            }
            write_stream(&c->out, '}');
            write_stream(&c->out, 0);
        }
    }
#else
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, ERR_UNSUPPORTED);
    write_stringz(&c->out, "null");
#endif
    write_stream(&c->out, MARKER_EOM);
}

#if SERVICE_Streams

typedef struct StreamsTest {
    VirtualStream * inp;
    VirtualStream * out;
    char buf[111];
    size_t buf_pos;
    size_t buf_len;
    int inp_eos;
    int out_eos;
} StreamsTest;

static void streams_test_callback(VirtualStream * stream, int event_code, void * args) {
    StreamsTest * st = (StreamsTest *)args;

    switch (event_code) {
    case VS_EVENT_SPACE_AVAILABLE:
        if (stream != st->out) return;
        break;
    case VS_EVENT_DATA_AVAILABLE:
        if (stream != st->inp) return;
        break;
    }

    if (st->buf_pos == st->buf_len && !st->inp_eos) {
        st->buf_pos = st->buf_len = 0;
        virtual_stream_get_data(st->inp, st->buf, sizeof(st->buf), &st->buf_len, &st->inp_eos);
    }

    if (st->buf_len > st->buf_pos || st->inp_eos != st->out_eos) {
        size_t done = 0;
        virtual_stream_add_data(st->out, st->buf + st->buf_pos, st->buf_len - st->buf_pos, &done, st->inp_eos);
        st->buf_pos += done;
        if (st->buf_pos == st->buf_len && st->inp_eos) st->out_eos = 1;
    }
}

#endif /* SERVICE_Streams */

static void command_create_test_streams(char * token, Channel * c) {
    char id_inp[256];
    char id_out[256];
    long buf_size0;
    long buf_size1;
    int err = 0;

    buf_size0 = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    buf_size1 = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_Streams
    if (buf_size0 <= 0 || buf_size1 <= 0) err = ERR_INV_NUMBER;
    if (!err) {
        StreamsTest * st = loc_alloc_zero(sizeof(StreamsTest));
        virtual_stream_create(DIAGNOSTICS, NULL, (unsigned)buf_size0,
            VS_ENABLE_REMOTE_WRITE, streams_test_callback, st, &st->inp);
        virtual_stream_create(DIAGNOSTICS, NULL, (unsigned)buf_size1,
            VS_ENABLE_REMOTE_READ, streams_test_callback, st, &st->out);
        virtual_stream_get_id(st->inp, id_inp, sizeof(id_inp));
        virtual_stream_get_id(st->out, id_out, sizeof(id_out));
    }
#else
    err = ERR_UNSUPPORTED;
#endif
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
        write_stringz(&c->out, "null");
    }
    else {
        json_write_string(&c->out, id_inp);
        write_stream(&c->out, 0);
        json_write_string(&c->out, id_out);
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_dispose_test_stream(char * token, Channel * c) {
    char id[256];
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

#if SERVICE_Streams
    if (!err) {
        VirtualStream * stream = virtual_stream_find(id);
        if (stream == NULL) err = errno;
        else virtual_stream_delete(stream);
    }
#else
    err = ERR_UNSUPPORTED;
#endif
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

void ini_diagnostics_service(Protocol * proto) {
    add_command_handler(proto, DIAGNOSTICS, "echo", command_echo);
    add_command_handler(proto, DIAGNOSTICS, "echoFP", command_echo_fp);
    add_command_handler(proto, DIAGNOSTICS, "getTestList", command_get_test_list);
    add_command_handler(proto, DIAGNOSTICS, "runTest", command_run_test);
    add_command_handler(proto, DIAGNOSTICS, "cancelTest", command_cancel_test);
    add_command_handler(proto, DIAGNOSTICS, "getSymbol", command_get_symbol);
    add_command_handler(proto, DIAGNOSTICS, "createTestStreams", command_create_test_streams);
    add_command_handler(proto, DIAGNOSTICS, "disposeTestStream", command_dispose_test_stream);
}



