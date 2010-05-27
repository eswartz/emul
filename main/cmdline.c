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
 *     Michael Sills-Lavoie - client enhancement system
 *******************************************************************************/

/*
 * Command line interpreter.
 */

#include <config.h>

#if ENABLE_Cmdline

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/myalloc.h>
#include <framework/peer.h>
#include <framework/protocol.h>
#include <framework/trace.h>
#include <framework/channel.h>
#include <framework/plugins.h>
#include <main/cmdline.h>

struct cmd {
    char * cmd;
    char * help;
    int (*hnd)(char *);
};

static Channel * chan;
static Protocol * proto;
static FILE * infile;
static int interactive_flag;
static int cmdline_suspended;
static int cmdline_pending;
static char * cmdline_string;
static pthread_mutex_t cmdline_mutex;
static pthread_cond_t cmdline_signal;
static pthread_t interactive_thread;
static struct cmd * cmds = NULL;
static size_t cmd_count = 0;

typedef void (*PluginCallBack)(Channel *);

static PluginCallBack * connect_hnds = NULL;
static size_t connect_hnd_count = 0;
static PluginCallBack * disconnect_hnds = NULL;
static size_t disconnect_hnd_count = 0;

static void destroy_cmdline_handler() {
    size_t i;
    for (i = 0; i < cmd_count; ++i) {
        loc_free(cmds[i].cmd);
        loc_free(cmds[i].help);
    }
    loc_free(cmds);
    loc_free(connect_hnds);
    loc_free(disconnect_hnds);
}

static void channel_disconnected(Channel * c) {
    size_t i = 0;
    if (chan == c) chan = NULL;
    protocol_release(c->protocol);
    for (; i < disconnect_hnd_count; ++i)
        disconnect_hnds[i](c);
}

static int cmd_exit(char * s) {
    destroy_cmdline_handler();
    exit(0);
}

static void cmd_done(void);

static void display_tcf_reply(Channel * c, void * client_data, int error) {
    int i;

    if (error) {
        printf("Reply error %d: %s\n", error, errno_to_str(error));
        cmd_done();
        return;
    }
    for (;;) {
        i = read_stream(&c->inp);
        if (i == MARKER_EOM) break;
        if (i == 0) i = ' ';
        putchar(i);
    }
    putchar('\n');

    /* We flush the stream to be able to connect to the client with pipes
     * and receive the message when it's displayed */
    fflush(0);

    cmd_done();
}

#define maxargs 20

static int cmd_tcf(char *s) {
    int i;
    int ind;
    char * args[maxargs];
    Channel * c = chan;

    if (c == NULL) {
        printf("Error: Channel not connected, use 'connect' command\n");
        return 0;
    }
    ind = 0;
    args[ind] = strtok(s, " \t");
    while (args[ind] != NULL && ++ind < maxargs) {
        args[ind] = strtok(NULL, " \t");
    }
    if (args[0] == NULL || args[1] == NULL) {
        printf("Error: Expected at least service and command name arguments\n");
        return 0;
    }
    protocol_send_command(c, args[0], args[1], display_tcf_reply, c);
    for (i = 2; i < ind; i++) {
        write_stringz(&c->out, args[i]);
    }
    write_stream(&c->out, MARKER_EOM);
    return 1;
}

static int print_peer_flags(PeerServer * ps) {
    unsigned int flags = ps->flags;
    int cnt;
    int i;
    struct {
        unsigned int flag;
        const char * name;
    } flagnames[] = {
        { PS_FLAG_LOCAL, "local" },
        { PS_FLAG_PRIVATE, "private" },
        { PS_FLAG_DISCOVERABLE, "discoverable" },
        { 0 }
    };

    printf("  ");
    cnt = 0;
    for (i = 0; flagnames[i].flag != 0; i++) {
        if (flags & flagnames[i].flag) {
            if (cnt != 0) {
                printf(", ");
            }
            cnt++;
            /* We add the "s" format string to get rid of a gcc warning */
            printf("%s", flagnames[i].name);
            flags &= ~flagnames[i].flag;
        }
    }
    if (flags || cnt == 0) printf("0x%x", flags);
    return 0;
}

static int print_peer_summary(PeerServer * ps, void * client_data) {
    const char * s = peer_server_getprop(ps, "Name", NULL);
    printf("  %s", ps->id);
    if (s != NULL) printf(", %s", s);
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
        fprintf(stderr, "Error: Cannot find id: %s\n", s);
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

static void connect_done(void * args, int error, Channel * c) {
    PeerServer * ps = (PeerServer *)args;

    if (error) {
        fprintf(stderr, "Error: Cannot connect: %s\n", errno_to_str(error));
    }
    else {
        size_t i = 0;
        c->disconnected = channel_disconnected;
        c->protocol = proto;
        protocol_reference(proto);
        channel_start(c);
        chan = c;
        for (; i < connect_hnd_count; ++i)
            connect_hnds[i](c);
    }
    peer_server_free(ps);
    cmd_done();
}

static int cmd_connect(char * s) {
    PeerServer * ps = NULL;

    ps = channel_peer_from_url(s);
    if (ps == NULL) {
        fprintf(stderr, "Error: Cannot parse peer identifer: %s\n", s);
        return 0;
    }

    channel_connect(ps, connect_done, ps);
    return 1;
}

static void event_cmd_line(void * arg) {
    char * s = (char *)arg;
    int len;
    int delayed = 0;
    size_t cp;

    if (cmdline_suspended) {
        cmdline_string = s;
        return;
    }

    while (*s && isspace(*s)) s++;
    if (*s) {
        for (cp = 0; cp < cmd_count; ++cp) {
            len = strlen(cmds[cp].cmd);
            if (strncmp(s, cmds[cp].cmd, len) == 0 && (s[len] == 0 || isspace(s[len]))) {
                s += len;
                while (*s && isspace(*s)) s++;
                delayed = cmds[cp].hnd(s);
                break;
            }
        }
        if (cp == cmd_count) {
            fprintf(stderr, "Unknown command: %s\n", s);
            fprintf(stderr, "Available commands:\n");
            for (cp = 0; cp < cmd_count; ++cp) {
                fprintf(stderr, "  %-10s - %s\n", cmds[cp].cmd, cmds[cp].help);
            }
        }
    }
    loc_free(arg);
    if (!delayed) cmd_done();
}

void cmdline_suspend(void) {
    assert(!cmdline_suspended);
    cmdline_suspended = 1;
}

void cmdline_resume(void) {
    assert(cmdline_suspended);
    cmdline_suspended = 0;
    if (cmdline_string != NULL) {
        post_event(event_cmd_line, cmdline_string);
        cmdline_string = NULL;
    }
}

static void cmd_done_event(void * arg) {
    check_error(pthread_mutex_lock(&cmdline_mutex));
    assert(cmdline_pending);
    cmdline_pending = 0;
    check_error(pthread_cond_signal(&cmdline_signal));
    check_error(pthread_mutex_unlock(&cmdline_mutex));
}

static void cmd_done(void) {
    post_event(cmd_done_event, NULL);
}

static void * interactive_handler(void * x) {
    int done = 0;
    int len;
    char buf[1000];

    check_error(pthread_mutex_lock(&cmdline_mutex));
    while (!done) {
        if (cmdline_pending) {
            check_error(pthread_cond_wait(&cmdline_signal, &cmdline_mutex));
            continue;
        }
        if (interactive_flag) {
            printf("> ");
            fflush(stdout);
        }
        if (fgets(buf, sizeof(buf), infile) == NULL) {
            strcpy(buf, "exit");
            done = 1;
        }
        len = strlen(buf);
        if (len > 0 && buf[len-1] == '\n') {
            buf[--len] = '\0';
        }
        post_event(event_cmd_line, loc_strdup(buf));
        cmdline_pending = 1;
    }
    check_error(pthread_mutex_unlock(&cmdline_mutex));
    return NULL;
}

void open_script_file(const char * script_name) {
    if (script_name == NULL || (infile = fopen(script_name, "r")) == NULL) {
        if (script_name == NULL) script_name = "<null>";
        fprintf(stderr, "Error: Cannot open script file %s\n", script_name);
        exit(1);
    }
}

static int add_cmdline_cmd(const char * cmd_name, const char * cmd_desc,
        int (*hnd)(char *)) {
    size_t i;
    assert(is_dispatch_thread());
    if (!cmd_name || !cmd_desc || !hnd) return -EINVAL;

    /* Check if the cmd name already exists */
    for (i = 0; i < cmd_count; ++i)
        if (!strcmp(cmd_name, cmds[i].cmd))
            return -EEXIST;

    cmds = (struct cmd *)loc_realloc(cmds, ++cmd_count * sizeof(struct cmd));

    cmds[cmd_count-1].cmd = loc_strdup(cmd_name);
    cmds[cmd_count-1].help = loc_strdup(cmd_desc);
    cmds[cmd_count-1].hnd = hnd;

    return 0;
}

#if ENABLE_Plugins
static int add_connect_callback(PluginCallBack hnd){
    size_t i;
    assert(is_dispatch_thread());
    if (!hnd) return -EINVAL;

    /* Check if the handle already exists */
    for (i = 0; i < connect_hnd_count; ++i)
        if (hnd == connect_hnds[i])
            return -EEXIST;

    connect_hnds = (PluginCallBack *)loc_realloc(connect_hnds, ++connect_hnd_count * sizeof(PluginCallBack));
    connect_hnds[connect_hnd_count - 1] = hnd;

    return 0;
}

static int add_disconnect_callback(PluginCallBack hnd) {
    size_t i;
    assert(is_dispatch_thread());
    if (!hnd) return -EINVAL;

    /* Check if the handle already exists */
    for (i = 0; i < disconnect_hnd_count; ++i)
        if (hnd == disconnect_hnds[i])
            return -EEXIST;

    disconnect_hnds = (PluginCallBack *)loc_realloc(disconnect_hnds, ++disconnect_hnd_count * sizeof(PluginCallBack));
    disconnect_hnds[disconnect_hnd_count - 1] = hnd;

    return 0;
}
#endif /* ENABLE_Plugins */

void ini_cmdline_handler(int interactive, Protocol * protocol) {
    proto = protocol;

#if ENABLE_Plugins
    if (plugin_add_function("Cmdline_cmd_done", (void *)cmd_done)) {
        fprintf(stderr, "Error: Cannot add cmd_done shared function\n");
    }
    if (plugin_add_function("Cmdline_add_cmd", (void *)add_cmdline_cmd)) {
        fprintf(stderr, "Error: Cannot add add_cmd shared function\n");
    }
    if (plugin_add_function("Cmdline_add_connect_callback", (void *)add_connect_callback)) {
        fprintf(stderr, "Error: Cannot add add_connect_callback shared function\n");
    }
    if (plugin_add_function("Cmdline_add_disconnect_callback", (void *)add_disconnect_callback)) {
        fprintf(stderr, "Error: Cannot add add_disconnect_callback shared function\n");
    }
#endif

    add_cmdline_cmd("exit",      "quit the program",          cmd_exit);
    add_cmdline_cmd("tcf",       "send TCF command",          cmd_tcf);
    add_cmdline_cmd("peers",     "show list of known peers",  cmd_peers);
    add_cmdline_cmd("peerinfo",  "show info about a peer",    cmd_peerinfo);
    add_cmdline_cmd("connect",   "connect a peer",            cmd_connect);

    interactive_flag = interactive;
    if (infile == NULL) infile = stdin;
    check_error(pthread_mutex_init(&cmdline_mutex, NULL));
    check_error(pthread_cond_init(&cmdline_signal, NULL));
    /* Create thread to read cmd line */
    check_error(pthread_create(&interactive_thread, &pthread_create_attr, interactive_handler, 0));
}

#endif /* ENABLE_Cmdline */
