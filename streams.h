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
 * Abstract byte stream. Bytes in the stream can be divided into groups - messages.
 */

#ifndef D_streams
#define D_streams

/* 
 * MARKER_EOM - end of message
 * MARKER_EOS - end of stream
 */
#define MARKER_EOM  (-1)
#define MARKER_EOS  (-2)
#define MARKER_NULL (-3)

typedef struct OutputStream OutputStream;

struct OutputStream {
    void (*write)(OutputStream * stream, int byte);
    void (*flush)(OutputStream * stream);
};

typedef struct InputStream InputStream;

struct InputStream {
    unsigned char *cur;
    unsigned char *end;
    int (*read)(InputStream * stream);
    int (*peek)(InputStream * stream);
};

#define read_stream(inp) (((inp)->cur < (inp)->end) ? *(inp)->cur++ : (inp)->read((inp)))
#define peek_stream(inp) (((inp)->cur < (inp)->end) ? *(inp)->cur : (inp)->peek((inp)))

#define write_stream(out, b) (out)->write((out), (b))

extern int (read_stream)(InputStream * inp);
extern int (peek_stream)(InputStream * inp);
extern void write_string(OutputStream * out, const char * str);
extern void write_stringz(OutputStream * out, const char * str);

#endif
