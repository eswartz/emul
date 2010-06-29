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
 * A TCF agent houses one or more service managers. A service manager has a one or more
 * services to expose to the world. The service manager creates one or more peers
 * to represent itself, one for every access path the agent is
 * reachable by. For example, in agents accessible via TCP/IP, the
 * service manger would create a peer for every subnet it wants to participate in.
 * All peers of particular service manager represent identical sets of services.
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

        /** Unique ID of service manager that is represented by this peer */
        ATTR_SERVICE_MANGER_ID = "ServiceManagerID",

        /** Agent unique ID */
        ATTR_AGENT_ID = "AgentID",

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
     * @return service manager unique ID, same as getAttributes().get(ATTR_SERVICE_MANAGER_ID)
     */
    String getServiceManagerID();

    /**
     * @return agent unique ID, same as getAttributes().get(ATTR_AGENT_ID)
     */
    String getAgentID();

    /**
     * @return peer name, same as getAttributes().get(ATTR_NAME)
     */
    String getName();

    /**
     * @return agent OS name, same as getAttributes().get(ATTR_OS_NAME)
     */
    String getOSName();

    /**
     * @return transport name, same as getAttributes().get(ATTR_TRANSPORT_NAME)
     */
    String getTransportName();

    /**
     * Open channel to communicate with this peer.
     * Note: the channel is not fully open yet when this method returns.
     * Its state is IChannel.STATE_OPENNING.
     * Protocol.Listener will be called when the channel will be opened or closed.
     */
    IChannel openChannel();
}
