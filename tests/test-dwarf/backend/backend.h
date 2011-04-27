/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
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

/* Fake debug context API implementation. It used for testing symbol services. */

#ifndef D_backend
#define D_backend

#include <config.h>
#include <framework/channel.h>

#define MAX_REGS 64

struct RegisterData {
    uint8_t data[MAX_REGS * 8];
    uint8_t mask[MAX_REGS * 8];
};

#endif /* D_backend */
