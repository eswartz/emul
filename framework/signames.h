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
 * This module provides POSIX signal names and descriptions.
 */

#ifndef D_signames
#define D_signames

/* Return signal name */
extern const char * signal_name(int signal);

/* Return human readable signal description, or NULL if not available */
extern const char * signal_description(int signal);

/* Return OS exception code for a signal. For POSIX OSes, the code and signal values are same */
extern unsigned signal_code(int signal);

/* Return signal for OS exception code. For POSIX OSes, the code and signal values are same */
extern int get_signal_from_code(unsigned code);

#endif /* D_signames */
