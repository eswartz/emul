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
 * This module handles process/thread OS contexts and their state machine.
 */

#ifndef D_context_win32
#define D_context_win32

#include <config.h>
#include <framework/context.h>

extern HANDLE get_context_handle(Context * ctx);
extern HANDLE get_context_file_handle(Context * ctx);
extern HANDLE get_context_module_handle(Context * ctx);

extern DWORD64 get_context_base_address(Context * ctx);
extern DWORD64 get_context_module_address(Context * ctx);

extern int is_context_module_loaded(Context * ctx);
extern int is_context_module_unloaded(Context * ctx);

#endif /* D_context_win32 */
