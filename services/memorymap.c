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

#include <config.h>

#if SERVICE_MemoryMap

#include <assert.h>
#include <errno.h>
#include <stdio.h>
#if defined(__linux__)
#  include <linux/kdev_t.h>
#endif
#if defined(_WRS_KERNEL)
#  include <moduleLib.h>
#endif
#if defined(_MSC_VER)
#  include <system/Windows/windbgcache.h>
#  include <system/Windows/context-win32.h>
#endif
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <services/memorymap.h>

typedef struct MemoryMap {
    int valid;
    unsigned region_cnt;
    unsigned region_max;
    MemoryRegion * regions;
    ErrorReport * error;
} MemoryMap;

typedef struct Listener {
    MemoryMapEventListener * listener;
    void * args;
} Listener;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((MemoryMap *)((char *)(ctx) + context_extension_offset))

static const char * MEMORYMAP = "MemoryMap";

static Listener * listeners = NULL;
static unsigned listener_cnt = 0;
static unsigned listener_max = 0;

static TCFBroadcastGroup * broadcast_group = NULL;

static void event_memory_map_changed(Context * ctx, void * args) {
    unsigned i;
    OutputStream * out;
    MemoryMap * map = NULL;

    ctx = ctx->mem;
    map = EXT(ctx);

    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        loc_free(r->file_name);
        loc_free(r->sect_name);
    }
    map->region_cnt = 0;

    if (!map->valid) return;
    map->valid = 0;

    out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, MEMORYMAP);
    write_stringz(out, "changed");

    json_write_string(out, ctx->id);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void event_context_disposed(Context * ctx, void * args) {
    unsigned i;
    MemoryMap * map = EXT(ctx);

    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        loc_free(r->file_name);
        loc_free(r->sect_name);
    }
    loc_free(map->regions);
    release_error_report(map->error);
    memset(map, 0, sizeof(MemoryMap));
}

#if defined(_WRS_KERNEL)

static int hooks_done = 0;

static void add_map_region(MemoryMap * map, void * addr, int size, unsigned flags, char * file, char * sect) {
    MemoryRegion * r = NULL;
    if (map->region_cnt >= map->region_max) {
        map->region_max += 8;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
    }
    r = map->regions + map->region_cnt++;
    memset(r, 0, sizeof(MemoryRegion));
    r->addr = (ContextAddress)addr;
    r->size = (ContextAddress)size;
    r->flags = flags;
    if (file != NULL) r->file_name = loc_strdup(file);
    if (sect != NULL) r->sect_name = loc_strdup(sect);
}

static int module_list_proc(MODULE_ID id, int args) {
    MODULE_INFO info;
    MemoryMap * map = (MemoryMap *)args;

    memset(&info, 0, sizeof(info));
    if (moduleInfoGet(id, &info) == OK) {
        char * file = id->nameWithPath;
        if (info.segInfo.textAddr != NULL && info.segInfo.textSize > 0) {
            add_map_region(map, info.segInfo.textAddr, info.segInfo.textSize, MM_FLAG_R | MM_FLAG_X, file, ".text");
        }
        if (info.segInfo.dataAddr != NULL && info.segInfo.dataSize > 0) {
            add_map_region(map, info.segInfo.dataAddr, info.segInfo.dataSize, MM_FLAG_R | MM_FLAG_W, file, ".data");
        }
        if (info.segInfo.bssAddr != NULL && info.segInfo.bssSize > 0) {
            add_map_region(map, info.segInfo.bssAddr, info.segInfo.bssSize, MM_FLAG_R | MM_FLAG_W, file, ".bss");
        }
    }
    return 0;
}

static void module_create_event(void * args) {
    LINK * l;
    for (l = context_root.next; l != &context_root; l = l->next) {
        Context * ctx = ctxl2ctxp(l);
        if (ctx->parent == NULL) event_memory_map_changed(ctx, NULL);
    }
}

static int module_create_func(MODULE_ID  id) {
    post_event(module_create_event, NULL);
    return 0;
}

static MemoryMap * get_memory_map(Context * ctx) {
    MemoryMap * map = NULL;
    if (!hooks_done) {
        hooks_done = 1;
        moduleCreateHookAdd(module_create_func);
    }
    ctx = ctx->mem;
    map = EXT(ctx);
    if (!map->valid && !ctx->exited) {
        moduleEach(module_list_proc, (int)map);
        map->valid = 1;
    }
    return map;
}

#elif defined(WIN32)

#if defined(_MSC_VER)
static void add_map_region(MemoryMap * map, DWORD64 addr, ULONG size, char * file) {
    MemoryRegion * r = NULL;
    if (map->region_cnt >= map->region_max) {
        map->region_max += 8;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
    }
    r = map->regions + map->region_cnt++;
    memset(r, 0, sizeof(MemoryRegion));
    r->addr = (ContextAddress)addr;
    r->size = (ContextAddress)size;
    r->file_name = loc_strdup(file);
}

static BOOL CALLBACK modules_callback(PCWSTR ModuleName, DWORD64 ModuleBase, ULONG ModuleSize, PVOID UserContext) {
    MemoryMap * map = (MemoryMap *)UserContext;
    static char * fnm_buf = NULL;
    static int fnm_max = 0;
    int fnm_len = 0;
    int fnm_err = 0;

    if (fnm_buf == NULL) {
        fnm_max = 256;
        fnm_buf = loc_alloc(fnm_max);
    }
    for (;;) {
        fnm_len = WideCharToMultiByte(CP_UTF8, 0, ModuleName, -1, fnm_buf, fnm_max - 1, NULL, NULL);
        if (fnm_len != 0) break;
        fnm_err = GetLastError();
        if (fnm_err != ERROR_INSUFFICIENT_BUFFER) {
            set_win32_errno(fnm_err);
            trace(LOG_ALWAYS, "Can't get module name: %s", errno_to_str(errno));
            return TRUE;
        }
        fnm_max *= 2;
        fnm_buf = loc_realloc(fnm_buf, fnm_max);
    }
    fnm_buf[fnm_len] = 0;

    add_map_region(map, ModuleBase, ModuleSize, fnm_buf);

    return TRUE;
}
#endif

static MemoryMap * get_memory_map(Context * ctx) {
    MemoryMap * map = NULL;

    ctx = ctx->mem;
    map = EXT(ctx);
    if (map->valid || ctx->exited) return map;

    release_error_report(map->error);
    map->error = NULL;
    map->region_cnt = 0;

#if defined(_MSC_VER)
    {
        HANDLE process = get_context_handle(ctx);
        map->region_cnt = 0;
        if (!EnumerateLoadedModulesW64(process, modules_callback, map)) {
            map->error = get_error_report(set_win32_errno(GetLastError()));
        }
        map->valid = 1;
    }
#endif

    return map;
}

#elif defined(__APPLE__) || defined(__FreeBSD__) || defined(__NetBSD__)

static MemoryMap * get_memory_map(Context * ctx) {
    return EXT(ctx->mem);
}

#else

static MemoryMap * get_memory_map(Context * ctx) {
    char maps_file_name[FILE_PATH_SIZE];
    MemoryMap * map = NULL;
    FILE * file = NULL;

    ctx = ctx->mem;
    map = EXT(ctx);
    if (map->valid || ctx->exited) return map;

    release_error_report(map->error);
    map->error = NULL;
    map->region_cnt = 0;

    snprintf(maps_file_name, sizeof(maps_file_name), "/proc/%d/maps", id2pid(ctx->id, NULL));
    if ((file = fopen(maps_file_name, "r")) == NULL) {
        map->error = get_error_report(errno);
        return map;
    }
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

        if (inode != 0 && file_name[0] && file_name[0] != '[') {
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
            r->file_name = loc_strdup(file_name);
            for (i = 0; permissions[i]; i++) {
                switch (permissions[i]) {
                case 'r': r->flags |= MM_FLAG_R; break;
                case 'w': r->flags |= MM_FLAG_W; break;
                case 'x': r->flags |= MM_FLAG_X; break;
                }
            }
        }
    }
    fclose(file);
    map->valid = 1;
    return map;
}

#endif

void memory_map_get_regions(Context * ctx, MemoryRegion ** regions, unsigned * cnt) {
    MemoryMap * map = get_memory_map(ctx);
    *regions = map->regions;
    *cnt = map->region_cnt;
    set_error_report_errno(map->error);
}

void memory_map_event_module_loaded(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(ctx->parent == NULL);
    event_memory_map_changed(ctx, NULL);
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->listener->module_loaded == NULL) continue;
        l->listener->module_loaded(ctx, l->args);
    }
}

void memory_map_event_code_section_ummapped(Context * ctx, ContextAddress addr, ContextAddress size) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(ctx->parent == NULL);
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->listener->code_section_ummapped == NULL) continue;
        l->listener->code_section_ummapped(ctx, addr, size, l->args);
    }
}

void memory_map_event_module_unloaded(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    event_memory_map_changed(ctx, NULL);
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->listener->module_unloaded == NULL) continue;
        l->listener->module_unloaded(ctx, l->args);
    }
}

void add_memory_map_event_listener(MemoryMapEventListener * listener, void * client_data) {
    Listener * l = NULL;
    if (listener_cnt >= listener_max) {
        listener_max += 8;
        listeners = (Listener *)loc_realloc(listeners, listener_max * sizeof(Listener));
    }
    l = listeners + listener_cnt++;
    l->listener = listener;
    l->args = client_data;
}

static void command_get(char * token, Channel * c) {
    char id[256];
    int err = 0;
    Context * ctx = NULL;
    MemoryMap * map = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    if (ctx == NULL) err = ERR_INV_COMMAND;
    else map = get_memory_map(ctx);

    if (!err) err = set_error_report_errno(map->error);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (map == NULL) {
        write_stringz(&c->out, "null");
    }
    else {
        unsigned n;
        write_stream(&c->out, '[');
        for (n = 0; n < map->region_cnt; n++) {
            MemoryRegion * m = map->regions + n;
            if (n > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '{');
            json_write_string(&c->out, "Addr");
            write_stream(&c->out, ':');
            json_write_uint64(&c->out, m->addr);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "Size");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, m->size);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "Flags");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, m->flags);
            if (m->file_name != NULL) {
                write_stream(&c->out, ',');
                json_write_string(&c->out, "FileName");
                write_stream(&c->out, ':');
                json_write_string(&c->out, m->file_name);
                write_stream(&c->out, ',');
                if (m->sect_name != NULL) {
                    json_write_string(&c->out, "SectionName");
                    write_stream(&c->out, ':');
                    json_write_string(&c->out, m->sect_name);
                }
                else {
                    json_write_string(&c->out, "Offs");
                    write_stream(&c->out, ':');
                    json_write_ulong(&c->out, m->file_offs);
                }
            }
            write_stream(&c->out, '}');
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

void ini_memory_map_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        NULL,
        event_memory_map_changed,
        NULL,
        NULL,
        event_memory_map_changed,
        event_context_disposed
    };
    broadcast_group = bcg;
    add_context_event_listener(&listener, NULL);
    add_command_handler(proto, MEMORYMAP, "get", command_get);
    context_extension_offset = context_extension(sizeof(MemoryMap));
}


#endif
