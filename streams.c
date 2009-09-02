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
 * Abstract byte stream. Bytes in the stream can be divided into groups - messages.
 */

#include "config.h"
#include "streams.h"

int (read_stream)(InputStream * inp) {
    if (inp->cur < inp->end) return *inp->cur++;
    return inp->read(inp);
}

int (peek_stream)(InputStream * inp) {
    if (inp->cur < inp->end) return *inp->cur;
    return inp->peek(inp);
}

void write_string(OutputStream * out, const char * str) {
    while (*str) out->write(out, (*str++) & 0xff);
}

void write_stringz(OutputStream * out, const char * str) {
    while (*str) out->write(out, (*str++) & 0xff);
    out->write(out, 0);
}
