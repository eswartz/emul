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

typedef struct PathMapRule PathMapRule;
typedef struct PathMapRuleAttribute PathMapRuleAttribute;

struct PathMapRuleAttribute {
    PathMapRuleAttribute * next;
    char * name;        /* Attribute name */
    char * value;       /* Attribute value as JSON string */
};

/* Path map attribute names */
#define PATH_MAP_ID             "ID"
#define PATH_MAP_SOURCE         "Source"
#define PATH_MAP_DESTINATION    "Destination"
#define PATH_MAP_CONTEXT        "Context"
#define PATH_MAP_HOST           "Host"
#define PATH_MAP_PROTOCOL       "Protocol"

/*
 * Path map events listener.
 */
typedef struct PathMapEventListener {
    void (*mapping_changed)(Channel * c, void * args);
} PathMapEventListener;

/*
 * Add path map events listener.
 */
extern void add_path_map_event_listener(PathMapEventListener * listener, void * args);

/*
 * Remove path map events listener.
 */
extern void rem_path_map_event_listener(PathMapEventListener * listener);

/*
 * Iterate all path mapping rules registerd for given client,
 * channel == NULL iterates rules that were created by create_path_mapping() function.
 */
typedef void IteratePathMapsCallBack(PathMapRule *, void *);
extern void iterate_path_map_rules(Channel * channel, IteratePathMapsCallBack * callback, void * args);

/*
 * Get path mapping rule attributes.
 */
extern PathMapRuleAttribute * get_path_mapping_attributes(PathMapRule * map);

/*
 * Create new path mapping rule with given attributes.
 * Caller should allocate attributes using myalloc.h functions.
 * Path Map service will free attributes memory using loc_free().
 */
extern PathMapRule * create_path_mapping(PathMapRuleAttribute * attrs);

/*
 * Change path mapping rule attributes to given attributes.
 * Caller should allocate attributes using myalloc.h functions.
 * Path Map service will free attributes memory using loc_free().
 * The function compares existing attributes with new ones,
 * and calls listeners only if attributes are different.
 */
extern void change_path_mapping_attributes(PathMapRule * map, PathMapRuleAttribute * attrs);

/*
 * Delete a path mapping rule.
 */
extern void delete_path_mapping(PathMapRule * bp);

#define PATH_MAP_TO_CLIENT 1
#define PATH_MAP_TO_LOCAL  2
#define PATH_MAP_TO_TARGET 3

/*
 * Translate debug file name to local or target file name using file path mapping table of given channel.
 * Return pointer to static buffer that contains translated file name.
 */
extern char * apply_path_map(Channel * channel, Context * ctx, char * file_name, int mode);

/*
 * Read new path map from the given input stream.
 * The function is used by forwarding value-add server,
 * and it is not intended for use by other clients.
 */
extern void set_path_map(Channel * channel, InputStream * inp);

extern void ini_path_map_service(Protocol * proto);

#endif /* SERVICE_PathMap */

#endif /* D_pathmap */
