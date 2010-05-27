/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 * IP interface list
 */

#ifndef D_ip_ifc
#define D_ip_ifc

typedef struct ip_ifc_info {
    uint32_t addr;
    uint32_t mask;
} ip_ifc_info;

/*
 * Build interface list for socket,
 * Return number of interfaces in the list.
 */
extern int build_ifclist(int sock, int max, ip_ifc_info * list);

#endif /* D_ip_ifc */
