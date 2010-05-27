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
 * Simple UDP based discovery server interface
 */

#ifndef D_discovery_udp
#define D_discovery_udp

/*
 * Packet max payload size, assuming:
 * IPv6 header: 40 bytes,
 * UDP header: 8 bytes,
 * max jumbo packet size: 9000 bytes,
 * max non-fragmented packet size: 1500 bytes
 */
#define MAX_PACKET_SIZE (9000 - 40 - 8)
#define PREF_PACKET_SIZE (1500 - 40 - 8)

/* UDP discovery packet types: */
#define UDP_REQ_INFO    1   /* Peer info request */
#define UDP_ACK_INFO    2   /* Peer info - contains list of peer attributes */
#define UDP_REQ_SLAVES  3   /* List of slaves request */
#define UDP_ACK_SLAVES  4   /* List of slaves - contains list of socket addresses */

#define UDP_VERSION     '2'

/*
 * Start UDP discovery server
 */
extern int discovery_start_udp(void);

#endif /* D_discovery_udp */
