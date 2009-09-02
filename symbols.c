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

static const char * SYMBOLS = "Symbols";

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    Symbol sym;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2symbol(id, &sym) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (err == 0) {
        char * name = NULL;
        int type_class = TYPE_CLASS_UNKNOWN;
        Symbol type;
        size_t size = 0;
        void * value = NULL;
        unsigned long length = 0;
        unsigned long offset = 0;
        ContextAddress address = 0;
        int frame = STACK_NO_FRAME; /* TODO: symbol frame */

        write_stream(&c->out, '{');

        json_write_string(&c->out, "ID");
        write_stream(&c->out, ':');
        json_write_string(&c->out, id);
        write_stream(&c->out, ',');

        json_write_string(&c->out, "ExeID");
        write_stream(&c->out, ':');
        json_write_string(&c->out, container_id(sym.ctx));
        write_stream(&c->out, ',');

        if (get_symbol_name(&sym, &name) == 0 && name != NULL) {
            json_write_string(&c->out, "Name");
            write_stream(&c->out, ':');
            json_write_string(&c->out, name);
            write_stream(&c->out, ',');
            loc_free(name);
        }

        if (get_symbol_type_class(&sym, &type_class) == 0 && type_class != TYPE_CLASS_UNKNOWN) {
            json_write_string(&c->out, "TypeClass");
            write_stream(&c->out, ':');
            json_write_long(&c->out, type_class);
            write_stream(&c->out, ',');
        }

        if (get_symbol_type(&sym, &type) == 0) {
            json_write_string(&c->out, "TypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(&type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_base_type(&sym, &type) == 0) {
            json_write_string(&c->out, "BaseTypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(&type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_index_type(&sym, &type) == 0) {
            json_write_string(&c->out, "IndexTypeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(&type));
            write_stream(&c->out, ',');
        }

        if (get_symbol_size(&sym, frame, &size) == 0) {
            json_write_string(&c->out, "Size");
            write_stream(&c->out, ':');
            json_write_long(&c->out, size);
            write_stream(&c->out, ',');
        }

        if (get_symbol_length(&sym, frame, &length) == 0) {
            json_write_string(&c->out, "Length");
            write_stream(&c->out, ':');
            json_write_long(&c->out, length);
            write_stream(&c->out, ',');
        }

        if (sym.sym_class == SYM_CLASS_REFERENCE) {
            if (get_symbol_offset(&sym, &offset) == 0) {
                json_write_string(&c->out, "Offset");
                write_stream(&c->out, ':');
                json_write_long(&c->out, offset);
                write_stream(&c->out, ',');
            }

            if (get_symbol_address(&sym, frame, &address) == 0) {
                json_write_string(&c->out, "Address");
                write_stream(&c->out, ':');
                json_write_long(&c->out, address);
                write_stream(&c->out, ',');
            }
        }

        if (sym.sym_class == SYM_CLASS_VALUE && get_symbol_value(&sym, &value, &size) == 0) {
            json_write_string(&c->out, "Value");
            write_stream(&c->out, ':');
            json_write_binary(&c->out, value, size);
            write_stream(&c->out, ',');
        }

        json_write_string(&c->out, "Class");
        write_stream(&c->out, ':');
        json_write_long(&c->out, sym.sym_class);

        write_stream(&c->out, '}');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    int err = 0;
    char id[256];
    Symbol sym;
    Symbol * list = NULL;
    int cnt = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2symbol(id, &sym) < 0) err = errno;
    if (err == 0 && get_symbol_children(&sym, &list, &cnt) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (err == 0) {
        int i;
        write_stream(&c->out, '[');
        for (i = 0; i < cnt; i++) {
            if (i > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, symbol2id(list + i));
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(list);
}

extern void ini_symbols_lib(void);

void ini_symbols_service(Protocol * proto) {
    ini_symbols_lib();
    add_command_handler(proto, SYMBOLS, "getContext", command_get_context);
    add_command_handler(proto, SYMBOLS, "getChildren", command_get_children);
}

#endif /* SERVICE_Symbols */


