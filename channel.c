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
 * Implements input and output stream over TCP/IP transport and UDP based auto discovery.
 */

#if _WRS_KERNEL
#  include <vxWorks.h>
#endif
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include "mdep.h"
#include "tcf.h"
#include "channel.h"
#include "myalloc.h"
#include "protocol.h"
#include "events.h"
#include "exceptions.h"
#include "trace.h"
#include "link.h"
#include "json.h"

#define ESC 3
#define BUF_SIZE 0x1000
#define CHANNEL_MAGIC 0x87208956

typedef struct Channel Channel;

struct Channel {
    int magic;
    LINK link;

    int socket;
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
    InputStream inp;
    unsigned char ibuf[BUF_SIZE];
    int ibuf_inp;
    int ibuf_out;
    int eof;
    int peek;

    /* Output stream state */
    OutputStream out;
    char obuf[BUF_SIZE];
    int obuf_inp;
    int out_errno;
};

#define link2channel(A) ((Channel *)((char *)(A) - (int)&((Channel *)0)->link))

static int ip_port = 0;
static int tcp_server_socket = -1;
static int udp_server_socket = -1;
static pthread_t tcp_server_thread = 0;
static pthread_t udp_server_thread = 0;
static LINK channels;
static int suspended = 0;

static void write_all(OutputStream * out, int byte);
static void flush_all(OutputStream * out);
OutputStream broadcast_stream = { write_all, flush_all };

static ChannelCloseListener close_listeners[16];
static int close_listeners_cnt = 0;

static void delete_channel(Channel * c) {
    int i;
    trace(LOG_PROTOCOL, "Deleting channel 0x%08x", c);
    assert(c->lock_cnt == 0);
    for (i = 0; i < close_listeners_cnt; i++) {
        close_listeners[i](&c->inp, &c->out);
    }
    list_remove(&c->link);
    c->magic = 0;
    loc_free(c);
}

void stream_lock(OutputStream * out) {
    Channel * channel = (Channel *)((char *)out - offsetof(Channel, out));
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    channel->lock_cnt++;
}

void stream_unlock(OutputStream * out) {
    Channel * channel = (Channel *)((char *)out - offsetof(Channel, out));
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    assert(channel->lock_cnt > 0);
    channel->lock_cnt--;
    if (channel->lock_cnt == 0) delete_channel(channel);
}

int is_stream_closed(OutputStream * out) {
    Channel * channel = (Channel *)((char *)out - offsetof(Channel, out));
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    assert(channel->lock_cnt > 0);
    return channel->socket < 0;
}

static void flush_stream(OutputStream * out) {
    Channel * channel = (Channel *)((char *)out - offsetof(Channel, out));
    assert(is_dispatch_thread());
    assert(channel->magic == CHANNEL_MAGIC);
    if (channel->obuf_inp == 0) return;
    if (channel->socket < 0) return;
    if (channel->out_errno) return;
    if (send(channel->socket, channel->obuf, channel->obuf_inp, 0) < 0) {
        int err = errno;
        trace(LOG_PROTOCOL, "Can't sent() on channel 0x%08x: %d %s", channel, err, errno_to_str(err));
        channel->out_errno = err;
    }
    else {
        channel->obuf_inp = 0;
    }
}

static void flush_all(OutputStream * out) {
    LINK * l = channels.next;
    assert(is_dispatch_thread());
    assert(out == &broadcast_stream);
    while (l != &channels) {
        flush_stream(&link2channel(l)->out);
        l = l->next;
    }
}

static void write_stream(OutputStream * out, int byte) {
    Channel * channel = (Channel *)((char *)out - offsetof(Channel, out));
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

static void write_all(OutputStream * out, int byte) {
    LINK * l = channels.next;
    assert(is_dispatch_thread());
    assert(out == &broadcast_stream);
    while (l != &channels) {
        write_stream(&link2channel(l)->out, byte);
        l = l->next;
    }
}

static int read_byte(Channel * channel) {
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
    Channel * c = (Channel *)((char *)inp - offsetof(Channel, inp));

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
    Channel * c = (Channel *)((char *)inp - offsetof(Channel, inp));
    return c->peek = read_stream(inp);
}

static void send_eof_and_close(Channel * c, int err) {
    assert(c->magic == CHANNEL_MAGIC);
    write_stream(&c->out, MARKER_EOS);
    write_errno(&c->out, err);
    c->out.write(&c->out, MARKER_EOM);
    c->out.flush(&c->out);
    trace(LOG_PROTOCOL, "Closing socket, channel 0x%08x", c);
    closesocket(c->socket);
    c->socket = -1;
}

static void handle_channel_msg(void * x) {
    Trap trap;
    Channel * c = (Channel *)x;
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
            if (c->socket >= 0) send_eof_and_close(c, 0);
            stream_unlock(&c->out);
            return;
        }
        if (c->message_count == 0 || suspended) {
            c->event_posted = 0;
            c->handling_msg = 0;
            pthread_mutex_unlock(&c->mutex);
            break;
        }
        if (set_trap(&trap)) {
            handle_protocol_message(&c->inp, &c->out);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception handling protocol message: %d %s",
                trap.error, errno_to_str(trap.error));
            c->peek = MARKER_NULL;
            c->ibuf_out = c->ibuf_inp;
            c->message_count = 0;
            send_eof_and_close(c, trap.error);
        }
    }
    broadcast_stream.flush(&broadcast_stream);
}

static void * stream_socket_handler(void * x) {
    int i;
    int esc = 0;
    Channel * channel = (Channel *)x;
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
                    if (!suspended && !channel->event_posted) {
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
                        if (!suspended && !channel->event_posted) {
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

static void handle_channel_open(void * x) {
    const int i = 1;
    int socket = (int)x;
    Channel * c = NULL;
    int error;

    if (setsockopt(socket, IPPROTO_TCP, TCP_NODELAY, (char *)&i, sizeof(i)) < 0) {
        trace(LOG_ALWAYS, "Can't set TCP_NODELAY option on a socket: %s", errno_to_str(errno));
        closesocket(socket);
        return;
    }

    c = (Channel *)loc_alloc_zero(sizeof(Channel));
    c->magic = CHANNEL_MAGIC;
    pthread_mutex_init(&c->mutex, NULL);
    pthread_cond_init(&c->signal, NULL);
    c->socket = socket;
    c->inp.read = read_stream;
    c->inp.peek = peek_stream;
    c->out.write = write_stream;
    c->out.flush = flush_stream;
    c->peek = MARKER_NULL;

    list_add_last(&c->link, &channels);
    stream_lock(&c->out);
    trace(LOG_PROTOCOL, "Openned channel 0x%08x", c);

    send_hello_message(&c->out);
    flush_stream(&c->out);

    error = pthread_create(&c->thread, &pthread_create_attr, stream_socket_handler, c);
    if (error) {
        trace(LOG_ALWAYS, "Can't create a thread: %d %s", error, errno_to_str(error));
        send_eof_and_close(c, 0);
        stream_unlock(&c->out);
    }
}

static void * tcp_server_socket_handler(void * x) {
    for (;;) {
        struct sockaddr_in sockaddr;
        int i = sizeof(sockaddr);
        int socket = accept(tcp_server_socket, (struct sockaddr *)&sockaddr, &i);

        if (socket < 0) {
            trace(LOG_ALWAYS, "socket accept failed: %d %s", errno, errno_to_str(errno));
            continue;
        }
        post_event(handle_channel_open, (void *)socket);
    }
    return 0;
}

static void event_locator_hello(char * token, InputStream * inp, OutputStream * out) {
    if (inp->read(inp) != '[') exception(ERR_PROTOCOL);
    if (inp->peek(inp) == ']') {
        inp->read(inp);
    }
    else {
        while (1) {
            char ch;
            char service[256];
            json_read_string(inp, service, sizeof(service));
            // TODO: remember remote service names
            ch = inp->read(inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void app_char(char * buf, int * pos, char ch) {
    if (*pos < PKT_SIZE) buf[*pos] = ch;
    (*pos)++;
}

static void app_str(char * buf, int * pos, char * str) {
    while (*str) {
        if (*pos < PKT_SIZE) buf[*pos] = *str;
        (*pos)++;
        str++;
    }
}

static void udp_send_info2(struct sockaddr_in * src_addr, struct sockaddr_in * dst_addr) {
    char str_port[32];
    char buf[PKT_SIZE];
    int i = 0;

    buf[i++] = 'T';
    buf[i++] = 'C';
    buf[i++] = 'F';
    buf[i++] = '1';
    buf[i++] = UDP_ACK_INFO;
    buf[i++] = 0;
    buf[i++] = 0;
    buf[i++] = 0;
    snprintf(str_port, sizeof(str_port), "%d", ip_port);
    app_str(buf, &i, "ID=");
    app_str(buf, &i, "TCP:");
    app_str(buf, &i, inet_ntoa(src_addr->sin_addr));
    app_str(buf, &i, ":");
    app_str(buf, &i, str_port);
    app_char(buf, &i, 0);
    app_str(buf, &i, "Name=");
    app_str(buf, &i, "TCF Agent");
    app_char(buf, &i, 0);
    app_str(buf, &i, "OSName=");
    app_str(buf, &i, get_os_name());
    app_char(buf, &i, 0);
    app_str(buf, &i, "TransportName=TCP");
    app_char(buf, &i, 0);
    app_str(buf, &i, "Host=");
    app_str(buf, &i, inet_ntoa(src_addr->sin_addr));
    app_char(buf, &i, 0);
    app_str(buf, &i, "Port=");
    app_str(buf, &i, str_port);
    app_char(buf, &i, 0);
    if (sendto(udp_server_socket, buf, i, 0, (struct sockaddr *)dst_addr, sizeof(*dst_addr)) < 0) {
        trace(LOG_ALWAYS, "Can't send UDP packet to %s: %s",
                inet_ntoa(dst_addr->sin_addr), errno_to_str(errno));
    }
}

#define MAX(x,y) ((x) > (y) ? (x) : (y))

static void udp_send_info(struct sockaddr_in * addr) {
    /* If addr == NULL - broadcast */
#ifdef WIN32
    int i;
    MIB_IPADDRTABLE * info = (MIB_IPADDRTABLE *)loc_alloc(sizeof(MIB_IPADDRTABLE));
    ULONG out_buf_len = sizeof(MIB_IPADDRTABLE);
    DWORD ret_val = GetIpAddrTable(info, &out_buf_len, 0);
    if (ret_val == ERROR_INSUFFICIENT_BUFFER) {
        loc_free(info);
        info = (MIB_IPADDRTABLE *)loc_alloc(out_buf_len);
        ret_val = GetIpAddrTable(info, &out_buf_len, 0);
    }
    if (ret_val != NO_ERROR) {
        trace(LOG_ALWAYS, "GetIpAddrTable() error: %d\n", ret_val);
        loc_free(info);
        return;
    }
    for (i = 0; i < (int)info->dwNumEntries; i++) {
        unsigned src_net_addr = info->table[i].dwAddr;
        unsigned src_net_mask = info->table[i].dwMask;
        struct sockaddr_in src_addr;
        if (src_net_addr == 0) continue;
        memset(&src_addr, 0, sizeof src_addr);
        src_addr.sin_family = AF_INET;
        src_addr.sin_port = htons((short)ip_port);
        src_addr.sin_addr.s_addr = src_net_addr;
        if (addr == NULL) {
            struct sockaddr_in dst_addr;
            memset(&dst_addr, 0, sizeof dst_addr);
            dst_addr.sin_family = PF_INET;
            dst_addr.sin_port = htons((short)ip_port);
            dst_addr.sin_addr.s_addr = src_net_addr | ~src_net_mask;
            udp_send_info2(&src_addr, &dst_addr);
        }
        else if ((src_net_addr & src_net_mask) == (addr->sin_addr.s_addr & src_net_mask)) {
            udp_send_info2(&src_addr, addr);
        }
    }
    loc_free(info);
#else
    char if_bbf[0x2000]; 
    struct ifconf ifc;
    char * cp;

    memset(&ifc, 0, sizeof ifc); 
    ifc.ifc_len = sizeof if_bbf; 
    ifc.ifc_buf = if_bbf; 
    if (ioctl(udp_server_socket, SIOCGIFCONF, &ifc) < 0) { 
        trace(LOG_ALWAYS, "error: ioctl(SIOCGIFCONF) returned %d: %s", errno, errno_to_str(errno));
        return; 
    } 
    cp = (char *)ifc.ifc_req;
    while (cp < (char *)ifc.ifc_req + ifc.ifc_len) {
        struct ifreq * ifreq_addr = (struct ifreq *)cp;
        struct ifreq ifreq_mask = *ifreq_addr;
        unsigned src_net_addr, src_net_mask;
        cp += sizeof(ifreq_addr->ifr_name);
        cp += MAX(SA_LEN(&ifreq_addr->ifr_addr), sizeof(ifreq_addr->ifr_addr));
        if (ioctl(udp_server_socket, SIOCGIFNETMASK, &ifreq_mask) < 0) { 
            trace(LOG_ALWAYS, "error: ioctl(SIOCGIFNETMASK) returned %d: %s", errno, errno_to_str(errno));
            continue; 
        }
        src_net_addr = ((struct sockaddr_in *)&ifreq_addr->ifr_addr)->sin_addr.s_addr;
        src_net_mask = ((struct sockaddr_in *)&ifreq_mask.ifr_netmask)->sin_addr.s_addr;
        if (addr == NULL) {
            struct sockaddr_in dst_addr;
            memset(&dst_addr, 0, sizeof dst_addr);
            dst_addr.sin_family = AF_INET;
            dst_addr.sin_port = htons((short)ip_port);
            dst_addr.sin_addr.s_addr = src_net_addr | ~src_net_mask;
            udp_send_info2((struct sockaddr_in *)&ifreq_addr->ifr_addr, &dst_addr);
        }
        else if ((src_net_addr & src_net_mask) == (addr->sin_addr.s_addr & src_net_mask)) {
            udp_send_info2((struct sockaddr_in *)&ifreq_addr->ifr_addr, addr);
        }
    }
#endif
}

static void * udp_server_socket_handler(void * x) {
    char buf[PKT_SIZE];
    struct sockaddr_in addr;

    memset(&addr, 0, sizeof addr);

    udp_send_info(NULL);
    for (;;) {
        int addr_len = sizeof(addr);
        int rd = recvfrom(udp_server_socket, buf, PKT_SIZE, 0,
            (struct sockaddr *)&addr, &addr_len);
        if (rd < 0) {
            trace(LOG_ALWAYS, "UDP socket receive failed: %s", errno_to_str(errno));
            continue;
        }
        if (rd == 0) continue;
        if (buf[0] != 'T' || buf[1] != 'C' || buf[2] != 'F' || buf[3] != '1') {
            trace(LOG_ALWAYS, "Received malformed UDP packet");
            continue;
        }
        if (buf[4] == UDP_REQ_INFO) {
            udp_send_info(&addr);
        }
    }
    return NULL;
}

void channels_suspend(void) {
    assert(is_dispatch_thread());
    trace(LOG_PROTOCOL, "All channels suspended");
    suspended = 1;
}

int are_channels_suspended(void) {
    assert(is_dispatch_thread());
    return suspended;
}

void channels_resume(void) {
    LINK * l = channels.next;
    assert(is_dispatch_thread());
    assert(suspended);
    trace(LOG_PROTOCOL, "All channels resumed");
    suspended = 0;
    while (l != &channels) {
        Channel * c = link2channel(l);
        pthread_mutex_lock(&c->mutex);
        if (c->message_count > 0 && !c->event_posted) {
            post_event(handle_channel_msg, c);
            c->event_posted = 1;
        }
        pthread_mutex_unlock(&c->mutex);
        l = l->next;
    }
}

int channels_get_message_count(void) {
    int cnt = 0;
    LINK * l = channels.next;
    assert(is_dispatch_thread());
    while (l != &channels) {
        Channel * c = link2channel(l);
        pthread_mutex_lock(&c->mutex);
        cnt += c->message_count;
        pthread_mutex_unlock(&c->mutex);
        l = l->next;
    }
    return cnt;
}

void add_channel_close_listener(ChannelCloseListener listener) {
    assert(close_listeners_cnt < sizeof(close_listeners) / sizeof(ChannelCloseListener));
    close_listeners[close_listeners_cnt++] = listener;
}

void ini_channel_manager(int port) {
    const int i = 1;
    struct sockaddr_in sockaddr;

    add_event_handler("Locator", "Hello", event_locator_hello);

    list_init(&channels);
    ip_port = port;
    memset(&sockaddr, 0, sizeof sockaddr);
    sockaddr.sin_family = PF_INET;
    sockaddr.sin_port = htons((short)port);
    sockaddr.sin_addr.s_addr = htonl(INADDR_ANY);

    tcp_server_socket = socket(PF_INET, SOCK_STREAM, 0);
    if (tcp_server_socket < 0) {
        perror("Can't create a socket");
        exit(1);
    }
    /* Allow rapid reuse of this port. */
    if (setsockopt(tcp_server_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&i, sizeof(i)) < 0) {
        perror("Can't set options on a socket");
        exit(1);
    }
    if (bind(tcp_server_socket, (struct sockaddr *)&sockaddr, sizeof(sockaddr))) {
        perror("Can't bind server TCP socket address");
        exit(1);
    }
    if (listen(tcp_server_socket, 16)) {
        perror("Can't listen on server socket");
        exit(1);
    }
    if (pthread_create(&tcp_server_thread, &pthread_create_attr, tcp_server_socket_handler, 0) != 0) {
        perror("Can't create socket listener thread");
        exit(1);
    }

    udp_server_socket = socket(PF_INET, SOCK_DGRAM, 0);
    if (udp_server_socket < 0) {
        perror("Can't create a socket");
        exit(1);
    }
    if (setsockopt(udp_server_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&i, sizeof(i)) < 0) {
        perror("Can't set SO_REUSEADDR option on a socket");
        exit(1);
    }
    if (setsockopt(udp_server_socket, SOL_SOCKET, SO_BROADCAST, (char *)&i, sizeof(i)) < 0) {
        perror("Can't set SO_BROADCAST option on a socket");
        exit(1);
    }
    if (bind(udp_server_socket, (struct sockaddr *)&sockaddr, sizeof(sockaddr))) {
        perror("Can't bind server UDP socket address");
        exit(1);
    }
    if (pthread_create(&udp_server_thread, &pthread_create_attr, udp_server_socket_handler, 0) != 0) {
        perror("Can't create a thread");
        exit(1);
    }

#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif
}
