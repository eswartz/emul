/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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


#include "config.h"

#if SERVICE_Symbols

#include "channel.h"
#include "json.h"
#include "myalloc.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "symbols.h"
#include "cache.h"

static const char * SYMBOLS = "Symbols";

typedef struct CommandGetContextArgs {
    char token[256];
    char id[256];
} CommandGetContextArgs;

static void command_get_context_cache_client(void * x) {
    CommandGetContextArgs * args = (CommandGetContextArgs *)x;
    Channel * c = cache_channel();
    int err = 0;
    Symbol * sym = NULL;

    if (id2symbol(args->id, &sym) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);

    if (err == 0) {
        char * name = NULL;
        int sym_class = SYM_CLASS_UNKNOWN;
        int type_class = TYPE_CLASS_UNKNOWN;
        Symbol * type = NULL;
        ContextAddress size = 0;
        ContextAddress length = 0;
        ContextAddress offset = 0;
        ContextAddress address = 0;

        get_symbol_class(sym, &sym_class);

        write_stream(&c->out, '{');

        json_write_string(&c->out, "ID");
        write_stream(&c->out, ':');
        json_write_string(&c->out, args->id);
        write_stream(&c->out, ',');

        if (get_symbol_name(sym, &name) == 0 && name != NULL) {
            json_write_string(&c->out, "Name");
            write_stream(&c->out, ':');
            json_write_string(&c->out, name);
            write_stream(&c->out, ',');
        }

        if (get_symbol_type_class(sym, &type_class) == 0 && type_class != TYPE_CLASS_UNKNOWN) {
            json_write_string(&c->out, "TypeClass");
            write_stream(&c->out, ':');
            json_write_long(&c->out, type_class);
            write_stream(&c->out, ',');
        }

        if (get_symbol_type(sym, &type) == 0 && type != NULL) {
            json_write_string(&c->out, "TypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_base_type(sym, &type) == 0 && type != NULL) {
            json_write_string(&c->out, "BaseTypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_index_type(sym, &type) == 0 && type != NULL) {
            json_write_string(&c->out, "IndexTypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_size(sym, &size) == 0) {
            json_write_string(&c->out, "Size");
            write_stream(&c->out, ':');
            json_write_int64(&c->out, size);
            write_stream(&c->out, ',');
        }

        if (get_symbol_length(sym, &length) == 0) {
            json_write_string(&c->out, "Length");
            write_stream(&c->out, ':');
            json_write_int64(&c->out, length);
            write_stream(&c->out, ',');

            if (get_symbol_lower_bound(sym, &offset) == 0) {
                json_write_string(&c->out, "LowerBound");
                write_stream(&c->out, ':');
                json_write_int64(&c->out, offset);
                write_stream(&c->out, ',');

                json_write_string(&c->out, "UpperBound");
                write_stream(&c->out, ':');
                json_write_int64(&c->out, offset + length - 1);
                write_stream(&c->out, ',');
            }
        }

        if (sym_class == SYM_CLASS_REFERENCE) {
            if (get_symbol_offset(sym, &offset) == 0) {
                json_write_string(&c->out, "Offset");
                write_stream(&c->out, ':');
                json_write_int64(&c->out, offset);
                write_stream(&c->out, ',');
            }
        }

        if (sym_class == SYM_CLASS_REFERENCE || sym_class == SYM_CLASS_FUNCTION) {
            if (get_symbol_address(sym, &address) == 0) {
                json_write_string(&c->out, "Address");
                write_stream(&c->out, ':');
                json_write_int64(&c->out, address);
                write_stream(&c->out, ',');
            }
        }

        if (sym_class == SYM_CLASS_VALUE) {
            void * value = NULL;
            size_t value_size = 0;
            if (get_symbol_value(sym, &value, &value_size) == 0 && value != NULL) {
                json_write_string(&c->out, "Value");
                write_stream(&c->out, ':');
                json_write_binary(&c->out, value, value_size);
                write_stream(&c->out, ',');
            }
        }

        json_write_string(&c->out, "Class");
        write_stream(&c->out, ':');
        json_write_long(&c->out, sym_class);

        write_stream(&c->out, '}');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(args);
}

static void command_get_context(char * token, Channel * c) {
    CommandGetContextArgs * args = (CommandGetContextArgs *)loc_alloc_zero(sizeof(CommandGetContextArgs));

    json_read_string(&c->inp, args->id, sizeof(args->id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strncpy(args->token, token, sizeof(args->token) - 1);
    cache_enter(command_get_context_cache_client, c, args);
}

typedef struct CommandGetChildrenArgs {
    char token[256];
    char id[256];
} CommandGetChildrenArgs;

static void command_get_children_cache_client(void * x) {
    CommandGetChildrenArgs * args = (CommandGetChildrenArgs *)x;
    Channel * c = cache_channel();
    int err = 0;
    Symbol * sym = NULL;
    Symbol ** list = NULL;
    int cnt = 0;

    if (id2symbol(args->id, &sym) < 0) err = errno;
    if (err == 0 && get_symbol_children(sym, &list, &cnt) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);

    if (err == 0) {
        int i;
        write_stream(&c->out, '[');
        for (i = 0; i < cnt; i++) {
            if (i > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, symbol2id(list[i]));
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(args);
}

static void command_get_children(char * token, Channel * c) {
    CommandGetChildrenArgs * args = (CommandGetChildrenArgs *)loc_alloc_zero(sizeof(CommandGetChildrenArgs));

    json_read_string(&c->inp, args->id, sizeof(args->id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strncpy(args->token, token, sizeof(args->token) - 1);
    cache_enter(command_get_children_cache_client, c, args);
}

typedef struct CommandFindArgs {
    char token[256];
    char id[256];
    char * name;
} CommandFindArgs;

static void command_find_cache_client(void * x) {
    CommandFindArgs * args = (CommandFindArgs *)x;
    Channel * c = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    Symbol * sym = NULL;
    int err = 0;

    ctx = id2ctx(args->id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    /* TODO: stack frame ID */

    if (err == 0 && find_symbol(ctx, frame, args->name, &sym) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);

    if (err == 0) {
        json_write_string(&c->out, symbol2id(sym));
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(args->name);
    loc_free(args);
}

static void command_find(char * token, Channel * c) {
    CommandFindArgs * args = (CommandFindArgs *)loc_alloc_zero(sizeof(CommandFindArgs));

    json_read_string(&c->inp, args->id, sizeof(args->id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args->name = json_read_alloc_string(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strncpy(args->token, token, sizeof(args->token) - 1);
    cache_enter(command_find_cache_client, c, args);
}

void ini_symbols_service(Protocol * proto) {
    static int ini_done = 0;
    if (!ini_done) {
        ini_symbols_lib();
        ini_done = 1;
    }
    add_command_handler(proto, SYMBOLS, "getContext", command_get_context);
    add_command_handler(proto, SYMBOLS, "getChildren", command_get_children);
    add_command_handler(proto, SYMBOLS, "find", command_find);
}

#endif /* SERVICE_Symbols */


