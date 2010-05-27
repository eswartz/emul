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
 * Agent main module.
 */

#include <config.h>

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

#if ENABLE_LUA

#include <errno.h>
#include <assert.h>
#include <signal.h>

#ifdef __cplusplus
extern "C" {
#endif
#include <lualib.h>
#include <lauxlib.h>
#ifdef __cplusplus
}
#endif

#include <framework/asyncreq.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <framework/channel.h>
#include <framework/protocol.h>
#include <framework/proxy.h>
#include <framework/myalloc.h>
#include <framework/errors.h>
#include <services/discovery.h>

static const char * progname;
static lua_State *luastate;

struct luaref {
    int ref;
    void *owner;
    struct luaref *next;
};

enum {
    bufst_normal,
    bufst_normal_optnl,
    bufst_esc,
    bufst_esc2,
    bufst_eol,
    bufst_eol_optnl
};

struct lua_read_command_state {
    int reqline;
    int reqdata;
    struct luaref *refp;
    AsyncReqInfo req;
    int eof;
    int bufrd;
    int bufwr;
    int bufst;
    char buf[1024];
    int linemax;
    int lineind;
    char *line;
};

struct peer_extra {
    lua_State *L;
    PeerServer * ps;
    int managed;
};

struct protocol_extra {
    lua_State *L;
    Protocol * p;
    unsigned int ucnt;
    struct luaref *self_refp;
};

struct channel_extra {
    lua_State *L;
    Channel * c;
    struct protocol_extra * pe;
    struct luaref *self_refp;
    struct luaref *connect_cbrefp;
    struct luaref *connecting_cbrefp;
    struct luaref *connected_cbrefp;
    struct luaref *receive_cbrefp;
    struct luaref *disconnected_cbrefp;
};

struct command_extra {
    struct luaref *self_refp;
    struct luaref *result_cbrefp;
    ReplyHandlerInfo * replyinfo;
};

struct post_event_extra {
    lua_State *L;
    struct luaref *self_refp;
    struct luaref *handler_refp;
};

static struct luaref *refroot;
static struct luaref *peers_refp;
static struct lua_read_command_state lua_read_command_state;

static void lua_read_command_fillbuf(struct lua_read_command_state *state);


static struct luaref *luaref_new(lua_State *L, void * owner)
{
    struct luaref *refp = (struct luaref *)loc_alloc(sizeof *refp);

    refp->ref = luaL_ref(L, LUA_REGISTRYINDEX);
    refp->owner = owner;
    refp->next = refroot;
    refroot = refp;
    return refp;
}

static void luaref_free(lua_State *L, struct luaref *p)
{
    struct luaref **refpp;
    struct luaref *refp;

    refpp = &refroot;
    while((refp = *refpp) != NULL) {
        if(refp == p) {
            *refpp = refp->next;
            luaL_unref(L, LUA_REGISTRYINDEX, refp->ref);
            loc_free(refp);
            return;
        }
        refpp = &refp->next;
    }
    assert(!"lua reference not found in list");
}

static void luaref_owner_free(lua_State *L, void * owner)
{
    struct luaref **refpp;
    struct luaref *refp;

    refpp = &refroot;
    while((refp = *refpp) != NULL) {
        if(refp->owner == owner) {
            *refpp = refp->next;
            luaL_unref(L, LUA_REGISTRYINDEX, refp->ref);
            loc_free(refp);
            continue;
        }
        refpp = &refp->next;
    }
}

#ifndef NDEBUG
static int lua_isclass(lua_State *L, int index, const char *name)
{
    int rval;

    if(!lua_getmetatable(L, index)) return 0;
    lua_getfield(L, LUA_REGISTRYINDEX, name);
    rval = lua_rawequal(L, -1, -2);
    lua_pop(L, 2);
    return rval;
}
#endif

static struct peer_extra *lua2peer(lua_State *L, int index)
{
    if(luaL_checkudata(L, index, "tcf_peer") == NULL) {
        return NULL;
    }
    return (struct peer_extra *)lua_touserdata(L, index);
}

static struct protocol_extra *lua2protocol(lua_State *L, int index)
{
    if(luaL_checkudata(L, index, "tcf_protocol") == NULL) {
        return NULL;
    }
    return (struct protocol_extra *)lua_touserdata(L, index);
}

static struct channel_extra *lua2channel(lua_State *L, int index)
{
    if(luaL_checkudata(L, index, "tcf_channel") == NULL) {
        return NULL;
    }
    return (struct channel_extra *)lua_touserdata(L, index);
}

static struct post_event_extra *lua2postevent(lua_State *L, int index)
{
    if(luaL_checkudata(L, index, "tcf_post_event") == NULL) {
        return NULL;
    }
    return (struct post_event_extra *)lua_touserdata(L, index);
}

static void lua_read_command_getline(void *client_data)
{
    int c;
    lua_State *L = luastate;
    struct lua_read_command_state *state = (struct lua_read_command_state *)client_data;

    assert(state->reqline != 0);
    assert(state->reqdata == 0);
    while(state->bufrd < state->bufwr) {
        c = state->buf[state->bufrd++];
        switch(state->bufst) {
        case bufst_normal:
        case_bufst_normal:
            if(c == '\\') {
                state->bufst = bufst_esc;
                continue;
            }
            if(c == '\r') {
                state->bufst = bufst_eol_optnl;
                goto eol;
            }
            if(c == '\n') {
                state->bufst = bufst_eol;
                goto eol;
            }
            break;

        case bufst_normal_optnl:
            if(c == '\n') {
                state->bufst = bufst_normal;
                continue;
            }
            goto case_bufst_normal;

        case bufst_esc:
            if(c == '\r') {
                state->bufst = bufst_normal_optnl;
                c = '\n';
                break;
            }
            if(c == '\n') {
                state->bufst = bufst_normal;
                break;
            }
            state->bufst = bufst_esc2;
            state->bufrd--;
            c = '\\';
            break;

        case bufst_esc2:
            state->bufst = bufst_normal;
            break;

        case bufst_eol:
            state->bufst = bufst_normal;
            goto case_bufst_normal;

        case bufst_eol_optnl:
            state->bufst = bufst_normal;
            if(c == '\n') continue;
            goto case_bufst_normal;

        default:
            assert(!"unexpected state");
        }
        if(state->lineind == state->linemax) {
            if(state->linemax == 0) {
                state->linemax = 1024;
                state->line = (char *)loc_alloc(state->linemax);
            } else {
                state->linemax *= 2;
                state->line = (char *)loc_realloc(state->line, state->linemax);
            }
        }
        state->line[state->lineind++] = c;
    }
eol:
    if(state->bufst != bufst_eol_optnl && state->bufst != bufst_eol && !state->eof) {
        assert(state->bufrd == state->bufwr);
        lua_read_command_fillbuf(state);
        return;
    }
    state->reqline = 0;
    assert(state->refp != NULL);
    lua_rawgeti(L, LUA_REGISTRYINDEX, state->refp->ref);
    luaref_free(L, state->refp);
    state->refp = NULL;
    if(state->lineind > 0 || !state->eof) {
        trace(LOG_LUA, "lua_read_command: %.*s", state->lineind, state->line);
        lua_pushlstring(L, state->line, state->lineind);
        state->lineind = 0;
    } else {
        trace(LOG_LUA, "lua_read_command: EOF");
        lua_pushnil(L);
    }
    if(lua_pcall(L, 1, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
    return;
}

static void lua_read_command_done(void *client_data)
{
    AsyncReqInfo *req = (AsyncReqInfo *)client_data;
    struct lua_read_command_state *state = (struct lua_read_command_state *)req->client_data;

    assert(state->reqdata != 0);
    state->reqdata = 0;
    if(state->req.u.fio.rval > 0) {
        state->bufwr += state->req.u.fio.rval;
        state->eof = 0;
    } else {
        state->eof = 1;
    }
    lua_read_command_getline(state);
}

static void lua_read_command_fillbuf(struct lua_read_command_state *state)
{
    assert(state->reqdata == 0);
    assert(state->bufrd == state->bufwr);
    state->reqdata = 1;
    state->bufrd = 0;
    state->bufwr = 0;
    state->req.done = lua_read_command_done;
    state->req.client_data = state;
    state->req.type = AsyncReqRead;
    state->req.u.fio.bufp = state->buf;
    state->req.u.fio.bufsz = sizeof state->buf;
    async_req_post(&state->req);
}

static int lua_read_command(lua_State *L)
{
    struct lua_read_command_state *state = &lua_read_command_state;

    assert(L == luastate);
    assert(state->reqline == 0);
    if(lua_gettop(L) != 1 || !lua_isfunction(L, 1)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_read_command start");
    lua_pushvalue(L, 1);
    state->refp = luaref_new(L, state);
    state->reqline = 1;
    if(state->bufrd == state->bufwr) {
        lua_read_command_fillbuf(state);
    } else {
        post_event(lua_read_command_getline, state);
    }
    return 0;
}

static struct peer_extra *lookup_pse(lua_State *L, PeerServer *ps)
{
    struct peer_extra *pse;

    lua_rawgeti(L, LUA_REGISTRYINDEX, peers_refp->ref);
    lua_pushstring(L, ps->id);          /* Key */
    lua_rawget(L, -2);
    if((pse = (struct peer_extra *)lua_touserdata(L, -1)) == NULL) {
        assert(lua_isnil(L, -1));
        lua_pop(L, 2);
        return NULL;
    }
    assert(lua_isclass(L, -1, "tcf_peer"));
    lua_pop(L, 2);
    return pse;
}

static struct peer_extra *lua_alloc_pse(lua_State *L, PeerServer *ps)
{
    struct peer_extra *pse;

    /* Allocate new LUA object for peer */
    pse = (struct peer_extra *)lua_newuserdata(L, sizeof *pse);
    memset(pse, 0, sizeof *pse);
    pse->L = L;
    pse->ps = ps;
    luaL_getmetatable(L, "tcf_peer");
    lua_setmetatable(L, -2);
    /* Returns userdata for peer on LUA stack */
    return pse;
}

static struct peer_extra *lua_push_pse(lua_State *L, PeerServer *ps)
{
    struct peer_extra *pse;

    lua_rawgeti(L, LUA_REGISTRYINDEX, peers_refp->ref);
    lua_pushstring(L, ps->id);          /* Key */
    lua_rawget(L, -2);
    if((pse = (struct peer_extra *)lua_touserdata(L, -1)) != NULL) {
        assert(lua_isclass(L, -1, "tcf_peer"));
        assert(pse->managed);
    } else {
        assert(lua_isnil(L, -1));
        lua_pop(L, 1);

        pse = lua_alloc_pse(L, ps);
        pse->managed = 1;

        /* Register mapping from PS to LUA object */
        lua_pushstring(L, ps->id);          /* Key */
        lua_pushvalue(L, -2);               /* Value */
        lua_rawset(L, -4);                  /* peers[id] = pse */
    }
    lua_remove(L, -2);                  /* Remove peers table */
    /* Returns userdata for peer on LUA stack */
    return pse;
}

static void peer_server_changes(PeerServer *ps, int changeType, void * client_data)
{
    lua_State *L = (lua_State *)client_data;
    struct peer_extra *pse = lookup_pse(L, ps);

    switch(changeType) {
    case PS_EVENT_ADDED:
        assert(pse == NULL);
        break;

    case PS_EVENT_HEART_BEAT:
        break;

    case PS_EVENT_CHANGED:
        if(pse != NULL) pse->ps = ps;
        break;

    case PS_EVENT_REMOVED:
        if(pse != NULL) pse->ps = NULL;
        break;

    default:
        assert(!"unknown peer change type");
    }
}

static int lua_peer_server_find(lua_State *L)
{
    PeerServer *ps;
    const char *name;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || !lua_isstring(L, 1)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    name = lua_tostring(L, 1);
    ps = peer_server_find(name);
    trace(LOG_LUA, "lua_peer_server_find %s %p", name, ps);
    if(ps == NULL) {
        lua_pushnil(L);
        return 1;
    }
    lua_push_pse(L, ps);
    return 1;
}

static int lua_peer_server_list_entry(PeerServer * ps, void * client_data)
{
    lua_State *L = (lua_State *)client_data;
    lua_push_pse(L, ps);
    lua_rawseti(L, -2, lua_objlen(L, -2) + 1);
    return 0;
}

static int lua_peer_server_list(lua_State *L)
{
    assert(L == luastate);
    if(lua_gettop(L) != 0) {
        luaL_error(L, "wrong number of arguments");
    }
    trace(LOG_LUA, "lua_peer_server_list");
    lua_newtable(L);
    peer_server_iter(lua_peer_server_list_entry, L);
    return 1;
}

static int lua_peer_server_from_url(lua_State *L)
{
    PeerServer *ps;
    const char *url;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || !lua_isstring(L, 1)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    url = lua_tostring(L, 1);
    ps = channel_peer_from_url(url);
    trace(LOG_LUA, "lua_peer_server_from_url %s %p", url, ps);
    if(ps == NULL) luaL_error(L, "cannot parse url: %s", lua_tostring(L, 1));
    if(ps->id == NULL) {
        peer_server_addprop(ps, loc_strdup("ID"), loc_strdup(lua_tostring(L, 1)));
    }
    lua_alloc_pse(L, ps);
    return 1;
}

static struct protocol_extra *lua_protocol_ref(struct protocol_extra *pe, int index)
{
    if(pe->ucnt == 0) {
        assert(lua_touserdata(pe->L, index) == pe);
        lua_pushvalue(pe->L, index); /* Prevent GC while connection is active */
        pe->self_refp = luaref_new(pe->L, pe);
    }
    pe->ucnt++;
    return pe;
}

static void lua_protocol_unref(struct protocol_extra *pe)
{
    assert(pe->ucnt > 0);
    assert(pe->self_refp != NULL);
    pe->ucnt--;
    if(pe->ucnt == 0) {
        luaref_free(pe->L, pe->self_refp);
        pe->self_refp = NULL;
    }
}

static int lua_protocol_alloc(lua_State *L)
{
    struct protocol_extra *pe;

    assert(L == luastate);
    if(lua_gettop(L) != 0) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_protocol_alloc");
    pe = (struct protocol_extra *)lua_newuserdata(L, sizeof *pe);
    memset(pe, 0, sizeof *pe);
    pe->L = L;
    pe->p = protocol_alloc();
    luaL_getmetatable(L, "tcf_protocol");
    lua_setmetatable(L, -2);
    return 1;
}

static int lua_channel_server(lua_State *L)
{
    trace(LOG_LUA, "lua_channel_server");
    luaL_error(L, "not implemented");
    return 0;
}


static void channel_connecting(Channel * c) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    lua_State *L = ce->L;

    if(ce->connecting_cbrefp != NULL) {
        lua_rawgeti(L, LUA_REGISTRYINDEX, ce->connecting_cbrefp->ref);
        if(lua_pcall(L, 0, 0, 0) != 0) {
            fprintf(stderr, "%s\n", lua_tostring(L,1));
            exit(1);
        }
    }
    trace(LOG_LUA, "lua_channel_connecting %p", c);
    send_hello_message(c);
}

static void channel_connected(Channel * c) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    lua_State *L = ce->L;

    trace(LOG_LUA, "lua_channel_connected %p", c);
    if(ce->connected_cbrefp != NULL) {
        lua_rawgeti(L, LUA_REGISTRYINDEX, ce->connected_cbrefp->ref);
        if(lua_pcall(L, 0, 0, 0) != 0) {
            fprintf(stderr, "%s\n", lua_tostring(L,1));
            exit(1);
        }
    }
}

static void channel_receive(Channel * c) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    lua_State *L = ce->L;

    trace(LOG_LUA, "lua_channel_receive %p", c);
    if(ce->receive_cbrefp != NULL) {
        lua_rawgeti(L, LUA_REGISTRYINDEX, ce->receive_cbrefp->ref);
        if(lua_pcall(L, 0, 0, 0) != 0) {
            fprintf(stderr, "%s\n", lua_tostring(L,1));
            exit(1);
        }
    }
    handle_protocol_message(c);
}

static void channel_disconnected(Channel * c) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    lua_State *L = ce->L;

    trace(LOG_LUA, "lua_channel_disconnected %p", c);
    if(ce->disconnected_cbrefp != NULL) {
        lua_rawgeti(L, LUA_REGISTRYINDEX, ce->disconnected_cbrefp->ref);
        if(lua_pcall(L, 0, 0, 0) != 0) {
            fprintf(stderr, "%s\n", lua_tostring(L,1));
            exit(1);
        }
    }
    lua_protocol_unref(ce->pe);
    ce->pe = NULL;
    c->client_data = NULL;
    ce->c = NULL;
    luaref_owner_free(L, ce);
}

static void lua_channel_connect_cb(void * client_data, int error, Channel * c)
{
    struct channel_extra *ce = (struct channel_extra *)client_data;
    lua_State *L = ce->L;

    trace(LOG_LUA, "lua_channel_connect_cb %p %d", c, error);
    assert(ce->connect_cbrefp != NULL);
    lua_rawgeti(L, LUA_REGISTRYINDEX, ce->connect_cbrefp->ref);
    if(!error) {
        assert(c != NULL);
        ce->c = c;
        c->client_data = ce;
        c->protocol = ce->pe->p;
        c->connecting = channel_connecting;
        c->connected = channel_connected;
        c->receive = channel_receive;
        c->disconnected = channel_disconnected;
        lua_rawgeti(L, LUA_REGISTRYINDEX, ce->self_refp->ref);
        lua_pushnil(L);
    } else {
        assert(c == NULL);
        luaref_owner_free(L, ce);
        lua_pushnil(L);
        lua_pushstring(L, errno_to_str(error));
    }
    if(lua_pcall(L, 2, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
}

static int lua_channel_connect(lua_State *L)
{
    struct peer_extra *pse = NULL;
    struct protocol_extra *pe = NULL;
    struct channel_extra *ce;

    assert(L == luastate);
    if(lua_gettop(L) != 3 ||
       (pse = lua2peer(L, 1)) == NULL ||
       (pe = lua2protocol(L, 2)) == NULL ||
       !lua_isfunction(L, 3)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_channel_connect %p", pse->ps);
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    ce = (struct channel_extra *)lua_newuserdata(L, sizeof *ce);
    memset(ce, 0, sizeof *ce);
    lua_pushvalue(L, -1);  /* Prevent GC while connection is active */
    ce->self_refp = luaref_new(L, ce);
    ce->L = L;
    ce->pe = lua_protocol_ref(pe, 2);
    luaL_getmetatable(L, "tcf_channel");
    lua_setmetatable(L, -2);
    lua_pushvalue(L, 3);
    ce->connect_cbrefp = luaref_new(L, ce);
    channel_connect(pse->ps, lua_channel_connect_cb, ce);
    return 0;
}

static void lua_post_event_cb(void * client_data)
{
    struct post_event_extra *p = (struct post_event_extra *)client_data;
    lua_State *L = p->L;

    assert(p->handler_refp != NULL);
    trace(LOG_LUA, "lua_post_event_cb %d", p->handler_refp);
    lua_rawgeti(L, LUA_REGISTRYINDEX, p->handler_refp->ref);
    luaref_owner_free(L, p);
    if(lua_pcall(L, 0, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
}

static int lua_post_event(lua_State *L)
{
    struct post_event_extra *p;
    unsigned long delay;

    assert(L == luastate);

    if(lua_gettop(L) > 2 || !lua_isfunction(L, 1) ||
       lua_gettop(L) > 1 && !(lua_isnil(L, 2) || lua_isnumber(L, 2))) {
        luaL_error(L, "wrong number or type of arguments");
    }
    p = (struct post_event_extra *)lua_newuserdata(L, sizeof *p);
    memset(p, 0, sizeof *p);
    p->L = L;
    lua_pushvalue(L, -1);
    p->self_refp = luaref_new(L, p);
    lua_pushvalue(L, 1);
    p->handler_refp = luaref_new(L, p);
    trace(LOG_LUA, "lua_post_event %d", p->handler_refp);
    luaL_getmetatable(L, "tcf_post_event");
    lua_setmetatable(L, -2);
    delay = lua_tointeger(L, 2);
    if(delay == 0) {
        post_event(lua_post_event_cb, p);
    } else {
        post_event_with_delay(lua_post_event_cb, p, delay);
    }
    return 1;
}

static const luaL_Reg tcffuncs[] = {
    { "read_command",       lua_read_command },
    { "peer_server_find",   lua_peer_server_find },
    { "peer_server_list",   lua_peer_server_list },
    { "peer_server_from_url",lua_peer_server_from_url },
    { "protocol_alloc",     lua_protocol_alloc },
    { "channel_server",     lua_channel_server },
    { "channel_connect",    lua_channel_connect },
    { "post_event",         lua_post_event },
    { 0 }
};


static int lua_protocol_tostring(lua_State *L)
{
    struct protocol_extra *pe = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pe = lua2protocol(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_protocol_tostring %p", pe->p);
    lua_pushfstring(L, "tcf_protocol (%p, %d)", pe, pe->ucnt);
    return 1;
}

static int lua_protocol_gc(lua_State *L)
{
    struct protocol_extra *pe = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pe = lua2protocol(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    assert(pe->ucnt == 0);
    assert(pe->p != NULL);
    trace(LOG_LUA, "lua_protocol_gc %p", pe->p);
    protocol_release(pe->p);
    pe->p = NULL;
    return 0;
}

static void protocol_command_handler(char * token, Channel * c, void * client_data) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    struct luaref *refp = (struct luaref *)client_data;
    lua_State *L = ce->L;
    InputStream * inp = &c->inp;
    luaL_Buffer msg;
    int ch;

    lua_rawgeti(L, LUA_REGISTRYINDEX, refp->ref);
    lua_pushstring(L, token);
    luaL_buffinit(L, &msg);
    while((ch = read_stream(inp)) >= 0) {
        luaL_addchar(&msg, ch);
    }
    luaL_pushresult(&msg);
    trace(LOG_LUA, "lua_protocol_command %d %s", refp->ref, lua_tostring(L, -1));
    if(lua_pcall(L, 2, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
}

static int lua_protocol_command_handler(lua_State *L)
{
    struct protocol_extra *pe = NULL;
    struct luaref *refp;
    const char *service;
    const char *name;

    assert(L == luastate);
    if(lua_gettop(L) != 4 || (pe = lua2protocol(L, 1)) == NULL ||
       !lua_isstring(L, 2) || !lua_isstring(L, 3) ||
       !lua_isfunction(L, 4)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    lua_pushvalue(L, 4);
    refp = luaref_new(L, pe);
    service = lua_tostring(L, 2);
    name = lua_tostring(L, 3);
    trace(LOG_LUA, "lua_protocol_command_handler %d %s %s", refp->ref, service, name);
    add_command_handler2(pe->p, service, name, protocol_command_handler, refp);
    return 0;
}

static const luaL_Reg protocolfuncs[] = {
    { "__tostring",         lua_protocol_tostring },
    { "__gc",               lua_protocol_gc },
    { "command_handler",    lua_protocol_command_handler },
    //    { "default_message_handler", lua_protocol_default_message_handler },
    { 0 }
};


static const char *channel_state_string(Channel * c)
{
    if (c == NULL) return "Disconnected";
    switch(c->state) {
    case ChannelStateStartWait: return "StartWait";
    case ChannelStateStarted: return "Started";
    case ChannelStateHelloSent: return "HelloSent";
    case ChannelStateHelloReceived: return "HelloReceived";
    case ChannelStateConnected: return "Connected";
    case ChannelStateRedirectSent: return "RedirectSent";
    case ChannelStateRedirectReceived: return "RedirectReceived";
    case ChannelStateDisconnected: return "Disconnected";
    default: return "Unknown";
    }
}

static int lua_channel_tostring(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_channel_tostring %p", ce->c);
    if(ce->c != NULL) {
        lua_pushfstring(L, "tcf_channel (%s, %p, %s)", ce->c->peer_name, ce,
                        channel_state_string(ce->c));
    } else {
        lua_pushfstring(L, "tcf_channel (<disconnected>, %p)", ce);
    }
    return 1;
}

static int lua_channel_state(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_channel_state %p", ce->c);
    lua_pushstring(L, channel_state_string(ce->c));
    return 1;
}

static int lua_channel_close(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_channel_close %p", ce->c);
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    channel_close(ce->c);
    return 0;
}

static void update_callback(lua_State *L, int index, struct luaref **cbrefpp, void * owner) {
    if(!lua_isfunction(L, index) && !lua_isnil(L, index)) {
        luaL_error(L, "handler must be function or nil");
    }
    if(*cbrefpp != NULL) {
        luaref_free(L, *cbrefpp);
        *cbrefpp = NULL;
    }
    if(lua_isfunction(L, index)) {
        lua_pushvalue(L, index);
        *cbrefpp = luaref_new(L, owner);
    }
}

static int lua_channel_connecting_handler(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 2 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    update_callback(L, 2, &ce->connecting_cbrefp, ce);
    trace(LOG_LUA, "lua_channel_connecting_handler %p", ce->c, ce->connecting_cbrefp->ref);
    return 0;
}

static int lua_channel_connected_handler(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 2 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    update_callback(L, 2, &ce->connected_cbrefp, ce);
    trace(LOG_LUA, "lua_channel_connected_handler %p", ce->c, ce->connected_cbrefp->ref);
    return 0;
}

static int lua_channel_receive_handler(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 2 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    update_callback(L, 2, &ce->receive_cbrefp, ce);
    trace(LOG_LUA, "lua_channel_receive_handler %p", ce->c, ce->receive_cbrefp->ref);
    return 0;
}

static int lua_channel_disconnected_handler(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 2 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    update_callback(L, 2, &ce->disconnected_cbrefp, ce);
    trace(LOG_LUA, "lua_channel_disconnected_handler %p", ce->c, ce->disconnected_cbrefp->ref);
    return 0;
}

static void channel_event_handler(Channel * c, void * client_data) {
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    struct luaref *refp = (struct luaref *)client_data;
    lua_State *L = ce->L;
    InputStream * inp = &c->inp;
    luaL_Buffer msg;
    int ch;

    lua_rawgeti(L, LUA_REGISTRYINDEX, refp->ref);
    luaL_buffinit(L, &msg);
    while((ch = read_stream(inp)) >= 0) {
        luaL_addchar(&msg, ch);
    }
    luaL_pushresult(&msg);
    trace(LOG_LUA, "lua_channel_event %p %d %s", c, refp->ref, lua_tostring(L, -1));
    if(lua_pcall(L, 1, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
}

static int lua_channel_event_handler(lua_State *L)
{
    struct channel_extra *ce = NULL;
    struct luaref *refp;
    const char *service;
    const char *name;

    assert(L == luastate);
    if(lua_gettop(L) != 4 || (ce = lua2channel(L, 1)) == NULL ||
       !lua_isstring(L, 2) || !lua_isstring(L, 3) ||
       !lua_isfunction(L, 4)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    lua_pushvalue(L, 4);
    refp = luaref_new(L, ce);
    service = lua_tostring(L, 2);
    name = lua_tostring(L, 3);
    trace(LOG_LUA, "lua_channel_event_handler %p %s %s %d", ce->c, service, name, refp->ref);
    add_event_handler2(ce->c, service, name, channel_event_handler, refp);
    return 0;
}

static int lua_channel_start(lua_State *L)
{
    struct channel_extra *ce = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    trace(LOG_LUA, "lua_channel_start %p", ce->c);
    channel_start(ce->c);
    return 0;
}

static int lua_channel_send_message(lua_State *L)
{
    struct channel_extra *ce = NULL;
    OutputStream *out;
    const char *s;
    size_t l;

    assert(L == luastate);
    if(lua_gettop(L) != 2 ||
       (ce = lua2channel(L, 1)) == NULL ||
       !lua_isstring(L, 2)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");

    s = lua_tolstring(L, 2, &l);
    trace(LOG_LUA, "lua_channel_send_message %p %.*s", ce->c, l, s);
    out = &ce->c->out;
    while(l-- > 0) {
        write_stream(out, (*s++) & 0xff);
    }
    write_stream(out, MARKER_EOM);
    return 0;
}

static void channel_send_command_cb(Channel * c, void * client_data, int error)
{
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    struct command_extra *cmd = (struct command_extra *)client_data;
    lua_State *L = ce->L;
    InputStream * inp = &c->inp;
    luaL_Buffer msg;
    int ch;

    lua_rawgeti(L, LUA_REGISTRYINDEX, cmd->result_cbrefp->ref);
    if(!error) {
        luaL_buffinit(L, &msg);
        while((ch = read_stream(inp)) >= 0) {
            luaL_addchar(&msg, ch);
        }
        luaL_pushresult(&msg);
        lua_pushnil(L);
        trace(LOG_LUA, "lua_channel_send_command_reply %p %d %s", c, cmd->result_cbrefp->ref, lua_tostring(L, -2));
    } else {
        lua_pushnil(L);
        lua_pushstring(L, errno_to_str(error));
        trace(LOG_LUA, "lua_channel_send_command_reply %p %d error %d", c, cmd->result_cbrefp->ref, error);
    }
    if(lua_pcall(L, 2, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
    luaref_owner_free(L, cmd);
}

static int lua_channel_send_command(lua_State *L)
{
    struct channel_extra *ce = NULL;
    struct command_extra *cmd;
    OutputStream *out;
    const char *s;
    size_t l;

    assert(L == luastate);
    if(lua_gettop(L) != 5 ||
       (ce = lua2channel(L, 1)) == NULL ||
       !lua_isstring(L, 2) ||
       !lua_isstring(L, 3) ||
       !lua_isstring(L, 4) ||
       !lua_isfunction(L, 5)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");

    /* Object to track outstanding command */
    cmd = (struct command_extra *)lua_newuserdata(L, sizeof *cmd);
    memset(cmd, 0, sizeof *cmd);
    luaL_getmetatable(L, "tcf_command");
    lua_setmetatable(L, -2);

    /* Make sure GC don't free until reply is received */
    lua_pushvalue(L, -1);
    cmd->self_refp = luaref_new(L, cmd);
    lua_pushvalue(L, 5);
    cmd->result_cbrefp = luaref_new(L, cmd);

    /* Send command header */
    cmd->replyinfo = protocol_send_command(ce->c,
                                           lua_tostring(L, 2),
                                           lua_tostring(L, 3),
                                           channel_send_command_cb, cmd);
    s = lua_tolstring(L, 4, &l);
    trace(LOG_LUA, "lua_channel_send_command %p %d %.*s", ce->c, cmd->result_cbrefp->ref, l, s);
    out = &ce->c->out;
    while(l-- > 0) {
        write_stream(out, (*s++) & 0xff);
    }
    write_stream(out, MARKER_EOM);
    return 1;
}

static int lua_channel_cancel_command(lua_State *L)
{
    trace(LOG_LUA, "lua_channel_cancel_command");
    luaL_error(L, "not implemented");
    return 0;
}

static void channel_redirect_cb(Channel * c, void * client_data, int error)
{
    struct channel_extra *ce = (struct channel_extra *)c->client_data;
    struct command_extra *cmd = (struct command_extra *)client_data;
    lua_State *L = ce->L;

    lua_rawgeti(L, LUA_REGISTRYINDEX, cmd->result_cbrefp->ref);
    if(!error) {
        lua_pushnil(L);
        trace(LOG_LUA, "lua_channel_redirect_reply %p %d %s", c, cmd->result_cbrefp->ref, lua_tostring(L, -2));
    } else {
        lua_pushstring(L, errno_to_str(error));
        trace(LOG_LUA, "lua_channel_redirect_reply %p %d error %d", c, cmd->result_cbrefp->ref, error);
    }
    if(lua_pcall(L, 1, 0, 0) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }
    luaref_owner_free(L, cmd);
}

static int lua_channel_redirect(lua_State *L)
{
    struct channel_extra *ce = NULL;
    struct command_extra *cmd;

    assert(L == luastate);
    if(lua_gettop(L) != 3 ||
       (ce = lua2channel(L, 1)) == NULL ||
       !lua_isstring(L, 2) ||
       !lua_isfunction(L, 3)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");

    /* Object to track outstanding command */
    cmd = (struct command_extra *)lua_newuserdata(L, sizeof *cmd);
    memset(cmd, 0, sizeof *cmd);
    luaL_getmetatable(L, "tcf_command");
    lua_setmetatable(L, -2);

    /* Make sure GC don't free until reply is received */
    lua_pushvalue(L, -1);
    cmd->self_refp = luaref_new(L, cmd);
    lua_pushvalue(L, 3);
    cmd->result_cbrefp = luaref_new(L, cmd);

    /* Send command header */
    cmd->replyinfo = send_redirect_command(ce->c,
                                           lua_tostring(L, 2),
                                           channel_redirect_cb, cmd);
    trace(LOG_LUA, "lua_channel_redirect %p %d %s", ce->c, cmd->result_cbrefp->ref, lua_tostring(L, 2));
    return 1;
}

static int lua_channel_get_services(lua_State *L)
{
    struct channel_extra *ce = NULL;
    int i;

    assert(L == luastate);
    if(lua_gettop(L) != 1 ||
       (ce = lua2channel(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(ce->c == NULL) luaL_error(L, "disconnected channel");
    trace(LOG_LUA, "lua_channel_get_services %p", ce->c);
    lua_newtable(L);
    for (i = 0; i < ce->c->peer_service_cnt; i++) {
        lua_pushstring(L, ce->c->peer_service_list[i]);
        lua_rawseti(L, -2, i + 1);
    }
    return 1;
}

static const luaL_Reg channelfuncs[] = {
    { "__tostring",         lua_channel_tostring },
    { "state",              lua_channel_state },
    { "close",              lua_channel_close },
    { "connecting_handler", lua_channel_connecting_handler },
    { "connected_handler",  lua_channel_connected_handler },
    { "receive_handler",    lua_channel_receive_handler },
    { "disconnected_handler",lua_channel_disconnected_handler },
    { "event_handler",      lua_channel_event_handler },
    { "start",              lua_channel_start },
    { "send_message",       lua_channel_send_message },
    { "send_command",       lua_channel_send_command },
    { "cancel_command",     lua_channel_cancel_command },
    { "redirect",           lua_channel_redirect },
    { "get_services",       lua_channel_get_services },
    { 0 }
};

static int lua_peer_tostring(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pse = lua2peer(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_peer_tostring %p", pse->ps);
    lua_pushfstring(L, "tcf_peer (%s, %p)", pse->ps ? pse->ps->id : "<stale>", pse);
    return 1;
}

static int lua_peer_gc(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pse = lua2peer(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->managed == 0) {
        trace(LOG_LUA, "lua_peer_gc %p", pse->ps);
        peer_server_free(pse->ps);
        pse->ps = NULL;
    }
    return 0;
}

static int lua_peer_getid(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pse = lua2peer(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_getid %p", pse->ps);
    lua_pushstring(L, pse->ps->id);
    return 1;
}

static int lua_peer_getnames(lua_State *L)
{
    int i;
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pse = lua2peer(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_getnames %p", pse->ps);
    lua_createtable(L, pse->ps->ind, 0);
    for(i = 0; i < pse->ps->ind; i++) {
        lua_pushstring(L, pse->ps->list[i].name);
        lua_rawseti(L, -2, i+1);
    }
    return 1;
}

static int lua_peer_getvalue(lua_State *L)
{
    struct peer_extra *pse = NULL;
    const char *s;

    assert(L == luastate);
    if(lua_gettop(L) != 2 ||
       (pse = lua2peer(L, 1)) == NULL ||
       !lua_isstring(L, 2)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_getvalue %p", pse->ps);
    if((s = peer_server_getprop(pse->ps, lua_tostring(L, 2), NULL)) == NULL) {
        lua_pushnil(L);
        return 1;
    }
    lua_pushstring(L, s);
    return 1;
}

static int lua_peer_setvalue(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 3 || (pse = lua2peer(L, 1)) == NULL ||
       !lua_isstring(L, 2) || !lua_isstring(L, 3)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_setvalue %p", pse->ps);
    peer_server_addprop(pse->ps, loc_strdup(lua_tostring(L, 2)), loc_strdup(lua_tostring(L, 3)));
    return 0;
}

static int lua_peer_getflags(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (pse = lua2peer(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_getflags %p", pse->ps);
    lua_createtable(L, 0, 0);
    if(pse->ps->flags & PS_FLAG_LOCAL) {
        lua_pushstring(L, "local");
        lua_pushboolean(L, 1);
        lua_rawset(L, -3);
    }
    if(pse->ps->flags & PS_FLAG_PRIVATE) {
        lua_pushstring(L, "private");
        lua_pushboolean(L, 1);
        lua_rawset(L, -3);
    }
    if(pse->ps->flags & PS_FLAG_DISCOVERABLE) {
        lua_pushstring(L, "discoverable");
        lua_pushboolean(L, 1);
        lua_rawset(L, -3);
    }
    return 1;
}

static int lua_peer_setflags(lua_State *L)
{
    struct peer_extra *pse = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 2 || (pse = lua2peer(L, 1)) == NULL ||
       !lua_istable(L, 2)) {
        luaL_error(L, "wrong number or type of arguments");
    }
    if(pse->ps == NULL) luaL_error(L, "stale peer");
    trace(LOG_LUA, "lua_peer_setflags %p", pse->ps);
    lua_pushnil(L);
    while(lua_next(L, 2) != 0) {
        if(lua_isstring(L, -2)) {
            if(strcmp(lua_tostring(L, -2), "local") == 0) {
                if(lua_toboolean(L, 2)) {
                    pse->ps->flags |= PS_FLAG_LOCAL;
                } else {
                    pse->ps->flags &= ~PS_FLAG_LOCAL;
                }
            } else if(strcmp(lua_tostring(L, -2), "private") == 0) {
                if(lua_toboolean(L, 2)) {
                    pse->ps->flags |= PS_FLAG_PRIVATE;
                } else {
                    pse->ps->flags &= ~PS_FLAG_PRIVATE;
                }
            } else if(strcmp(lua_tostring(L, -2), "discoverable") == 0) {
                if(lua_toboolean(L, 2)) {
                    pse->ps->flags |= PS_FLAG_DISCOVERABLE;
                } else {
                    pse->ps->flags &= ~PS_FLAG_DISCOVERABLE;
                }
            }
        }
        lua_pop(L, 1);
    }
    lua_pop(L, 1);
    return 0;
}

static const luaL_Reg peerfuncs[] = {
    { "__tostring",         lua_peer_tostring },
    { "__gc",               lua_peer_gc },
    { "getid",              lua_peer_getid },
    { "getnames",           lua_peer_getnames },
    { "getvalue",           lua_peer_getvalue },
    { "setvalue",           lua_peer_setvalue },
    { "getflags",           lua_peer_getflags },
    { "setflags",           lua_peer_setflags },
    { 0 }
};

static int lua_post_event_tostring(lua_State *L)
{
    struct post_event_extra *p = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (p = lua2postevent(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_post_event_tostring %p", p);
    lua_pushfstring(L, "tcf_post_event (%s, %p)", p->handler_refp ? "pending" : "stale", p);
    return 1;
}

static int lua_post_event_cancel(lua_State *L)
{
    struct post_event_extra *p = NULL;

    assert(L == luastate);
    if(lua_gettop(L) != 1 || (p = lua2postevent(L, 1)) == NULL) {
        luaL_error(L, "wrong number or type of arguments");
    }
    trace(LOG_LUA, "lua_post_event_cancel %p", p);
    p->self_refp = NULL;
    p->handler_refp = NULL;
    luaref_owner_free(L, p);
    cancel_event(lua_post_event_cb, p, 0);
    return 0;
}

static const luaL_Reg posteventfuncs[] = {
    { "__tostring",         lua_post_event_tostring },
    { "cancel",             lua_post_event_cancel },
    { 0 }
};


/*
 * main entry point for TCF client
 *
 * The client is a simple shell permitting communication with the TCF agent.
 * By default the client will run in interactive mode. The client accepts
 * 3 command line options:
 * -L <log_file>        : specify a log file
 * -l <log_mode>        : logging level see trace.c for more details
 * -S <script_file>     : script of commands to run - non-interactive mode
 */

#if defined(_WRS_KERNEL)
int tcf_lua(void) {
#else
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    int error;
    int interactive = 1;
    const char * log_name = "-";
    const char * script_name = NULL;
    char * engine_name;
    lua_State *L;

    log_mode = 0;

#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif
    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_asyncreq();

#if defined(_WRS_KERNEL)

    progname = "tcf";
    open_log_file("-");

#else

    progname = argv[0];

    /* Parse arguments */
    for (ind = 1; ind < argc; ind++) {
        const char * s = argv[ind];
        if (*s != '-') {
            break;
        }
        s++;
        while ((c = *s++) != '\0') {
            switch (c) {
            case 'b':
                interactive = 0;
                break;

            case 'i':
                interactive = 1;
                break;

            case 'l':
            case 'L':
            case 'S':
                if (*s == '\0') {
                    if (++ind >= argc) {
                        fprintf(stderr, "%s: error: no argument given to option '%c'\n", progname, c);
                        exit(1);
                    }
                    s = argv[ind];
                }
                switch (c) {
                case 'l':
                    log_mode = strtol(s, 0, 0);
                    break;

                case 'L':
                    log_name = s;
                    break;

                case 'S':
                    script_name = s;
                    interactive = 0;
                    break;

                default:
                    fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                    exit(1);
                }
                s = "";
                break;

            default:
                fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                exit(1);
            }
        }
    }
    if (ind >= argc) {
        fprintf(stderr, "%s: error: no Lua script specified\n", progname);
        exit(1);
    }
    engine_name = argv[ind++];
    if (ind < argc) {
        fprintf(stderr, "%s: error: too many arguments\n", progname);
        exit(1);
    }

    open_log_file(log_name);

#endif

    if (script_name != NULL) {
        if((lua_read_command_state.req.u.fio.fd = open(script_name, 0)) < 0) {
            fprintf(stderr, "%s: error: cannot open script: %s\n", progname, script_name);
            exit(1);
        }
    } else {
        lua_read_command_state.req.u.fio.fd = fileno(stdin);
    }

    discovery_start();

    if((luastate = L = luaL_newstate()) == NULL) {
        fprintf(stderr, "error from luaL_newstate\n");
        exit(1);
    }
    luaL_openlibs(L);

    luaL_register(L, "tcf", tcffuncs);
    lua_pop(L, 1);

    /* Peer metatable */
    luaL_newmetatable(L, "tcf_peer");
    luaL_register(L, NULL, peerfuncs);
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__index");     /* m.__index = m */
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__metatable"); /* m.__metatable = m */
    lua_pop(L, 1);

    /* Protocol metatable */
    luaL_newmetatable(L, "tcf_protocol");
    luaL_register(L, NULL, protocolfuncs);
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__index");     /* m.__index = m */
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__metatable"); /* m.__metatable = m */
    lua_pop(L, 1);

    /* Channel metatable */
    luaL_newmetatable(L, "tcf_channel");
    luaL_register(L, NULL, channelfuncs);
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__index");     /* m.__index = m */
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__metatable"); /* m.__metatable = m */
    lua_pop(L, 1);

    /* Post event metatable */
    luaL_newmetatable(L, "tcf_post_event");
    luaL_register(L, NULL, posteventfuncs);
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__index");     /* m.__index = m */
    lua_pushvalue(L, -1);
    lua_setfield(L, -1, "__metatable"); /* m.__metatable = m */
    lua_pop(L, 1);

    lua_newtable(L);                    /* peers = {} */
    lua_newtable(L);                    /* m = {} */
    lua_pushstring(L, "v");             /* Values are weak */
    lua_setfield(L, -2, "__mode");      /* m.__mode = "v" */
    lua_setmetatable(L, -2);            /* setmetatable(peer, m) */
    peers_refp = luaref_new(L, NULL);
    peer_server_add_listener(peer_server_changes, L);

    if((error = luaL_loadfile(L, engine_name)) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }

    if((error = lua_pcall(L, 0, LUA_MULTRET, 0)) != 0) {
        fprintf(stderr, "%s\n", lua_tostring(L,1));
        exit(1);
    }

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();

    lua_close(L);
    return 0;
}

#else /* ENABLE_LUA */

#if defined(_WRS_KERNEL)
int tcf_lua(void) {
#else
int main(int argc, char ** argv) {
#endif
    fprintf(stderr, "Agent was built without LUA support\n");
    return 1;
}

#endif /* ENABLE_LUA */
