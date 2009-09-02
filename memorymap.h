/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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

#include "config.h"
#include "context.h"

typedef struct MemoryRegion MemoryRegion;

struct MemoryRegion {
    ContextAddress addr;
    unsigned long size;
    unsigned long file_offs;
    dev_t dev;
    ino_t ino;
    char * file_name;
    unsigned flags;
    void * file;
};

#define MM_FLAG_R   1
#define MM_FLAG_W   2
#define MM_FLAG_X   4

extern void memory_map_get_regions(Context * ctx, MemoryRegion ** regions, unsigned * cnt);

extern void memory_map_event_module_loaded(Context * ctx);
extern void memory_map_event_code_section_ummapped(Context * ctx, ContextAddress addr, ContextAddress size);
extern void memory_map_event_module_unloaded(Context * ctx);

typedef struct MemoryMapEventListener {
    void (*module_loaded)(Context * ctx, void * client_data);
    void (*code_section_ummapped)(Context * ctx, ContextAddress addr, ContextAddress size, void * client_data);
    void (*module_unloaded)(Context * ctx, void * client_data);
    /* Private: */
    void * client_data;
    struct MemoryMapEventListener * next;
} MemoryMapEventListener;

extern void add_memory_map_event_listener(MemoryMapEventListener * listener, void * client_data);

extern void ini_memory_map_service(void);

#endif
