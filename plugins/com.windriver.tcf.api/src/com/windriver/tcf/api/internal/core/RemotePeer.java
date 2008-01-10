/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.internal.core;

import java.util.Iterator;
import java.util.Map;

import com.windriver.tcf.api.core.AbstractPeer;
import com.windriver.tcf.api.core.ChannelTCP;
import com.windriver.tcf.api.protocol.IChannel;

public class RemotePeer extends AbstractPeer {
    
    public RemotePeer(Map<String,String> attrs) {
        super(attrs);
    }
    
    public boolean updateAttributes(Map<String,String> attrs1) {
        boolean equ = true;
        Map<String,String> attrs0 = getAttributesStorage();
        assert attrs1.get(ATTR_ID).equals(attrs0.get(ATTR_ID));
        for (Iterator<String> i = attrs0.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (!attrs0.get(key).equals(attrs1.get(key))) {
                equ = false;
                break;
            }
        }
        for (Iterator<String> i = attrs1.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (!attrs1.get(key).equals(attrs0.get(key))) {
                equ = false;
                break;
            }
        }
        if (!equ) {
            attrs0.clear();
            attrs0.putAll(attrs1);
        }
        return !equ;
    }

    public IChannel openChannel() {
        String transport = getTransportName();
        if (transport.equals("TCP")) {
            Map<String,String> attrs = getAttributes();
            String host = attrs.get(ATTR_IP_HOST);
            String port = attrs.get(ATTR_IP_PORT);
            if (host == null) throw new Error("No host name");
            if (port == null) throw new Error("No port number");
            return new ChannelTCP(this, host, Integer.parseInt(port));
        }
        else {
            throw new Error("Unknow transport name: " + transport);
        }
    }
}
