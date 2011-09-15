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

#ifndef D_pathmap
#define D_pathmap

#include <config.h>
#include <framework/context.h>
#include <framework/protocol.h>

/*
 * Convert a file name to canonic form that is suitable for file name comparisons.
 * Unlike canonicalize_file_name() or realpath(), the function can be used for remote files.
 * Return pointer to a static array with converted name.
 */
extern char * canonic_path_map_file_name(const char * fnm);

#if SERVICE_PathMap

#define PATH_MAP_TO_CLIENT 1
#define PATH_MAP_TO_LOCAL  2
#define PATH_MAP_TO_TARGET 3

/*
 * Path map listener.
 */
typedef struct PathMapEventListener {
    void (*mapping_changed)(Channel * c, void * client_data);
} PathMapEventListener;

/*
 * Translate debug file name to local or target file name using file path mapping table of given channel.
 * Return pointer to static buffer that contains translated file name.
 */
extern char * apply_path_map(Channel * channel, Context * ctx, char * file_name, int mode);

/*
 * Read new path map from the given input stream.
 */
extern void set_path_map(Channel * c, InputStream * inp);

/*
 * Add path map listener.
 */
extern void add_path_map_event_listener(PathMapEventListener * listener, void * client_data);

extern void ini_path_map_service(Protocol * proto);

#endif /* SERVICE_PathMap */

#endif /* D_pathmap */
