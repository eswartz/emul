/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * This module holds execution context memory maps.
 */
#ifndef D_memorymap
#define D_memorymap

#include <config.h>
#include <framework/context.h>
#include <framework/protocol.h>

/*
 * Get memory maps for given context.
 * 'client_map' returns map entries that are created by the agent clients.
 * 'target_map' returns map entries that the agent has found on a target.
 * Return -1 and set errno if the context memory map cannot be retrieved.
 */
extern int memory_map_get(Context * ctx, MemoryMap ** client_map, MemoryMap ** target_map);

/*
 * Functions that are used by context implementation to notify memory map services about map changes.
 */
extern void memory_map_event_module_loaded(Context * ctx);
extern void memory_map_event_code_section_ummapped(Context * ctx, ContextAddress addr, ContextAddress size);
extern void memory_map_event_module_unloaded(Context * ctx);

/*
 * Memory map listener.
 */
typedef struct MemoryMapEventListener {
    void (*module_loaded)(Context * ctx, void * client_data);
    void (*code_section_ummapped)(Context * ctx, ContextAddress addr, ContextAddress size, void * client_data);
    void (*module_unloaded)(Context * ctx, void * client_data);
} MemoryMapEventListener;

/*
 * Add memory map listener.
 */
extern void add_memory_map_event_listener(MemoryMapEventListener * listener, void * client_data);

extern void ini_memory_map_service(Protocol * proto, TCFBroadcastGroup * bcg);

#endif
