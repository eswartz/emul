/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * Path Map service.
 * The service manages file path mapping rules.
 */

#include "config.h"

#if SERVICE_PathMap

#include "json.h"
#include "exceptions.h"
#include "myalloc.h"
#include "pathmap.h"

typedef struct PathMapRule {
    struct {
        char * name;
        char * value;
        char * json;
    } * attrs;
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

static LINK maps;

static PathMap * find_map(Channel * c) {
    LINK * l;
    for (l = maps.next; l != &maps; l = l->next) {
        PathMap * m = maps2map(l);
        if (m->channel == c) return m;
    }
    return NULL;
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

static void read_rule_attrs(InputStream * inp, char * name, void * args) {
    PathMapRule * r = args;

    if (r->attrs_cnt >= r->attrs_max) {
        r->attrs_max = r->attrs_max ? r->attrs_max * 2 : 8;
        r->attrs = loc_realloc(r->attrs, r->attrs_max * sizeof(*r->attrs));
    }

    memset(r->attrs + r->attrs_cnt, 0, sizeof(*r->attrs));
    if (peek_stream(inp) == '"') {
        r->attrs[r->attrs_cnt].value = json_read_alloc_string(inp);
    }
    else {
        r->attrs[r->attrs_cnt].json = json_skip_object(inp);
    }
    r->attrs[r->attrs_cnt++].name = loc_strdup(name);
}

static void read_rule(InputStream * inp, void * args) {
    PathMap * m = args;
    PathMapRule * r = NULL;

    if (m->rules_cnt >= m->rules_max) {
        m->rules_max = m->rules_max ? m->rules_max * 2 : 8;
        m->rules = loc_realloc(m->rules, m->rules_max * sizeof(*m->rules));
    }

    r = m->rules + m->rules_cnt;
    memset(r, 0, sizeof(*r));
    if (json_read_struct(inp, read_rule_attrs, r)) m->rules_cnt++;
}

static void command_get(char * token, Channel * c) {
    PathMap * m = find_map(c);

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
    PathMap * m = find_map(c);

    if (m == NULL) {
        m = loc_alloc_zero(sizeof(PathMap));
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
            r->attrs_cnt = 0;
        }
        m->rules_cnt = 0;
    }

    json_read_array(&c->inp, read_rule, m);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void channel_close_listener(Channel * c) {
    unsigned i, j;
    PathMap * m = find_map(c);
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

void ini_path_map_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    list_init(&maps);
    add_channel_close_listener(channel_close_listener);
    add_command_handler(proto, PATH_MAP, "get", command_get);
    add_command_handler(proto, PATH_MAP, "set", command_set);
}

#endif /* SERVICE_PathMap */
