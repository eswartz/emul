/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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
 * This module implements handling of .debug_frame and .eh_frame sections.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#ifndef D_dwarfframe
#define D_dwarfframe

#include "config.h"

#if ENABLE_ELF

#include "context.h"
#include "dwarfcache.h"

/*
 * Lookup stack frame data in ELF file, in .debug_frame and .eh_frame sections.
 *
 * "frame" is current frame info, it should have frame->regs and frame->mask filled with
 * proper values before this function is called.
 *
 * "down" is next frame - moving from stack top to the bottom.
 *
 * The function uses register values in current frame to calculate frame address "frame->fp",
 * and calculate register values in the next frame.
 *
 * If frame data is not found the function does nothing.
 * In case of error reading frame data, the function throws an exception.
 */
extern void get_dwarf_stack_frame_info(Context * ctx, ELF_File * file, StackFrame * frame, StackFrame * down);

#endif /* ENABLE_ELF */

#endif /* D_dwarfframe */
