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
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <services/memorymap.h>

typedef struct Listener {
    MemoryMapEventListener * listener;
    void * args;
} Listener;

typedef struct ContextExtensionMM {
    int valid;
    ErrorReport * error;
    MemoryMap target_map;
    MemoryMap client_map;
} ContextExtensionMM;

typedef struct ClientMap {
    LINK link_list;
    LINK link_hash;
    LINK link_ctx;
    char * id;
    MemoryMap map;
    Channel * channel;
} ClientMap;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionMM *)((char *)(ctx) + context_extension_offset))

static const char MEMORY_MAP[] = "MemoryMap";

static Listener * listeners = NULL;
static unsigned listener_cnt = 0;
static unsigned listener_max = 0;

#define list2map(A)    ((ClientMap *)((char *)(A) - offsetof(ClientMap, link_list)))
#define hash2map(A)    ((ClientMap *)((char *)(A) - offsetof(ClientMap, link_hash)))
#define ctx2map(A)     ((ClientMap *)((char *)(A) - offsetof(ClientMap, link_ctx)))

#define MAP_HASH_SIZE (4 * MEM_USAGE_FACTOR - 1)
static LINK client_map_list;
static LINK client_map_hash[MAP_HASH_SIZE];

static TCFBroadcastGroup * broadcast_group = NULL;

static unsigned map_id_hash(const char * id) {
    int i;
    unsigned h = 0;
    for (i = 0; id[i]; i++) h += id[i];
    return h % MAP_HASH_SIZE;
}

static MemoryRegion * add_region(MemoryMap * map) {
    MemoryRegion * r = NULL;
    if (map->region_cnt >= map->region_max) {
        map->region_max += 8;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
    }
    r = map->regions + map->region_cnt++;
    memset(r, 0, sizeof(MemoryRegion));
    return r;
}

static int str_equ(char * x, char * y) {
    if (x == y) return 1;
    if (x == NULL) return 0;
    if (y == NULL) return 0;
    return strcmp(x, y) == 0;
}

static int update_context_client_map(Context * ctx) {
    ContextExtensionMM * ext = EXT(ctx);
    unsigned r_cnt = 0;
    int equ = 0;
    unsigned h, i;
    LINK * l;
    LINK maps;

    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_PROCESS));
    list_init(&maps);
    h = map_id_hash(ctx->id);
    for (l = client_map_hash[h].next; l != &client_map_hash[h]; l = l->next) {
        ClientMap * m = hash2map(l);
        if (strcmp(m->id, ctx->id) == 0) {
            list_add_last(&m->link_ctx, &maps);
            r_cnt += m->map.region_cnt;
        }
    }
    if (ctx->name != NULL) {
        h = map_id_hash(ctx->name);
        for (l = client_map_hash[h].next; l != &client_map_hash[h]; l = l->next) {
            ClientMap * m = hash2map(l);
            if (strcmp(m->id, ctx->name) == 0) {
                list_add_last(&m->link_ctx, &maps);
                r_cnt += m->map.region_cnt;
            }
        }
    }
    equ = ext->client_map.region_cnt == r_cnt;
    if (equ) {
        unsigned k = 0;
        for (l = maps.next; equ && l != &maps; l = l->next) {
            ClientMap * m = ctx2map(l);
            for (i = 0; equ && i < m->map.region_cnt; i++) {
                MemoryRegion * x = m->map.regions + i;
                MemoryRegion * y = ext->client_map.regions + k++;
                equ =
                    y->addr == x->addr &&
                    y->size == x->size &&
                    y->file_offs == x->file_offs &&
                    y->bss == x->bss &&
                    y->flags == x->flags &&
                    str_equ(y->file_name, x->file_name) &&
                    str_equ(y->sect_name, x->sect_name);
            }
        }
    }
    if (equ) return 0;

    context_clear_memory_map(&ext->client_map);
    for (l = maps.next; l != &maps; l = l->next) {
        ClientMap * m = ctx2map(l);
        for (i = 0; i < m->map.region_cnt; i++) {
            MemoryRegion * x = m->map.regions + i;
            MemoryRegion * y = add_region(&ext->client_map);
            y->addr = x->addr;
            y->size = x->size;
            y->file_offs = x->file_offs;
            y->bss = x->bss;
            y->flags = x->flags;
            if (x->file_name) y->file_name = loc_strdup(x->file_name);
            if (x->sect_name) y->sect_name = loc_strdup(x->sect_name);
            if (x->id) y->id = loc_strdup(x->id);
        }
    }

    return 1;
}

static void update_all_context_client_maps(void) {
    LINK * l;
    for (l = context_root.next; l != &context_root; l = l->next) {
        Context * ctx = ctxl2ctxp(l);
        if (ctx->exited) continue;
        if (ctx != context_get_group(ctx, CONTEXT_GROUP_PROCESS)) continue;
        if (update_context_client_map(ctx)) send_context_changed_event(ctx);
    }
}

static void event_memory_map_changed(Context * ctx, void * args) {
    OutputStream * out;
    ContextExtensionMM * ext = EXT(ctx);

    if (ctx != context_get_group(ctx, CONTEXT_GROUP_PROCESS)) return;
    update_context_client_map(ctx);
    context_clear_memory_map(&ext->target_map);
    if (!ext->valid) return;
    ext->valid = 0;

    if (ctx->exited) return;
    out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, MEMORY_MAP);
    write_stringz(out, "changed");

    json_write_string(out, ctx->id);
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
}

static void event_context_disposed(Context * ctx, void * args) {
    MemoryMap * map;
    ContextExtensionMM * ext = EXT(ctx);

    map = &ext->target_map;
    context_clear_memory_map(map);
    loc_free(map->regions);
    memset(map, 0, sizeof(MemoryMap));

    map = &ext->client_map;
    context_clear_memory_map(map);
    loc_free(map->regions);
    memset(map, 0, sizeof(MemoryMap));

    release_error_report(ext->error);
}

int memory_map_get(Context * ctx, MemoryMap ** client_map, MemoryMap ** target_map) {
    ContextExtensionMM * ext = EXT(ctx);
    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_PROCESS));
    if (!ext->valid) {
        context_clear_memory_map(&ext->target_map);
        release_error_report(ext->error);
        if (context_get_memory_map(ctx, &ext->target_map) < 0) {
            ext->error = get_error_report(errno);
            ext->valid = get_error_code(errno) != ERR_CACHE_MISS;
        }
        else {
            ext->error = NULL;
            ext->valid = 1;
        }
    }
    if (ext->error != NULL) {
        set_error_report_errno(ext->error);
        return -1;
    }
    *client_map = &ext->client_map;
    *target_map = &ext->target_map;
    return 0;
}

void memory_map_event_module_loaded(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_PROCESS));
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
    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_PROCESS));
    event_memory_map_changed(ctx, NULL);
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->listener->code_section_ummapped == NULL) continue;
        l->listener->code_section_ummapped(ctx, addr, size, l->args);
    }
}

void memory_map_event_module_unloaded(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(ctx == context_get_group(ctx, CONTEXT_GROUP_PROCESS));
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

static void write_map_region(OutputStream * out, MemoryRegion * m) {
    MemoryRegionAttribute * x = m->attrs;

    write_stream(out, '{');
    json_write_string(out, "Addr");
    write_stream(out, ':');
    json_write_uint64(out, m->addr);
    write_stream(out, ',');
    json_write_string(out, "Size");
    write_stream(out, ':');
    json_write_uint64(out, m->size);
    write_stream(out, ',');
    json_write_string(out, "Flags");
    write_stream(out, ':');
    json_write_ulong(out, m->flags);
    if (m->file_name != NULL) {
        write_stream(out, ',');
        json_write_string(out, "FileName");
        write_stream(out, ':');
        json_write_string(out, m->file_name);
        write_stream(out, ',');
        if (m->sect_name != NULL) {
            json_write_string(out, "SectionName");
            write_stream(out, ':');
            json_write_string(out, m->sect_name);
        }
        else {
            json_write_string(out, "Offs");
            write_stream(out, ':');
            json_write_uint64(out, m->file_offs);
        }
        if (m->bss) {
            write_stream(out, ',');
            json_write_string(out, "BSS");
            write_stream(out, ':');
            json_write_boolean(out, m->bss);
        }
    }
    if (m->id != NULL) {
        write_stream(out, ',');
        json_write_string(out, "ID");
        write_stream(out, ':');
        json_write_string(out, m->id);
    }
    while (x != NULL) {
        write_stream(out, ',');
        json_write_string(out, x->name);
        write_stream(out, ':');
        write_string(out, x->value);
        x = x->next;
    }
    write_stream(out, '}');
}

static void command_get(char * token, Channel * c) {
    char id[256];
    int err = 0;
    Context * ctx = NULL;
    MemoryMap * client_map = NULL;
    MemoryMap * target_map = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);

    if (!err && memory_map_get(ctx, &client_map, &target_map) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        unsigned n;
        unsigned cnt = 0;
        write_stream(&c->out, '[');
        for (n = 0; n < client_map->region_cnt; n++) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_map_region(&c->out, client_map->regions + n);
            cnt++;
        }
        for (n = 0; n < target_map->region_cnt; n++) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_map_region(&c->out, target_map->regions + n);
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void read_map_attribute(InputStream * inp, const char * name, void * args) {
    MemoryRegion * r = (MemoryRegion *)args;
    if (strcmp(name, "Addr") == 0) r->addr = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "Size") == 0) r->size = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "Offs") == 0) r->file_offs = json_read_uint64(inp);
    else if (strcmp(name, "BSS") == 0) r->bss = json_read_boolean(inp);
    else if (strcmp(name, "Flags") == 0) r->flags = (unsigned)json_read_long(inp);
    else if (strcmp(name, "FileName") == 0) r->file_name = json_read_alloc_string(inp);
    else if (strcmp(name, "SectionName") == 0) r->sect_name = json_read_alloc_string(inp);
    else if (strcmp(name, "ID") == 0) r->id = json_read_alloc_string(inp);
    else {
        MemoryRegionAttribute * x = (MemoryRegionAttribute *)loc_alloc(sizeof(MemoryRegionAttribute));
        x->name = loc_strdup(name);
        x->value = json_read_object(inp);
        x->next = r->attrs;
        r->attrs = x;
    }
}

static void read_map_item(InputStream * inp, void * args) {
    MemoryMap * map = (MemoryMap *)args;
    MemoryRegion * r = add_region(map);

    json_read_struct(inp, read_map_attribute, r);
}

static void command_set(char * token, Channel * c) {
    char id[256];
    unsigned h;
    ClientMap * cm = NULL;
    LINK * l = NULL;
    MemoryMap map;

    memset(&map, 0, sizeof(map));

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_array(&c->inp, read_map_item, &map);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    h = map_id_hash(id);
    for (l = client_map_hash[h].next; l != &client_map_hash[h]; l = l->next) {
        ClientMap * m = hash2map(l);
        if (m->channel == c && strcmp(m->id, id) == 0) {
            context_clear_memory_map(&m->map);
            loc_free(m->map.regions);
            cm = m;
            break;
        }
    }
    if (map.region_cnt > 0) {
        if (cm == NULL) {
            cm = (ClientMap *)loc_alloc_zero(sizeof(ClientMap));
            cm->id = loc_strdup(id);
            cm->channel = c;
            list_add_last(&cm->link_list, &client_map_list);
            list_add_last(&cm->link_hash, &client_map_hash[h]);
        }
        cm->map = map;
        update_all_context_client_maps();
    }
    else if (cm != NULL) {
        list_remove(&cm->link_list);
        list_remove(&cm->link_hash);
        loc_free(cm->id);
        loc_free(cm);
        update_all_context_client_maps();
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void channel_close_listener(Channel * c) {
    int notify = 0;
    LINK * l = client_map_list.next;
    while (l != &client_map_list) {
        ClientMap * m = list2map(l);
        l = l->next;
        if (m->channel != c) continue;
        list_remove(&m->link_list);
        list_remove(&m->link_hash);
        context_clear_memory_map(&m->map);
        loc_free(m->map.regions);
        loc_free(m->id);
        loc_free(m);
        notify = 1;
    }
    if (notify) update_all_context_client_maps();
}

void ini_memory_map_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    int i;
    static ContextEventListener listener = {
        event_memory_map_changed,
        NULL,
        NULL,
        NULL,
        event_memory_map_changed,
        event_context_disposed
    };
    broadcast_group = bcg;
    add_channel_close_listener(channel_close_listener);
    list_init(&client_map_list);
    for (i = 0; i < MAP_HASH_SIZE; i++) {
        list_init(&client_map_hash[i]);
    }
    add_context_event_listener(&listener, NULL);
    add_command_handler(proto, MEMORY_MAP, "get", command_get);
    add_command_handler(proto, MEMORY_MAP, "set", command_set);
    context_extension_offset = context_extension(sizeof(ContextExtensionMM));
}


#endif
