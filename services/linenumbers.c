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
 * TCF service line Numbers - common part.
 *
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include <config.h>

#if SERVICE_LineNumbers

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include <framework/context.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/cache.h>
#include <framework/json.h>
#include <framework/protocol.h>
#include <framework/trace.h>
#include <services/linenumbers.h>

typedef struct MapToSourceArgs {
    char token[256];
    char id[256];
    ContextAddress addr0;
    ContextAddress addr1;
} MapToSourceArgs;

typedef struct MapToMemoryArgs {
    char token[256];
    char id[256];
    char * file;
    int line;
    int column;
} MapToMemoryArgs;

static int code_area_cnt = 0;
static int code_area_max = 0;
static CodeArea * code_area_buf = NULL;

static const char * LINENUMBERS = "LineNumbers";

static void write_line_info(OutputStream * out, int cnt) {
    CodeArea * area = code_area_buf + cnt;
    CodeArea * prev = cnt == 0 ? NULL : code_area_buf + cnt - 1;

    write_stream(out, '{');
    json_write_string(out, "SAddr");
    write_stream(out, ':');
    json_write_uint64(out, area->start_address);
    if (area->start_line > 0) {
        write_stream(out, ',');
        json_write_string(out, "SLine");
        write_stream(out, ':');
        json_write_ulong(out, area->start_line);
        if (area->start_column > 0) {
            write_stream(out, ',');
            json_write_string(out, "SCol");
            write_stream(out, ':');
            json_write_ulong(out, area->start_column);
        }
    }
    if (area->end_address != 0) {
        write_stream(out, ',');
        json_write_string(out, "EAddr");
        write_stream(out, ':');
        json_write_uint64(out, area->end_address);
    }
    if (area->end_line > 0) {
        write_stream(out, ',');
        json_write_string(out, "ELine");
        write_stream(out, ':');
        json_write_ulong(out, area->end_line);
        if (area->end_column > 0) {
            write_stream(out, ',');
            json_write_string(out, "ECol");
            write_stream(out, ':');
            json_write_ulong(out, area->end_column);
        }
    }
    if (area->file != NULL && (prev == NULL || prev->file != area->file)) {
        write_stream(out, ',');
        json_write_string(out, "File");
        write_stream(out, ':');
        json_write_string(out, area->file);
    }
    if (area->directory != NULL && (prev == NULL || prev->directory != area->directory)) {
        write_stream(out, ',');
        json_write_string(out, "Dir");
        write_stream(out, ':');
        json_write_string(out, area->directory);
    }
    if (area->isa > 0) {
        write_stream(out, ',');
        json_write_string(out, "ISA");
        write_stream(out, ':');
        json_write_ulong(out, area->isa);
    }
    if (area->is_statement) {
        write_stream(out, ',');
        json_write_string(out, "IsStmt");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }
    if (area->basic_block) {
        write_stream(out, ',');
        json_write_string(out, "BasicBlock");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }
    if (area->prologue_end) {
        write_stream(out, ',');
        json_write_string(out, "PrologueEnd");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }
    if (area->epilogue_begin) {
        write_stream(out, ',');
        json_write_string(out, "EpilogueBegin");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }
    write_stream(out, '}');
}

static void add_code_area(CodeArea * area, void * args) {
    CodeArea * buf = NULL;
    if (code_area_cnt >= code_area_max) {
        code_area_max += 8;
        code_area_buf = (CodeArea *)loc_realloc(code_area_buf, sizeof(CodeArea) * code_area_max);
    }
    buf = code_area_buf + code_area_cnt++;
    memcpy(buf, area, sizeof(CodeArea));
}

static void map_to_source_cache_client(void * x) {
    int err = 0;
    Context * ctx = NULL;
    MapToSourceArgs * args = (MapToSourceArgs *)x;
    Channel * c = cache_channel();

    ctx = id2ctx(args->id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    code_area_cnt = 0;
    if (err == 0 && address_to_line(ctx, args->addr0, args->addr1, add_code_area, NULL) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);
    if (err != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        write_stream(&c->out, '[');
        while (cnt < code_area_cnt) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_line_info(&c->out, cnt);
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_map_to_source(char * token, Channel * c) {
    MapToSourceArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.addr0 = (ContextAddress)json_read_uint64(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.addr1 = (ContextAddress)json_read_uint64(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(map_to_source_cache_client, c, &args, sizeof(args));
}

static void map_to_memory_cache_client(void * x) {
    int err = 0;
    Context * ctx = NULL;
    MapToMemoryArgs * args = (MapToMemoryArgs *)x;
    Channel * c = cache_channel();

    ctx = id2ctx(args->id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    code_area_cnt = 0;
    if (err == 0 && line_to_address(ctx, args->file,
        args->line, args->column, add_code_area, NULL) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);
    if (err != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        write_stream(&c->out, '[');
        while (cnt < code_area_cnt) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_line_info(&c->out, cnt);
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
    loc_free(args->file);
}

static void command_map_to_memory(char * token, Channel * c) {
    MapToMemoryArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.file = json_read_alloc_string(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.line = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.column = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(map_to_memory_cache_client, c, &args, sizeof(args));
}

void ini_line_numbers_service(Protocol * proto) {
    add_command_handler(proto, LINENUMBERS, "mapToSource", command_map_to_source);
    add_command_handler(proto, LINENUMBERS, "mapToMemory", command_map_to_memory);
}

#endif /* SERVICE_LineNumbers */

