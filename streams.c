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

#include "streams.h"

void write_string(OutputStream * out, const char * str) {
    while (*str) out->write(out, (*str++) & 0xff);
}

void write_stringz(OutputStream * out, const char * str) {
    while (*str) out->write(out, (*str++) & 0xff);
    out->write(out, 0);
}
