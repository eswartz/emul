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
 * Simple UDP based discovery server interface
 */

#ifndef D_discovery_udp
#define D_discovery_udp

/*
 * Start discovery server
 */
extern int discovery_udp_server(const char * port);

#endif
