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

#ifndef D_plugins
#define D_plugins

#include <framework/protocol.h>

/*
 * Loads ALL plugins from the directory PATH_Plugins (from `config.h') in
 * alphabetical order.
 */
int plugins_load(Protocol *, TCFBroadcastGroup *);

/*
 * Initializes a particular plugin according to its path.
 */
int plugin_init(const char *, Protocol *, TCFBroadcastGroup *);

/*
 * Add a new public plugin function for the other plugins to see.
 */
int plugin_add_function(const char *, void *);

/*
 * Get a public function from its name.
 */
void * plugin_get_function(const char *);

/*
 * Destroys loaded plugins.
 */
int plugins_destroy(void);

#endif /* D_plugins */

