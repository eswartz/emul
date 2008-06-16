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
 * TCF communication protocol.
 * This module handles registration of command and event handlers.
 * It is called when new messages are received and will dispatch
 * messages to the appropriate handler. It has no knowledge of what transport
 * protocol is used and what services do.
 */

#include "mdep.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <assert.h>
#include "protocol.h"
#include "trace.h"
#include "events.h"
#include "events.h"
#include "exceptions.h"
#include "json.h"
#include "myalloc.h"

#define REFRESH_TIME            10
#define STALE_TIME_DELTA        (REFRESH_TIME*3)

static const char * LOCATOR = "Locator";

struct ServiceInfo {
    void * owner;
    const char * name;
    struct ServiceInfo * next;
};

struct MessageHandlerInfo {
    Protocol * p;
    ServiceInfo * service;
    const char * name;
    ProtocolCommandHandler handler;
    struct MessageHandlerInfo * next;
};

typedef struct MessageHandlerInfo MessageHandlerInfo;

struct EventHandlerInfo {
    Channel * c;
    ServiceInfo * service;
    const char * name;
    ProtocolEventHandler handler;
    struct EventHandlerInfo * next;
};

typedef struct EventHandlerInfo EventHandlerInfo;

struct ReplyHandlerInfo {
    unsigned long tokenid;
    Channel * c;
    ReplyHandlerCB handler;
    void * client_data;
    struct ReplyHandlerInfo * next;
};

#define MESSAGE_HASH_SIZE 127
#define EVENT_HASH_SIZE 127
#define REPLY_HASH_SIZE 127

static MessageHandlerInfo * message_handlers[MESSAGE_HASH_SIZE];
static EventHandlerInfo * event_handlers[EVENT_HASH_SIZE];
static ReplyHandlerInfo * reply_handlers[REPLY_HASH_SIZE];
static ServiceInfo * services;

struct Protocol {
    int lock_cnt;           /* Lock count, cannot delete when > 0 */
    unsigned long tokenid;
    ProtocolMessageHandler default_handler;
};

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

ServiceInfo * protocol_get_service(void * owner, const char * name) {
    ServiceInfo * s = services;

    while (s != NULL && (s->owner != owner || strcmp(s->name, name) != 0)) s = s->next;
    if (s == NULL) {
        s = (ServiceInfo *)loc_alloc(sizeof(ServiceInfo));
        s->owner = owner;
        s->name = name;
        s->next = services;
        services = s;
    }
    return s;
}

static void free_services(void * owner) {
    ServiceInfo ** sp = &services;
    ServiceInfo * s;

    while ((s = *sp) != NULL) {
        if (s->owner == owner) {
            *sp = s->next;
            loc_free(s);
        }
        else {
            sp = &s->next;
        }
    }
}

static unsigned message_hash(Protocol * p, const char * service, const char * name) {
    int i;
    unsigned h = (unsigned)p;
    for (i = 0; service[i]; i++) h += service[i];
    for (i = 0; name[i]; i++) h += name[i];
    h = h + h / MESSAGE_HASH_SIZE;
    return h % MESSAGE_HASH_SIZE;
}

static MessageHandlerInfo * find_message_handler(Protocol * p, char * service, char * name) {
    MessageHandlerInfo * mh = message_handlers[message_hash(p, service, name)];
    while (mh != NULL) {
        if (mh->p == p && !strcmp(mh->service->name, service) && !strcmp(mh->name, name)) return mh;
        mh = mh->next;
    }
    return NULL;
}

static unsigned event_hash(Channel * c, const char * service, const char * name) {
    int i;
    unsigned h = (unsigned)c;
    for (i = 0; service[i]; i++) h += service[i];
    for (i = 0; name[i]; i++) h += name[i];
    h = h + h / EVENT_HASH_SIZE;
    return h % EVENT_HASH_SIZE;
}

static EventHandlerInfo * find_event_handler(Channel * c, char * service, char * name) {
    EventHandlerInfo * mh = event_handlers[event_hash(c, service, name)];
    while (mh != NULL) {
        if (mh->c == c && !strcmp(mh->service->name, service) && !strcmp(mh->name, name)) return mh;
        mh = mh->next;
    }
    return NULL;
}

#define reply_hash(c, tokenid) (((unsigned)(c)+(unsigned)(tokenid)) % REPLY_HASH_SIZE)

static ReplyHandlerInfo * find_reply_handler(Channel * c, unsigned long tokenid, int take) {
    ReplyHandlerInfo ** rhp = &reply_handlers[reply_hash(c, tokenid)];
    ReplyHandlerInfo * rh;
    while ((rh = *rhp) != NULL) {
        if (rh->c == c && rh->tokenid == tokenid) {
            if (take) {
                *rhp = rh->next;
            }
            return rh;
        }
        rhp = &rh->next;
    }
    return NULL;
}

void handle_protocol_message(Protocol * p, Channel * c) {
    char type[8];
    char token[256];
    char service[256];
    char name[256];
    char * args[4];

    assert(is_dispatch_thread());

    read_stringz(&c->inp, type, sizeof(type));
    if (strlen(type) != 1) {
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        exception(ERR_PROTOCOL);
    }
    else if (type[0] == 'C') {
        Trap trap;
        MessageHandlerInfo * mh;
        read_stringz(&c->inp, token, sizeof(token));
        read_stringz(&c->inp, service, sizeof(service));
        read_stringz(&c->inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Command: C %s %s %s ...", token, service, name);
        mh = find_message_handler(p, service, name);
        if (mh == NULL) {
            if (p->default_handler != NULL) {
                args[0] = type;
                args[1] = token;
                args[2] = service;
                args[3] = name;
                p->default_handler(c, args, 4);
                return;
            }
            trace(LOG_ALWAYS, "Unsupported TCF command: %s %s ...", service, name);
            exception(ERR_PROTOCOL);
        }
        if (set_trap(&trap)) {
            mh->handler(token, c);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling command %s.%s: %d %s",
                service, name, trap.error, errno_to_str(trap.error));
            exception(trap.error);
        }
    }
    else if (type[0] == 'R') {
        Trap trap;
        char *endptr;
        unsigned long tokenid;
        ReplyHandlerInfo *rh;
        read_stringz(&c->inp, token, sizeof(token));
        trace(LOG_PROTOCOL, "Reply: R %s ...", token);
        errno = 0;
        tokenid = strtoul(token, &endptr, 10);
        if (errno != 0 || *endptr != '\0' ||
           (rh = find_reply_handler(c, tokenid, 1)) == NULL) {
            if (p->default_handler != NULL) {
                args[0] = type;
                args[1] = token;
                p->default_handler(c, args, 2);
                return;
            }
            trace(LOG_ALWAYS, "Reply with unexpected token: %s", token);
            exception(ERR_PROTOCOL);
        }
        if (set_trap(&trap)) {
            rh->handler(c, rh->client_data, 0);
            loc_free(rh);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling reply %ul: %d %s",
                  rh->tokenid, trap.error, errno_to_str(trap.error));
            loc_free(rh);
            exception(trap.error);
        }
    }
    else if (type[0] == 'E') {
        Trap trap;
        EventHandlerInfo * eh;
        read_stringz(&c->inp, service, sizeof(service));
        read_stringz(&c->inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Event: E %s %s ...", service, name);
        eh = find_event_handler(c, service, name);
        if (eh == NULL && p->default_handler != NULL) {
            args[0] = type;
            args[1] = service;
            args[2] = name;
            p->default_handler(c, args, 3);
            return;
        }
        if (set_trap(&trap)) {
            if (eh != NULL) {
                eh->handler(c);
            }
            else {
                /* Eat the body of the event */
                int ch;
                while ((ch = c->inp.read(&c->inp)) != MARKER_EOM);
            }
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
        int ch = c->inp.read(&c->inp);
        if (ch == '-') {
            s = 1;
            ch = c->inp.read(&c->inp);
        }
        while (ch >= '0' && ch <= '9') {
            n = n * 10 + (ch - '0');
            ch = c->inp.read(&c->inp);
        }
        if (ch == 0) {
            ch = c->inp.read(&c->inp);
        }
        else {
            trace(LOG_ALWAYS, "Received F with no zero termination.");
        }
        if (ch != MARKER_EOM) exception(ERR_PROTOCOL);
        c->congestion_level = s ? -n : n;
    }
    else {
        if (p->default_handler != NULL) {
            args[0] = type;
            p->default_handler(c, args, 1);
            return;
        }
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        exception(ERR_PROTOCOL);
    }
}

void set_default_message_handler(Protocol *p, ProtocolMessageHandler handler) {
    p->default_handler = handler;
}

void add_command_handler(Protocol * p, const char * service, const char * name, ProtocolCommandHandler handler) {
    unsigned h = message_hash(p, service, name);
    MessageHandlerInfo * mh = (MessageHandlerInfo *)loc_alloc(sizeof(MessageHandlerInfo));
    mh->p = p;
    mh->service = protocol_get_service(p, service);
    mh->name = name;
    mh->handler = handler;
    mh->next = message_handlers[h];
    message_handlers[h] = mh;
}

void add_event_handler(Channel * c, const char * service, const char * name, ProtocolEventHandler handler) {
    unsigned h = event_hash(c, service, name);
    EventHandlerInfo * eh = (EventHandlerInfo *)loc_alloc(sizeof(EventHandlerInfo));
    eh->c = c;
    eh->service = protocol_get_service(c, service);
    eh->name = name;
    eh->handler = handler;
    eh->next = event_handlers[h];
    event_handlers[h] = eh;
}

ReplyHandlerInfo * protocol_send_command(Protocol * p, Channel * c, const char *service, const char *name, ReplyHandlerCB handler, void *client_data) {
    ReplyHandlerInfo *rh;
    int h;
    unsigned long tokenid;
    char token[256];

    do tokenid = p->tokenid++;
    while (find_reply_handler(c, tokenid, 0) != NULL);
    sprintf(token, "%lu", tokenid);
    write_stringz(&c->out, "C");
    write_stringz(&c->out, token);
    write_stringz(&c->out, service);
    write_stringz(&c->out, name);
    rh = loc_alloc(sizeof *rh);
    rh->tokenid = tokenid;
    rh->c = c;
    rh->handler = handler;
    rh->client_data = client_data;
    h = reply_hash(c, tokenid);
    rh->next = reply_handlers[h];
    reply_handlers[h] = rh;
    return rh;
}

void send_hello_message(Protocol * p, Channel * c) {
    ServiceInfo * s = services;
    int cnt = 0;

    write_stringz(&c->out, "E");
    write_stringz(&c->out, LOCATOR);
    write_stringz(&c->out, "Hello");
    c->out.write(&c->out, '[');
    while (s) {
        if (s->owner == p) {
            if (cnt != 0) c->out.write(&c->out, ',');
            json_write_string(&c->out, s->name);
            cnt++;
        }
        s = s->next;
    }
    c->out.write(&c->out, ']');
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_sync(char * token, Channel * c) {
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void command_redirect(char * token, Channel * c) {
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    if (c->redirecting != NULL) {
        c->redirecting(c, token, id);
        return;
    }
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, ERR_UNSUPPORTED);
    c->out.write(&c->out, MARKER_EOM);
}

static PeerServer * read_peer_properties(InputStream * inp) {
    PeerServer * ps;

    if (inp->read(inp) != '{') exception(ERR_JSON_SYNTAX);
    ps = peer_server_alloc();
    if (inp->peek(inp) == '}') {
        inp->read(inp);
        return ps;
    }
    while (1) {
        int ch;
        char *name;
        char *value;

        name = json_read_alloc_string(inp);
        if (inp->read(inp) != ':') {
            loc_free(name);
            exception(ERR_JSON_SYNTAX);
        }
        value = json_read_alloc_string(inp);
        peer_server_addprop(ps, name, value);
        ch = inp->read(inp);
        if (ch == ',') continue;
        if (ch == '}') break;
        peer_server_free(ps);
        exception(ERR_JSON_SYNTAX);
    }
    return ps;
}

static void command_publish_peer(char * token, Channel * c) {
    PeerServer * ps = read_peer_properties(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    ps->flags |= PS_FLAG_DISCOVERABLE;
    peer_server_add(ps, STALE_TIME_DELTA);
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    json_write_ulong(&c->out, REFRESH_TIME);
    c->out.write(&c->out, 0);
    c->out.write(&c->out, MARKER_EOM);
}

static void event_locator_hello(Channel * c) {
    int max;
    int cnt;
    char **list;

    if (c->inp.read(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (c->inp.peek(&c->inp) == ']') {
        c->inp.read(&c->inp);
        cnt = 0;
        list = NULL;
    }
    else {
        max = 1;
        cnt = 0;
        list = loc_alloc(max * sizeof *list);
        while (1) {
            char ch;
            char service[256];
            json_read_string(&c->inp, service, sizeof(service));
            if (cnt == max) {
                max *= 2;
                list = loc_realloc(list, max * sizeof *list);
            }
            list[cnt++] = loc_strdup(service);
            ch = c->inp.read(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            while (cnt > 0) loc_free(list[--cnt]);
            loc_free(list);
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (c->inp.read(&c->inp) != 0 || c->inp.read(&c->inp) != MARKER_EOM) {
        while (cnt > 0) loc_free(list[--cnt]);
        loc_free(list);
        exception(ERR_JSON_SYNTAX);
    }
    if (c->peer_service_list != NULL) {
        loc_free(c->peer_service_list);
    }
    c->peer_service_cnt = cnt;
    c->peer_service_list = list;
    c->connected(c);
}

void event_locator_peer_added(Channel * c) {
    PeerServer * ps = read_peer_properties(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    if (ps->id == NULL) exception(ERR_JSON_SYNTAX);
    trace(LOG_DISCOVERY, "event_locator_peer_added: %s ...", ps->id);
    ps->flags |= PS_FLAG_DISCOVERABLE;
    peer_server_add(ps, 0);
}

void event_locator_peer_changed(Channel * c) {
    PeerServer * ps = read_peer_properties(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    if (ps->id == NULL) exception(ERR_JSON_SYNTAX);
    trace(LOG_DISCOVERY, "event_locator_peer_changed: %s ...", ps->id);
    ps->flags |= PS_FLAG_DISCOVERABLE;
    peer_server_add(ps, 0);
}

void event_locator_peer_removed(Channel * c) {
    char id[256];
    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    trace(LOG_DISCOVERY, "event_locator_peer_removed: %s ...", id);
    peer_server_remove(id);
}

int protocol_cancel_command(Protocol * p, ReplyHandlerInfo * rh) {
    return 0;
}

void protocol_channel_opened(Protocol * p, Channel * c) {
    add_event_handler(c, LOCATOR, "Hello", event_locator_hello);
}

void protocol_channel_closed(Protocol * p, Channel * c) {
    int i;
    int cnt;
    char ** list;

    assert(is_dispatch_thread());
    for (i = 0; i < EVENT_HASH_SIZE; i++) {
        EventHandlerInfo ** ehp = &event_handlers[i];
        EventHandlerInfo * eh;

        while ((eh = *ehp) != NULL) {
            if (eh->c == c) {
                *ehp = eh->next;
                loc_free(eh);
            }
            else {
                ehp = &eh->next;
            }
        }
    }
    free_services(c);

    for (i = 0; i < REPLY_HASH_SIZE; i++) {
        ReplyHandlerInfo ** rhp = &reply_handlers[i];
        ReplyHandlerInfo * rh;
        while ((rh = *rhp) != NULL) {
            if (rh->c == c) {
                Trap trap;
                *rhp = rh->next;
                if (set_trap(&trap)) {
                    rh->handler(c, rh->client_data, ERR_CHANNEL_CLOSED);
                    loc_free(rh);
                    clear_trap(&trap);
                }
                else {
                    trace(LOG_ALWAYS, "Exception handling reply %ul: %d %s",
                          rh->tokenid, trap.error, errno_to_str(trap.error));
                    loc_free(rh);
                }
            }
            rhp = &rh->next;
        }
    }
    cnt = c->peer_service_cnt;
    list = c->peer_service_list;
    if (list) {
        c->peer_service_list = NULL;
        while (cnt > 0) loc_free(list[--cnt]);
        loc_free(list);
    }
}

Protocol * protocol_alloc(void) {
    Protocol * p = loc_alloc_zero(sizeof *p);

    assert(is_dispatch_thread());
    p->lock_cnt = 1;
    p->tokenid = 1;
    return p;
}

void protocol_reference(Protocol * p) {
    assert(is_dispatch_thread());
    assert(p->lock_cnt > 0);
    p->lock_cnt++;
}

void protocol_release(Protocol * p) {
    MessageHandlerInfo ** mhp;
    MessageHandlerInfo * mh;
    int i;

    assert(is_dispatch_thread());
    assert(p->lock_cnt > 0);
    if (--p->lock_cnt != 0) return;
    for (i = 0; i < MESSAGE_HASH_SIZE; i++) {
        mhp = &message_handlers[i];
        while ((mh = *mhp) != NULL) {
            if (mh->p == p) {
                *mhp = mh->next;
                loc_free(mh);
            }
            else {
                mhp = &mh->next;
            }
        }
    }
    free_services(p);
}

void ini_locator_service(Protocol * p) {
    add_command_handler(p, LOCATOR, "sync", command_sync);
    add_command_handler(p, LOCATOR, "redirect", command_redirect);
    add_command_handler(p, LOCATOR, "publishPeer", command_publish_peer);
}
