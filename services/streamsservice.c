/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * TCF Streams - generic streams service.
 */

#include <config.h>

#if SERVICE_Streams

#include <assert.h>
#include <framework/channel.h>
#include <framework/exceptions.h>
#include <framework/json.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/events.h>
#include <framework/myalloc.h>
#include <services/streamsservice.h>

static const char * STREAMS = "Streams";

typedef struct StreamClient StreamClient;
typedef struct ReadRequest ReadRequest;
typedef struct WriteRequest WriteRequest;
typedef struct Subscription Subscription;

#define STREAM_MAGIC 0x29465398

struct VirtualStream {
    LINK link_all;
    unsigned magic;
    char type[256];
    unsigned id;
    unsigned access;
    VirtualStreamCallBack * callback;
    void * callback_args;
    int ref_cnt;
    int deleted;
    LINK clients;
    uint64_t pos;
    char * buf;
    unsigned buf_len;
    unsigned buf_inp;
    unsigned buf_out;
    unsigned eos_inp;
    unsigned eos_out;
    unsigned data_available_posted;
    unsigned space_available_posted;
};

struct StreamClient {
    LINK link_hash;
    LINK link_stream;
    LINK link_all;
    LINK read_requests;
    LINK write_requests;
    VirtualStream * stream;
    Channel * channel;
    uint64_t pos;
};

struct ReadRequest {
    LINK link_client;
    StreamClient * client;
    char token[256];
    size_t size;
};

struct WriteRequest {
    LINK link_client;
    StreamClient * client;
    char token[256];
    char * data;
    size_t offs;
    size_t size;
    int eos;
};

struct Subscription {
    LINK link_all;
    char type[256];
    Channel * channel;
};

#define hash2client(A)          ((StreamClient *)((char *)(A) - offsetof(StreamClient, link_hash)))
#define stream2client(A)        ((StreamClient *)((char *)(A) - offsetof(StreamClient, link_stream)))
#define all2client(A)           ((StreamClient *)((char *)(A) - offsetof(StreamClient, link_all)))
#define all2subscription(A)     ((Subscription *)((char *)(A) - offsetof(Subscription, link_all)))
#define all2stream(A)           ((VirtualStream *)((char *)(A) - offsetof(VirtualStream, link_all)))
#define client2read_request(A)  ((ReadRequest *)((char *)(A) - offsetof(ReadRequest, link_client)))
#define client2write_request(A) ((WriteRequest *)((char *)(A) - offsetof(WriteRequest, link_client)))

#define HANDLE_HASH_SIZE 0x100
static LINK handle_hash[HANDLE_HASH_SIZE];
static LINK clients;
static LINK streams;
static LINK subscriptions;
static unsigned id_cnt = 0;

static unsigned get_client_hash(unsigned id, Channel * c) {
    return (id + (unsigned)(uintptr_t)c) % HANDLE_HASH_SIZE;
}

static int str2id(char * s, unsigned * id) {
    char * p = NULL;
    if (*s++ != 'V') return 0;
    if (*s++ != 'S') return 0;
    *id = (unsigned)strtoul(s, &p, 10);
    return *p == 0;
}

void virtual_stream_get_id(VirtualStream * stream, char * id_buf, size_t buf_size) {
    snprintf(id_buf, buf_size, "VS%u", stream->id);
}

static StreamClient * find_client(char * s, Channel * c) {
    unsigned id = 0;
    if (str2id(s, &id)) {
        unsigned h = get_client_hash(id, c);
        LINK * l = handle_hash[h].next;
        while (l != &handle_hash[h]) {
            StreamClient * client = hash2client(l);
            if (client->stream->id == id && client->channel == c) return client;
            l = l->next;
        }
    }
    errno = ERR_INV_CONTEXT;
    return NULL;
}

static void send_event_stream_created(OutputStream * out, VirtualStream * stream, const char * context_id) {
    char id[256];

    virtual_stream_get_id(stream, id, sizeof(id));
    write_stringz(out, "E");
    write_stringz(out, STREAMS);
    write_stringz(out, "created");

    json_write_string(out, stream->type);
    write_stream(out, 0);
    json_write_string(out, id);
    write_stream(out, 0);
    json_write_string(out, context_id);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void send_event_stream_disposed(OutputStream * out, VirtualStream * stream) {
    char id[256];

    virtual_stream_get_id(stream, id, sizeof(id));
    write_stringz(out, "E");
    write_stringz(out, STREAMS);
    write_stringz(out, "disposed");

    json_write_string(out, stream->type);
    write_stream(out, 0);
    json_write_string(out, id);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void delete_read_request(ReadRequest * r) {
    Channel * c = r->client->channel;
    Trap trap;

    if (set_trap(&trap)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, r->token);
        write_stringz(&c->out, "null");
        write_errno(&c->out, ERR_COMMAND_CANCELLED);
        json_write_long(&c->out, 0);
        write_stream(&c->out, 0);
        json_write_boolean(&c->out, 1);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception handling pending stream read command: %d %s",
              trap.error, errno_to_str(trap.error));
    }
    list_remove(&r->link_client);
    loc_free(r);
}

static void delete_write_request(WriteRequest * r, int error) {
    Channel * c = r->client->channel;
    Trap trap;

    if (set_trap(&trap)) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, r->token);
        write_errno(&c->out, error);
        write_stream(&c->out, MARKER_EOM);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception handling pending stream write command: %d %s",
              trap.error, errno_to_str(trap.error));
    }
    list_remove(&r->link_client);
    loc_free(r->data);
    loc_free(r);
}

static void delete_stream(void * args) {
    VirtualStream * stream = (VirtualStream *)args;

    assert(stream->magic == STREAM_MAGIC);
    assert(list_is_empty(&stream->clients));
    assert(stream->deleted);
    stream->magic = 0;
    list_remove(&stream->link_all);
    loc_free(stream->buf);
    loc_free(stream);
}

static void notify_data_available(void * args) {
    VirtualStream * stream = (VirtualStream *)args;
    assert(stream->magic == STREAM_MAGIC);
    stream->data_available_posted = 0;
    if (stream->deleted || stream->eos_out) return;
    stream->callback(stream, VS_EVENT_DATA_AVAILABLE, stream->callback_args);
}

static void notify_space_available(void * args) {
    VirtualStream * stream = (VirtualStream *)args;
    assert(stream->magic == STREAM_MAGIC);
    stream->space_available_posted = 0;
    if (stream->deleted || stream->eos_inp) return;
    stream->callback(stream, VS_EVENT_SPACE_AVAILABLE, stream->callback_args);
}

static void advance_stream_buffer(VirtualStream * stream) {
    unsigned len = (stream->buf_inp + stream->buf_len - stream->buf_out) % stream->buf_len;
    uint64_t min_pos = ~(uint64_t)0;
    uint64_t buf_pos = stream->pos - len;
    LINK * l;

    assert(stream->access & VS_ENABLE_REMOTE_READ);
    for (l = stream->clients.next; l != &stream->clients; l = l->next) {
        StreamClient * client = stream2client(l);
        assert(client->pos <= stream->pos);
        if (client->pos < min_pos) min_pos = client->pos;
    }
    if (min_pos == ~(uint64_t)0) {
        stream->buf_out = stream->buf_inp;
    }
    else if (min_pos > buf_pos) {
        assert(min_pos - buf_pos <= len);
        stream->buf_out = (stream->buf_out + (unsigned)(min_pos - buf_pos)) % stream->buf_len;
    }
    else if (stream->pos - min_pos >= stream->buf_len) {
        /* TODO: drop stream data */
        assert(0);
    }
    if (len != (stream->buf_inp + stream->buf_len - stream->buf_out) % stream->buf_len &&
        !stream->space_available_posted) {
        post_event(notify_space_available, stream);
        stream->space_available_posted = 1;
    }
}

static StreamClient * create_client(VirtualStream * stream, Channel * channel) {
    unsigned len = (stream->buf_inp + stream->buf_len - stream->buf_out) % stream->buf_len;
    StreamClient * client = (StreamClient *)loc_alloc_zero(sizeof(StreamClient));
    list_init(&client->link_hash);
    list_init(&client->link_stream);
    list_init(&client->link_all);
    list_init(&client->read_requests);
    list_init(&client->write_requests);
    client->stream = stream;
    client->channel = channel;
    client->pos = stream->pos - len;
    list_add_first(&client->link_hash, &handle_hash[get_client_hash(stream->id, channel)]);
    list_add_first(&client->link_stream, &stream->clients);
    list_add_first(&client->link_all, &clients);
    stream->ref_cnt++;
    return client;
}

static void delete_client(StreamClient * client) {
    VirtualStream * stream = client->stream;
    Trap trap;
    LINK * n;

    assert(stream->ref_cnt > 0);
    if (set_trap(&trap)) {
        send_event_stream_disposed(&client->channel->out, stream);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception sending stream deleted event: %d %s",
              trap.error, errno_to_str(trap.error));
    }
    list_remove(&client->link_hash);
    list_remove(&client->link_stream);
    list_remove(&client->link_all);
    for (n = client->read_requests.next; n != &client->read_requests;) {
        ReadRequest * r = client2read_request(n);
        n = n->next;
        delete_read_request(r);
    }
    for (n = client->write_requests.next; n != &client->write_requests;) {
        WriteRequest * r = client2write_request(n);
        n = n->next;
        delete_write_request(r, ERR_COMMAND_CANCELLED);
    }
    loc_free(client);
    if (--stream->ref_cnt == 0) {
        assert(list_is_empty(&stream->clients));
        assert(stream->deleted);
        post_event(delete_stream, stream);
    }
    else if (stream->access & VS_ENABLE_REMOTE_READ) {
        advance_stream_buffer(stream);
    }
}

static void delete_subscription(Subscription * s) {
    list_remove(&s->link_all);
    loc_free(s);
}

static void send_read_reply(StreamClient * client, char * token, size_t size) {
    VirtualStream * stream = client->stream;
    Channel * c = client->channel;
    unsigned lost = 0;
    unsigned read1 = 0;
    unsigned read2 = 0;
    int eos = 0;
    char * data1 = NULL;
    char * data2 = NULL;
    unsigned pos = 0;
    unsigned len = (stream->buf_inp + stream->buf_len - stream->buf_out) % stream->buf_len;

    assert(len > 0 || stream->eos_inp);
    assert(client->pos <= stream->pos);
    if ((uint64_t)len < stream->pos - client->pos) {
        lost = (long)(stream->pos - client->pos - len);
    }
    else {
        len = (unsigned)(stream->pos - client->pos);
    }
    pos = (stream->buf_inp + stream->buf_len - len) % stream->buf_len;
    if (len > size) len = size;
    data1 = stream->buf + pos;
    if (pos + len <= stream->buf_len) {
        read1 = len;
    }
    else {
        read1 = stream->buf_len - pos;
        data2 = stream->buf;
        read2 = len - read1;
    }
    assert(read1 + read2 == len);
    client->pos += lost + read1 + read2;
    assert(client->pos <= stream->pos);
    if (client->pos == stream->pos && stream->eos_inp) eos = 1;
    assert(eos || lost + read1 + read2 > 0);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    if (read1 + read2 > 0) {
        JsonWriteBinaryState state;

        json_write_binary_start(&state, &c->out, read1 + read2);
        json_write_binary_data(&state, data1, read1);
        json_write_binary_data(&state, data2, read2);
        json_write_binary_end(&state);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_errno(&c->out, 0);
    json_write_long(&c->out, lost);
    write_stream(&c->out, 0);
    json_write_boolean(&c->out, eos);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

void virtual_stream_create(const char * type, const char * context_id, size_t buf_len, unsigned access,
        VirtualStreamCallBack * callback, void * callback_args, VirtualStream ** res) {
    LINK * l;
    VirtualStream * stream = (VirtualStream *)loc_alloc_zero(sizeof(VirtualStream));

    buf_len++;
    list_init(&stream->clients);
    strlcpy(stream->type, type, sizeof(stream->type));
    stream->magic = STREAM_MAGIC;
    stream->id = id_cnt++;
    stream->access = access;
    stream->callback = callback;
    stream->callback_args = callback_args;
    stream->ref_cnt = 1;
    stream->buf = (char *)loc_alloc(buf_len);
    stream->buf_len = buf_len;
    for (l = subscriptions.next; l != &subscriptions; l = l->next) {
        Subscription * h = all2subscription(l);
        if (strcmp(type, h->type) == 0) {
            Trap trap;
            create_client(stream, h->channel);
            if (set_trap(&trap)) {
                send_event_stream_created(&h->channel->out, stream, context_id);
                clear_trap(&trap);
            }
            else {
                trace(LOG_ALWAYS, "Exception sending stream created event: %d %s",
                      trap.error, errno_to_str(trap.error));
            }
        }
    }
    list_add_first(&stream->link_all, &streams);
    *res = stream;
}

VirtualStream * virtual_stream_find(char * id) {
    LINK * l;
    unsigned n = 0;

    if (str2id(id, &n)) {
        for (l = streams.next; l != &streams; l = l->next) {
            VirtualStream * stream = all2stream(l);
            if (stream->id == n && !stream->deleted) return stream;
        }
    }
    errno = ERR_INV_CONTEXT;
    return NULL;
}

int virtual_stream_add_data(VirtualStream * stream, char * buf, size_t buf_size, size_t * data_size, int eos) {
    int err = 0;

    assert(stream->magic == STREAM_MAGIC);
    if (stream->eos_inp) err = ERR_EOF;

    if (!err) {
        unsigned len = (stream->buf_out + stream->buf_len - stream->buf_inp - 1) % stream->buf_len;
        assert(len < stream->buf_len);
        if (buf_size < len) len = buf_size;
        if (stream->buf_inp + len <= stream->buf_len) {
            memcpy(stream->buf + stream->buf_inp, buf, len);
        }
        else {
            unsigned x = stream->buf_len - stream->buf_inp;
            unsigned y = len - x;
            memcpy(stream->buf + stream->buf_inp, buf, x);
            memcpy(stream->buf, buf + x, y);
        }
        stream->buf_inp = (stream->buf_inp + len) % stream->buf_len;
        stream->pos += len;
        *data_size = len;
        if (eos && buf_size == len) stream->eos_inp = 1;
    }

    if (stream->access & VS_ENABLE_REMOTE_READ) {
        if (!err && (stream->eos_inp || *data_size > 0)) {
            LINK * l;
            for (l = stream->clients.next; l != &stream->clients; l = l->next) {
                StreamClient * client = stream2client(l);
                while (!list_is_empty(&client->read_requests) && (client->pos < stream->pos || stream->eos_inp)) {
                    ReadRequest * r = client2read_request(client->read_requests.next);
                    list_remove(&r->link_client);
                    send_read_reply(client, r->token, r->size);
                    loc_free(r);
                }
            }
            advance_stream_buffer(stream);
        }
    }
    else if (!stream->data_available_posted) {
        post_event(notify_data_available, stream);
        stream->data_available_posted = 1;
    }

    errno = err;
    return err ? -1 : 0;
}

int virtual_stream_get_data(VirtualStream * stream, char * buf, size_t buf_size, size_t * data_size, int * eos) {
    size_t len;

    assert(stream->magic == STREAM_MAGIC);
    len = (stream->buf_inp + stream->buf_len - stream->buf_out) % stream->buf_len;

    if (len > buf_size) {
        len = buf_size;
        *eos = 0;
    }
    else {
        *eos = stream->eos_inp;
    }
    *data_size = len;
    if (*eos) stream->eos_out = 1;
    if (stream->buf_out + len <= stream->buf_len) {
        memcpy(buf, stream->buf + stream->buf_out, len);
    }
    else {
        unsigned x = stream->buf_len - stream->buf_out;
        unsigned y = len - x;
        memcpy(buf, stream->buf + stream->buf_out, x);
        memcpy(buf + x, stream->buf, y);
    }
    if (stream->access & VS_ENABLE_REMOTE_WRITE) {
        LINK * l;
        for (l = stream->clients.next; l != &stream->clients; l = l->next) {
            StreamClient * client = stream2client(l);
            if (!list_is_empty(&client->write_requests)) {
                WriteRequest * r = client2write_request(client->write_requests.next);
                size_t done = 0;
                int error = 0;
                if (virtual_stream_add_data(client->stream, r->data + r->offs,
                    r->size - r->offs, &done, r->eos) < 0) error = errno;
                r->offs += done;
                if (error || r->offs >= r->size) {
                    delete_write_request(r, error);
                }
                while (error && !list_is_empty(&client->write_requests)) {
                    r = client2write_request(client->write_requests.next);
                    delete_write_request(r, ERR_COMMAND_CANCELLED);
                }
            }
        }
    }
    if ((stream->access & VS_ENABLE_REMOTE_READ) == 0 && len > 0) {
        stream->buf_out = (stream->buf_out + len) % stream->buf_len;
        assert(!*eos || stream->buf_out == stream->buf_inp);
        if (!stream->space_available_posted) {
            post_event(notify_space_available, stream);
            stream->space_available_posted = 1;
        }
    }
    return 0;
}

int virtual_stream_is_empty(VirtualStream * stream) {
    assert(stream->magic == STREAM_MAGIC);
    assert(!stream->deleted);
    return stream->buf_out == stream->buf_inp;
}

void virtual_stream_delete(VirtualStream * stream) {
    assert(stream->magic == STREAM_MAGIC);
    assert(!stream->deleted);
    stream->deleted = 1;
    if (--stream->ref_cnt > 0) return;
    assert(list_is_empty(&stream->clients));
    post_event(delete_stream, stream);
}

static void command_subscribe(char * token, Channel * c) {
    char type[256];
    int err = 0;
    LINK * l;

    json_read_string(&c->inp, type, sizeof(type));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    for (l = subscriptions.next; l != &subscriptions;) {
        Subscription * h = all2subscription(l);
        l = l->next;
        if (h->channel == c && strcmp(type, h->type) == 0) {
            err = ERR_OTHER;
            break;
        }
    }
    if (err == 0) {
        Subscription * s = (Subscription *)loc_alloc_zero(sizeof(Subscription));
        list_init(&s->link_all);
        list_add_first(&s->link_all, &subscriptions);
        strlcpy(s->type, type, sizeof(s->type));
        s->channel = c;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_unsubscribe(char * token, Channel * c) {
    char type[256];
    int err = 0;
    Subscription * s = NULL;
    LINK * l;

    json_read_string(&c->inp, type, sizeof(type));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    for (l = subscriptions.next; l != &subscriptions;) {
        Subscription * h = all2subscription(l);
        l = l->next;
        if (h->channel == c && strcmp(type, h->type) == 0) {
            s = h;
            break;
        }
    }
    if (s == NULL) err = ERR_INV_CONTEXT;
    if (err == 0) delete_subscription(s);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_read(char * token, Channel * c) {
    char id[256];
    size_t size = 0;
    StreamClient * client = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    size = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    client = find_client(id, c);
    if (client == NULL) err = errno;
    if (!err && (client->stream->access & VS_ENABLE_REMOTE_READ) == 0) err = ERR_UNSUPPORTED;

    if (err == 0) {
        VirtualStream * stream = client->stream;
        if (client->pos == stream->pos && !stream->eos_inp) {
            ReadRequest * r = (ReadRequest *)loc_alloc_zero(sizeof(ReadRequest));
            list_init(&r->link_client);
            r->client = client;
            r->size = size;
            strlcpy(r->token, token, sizeof(r->token));
            list_add_last(&r->link_client, &client->read_requests);
        }
        else {
            assert(list_is_empty(&client->read_requests));
            assert(client->channel == c);
            send_read_reply(client, token, size);
            advance_stream_buffer(stream);
        }
    }
    else {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_stringz(&c->out, "null");
        write_errno(&c->out, err);
        json_write_long(&c->out, 0);
        write_stream(&c->out, 0);
        json_write_boolean(&c->out, 0);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
    }
}

static void command_write(char * token, Channel * c) {
    char id[256];
    StreamClient * client = NULL;
    long size = 0;
    long offs = 0;
    char * data = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    size = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);

    client = find_client(id, c);
    if (client == NULL) err = errno;
    if (!err && (client->stream->access & VS_ENABLE_REMOTE_WRITE) == 0) err = ERR_UNSUPPORTED;

    {
        JsonReadBinaryState state;
        unsigned data_pos = 0;

        if (!err && !list_is_empty(&client->write_requests)) data = (char *)loc_alloc(size);

        json_read_binary_start(&state, &c->inp);
        for (;;) {
            if (data != NULL) {
                size_t rd = json_read_binary_data(&state, data + data_pos, size - offs - data_pos);
                if (rd == 0) break;
                data_pos += rd;
            }
            else {
                char buf[256];
                size_t rd = json_read_binary_data(&state, buf, sizeof(buf));
                if (rd == 0) break;
                if (!err) {
                    size_t done = 0;
                    if (virtual_stream_add_data(client->stream, buf, rd, &done, 0) < 0) err = errno;
                    assert(done <= rd);
                    offs += done;
                    if (!err && done < rd) {
                        data = (char *)loc_alloc(size - offs);
                        memcpy(data, buf + done, rd - done);
                        data_pos = rd - done;
                    }
                }
            }
        }
        json_read_binary_end(&state);
    }

    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (data != NULL) {
        WriteRequest * r = (WriteRequest *)loc_alloc_zero(sizeof(WriteRequest));
        list_init(&r->link_client);
        r->client = client;
        r->data = data;
        r->size = size - offs;
        strlcpy(r->token, token, sizeof(r->token));
        list_add_last(&r->link_client, &client->write_requests);
    }
    else {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_errno(&c->out, err);
        write_stream(&c->out, MARKER_EOM);
    }
}

static void command_eos(char * token, Channel * c) {
    char id[256];
    StreamClient * client = NULL;
    size_t done = 0;
    WriteRequest * r = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    client = find_client(id, c);
    if (client == NULL) err = errno;
    if (!err && (client->stream->access & VS_ENABLE_REMOTE_WRITE) == 0) err = ERR_UNSUPPORTED;
    if (!err && !list_is_empty(&client->write_requests)) r = (WriteRequest *)loc_alloc_zero(sizeof(WriteRequest));
    if (!err && r == NULL && virtual_stream_add_data(client->stream, NULL, 0, &done, 1) < 0) err = errno;

    if (r != NULL) {
        list_init(&r->link_client);
        r->client = client;
        r->eos = 1;
        strlcpy(r->token, token, sizeof(r->token));
        list_add_last(&r->link_client, &client->write_requests);
    }
    else {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_errno(&c->out, err);
        write_stream(&c->out, MARKER_EOM);
    }
}

static void command_connect(char * token, Channel * c) {
    char id[256];
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (find_client(id, c) == NULL) {
        VirtualStream * stream = virtual_stream_find(id);
        if (stream == NULL) err = errno;
        else create_client(stream, c);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_disconnect(char * token, Channel * c) {
    char id[256];
    StreamClient * client = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    client = find_client(id, c);
    if (client == NULL) err = errno;
    if (!err) delete_client(client);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void channel_close_listener(Channel * c) {
    LINK * l = NULL;

    for (l = clients.next; l != &clients;) {
        StreamClient * client = all2client(l);
        l = l->next;
        if (client->channel == c) {
            trace(LOG_ALWAYS, "Stream is left connected by client: VS%d", client->stream->id);
            delete_client(client);
        }
    }

    for (l = subscriptions.next; l != &subscriptions;) {
        Subscription * h = all2subscription(l);
        l = l->next;
        if (h->channel == c) {
            delete_subscription(h);
        }
    }
}

void ini_streams_service(Protocol * proto) {
    int i;

    list_init(&clients);
    list_init(&streams);
    list_init(&subscriptions);
    for (i = 0; i < HANDLE_HASH_SIZE; i++) {
        list_init(&handle_hash[i]);
    }

    add_channel_close_listener(channel_close_listener);

    add_command_handler(proto, STREAMS, "subscribe", command_subscribe);
    add_command_handler(proto, STREAMS, "unsubscribe", command_unsubscribe);
    add_command_handler(proto, STREAMS, "read", command_read);
    add_command_handler(proto, STREAMS, "write", command_write);
    add_command_handler(proto, STREAMS, "eos", command_eos);
    add_command_handler(proto, STREAMS, "connect", command_connect);
    add_command_handler(proto, STREAMS, "disconnect", command_disconnect);
}

#endif /* SERVICE_Streams */
