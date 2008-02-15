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
 * Implements input and output stream over TCP/IP transport.
 */

#if defined(_WRS_KERNEL)
#  include <vxWorks.h>
#endif
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "mdep.h"
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
    pthread_t thread;       /* Socket receiving thread */
    int thread_exited;
    pthread_mutex_t mutex;  /* Channel data access synchronization lock */
    pthread_cond_t signal;
    int long_msg;           /* Message is longer then buffer, handlig should start before receiving EOM */
    int waiting_space;      /* Receiving thread is waiting for buffer space */
    int waiting_data;       /* Dispatch thread is waiting for data to read (long messages only) */
    int message_count;      /* Number of messages waiting to be dispatched */
    int event_posted;       /* Message handling event is posted to event queue */
    int lock_cnt;           /* Stream lock count, when > 0 channel cannot be deleted */
    int handling_msg;       /* Stream mutex is locked for input message handling */

    /* Input stream buffer */
    unsigned char ibuf[BUF_SIZE];
    int ibuf_inp;
    int ibuf_out;
    int eof;
    int peek;

    /* Output stream state */
    char obuf[BUF_SIZE];
    int obuf_inp;
    int out_errno;
};

typedef struct ServerTCP ServerTCP;

struct ServerTCP {
    ChannelServer serv;
    int sock;
    TCFSuspendGroup * spg;
    TCFBroadcastGroup * bcg;
    pthread_t server_thread;
    PeerServer * ps;
    LINK servlink;
};

#define channel2tcp(A)  ((ChannelTCP *)((char *)(A) - offsetof(ChannelTCP, chan)))
#define inp2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, inp)))
#define out2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, out)))
#define server2tcp(A)   ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, serv)))
#define servlink2tcp(A) ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, servlink)))

static ChannelCloseListener close_listeners[16];
static int close_listeners_cnt = 0;
static LINK server_list;

static void delete_channel(ChannelTCP * c) {
    int i;
    trace(LOG_PROTOCOL, "Deleting channel 0x%08x", c);
    assert(c->lock_cnt == 0);
    assert(c->magic == CHANNEL_MAGIC);
    c->chan.cb->disconnected(&c->chan);
    for (i = 0; i < close_listeners_cnt; i++) {
        close_listeners[i](&c->chan);
    }
    if (c->chan.bcg) list_remove(&c->chan.bclink);
    if (c->chan.spg) list_remove(&c->chan.susplink);
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
    if (channel->lock_cnt == 0) delete_channel(channel);
}

static int tcp_is_closed(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    assert(channel->lock_cnt > 0);
    return channel->socket < 0;
}

static void flush_stream(OutputStream * out) {
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
            trace(LOG_PROTOCOL, "Can't sent() on channel 0x%08x: %d %s", channel, err, errno_to_str(err));
            channel->out_errno = err;
            return;
        }
        cnt += wr;
    }
    assert(cnt == channel->obuf_inp);
    channel->obuf_inp = 0;
}

static void write_stream(OutputStream * out, int byte) {
    ChannelTCP * channel = channel2tcp(out2channel(out));
    int b0 = byte;
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    if (channel->socket < 0) return;
    if (channel->out_errno) return;
    if (b0 < 0) byte = ESC;
    channel->obuf[channel->obuf_inp++] = byte;
    if (channel->obuf_inp == BUF_SIZE) flush_stream(out);
    if (b0 < 0 || b0 == ESC) {
        if (b0 == ESC) byte = 0;
        else if (b0 == MARKER_EOM) byte = 1;
        else if (b0 == MARKER_EOS) byte = 2;
        else assert(0);
        if (channel->socket < 0) return;
        if (channel->out_errno) return;
        channel->obuf[channel->obuf_inp++] = byte;
        if (channel->obuf_inp == BUF_SIZE) flush_stream(out);
    }
}

static int read_byte(ChannelTCP * channel) {
    int res;
    assert(channel->message_count > 0);
    while (channel->ibuf_inp == channel->ibuf_out) {
        assert(channel->long_msg);
        assert(channel->message_count == 1);
        if (channel->waiting_space) {
            assert(!channel->waiting_data);
            pthread_cond_signal(&channel->signal);
            channel->waiting_space = 0;
        }
        if (channel->eof) return MARKER_EOS;
        if (channel->socket < 0) return MARKER_EOS;
        assert(!channel->waiting_data);
        assert(!channel->waiting_space);
        channel->waiting_data = 1;
        pthread_cond_wait(&channel->signal, &channel->mutex);
        assert(!channel->waiting_data);
    }
    res = channel->ibuf[channel->ibuf_out];
    channel->ibuf_out = (channel->ibuf_out + 1) % BUF_SIZE;
    return res;
}

static int read_stream(InputStream * inp) {
    int b;
    ChannelTCP * c = channel2tcp(inp2channel(inp));

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);

    if (c->peek != MARKER_NULL) {
        assert(c->peek != MARKER_EOM);
        b = c->peek;
        c->peek = MARKER_NULL;
    }
    else {
        if (!c->handling_msg) {
            pthread_mutex_lock(&c->mutex);
            c->handling_msg = 1;
        }
        b = read_byte(c);
        if (b == ESC) {
            b = read_byte(c);
            if (b == 0) {
                b = ESC;
            }
            else if (b == 1) {
                b = MARKER_EOM;
                c->message_count--;
                if (c->waiting_space) {
                    assert(!c->waiting_data);
                    pthread_cond_signal(&c->signal);
                    c->waiting_space = 0;
                }
                pthread_mutex_unlock(&c->mutex);
                c->handling_msg = 0;
            }
            else if (b == 2) {
                b = MARKER_EOS;
            }
        }
    }

    return b;
}

static int peek_stream(InputStream * inp) {
    ChannelTCP * c = channel2tcp(inp2channel(inp));
    return c->peek = read_stream(inp);
}

static void send_eof_and_close(Channel * channel, int err) {
    ChannelTCP * c = channel2tcp(channel);
    assert(c->magic == CHANNEL_MAGIC);
    write_stream(&c->chan.out, MARKER_EOS);
    write_errno(&c->chan.out, err);
    c->chan.out.write(&c->chan.out, MARKER_EOM);
    c->chan.out.flush(&c->chan.out);
    trace(LOG_PROTOCOL, "Closing socket, channel 0x%08x", c);
    closesocket(c->socket);
    c->socket = -1;
}

static void handle_channel_msg(void * x) {
    Trap trap;
    ChannelTCP * c = (ChannelTCP *)x;
    assert(is_dispatch_thread());
    for (;;) {
        assert(c->magic == CHANNEL_MAGIC);
        if (!c->handling_msg) {
            pthread_mutex_lock(&c->mutex);
            c->handling_msg = 1;
        }
        assert(c->event_posted);
        if (c->thread_exited && (c->socket < 0 || c->message_count == 0 || c->long_msg)) {
            void * res = NULL;
            c->event_posted = 0;
            c->handling_msg = 0;
            pthread_mutex_unlock(&c->mutex);
            if (c->thread) pthread_join(c->thread, &res);
            if (c->socket >= 0) send_eof_and_close(&c->chan, 0);
            tcp_unlock(&c->chan);
            return;
        }
        if (c->message_count == 0 || is_suspended(c)) {
            c->event_posted = 0;
            c->handling_msg = 0;
            pthread_mutex_unlock(&c->mutex);
            break;
        }
        if (set_trap(&trap)) {
            c->chan.cb->receive(&c->chan);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling protocol message: %d %s",
                trap.error, errno_to_str(trap.error));
            c->peek = MARKER_NULL;
            c->ibuf_out = c->ibuf_inp;
            c->message_count = 0;
            send_eof_and_close(&c->chan, trap.error);
        }
    }
    if (c->chan.bcg) {
        c->chan.bcg->out.flush(&c->chan.bcg->out);
    }
    else {
        c->chan.out.flush(&c->chan.out);
    }
}

static void channel_check_pending(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    assert(is_dispatch_thread());
    if (!is_suspended(channel)) {
        pthread_mutex_lock(&channel->mutex);
        if (channel->message_count > 0 && !channel->event_posted) {
            post_event(handle_channel_msg, channel);
            channel->event_posted = 1;
        }
        pthread_mutex_unlock(&channel->mutex);
    }
}

static int channel_get_message_count(Channel * c) {
    ChannelTCP * channel = channel2tcp(c);
    int cnt;
    assert(is_dispatch_thread());
    pthread_mutex_lock(&channel->mutex);
    cnt = channel->message_count;
    pthread_mutex_unlock(&channel->mutex);
    return cnt;
}

static void * stream_socket_handler(void * x) {
    int i;
    int esc = 0;
    ChannelTCP * channel = (ChannelTCP *)x;
    unsigned char pkt[BUF_SIZE];

    pthread_mutex_lock(&channel->mutex);
    while (!channel->eof && channel->socket >= 0) {
        int err = 0;
        int rd = 0;

        pthread_mutex_unlock(&channel->mutex);
        rd = recv(channel->socket, (void *)pkt, sizeof(pkt), 0);
        err = errno;
        assert(channel->magic == CHANNEL_MAGIC);
        pthread_mutex_lock(&channel->mutex);

        if (rd < 0) {
            trace(LOG_ALWAYS, "Can't read from socket: %s", errno_to_str(errno));
            if (channel->socket < 0) break;
            channel->eof = 1;
            break;
        }

        if (rd == 0) {
            trace(LOG_PROTOCOL, "Socket is shutdown by remote peer, channel 0x%08x", channel);
            channel->eof = 1;
            break;
        }

        for (i = 0; i < rd && !channel->eof; i++) {
            unsigned char ch = pkt[i];
            int ibuf_next = (channel->ibuf_inp + 1) % BUF_SIZE;
            while (ibuf_next == channel->ibuf_out) {
                if (channel->message_count == 0 && !channel->long_msg) {
                    channel->long_msg = 1;
                    channel->message_count = 1;
                    if (!is_suspended(channel) && !channel->event_posted) {
                        post_event(handle_channel_msg, channel);
                        channel->event_posted = 1;
                    }
                }
                assert(channel->message_count > 0);
                assert(!channel->waiting_data);
                assert(!channel->waiting_space);
                channel->waiting_space = 1;
                pthread_cond_wait(&channel->signal, &channel->mutex);
                assert(ibuf_next == (channel->ibuf_inp + 1) % BUF_SIZE);
                assert(!channel->waiting_space);
            }
            if (esc) {
                esc = 0;
                switch (ch) {
                case 0:
                    /* ESC byte */
                    break;
                case 1:
                    /* EOM - End Of Message */
                    if (channel->long_msg) {
                        channel->long_msg = 0;
                        assert(channel->message_count == 1);
                    }
                    else {
                        channel->message_count++;
                        if (!is_suspended(channel) && !channel->event_posted) {
                            post_event(handle_channel_msg, channel);
                            channel->event_posted = 1;
                        }
                    }
                    break;
                case 2:
                    /* EOS - End Of Stream */
                    trace(LOG_PROTOCOL, "End of stream on channel 0x%08x", channel);
                    channel->eof = 1;
                    break;
                default:
                    /* Invalid escape sequence */
                    trace(LOG_ALWAYS, "Protocol: Invalid escape sequence");
                    channel->eof = 1;
                    ch = 2;
                    break;
                }
            }
            else {
                esc = ch == ESC;
            }
            channel->ibuf[channel->ibuf_inp] = ch;
            channel->ibuf_inp = ibuf_next;
            if (channel->waiting_data) {
                assert(!channel->waiting_space);
                pthread_cond_signal(&channel->signal);
                channel->waiting_data = 0;
            }
        }
    }
    if (channel->waiting_data) {
        assert(!channel->waiting_space);
        pthread_cond_signal(&channel->signal);
        channel->waiting_data = 0;
    }
    if (!channel->event_posted) {
        post_event(handle_channel_msg, channel);
        channel->event_posted = 1;
    }
    channel->thread_exited = 1;
    pthread_mutex_unlock(&channel->mutex);
    return NULL;
}

static ChannelTCP * create_channel(int sock) {
    const int i = 1;
    ChannelTCP * c;

    if (setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&i, sizeof(i)) < 0) {
        trace(LOG_ALWAYS, "Can't set TCP_NODELAY option on a socket: %s", errno_to_str(errno));
        closesocket(sock);
        return NULL;
    }

    c = loc_alloc_zero(sizeof *c);
    c->magic = CHANNEL_MAGIC;
    pthread_mutex_init(&c->mutex, NULL);
    pthread_cond_init(&c->signal, NULL);
    c->chan.inp.read = read_stream;
    c->chan.inp.peek = peek_stream;
    c->chan.out.write = write_stream;
    c->chan.out.flush = flush_stream;
    c->chan.check_pending = channel_check_pending;
    c->chan.message_count = channel_get_message_count;
    c->chan.lock = tcp_lock;
    c->chan.unlock = tcp_unlock;
    c->chan.is_closed = tcp_is_closed;
    c->chan.close = send_eof_and_close;
    c->socket = sock;
    c->peek = MARKER_NULL;
    return c;
}

static void start_channel(ChannelTCP * c) {
    int error;

    if (c->chan.spg) list_add_last(&c->chan.susplink, &c->chan.spg->channels);
    if (c->chan.bcg) list_add_last(&c->chan.bclink, &c->chan.bcg->channels);

    tcp_lock(&c->chan);
    trace(LOG_PROTOCOL, "Starting channel 0x%08x", c);

    c->chan.cb->connecting(&c->chan);

    error = pthread_create(&c->thread, &pthread_create_attr, stream_socket_handler, c);
    if (error) {
        trace(LOG_ALWAYS, "Can't create a thread: %d %s", error, errno_to_str(error));
        send_eof_and_close(&c->chan, 0);
        tcp_unlock(&c->chan);
    }
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

struct NewChannelInfo {
    int sock;
    ServerTCP * si;
};

static void handle_channel_open(void * x) {
    struct NewChannelInfo * i = x;
    ChannelTCP * c;

    c = create_channel(i->sock);
    i->si->serv.cb->newConnection(&i->si->serv, &c->chan);
    start_channel(c);
    loc_free(i);
}

static void * tcp_server_socket_handler(void * x) {
    ServerTCP * si = x;

    while (si->sock >= 0) {
        struct NewChannelInfo *i;
        int sock = accept(si->sock, NULL, NULL);
        if (sock < 0) {
            if (si->sock < 0) {
                break;
            }
            trace(LOG_ALWAYS, "socket accept failed: %d %s", errno, errno_to_str(errno));
            continue;
        }
        i = loc_alloc(sizeof *i);
        i->sock = sock;
        i->si = si;
        post_event(handle_channel_open, i);
    }
    loc_free(si);
    return 0;
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

ChannelServer * channel_tcp_server(PeerServer * ps, ChannelServerCallbacks * cb, void * client_data) {
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
    si->serv.client_data = client_data;
    si->serv.cb = cb;
    si->serv.close = server_close;
    si->sock = sock;
    si->ps = ps;
    if (server_list.next == NULL) list_init(&server_list);
    if (list_is_empty(&server_list)) {
            post_event_with_delay(refresh_all_peer_server, NULL, REFRESH_TIME * 1000 * 1000);
    }
    list_add_last(&si->servlink, &server_list);
    refresh_peer_server(sock, ps);
    if (pthread_create(&si->server_thread, &pthread_create_attr, tcp_server_socket_handler, si) != 0) {
        perror("Can't create socket listener thread");
        return NULL;
    }
    return &si->serv;
}

Channel * channel_tcp_connect(PeerServer * ps, ChannelCallbacks * cb,
    void * client_data, TCFSuspendGroup * spg, TCFBroadcastGroup * bcg) {
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
    c->chan.cb = cb;
    c->chan.client_data = client_data;
    c->chan.spg = spg;
    c->chan.bcg = bcg;
    start_channel(c);
    return &c->chan;
}
