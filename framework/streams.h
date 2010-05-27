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
 *              *                         *                       - splice_block
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

#define ESC 3

typedef struct OutputStream OutputStream;

struct OutputStream {
    int supports_zero_copy; /* Stream supports block (zero copy) write */
    unsigned char * cur;
    unsigned char * end;
    void (*write)(OutputStream * stream, int byte);
    void (*write_block)(OutputStream * stream, const char * bytes, size_t size);
    int (*splice_block)(OutputStream * stream, int fd, size_t size, off_t * offset);
    void (*flush)(OutputStream * stream);
};

typedef struct InputStream InputStream;

struct InputStream {
    unsigned char * cur;
    unsigned char * end;
    int (*read)(InputStream * stream);
    int (*peek)(InputStream * stream);
};

#define read_stream(inp) (((inp)->cur < (inp)->end) ? *(inp)->cur++ : (inp)->read((inp)))
#define peek_stream(inp) (((inp)->cur < (inp)->end) ? *(inp)->cur : (inp)->peek((inp)))

#define write_stream(out, b) { OutputStream * _s_ = (out); int _x_ = (b); \
    if (_x_ > ESC && _s_->cur < _s_->end) *_s_->cur++ = (unsigned char)_x_; else _s_->write(_s_, _x_); }
#define write_block_stream(out, b, size) (out)->write_block((out), (b), (size))
#define splice_block_stream(out, fd, size, offset) (out)->splice_block((out), (fd), (size), (offset))
#define flush_stream(out) (out)->flush((out))

extern int (read_stream)(InputStream * inp);
extern int (peek_stream)(InputStream * inp);
extern void write_string(OutputStream * out, const char * str);
extern void write_stringz(OutputStream * out, const char * str);

/*
 * Implementation of an output stream in which the data is written into a byte array.
 * The buffer automatically grows as data is written to it.
 * The data can be retrieved using get_byte_array_output_stream_data().
 * Clients should dispose the data using loc_free().
 */
typedef struct ByteArrayOutputStream {
    OutputStream out;
    char buf[256];
    char * mem;
    size_t pos;
    size_t max;
} ByteArrayOutputStream;

extern OutputStream * create_byte_array_output_stream(ByteArrayOutputStream * buf);
extern void get_byte_array_output_stream_data(ByteArrayOutputStream * buf, char ** data, size_t * size);

/*
 * Implementation of an input stream that forwards all data being read
 * into an output stream.
 */
typedef struct ForwardingInputStream {
    InputStream fwd;
    InputStream * inp;
    OutputStream * out;
} ForwardingInputStream;

extern InputStream * create_forwarding_input_stream(ForwardingInputStream * buf,
                                                    InputStream * inp, OutputStream * out);

#endif /* D_streams */
