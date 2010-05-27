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

void ibuf_trigger_read(InputBuf * ibuf) {
    int size;

    if (ibuf->full || ibuf->eof) return;
    if (ibuf->out <= ibuf->inp) size = ibuf->buf + INPUT_BUF_SIZE - ibuf->inp;
    else size = ibuf->out - ibuf->inp;
    ibuf->post_read(ibuf, ibuf->inp, size);
}

int ibuf_get_more(InputBuf * ibuf, InputStream * inp, int peeking) {
    unsigned char * out = inp->cur;
    unsigned char * max;
    int ch;

    assert(ibuf->message_count > 0);
    assert(ibuf->handling_msg == HandleMsgActive);
    assert(out >= ibuf->buf && out <= ibuf->buf + INPUT_BUF_SIZE);
    assert(out == inp->end);
    if (out == ibuf->buf + INPUT_BUF_SIZE) {
        inp->end = inp->cur = out = ibuf->buf;
    }
    if (out != ibuf->out) {
        /* Data read - update buf */
        ibuf->out = out;
        ibuf->full = 0;
        ibuf_trigger_read(ibuf);
    }
    for (;;) {
        if (out == ibuf->buf + INPUT_BUF_SIZE) out = ibuf->buf;
        if (out == ibuf->inp && !ibuf->full) {
            /* No data available */
            assert(ibuf->long_msg || ibuf->eof);
            if (ibuf->eof) return MARKER_EOS;
            assert(ibuf->message_count == 1);
            ibuf_trigger_read(ibuf);
            ibuf->wait_read(ibuf);
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
            max = out <= ibuf->inp ? ibuf->inp : ibuf->buf + INPUT_BUF_SIZE;
            if (max - out < ibuf->out_data_size) {
                ibuf->out_data_size -= max - out;
                out = max;
            }
            else {
                out += ibuf->out_data_size;
                ibuf->out_data_size = 0;
            }
            inp->end = out;
            if (!peeking) inp->cur++;
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
            return ch;
        }
        if (ch != ESC) {
            /* Plain data - fast path */
            inp->cur = out;
            max = out <= ibuf->inp ? ibuf->inp : ibuf->buf + INPUT_BUF_SIZE;
            while (out != max && *out != ESC) out++;
            inp->end = out;
            if (!peeking) inp->cur++;
            return ch;
        }
        ibuf->out_esc = 1;
        out++;
    }
}

void ibuf_init(InputBuf * ibuf, InputStream * inp) {
    inp->cur = inp->end = ibuf->out = ibuf->inp = ibuf->buf;
#if ENABLE_ZeroCopy
    ibuf->out_data_size = ibuf->out_size_mode = 0;
    ibuf->inp_data_size = ibuf->inp_size_mode = 0;
#endif
}

void ibuf_flush(InputBuf * ibuf, InputStream * inp) {
    inp->cur = inp->end = ibuf->out = ibuf->inp;
    ibuf->full = 0;
    ibuf->message_count = 0;
#if ENABLE_ZeroCopy
    ibuf->out_data_size = ibuf->out_size_mode = 0;
    ibuf->inp_data_size = ibuf->inp_size_mode = 0;
#endif
}

void ibuf_read_done(InputBuf * ibuf, int len) {
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
            if (inp == ibuf->buf + INPUT_BUF_SIZE) inp = ibuf->buf;
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
            if (inp >= ibuf->buf + INPUT_BUF_SIZE) inp -= INPUT_BUF_SIZE;
            continue;
        }
#endif

        len--;
        ch = *inp++;
        if (inp == ibuf->buf + INPUT_BUF_SIZE) inp = ibuf->buf;

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

    if (inp == ibuf->out) {
        ibuf->full = 1;
        if (ibuf->message_count == 0) {
            /* Buffer full with incomplete message - start processing anyway */
            ibuf->long_msg = 1;
            ibuf_new_message(ibuf);
        }
    }
    else {
        ibuf_trigger_read(ibuf);
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
