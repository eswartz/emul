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
 * Implements input and output stream over TCP/IP transport.
 */

#include "mdep.h"
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "tcf.h"
#include "channel.h"
#include "channel_tcp.h"
#include "myalloc.h"
#include "protocol.h"
#include "events.h"
#include "exceptions.h"
#include "trace.h"
#include "json.h"
#include "peer.h"
#include "ip_ifc.h"
#include "asyncreq.h"
#include "inputbuf.h"

#define ESC 3
#define BUF_SIZE 0x1000
#define CHANNEL_MAGIC 0x87208956
#define REFRESH_TIME 15
#define STALE_TIME_DELTA (REFRESH_TIME * 2)
#define MAX_IFC 10

#define is_suspended(CH) ((CH)->chan.spg && (CH)->chan.spg->suspended)

typedef struct ChannelTCP ChannelTCP;

struct ChannelTCP {
    Channel chan;           /* Public channel information - must be first */
    int magic;              /* Magic number */
    int socket;             /* Socket file descriptor */
    int lock_cnt;           /* Stream lock count, when > 0 channel cannot be deleted */
    int read_pending;       /* Read request is pending */

    /* Input stream buffer */
    InputBuf ibuf;

    /* Output stream state */
    char obuf[BUF_SIZE];
    int obuf_inp;
    int out_errno;

    /* Async read request */
    AsyncReqInfo rdreq;
};

typedef struct ServerTCP ServerTCP;

struct ServerTCP {
    ChannelServer serv;
    int sock;
    PeerServer * ps;
    LINK servlink;
    AsyncReqInfo accreq;
};

#define channel2tcp(A)  ((ChannelTCP *)((char *)(A) - offsetof(ChannelTCP, chan)))
#define inp2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, inp)))
#define out2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, out)))
#define server2tcp(A)   ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, serv)))
#define servlink2tcp(A) ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, servlink)))
#define ibuf2tcp(A)    ((ChannelTCP *)((char *)(A) - offsetof(ChannelTCP, ibuf)))

static LINK server_list;
static void tcp_channel_read_done(void * x);
static void handle_channel_msg(void * x);

static void delete_channel(ChannelTCP * c) {
    trace(LOG_PROTOCOL, "Deleting channel 0x%08x", c);
    assert(c->lock_cnt == 0);
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending == 0);
    if (c->ibuf.handling_msg != HandleMsgIdle) {
        /* Cancel pending message handling since channel object is gone. */
        assert(c->ibuf.handling_msg == HandleMsgTriggered);
        c->ibuf.handling_msg = HandleMsgIdle;
        cancel_event(handle_channel_msg, c, 0);
    }
    channel_clear_broadcast_group(&c->chan);
    channel_clear_suspend_group(&c->chan);
    c->magic = 0;
    loc_free(c);
}

static void tcp_lock(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    channel->lock_cnt++;
}

static void tcp_unlock(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    assert(channel->lock_cnt > 0);
    channel->lock_cnt--;
    if (channel->lock_cnt == 0 && !channel->read_pending) {
        delete_channel(channel);
    }
}

static int tcp_is_closed(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    assert(channel->lock_cnt > 0);
    return channel->socket < 0;
}

static void tcp_flush_stream(OutputStream * out) {
    int cnt = 0;
    ChannelTCP * channel = channel2tcp(out2channel(out));
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    if (channel->obuf_inp == 0) return;
    if (channel->socket < 0) return;
    if (channel->out_errno) return;
    while (cnt < channel->obuf_inp) {
        int wr = send(channel->socket, channel->obuf + cnt, channel->obuf_inp - cnt, 0);
        if (wr < 0) {
            int err = errno;
            trace(LOG_PROTOCOL, "Can't send() on channel 0x%08x: %d %s", channel, err, errno_to_str(err));
            channel->out_errno = err;
            return;
        }
        cnt += wr;
    }
    assert(cnt == channel->obuf_inp);
    channel->obuf_inp = 0;
}

static void tcp_write_stream(OutputStream * out, int byte) {
    ChannelTCP * channel = channel2tcp(out2channel(out));
    int b0 = byte;
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    if (channel->socket < 0) return;
    if (channel->out_errno) return;
    if (b0 < 0) byte = ESC;
    channel->obuf[channel->obuf_inp++] = byte;
    if (channel->obuf_inp == BUF_SIZE) tcp_flush_stream(out);
    if (b0 < 0 || b0 == ESC) {
        if (b0 == ESC) byte = 0;
        else if (b0 == MARKER_EOM) byte = 1;
        else if (b0 == MARKER_EOS) byte = 2;
        else assert(0);
        if (channel->socket < 0) return;
        if (channel->out_errno) return;
        channel->obuf[channel->obuf_inp++] = byte;
        if (channel->obuf_inp == BUF_SIZE) tcp_flush_stream(out);
    }
}

static void tcp_post_read(InputBuf * ibuf, unsigned char *buf, int size) {
    ChannelTCP * channel = ibuf2tcp(ibuf);

    if (channel->read_pending || channel->socket < 0) return;
    channel->read_pending = 1;
    channel->rdreq.u.sio.bufp = buf;
    channel->rdreq.u.sio.bufsz = size;
    async_req_post(&channel->rdreq);
}

static void tcp_wait_read(InputBuf * ibuf) {
    ChannelTCP * channel = ibuf2tcp(ibuf);

    /* Wait for read to complete */
    assert(channel->lock_cnt > 0);
    assert(channel->read_pending != 0);
    cancel_event(tcp_channel_read_done, &channel->rdreq, 1);
    tcp_channel_read_done(&channel->rdreq);
}

static int tcp_read_stream(InputStream * inp) {
    Channel * c = inp2channel(inp);
    ChannelTCP * channel = channel2tcp(c);

    assert(channel->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur++;
    return ibuf_get_more(&channel->ibuf, inp, 0);
}

static int tcp_peek_stream(InputStream * inp) {
    Channel * c = inp2channel(inp);
    ChannelTCP * channel = channel2tcp(c);

    assert(channel->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur;
    return ibuf_get_more(&channel->ibuf, inp, 1);
}

static void send_eof_and_close(Channel * channel, int err) {
    ChannelTCP * c = channel2tcp(channel);

    assert(c->magic == CHANNEL_MAGIC);
    if (c->socket < 0) return;
    ibuf_flush(&c->ibuf, &c->chan.inp);
    ibuf_read_done(&c->ibuf, 0);      /* EOF */
    tcp_write_stream(&c->chan.out, MARKER_EOS);
    write_errno(&c->chan.out, err);
    c->chan.out.write(&c->chan.out, MARKER_EOM);
    c->chan.out.flush(&c->chan.out);
    closesocket(c->socket);
    c->socket = -1;
    notify_channel_closed(channel);
    if (channel->disconnected) channel->disconnected(channel);
    tcp_unlock(channel);
}

static void handle_channel_msg(void * x) {
    Trap trap;
    ChannelTCP * c = (ChannelTCP *)x;
    int hasmsg;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->ibuf.handling_msg == HandleMsgTriggered);
    assert(c->ibuf.message_count);

    if (is_suspended(c)) {
        /* Honor suspend before message processing started */
        c->ibuf.handling_msg = HandleMsgIdle;
        return;
    }
    hasmsg = ibuf_start_message(&c->ibuf);
    if (hasmsg <= 0) {
        if (hasmsg < 0) {
            trace(LOG_PROTOCOL, "Socket is shutdown by remote peer, channel 0x%08x", c);
            channel_close(&c->chan);
        }
        return;
    }
    if (set_trap(&trap)) {
        c->chan.receive(&c->chan);
        clear_trap(&trap);
    }
    else {
        trace(LOG_ALWAYS, "Exception handling protocol message: %d %s",
              trap.error, errno_to_str(trap.error));
        c->ibuf.handling_msg = HandleMsgIdle;
        channel_close(&c->chan);
        return;
    }
    if (c->ibuf.handling_msg == HandleMsgIdle) {
        /* Completed processing of current message and there are no
         * messages pending - flush output */
        if (c->chan.bcg) {
            c->chan.bcg->out.flush(&c->chan.bcg->out);
        }
        else {
            c->chan.out.flush(&c->chan.out);
        }
    }
}

static void channel_check_pending(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);

    assert(is_dispatch_thread());
    if (channel->ibuf.handling_msg == HandleMsgIdle &&
        channel->ibuf.message_count && !is_suspended(channel)) {
        post_event(handle_channel_msg, channel);
        channel->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static void tcp_trigger_message(InputBuf * ibuf) {
    ChannelTCP * channel = ibuf2tcp(ibuf);

    assert(is_dispatch_thread());
    assert(channel->ibuf.message_count > 0);
    if (channel->ibuf.handling_msg == HandleMsgIdle && !is_suspended(channel)) {
        post_event(handle_channel_msg, channel);
        channel->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static int channel_get_message_count(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    int cnt;
    assert(is_dispatch_thread());
    cnt = channel->ibuf.message_count;
    return cnt;
}

static void tcp_channel_read_done(void * x) {
    AsyncReqInfo * req = x;
    ChannelTCP * c = req->client_data;
    int len;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending != 0);
    assert(c->lock_cnt != 0 || c->socket < 0);
    c->read_pending = 0;
    if (c->socket < 0) {
        if (c->lock_cnt == 0) {
            delete_channel(c);
        }
        return;
    }
    len = c->rdreq.u.sio.rval;
    if (req->error) {
        trace(LOG_ALWAYS, "Can't read from socket: %s", errno_to_str(req->error));
        len = 0;                        /* Treat error as eof */
    }
    ibuf_read_done(&c->ibuf, len);
}

static void start_channel(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    c->chan.connecting(&c->chan);
    c->rdreq.done = tcp_channel_read_done;
    c->rdreq.client_data = c;
    c->rdreq.type = AsyncReqRecv;
    c->rdreq.u.sio.sock = c->socket;
    c->rdreq.u.sio.flags = 0;
    ibuf_trigger_read(&c->ibuf);
}

static ChannelTCP * create_channel(int sock) {
    const int i = 1;
    ChannelTCP * c;

    assert(sock >= 0);
    if (setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&i, sizeof(i)) < 0) {
        trace(LOG_ALWAYS, "Can't set TCP_NODELAY option on a socket: %s", errno_to_str(errno));
        closesocket(sock);
        return NULL;
    }

    c = loc_alloc_zero(sizeof *c);
    c->magic = CHANNEL_MAGIC;
    c->chan.inp.read = tcp_read_stream;
    c->chan.inp.peek = tcp_peek_stream;
    c->chan.out.write = tcp_write_stream;
    c->chan.out.flush = tcp_flush_stream;
    c->chan.start_comm = start_channel;
    c->chan.check_pending = channel_check_pending;
    c->chan.message_count = channel_get_message_count;
    c->chan.lock = tcp_lock;
    c->chan.unlock = tcp_unlock;
    c->chan.is_closed = tcp_is_closed;
    c->chan.close = send_eof_and_close;
    ibuf_init(&c->ibuf, &c->chan.inp);
    c->ibuf.post_read = tcp_post_read;
    c->ibuf.wait_read = tcp_wait_read;
    c->ibuf.trigger_message = tcp_trigger_message;
    c->socket = sock;
    c->lock_cnt = 1;
    return c;
}

static void refresh_peer_server(int sock, PeerServer * ps) {
    int i;
    PeerServer * ps2;
    struct sockaddr_in sin;
#if defined(_WRS_KERNEL)
    int sinlen;
#else
    socklen_t sinlen;
#endif
    char *transport;
    char *str_host;
    char str_port[32];
    char str_id[64];
    int ifcind;
    struct in_addr src_addr;
    ip_ifc_info ifclist[MAX_IFC];

    sinlen = sizeof sin;
    if (getsockname(sock, (struct sockaddr *)&sin, &sinlen) != 0) {
        trace(LOG_ALWAYS, "refresh_peer_server: getsockname error: %s", errno_to_str(errno));
        return;
    }
    ifcind = build_ifclist(sock, MAX_IFC, ifclist);
    while (ifcind-- > 0) {
        if (sin.sin_addr.s_addr != INADDR_ANY &&
            (ifclist[ifcind].addr & ifclist[ifcind].mask) !=
            (sin.sin_addr.s_addr & ifclist[ifcind].mask)) {
            continue;
        }
        src_addr.s_addr = ifclist[ifcind].addr;
        ps2 = peer_server_alloc();
        ps2->flags = ps->flags | PS_FLAG_LOCAL | PS_FLAG_DISCOVERABLE;
        for (i = 0; i < ps->ind; i++) {
            peer_server_addprop(ps2, loc_strdup(ps->list[i].name), loc_strdup(ps->list[i].value));
        }
        transport = peer_server_getprop(ps2, "TransportName", NULL);
        assert(transport != NULL);
        snprintf(str_port, sizeof(str_port), "%d", ntohs(sin.sin_port));
        str_host = loc_strdup(inet_ntoa(src_addr));
        snprintf(str_id, sizeof(str_id), "%s:%s:%s", transport, str_host, str_port);
        peer_server_addprop(ps2, loc_strdup("ID"), loc_strdup(str_id));
        peer_server_addprop(ps2, loc_strdup("Host"), str_host);
        peer_server_addprop(ps2, loc_strdup("Port"), loc_strdup(str_port));
        peer_server_add(ps2, STALE_TIME_DELTA);
    }
}

static void refresh_all_peer_server(void *x) {
    LINK * l;

    if (list_is_empty(&server_list)) {
        return;
    }
    l = server_list.next;
    while (l != &server_list) {
        ServerTCP * si = servlink2tcp(l);
        refresh_peer_server(si->sock, si->ps);
        l = l->next;
    }
    post_event_with_delay(refresh_all_peer_server, NULL, REFRESH_TIME*1000*1000);
}

static void tcp_server_accept_done(void * x) {
    AsyncReqInfo * req = x;
    ServerTCP * si = req->client_data;
    ChannelTCP * c;
    int sock;

    if (si->sock < 0) {
        /* Server closed. */
        loc_free(si);
        return;
    }
    if (req->error) {
        trace(LOG_ALWAYS, "socket accept failed: %d %s", req->error, errno_to_str(req->error));
        async_req_post(req);
        return;
    }
    sock = req->u.acc.rval;
    async_req_post(req);
    c = create_channel(sock);
    si->serv.new_conn(&si->serv, &c->chan);
}

static void server_close(ChannelServer * serv) {
    ServerTCP * s = server2tcp(serv);

    assert(is_dispatch_thread());
    if (s->sock < 0) {
        return;
    }
    list_remove(&s->servlink);
    peer_server_free(s->ps);
    closesocket(s->sock);
    s->sock = -1;
}

ChannelServer * channel_tcp_server(PeerServer * ps) {
    const int i = 1;
    int sock;
    int error;
    char * reason = NULL;
    struct addrinfo hints;
    struct addrinfo * reslist = NULL;
    struct addrinfo * res = NULL;
    ServerTCP * si;
    char * host = peer_server_getprop(ps, "Host", NULL);
    char * port = peer_server_getprop(ps, "Port", "");

    assert(is_dispatch_thread());
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    hints.ai_flags = AI_PASSIVE;
    error = loc_getaddrinfo(host, port, &hints, &reslist);
    if (error) {
        trace(LOG_ALWAYS, "getaddrinfo error: %s", loc_gai_strerror(error));
        return NULL;
    }
    sock = -1;
    reason = NULL;
    for (res = reslist; res != NULL; res = res->ai_next) {
        sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
        if (sock < 0) {
            error = errno;
            reason = "create";
            continue;
        }
        /* Allow rapid reuse of this port. */
        if (((struct sockaddr_in *)res->ai_addr)->sin_port != 0 &&
            setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (char *)&i, sizeof(i)) < 0) {
            error = errno;
            reason = "setsockopt(reuse) for";
            closesocket(sock);
            sock = -1;
            continue;
        }
        if (bind(sock, res->ai_addr, res->ai_addrlen)) {
            error = errno;
            reason = "bind";
            closesocket(sock);
            sock = -1;
            continue;
        }
        if (listen(sock, 16)) {
            error = errno;
            reason = "listen on";
            closesocket(sock);
            sock = -1;
            continue;
        }

        /* Only create one listener - don't see how getaddrinfo with
         * the given arguments could return more then one anyway */
        break;
    }
    loc_freeaddrinfo(reslist);
    if (sock < 0) {
        trace(LOG_ALWAYS, "socket %s error: %s", reason, errno_to_str(error));
        return NULL;
    }
    si = loc_alloc(sizeof *si);
    si->serv.close = server_close;
    si->sock = sock;
    si->ps = ps;
    if (server_list.next == NULL) list_init(&server_list);
    if (list_is_empty(&server_list)) {
            post_event_with_delay(refresh_all_peer_server, NULL, REFRESH_TIME * 1000 * 1000);
    }
    list_add_last(&si->servlink, &server_list);
    refresh_peer_server(sock, ps);

    si->accreq.done = tcp_server_accept_done;
    si->accreq.client_data = si;
    si->accreq.type = AsyncReqAccept;
    si->accreq.u.acc.sock = sock;
    si->accreq.u.acc.addr = NULL;
    si->accreq.u.acc.addrlen = 0;
    async_req_post(&si->accreq);
    return &si->serv;
}

Channel * channel_tcp_connect(PeerServer * ps) {
    const int i = 1;
    int sock = -1;
    ChannelTCP * c = NULL;
    int error = 0;
    char * reason = NULL;
    char * host = peer_server_getprop(ps, "Host", NULL);
    char * port = peer_server_getprop(ps, "Port", NULL);
    struct addrinfo hints;
    struct addrinfo * reslist = NULL;
    struct addrinfo * res = NULL;
 
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    error = loc_getaddrinfo(host, port, &hints, &reslist);
    if (error) {
        trace(LOG_ALWAYS, "getaddrinfo error: %s", loc_gai_strerror(error));
        return NULL;
    }
    sock = -1;
    reason = NULL;
    for (res = reslist; res != NULL; res = res->ai_next) {
        sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
        if (sock < 0) {
            error = errno;
            reason = "create";
            continue;
        }
        if (connect(sock, res->ai_addr, res->ai_addrlen)) {
            error = errno;
            reason = "connect";
            closesocket(sock);
            sock = -1;
            continue;
        }
        break;
    }
    loc_freeaddrinfo(reslist);
    if (sock < 0) {
        trace(LOG_ALWAYS, "socket %s error: %s", reason, errno_to_str(error));
        return NULL;
    }

    c = create_channel(sock);
    if (c == NULL) {
        return NULL;
    }
    return &c->chan;
}
