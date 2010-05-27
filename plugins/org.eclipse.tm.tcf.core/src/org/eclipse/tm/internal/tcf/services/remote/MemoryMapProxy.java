/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemoryMap;

public class MemoryMapProxy implements IMemoryMap {

    private final IChannel channel;
    private final Map<MemoryMapListener,IChannel.IEventListener> listeners =
        new HashMap<MemoryMapListener,IChannel.IEventListener>();

    private class Region implements MemoryRegion {

        final Map<String,Object> props;

        Region(Map<String,Object> props) {
            this.props = props;
        }

        public Number getAddress() {
            return (Number)props.get(PROP_ADDRESS);
        }

        public Number getOffset() {
            return (Number)props.get(PROP_OFFSET);
        }

        public Number getSize() {
            return (Number)props.get(PROP_SIZE);
        }

        public String getFileName() {
            return (String)props.get(PROP_FILE_NAME);
        }

        public int getFlags() {
            Number n = (Number)props.get(PROP_FLAGS);
            if (n != null) return n.intValue();
            return 0;
        }

        public Map<String,Object> getProperties() {
            return props;
        }
    }

    public MemoryMapProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken get(String id, final DoneGet done) {
        return new Command(channel, this, "get", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                MemoryRegion[] map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = toMemoryMap(args[1]);
                }
                done.doneGet(token, error, map);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private MemoryRegion[] toMemoryMap(Object o) {
        if (o == null) return null;
        int i = 0;
        Collection<Object> c = (Collection<Object>)o;
        MemoryRegion[] map = new MemoryRegion[c.size()];
        for (Object x : c) map[i++] = toMemoryRegion(x);
        return map;
    }

    @SuppressWarnings("unchecked")
    private MemoryRegion toMemoryRegion(Object o) {
        if (o == null) return null;
        return new Region((Map<String,Object>)o);
    }

    public void addListener(final MemoryMapListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("changed")) {
                        assert args.length == 1;
                        listener.changed((String)args[0]);
                    }
                    else {
                        throw new IOException("Memory Map service: unknown event: " + name);
                    }
                }
                catch (Throwable x) {
                    channel.terminate(x);
                }
            }
        };
        channel.addEventListener(this, l);
        listeners.put(listener, l);
    }

    public void removeListener(MemoryMapListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }
}
