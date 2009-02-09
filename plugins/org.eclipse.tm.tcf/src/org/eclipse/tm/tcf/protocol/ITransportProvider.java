/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.protocol;

/**
 * ITransportProvider represents communication protocol that can be used to open TCF communication channels.
 * Examples of transports are: TCP/IP, RS-232, USB.
 * 
 * Client can implement this interface if they want to provide support for a transport that is not
 * supported directly by the framework.
 */
public interface ITransportProvider {
    
    /**
     * Return transport name. Same as used as peer attribute, @see IPeer.ATTR_TRANSPORT_NAME
     * @return transport name.
     */
    String getName();

    /**
     * Open channel to communicate with this peer using this transport.
     * Note: the channel can be not fully open yet when this method returns.
     * It’s state can be IChannel.STATE_OPENNING.
     * Protocol.Listener will be called when the channel will be opened or closed.
     * @param peer - a IPeer object that describes remote end-point of the channel. 
     * @return TCF communication channel.
     */
    IChannel openChannel(IPeer peer);
}
