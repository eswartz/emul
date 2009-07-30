/*******************************************************************************
 * Copyright (c) 2009 Philippe Proulx, École Polytechnique de Montréal
 *                    Michael Sills-Lavoie, École Polytechnique de Montréal
 * and others. All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Philippe Proulx - initial plugins system
 *******************************************************************************/

/*
 * Plugins system.
 */

#include "config.h"

#if ENABLE_Plugins

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include <dlfcn.h>

#include "trace.h"
#include "plugins.h"

static void ** plugins_handles = NULL;
static size_t plugins_count = 0;

static inline int plugins_ext_is(const char * ext, const char * filename) {
    const char * real_ext = strrchr(filename, '.');
    return real_ext != NULL && !strcmp(real_ext + 1, ext);
}

int plugins_load(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
    int ret = 0;
    struct dirent * dirent;
    DIR * dir;

    dir = opendir(QUOTE(PATH_Plugins));
    if (!dir) {
        trace(LOG_ALWAYS, "plugins error: failed opening plugins directory \"" QUOTE(PATH_Plugins) "\"");
        return -1;
    }
    while (dirent = readdir(dir)) {
        char * cur_plugin_path = NULL;
        if (!strcmp(dirent->d_name, ".") || !strcmp(dirent->d_name, "..")) {
            continue;
        }
        if (!plugins_ext_is(PLUGINS_DEF_EXT, dirent->d_name) || dirent->d_type == DT_DIR) {
            continue;
        }
        if (asprintf(&cur_plugin_path, QUOTE(PATH_Plugins) "/%s", dirent->d_name) == -1) {
            trace(LOG_ALWAYS, "plugins error: `asprintf' failed for plugin \"%s\"", dirent->d_name);
            return -1;
        }
        if (plugin_init(cur_plugin_path, proto, bcg, spg)) {
            trace(LOG_ALWAYS, "plugins error: unable to start plugin \"%s\"", cur_plugin_path);
            ret = -1;
            /* Continue to load the rest of plugins */
        }
        free(cur_plugin_path);
    }
    closedir(dir);

    return ret;
}

int plugin_init(const char * name, Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
    void * handle;
    void (* init)(Protocol *, TCFBroadcastGroup *, TCFSuspendGroup *);
    char * error;

    /* Plugin loading: */
    trace(LOG_ALWAYS, "loading plugin \"%s\"", name);
    handle = dlopen(name, RTLD_LAZY);
    if (!handle) {
        trace(LOG_ALWAYS, "plugins error: \"%s\"", dlerror());
        return -1;
    }

    /* Plugin initialization: */
    init = dlsym(handle, "tcf_init_plugin");
    if ((error = dlerror()) != NULL) {
        trace(LOG_ALWAYS, "plugins error: \"%s\"", error);
        return -1;
    }
    trace(LOG_ALWAYS, "initializing plugin \"%s\"", name);
    init(proto, bcg, spg);

    /* Handles table update: */
    plugins_handles = (void **) realloc(plugins_handles, ++plugins_count * sizeof(void *));
    plugins_handles[plugins_count - 1] = handle;

    return 0;
}

int plugins_destroy(void) {
    size_t i;

    if (plugins_handles == NULL) return 0;

    for (i = 0; i < plugins_count; ++i) {
        if (dlclose(plugins_handles[i])) {
            trace(LOG_ALWAYS, "plugins error: \"%s\"", dlerror());
        }
    }
    free(plugins_handles);

    return 0;
}

#endif  /* if ENABLE_Plugins */

