/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * Path Map service.
 * The service manages file path mapping rules.
 */

#include <config.h>

#if SERVICE_PathMap

#include <stdio.h>
#include <sys/stat.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <services/pathmap.h>

typedef struct Listener {
    PathMapEventListener * listener;
    void * args;
} Listener;

typedef struct PathMapRuleAttr {
    char * name;
    char * value;
    char * json;
} PathMapRuleAttr;

typedef struct PathMapRule {
    PathMapRuleAttr * attrs;
    unsigned attrs_cnt;
    unsigned attrs_max;
} PathMapRule;

typedef struct PathMap {
    LINK maps;
    Channel * channel;
    PathMapRule * rules;
    unsigned rules_cnt;
    unsigned rules_max;
} PathMap;

#define maps2map(x) ((PathMap *)((char *)(x) - offsetof(PathMap, maps)))

static const char PATH_MAP[] = "PathMap";

static int ini_done = 0;
static LINK maps;
static char host_name[256];

static Listener * listeners = NULL;
static unsigned listener_cnt = 0;
static unsigned listener_max = 0;

static void path_map_event_mapping_changed(Channel * c) {
    unsigned i;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->listener->mapping_changed == NULL) continue;
        l->listener->mapping_changed(c, l->args);
    }
}

void add_path_map_event_listener(PathMapEventListener * listener, void * client_data) {
    Listener * l = NULL;
    if (listener_cnt >= listener_max) {
        listener_max += 8;
        listeners = (Listener *)loc_realloc(listeners, listener_max * sizeof(Listener));
    }
    l = listeners + listener_cnt++;
    l->listener = listener;
    l->args = client_data;
}

static PathMap * find_map(Channel * c) {
    LINK * l;
    for (l = maps.next; l != &maps; l = l->next) {
        PathMap * m = maps2map(l);
        if (m->channel == c) return m;
    }
    return NULL;
}

static void flush_host_name(void * args) {
    memset(host_name, 0, sizeof(host_name));
}

static int is_my_host(char * host) {
    if (host == NULL || host[0] == 0) return 1;
    if (host_name[0] == 0) {
        gethostname(host_name, sizeof(host_name));
        if (host_name[0] != 0) post_event_with_delay(flush_host_name, NULL, 1000000);
    }
    return strcasecmp(host, host_name) == 0;
}

static char * map_file_name(Context * ctx, PathMap * m, char * fnm, int mode) {
    unsigned i, j, k;
    static char buf[FILE_PATH_SIZE];

    for (i = 0; i < m->rules_cnt; i++) {
        PathMapRule * r = m->rules + i;
        char * src = NULL;
        char * dst = NULL;
        char * host = NULL;
        char * prot = NULL;
        char * sctx = NULL;
        struct stat st;
        for (j = 0; j < r->attrs_cnt; j++) {
            char * nm = r->attrs[j].name;
            if (strcmp(nm, "Source") == 0) src = r->attrs[j].value;
            else if (strcmp(nm, "Destination") == 0) dst = r->attrs[j].value;
            else if (strcmp(nm, "Protocol") == 0) prot = r->attrs[j].value;
            else if (strcmp(nm, "Host") == 0) host = r->attrs[j].value;
            else if (strcmp(nm, "Context") == 0) sctx = r->attrs[j].value;
        }
        if (src == NULL || src[0] == 0) continue;
        if (dst == NULL || dst[0] == 0) continue;
        if (prot != NULL && prot[0] != 0 && strcasecmp(prot, "file")) continue;
        switch (mode) {
        case PATH_MAP_TO_LOCAL:
            if (host && !is_my_host(host)) continue;
            break;
        }
        if (sctx != NULL) {
            int ok = 0;
#if ENABLE_DebugContext
            Context * syms = context_get_group(ctx, CONTEXT_GROUP_SYMBOLS);
            if (syms != NULL) {
                ok = strcmp(sctx, syms->id) == 0;
                if (!ok && syms->name != NULL) {
                    ok = strcmp(sctx, syms->name) == 0;
                    if (!ok) ok = strcmp(sctx, context_full_name(syms)) == 0;
                }
            }
#endif
            if (!ok) continue;
        }
        k = strlen(src);
        if (strncmp(src, fnm, k)) continue;
        if (fnm[k] != 0 && fnm[k] != '/' && fnm[k] != '\\') continue;
        j = strlen(dst) - 1;
        if (fnm[k] != 0 && (dst[j] == '/' || dst[j] == '\\')) k++;
        snprintf(buf, sizeof(buf), "%s%s", dst, fnm + k);
        if (mode != PATH_MAP_TO_LOCAL || stat(buf, &st) == 0) return buf;
    }

    return fnm;
}

char * apply_path_map(Channel * c, Context * ctx, char * fnm, int mode) {
    if (c == NULL) {
        LINK * l = maps.next;
        while (l != &maps) {
            PathMap * m = maps2map(l);
            char * lnm = map_file_name(ctx, m, fnm, mode);
            if (lnm != fnm) return lnm;
            l = l->next;
        }
    }
    else {
        PathMap * m = find_map(c);
        if (m == NULL) return NULL;
        return map_file_name(ctx, m, fnm, mode);
    }
    return fnm;
}

static void write_rule(OutputStream * out, PathMapRule * r) {
    unsigned i = 0;

    write_stream(out, '{');
    for (i = 0; i < r->attrs_cnt; i++) {
        if (i > 0) write_stream(out, ',');
        json_write_string(out, r->attrs[i].name);
        write_stream(out, ':');
        if (r->attrs[i].value) json_write_string(out, r->attrs[i].value);
        else write_string(out, r->attrs[i].json);
    }
    write_stream(out, '}');
}

static void read_rule_attrs(InputStream * inp, const char * name, void * args) {
    PathMapRule * r = (PathMapRule *)args;

    if (r->attrs_cnt >= r->attrs_max) {
        r->attrs_max = r->attrs_max ? r->attrs_max * 2 : 4;
        r->attrs = (PathMapRuleAttr *)loc_realloc(r->attrs, r->attrs_max * sizeof(*r->attrs));
    }

    memset(r->attrs + r->attrs_cnt, 0, sizeof(*r->attrs));
    if (peek_stream(inp) == '"') {
        r->attrs[r->attrs_cnt].value = json_read_alloc_string(inp);
    }
    else {
        r->attrs[r->attrs_cnt].json = json_read_object(inp);
    }
    r->attrs[r->attrs_cnt++].name = loc_strdup(name);
}

static void read_rule(InputStream * inp, void * args) {
    PathMap * m = (PathMap *)args;
    PathMapRule * r = NULL;

    if (m->rules_cnt >= m->rules_max) {
        m->rules_max = m->rules_max ? m->rules_max * 2 : 8;
        m->rules = (PathMapRule *)loc_realloc(m->rules, m->rules_max * sizeof(*m->rules));
    }

    r = m->rules + m->rules_cnt;
    memset(r, 0, sizeof(*r));
    if (json_read_struct(inp, read_rule_attrs, r)) m->rules_cnt++;
}

void set_path_map(Channel * c, InputStream * inp) {
    PathMap * m = find_map(c);

    if (m == NULL) {
        m = (PathMap *)loc_alloc_zero(sizeof(PathMap));
        m->channel = c;
        list_add_first(&m->maps, &maps);
    }
    else {
        unsigned i, j;
        for (i = 0; i < m->rules_cnt; i++) {
            PathMapRule * r = m->rules + i;
            for (j = 0; j < r->attrs_cnt; j++) {
                loc_free(r->attrs[j].name);
                loc_free(r->attrs[j].value);
                loc_free(r->attrs[j].json);
            }
            loc_free(r->attrs);
            r->attrs_cnt = 0;
            r->attrs_max = 0;
        }
        m->rules_cnt = 0;
    }
    json_read_array(inp, read_rule, m);
    path_map_event_mapping_changed(c);
}

static void command_get(char * token, Channel * c) {
    PathMap * m = (PathMap *)find_map(c);

    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    if (m == NULL) {
        write_stringz(&c->out, "null");
    }
    else {
        unsigned i;
        write_stream(&c->out, '[');
        for (i = 0; i < m->rules_cnt; i++) {
            PathMapRule * r = m->rules + i;
            if (i > 0) write_stream(&c->out, ',');
            write_rule(&c->out, r);
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_set(char * token, Channel * c) {
    set_path_map(c, &c->inp);

    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void channel_close_listener(Channel * c) {
    unsigned i, j;
    PathMap * m = NULL;
    /* Keep path map over channel redirection */
    if (c->state == ChannelStateHelloReceived) return;
    m = find_map(c);
    if (m == NULL) return;
    list_remove(&m->maps);
    for (i = 0; i < m->rules_cnt; i++) {
        PathMapRule * r = m->rules + i;
        for (j = 0; j < r->attrs_cnt; j++) {
            loc_free(r->attrs[j].name);
            loc_free(r->attrs[j].value);
            loc_free(r->attrs[j].json);
        }
        loc_free(r->attrs);
    }
    loc_free(m->rules);
    loc_free(m);
}

void ini_path_map_service(Protocol * proto) {
    if (!ini_done) {
        ini_done = 1;
        list_init(&maps);
        add_channel_close_listener(channel_close_listener);
    }
    add_command_handler(proto, PATH_MAP, "get", command_get);
    add_command_handler(proto, PATH_MAP, "set", command_set);
}

#endif /* SERVICE_PathMap */
