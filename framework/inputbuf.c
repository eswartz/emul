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
 *******************************************************************************/

/*
 * Implements input buffer used by unbuffered channel transports.
 */

#include <config.h>
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <framework/exceptions.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/inputbuf.h>

static void ibuf_new_message(InputBuf * ibuf) {
    ibuf->message_count++;
    ibuf->trigger_message(ibuf);
}

static void ibuf_eof(InputBuf * ibuf) {
    if (!ibuf->eof) {
        /* Treat eof as a message */
        ibuf->eof = 1;
        ibuf_new_message(ibuf);
    }
}

static size_t ibuf_free_size(InputBuf * ibuf) {
    if (ibuf->eof) return 0;
    if (ibuf->stream->cur == ibuf->buf + ibuf->buf_size) {
        ibuf->stream->cur = ibuf->stream->end = ibuf->buf;
    }
    assert(ibuf->inp >= ibuf->buf && ibuf->inp < ibuf->buf + ibuf->buf_size);
    if (ibuf->stream->cur <= ibuf->inp) {
        size_t size = ibuf->buf + ibuf->buf_size - ibuf->inp;
        if (ibuf->stream->cur == ibuf->buf) size--;
        return size;
    }
    return ibuf->stream->cur - ibuf->inp - 1;
}

void ibuf_trigger_read(InputBuf * ibuf) {
    size_t size = ibuf_free_size(ibuf);
    if (size > 0) ibuf->post_read(ibuf, ibuf->inp, size);
}

int ibuf_get_more(InputBuf * ibuf, int peeking) {
    InputStream * inp = ibuf->stream;
    unsigned char * out = inp->cur;
    unsigned char * max;
    int ch;

    assert(ibuf->message_count > 0);
    assert(ibuf->handling_msg == HandleMsgActive);
    assert(out >= ibuf->buf && out <= ibuf->buf + ibuf->buf_size);
    assert(out == inp->end);
    for (;;) {
        if (out == ibuf->buf + ibuf->buf_size) out = ibuf->buf;
        if (out == ibuf->inp) {
            /* No data available */
            assert(ibuf->long_msg || ibuf->eof);
            inp->cur = out;
            if (ibuf->eof) return MARKER_EOS;
            assert(ibuf->message_count == 1);
            ibuf_trigger_read(ibuf);
            ibuf->wait_read(ibuf);
            out = inp->cur;
            continue;
        }

        /* Data available */
        ch = *out;

#if ENABLE_ZeroCopy
        if (ibuf->out_size_mode) {
            /* Reading the size of the bin data */
            assert(!ibuf->out_esc);
            ibuf->out_data_size |= (ch & 0x7f) << (ibuf->out_size_mode++ - 1) * 7;
            if ((ch & 0x80) == 0) ibuf->out_size_mode = 0;
            out++;
            continue;
        }
        if (ibuf->out_data_size > 0) {
            /* Reading the bin data */
            assert(!ibuf->out_esc);
            inp->cur = out;
            max = out + 1 <= ibuf->inp ? ibuf->inp : ibuf->buf + ibuf->buf_size;
            if ((size_t)(max - out) < ibuf->out_data_size) {
                ibuf->out_data_size -= max - out;
                out = max;
            }
            else {
                out += ibuf->out_data_size;
                ibuf->out_data_size = 0;
            }
            inp->end = out;
            if (!peeking) inp->cur++;
            ibuf_trigger_read(ibuf);
            return ch;
        }
#endif

        if (ibuf->out_esc) {
            switch (ch) {
            case 0:
                ch = ESC;
                break;
            case 1:
                ch = MARKER_EOM;
                if (!peeking) {
                    ibuf->message_count--;
                    ibuf->handling_msg = HandleMsgIdle;
                    if (ibuf->message_count) {
                        ibuf->trigger_message(ibuf);
                    }
                }
                break;
            case 2:
                ch = MARKER_EOS;
                break;
#if ENABLE_ZeroCopy
            case 3:
                ibuf->out_size_mode = 1;
                ibuf->out_data_size = 0;
                ibuf->out_esc = 0;
                out++;
                continue;
#endif
            }
            if (!peeking) {
                ibuf->out_esc = 0;
                out++;
            }
            inp->cur = inp->end = out;
            ibuf_trigger_read(ibuf);
            return ch;
        }
        if (ch != ESC) {
            /* Plain data - fast path */
            inp->cur = out++;
            max = out <= ibuf->inp ? ibuf->inp : ibuf->buf + ibuf->buf_size;
            while (out != max && *out != ESC) out++;
            inp->end = out;
            if (!peeking) inp->cur++;
            assert(inp->cur <= inp->end);
            ibuf_trigger_read(ibuf);
            return ch;
        }
        ibuf->out_esc = 1;
        out++;
    }
}

void ibuf_init(InputBuf * ibuf, InputStream * inp) {
    ibuf->stream = inp;
    ibuf->buf_size = 128 * MEM_USAGE_FACTOR;
    ibuf->buf = (unsigned char *)loc_alloc(ibuf->buf_size);
    inp->cur = inp->end = ibuf->inp = ibuf->buf;
#if ENABLE_ZeroCopy
    ibuf->out_data_size = ibuf->out_size_mode = 0;
    ibuf->inp_data_size = ibuf->inp_size_mode = 0;
#endif
}

void ibuf_flush(InputBuf * ibuf) {
    InputStream * inp = ibuf->stream;
    inp->cur = inp->end = ibuf->inp;
    ibuf->message_count = 0;
#if ENABLE_ZeroCopy
    ibuf->out_data_size = ibuf->out_size_mode = 0;
    ibuf->inp_data_size = ibuf->inp_size_mode = 0;
#endif
}

void ibuf_read_done(InputBuf * ibuf, size_t len) {
    unsigned char * inp;

    assert(len >= 0);
    if (len == 0) {
        ibuf_eof(ibuf);
        return;
    }
    assert(!ibuf->eof);

    /* Preprocess newly read data to count messages */
    inp = ibuf->inp;
    while (len > 0) {
        unsigned char ch;

#if ENABLE_ZeroCopy
        if (ibuf->inp_size_mode) {
            /* Reading the size of the bin data */
            assert(!ibuf->inp_esc);
            len--;
            ch = *inp++;
            if (inp == ibuf->buf + ibuf->buf_size) inp = ibuf->buf;
            ibuf->inp_data_size |= (ch & 0x7f) << (ibuf->inp_size_mode++ - 1) * 7;
            if ((ch & 0x80) == 0) ibuf->inp_size_mode = 0;
            continue;
        }
        if (ibuf->inp_data_size > 0) {
            assert(!ibuf->inp_esc);
            if (ibuf->inp_data_size < len) {
                len -= ibuf->inp_data_size;
                inp += ibuf->inp_data_size;
                ibuf->inp_data_size = 0;
            }
            else {
                ibuf->inp_data_size -= len;
                inp += len;
                len = 0;
            }
            if (inp >= ibuf->buf + ibuf->buf_size) inp -= ibuf->buf_size;
            continue;
        }
#endif

        len--;
        ch = *inp++;
        if (inp == ibuf->buf + ibuf->buf_size) inp = ibuf->buf;

        if (ibuf->inp_esc) {
            ibuf->inp_esc = 0;
            switch (ch) {
            case 0:
                /* ESC byte */
                break;
            case 1:
                /* EOM - End Of Message */
                if (ibuf->long_msg) {
                    ibuf->long_msg = 0;
                    assert(ibuf->message_count == 1);
                }
                else {
                    ibuf_new_message(ibuf);
                }
                break;
            case 2:
                /* EOS - End Of Stream */
                ibuf_eof(ibuf);
                break;
#if ENABLE_ZeroCopy
            case 3:
                /* Entering bin size mode */
                ibuf->inp_size_mode = 1;
                ibuf->inp_data_size = 0;
                break;
#endif
            default:
                /* Invalid escape sequence */
                trace(LOG_ALWAYS, "Protocol: Invalid escape sequence");
                ibuf_eof(ibuf);
                break;
            }
        }
        else if (ch == ESC) {
            ibuf->inp_esc = 1;
        }
    }

    ibuf->inp = inp;

#if ENABLE_ContextProxy
    if (!ibuf->eof && ibuf_free_size(ibuf) == 0 && ibuf->buf_size < 0x1000000) {
        /* Not running on a target - increase size of input buffer
           to accommodate very larges messages, up to 16MB */
        unsigned char * tmp = (unsigned char *)loc_alloc(ibuf->buf_size * 2);
        size_t size = ibuf->buf + ibuf->buf_size - ibuf->stream->cur;
        memcpy(tmp, ibuf->stream->cur, size);
        memcpy(tmp + size, ibuf->buf, ibuf->buf_size - size);
        loc_free(ibuf->buf);
        ibuf->buf = tmp;
        ibuf->inp = tmp + ibuf->buf_size - 1;
        ibuf->buf_size *= 2;
        ibuf->stream->cur = ibuf->stream->end = tmp;
    }
#endif

    if (ibuf_free_size(ibuf) > 0) {
        ibuf_trigger_read(ibuf);
    }
    else if (ibuf->message_count == 0) {
        /* Buffer full with incomplete message - start processing anyway.
           This will cause dispatch thread to block waiting for the rest of the message. */
        ibuf->long_msg = 1;
        ibuf_new_message(ibuf);
    }
}

int ibuf_start_message(InputBuf * ibuf) {
    assert(ibuf->handling_msg == HandleMsgTriggered);
    if (ibuf->message_count == 0) {
        ibuf->handling_msg = HandleMsgIdle;
        return 0;
    }
    if (ibuf->eof) {
        ibuf->handling_msg = HandleMsgIdle;
        return -1;
    }
    ibuf->handling_msg = HandleMsgActive;
    return 1;
}
