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
 *******************************************************************************/

/*
 * TCF service Line Numbers
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#ifndef D_linenumbers
#define D_linenumbers

#include <framework/protocol.h>
#include <framework/context.h>

typedef struct CodeArea {
    char * directory;
    char * file;
    ContextAddress start_address;
    int start_line;
    int start_column;
    ContextAddress end_address;
    int end_line;
    int end_column;
    int isa;
    int is_statement;
    int basic_block;
    int prologue_end;
    int epilogue_begin;
} CodeArea;

typedef void LineNumbersCallBack(CodeArea *, void *);

extern int line_to_address(Context * ctx, char * file, int line, int column, LineNumbersCallBack * client, void * args);

extern int address_to_line(Context * ctx, ContextAddress addr0, ContextAddress addr1, LineNumbersCallBack * client, void * args);

/*
 * Initialize Line Numbers service.
 */
extern void ini_line_numbers_service(Protocol *);
extern void ini_line_numbers_lib(void);


#endif /* D_linenumbers */
