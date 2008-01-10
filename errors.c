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
 * This module defines agent error codes in addition to system codes defined in errno.h
 */

#include <string.h>
#include "errors.h"

char * errno_to_str(int err) {
    switch (err) {
    case ERR_ALREADY_STOPPED:
        return "Already stopped";
    case ERR_ALREADY_EXITED:
        return "Already exited";
    case ERR_ALREADY_RUNNING:
        return "Already running";
    case ERR_JSON_SYNTAX:
        return "JSON syntax error";
    case ERR_PROTOCOL:
        return "Protocol format error";
    case ERR_INV_CONTEXT:
        return "Invalid context ID";
    case ERR_INV_ADDRESS:
        return "Invalid address";
    case ERR_EOF:
        return "End of file";
    case ERR_BASE64:
        return "Invalid BASE64 string";
    case ERR_INV_EXPRESSION:
        return "Invalid expression";
    case ERR_SYM_NOT_FOUND:
        return "Symbol not found";
    case ERR_ALREADY_ATTACHED:
        return "Already attached";
    case ERR_BUFFER_OVERFLOW:
        return "Buffer overflow";
    case ERR_INV_FORMAT:
        return "Format is not supported";
    case ERR_INV_NUMBER:
        return "Invalid number";
    case ERR_IS_RUNNING:
        return "Execution context is running";
    case ERR_DWARF:
        return "Error reading DWARF data";
    default:
        return strerror(err);
    }
}
