/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
 * This module implements DWARF expressions evaluation.
 */
#ifndef D_dwarfexpr
#define D_dwarfexpr

#include "context.h"
#include "dwarfcache.h"

extern U8_T dwarf_expression_addr(Context * ctx, int frame, ObjectInfo * obj);
extern int dwarf_expression_read(Context * ctx, int frame, ObjectInfo * obj, U1_T * buf, size_t size);
extern int dwarf_expression_write(Context * ctx, int frame, ObjectInfo * obj, U1_T * buf, size_t size);

#endif
