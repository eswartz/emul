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
 * Discovery helper interface
 */

#ifndef D_discovery_help
#define D_discovery_help

/*
 * Default discovery master notifier that creates a simple server
 * supporting only basic services needed for discovery
 */
extern void discovery_default_master_notifier(void);

#endif
