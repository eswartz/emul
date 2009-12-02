/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * This module forwards handling of process/thread OS contexts to remote peer.
 */

#include "config.h"

#if ENABLE_DebugContext && ENABLE_ContextProxy

#include <errno.h>
#include "context.h"

typedef struct ContextCache ContextCache;
typedef struct PeerCache PeerCache;

struct ContextCache {
    Context * ctx;
};

struct PeerCache {
    int x;
};

int context_has_state(Context * ctx) {
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    errno = EINVAL;
    return -1;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    errno = EINVAL;
    return -1;
}

void init_contexts_sys_dep(void) {
}

#endif /* ENABLE_DebugContext && ENABLE_ContextProxy */
