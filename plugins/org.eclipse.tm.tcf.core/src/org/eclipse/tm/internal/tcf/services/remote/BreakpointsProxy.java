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
package org.eclipse.tm.internal.tcf.services.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IBreakpoints;


public class BreakpointsProxy implements IBreakpoints {

    private final IChannel channel;
    private final Map<BreakpointsListener,IChannel.IEventListener> listeners =
        new HashMap<BreakpointsListener,IChannel.IEventListener>();

    public BreakpointsProxy(IChannel channel) {
        this.channel = channel;
    }

    public IToken set(Map<String,Object>[] properties, final DoneCommand done) {
        return new Command(channel, this, "set", new Object[]{ properties }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken add(Map<String,Object> properties, final DoneCommand done) {
        return new Command(channel, this, "add", new Object[]{ properties }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken change(Map<String,Object> properties, final DoneCommand done) {
        return new Command(channel, this, "change", new Object[]{ properties }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken disable(String[] ids, final DoneCommand done) {
        return new Command(channel, this, "disable", new Object[]{ ids }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken enable(String[] ids, final DoneCommand done) {
        return new Command(channel, this, "enable", new Object[]{ ids }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken remove(String[] ids, final DoneCommand done) {
        return new Command(channel, this, "remove", new Object[]{ ids }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneCommand(token, error);
            }
        }.token;
    }

    public IToken getIDs(final DoneGetIDs done) {
        return new Command(channel, this, "getIDs", null) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toStringArray(args[1]);
                }
                done.doneGetIDs(token, error, arr);
            }
        }.token;
    }

    public IToken getProperties(String id, final DoneGetProperties done) {
        return new Command(channel, this, "getProperties", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                Map<String,Object> map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = (Map<String,Object>)args[1];
                }
                done.doneGetProperties(token, error, map);
            }
        }.token;
    }

    public IToken getStatus(String id, final DoneGetStatus done) {
        return new Command(channel, this, "getStatus", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                Map<String,Object> map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = (Map<String,Object>)args[1];
                }
                done.doneGetStatus(token, error, map);
            }
        }.token;
    }

    public IToken getCapabilities(String id, final DoneGetCapabilities done) {
        return new Command(channel, this, "getCapabilities", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                Map<String,Object> map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = (Map<String,Object>)args[1];
                }
                done.doneGetCapabilities(token, error, map);
            }
        }.token;
    }

    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object>[] toBreakpointArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        return (Map<String,Object>[])c.toArray(new Map[c.size()]);
    }

    public void addListener(final BreakpointsListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            @SuppressWarnings("unchecked")
            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("status")) {
                        assert args.length == 2;
                        listener.breakpointStatusChanged((String)args[0], (Map<String,Object>)args[1]);
                    }
                    else if (name.equals("contextAdded")) {
                        assert args.length == 1;
                        listener.contextAdded(toBreakpointArray(args[0]));
                    }
                    else if (name.equals("contextChanged")) {
                        assert args.length == 1;
                        listener.contextChanged(toBreakpointArray(args[0]));
                    }
                    else if (name.equals("contextRemoved")) {
                        assert args.length == 1;
                        listener.contextRemoved(toStringArray(args[0]));
                    }
                    else {
                        throw new IOException("Breakpoints service: unknown event: " + name);
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

    public void removeListener(BreakpointsListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }
}
