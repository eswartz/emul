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
 * Transport agnostic input buf interface
 */

#ifndef D_input_buf
#define D_input_buf

#include <config.h>
#include <framework/streams.h>

#define INPUT_BUF_SIZE 0x1000

typedef struct InputBuf InputBuf;

struct InputBuf {
    unsigned char buf[INPUT_BUF_SIZE];
    unsigned char * inp;
    unsigned char * out;
    int full;
    int inp_esc;
    int out_esc;
    int eof;
    int long_msg;           /* Message is longer then buffer, start handling before EOM */
    int message_count;      /* Number of messages waiting to be dispatched */
    int handling_msg;       /* Channel in the process of handling a message */
#if ENABLE_ZeroCopy
    int out_size_mode;      /* Checking the binary data size */
    int out_data_size;      /* Size of the bin data to get */
    int inp_size_mode;      /* (Read done) Checking the binary data size */
    int inp_data_size;      /* (Read done) Size of the bin data to get */
#endif
    void (*post_read)(InputBuf *, unsigned char *, int);
    void (*wait_read)(InputBuf *);
    void (*trigger_message)(InputBuf *);
};

enum {
    HandleMsgIdle,
    HandleMsgTriggered,
    HandleMsgActive
};

extern void ibuf_init(InputBuf * ibuf, InputStream * inp);
extern void ibuf_trigger_read(InputBuf * ibuf);
extern int ibuf_get_more(InputBuf * ibuf, InputStream * inp, int peeking);
extern void ibuf_flush(InputBuf * ibuf, InputStream * inp);
extern void ibuf_read_done(InputBuf * ibuf, int len);
extern int ibuf_start_message(InputBuf * ibuf);

#endif /* D_input_buf */
