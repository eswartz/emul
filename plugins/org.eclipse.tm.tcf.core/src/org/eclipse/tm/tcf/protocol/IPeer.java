/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.protocol;

import java.util.Map;

/**
 * Both hosts and targets are represented by objects
 * implementing IPeer interface. A peer can act as host or
 * target depending on services it implements.
 * List of currently known peers can be retrieved by
 * calling ILocator.getPeers()
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * Client can extends the abstract IPeer implementation: AbstractPeer.
 */
public interface IPeer {

    /**
     * Peer property names. Implementation can define additional properties.
     */
    static final String
        /** Peer unique ID */
        ATTR_ID = "ID",

        /** Peer name */
        ATTR_NAME = "Name",

        /** Name of the peer operating system */
        ATTR_OS_NAME = "OSName",

        /** Transport name, for example TCP, SSL */
        ATTR_TRANSPORT_NAME = "TransportName",

        /** If present, indicates that the peer can forward traffic to other peers */
        ATTR_PROXY = "Proxy",

        /** Host DNS name or IP address */
        ATTR_IP_HOST = "Host",

        /** Optional list of host aliases */
        ATTR_IP_ALIASES = "Aliases",

        /** Optional list of host addresses */
        ATTR_IP_ADDRESSES = "Addresses",

        /** IP port number, must be decimal number */
        ATTR_IP_PORT = "Port";


    /**
     * @return map of peer attributes
     */
    Map<String, String> getAttributes();

    /**
     * @return peer unique ID, same as getAttributes().get(ATTR_ID)
     */
    String getID();

    /**
     * @return peer name, same as getAttributes().get(ATTR_NAME)
     */
    String getName();

    /**
     * Same as getAttributes().get(ATTR_OS_NAME)
     */
    String getOSName();

    /**
     * Same as getAttributes().get(ATTR_TRANSPORT_NAME)
     */
    String getTransportName();

    /**
     * Open channel to communicate with this peer.
     * Note: the channel is not fully open yet when this method returns.
     * It’s state is IChannel.STATE_OPENNING.
     * Protocol.Listener will be called when the channel will be opened or closed.
     */
    IChannel openChannel();
}
