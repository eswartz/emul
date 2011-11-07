/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
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
#include <assert.h>
#include <framework/mdep-inet.h>
#include <framework/myalloc.h>
#include <services/pathmap.h>

char * canonic_path_map_file_name(const char * fnm) {
    static char * buf = NULL;
    static size_t buf_pos = 0;
    static size_t buf_max = 0;

    buf_pos = 0;
    for (;;) {
        char ch = *fnm++;
        if (ch == 0) break;
        if (ch == '\\') ch = '/';
        if (ch == '/' && buf_pos >= 2 && buf[buf_pos - 1] == '/') continue;
        if (ch == '/' && *fnm == 0 && buf_pos > 0 && buf[buf_pos - 1] != ':') break;
        if (ch == '.' && (buf_pos == 0 || buf[buf_pos - 1] == '/')) {
            if (*fnm == '/' || *fnm == '\\') {
                fnm++;
                continue;
            }
            if (buf_pos > 0 && *fnm == '.' && (fnm[1] == '/' || fnm[1] == '\\')) {
                unsigned j = buf_pos - 1;
                if (j > 0 && buf[j - 1] != '/') {
                    while (j > 0 && buf[j - 1] != '/') j--;
                    buf_pos = j;
                    fnm += 2;
                    continue;
                }
            }
        }
        if (buf_pos == 0 && ch >= 'a' && ch <= 'z' && *fnm == ':') {
            ch = ch - 'a' + 'A';
        }
        if (buf_pos + 1 >= buf_max) {
            buf_max += 0x100;
            buf = (char *)loc_realloc(buf, buf_max);
        }
        buf[buf_pos++] = ch;
    }
    buf[buf_pos] = 0;
    return buf;
}

#if SERVICE_PathMap

#include <stdio.h>
#include <sys/stat.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/exceptions.h>

typedef struct Listener Listener;
typedef struct PathMap PathMap;

struct Listener {
    PathMapEventListener * listener;
    void * args;
};

struct PathMapRule {
    PathMapRuleAttribute * attrs;
    char * src;
    char * dst;
    char * host;
    char * prot;
    char * ctx;
};

struct PathMap {
    LINK maps;
    Channel * channel;
    PathMapRule * rules;
    unsigned rules_cnt;
    unsigned rules_max;
};

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

void add_path_map_event_listener(PathMapEventListener * listener, void * args) {
    Listener * l = NULL;
    if (listener_cnt >= listener_max) {
        listener_max += 8;
        listeners = (Listener *)loc_realloc(listeners, listener_max * sizeof(Listener));
    }
    l = listeners + listener_cnt++;
    l->listener = listener;
    l->args = args;
}

void rem_path_map_event_listener(PathMapEventListener * listener) {
    unsigned i = 0;
    while (i < listener_cnt) {
        if (listeners[i++].listener == listener) {
            while (i < listener_cnt) {
                listeners[i - 1] = listeners[i];
                i++;
            }
            listener_cnt--;
            break;
        }
    }
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

static int update_rule(PathMapRule * r, PathMapRuleAttribute * new_attrs) {
    int diff = 0;
    PathMapRuleAttribute * old_attrs = r->attrs;
    PathMapRuleAttribute ** new_ref = &r->attrs;
    r->attrs = NULL;

    while (new_attrs != NULL) {
        PathMapRuleAttribute * new_attr = new_attrs;
        PathMapRuleAttribute * old_attr = old_attrs;
        PathMapRuleAttribute ** old_ref = &old_attrs;
        InputStream * buf_inp = NULL;
        ByteArrayInputStream buf;
        char * name = new_attr->name;

        new_attrs = new_attr->next;
        new_attr->next = NULL;
        while (old_attr && strcmp(old_attr->name, name)) {
            old_ref = &old_attr->next;
            old_attr = old_attr->next;
        }

        if (old_attr != NULL) {
            assert(old_attr == *old_ref);
            *old_ref = old_attr->next;
            old_attr->next = NULL;
            if (strcmp(old_attr->value, new_attr->value) == 0) {
                *new_ref = old_attr;
                new_ref = &old_attr->next;
                loc_free(new_attr->value);
                loc_free(new_attr->name);
                loc_free(new_attr);
                continue;
            }
            diff++;
            loc_free(old_attr->value);
            loc_free(old_attr->name);
            loc_free(old_attr);
            old_attr = NULL;
        }

        *new_ref = new_attr;
        new_ref = &new_attr->next;

        buf_inp = create_byte_array_input_stream(&buf, new_attr->value, strlen(new_attr->value));

        if (strcmp(name, PATH_MAP_SOURCE) == 0) {
            loc_free(r->src);
            r->src = json_read_alloc_string(buf_inp);
        }
        else if (strcmp(name, PATH_MAP_DESTINATION) == 0) {
            loc_free(r->dst);
            r->dst = json_read_alloc_string(buf_inp);
        }
        else if (strcmp(name, PATH_MAP_PROTOCOL) == 0) {
            loc_free(r->prot);
            r->prot = json_read_alloc_string(buf_inp);
        }
        else if (strcmp(name, PATH_MAP_HOST) == 0) {
            loc_free(r->host);
            r->host = json_read_alloc_string(buf_inp);
        }
        else if (strcmp(name, PATH_MAP_CONTEXT) == 0) {
            loc_free(r->ctx);
            r->ctx = json_read_alloc_string(buf_inp);
        }
    }

    while (old_attrs != NULL) {
        PathMapRuleAttribute * old_attr = old_attrs;
        char * name = old_attr->name;
        old_attrs = old_attr->next;

        if (strcmp(name, PATH_MAP_SOURCE) == 0) {
            loc_free(r->src);
            r->src = NULL;
        }
        else if (strcmp(name, PATH_MAP_DESTINATION) == 0) {
            loc_free(r->dst);
            r->dst = NULL;
        }
        else if (strcmp(name, PATH_MAP_PROTOCOL) == 0) {
            loc_free(r->prot);
            r->prot = NULL;
        }
        else if (strcmp(name, PATH_MAP_HOST) == 0) {
            loc_free(r->host);
            r->host = NULL;
        }
        else if (strcmp(name, PATH_MAP_CONTEXT) == 0) {
            loc_free(r->ctx);
            r->ctx = NULL;
        }

        loc_free(old_attr->value);
        loc_free(old_attr->name);
        loc_free(old_attr);
        diff++;
    }

    return diff;
}

static char * map_file_name(Context * ctx, PathMap * m, char * fnm, int mode) {
    unsigned i, j, k;
    static char buf[FILE_PATH_SIZE];

    for (i = 0; i < m->rules_cnt; i++) {
        PathMapRule * r = m->rules + i;
        char * src;
        struct stat st;
        if (r->src == NULL) continue;
        if (r->dst == NULL) continue;
        if (r->prot != NULL && strcasecmp(r->prot, "file")) continue;
        switch (mode) {
        case PATH_MAP_TO_LOCAL:
            if (r->host != NULL && !is_my_host(r->host)) continue;
            break;
        }
        if (r->ctx != NULL) {
            int ok = 0;
#if ENABLE_DebugContext
            Context * syms = context_get_group(ctx, CONTEXT_GROUP_SYMBOLS);
            if (syms != NULL) {
                ok = strcmp(r->ctx, syms->id) == 0;
                if (!ok && syms->name != NULL) {
                    ok = strcmp(r->ctx, syms->name) == 0;
                    if (!ok) ok = strcmp(r->ctx, context_full_name(syms)) == 0;
                }
            }
#endif
            if (!ok) continue;
        }
        src = canonic_path_map_file_name(r->src);
        k = strlen(src);
        if (strncmp(src, fnm, k)) continue;
        if (fnm[k] != 0 && fnm[k] != '/' && fnm[k] != '\\') continue;
        j = strlen(r->dst) - 1;
        if (fnm[k] != 0 && (r->dst[j] == '/' || r->dst[j] == '\\')) k++;
        snprintf(buf, sizeof(buf), "%s%s", r->dst, fnm + k);
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

void iterate_path_map_rules(Channel * channel, IteratePathMapsCallBack * callback, void * args) {
    PathMap * m = find_map(channel);
    if (m != NULL) {
        unsigned i;
        for (i = 0; i < m->rules_cnt; i++) {
            callback(m->rules + i, args);
        }
    }
}

PathMapRuleAttribute * get_path_mapping_attributes(PathMapRule * map) {
    return map->attrs;
}

PathMapRule * create_path_mapping(PathMapRuleAttribute * attrs) {
    PathMapRule * r = NULL;
    PathMap * m = find_map(NULL);

    if (m == NULL) {
        m = (PathMap *)loc_alloc_zero(sizeof(PathMap));
        list_add_first(&m->maps, &maps);
    }
    if (m->rules_cnt >= m->rules_max) {
        m->rules_max = m->rules_max ? m->rules_max * 2 : 8;
        m->rules = (PathMapRule *)loc_realloc(m->rules, m->rules_max * sizeof(*m->rules));
    }

    r = m->rules + m->rules_cnt++;
    memset(r, 0, sizeof(*r));
    if (update_rule(r, attrs)) path_map_event_mapping_changed(NULL);
    return r;
}

void change_path_mapping_attributes(PathMapRule * r, PathMapRuleAttribute * attrs) {
    if (update_rule(r, attrs)) path_map_event_mapping_changed(NULL);
}

/*
 * Delete a path mapping rule.
 */
extern void delete_path_mapping(PathMapRule * bp);

static void write_rule(OutputStream * out, PathMapRule * r) {
    unsigned i = 0;
    PathMapRuleAttribute * attr = r->attrs;

    write_stream(out, '{');
    while (attr != NULL) {
        if (i > 0) write_stream(out, ',');
        json_write_string(out, attr->name);
        write_stream(out, ':');
        write_string(out, attr->value);
        attr = attr->next;
        i++;
    }
    write_stream(out, '}');
}

static void read_rule_attrs(InputStream * inp, const char * name, void * args) {
    PathMapRuleAttribute *** list = (PathMapRuleAttribute ***)args;
    PathMapRuleAttribute * attr = (PathMapRuleAttribute *)loc_alloc_zero(sizeof(PathMapRuleAttribute));

    attr->name = loc_strdup(name);
    attr->value = json_read_object(inp);
    **list = attr;
    *list = &attr->next;
}

static void read_rule(InputStream * inp, void * args) {
    PathMap * m = (PathMap *)args;
    PathMapRule * r = NULL;
    PathMapRuleAttribute * attrs = NULL;
    PathMapRuleAttribute ** attr_list = &attrs;


    if (m->rules_cnt >= m->rules_max) {
        m->rules_max = m->rules_max ? m->rules_max * 2 : 8;
        m->rules = (PathMapRule *)loc_realloc(m->rules, m->rules_max * sizeof(*m->rules));
    }

    r = m->rules + m->rules_cnt;
    memset(r, 0, sizeof(*r));
    if (json_read_struct(inp, read_rule_attrs, &attr_list)) m->rules_cnt++;
    update_rule(r, attrs);
}

static void free_rule(PathMapRule * r) {
    loc_free(r->src);
    loc_free(r->dst);
    loc_free(r->host);
    loc_free(r->prot);
    loc_free(r->ctx);
    while (r->attrs != NULL) {
        PathMapRuleAttribute * attr = r->attrs;
        r->attrs = attr->next;
        loc_free(attr->name);
        loc_free(attr->value);
        loc_free(attr);
    }
    memset(r, 0, sizeof(PathMapRule));
}

void set_path_map(Channel * c, InputStream * inp) {
    PathMap * m = find_map(c);

    if (m == NULL) {
        m = (PathMap *)loc_alloc_zero(sizeof(PathMap));
        m->channel = c;
        list_add_first(&m->maps, &maps);
    }
    else {
        unsigned i;
        for (i = 0; i < m->rules_cnt; i++) free_rule(m->rules + i);
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
    unsigned i;
    PathMap * m = NULL;
    /* Keep path map over channel redirection */
    if (c->state == ChannelStateHelloReceived) return;
    m = find_map(c);
    if (m == NULL) return;
    list_remove(&m->maps);
    for (i = 0; i < m->rules_cnt; i++) free_rule(m->rules + i);
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
