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
 * TCF communication protocol.
 * This module handles registration of command and event handlers.
 * It is called when new messages are received and will dispatch
 * messages to the appropriate handler. It has no knowledge of what transport
 * protocol is used and what services do.
 */

#include <config.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <assert.h>
#include <framework/protocol.h>
#include <framework/trace.h>
#include <framework/events.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/json.h>
#include <framework/myalloc.h>

static const char * LOCATOR = "Locator";

struct ServiceInfo {
    void * owner;
    char * name;
    struct ServiceInfo * next;
};

struct MessageHandlerInfo {
    Protocol * p;
    ServiceInfo * service;
    const char * name;
    ProtocolCommandHandler2 handler;
    void * client_data;
    struct MessageHandlerInfo * next;
};

typedef struct MessageHandlerInfo MessageHandlerInfo;

struct EventHandlerInfo {
    Channel * c;
    ServiceInfo * service;
    const char * name;
    ProtocolEventHandler2 handler;
    void * client_data;
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
static int ini_done = 0;

struct Protocol {
    int lock_cnt;           /* Lock count, cannot delete when > 0 */
    unsigned long tokenid;
    ProtocolMessageHandler2 default_handler;
    void * client_data;
};

static void read_stringz(InputStream * inp, char * str, size_t size) {
    unsigned len = 0;
    for (;;) {
        int ch = read_stream(inp);
        if (ch == 0) break;
        if (ch < 0) {
            trace(LOG_ALWAYS, "Unexpected end of message");
            exception(ERR_PROTOCOL);
        }
        if (len < size - 1) str[len++] = (char)ch;
    }
    str[len] = 0;
}

ServiceInfo * protocol_get_service(void * owner, const char * name) {
    ServiceInfo * s = services;

    while (s != NULL && (s->owner != owner || strcmp(s->name, name) != 0)) s = s->next;
    if (s == NULL) {
        assert(strcmp(name, "ZeroCopy") != 0);
        s = (ServiceInfo *)loc_alloc(sizeof(ServiceInfo));
        s->owner = owner;
        s->name = loc_strdup(name);
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
            loc_free(s->name);
            loc_free(s);
        }
        else {
            sp = &s->next;
        }
    }
}

static unsigned message_hash(Protocol * p, const char * service, const char * name) {
    int i;
    unsigned h = (unsigned)(uintptr_t)p >> 4;
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
    unsigned h = (unsigned)(uintptr_t)c >> 4;
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

#define reply_hash(c, tokenid) ((((unsigned)(uintptr_t)(c) >> 4) + (unsigned)(tokenid)) % REPLY_HASH_SIZE)

static ReplyHandlerInfo * find_reply_handler(Channel * c, unsigned long tokenid, int take) {
    ReplyHandlerInfo ** rhp = &reply_handlers[reply_hash(c, tokenid)];
    ReplyHandlerInfo * rh;
    while ((rh = *rhp) != NULL) {
        if (rh->c == c && rh->tokenid == tokenid) {
            if (take) *rhp = rh->next;
            return rh;
        }
        rhp = &rh->next;
    }
    return NULL;
}

static void skip_until_EOM(Channel * c) {
    for (;;) {
        int ch = read_stream(&c->inp);
        if (ch == MARKER_EOM) return;
        if (ch == MARKER_EOS) return;
    }
}

static void event_locator_hello(Channel * c);

void handle_protocol_message(Channel * c) {
    char type[8];
    char token[256];
    char service[256];
    char name[256];
    char * args[4];
    int error = 0;
    Protocol * p = c->protocol;

    assert(is_dispatch_thread());

    read_stringz(&c->inp, type, sizeof(type));
    if (strlen(type) != 1) {
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        error = ERR_PROTOCOL;
    }
    else if (type[0] == 'C') {
        Trap trap;
        read_stringz(&c->inp, token, sizeof(token));
        read_stringz(&c->inp, service, sizeof(service));
        read_stringz(&c->inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Peer %s: Command: C %s %s %s ...", c->peer_name, token, service, name);
        if (c->state != ChannelStateConnected) {
            trace(LOG_PROTOCOL, "Wrong channel state for commands");
            skip_until_EOM(c);
            write_stringz(&c->out, "N");
            write_stringz(&c->out, token);
            write_stream(&c->out, MARKER_EOM);
        }
        else if (set_trap(&trap)) {
            MessageHandlerInfo * mh = find_message_handler(p, service, name);
            if (mh != NULL) {
                mh->handler(token, c, mh->client_data);
            }
            else if (p->default_handler != NULL) {
                args[0] = type;
                args[1] = token;
                args[2] = service;
                args[3] = name;
                p->default_handler(c, args, 4, p->client_data);
            }
            else {
                trace(LOG_PROTOCOL, "Command is not recognized: %s %s ...", service, name);
                skip_until_EOM(c);
                write_stringz(&c->out, "N");
                write_stringz(&c->out, token);
                write_stream(&c->out, MARKER_EOM);
            }
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling command %s.%s: %d %s",
                service, name, trap.error, errno_to_str(trap.error));
            error = trap.error;
        }
    }
    else if (type[0] == 'R' || type[0] == 'P' || type[0] == 'N') {
        Trap trap;
        read_stringz(&c->inp, token, sizeof(token));
        trace(LOG_PROTOCOL, "Peer %s: Reply: %c %s ...", c->peer_name, type[0], token);
        if (set_trap(&trap)) {
            ReplyHandlerInfo * rh = NULL;
            char * endptr = NULL;
            unsigned long tokenid;
            errno = 0;
            tokenid = strtoul(token, &endptr, 10);
            if (errno != 0 || *endptr != '\0' ||
               (rh = find_reply_handler(c, tokenid, type[0] != 'P')) == NULL) {
                if (p->default_handler != NULL) {
                    args[0] = type;
                    args[1] = token;
                    p->default_handler(c, args, 2, p->client_data);
                }
                else {
                    trace(LOG_ALWAYS, "Reply with unexpected token: %s", token);
                    exception(ERR_PROTOCOL);
                }
            }
            else {
                int n = 0;
                if (type[0] == 'N') {
                    skip_until_EOM(c);
                    n = ERR_INV_COMMAND;
                }
                rh->handler(c, rh->client_data, n);
                if (type[0] != 'P') loc_free(rh);
            }
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling reply %s: %d %s",
                  token, trap.error, errno_to_str(trap.error));
            error = trap.error;
        }
    }
    else if (type[0] == 'E') {
        Trap trap;
        read_stringz(&c->inp, service, sizeof(service));
        read_stringz(&c->inp, name, sizeof(name));
        trace(LOG_PROTOCOL, "Peer %s: Event: E %s %s ...", c->peer_name, service, name);
        if (set_trap(&trap)) {
            if ((c->state == ChannelStateStarted || c->state == ChannelStateHelloSent) &&
                strcmp(service, LOCATOR) == 0 && strcmp(name, "Hello") == 0) {
                event_locator_hello(c);
            }
            else {
                EventHandlerInfo * eh = find_event_handler(c, service, name);
                if (eh != NULL) {
                    eh->handler(c, eh->client_data);
                }
                else if (p->default_handler != NULL) {
                    args[0] = type;
                    args[1] = service;
                    args[2] = name;
                    p->default_handler(c, args, 3, p->client_data);
                }
                else {
                    skip_until_EOM(c);
                }
            }
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling event %s.%s: %d %s",
                service, name, trap.error, errno_to_str(trap.error));
            error = trap.error;
        }
    }
    else if (type[0] == 'F') {
        int n = 0;
        int s = 0;
        int ch = read_stream(&c->inp);
        if (ch == '-') {
            s = 1;
            ch = read_stream(&c->inp);
        }
        while (ch >= '0' && ch <= '9') {
            n = n * 10 + (ch - '0');
            ch = read_stream(&c->inp);
        }
        if (ch == 0) {
            ch = read_stream(&c->inp);
        }
        else {
            trace(LOG_ALWAYS, "Received F with no zero termination.");
        }
        if (ch != MARKER_EOM) error = ERR_PROTOCOL;
        else c->congestion_level = s ? -n : n;
    }
    else if (p->default_handler != NULL) {
        args[0] = type;
        p->default_handler(c, args, 1, p->client_data);
    }
    else {
        trace(LOG_ALWAYS, "Invalid TCF message: %s ...", type);
        error = ERR_PROTOCOL;
    }
    if (error != 0) exception(error);
}

static void message_handler_old(Channel * c, char ** args, int nargs, void * client_data) {
    ProtocolMessageHandler handler = (ProtocolMessageHandler)client_data;
    handler(c, args, nargs);
}

void set_default_message_handler(Protocol * p, ProtocolMessageHandler handler) {
    set_default_message_handler2(p, (ProtocolMessageHandler2)message_handler_old, (void *)handler);
}

void set_default_message_handler2(Protocol * p, ProtocolMessageHandler2 handler, void * client_data) {
    p->default_handler = handler;
    p->client_data = client_data;
}

static void command_handler_old(char * token, Channel * c, void * client_data) {
    ProtocolCommandHandler handler = (ProtocolCommandHandler)client_data;
    handler(token, c);
}

void add_command_handler(Protocol * p, const char * service, const char * name, ProtocolCommandHandler handler) {
    add_command_handler2(p, service, name, (ProtocolCommandHandler2)command_handler_old, (void *)handler);
}

void add_command_handler2(Protocol * p, const char * service, const char * name, ProtocolCommandHandler2 handler, void * client_data) {
    unsigned h = message_hash(p, service, name);
    MessageHandlerInfo * mh = (MessageHandlerInfo *)loc_alloc(sizeof(MessageHandlerInfo));
    mh->p = p;
    mh->service = protocol_get_service(p, service);
    mh->name = name;
    mh->handler = handler;
    mh->client_data = client_data;
    mh->next = message_handlers[h];
    message_handlers[h] = mh;
}

static void event_handler_old(Channel * c, void * client_data) {
    ProtocolEventHandler handler = (ProtocolEventHandler)client_data;
    handler(c);
}

void add_event_handler(Channel * c, const char * service, const char * name, ProtocolEventHandler handler) {
    add_event_handler2(c, service, name, (ProtocolEventHandler2)event_handler_old, (void *)handler);
}

void add_event_handler2(Channel * c, const char * service, const char * name, ProtocolEventHandler2 handler, void * client_data) {
    unsigned h = event_hash(c, service, name);
    EventHandlerInfo * eh = (EventHandlerInfo *)loc_alloc(sizeof(EventHandlerInfo));
    eh->c = c;
    eh->service = protocol_get_service(c, service);
    eh->name = name;
    eh->handler = handler;
    eh->client_data = client_data;
    eh->next = event_handlers[h];
    event_handlers[h] = eh;
}

static void send_command_failed(void * args) {
    ReplyHandlerInfo * rh = (ReplyHandlerInfo *)args;
    rh->handler(rh->c, rh->client_data, ERR_CHANNEL_CLOSED);
    loc_free(rh);
}

ReplyHandlerInfo * protocol_send_command(Channel * c, const char * service, const char * name, ReplyHandlerCB handler, void * client_data) {
    Protocol * p = c->protocol;
    ReplyHandlerInfo * rh = (ReplyHandlerInfo *)loc_alloc(sizeof(ReplyHandlerInfo));

    rh->c = c;
    rh->handler = handler;
    rh->client_data = client_data;
    if (c->peer_service_list == NULL) {
        post_event(send_command_failed, rh);
    }
    else {
        unsigned h;
        unsigned long tokenid;
        do tokenid = p->tokenid++;
        while (find_reply_handler(c, tokenid, 0) != NULL);
        write_stringz(&c->out, "C");
        json_write_ulong(&c->out, tokenid);
        write_stream(&c->out, 0);
        write_stringz(&c->out, service);
        write_stringz(&c->out, name);
        rh->tokenid = tokenid;
        h = reply_hash(c, tokenid);
        rh->next = reply_handlers[h];
        reply_handlers[h] = rh;
    }
    return rh;
}

struct sendRedirectInfo {
    ReplyHandlerCB handler;
    void * client_data;
};

static void redirect_done(Channel * c, void * client_data, int error) {
    struct sendRedirectInfo * info = (struct sendRedirectInfo *)client_data;

    if (!error) {
        assert(c->state == ChannelStateRedirectSent);
        error = read_errno(&c->inp);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        if (!error) {
            c->state = ChannelStateHelloSent;
        }
        else {
            c->state = ChannelStateConnected;
        }
    }
    else if (c->state == ChannelStateRedirectSent) {
        c->state = ChannelStateConnected;
    }
    else {
        assert(c->state == ChannelStateDisconnected);
    }
    info->handler(c, info->client_data, error);
}

ReplyHandlerInfo * send_redirect_command(Channel * c, const char * peerId, ReplyHandlerCB handler, void * client_data) {
    struct sendRedirectInfo * info = (struct sendRedirectInfo *)loc_alloc_zero(sizeof *info);
    ReplyHandlerInfo * rh;

    assert(c->state == ChannelStateConnected);
    c->state = ChannelStateRedirectSent;
    info->handler = handler;
    info->client_data = client_data;
    rh = protocol_send_command(c, LOCATOR, "redirect", redirect_done, info);
    json_write_string(&c->out, peerId);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    return rh;
}

static void connect_done(Channel * c) {
    assert(c->state == ChannelStateConnected);
    if (c->connected) {
        c->connected(c);
    }
    else {
        int i;
        trace(LOG_PROTOCOL, "channel server connected, remote services:");
        for (i = 0; i < c->peer_service_cnt; i++) {
            trace(LOG_PROTOCOL, "  %s", c->peer_service_list[i]);
        }
    }
}

void send_hello_message(Channel * c) {
    Protocol * p = c->protocol;
    ServiceInfo * s = services;
    int cnt = 0;

    assert(c->state == ChannelStateStarted || c->state == ChannelStateHelloReceived);
    write_stringz(&c->out, "E");
    write_stringz(&c->out, LOCATOR);
    write_stringz(&c->out, "Hello");
    write_stream(&c->out, '[');
#if ENABLE_ZeroCopy
    if (!c->disable_zero_copy) {
        json_write_string(&c->out, "ZeroCopy");
        cnt++;
    }
#endif
    while (s) {
        if (s->owner == p) {
            if (cnt != 0) write_stream(&c->out, ',');
            json_write_string(&c->out, s->name);
            cnt++;
        }
        s = s->next;
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    if (c->state == ChannelStateStarted) {
        c->state = ChannelStateHelloSent;
    }
    else {
        c->state = ChannelStateConnected;
        connect_done(c);
    }
}

static void free_string_list(int cnt, char **list) {
    while (cnt > 0) loc_free(list[--cnt]);
    loc_free(list);
}

static void event_locator_hello(Channel * c) {
    int cnt = 0;
    char **list = NULL;

    c->out.supports_zero_copy = 0;
    if (read_stream(&c->inp) != '[') exception(ERR_PROTOCOL);
    if (peek_stream(&c->inp) == ']') {
        read_stream(&c->inp);
    }
    else {
        int max = 4;
        list = (char **)loc_alloc(max * sizeof *list);
        for (;;) {
            int ch;
            char * service = json_read_alloc_string(&c->inp);
            if (strcmp(service, "ZeroCopy") == 0) c->out.supports_zero_copy = 1;
            if (cnt == max) {
                max *= 2;
                list = (char **)loc_realloc(list, max * sizeof *list);
            }
            list[cnt++] = service;
            ch = read_stream(&c->inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            free_string_list(cnt, list);
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (read_stream(&c->inp) != 0 || read_stream(&c->inp) != MARKER_EOM) {
        free_string_list(cnt, list);
        exception(ERR_JSON_SYNTAX);
    }
    if (c->state != ChannelStateStarted && c->state != ChannelStateHelloSent) {
        free_string_list(cnt, list);
        /* TODO: should this be a protocol error? */
        return;
    }
    if (c->peer_service_list != NULL) {
        free_string_list(c->peer_service_cnt, c->peer_service_list);
    }
    c->peer_service_cnt = cnt;
    c->peer_service_list = list;
    if (c->state == ChannelStateStarted) {
        c->state = ChannelStateHelloReceived;
    }
    else {
        c->state = ChannelStateConnected;
        connect_done(c);
    }
}

int protocol_cancel_command(ReplyHandlerInfo * rh) {
    /* TODO: protocol_cancel_command() */
    return 0;
}

static void channel_closed(Channel * c) {
    unsigned i;

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
                    clear_trap(&trap);
                }
                else {
                    trace(LOG_ALWAYS, "Exception handling reply %ul: %d %s",
                          rh->tokenid, trap.error, errno_to_str(trap.error));
                }
                loc_free(rh);
            }
            else {
                rhp = &rh->next;
            }
        }
    }
    if (c->peer_service_list) {
        free_string_list(c->peer_service_cnt, c->peer_service_list);
        c->peer_service_cnt = 0;
        c->peer_service_list = NULL;
    }
}

Protocol * protocol_alloc(void) {
    Protocol * p = (Protocol *)loc_alloc_zero(sizeof *p);

    assert(is_dispatch_thread());
    if (!ini_done) {
        add_channel_close_listener(channel_closed);
        ini_done = 1;
    }
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
