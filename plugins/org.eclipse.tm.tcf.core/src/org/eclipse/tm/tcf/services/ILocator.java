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
package org.eclipse.tm.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;


/**
 * ILocator service uses transport layer to search for peers and to collect data about
 * peer’s attributes and capabilities (services). Discovery mechanism depends on transport protocol
 * and is part of that protocol handler. Targets, known to other hosts, can be found through
 * remote instances of ILocator service. Automatically discovered targets require no further
 * configuration. Additional targets can be configured manually.
 * 
 * Clients should use Protocol.getLocator() to obtain local instance of ILocator,
 * then ILocator.getPeers() can be used to get list of available peers (hosts and targets).
 */

public interface ILocator extends IService {

    static final String NAME = "Locator";

    /**
     * Peer data retention period in milliseconds.
     */
    static final long DATA_RETENTION_PERIOD = 60 * 1000;
    
    /**
     * Auto-configuration protocol version.
     */
    static char CONF_VERSION = '2';

    /**
     * Auto-configuration command and response codes.
     */
    static final int
        CONF_REQ_INFO = 1,
        CONF_PEER_INFO = 2,
        CONF_REQ_SLAVES = 3,
        CONF_SLAVES_INFO = 4;

    /**
     * @return Locator service name: "Locator"
     */
    String getName();

    /** 
     * Get map (ID -> IPeer) of available peers (hosts and targets).
     * The method return cached (currently known to the framework) list of peers.
     * The list is updated according to event received from transport layer
     */
    Map<String,IPeer> getPeers();
    
    /**
     * Redirect this service channel to given peer using this service as a proxy.
     * @param peer_id - Peer ID.
     */
    IToken redirect(String peer_id, DoneRedirect done);
    
    interface DoneRedirect {
        void doneRedirect(IToken token, Exception error);
    }
    
    /**
     * Call back after TCF messages sent to this target up to this moment are delivered.
     * This method is intended for synchronization of messages
     * across multiple channels.
     * 
     * Note: Cross channel synchronization can reduce performance and throughput.
     * Most clients don't need channel synchronization and should not call this method. 
     *  
     * @param done will be executed by dispatch thread after communication 
     * messages are delivered to corresponding targets.
     * 
     * This is internal API, TCF clients should use {@code org.eclipse.tm.tcf.protocol.Protocol}.
     */
    IToken sync(DoneSync done);
    
    interface DoneSync {
        void doneSync(IToken token);
    }

    /**
     * Add a listener for ILocator service events.
     */
    void addListener(LocatorListener listener);

    /**
     * Remove a listener for ILocator service events.
     */
    void removeListener(LocatorListener listener);

    interface LocatorListener {
        /**
         * A new peer is added into locator peer table.
         * @param peer
         */
        void peerAdded(IPeer peer);

        /**
         * Peer attributes have changed.
         * @param peer
         */
        void peerChanged(IPeer peer);

        /**
         * A peer is removed from locator peer table.
         * @param id - peer ID
         */
        void peerRemoved(String id);
        
        /**
         * Peer heart beat detected.
         * @param id - peer ID
         */
        void peerHeartBeat(String id);
    }
}