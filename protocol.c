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
 * TCF communication protocol.
 * This module handles registration of command and event handlers.
 * It is called when new messages are received and will dispatch
 * messages to the appropriate handler. It has no knowledge of what transport
 * protocol is used and what services do.
 */

#include <stdlib.h>
#include <string.h>
#include "protocol.h"
#include "trace.h"
#include "exceptions.h"
#include "json.h"
#include "myalloc.h"

static const char * LOCATOR = "Locator";

struct ServiceInfo {
    const char * name;
    struct ServiceInfo * next;
};

typedef struct ServiceInfo ServiceInfo;

struct MessageHandlerInfo {
    char type;
    ServiceInfo * service;
    const char * name;
    MessageHandler handler;
    struct MessageHandlerInfo * next;
};

typedef struct MessageHandlerInfo MessageHandlerInfo;

#define MESSAGE_HASH_SIZE 127
static MessageHandlerInfo * message_handlers[MESSAGE_HASH_SIZE];
static ServiceInfo * services = NULL;

int congestion_level = 0;

static void read_stringz(InputStream * inp, char * str, size_t size) {
    unsigned len = 0;
    for (;;) {
        int ch = inp->read(inp);
        if (ch <= 0) break;
        if (len < size - 1) str[len] = ch;
        len++;
    }
    str[len] = 0;
}

static int message_hash(const char * service, const char * name) {
    int i;
    int h = 0;
    for (i = 0; service[i]; i++) h += service[i];
    for (i = 0; name[i]; i++) h += name[i];
    h = h + h / MESSAGE_HASH_SIZE;
    return h % MESSAGE_HASH_SIZE;
}

static MessageHandlerInfo * find_message_handler(char type, char * service, char * name) {
    MessageHandlerInfo * c = message_handlers[message_hash(service, name)];
    while (c != NULL) {
        if (c->type == type && !strcmp(c->service->name, service) && !strcmp(c->name, name)) return c;
        c = c->next;
    }
    trace(LOG_ALWAYS, "Unsupported TCF message: %c %s %s ...", type, service, name);
    exception(ERR_PROTOCOL);
    return NULL;
}

void handle_protocol_message(InputStream * inp, OutputStream * out) {
    char type[8];
    char token[256];
    char service[256];
    char name[256];
    read_stringz(inp, type, sizeof(type));
    if (strlen(type) != 1) {
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        exception(ERR_PROTOCOL);
    }
    else if (type[0] == 'C') {
        Trap trap;
        MessageHandlerInfo * c;
        read_stringz(inp, token, sizeof(token));
        read_stringz(inp, service, sizeof(service));
        read_stringz(inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Command: C %s %s %s ...", token, service, name);
        c = find_message_handler('C', service, name);
        if (set_trap(&trap)) {
            c->handler(token, inp, out);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling command %s.%s: %d %s",
                service, name, trap.error, errno_to_str(trap.error));
            exception(trap.error);
        }
    }
    else if (type[0] == 'E') {
        Trap trap;
        MessageHandlerInfo * c;
        read_stringz(inp, service, sizeof(service));
        read_stringz(inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Event: E %s %s ...", service, name);
        c = find_message_handler('E', service, name);
        if (set_trap(&trap)) {
            c->handler(NULL, inp, out);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling event %s.%s: %d %s",
                service, name, trap.error, errno_to_str(trap.error));
            exception(trap.error);
        }
    }
    else if (type[0] == 'F') {
        int n = 0;
        int s = 0;
        int ch = inp->read(inp);
        if (ch == '-') {
            s = 1;
            ch = inp->read(inp);
        }
        while (ch >= '0' && ch <= '9') {
            n = n * 10 + (ch - '0');
            ch = inp->read(inp);
        }
        if (ch != MARKER_EOM) exception(ERR_PROTOCOL);
        congestion_level = n;
    }
    else {
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        exception(ERR_PROTOCOL);
    }
}

static void add_message_handler(const char type, const char * service, const char * name, MessageHandler handler) {
    int h = message_hash(service, name);
    ServiceInfo * s = services;
    MessageHandlerInfo * c = (MessageHandlerInfo *)loc_alloc(sizeof(MessageHandlerInfo));
    while (s != NULL && strcmp(s->name, service) != 0) s = s->next;
    if (s == NULL) {
        s = (ServiceInfo *)loc_alloc(sizeof(ServiceInfo));
        s->name = service;
        s->next = services;
        services = s;
    }
    c->type = type;
    c->service = s;
    c->name = name;
    c->handler = handler;
    c->next = message_handlers[h];
    message_handlers[h] = c;
}

void add_command_handler(const char * service, const char * name, MessageHandler handler) {
    add_message_handler('C', service, name, handler);
}

void add_event_handler(const char * service, const char * name, MessageHandler handler) {
    add_message_handler('E', service, name, handler);
}

void send_hello_message(OutputStream * out) {
    ServiceInfo * s = services;
    write_stringz(out, "E");
    write_stringz(out, LOCATOR);
    write_stringz(out, "Hello");
    out->write(out, '[');
    while (s) {
        if (s != services) out->write(out, ',');
        json_write_string(out, s->name);
        s = s->next;
    }
    out->write(out, ']');
    out->write(out, 0);
    out->write(out, MARKER_EOM);
}

static void command_sync(char * token, InputStream * inp, OutputStream * out) {
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, 0);
    out->write(out, MARKER_EOM);
}

void ini_protocol(void) {
    add_command_handler(LOCATOR, "sync", command_sync);
}


