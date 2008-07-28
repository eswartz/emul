/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
 * Transport agnostic input buf interface
 */

#ifndef D_input_buf
#define D_input_buf

#include "streams.h"

#define ESC 3
#define BUF_SIZE 0x1000

typedef struct InputBuf InputBuf;

struct InputBuf {
    unsigned char buf[BUF_SIZE];
    unsigned char * inp;
    unsigned char * out;
    int full;
    int esc;
    int eof;
    int long_msg;           /* Message is longer then buffer, start handling before EOM */
    int message_count;      /* Number of messages waiting to be dispatched */
    int handling_msg;       /* Channel in the process of handling a message */
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

#endif
