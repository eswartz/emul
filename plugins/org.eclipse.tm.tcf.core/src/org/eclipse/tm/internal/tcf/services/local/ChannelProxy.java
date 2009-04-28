/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.ChannelLoop;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * ChannelProxy implements forwarding of TCF messages between two channels.
 * The class is used to implement Locator service "redirect" command.
 */
class ChannelProxy {

    private final AbstractChannel ch_x;
    private final AbstractChannel ch_y;
    
    private boolean closed_x;
    private boolean closed_y;
    
    private final Map<IToken,IToken> tokens_x = new HashMap<IToken,IToken>();
    private final Map<IToken,IToken> tokens_y = new HashMap<IToken,IToken>();
    
    private final AbstractChannel.Proxy proxy_x = new AbstractChannel.Proxy() {

        public void onChannelClosed(Throwable error) {
            closed_x = true;
            if (closed_y) return;
            if (error == null) ch_y.close();
            else ch_y.terminate(error);
        }

        public void onCommand(IToken token, String service, String name, byte[] data) {
            if (closed_y) return;
            assert ch_y.getState() == IChannel.STATE_OPEN;
            IService s = ch_y.getRemoteService(service);
            if (s == null) ch_x.terminate(new IOException("Invalid service name"));
            else tokens_x.put(ch_y.sendCommand(s, name, data, cmd_listener_x), token);
        }
        
        public void onEvent(String service, String name, byte[] data) {
            IService s = ch_x.getRemoteService(service);
            if (s == null) ch_x.terminate(new IOException("Invalid service name"));
            else if (!closed_y) ch_y.sendEvent(s, name, data);
        }
    };
    
    private final AbstractChannel.Proxy proxy_y = new AbstractChannel.Proxy() {

        public void onChannelClosed(Throwable error) {
            closed_y = true;
            if (closed_x) return;
            if (error == null) ch_x.close();
            else ch_x.terminate(error);
        }

        public void onCommand(IToken token, String service, String name, byte[] data) {
            if (closed_x) return;
            assert ch_x.getState() == IChannel.STATE_OPEN;
            IService s = ch_x.getRemoteService(service);
            if (s == null) ch_y.terminate(new IOException("Invalid service name"));
            else tokens_y.put(ch_x.sendCommand(s, name, data, cmd_listener_y), token);
        }
        
        public void onEvent(String service, String name, byte[] data) {
            IService s = ch_y.getRemoteService(service);
            if (s == null) ch_y.terminate(new IOException("Invalid service name"));
            else if (!closed_x) ch_x.sendEvent(s, name, data);
        }
    };
    
    private final IChannel.ICommandListener cmd_listener_x = new IChannel.ICommandListener() {

        public void progress(IToken token, byte[] data) {
            ch_x.sendProgress(tokens_x.get(token), data);
        }

        public void result(IToken token, byte[] data) {
            ch_x.sendResult(tokens_x.get(token), data);
            tokens_x.remove(token);
        }

        public void terminated(IToken token, Exception error) {
            tokens_x.remove(token);
        }
    };
    
    private final IChannel.ICommandListener cmd_listener_y = new IChannel.ICommandListener() {

        public void progress(IToken token, byte[] data) {
            ch_y.sendProgress(tokens_y.get(token), data);
        }

        public void result(IToken token, byte[] data) {
            ch_y.sendResult(tokens_y.get(token), data);
            tokens_y.remove(token);
        }

        public void terminated(IToken token, Exception error) {
            tokens_y.remove(token);
        }
    };
    
    ChannelProxy(IChannel x, IChannel y) {
        assert !(x instanceof ChannelLoop);
        assert !(y instanceof ChannelLoop);
        ch_x = (AbstractChannel)x;
        ch_y = (AbstractChannel)y;
        assert ch_x.getState() == IChannel.STATE_OPEN;
        assert ch_y.getState() == IChannel.STATE_OPENNING;
        try {
            ch_y.setProxy(proxy_y, ch_x.getRemoteServices());
            ch_y.addChannelListener(new IChannel.IChannelListener() {

                public void congestionLevel(int level) {
                }

                public void onChannelClosed(Throwable error) {
                    ch_y.removeChannelListener(this);
                    if (error == null) error = new Exception("Channel closed");
                }

                public void onChannelOpened() {
                    ch_y.removeChannelListener(this);
                    try {
                        ch_x.setProxy(proxy_x, ch_y.getRemoteServices());
                    }
                    catch (IOException e) {
                        ch_x.terminate(e);
                        ch_y.terminate(e);
                    }
                }
            });
        }
        catch (IOException e) {
            ch_x.terminate(e);
            ch_y.terminate(e);
        }
    }
}
