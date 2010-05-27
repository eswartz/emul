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
 *     Michael Sills-Lavoie(École Polytechnique de Montréal)  - ZeroCopy support
 *              *                         *            - tcp_splice_block_stream
 *******************************************************************************/

/*
 * Implements input and output stream over TCP/IP transport.
 */

#include <config.h>
#include <fcntl.h>
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <sys/stat.h>
#if ENABLE_SSL
#  include <openssl/ssl.h>
#  include <openssl/rand.h>
#  include <openssl/err.h>
#  ifndef _MSC_VER
#    include <dirent.h>
#  endif
#else
   typedef void SSL;
#endif
#include <framework/tcf.h>
#include <framework/channel.h>
#include <framework/channel_tcp.h>
#include <framework/myalloc.h>
#include <framework/protocol.h>
#include <framework/errors.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/peer.h>
#include <framework/ip_ifc.h>
#include <framework/asyncreq.h>
#include <framework/inputbuf.h>
#include <services/discovery.h>

#ifndef MSG_MORE
#define MSG_MORE 0
#endif

#if defined(_WRS_KERNEL)
/* Bug in VxWorks: send() crashes if buffer is too large */
#  define BUF_SIZE 0x100
#else
#  define BUF_SIZE 0x1000
#endif
#define CHANNEL_MAGIC 0x27208956
#define MAX_IFC 10

typedef struct ChannelTCP ChannelTCP;

struct ChannelTCP {
    Channel chan;           /* Public channel information - must be first */
    int magic;              /* Magic number */
    int socket;             /* Socket file descriptor */
    struct sockaddr addr;   /* Socket remote address */
    SSL * ssl;
    int lock_cnt;           /* Stream lock count, when > 0 channel cannot be deleted */
    int read_pending;       /* Read request is pending */
    unsigned char * read_buf;
    int read_buf_size;
    int read_done;

#if ENABLE_Splice
    int pipefd[2];          /* Pipe used to splice data between a fd and the channel */
#endif /* ENABLE_Splice */

    /* Input stream buffer */
    InputBuf ibuf;

    /* Output stream state */
    unsigned char obuf[BUF_SIZE];
    int out_errno;
    int out_flush_cnt;

    /* Async read request */
    AsyncReqInfo rdreq;
};

typedef struct ServerTCP ServerTCP;

struct ServerTCP {
    ChannelServer serv;
    int sock;
    struct sockaddr addr;
    PeerServer * ps;
    LINK servlink;
    AsyncReqInfo accreq;
};

#define channel2tcp(A)  ((ChannelTCP *)((char *)(A) - offsetof(ChannelTCP, chan)))
#define inp2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, inp)))
#define out2channel(A)  ((Channel *)((char *)(A) - offsetof(Channel, out)))
#define server2tcp(A)   ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, serv)))
#define servlink2tcp(A) ((ServerTCP *)((char *)(A) - offsetof(ServerTCP, servlink)))
#define ibuf2tcp(A)     ((ChannelTCP *)((char *)(A) - offsetof(ChannelTCP, ibuf)))

static LINK server_list;
static void tcp_channel_read_done(void * x);
static void handle_channel_msg(void * x);

#if ENABLE_SSL
#define ERR_SSL (STD_ERR_BASE + 200)
static const char * issuer_name = "TCF";
static const char * tcf_dir = "/etc/tcf";
static SSL_CTX * ssl_ctx = NULL;
static X509 * ssl_cert = NULL;
static RSA * rsa_key = NULL;

static void ini_ssl(void) {
    static int inited = 0;
    if (inited) return;
    OpenSSL_add_all_algorithms();
    SSL_load_error_strings();
    SSL_library_init();
    while (!RAND_status()) {
        struct timespec ts;
        clock_gettime(CLOCK_REALTIME, &ts);
        RAND_add(&ts.tv_nsec, sizeof(ts.tv_nsec), 0.1);
    }
    inited = 1;
}

static int certificate_verify_callback(int preverify_ok, X509_STORE_CTX * ctx) {
    char fnm[FILE_PATH_SIZE];
    DIR * dir = NULL;
    int err = 0;
    int found = 0;

    snprintf(fnm, sizeof(fnm), "%s/ssl", tcf_dir);
    if (!err && (dir = opendir(fnm)) == NULL) err = errno;
    while (!err && !found) {
        int l = 0;
        X509 * cert = NULL;
        FILE * fp = NULL;
        struct dirent * ent = readdir(dir);
        if (ent == NULL) break;
        l = strlen(ent->d_name);
        if (l < 5 || strcmp(ent->d_name + l -5 , ".cert") != 0) continue;
        snprintf(fnm, sizeof(fnm), "%s/ssl/%s", tcf_dir, ent->d_name);
        if (!err && (fp = fopen(fnm, "r")) == NULL) err = errno;
        if (!err && (cert = PEM_read_X509(fp, NULL, NULL, NULL)) == NULL) err = ERR_SSL;
        if (!err && fclose(fp) != 0) err = errno;
        if (!err && X509_cmp(X509_STORE_CTX_get_current_cert(ctx), cert) == 0) found = 1;
    }
    if (dir != NULL && closedir(dir) < 0 && !err) err = errno;
    if (err) trace(LOG_ALWAYS, "Cannot read certificate: %s",
        err == ERR_SSL ? ERR_error_string(ERR_get_error(), NULL) : errno_to_str(err));
    return err == 0 && found;
}
#endif /* ENABLE_SSL */

static void delete_channel(ChannelTCP * c) {
    trace(LOG_PROTOCOL, "Deleting channel %#lx", c);
    assert(c->lock_cnt == 0);
    assert(c->out_flush_cnt == 0);
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending == 0);
    assert(c->ibuf.handling_msg != HandleMsgTriggered);
    channel_clear_broadcast_group(&c->chan);
    c->magic = 0;
#if ENABLE_SSL
    if (c->ssl) SSL_free(c->ssl);
#endif /* ENABLE_SSL */
#if ENABLE_Splice
    close(c->pipefd[0]);
    close(c->pipefd[1]);
#endif /* ENABLE_Splice */
    loc_free(c->chan.peer_name);
    loc_free(c);
}

static void tcp_lock(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    c->lock_cnt++;
}

static void tcp_unlock(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->lock_cnt > 0);
    c->lock_cnt--;
    if (c->lock_cnt == 0) {
        assert(!c->read_pending);
        delete_channel(c);
    }
}

static int tcp_is_closed(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->lock_cnt > 0);
    return c->chan.state == ChannelStateDisconnected;
}

static void tcp_flush_with_flags(ChannelTCP * c, int flags) {
    unsigned char * p = c->obuf;
    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->chan.out.end == p + sizeof(c->obuf));
    if (c->chan.out.cur == p) return;
    assert(c->chan.out.cur >= p && c->chan.out.cur <= p + sizeof(c->obuf));
    if (c->chan.state == ChannelStateDisconnected || c->out_errno) {
        c->chan.out.cur = p;
        return;
    }
    while (p < c->chan.out.cur) {
        int wr = 0;
        if (c->ssl) {
#if ENABLE_SSL
            wr = SSL_write(c->ssl, p, c->chan.out.cur - p);
            if (wr <= 0) {
                int err = SSL_get_error(c->ssl, wr);
                if (err == SSL_ERROR_WANT_READ || err == SSL_ERROR_WANT_WRITE) {
                    struct timeval tv;
                    fd_set readfds;
                    fd_set writefds;
                    fd_set errorfds;
                    FD_ZERO(&readfds);
                    FD_ZERO(&writefds);
                    FD_ZERO(&errorfds);
                    if (err == SSL_ERROR_WANT_READ) FD_SET(c->socket, &readfds);
                    if (err == SSL_ERROR_WANT_WRITE) FD_SET(c->socket, &writefds);
                    FD_SET(c->socket, &errorfds);
                    tv.tv_sec = 10L;
                    tv.tv_usec = 0;
                    if (select(c->socket + 1, &readfds, &writefds, &errorfds, &tv) >= 0) continue;
                }
                trace(LOG_PROTOCOL, "Can't SSL_write() on channel %#lx: %s", c,
                    ERR_error_string(ERR_get_error(), NULL));
                c->out_errno = EIO;
                c->chan.out.cur = c->obuf;
                return;
            }
#else
            assert(0);
#endif
        }
        else {
            wr = send(c->socket, p, c->chan.out.cur - p, flags);
            if (wr < 0) {
                int err = errno;
                trace(LOG_PROTOCOL, "Can't send() on channel %#lx: %d %s", c, err, errno_to_str(err));
                c->out_errno = err;
                c->chan.out.cur = c->obuf;
                return;
            }
        }
        p += wr;
    }
    assert(p == c->chan.out.cur);
    c->chan.out.cur = c->obuf;
}

static void tcp_flush_event(void * x) {
    ChannelTCP * c = (ChannelTCP *)x;
    assert(c->magic == CHANNEL_MAGIC);
    if (--c->out_flush_cnt == 0) {
        tcp_flush_with_flags(c, 0);
        tcp_unlock(&c->chan);
    }
}

static void tcp_flush_stream(OutputStream * out) {
    ChannelTCP * c = channel2tcp(out2channel(out));
    assert(c->magic == CHANNEL_MAGIC);
    if (c->out_flush_cnt < 8) {
        if (c->out_flush_cnt++ == 0) tcp_lock(&c->chan);
        post_event(tcp_flush_event, c);
    }
}

static void tcp_write_stream(OutputStream * out, int byte) {
    ChannelTCP * c = channel2tcp(out2channel(out));
    assert(c->magic == CHANNEL_MAGIC);
    if (c->chan.state == ChannelStateDisconnected) return;
    if (c->out_errno) return;
    if (c->chan.out.cur == c->chan.out.end) tcp_flush_with_flags(c, MSG_MORE);
    *c->chan.out.cur++ = (char)(byte < 0 ? ESC : byte);
    if (byte < 0 || byte == ESC) {
        char esc = 0;
        if (byte == ESC) esc = 0;
        else if (byte == MARKER_EOM) esc = 1;
        else if (byte == MARKER_EOS) esc = 2;
        else assert(0);
        if (c->chan.state == ChannelStateDisconnected) return;
        if (c->out_errno) return;
        if (c->chan.out.cur == c->chan.out.end) tcp_flush_with_flags(c, MSG_MORE);
        *c->chan.out.cur++ = esc;
    }
    if (byte == MARKER_EOM) {
        int congestion_level = out2channel(out)->congestion_level;
        tcp_flush_stream(out);
        if (congestion_level > 0) usleep(congestion_level * 2500);
    }
}

static void tcp_write_block_stream(OutputStream * out, const char * bytes, size_t size) {
    size_t cnt = 0;

#if ENABLE_ZeroCopy
    ChannelTCP * c = channel2tcp(out2channel(out));

    if (!c->ssl && out->supports_zero_copy && size > 32) {
        /* Send the binary data escape seq */
        size_t n = size;
        if (c->chan.out.cur >= c->chan.out.end - 8) tcp_flush_with_flags(c, MSG_MORE);
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
        tcp_flush_with_flags(c, MSG_MORE);

        if (c->chan.state == ChannelStateDisconnected) return;
        if (c->out_errno) return;

        while (cnt < size) {
            int wr = send(c->socket, bytes + cnt, size - cnt, MSG_MORE);
            if (wr < 0) {
                int err = errno;
                trace(LOG_PROTOCOL, "Can't send() on channel %#lx: %d %s", c, err, errno_to_str(err));
                c->out_errno = err;
                return;
            }
            cnt += wr;
        }
        return;
    }
#endif /* ENABLE_ZeroCopy */

    while (cnt < size) write_stream(out, (unsigned char)bytes[cnt++]);
}

static int tcp_splice_block_stream(OutputStream * out, int fd, size_t size, off_t * offset) {
    assert(is_dispatch_thread());
    if (size == 0) return 0;
#if ENABLE_Splice
    {
        ChannelTCP * c = channel2tcp(out2channel(out));
        if (!c->ssl && out->supports_zero_copy) {
            int rd = splice(fd, offset, c->pipefd[1], NULL, size, SPLICE_F_MOVE);
            if (rd > 0) {
                /* Send the binary data escape seq */
                unsigned n = rd;
                if (c->chan.out.cur >= c->chan.out.end - 8) tcp_flush_with_flags(c, MSG_MORE);
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
                tcp_flush_with_flags(c, MSG_MORE);

                if (c->chan.state == ChannelStateDisconnected) return rd;
                if (c->out_errno) return rd;

                n = rd;
                while (n > 0) {
                    int wr = splice(c->pipefd[0], NULL, c->socket, NULL, n, SPLICE_F_MORE);

                    if (wr < 0) {
                        c->out_errno = errno;
                        trace(LOG_PROTOCOL, "Error in socket splice: %d %s", errno, errno_to_str(errno));
                        break;
                    }
                    n -= wr;
                }
            }
            return rd;
        }
    }
#endif /* ENABLE_Splice */
    {
        int rd = 0;
        char buffer[BUF_SIZE];
        if (size > BUF_SIZE) size = BUF_SIZE;
        if (offset != NULL) {
            rd = pread(fd, buffer, size, *offset);
            if (rd > 0) *offset += rd;
        }
        else {
            rd = read(fd, buffer, size);
        }
        if (rd > 0) tcp_write_block_stream(out, buffer, rd);
        return rd;
    }
}

static void tcp_post_read(InputBuf * ibuf, unsigned char * buf, int size) {
    ChannelTCP * c = ibuf2tcp(ibuf);

    if (c->read_pending) return;
    c->read_pending = 1;
    c->read_buf = buf;
    c->read_buf_size = size;
    if (c->ssl) {
#if ENABLE_SSL
        c->read_done = SSL_read(c->ssl, c->read_buf, c->read_buf_size);
        if (c->read_done > 0) {
            post_event(c->rdreq.done, &c->rdreq);
            return;
        }
        FD_ZERO(&c->rdreq.u.select.readfds);
        FD_ZERO(&c->rdreq.u.select.writefds);
        FD_ZERO(&c->rdreq.u.select.errorfds);
        FD_SET(c->socket, &c->rdreq.u.select.readfds);
        FD_SET(c->socket, &c->rdreq.u.select.errorfds);
        c->rdreq.u.select.timeout.tv_sec = 10;
#else
        assert(0);
#endif
    }
    else {
        c->read_done = 0;
        c->rdreq.u.sio.bufp = buf;
        c->rdreq.u.sio.bufsz = size;
    }
    async_req_post(&c->rdreq);
}

static void tcp_wait_read(InputBuf * ibuf) {
    ChannelTCP * c = ibuf2tcp(ibuf);

    /* Wait for read to complete */
    assert(c->lock_cnt > 0);
    assert(c->read_pending != 0);
    cancel_event(tcp_channel_read_done, &c->rdreq, 1);
    tcp_channel_read_done(&c->rdreq);
}

static int tcp_read_stream(InputStream * inp) {
    Channel * channel = inp2channel(inp);
    ChannelTCP * c = channel2tcp(channel);

    assert(c->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur++;
    return ibuf_get_more(&c->ibuf, inp, 0);
}

static int tcp_peek_stream(InputStream * inp) {
    Channel * channel = inp2channel(inp);
    ChannelTCP * c = channel2tcp(channel);

    assert(c->lock_cnt > 0);
    if (inp->cur < inp->end) return *inp->cur;
    return ibuf_get_more(&c->ibuf, inp, 1);
}

static void send_eof_and_close(Channel * channel, int err) {
    ChannelTCP * c = channel2tcp(channel);

    assert(c->magic == CHANNEL_MAGIC);
    if (channel->state == ChannelStateDisconnected) return;
    ibuf_flush(&c->ibuf, &c->chan.inp);
    if (c->ibuf.handling_msg == HandleMsgTriggered) {
        /* Cancel pending message handling */
        cancel_event(handle_channel_msg, c, 0);
        c->ibuf.handling_msg = HandleMsgIdle;
    }
    write_stream(&c->chan.out, MARKER_EOS);
    write_errno(&c->chan.out, err);
    write_stream(&c->chan.out, MARKER_EOM);
    tcp_flush_with_flags(c, 0);
    shutdown(c->socket, SHUT_WR);
    c->chan.state = ChannelStateDisconnected;
    tcp_post_read(&c->ibuf, c->read_buf, c->read_buf_size);
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
    ChannelTCP * c = (ChannelTCP *)x;
    int has_msg;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->ibuf.handling_msg == HandleMsgTriggered);
    assert(c->ibuf.message_count);

    has_msg = ibuf_start_message(&c->ibuf);
    if (has_msg <= 0) {
        if (has_msg < 0 && c->chan.state != ChannelStateDisconnected) {
            trace(LOG_PROTOCOL, "Socket is shutdown by remote peer, channel %#lx %s", c, c->chan.peer_name);
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
    ChannelTCP * c = channel2tcp(channel);

    assert(is_dispatch_thread());
    if (c->ibuf.handling_msg == HandleMsgIdle && c->ibuf.message_count) {
        post_event(handle_channel_msg, c);
        c->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static void tcp_trigger_message(InputBuf * ibuf) {
    ChannelTCP * c = ibuf2tcp(ibuf);

    assert(is_dispatch_thread());
    assert(c->ibuf.message_count > 0);
    if (c->ibuf.handling_msg == HandleMsgIdle) {
        post_event(handle_channel_msg, c);
        c->ibuf.handling_msg = HandleMsgTriggered;
    }
}

static int channel_get_message_count(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);
    assert(is_dispatch_thread());
    if (c->ibuf.handling_msg != HandleMsgTriggered) return 0;
    return c->ibuf.message_count;
}

static void tcp_channel_read_done(void * x) {
    AsyncReqInfo * req = (AsyncReqInfo *)x;
    ChannelTCP * c = (ChannelTCP *)req->client_data;
    int len = 0;

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->read_pending != 0);
    assert(c->lock_cnt > 0);
    c->read_pending = 0;
    if (c->ssl) {
#if ENABLE_SSL
        if (c->read_done > 0) {
            len = c->read_done;
        }
        else {
            len = SSL_read(c->ssl, c->read_buf, c->read_buf_size);
            if (len <= 0) {
                int err = SSL_get_error(c->ssl, len);
                if (err == SSL_ERROR_WANT_READ) {
                    tcp_post_read(&c->ibuf, c->read_buf, c->read_buf_size);
                    return;
                }
                trace(LOG_ALWAYS, "Can't SSL_read() on channel %#lx: %s", c,
                    ERR_error_string(ERR_get_error(), NULL));
                len = 0;
            }
        }
#else
        assert(0);
#endif
    }
    else {
        assert(c->read_buf == c->rdreq.u.sio.bufp);
        assert((size_t)c->read_buf_size == c->rdreq.u.sio.bufsz);
        len = c->rdreq.u.sio.rval;
        if (req->error) {
            if (c->chan.state != ChannelStateDisconnected) {
                trace(LOG_ALWAYS, "Can't read from socket: %s", errno_to_str(req->error));
            }
            len = 0; /* Treat error as eof */
        }
    }
    if (c->chan.state != ChannelStateDisconnected) {
        ibuf_read_done(&c->ibuf, len);
    }
    else if (len > 0) {
        tcp_post_read(&c->ibuf, c->read_buf, c->read_buf_size);
    }
    else {
        closesocket(c->socket);
        c->socket = -1;
        tcp_unlock(&c->chan);
    }
}

static void start_channel(Channel * channel) {
    ChannelTCP * c = channel2tcp(channel);

    assert(is_dispatch_thread());
    assert(c->magic == CHANNEL_MAGIC);
    assert(c->socket >= 0);
    if (c->chan.connecting) {
        c->chan.connecting(&c->chan);
    }
    else {
        trace(LOG_PROTOCOL, "channel server connecting");
        send_hello_message(&c->chan);
    }
    ibuf_trigger_read(&c->ibuf);
}

static ChannelTCP * create_channel(int sock, int en_ssl, int server) {
    const int i = 1;
    ChannelTCP * c = NULL;
    SSL * ssl = NULL;

    assert(sock >= 0);
    if (setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&i, sizeof(i)) < 0) {
        int error = errno;
        trace(LOG_ALWAYS, "Can't set TCP_NODELAY option on a socket: %s", errno_to_str(error));
        closesocket(sock);
        errno = error;
        return NULL;
    }
    if (setsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, (char *)&i, sizeof(i)) < 0) {
        int error = errno;
        trace(LOG_ALWAYS, "Can't set SO_KEEPALIVE option on a socket: %s", errno_to_str(error));
        closesocket(sock);
        errno = error;
        return NULL;
    }

    if (en_ssl) {
#if ENABLE_SSL
        long opts = 0;

        if (ssl_ctx == NULL) {
            ini_ssl();
            ssl_ctx = SSL_CTX_new(SSLv23_method());
            SSL_CTX_set_verify(ssl_ctx, SSL_VERIFY_PEER | SSL_VERIFY_FAIL_IF_NO_PEER_CERT, certificate_verify_callback);
        }

        if (ssl_cert == NULL) {
            int err = 0;
            char fnm[FILE_PATH_SIZE];
            FILE * fp = NULL;
            snprintf(fnm, sizeof(fnm), "%s/ssl/local.priv", tcf_dir);
            if (!err && (fp = fopen(fnm, "r")) == NULL) err = errno;
            if (!err && (rsa_key = PEM_read_RSAPrivateKey(fp, NULL, NULL, NULL)) == NULL) err = ERR_SSL;
            if (!err && fclose(fp) != 0) err = errno;
            snprintf(fnm, sizeof(fnm), "%s/ssl/local.cert", tcf_dir);
            if (!err && (fp = fopen(fnm, "r")) == NULL) err = errno;
            if (!err && (ssl_cert = PEM_read_X509(fp, NULL, NULL, NULL)) == NULL) err = ERR_SSL;
            if (!err && fclose(fp) != 0) err = errno;
            if (err) {
                trace(LOG_ALWAYS, "Cannot read server certificate: %s",
                    err == ERR_SSL ? ERR_error_string(ERR_get_error(), NULL) : errno_to_str(err));
                errno = ERR_SSL ? EINVAL : err;
                return NULL;
            }
        }

        if ((opts = fcntl(sock, F_GETFL, NULL)) < 0) return NULL;
        opts |= O_NONBLOCK;
        if (fcntl(sock, F_SETFL, opts) < 0) return NULL;
        ssl = SSL_new(ssl_ctx);
        SSL_set_fd(ssl, sock);
        SSL_use_certificate(ssl, ssl_cert);
        SSL_use_RSAPrivateKey(ssl, rsa_key);
        if (server) SSL_set_accept_state(ssl);
        else SSL_set_connect_state(ssl);
#endif
    }

    c = (ChannelTCP *)loc_alloc_zero(sizeof *c);
#if ENABLE_Splice
    if (pipe(c->pipefd) == -1) {
        int err = errno;
        loc_free(c);
        trace(LOG_ALWAYS, "Cannot create channel pipe : %s", strerror(err));
        errno = err;
        return NULL;
    }
#endif /* ENABLE_Splice */
    c->magic = CHANNEL_MAGIC;
    c->ssl = ssl;
    c->chan.inp.read = tcp_read_stream;
    c->chan.inp.peek = tcp_peek_stream;
    c->chan.out.cur = c->obuf;
    c->chan.out.end = c->obuf + sizeof(c->obuf);
    c->chan.out.write = tcp_write_stream;
    c->chan.out.flush = tcp_flush_stream;
    c->chan.out.write_block = tcp_write_block_stream;
    c->chan.out.splice_block = tcp_splice_block_stream;
    c->chan.state = ChannelStateStartWait;
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
    c->rdreq.done = tcp_channel_read_done;
    c->rdreq.client_data = c;
    if (c->ssl) {
#if ENABLE_SSL
        c->rdreq.type = AsyncReqSelect;
        c->rdreq.u.select.nfds = c->socket + 1;
#else
        assert(0);
#endif
    }
    else {
        c->rdreq.type = AsyncReqRecv;
        c->rdreq.u.sio.sock = c->socket;
        c->rdreq.u.sio.flags = 0;
    }
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
    char str_port[32];
    char str_host[64];
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
        const char * transport;
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
        inet_ntop(AF_INET, &src_addr, str_host, sizeof(str_host));
        snprintf(str_id, sizeof(str_id), "%s:%s:%s", transport, str_host, str_port);
        peer_server_addprop(ps2, loc_strdup("ID"), loc_strdup(str_id));
        peer_server_addprop(ps2, loc_strdup("Host"), loc_strdup(str_host));
        peer_server_addprop(ps2, loc_strdup("Port"), loc_strdup(str_port));
        peer_server_add(ps2, PEER_DATA_RETENTION_PERIOD);
    }
}

static void refresh_all_peer_server(void * x) {
    LINK * l;

    if (list_is_empty(&server_list)) return;
    l = server_list.next;
    while (l != &server_list) {
        ServerTCP * si = servlink2tcp(l);
        refresh_peer_server(si->sock, si->ps);
        l = l->next;
    }
    post_event_with_delay(refresh_all_peer_server, NULL, PEER_DATA_REFRESH_PERIOD * 1000000);
}

static void set_peer_addr(ChannelTCP * c, struct sockaddr * addr) {
    /* Create a human readable channel name that uniquely identifies remote peer */
    char name[128];
    char * fmt = (char *)(c->ssl != NULL ? "SSL:%s:%d" : "TCP:%s:%d");
    char nbuf[128];
    c->addr = *addr;
    snprintf(name, sizeof(name), fmt,
        inet_ntop(addr->sa_family, &((struct sockaddr_in *)addr)->sin_addr, nbuf, sizeof(nbuf)),
        ntohs(((struct sockaddr_in *)addr)->sin_port));
    c->chan.peer_name = loc_strdup(name);
}

static void tcp_server_accept_done(void * x) {
    AsyncReqInfo * req = (AsyncReqInfo *)x;
    ServerTCP * si = (ServerTCP *)req->client_data;
    ChannelTCP * c = NULL;
    int sock;
    struct sockaddr peer_addr;

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
    peer_addr = si->addr;
    async_req_post(req);
    c = create_channel(sock, strcmp(peer_server_getprop(si->ps, "TransportName", ""), "SSL") == 0, 1);
    if (c == NULL) return;
    set_peer_addr(c, &peer_addr);
    si->serv.new_conn(&si->serv, &c->chan);
}

static void server_close(ChannelServer * serv) {
    ServerTCP * s = server2tcp(serv);

    assert(is_dispatch_thread());
    if (s->sock < 0) return;
    list_remove(&s->servlink);
    peer_server_free(s->ps);
    closesocket(s->sock);
    s->sock = -1;
}

static void set_socket_buffer_sizes(int sock) {
    /* Buffer sizes need to be large enough to avoid deadlocking when agent connects to itself */
    int snd_buf = 4 * BUF_SIZE;
    int rcv_buf = 8 * BUF_SIZE;
    setsockopt(sock, SOL_SOCKET, SO_SNDBUF, (char *)&snd_buf, sizeof(snd_buf));
    setsockopt(sock, SOL_SOCKET, SO_RCVBUF, (char *)&rcv_buf, sizeof(rcv_buf));
}

ChannelServer * channel_tcp_server(PeerServer * ps) {
    int sock;
    int error;
    const char * reason = NULL;
    struct addrinfo hints;
    struct addrinfo * reslist = NULL;
    struct addrinfo * res = NULL;
    ServerTCP * si;
    const char * host = peer_server_getprop(ps, "Host", NULL);
    const char * port = peer_server_getprop(ps, "Port", NULL);
    int def_port = 0;
    char port_str[16];

    assert(is_dispatch_thread());
    if (port == NULL) {
        sprintf(port_str, "%d", DISCOVERY_TCF_PORT);
        port = port_str;
        def_port = 1;
    }
    memset(&hints, 0, sizeof hints);
    hints.ai_family = PF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    hints.ai_flags = AI_PASSIVE;
    error = loc_getaddrinfo(host, port, &hints, &reslist);
    if (error) {
        trace(LOG_ALWAYS, "getaddrinfo error: %s", loc_gai_strerror(error));
        set_gai_errno(error);
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
#if defined __linux__ || defined __SYMBIAN32__
        {
            const int i = 1;
            if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (char *)&i, sizeof(i)) < 0) {
                error = errno;
                reason = "setsockopt";
                closesocket(sock);
                sock = -1;
                continue;
            }
        }
#endif
        set_socket_buffer_sizes(sock);
        if (bind(sock, res->ai_addr, res->ai_addrlen)) {
            error = errno;
            if (def_port && res->ai_addr->sa_family == AF_INET) {
                struct sockaddr_in addr;
                trace(LOG_ALWAYS, "Cannot bind to default TCP port %d: %s",
                    DISCOVERY_TCF_PORT, errno_to_str(error));
                assert(sizeof(addr) >= res->ai_addrlen);
                memset(&addr, 0, sizeof(addr));
                memcpy(&addr, res->ai_addr, res->ai_addrlen);
                addr.sin_port = 0;
                error = 0;
                if (bind(sock, (struct sockaddr *)&addr, sizeof(addr))) {
                    error = errno;
                }
            }
            if (error) {
                reason = "bind";
                closesocket(sock);
                sock = -1;
                continue;
            }
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
        trace(LOG_ALWAYS, "Socket %s error: %s", reason, errno_to_str(error));
        errno = error;
        return NULL;
    }
    si = (ServerTCP *)loc_alloc_zero(sizeof *si);
    si->serv.close = server_close;
    si->sock = sock;
    si->ps = ps;
    if (server_list.next == NULL) list_init(&server_list);
    if (list_is_empty(&server_list)) {
        post_event_with_delay(refresh_all_peer_server, NULL, PEER_DATA_REFRESH_PERIOD * 1000000);
    }
    list_add_last(&si->servlink, &server_list);
    refresh_peer_server(sock, ps);

    si->accreq.done = tcp_server_accept_done;
    si->accreq.client_data = si;
    si->accreq.type = AsyncReqAccept;
    si->accreq.u.acc.sock = sock;
    si->accreq.u.acc.addr = &si->addr;
    si->accreq.u.acc.addrlen = sizeof(si->addr);
    async_req_post(&si->accreq);
    return &si->serv;
}

typedef struct ChannelConnectInfo {
    ChannelConnectCallBack callback;
    void * callback_args;
    int ssl;
    struct sockaddr peer_addr;
    size_t peer_addr_len;
    int sock;
    AsyncReqInfo req;
} ChannelConnectInfo;

static void channel_tcp_connect_done(void * args) {
    ChannelConnectInfo * info = (ChannelConnectInfo *)((AsyncReqInfo *)args)->client_data;
    if (info->req.error) {
        info->callback(info->callback_args, info->req.error, NULL);
        closesocket(info->sock);
    }
    else {
        ChannelTCP * c = create_channel(info->sock, info->ssl, 0);
        if (c == NULL) {
            info->callback(info->callback_args, errno, NULL);
            closesocket(info->sock);
        }
        else {
            set_peer_addr(c, &info->peer_addr);
            info->callback(info->callback_args, 0, &c->chan);
        }
    }
    loc_free(info);
}

void channel_tcp_connect(PeerServer * ps, ChannelConnectCallBack callback, void * callback_args) {
    int error = 0;
    const char * host = peer_server_getprop(ps, "Host", NULL);
    const char * port = peer_server_getprop(ps, "Port", NULL);
    struct addrinfo hints;
    struct addrinfo * reslist = NULL;
    struct addrinfo * res = NULL;
    ChannelConnectInfo * info = NULL;
    char port_str[16];

    if (port == NULL) {
        sprintf(port_str, "%d", DISCOVERY_TCF_PORT);
        port = port_str;
    }
    memset(&hints, 0, sizeof hints);
    hints.ai_family = PF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    error = loc_getaddrinfo(host, port, &hints, &reslist);
    if (error) error = set_gai_errno(error);
    if (!error) {
        info = (ChannelConnectInfo *)loc_alloc_zero(sizeof(ChannelConnectInfo));
        for (res = reslist; res != NULL; res = res->ai_next) {
            assert(sizeof(info->peer_addr) >= res->ai_addrlen);
            memcpy(&info->peer_addr, res->ai_addr, res->ai_addrlen);
            info->peer_addr_len = res->ai_addrlen;
            info->sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
            if (info->sock < 0) {
                error = errno;
            }
            else {
                set_socket_buffer_sizes(info->sock);
                error = 0;
                break;
            }
        }
        loc_freeaddrinfo(reslist);
    }
    if (!error && info->peer_addr_len == 0) error = ENOENT;
    if (error) {
        if (info != NULL) {
            if (info->sock >= 0) closesocket(info->sock);
            loc_free(info);
        }
        callback(callback_args, error, NULL);
    }
    else {
        info->callback = callback;
        info->callback_args = callback_args;
        info->ssl = strcmp(peer_server_getprop(ps, "TransportName", ""), "SSL") == 0;
        info->req.client_data = info;
        info->req.done = channel_tcp_connect_done;
        info->req.type = AsyncReqConnect;
        info->req.u.con.sock = info->sock;
        info->req.u.con.addr = &info->peer_addr;
        info->req.u.con.addrlen = info->peer_addr_len;
        async_req_post(&info->req);
    }
}

void generate_ssl_certificate(void) {
#if ENABLE_SSL
    char subject_name[256];
    char fnm[FILE_PATH_SIZE];
    X509 * cert = NULL;
    RSA * rsa = NULL;
    EVP_PKEY * rsa_key = NULL;
    ASN1_INTEGER * serial = NULL;
    X509_NAME * name = NULL;
    int err = 0;
    struct stat st;
    FILE * fp = NULL;

    ini_ssl();
    if (!err && (rsa = RSA_generate_key(2048, 3, NULL, (void *)"RSA")) == NULL) err = ERR_SSL;
    if (!err && !RSA_check_key(rsa)) err = ERR_SSL;
    if (!err && gethostname(subject_name, sizeof(subject_name)) != 0) err = errno;
    if (!err) {
        rsa_key = EVP_PKEY_new();
        EVP_PKEY_assign_RSA(rsa_key, rsa);
        cert = X509_new();
        X509_set_version(cert, 2L);
        serial = ASN1_INTEGER_new();
        ASN1_INTEGER_set(serial, 1);
        X509_set_serialNumber(cert, serial);
        ASN1_INTEGER_free(serial);
        X509_gmtime_adj(X509_get_notBefore(cert), 0L);
        X509_gmtime_adj(X509_get_notAfter(cert), 60 * 60 * 24 * 365L * 10L);
        name = X509_get_subject_name(cert);
        X509_NAME_add_entry_by_txt(name, "commonName", MBSTRING_ASC,
            (unsigned char *)subject_name, strlen(subject_name), -1, 0);
        name = X509_get_issuer_name(cert);
        X509_NAME_add_entry_by_txt(name, "commonName", MBSTRING_ASC,
            (unsigned char *)issuer_name, strlen(issuer_name), -1, 0);
    }
    if (!err && !X509_set_pubkey(cert, rsa_key)) err = ERR_SSL;
    if (!err) X509_sign(cert, rsa_key, EVP_md5());
    if (!err && !X509_verify(cert, rsa_key)) err = ERR_SSL;
    if (stat(tcf_dir, &st) != 0 && mkdir(tcf_dir, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH) != 0) err = errno;
    snprintf(fnm, sizeof(fnm), "%s/ssl", tcf_dir);
    if (stat(fnm, &st) != 0 && mkdir(fnm, S_IRWXU) != 0) err = errno;
    snprintf(fnm, sizeof(fnm), "%s/ssl/local.priv", tcf_dir);
    if (!err && (fp = fopen(fnm, "w")) == NULL) err = errno;
    if (!err && !PEM_write_PKCS8PrivateKey(fp, rsa_key, NULL, NULL, 0, NULL, NULL)) err = ERR_SSL;
    if (!err && fclose(fp) != 0) err = errno;
    if (!err && chmod(fnm, S_IRWXU) != 0) err = errno;
    snprintf(fnm, sizeof(fnm), "%s/ssl/local.cert", tcf_dir);
    if (!err && (fp = fopen(fnm, "w")) == NULL) err = errno;
    if (!err && !PEM_write_X509(fp, cert)) err = ERR_SSL;
    if (!err && fclose(fp) != 0) err = errno;
    if (!err && chmod(fnm, S_IRWXU) != 0) err = errno;
    if (err) {
        fprintf(stderr, "Cannot create server certificate: %s\n",
            err == ERR_SSL ? ERR_error_string(ERR_get_error(), NULL) : errno_to_str(err));
    }
    if (cert != NULL) X509_free(cert);
    if (rsa != NULL) RSA_free(rsa);
#else /* ENABLE_SSL */
    fprintf(stderr, "SSL support not available\n");
#endif /* ENABLE_SSL */
}
