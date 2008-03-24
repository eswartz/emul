/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Command line interpreter.
 */

#include "mdep.h"
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "cmdline.h"
#include "events.h"
#include "errors.h"
#include "myalloc.h"
#include "peer.h"
#include "protocol.h"
#include "trace.h"
#include "channel.h"
#include "discovery.h"

Channel * chan;

static int cmdline_suspended;
static pthread_mutex_t cmdline_mutex;
static pthread_cond_t cmdline_signal;
static pthread_t interactive_thread;

static void channel_connecting(Channel * c) {
    trace(LOG_ALWAYS, "channel_connecting");

    send_hello_message(c->client_data, c);
    discovery_channel_add(c);
    c->out.flush(&c->out);
}

static void channel_connected(Channel * c) {
    int i;

    trace(LOG_ALWAYS, "channel_connected, services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_ALWAYS, "  %s", c->peer_service_list[i]);
    }
}

static void channel_receive(Channel * c) {
    handle_protocol_message(c->client_data, c);
}

static void channel_disconnected(Channel * c) {
    trace(LOG_ALWAYS, "channel_disconnected");
    discovery_channel_remove(c);
    protocol_channel_closed(c->client_data, c);
    if (chan == c) chan = NULL;
}

static int cmd_exit(char * s) {
    exit(0);
    return 0;
}

static void display_tcf_reply(Channel * c, void * client_data, int error) {
    int i;

    if (error) {
        printf("reply error %d\n", error);
        cmdline_resume();
        return;
    }
    for (;;) {
        i = c->inp.read(&c->inp);
        if (i == MARKER_EOM) break;
        if (i == 0) i = ' ';
        putchar(i);
    }
    putchar('\n');
    cmdline_resume();
}
#define maxargs 20
 
static int cmd_tcf(char *s) {
    int i;
    int ind;
    char * args[maxargs];
    Channel * c = chan;

    if (c == NULL) {
        printf("error: channel not connected, use 'connect' command\n");
        return 0;
    }
    ind = 0;
    args[ind] = strtok(s, " \t");
    while (args[ind] != NULL && ++ind < maxargs) {
        args[ind] = strtok(NULL, " \t");
    }
    if (args[0] == NULL || args[1] == NULL) {
        printf("error: expected at least service and command name arguments\n");
        return 0;
    }
    protocol_send_command(c->client_data, c, args[0], args[1], display_tcf_reply, c);
    for (i = 2; i < ind; i++) {
        write_stringz(&c->out, args[i]);
    }
    c->out.write(&c->out, MARKER_EOM);
    c->out.flush(&c->out);
    return 1;
}

static int print_peer_flags(PeerServer * ps) {
    unsigned int flags = ps->flags;
    int cnt;
    int i;
    struct {
        unsigned int flag;
        char *name;
    } flagnames[] = {
        { PS_FLAG_LOCAL, "local" },
        { PS_FLAG_PRIVATE, "private" },
        { PS_FLAG_DISCOVERABLE, "discoverable" },
        { 0 }
    };

    cnt = 0;
    for (i = 0; flagnames[i].flag != 0; i++) {
        if (flags & flagnames[i].flag) {
            if (cnt != 0) {
                printf(", ");
            }
            cnt++;
            printf("local");
            flags &= ~flagnames[i].flag;
        }
    }
    if (flags || cnt == 0) printf("0x%x", flags);
    return 0;
}

static int print_peer_summary(PeerServer * ps, void * client_data) {
    unsigned int flags = ps->flags;
    char *s;

    printf("  %s", ps->id);
    s = peer_server_getprop(ps, "Description", NULL);
    if (s != NULL) {
        printf(", %s", s);
    }
    printf("\n");
    return 0;
}

static int cmd_peers(char * s) {
    printf("Peers:\n");
    peer_server_iter(print_peer_summary, NULL);
    return 0;
}

static int cmd_peerinfo(char * s) {
    PeerServer * ps;
    int i;

    printf("Peer information: %s\n", s);
    ps = peer_server_find(s);
    if (ps == NULL) {
        fprintf(stderr, "error: cannot find id: %s\n", s);
        return 0;
    }
    printf("  ID: %s\n", ps->id);
    for (i = 0; i < ps->ind; i++) {
        printf("  %s: %s\n", ps->list[i].name, ps->list[i].value);
    }
    print_peer_flags(ps);
    printf("\n");
    return 0;
}

static int cmd_connect(char * s) {
    PeerServer * ps;
    Protocol * proto;
    Channel * c;

    ps = channel_peer_from_url(s);
    if (ps == NULL) {
        fprintf(stderr, "error: cannot parse peer identifer: %s\n", s);
        return 0;
    }
    proto = protocol_alloc();
    ini_locator_service(proto);
    c = channel_connect(ps);
    peer_server_free(ps);
    if (c == NULL) {
        fprintf(stderr, "error: cannot estabilish connection\n");
        return 0;
    }
    c->connecting = channel_connecting;
    c->connected = channel_connected;
    c->receive = channel_receive;
    c->disconnected = channel_disconnected;
    c->client_data = proto;
    protocol_channel_opened(proto, c);
    channel_start(c);
    chan = c;
    return 0;
}

static void event_cmd_line(void * arg) {
    char * s = (char *)arg;
    int len;
    int delayed;
    struct {
        char * cmd;
        int (*hnd)(char *);
    } cmds[] = {
        { "exit",               cmd_exit },
        { "tcf",                cmd_tcf },
        { "peers",              cmd_peers },
        { "peerinfo",           cmd_peerinfo },
        { "connect",            cmd_connect },
        { 0 }
    }, *cp;

    while (*s && isspace(*s)) s++;
    if (*s == '\0') {
        cmdline_resume();
        return;
    }
    for (cp = cmds; cp->cmd != 0; cp++) {
        len = strlen(cp->cmd);
        if (strncmp(s, cp->cmd, len) == 0 && (s[len] == 0 || isspace(s[len]))) {
            s += len;
            while (*s && isspace(*s)) s++;
            delayed = cp->hnd(s);
            break;
        }
    }
    if (cp->cmd == 0) {
        fprintf(stderr, "unknown command: %s\n", s);
        delayed = 0;
    }
    loc_free(arg);
    if (!delayed) cmdline_resume();
}

void cmdline_suspend(void) {
    cmdline_suspended = 1;
}

void cmdline_resume(void) {
    check_error(pthread_mutex_lock(&cmdline_mutex));
    assert(cmdline_suspended);
    check_error(pthread_cond_signal(&cmdline_signal));
    cmdline_suspended = 0;

    check_error(pthread_mutex_unlock(&cmdline_mutex));
}

static void * interactive_handler(void * x) {
    int done = 0;
    int len;
    char buf[1000];

    check_error(pthread_mutex_lock(&cmdline_mutex));
    while (!done) {
        if (cmdline_suspended) {
            check_error(pthread_cond_wait(&cmdline_signal, &cmdline_mutex));
            continue;
        }
        printf("> ");
        fflush(stdout);
        if (fgets(buf, sizeof(buf), stdin) == NULL) {
            strcpy(buf, "exit");
            done = 1;
        }
        len = strlen(buf);
        if (len > 0 && buf[len-1] == '\n') {
            buf[--len] = '\0';
        }
        post_event(event_cmd_line, loc_strdup(buf));
        cmdline_suspended = 1;
    }
    check_error(pthread_mutex_unlock(&cmdline_mutex));
    return NULL;
}

void ini_cmdline_handler(void) {
    check_error(pthread_mutex_init(&cmdline_mutex, NULL));
    check_error(pthread_cond_init(&cmdline_signal, NULL));
    /* Create thread to read cmd line */
    check_error(pthread_create(&interactive_thread, &pthread_create_attr, interactive_handler, 0));
}



