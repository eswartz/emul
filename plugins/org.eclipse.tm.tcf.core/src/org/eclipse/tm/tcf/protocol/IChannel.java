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
package org.eclipse.tm.tcf.protocol;

import java.util.Collection;

/**
 * IChannel represents communication link connecting two end points (peers).
 * The channel asynchronously transmits messages: commands, results and events.
 * A single channel may be used to communicate with multiple services.
 * Multiple channels may be used to connect the same peers, however no command or event
 * ordering is guaranteed across channels.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * Clients can extend abstract implementations of IChannel: AbstractChannel or StreamChannel.
 */

public interface IChannel {

    /**
     * Channel state IDs
     */
    static final int
        STATE_OPENNING = 0,
        STATE_OPEN = 1,
        STATE_CLOSED = 2;

    /**
     * @return channel state, see STATE_*
     */
    int getState();

    /**
     * Send command message to remote peer for execution. Commands can be queued
     * locally before transmission. Sending commands too fast can fill up
     * communication channel buffers. Calling thread will be blocked until
     * enough buffer space is freed up by transmitting pending messages.
     * @param service - a remote service that will be sent the command
     * @param name - command name
     * @param args - command arguments encoded into array of bytes
     * @param done - call back object
     * @return pending command handle 
     */
    IToken sendCommand(IService service, String name, byte[] args, ICommandListener done);

    /**
     * Command listener interface. Clients implement this interface to
     * receive command results.  
     */
    interface ICommandListener {

        /**
         * Called when progress message (intermediate result) is received
         * from remote peer.
         * @param token - command handle
         * @param data - progress message arguments encoded into array of bytes
         */
        void progress(IToken token, byte[] data);

        /**
         * Called when command result received from remote peer.
         * @param token - command handle
         * @param data - command result message arguments encoded into array of bytes
         */
        void result(IToken token, byte[] data);

        /**
         * Called when command is terminated because communication channel was closed or
         * command is not recognized by remote peer.
         * @param token - command handle
         * @param error - exception that forced the channel to close
         */
        void terminated(IToken token, Exception error);
    }

    /**
     * Send result message to remote peer. Messages can be queued locally before
     * transmission. Sending messages too fast can fill up communication channel
     * buffers. Calling thread will be blocked until enough buffer space is
     * freed up by transmitting pending messages.
     * @param token - command handle
     * @param results - result message arguments encoded into array of bytes
     */
    void sendResult(IToken token, byte[] results);

    /**
     * Reject a command by sending "N" result message to remote peer.
     * Clients should reject commands that they don't recognize.
     * Messages can be queued locally before
     * transmission. Sending messages too fast can fill up communication channel
     * buffers. Calling thread will be blocked until enough buffer space is
     * freed up by transmitting pending messages.
     * @param token - command handle
     * @param results - result message arguments encoded into array of bytes
     */
    void rejectCommand(IToken token);

    /**
     * Get current level of out-bound traffic congestion.
     * 
     * @return integer value in range –100..100, where –100 means no pending
     *         messages (no traffic), 0 means optimal load, and positive numbers
     *         indicate level of congestion.
     * 
     * Note: in-bound traffic congestion is detected by framework and reported to
     * remote peer without client needed to be involved. Clients willing to provide
     * additional data about local congestion should register itself using
     * Protocol.addCongestionMonitor().
     */
    int getCongestion();

    /**
     * Channel listener interface.
     */
    interface IChannelListener {

        /**
         * Called when a channel is opened or redirected.
         */
        void onChannelOpened();

        /**
         * Called when channel closed. If it is closed because of an error,
         * ‘error’ parameter will describe the error. ‘error’ is null if channel
         * is closed normally by calling Channel.close().
         * @param error - channel exception or null
         */
        void onChannelClosed(Throwable error);

        /**
         * Notifies listeners about channel out-bound traffic congestion level changes.
         * When level > 0 client should delay sending more messages.
         * @param level - current congestion level
         */
        void congestionLevel(int level);
    }

    /**
     * Subscribe a channel listener. The listener will be notified about changes of
     * channel state and changes of out-bound traffic congestion level.
     * @param listener - channel listener implementation
     */
    void addChannelListener(IChannelListener listener);

    /**
     * Remove a channel listener.
     * @param listener - channel listener implementation
     */
    void removeChannelListener(IChannelListener listener);

    /**
     * Command server interface.
     * This interface is to be implemented by service providers.
     */
    interface ICommandServer {

        /**
         * Called every time a command is received from remote peer.
         * @param token - command handle
         * @param name - command name
         * @param data - command arguments encoded into array of bytes
         */
        void command(IToken token, String name, byte[] data);
    }

    /**
     * Subscribe a command server. The server will be notified about command
     * messages received through this channel for given service.
     * @param service - local service implementation
     * @param server - implementation of service commands listener 
     */
    void addCommandServer(IService service, ICommandServer server);

    /**
     * Remove a command server.
     * @param service - local service implementation
     * @param server - implementation of service commands listener 
     */
    void removeCommandServer(IService service, ICommandServer server);

    /**
     * A generic interface for service event listener.
     * Services usually define a service specific event listener interface,
     * which is implemented using this generic listener.
     * Clients should user service specific listener interface,
     * unless no such interface is defined. 
     */
    interface IEventListener {
        /**
         * Called when service event message is received
         * @param name - event name
         * @param data - event arguments encode as array of bytes
         */
        void event(String name, byte[] data);
    }

    /**
     * Subscribe an event listener for given service.
     * @param service - remote service proxy
     * @param server - implementation of service event listener 
     */
    void addEventListener(IService service, IEventListener listener);

    /**
     * Unsubscribe an event listener for given service.
     * @param service - remote service proxy
     * @param server - implementation of service event listener 
     */
    void removeEventListener(IService service, IEventListener listener);

    /**
     * @return IPeer object representing local endpoint of communication channel.
     */
    IPeer getLocalPeer();

    /**
     * @return IPeer object representing remote endpoint of communication channel.
     */
    IPeer getRemotePeer();

    /**
     * @return collection of services available on local peer.
     */
    Collection<String> getLocalServices();

    /**
     * @return an object representing a service from local peer.
     * Return null if the service is not available.
     */
    IService getLocalService(String service_name);

    /**
     * @return an object representing a service from local peer.
     * Service object should implement given interface.
     * Return null if implementation of the interface is not available.
     */
    <V extends IService> V getLocalService(Class<V> service_interface);

    /**
     * @return collection of services available on remote peer.
     */
    Collection<String> getRemoteServices();

    /**
     * Get a proxy for remote service.
     * @param service_name - remote service name
     * @return an object (proxy) representing a service from remote peer.
     * Return null if the service is not available.
     * Return an instance of GenericProxy if 'service_name' is not a standard TCF service.
     */
    IService getRemoteService(String service_name);

    /**
     * Get a proxy for remote service.
     * @param service_interface - service interface class
     * @return an object (proxy) representing a service from remote peer,
     * which implements given interface.
     * Return null if implementation of the interface is not available.
     */
    <V extends IService> V getRemoteService(Class<V> service_interface);
    
    /**
     * Install a service proxy object on this channel.
     * This method can be called only from channel open call-back.
     * It allows a client to extend TCF by adding proxy objects for non-standard services.
     * Client, wishing to become service proxy provider, should register itself
     * using either Protocol.addChannelOpenListener() or IChannel.addChannelListener().
     * It is not allowed to install more then one proxy for a given service interface on
     * a particular channel.
     * @param service_interface - service interface class
     * @param service_proxy - service proxy object    
     */
    <V extends IService> void setServiceProxy(Class<V> service_interface, IService service_proxy);
    
    /**
     * Close communication channel.
     */
    void close();

    /**
     * Close channel in case of communication error.
     * @param error - cause of channel termination
     */
    void terminate(Throwable error);
    
    /**
     * Redirect this channel to given peer using this channel remote peer locator service as a proxy.
     * @param peer_id - peer that will become new remote communication endpoint of this channel
     */
    void redirect(String peer_id);
}
