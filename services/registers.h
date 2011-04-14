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
 * TCF Registers - CPU registers access service.
 */

#ifndef D_registers
#define D_registers

#include <config.h>

#if SERVICE_Registers

#include <framework/protocol.h>
#include <framework/cpudefs.h>

/*
 * Notify clients about register value change.
 */
extern void send_event_register_changed(const char * id);

typedef struct RegistersEventListener {
    void (*register_changed)(Context * ctx, int frame, RegisterDefinition * def, void * args);
} RegistersEventListener;

/*
 * Add a listener for Registers service events.
 */
extern void add_registers_event_listener(RegistersEventListener * listener, void * args);

/*
 * Initialize registers service.
 */
extern void ini_registers_service(Protocol *, TCFBroadcastGroup *);

#endif /* SERVICE_Registers */

#endif /* D_registers */
