/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * TCF Memory - memory access service.
 */

#ifndef D_memoryservice
#define D_memoryservice

/*
 * Notify clients about memory content change.
 */
extern void send_event_memory_changed(Context * ctx, ContextAddress addr, unsigned long size);

/*
 * Initialize memory service.
 */
extern void ini_memory_service(Protocol * proto, TCFBroadcastGroup * bcg);


#endif /* D_memoryservice */
