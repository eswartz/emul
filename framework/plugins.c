/*******************************************************************************
 * Copyright (c) 2009, 2010 Philippe Proulx, École Polytechnique de Montréal
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
 *     Michael Sills-Lavoie - deterministic plugins loading order
 *     Michael Sills-Lavoie - plugin's shared functions
 *******************************************************************************/

/*
 * Plugins system.
 */

#include <config.h>

#if ENABLE_Plugins

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include <dlfcn.h>
#include <errno.h>

#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/plugins.h>

#define _QUOTEME(x)     #x
#define QUOTE(x)      _QUOTEME(x)

#if defined(WIN32)
#define PLUGINS_DEF_EXT     "dll"       /* Default plugins' extension */
#else
#define PLUGINS_DEF_EXT     "so"        /* Default plugins' extension */
#endif

typedef void (*InitFunc)(Protocol *, TCFBroadcastGroup *, void *);

static void ** plugins_handles = NULL;
static size_t plugins_count = 0;
static struct function_entry {
    char * name;
    void * function;
} * function_entries = NULL;
static size_t function_entry_count = 0;

static inline int plugins_ext_is(const char * ext, const char * filename) {
    const char * real_ext = strrchr(filename, '.');
    return real_ext != NULL && !strcmp(real_ext + 1, ext);
}

static int plugins_filter(const struct dirent * dirent) {
    if (!strcmp(dirent->d_name, ".") || !strcmp(dirent->d_name, ".."))
        return 0;
    if (!plugins_ext_is(PLUGINS_DEF_EXT, dirent->d_name) || dirent->d_type == DT_DIR)
        return 0;
    return 1;
}

#if defined(__GLIBC__) && (__GLIBC__ < 2 || (__GLIBC__ == 2 && __GLIBC_MINOR__ < 10))
static int plugins_ralphasort(const void * a, const void * b) {
#else
static int plugins_ralphasort(const struct dirent ** a, const struct dirent ** b) {
#endif
    return -alphasort(a, b);
}

int plugins_load(Protocol * proto, TCFBroadcastGroup * bcg) {
    struct dirent ** files;
    int file_count = -1;
    int ret = 0;

    file_count = scandir(QUOTE(PATH_Plugins), &files, plugins_filter, plugins_ralphasort);
    if (file_count < 0) {
        trace(LOG_ALWAYS, "plugins error: failed opening plugins directory \"" QUOTE(PATH_Plugins) "\"");
        return -1;
    }

    while (file_count--) {
        char * cur_plugin_path = NULL;

        if (asprintf(&cur_plugin_path, QUOTE(PATH_Plugins) "/%s", files[file_count]->d_name) == -1) {
            trace(LOG_ALWAYS, "plugins error: `asprintf' failed for plugin \"%s\"", files[file_count]->d_name);
            ret = -1;
            goto delete_cur_entry;
        }
        if (plugin_init(cur_plugin_path, proto, bcg)) {
            trace(LOG_ALWAYS, "plugins error: unable to start plugin \"%s\"", cur_plugin_path);
            ret = -1;
            /* Continue to load the rest of plugins */
        }

        /* cur_plugin_path and files were allocated by asprintf() and scandir(),
         * and they should be released by free(), don't call loc_free() here. */
        free(cur_plugin_path);
delete_cur_entry:
        free(files[file_count]);
    }
    free(files);

    return ret;
}

int plugin_init(const char * name, Protocol * proto, TCFBroadcastGroup * bcg) {
    void * handle;
    char * error;
    InitFunc init;

    /* Plugin loading: */
    trace(LOG_ALWAYS, "loading plugin \"%s\"", name);
    handle = dlopen(name, RTLD_LAZY);
    if (!handle) {
        trace(LOG_ALWAYS, "plugins error: \"%s\"", dlerror());
        return -1;
    }

    /* Plugin initialization: */
    init = (InitFunc)dlsym(handle, "tcf_init_plugin");
    if ((error = dlerror()) != NULL) {
        trace(LOG_ALWAYS, "plugins error: \"%s\"", error);
        return -1;
    }
    trace(LOG_ALWAYS, "initializing plugin \"%s\"", name);
    init(proto, bcg, NULL);

    /* Handles table update: */
    plugins_handles = (void **) loc_realloc(plugins_handles,
            ++plugins_count * sizeof(void *));
    plugins_handles[plugins_count - 1] = handle;

    return 0;
}

int plugin_add_function(const char * name, void * function) {
    size_t i;

    if (!name || !function) return -EINVAL;

    /* Check if the function name already exists */
    for (i = 0; i < function_entry_count; ++i)
        if (!strcmp(name, function_entries[i].name))
            return -EEXIST;

    function_entries = (struct function_entry *) loc_realloc(function_entries,
            ++function_entry_count * sizeof(struct function_entry));

    function_entries[function_entry_count-1].function = function;
    function_entries[function_entry_count-1].name = loc_strdup(name);
    return 0;
}

void * plugin_get_function(const char * name) {
    size_t i;

    if (!name) return NULL;

    for (i = 0; i < function_entry_count; ++i)
        if (!strcmp(name, function_entries[i].name))
            return function_entries[i].function;

    return NULL;
}

int plugins_destroy(void) {
    size_t i;

    for (i = 0; i < plugins_count; ++i) {
        if (dlclose(plugins_handles[i])) {
            trace(LOG_ALWAYS, "plugins error: \"%s\"", dlerror());
        }
    }
    loc_free(plugins_handles);

    for (i = 0; i < function_entry_count; ++i)
        loc_free(function_entries[i].name);
    loc_free(function_entries);

    return 0;
}

#endif  /* if ENABLE_Plugins */

