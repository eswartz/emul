/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "mdep.h"
#include "context.h"
#include "events.h"
#include "myalloc.h"

static pthread_t interactive_thread;

static void cmd_list_contexts(char *s) {
    LINK * qp;
    for (qp = context_root.next; qp != &context_root; qp = qp->next) {
        Context * ctx = ctxl2ctxp(qp);
        printf("ctx %#x pid %d state %s\n", ctx, ctx->pid, context_state_name(ctx));
    }
}

static void cmd_exit(char *s) {
    exit(0);
}

static void event_cmd_line(void * arg) {
    char * s = (char *)arg;
    int len;
    struct {
        char *cmd;
        void (*hnd)(char *);
    } cmds[] = {
        { "list-contexts",      cmd_list_contexts },
        { "exit",               cmd_exit },
        { 0 }
    }, *cp;

    while (*s && isspace(*s)) s++;
    for (cp = cmds; cp->cmd != 0; cp++) {
        len = strlen(cp->cmd);
        if (strncmp(s, cp->cmd, len) == 0 && (s[len] == 0 || isspace(s[len]))) {
            s += len;
            while (*s && isspace(*s)) s++;
            cp->hnd(s);
            break;
        }
    }
    if (cp->cmd == 0) {
        fprintf(stderr, "unknown command: %s\n", s);
    }
    loc_free(arg);
}

static void * interactive_handler(void *x) {
    char buf[1000];

    while (fgets(buf, sizeof(buf), stdin) != NULL) {
        char * s = (char *)loc_alloc(strlen(buf) + 1);
        strcpy(s, buf);
        post_event(event_cmd_line, s);
    }
    return NULL;
}

void ini_cmdline_handler(void) {
    /* Create thread to read cmd line */
    if (pthread_create(&interactive_thread, &pthread_create_attr, interactive_handler, 0) != 0) {
        perror("pthread_create");
        exit(1);
    }
}

