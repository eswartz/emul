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

#ifndef D_pathmap
#define D_pathmap

#include <config.h>
#include <framework/protocol.h>

/*
 * Translate debug file name to local name using file path mapping table of given channel.
 * Return pointer to static buffer that contains translated file name,
 * or null if mapping not found.
 */
extern char * path_map_to_local(Channel * channel, char * file_name);

extern void ini_path_map_service(Protocol * proto);

#endif /* D_pathmap */
