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

#include "config.h"

#if SERVICE_MemoryMap

#include <assert.h>
#include <errno.h>
#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#elif defined(__APPLE__)
#else
#  include <stdio.h>
#  include <linux/kdev_t.h>
#endif
#include "memorymap.h"
#include "myalloc.h"

typedef struct MemoryMap MemoryMap;

struct MemoryMap {
    unsigned region_cnt;
    unsigned region_max;
    MemoryRegion * regions;
};

static MemoryMapEventListener * event_listeners = NULL;

static void dispose_memory_map(MemoryMap * map) {
    unsigned i;

    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        assert(r->file == NULL);
        loc_free(r->file_name);
    }
    loc_free(map->regions);
    loc_free(map);
}

static void event_memory_map_changed(Context * ctx, void * client_data) {
    if (ctx->memory_map == NULL) return;
    dispose_memory_map((MemoryMap *)ctx->memory_map);
    ctx->memory_map = NULL;
}

#if defined(_WRS_KERNEL) || defined(WIN32) || defined(__APPLE__)

static MemoryMap * get_memory_map(Context * ctx) {
    errno = 0;
    return NULL;
}

#else

static MemoryMap * get_memory_map(Context * ctx) {
    char maps_file_name[FILE_PATH_SIZE];
    MemoryMap * map = NULL;
    FILE * file;

    if (ctx->pid != ctx->mem) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (ctx->memory_map != NULL) return (MemoryMap *)ctx->memory_map;

    snprintf(maps_file_name, sizeof(maps_file_name), "/proc/%d/maps", ctx->pid);
    if ((file = fopen(maps_file_name, "r")) == NULL) return NULL;
    map = loc_alloc_zero(sizeof(MemoryMap));
    for (;;) {
        unsigned long addr0 = 0;
        unsigned long addr1 = 0;
        unsigned long offset = 0;
        unsigned long dev_ma = 0;
        unsigned long dev_mi = 0;
        unsigned long inode = 0;
        char permissions[16];
        char file_name[FILE_PATH_SIZE];
        MemoryRegion * r = NULL;
        unsigned i = 0;

        int cnt = fscanf(file, "%lx-%lx %s %lx %lx:%lx %ld",
            &addr0, &addr1, permissions, &offset, &dev_ma, &dev_mi, &inode);
        if (cnt == 0 || cnt == EOF) break;

        for (;;) {
            int ch = fgetc(file);
            if (ch == '\n' || ch == EOF) break;
            if (i < FILE_PATH_SIZE - 1 && (ch != ' ' || i > 0)) {
                file_name[i++] = ch;
            }
        }
        file_name[i++] = 0;

        if (map->region_cnt >= map->region_max) {
            map->region_max += 8;
            map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
        }
        r = map->regions + map->region_cnt++;
        memset(r, 0, sizeof(MemoryRegion));
        r->addr = addr0;
        r->size = addr1 - addr0;
        r->file_offs = offset;
        r->dev = MKDEV(dev_ma, dev_mi);
        r->ino = (ino_t)inode;
        if (inode != 0 && file_name[0]) {
            r->file_name = loc_strdup(file_name);
        }
        for (i = 0; permissions[i]; i++) {
            switch (permissions[i]) {
            case 'r': r->flags |= MM_FLAG_R; break;
            case 'w': r->flags |= MM_FLAG_W; break;
            case 'x': r->flags |= MM_FLAG_X; break;
            }
        }
    }
    fclose(file);
    ctx->memory_map = map;
    return map;
}

#endif

void memory_map_get_regions(Context * ctx, MemoryRegion ** regions, unsigned * cnt) {
    MemoryMap * map = get_memory_map(ctx);
    if (map == NULL) {
        *regions = NULL;
        *cnt = 0;
    }
    else {
        *regions = map->regions;
        *cnt = map->region_cnt;
    }
}

void memory_map_event_module_loaded(Context * ctx) {
    MemoryMapEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    event_memory_map_changed(ctx, NULL);
    while (listener != NULL) {
        if (listener->module_loaded != NULL) {
            listener->module_loaded(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

void memory_map_event_code_section_ummapped(Context * ctx, ContextAddress addr, ContextAddress size) {
    MemoryMapEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    while (listener != NULL) {
        if (listener->code_section_ummapped != NULL) {
            listener->code_section_ummapped(ctx, addr, size, listener->client_data);
        }
        listener = listener->next;
    }
}

void memory_map_event_module_unloaded(Context * ctx) {
    MemoryMapEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    event_memory_map_changed(ctx, NULL);
    while (listener != NULL) {
        if (listener->module_unloaded != NULL) {
            listener->module_unloaded(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

void add_memory_map_event_listener(MemoryMapEventListener * listener, void * client_data) {
    listener->client_data = client_data;
    listener->next = event_listeners;
    event_listeners = listener;
}

void ini_memory_map_service(void) {
    static ContextEventListener listener = {
        NULL,
        event_memory_map_changed,
        NULL,
        NULL,
        event_memory_map_changed
    };
    add_context_event_listener(&listener, NULL);
}


#endif
