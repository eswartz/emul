/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * Implements input and output stream over named pipe transport.
 */

#include <config.h>

#if defined(WIN32)

#include <fcntl.h>
#include <errno.h>
#include <assert.h>
#include <framework/channel.h>
#include <framework/channel_pipe.h>
#include <framework/myalloc.h>
#include <framework/protocol.h>
#include <framework/errors.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/peer.h>
#include <framework/asyncreq.h>
#include <framework/inputbuf.h>
#include <framework/outputbuf.h>

#define BUF_SIZE (128 * MEM_USAGE_FACTOR)
#define CHANNEL_MAGIC 0x52376532
#define SERVER_INSTANCE_CNT 16
#define DEFAULT_PIPE_NAME "TCF-Agent"
#define DEFAULT_PIPE_DIR  "//./pipe/"

static const char * def_pipe_name = DEFAULT_PIPE_DIR DEFAULT_PIPE_NAME;
static const char * attr_pipe_name = "PipeName";

typedef struct ChannelPIPE ChannelPIPE;
typedef struct ServerPIPE ServerPIPE;
typedef struct ServerInstance ServerInstance;

struct ChannelPIPE {
    Channel chan;           /* Public channel information - must be first */
    int magic;              /* Magic number */
    int lock_cnt;           /* Stream lock count, when > 0 channel cannot be deleted */
    int fd_inp;
    int fd_out;
    ServerInstance * server;

    /* Input stream buffer */
    InputBuf ibuf;

    /* Output stream state */
    int out_flush_cnt;
    unsigned char obuf[BUF_SIZE];
    OutputQueue out_queue;
    AsyncReqInfo out_req;

    /* Async read request */
    AsyncReqInfo rd_req;
    int read_pending;
};

struct ServerInstance {
    ServerPIPE * server;
    int index;
    int fd_inp;
    int fd_out;
#if defined(WIN32) && !defined(__CYGWIN__)
    HANDLE pipe;
    AsyncReqInfo req;
#endif
};

struct ServerPIPE {
    ChannelServer serv;
    PeerServer * ps;
    LINK servlink;
    ServerInstance arr[SERVER_INSTANCE_CNT];
};

#define channel2pipe(A)     ((ChannelPIPE *)((char *)(A) - offsetof(ChannelPIPE, chan)))
#define inp2channel(A)      ((Channel *)((char *)(A) - offsetof(Channel, inp)))
#define out2channel(A)      ((Channel *)((char *)(A) - offsetof(Channel, out)))
#define server2pipe(A)      ((ServerPIPE *)((char *)(A) - offsetof(ServerPIPE, serv)))
#define servlink2pipe(A)    ((ServerPIPE *)((char *)(A) - offsetof(ServerPIPE, servlink)))
#define ibuf2pipe(A)        ((ChannelPIPE *)((char *)(A) - offsetof(ChannelPIPE, ibuf)))
#define obuf2pipe(A)        ((ChannelPIPE *)((char *)(A) - offsetof(ChannelPIPE, out_queue)))

static LINK server_list;
static void pipe_read_done(void * x);
static void handle_channel_msg(void * x);

static void close_input_pipe(ChannelPIPE * c);
static void close_output_pipe(ChannelPIPE * c);

static void delete_channel(ChannelPIPE * c) {
    trace(LOG_PROTOCOL, "Deleting channel %#lx", c);
    assert(c->lock_cnt == 0);
    assert(c->out_flush_cnt == 0);
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending == 0);
    assert(c->ibuf.handling_msg != HandleMsgTriggered);
    assert(output_queue_is_empty(&c->out_queue));
    output_queue_clear(&c->out_queue);
    channel_clear_broadcast_group(&c->chan);
    close_input_pipe(c);
    c->magic = 0;
    loc_free(c->ibuf.buf);
    loc_free(c->chan.peer_name);
    loc_free(c);
}

static void pipe_lock(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    c->lock_cnt++;
}

static void pipe_unlock(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->lock_cnt > 0);
    c->lock_cnt--;
    if (c->lock_cnt == 0) {
        assert(!c->read_pending);
        delete_channel(c);
    }
}

static int pipe_is_closed(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->lock_cnt > 0);
    return c->chan.state == ChannelStateDisconnected;
}

static void done_write_request(void * args) {
    ChannelPIPE * c = (ChannelPIPE *)((AsyncReqInfo *)args)->client_data;
    int size = 0;
    int error = 0;

    assert(args == &c->out_req);
    if (c->out_req.u.fio.rval < 0) error = c->out_req.error;
    else size = c->out_req.u.fio.rval;
    output_queue_done(&c->out_queue, error, size);

    if (output_queue_is_empty(&c->out_queue) &&
        c->chan.state == ChannelStateDisconnected) close_output_pipe(c);

    pipe_unlock(&c->chan);
}

static void post_write_request(OutputBuffer * bf) {
    ChannelPIPE * c = obuf2pipe(bf->queue);
    c->out_req.client_data = c;
    c->out_req.done = done_write_request;
    c->out_req.type = AsyncReqWrite;
    c->out_req.u.fio.fd = c->fd_out;
    c->out_req.u.fio.bufp = bf->buf + bf->buf_pos;
    c->out_req.u.fio.bufsz = bf->buf_len - bf->buf_pos;
    async_req_post(&c->out_req);
    pipe_lock(&c->chan);
}

static void create_write_request(ChannelPIPE * c, const void * buf, size_t size) {
    if (c->chan.state == ChannelStateDisconnected) return;
    c->out_queue.post_io_request = post_write_request;
    output_queue_add(&c->out_queue, buf, size);
}

static void pipe_flush(ChannelPIPE * c) {
    unsigned char * p = c->obuf;
    unsigned char * e = c->chan.out.cur;
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->chan.out.end == p + sizeof(c->obuf));
    if (e == p) return;
    assert(e >= p && e <= p + sizeof(c->obuf));
    create_write_request(c, p, e - p);
    c->chan.out.cur = p;
}

static void pipe_flush_event(void * x) {
    ChannelPIPE * c = (ChannelPIPE *)x;
    assert(c->magic == CHANNEL_MAGIC);
    if (--c->out_flush_cnt == 0) {
        int congestion_level = c->chan.congestion_level;
        if (congestion_level > 0) usleep(congestion_level * 2500);
        pipe_flush(c);
        pipe_unlock(&c->chan);
    }
}

static void pipe_write_stream(OutputStream * out, int byte) {
    ChannelPIPE * c = channel2pipe(out2channel(out));
    assert(c->magic == CHANNEL_MAGIC);
    if (c->chan.state == ChannelStateDisconnected) return;
    if (c->chan.out.cur == c->chan.out.end) pipe_flush(c);
    if (byte < 0 || byte == ESC) {
        char esc = 0;
        *c->chan.out.cur++ = ESC;
        if (byte == ESC) esc = 0;
        else if (byte == MARKER_EOM) esc = 1;
        else if (byte == MARKER_EOS) esc = 2;
        else assert(0);
        if (c->chan.state == ChannelStateDisconnected) return;
        if (c->chan.out.cur == c->chan.out.end) pipe_flush(c);
        *c->chan.out.cur++ = esc;
        if (byte == MARKER_EOM && c->out_flush_cnt < 8) {
            if (c->out_flush_cnt++ == 0) pipe_lock(&c->chan);
            post_event(pipe_flush_event, c);
        }
        return;
    }
    *c->chan.out.cur++ = (char)byte;
}

static void pipe_write_block_stream(OutputStream * out, const char * bytes, size_t size) {
    size_t cnt = 0;
    ChannelPIPE * c = channel2pipe(out2channel(out));

    if (out->supports_zero_copy && size > 32) {
        /* Send the binary data escape seq */
        size_t n = size;
        if (c->chan.out.cur >= c->chan.out.end - 8) pipe_flush(c);
        *c->chan.out.cur++ = ESC;
        *c->chan.out.cur++ = 3;
        for (;;) {
            if (n <= 0x7fu) {
                *c->chan.out.cur++ = (char)n;
                break;
            }
            *c->chan.out.cur++ = (n & 0x7fu) | 0x80u;
            n = n >> 7;
        }
        /* We need to flush the buffer then send our data */
        pipe_flush(c);

        create_write_request(c, bytes, size);
        return;
    }

    while (cnt < size) write_stream(out, (unsigned char)bytes[cnt++]);
}

static ssize_t pipe_splice_block_stream(OutputStream * out, int fd, size_t size, off_t * offset) {
    ssize_t rd = 0;
    char buffer[BUF_SIZE];
    assert(is_dispatch_thread());
    if (size == 0) return 0;
    if (size > BUF_SIZE) size = BUF_SIZE;
    if (offset != NULL) {
        rd = pread(fd, buffer, size, *offset);
        if (rd > 0) *offset += rd;
    }
    else {
        rd = read(fd, buffer, size);
    }
    if (rd > 0) pipe_write_block_stream(out, buffer, rd);
    return rd;
}

static void pipe_post_read(InputBuf * ibuf, unsigned char * buf, size_t size) {
    ChannelPIPE * c = ibuf2pipe(ibuf);

    if (c->read_pending) return;
    c->read_pending = 1;
    c->rd_req.u.fio.bufp = buf;
    c->rd_req.u.fio.bufsz = size;
    async_req_post(&c->rd_req);
}

static void pipe_wait_read(InputBuf * ibuf) {
    ChannelPIPE * c = ibuf2pipe(ibuf);

    /* Wait for read to complete */
    assert(c->lock_cnt > 0);
    assert(c->read_pending != 0);
    cancel_event(pipe_read_done, &c->rd_req, 1);
    pipe_read_done(&c->rd_req);
}

static int pipe_read_stream(InputStream * inp) {
    Channel * channel = inp2channel(inp);
    ChannelPIPE * c = channel2pipe(channel);

    assert(c->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur++;
    return ibuf_get_more(&c->ibuf, 0);
}

static int pipe_peek_stream(InputStream * inp) {
    Channel * channel = inp2channel(inp);
    ChannelPIPE * c = channel2pipe(channel);

    assert(c->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur;
    return ibuf_get_more(&c->ibuf, 1);
}

static void send_eof_and_close(Channel * channel, int err) {
    ChannelPIPE * c = channel2pipe(channel);

    assert(c->magic == CHANNEL_MAGIC);
    if (channel->state == ChannelStateDisconnected) return;
    ibuf_flush(&c->ibuf);
    if (c->ibuf.handling_msg == HandleMsgTriggered) {
        /* Cancel pending message handling */
        cancel_event(handle_channel_msg, c, 0);
        c->ibuf.handling_msg = HandleMsgIdle;
    }
    write_stream(&c->chan.out, MARKER_EOS);
    write_errno(&c->chan.out, err);
    write_stream(&c->chan.out, MARKER_EOM);
    pipe_flush(c);
    pipe_post_read(&c->ibuf, c->obuf, sizeof(c->obuf));
    c->chan.state = ChannelStateDisconnected;
    if (output_queue_is_empty(&c->out_queue)) close_output_pipe(c);
    notify_channel_closed(channel);
    if (channel->disconnected) {
        channel->disconnected(channel);
    }
    else {
        trace(LOG_PROTOCOL, "channel %#lx disconnected", c);
        protocol_release(channel->protocol);
    }
    channel->protocol = NULL;
}

static void handle_channel_msg(void * x) {
    Trap trap;
    ChannelPIPE * c = (ChannelPIPE *)x;
    int has_msg;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->ibuf.handling_msg == HandleMsgTriggered);
    assert(c->ibuf.message_count);

    has_msg = ibuf_start_message(&c->ibuf);
    if (has_msg <= 0) {
        if (has_msg < 0 && c->chan.state != ChannelStateDisconnected) {
            trace(LOG_PROTOCOL, "Pipe is close by remote peer, channel %#lx %s", c, c->chan.peer_name);
            channel_close(&c->chan);
        }
    }
    else if (set_trap(&trap)) {
        if (c->chan.receive) {
            c->chan.receive(&c->chan);
        }
        else {
            handle_protocol_message(&c->chan);
        }
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception in message handler: %d %s",
              trap.error, errno_to_str(trap.error));
        send_eof_and_close(&c->chan, trap.error);
    }
}

static void channel_check_pending(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);

    assert(is_dispatch_thread());
    if (c->ibuf.handling_msg == HandleMsgIdle && c->ibuf.message_count) {
        post_event(handle_channel_msg, c);
        c->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static void pipe_trigger_message(InputBuf * ibuf) {
    ChannelPIPE * c = ibuf2pipe(ibuf);

    assert(is_dispatch_thread());
    assert(c->ibuf.message_count > 0);
    if (c->ibuf.handling_msg == HandleMsgIdle) {
        post_event(handle_channel_msg, c);
        c->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static int channel_get_message_count(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);
    assert(is_dispatch_thread());
    if (c->ibuf.handling_msg != HandleMsgTriggered) return 0;
    return c->ibuf.message_count;
}

static void pipe_read_done(void * x) {
    AsyncReqInfo * req = (AsyncReqInfo *)x;
    ChannelPIPE * c = (ChannelPIPE *)req->client_data;
    int len = 0;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending != 0);
    assert(c->lock_cnt > 0);
    c->read_pending = 0;
    len = c->rd_req.u.fio.rval;
    if (req->error) {
        if (c->chan.state != ChannelStateDisconnected) {
            trace(LOG_ALWAYS, "Can't read from pipe: %s", errno_to_str(req->error));
        }
        len = 0; /* Treat error as eof */
    }
    if (c->chan.state != ChannelStateDisconnected) {
        ibuf_read_done(&c->ibuf, len);
    }
    else if (len > 0) {
        pipe_post_read(&c->ibuf, c->obuf, sizeof(c->obuf));
    }
    else {
        pipe_unlock(&c->chan);
    }
}

static void start_channel(Channel * channel) {
    ChannelPIPE * c = channel2pipe(channel);

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->fd_inp >= 0);
    assert(c->fd_out >= 0);
    if (c->chan.connecting) {
        c->chan.connecting(&c->chan);
    }
    else {
        trace(LOG_PROTOCOL, "channel server connecting");
        send_hello_message(&c->chan);
    }
    ibuf_trigger_read(&c->ibuf);
}

static ChannelPIPE * create_channel(int fd_inp, int fd_out, ServerInstance * server) {
    ChannelPIPE * c = NULL;

    assert(fd_inp >= 0);
    assert(fd_out >= 0);

    c = (ChannelPIPE *)loc_alloc_zero(sizeof *c);
    c->magic = CHANNEL_MAGIC;
    c->chan.inp.read = pipe_read_stream;
    c->chan.inp.peek = pipe_peek_stream;
    c->chan.out.cur = c->obuf;
    c->chan.out.end = c->obuf + sizeof(c->obuf);
    c->chan.out.write = pipe_write_stream;
    c->chan.out.write_block = pipe_write_block_stream;
    c->chan.out.splice_block = pipe_splice_block_stream;
    c->chan.state = ChannelStateStartWait;
    c->chan.start_comm = start_channel;
    c->chan.check_pending = channel_check_pending;
    c->chan.message_count = channel_get_message_count;
    c->chan.lock = pipe_lock;
    c->chan.unlock = pipe_unlock;
    c->chan.is_closed = pipe_is_closed;
    c->chan.close = send_eof_and_close;
    ibuf_init(&c->ibuf, &c->chan.inp);
    c->ibuf.post_read = pipe_post_read;
    c->ibuf.wait_read = pipe_wait_read;
    c->ibuf.trigger_message = pipe_trigger_message;
    c->fd_inp = fd_inp;
    c->fd_out = fd_out;
    c->lock_cnt = 1;
    c->server = server;
    c->rd_req.done = pipe_read_done;
    c->rd_req.client_data = c;
    c->rd_req.type = AsyncReqRead;
    c->rd_req.u.fio.fd = fd_inp;
    output_queue_ini(&c->out_queue);
    return c;
}

static void set_peer_name(ChannelPIPE * c) {
    /* Create a human readable channel name that uniquely identifies remote peer */
    char name[256];
    static int pipe_cnt = 0;
    snprintf(name, sizeof(name), "PIPE:%d", pipe_cnt++);
    c->chan.peer_name = loc_strdup(name);
}

typedef struct ChannelConnectInfo {
    ChannelConnectCallBack callback;
    void * callback_args;
    int error;
    int fd_inp;
    int fd_out;
} ChannelConnectInfo;

static void channel_pipe_connect_done(void * args) {
    ChannelConnectInfo * info = (ChannelConnectInfo *)args;
    if (info->error) {
        info->callback(info->callback_args, info->error, NULL);
    }
    else {
        ChannelPIPE * c = create_channel(info->fd_inp, info->fd_out, 0);
        set_peer_name(c);
        info->callback(info->callback_args, 0, &c->chan);
    }
    loc_free(info);
}

void channel_pipe_connect(PeerServer * ps, ChannelConnectCallBack callback, void * callback_args) {
    const char * path = peer_server_getprop(ps, attr_pipe_name, def_pipe_name);
    ChannelConnectInfo * info = (ChannelConnectInfo *)loc_alloc_zero(sizeof(ChannelConnectInfo));
    char out_path[FILE_PATH_SIZE];

    info->fd_out = -1;
    info->fd_inp = open(path, O_BINARY | O_RDONLY, 0);
    if (info->fd_inp < 0) info->error = errno;

    if (!info->error) {
        int l = read(info->fd_inp, out_path, sizeof(out_path) - 1);
        if (l < 0) info->error = errno;
        else out_path[l] = 0;
    }

    if (!info->error) {
        info->fd_out = open(out_path, O_BINARY | O_WRONLY, 0);
        if (info->fd_out < 0) info->error = errno;
    }

    if (info->error) {
        if (info->fd_inp >= 0) close(info->fd_inp);
        if (info->fd_out >= 0) close(info->fd_out);
        info->fd_inp = -1;
        info->fd_out = -1;
    }

    info->callback = callback;
    info->callback_args = callback_args;
    post_event(channel_pipe_connect_done, info);
}

#if defined(WIN32) && !defined(__CYGWIN__)

#define check_error_win32(ok) { if (!(ok)) check_error(set_win32_errno(GetLastError())); }

static void pipe_client_connected(void * args) {
    AsyncReqInfo * req = (AsyncReqInfo *)args;
    ServerInstance * ins = (ServerInstance *)req->client_data;
    int error = 0;

    assert(req == &ins->req);

    if (req->error) error = req->error;

    if (!error) {
        int l = 0;
        HANDLE h = NULL;
        OVERLAPPED overlap;
        char inp_path[FILE_PATH_SIZE];
        const char * path = peer_server_getprop(ins->server->ps, attr_pipe_name, def_pipe_name);
        static unsigned pipe_cnt = 0;

        memset(&overlap, 0, sizeof(overlap));
        snprintf(inp_path, sizeof(inp_path), "%s-%u", path, pipe_cnt++);
        l = strlen(inp_path) + 1;
        h = CreateNamedPipe(inp_path, PIPE_ACCESS_DUPLEX | FILE_FLAG_OVERLAPPED,
                         PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
                         1, BUF_SIZE, BUF_SIZE, 0, NULL);
        if (h == INVALID_HANDLE_VALUE) error = set_win32_errno(GetLastError());
        if (!error) {
            ins->fd_inp = _open_osfhandle((intptr_t)h, O_BINARY | O_RDONLY);
            if (ins->fd_inp < 0) error = errno;
        }
        if (!error) {
            overlap.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
            if (overlap.hEvent == NULL) error = set_win32_errno(GetLastError());
        }
        if (!error) {
            if (ConnectNamedPipe(h, &overlap)) {
                error = ERR_PROTOCOL;
            }
            else {
                DWORD e = GetLastError();
                if (e != ERROR_IO_PENDING) error = set_win32_errno(GetLastError());
            }
        }
        if (!error) {
            int wr = write(ins->fd_out, inp_path, l);
            if (wr < 0) error = errno;
        }
        if (!error) {
            switch (WaitForSingleObject(overlap.hEvent, 10000)) {
            case WAIT_OBJECT_0:
                break;
            case WAIT_ABANDONED:
            case WAIT_TIMEOUT:
                error = ETIMEDOUT;
                break;
            default:
                error = set_win32_errno(GetLastError());
                break;
            }
        }
        if (!CloseHandle(overlap.hEvent)) error = set_win32_errno(GetLastError());
    }

    if (error) {
        trace(LOG_ALWAYS, "Cannot connect pipe: %s", errno_to_str(error));
        if (ins->fd_inp >= 0) close(ins->fd_inp);
        ins->fd_inp = -1;
        DisconnectNamedPipe(ins->pipe);
        async_req_post(&ins->req);
    }
    else {
        ChannelPIPE * c = create_channel(ins->fd_inp, ins->fd_out, ins);
        set_peer_name(c);
        ins->server->serv.new_conn(&ins->server->serv, &c->chan);
    }
}

static void close_input_pipe(ChannelPIPE * c) {
    assert(c->fd_out < 0);
    assert(c->fd_inp > 0);
    if (c->server != NULL) {
        ServerInstance * ins = c->server;
        close(ins->fd_inp);
        ins->fd_inp = -1;
        async_req_post(&ins->req);
        c->server = NULL;
    }
    else {
        close(c->fd_inp);
    }
    c->fd_inp = -1;
}

static void close_output_pipe(ChannelPIPE * c) {
    assert(c->fd_out > 0);
    assert(c->fd_inp > 0);
    if (c->server != NULL) {
        ServerInstance * ins = c->server;
        check_error_win32(DisconnectNamedPipe(ins->pipe));
    }
    else {
        close(c->fd_out);
    }
    c->fd_out = -1;
}

static void register_server(ServerPIPE * s) {
    int i;
    PeerServer * ps = s->ps;
    PeerServer * ps2 = peer_server_alloc();
    const char * transport = peer_server_getprop(ps, "TransportName", NULL);
    const char * path = peer_server_getprop(ps, attr_pipe_name, def_pipe_name);
    char id[256];

    ps2->flags = ps->flags | PS_FLAG_LOCAL | PS_FLAG_DISCOVERABLE;
    for (i = 0; i < ps->ind; i++) {
        peer_server_addprop(ps2, loc_strdup(ps->list[i].name), loc_strdup(ps->list[i].value));
    }
    i = strlen(DEFAULT_PIPE_DIR);
    if (strncmp(path, DEFAULT_PIPE_DIR, i) != 0) i = 0;
    snprintf(id, sizeof(id), "%s:%s", transport, path + i);
    for (i = 0; id[i]; i++) {
        /* Character '/' is prohibited in a peer ID string */
        if (id[i] == '/') id[i] = '|';
    }
    peer_server_addprop(ps2, loc_strdup("ID"), loc_strdup(id));
    peer_server_addprop(ps2, loc_strdup(attr_pipe_name), loc_strdup(path));
    peer_server_add(ps2, ~0u);
}

#else

static void close_output_pipe(ChannelPIPE * c) {
}

static void close_input_pipe(ChannelPIPE * c) {
}

#endif

static void server_close(ChannelServer * serv) {
    ServerPIPE * s = server2pipe(serv);
    int i;

    assert(is_dispatch_thread());
    list_remove(&s->servlink);
    peer_server_free(s->ps);
    for (i = 0; i < SERVER_INSTANCE_CNT; i++) {
        ServerInstance * ins = s->arr + i;
        if (ins->fd_inp >= 0 && close(ins->fd_inp) < 0) check_error(errno);
        if (ins->fd_out >= 0 && close(ins->fd_out) < 0) check_error(errno);
        ins->fd_inp = ins->fd_out = -1;
#if defined(WIN32) && !defined(__CYGWIN__)
        ins->pipe = NULL;
#endif
    }
    /* TODO: free 's' when all pending reqs are done */
}

ChannelServer * channel_pipe_server(PeerServer * ps) {
    ServerPIPE * s = (ServerPIPE *)loc_alloc_zero(sizeof(ServerPIPE));
    if (server_list.next == NULL) list_init(&server_list);
#if defined(WIN32) && !defined(__CYGWIN__)
    {
        int i;
        const char * path = peer_server_getprop(ps, attr_pipe_name, def_pipe_name);
        for (i = 0; i < SERVER_INSTANCE_CNT; i++) {
            ServerInstance * ins = s->arr + i;
            ins->server = s;
            ins->index = i;
            ins->pipe = CreateNamedPipe(path, PIPE_ACCESS_OUTBOUND,
                 PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
                 SERVER_INSTANCE_CNT, BUF_SIZE, BUF_SIZE, 0, NULL);

            if (ins->pipe == INVALID_HANDLE_VALUE) {
                set_win32_errno(GetLastError());
                return NULL;
            }
            ins->fd_inp = -1;
            ins->fd_out = _open_osfhandle((intptr_t)ins->pipe, O_BINARY | O_WRONLY);
        }
        s->ps = ps;
        s->serv.close = server_close;
        list_add_last(&s->servlink, &server_list);
        for (i = 0; i < SERVER_INSTANCE_CNT; i++) {
            ServerInstance * ins = s->arr + i;
            ins->req.type = AsyncReqConnectPipe;
            ins->req.client_data = &s->arr[i];
            ins->req.done = pipe_client_connected;
            ins->req.u.cnp.pipe = ins->pipe;
            async_req_post(&ins->req);
        }
        register_server(s);
        return &s->serv;
    }
#else
    s->serv.close = server_close;
    /* TODO: Unix pipe channel */
    loc_free(s);
    errno = ERR_UNSUPPORTED;
    return NULL;
#endif
}

#else
/* Pipes are not supported */
#include <framework/errors.h>
#include <framework/channel_pipe.h>

void channel_pipe_connect(PeerServer * server, ChannelConnectCallBack callback, void * callback_args) {
    callback(callback_args, ERR_UNSUPPORTED, NULL);
}

ChannelServer * channel_pipe_server(PeerServer * server) {
    errno = ERR_UNSUPPORTED;
    return NULL;
}

#endif
