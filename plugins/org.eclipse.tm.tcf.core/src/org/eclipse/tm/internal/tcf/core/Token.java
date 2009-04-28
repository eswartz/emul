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
package org.eclipse.tm.internal.tcf.core;

import java.io.UnsupportedEncodingException;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;


public class Token implements IToken {

    private static int cnt = 0;

    private final String id;
    private final byte[] bytes;
    private final IChannel.ICommandListener listener;

    public Token() {
        id = null;
        bytes = null;
        listener = null;
    }

    public Token(IChannel.ICommandListener listener) {
        this.listener = listener;
        id = Integer.toString(cnt++);
        try {
            bytes = id.getBytes("ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public Token(byte[] bytes) {
        this.bytes = bytes;
        listener = null;
        try {
            id = new String(bytes, "ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public boolean cancel() {
        return false;
    }

    public String getID() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public IChannel.ICommandListener getListener() {
        return listener;
    }
    
    @Override
    public String toString() {
        return id;
    }
}
