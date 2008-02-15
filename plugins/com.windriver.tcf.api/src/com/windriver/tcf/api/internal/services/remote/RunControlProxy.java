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
package com.windriver.tcf.api.internal.services.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.windriver.tcf.api.core.Command;
import com.windriver.tcf.api.internal.core.ReadOnlyMap;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.JSON;
import com.windriver.tcf.api.services.IRunControl;

public class RunControlProxy implements IRunControl {

    private final IChannel channel;
    private final Map<RunControlListener,IChannel.IEventListener> listeners =
        new HashMap<RunControlListener,IChannel.IEventListener>();

    private class RunContext implements IRunControl.RunControlContext {

        private final Map<String, Object> props;

        RunContext(Map<String, Object> props) {
            assert props instanceof ReadOnlyMap;
            this.props = props;
        }

        public Map<String, Object> getProperties() {
            return props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENT_ID);
        }

        public boolean isContainer() {
            Boolean b = (Boolean)props.get(PROP_IS_CONTAINER);
            return b != null && b.booleanValue();
        }

        public boolean hasState() {
            Boolean b = (Boolean)props.get(PROP_HAS_STATE);
            return b != null && b.booleanValue();
        }

        public boolean canResume(int mode) {
            if (props.containsKey(PROP_CAN_RESUME)) {
                int b = ((Number)props.get(PROP_CAN_RESUME)).intValue();
                return (b & (1 << mode)) != 0;
            }
            return false;
        }

        public boolean canCount(int mode) {
            if (props.containsKey(PROP_CAN_COUNT)) {
                int b = ((Number)props.get(PROP_CAN_COUNT)).intValue();
                return (b & (1 << mode)) != 0;
            }
            return false;
        }

        public boolean canSuspend() {
            Boolean b = (Boolean)props.get(PROP_CAN_SUSPEND);
            return b != null && b.booleanValue();
        }

        public boolean canTerminate() {
            Boolean b = (Boolean)props.get(PROP_CAN_TERMINATE);
            return b != null && b.booleanValue();
        }

        public IToken getState(final DoneGetState done) {
            return new Command(channel, RunControlProxy.this, "getState", new Object[]{ getID() }) {
                @SuppressWarnings("unchecked")
                @Override
                public void done(Exception error, Object[] args) {
                    boolean susp = false;
                    String pc = null;
                    String reason = null;
                    Map<String,Object> map = null;
                    if (error == null) {
                        assert args.length == 6;
                        error = toError(args[0], args[1]);
                        susp = ((Boolean)args[2]).booleanValue();
                        if (args[3] != null) pc =  ((Number)args[3]).toString();
                        reason = (String)args[4];
                        map = (Map<String,Object>)args[5];
                    }
                    done.doneGetState(token, error, susp, pc, reason, map);
                }
            }.token;
        }

        public IToken resume(int mode, int count, DoneCommand done) {
            return command("resume", new Object[]{ getID(), mode, count }, done);
        }

        public IToken suspend(DoneCommand done) {
            return command("suspend", new Object[]{ getID() }, done);
        }

        public IToken terminate(DoneCommand done) {
            return command("terminate", new Object[]{ getID() }, done);
        }

        private IToken command(String cmd, Object[] args, final DoneCommand done) {
            return new Command(channel, RunControlProxy.this, cmd, args) {
                @Override
                public void done(Exception error, Object[] args) {
                    if (error == null) {
                        assert args.length == 2;
                        error = toError(args[0], args[1]);
                    }
                    done.doneCommand(token, error);
                }
            }.token;
        }
        
        public String toString() {
            return "[Run Control Context " + props.toString() + "]";
        }
    }

    public RunControlProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public void addListener(final RunControlListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            @SuppressWarnings("unchecked")
            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("contextSuspended")) {
                        assert args.length == 4;
                        listener.contextSuspended(
                                (String)args[0],
                                args[1] == null ? null : ((Number)args[1]).toString(),
                                        (String)args[2], (Map<String,Object>)args[3]);
                    }
                    else if (name.equals("contextResumed")) {
                        assert args.length == 1;
                        listener.contextResumed((String)args[0]);
                    }
                    else if (name.equals("contextAdded")) {
                        assert args.length == 1;
                        listener.contextAdded(toContextArray(args[0]));
                    }
                    else if (name.equals("contextChanged")) {
                        assert args.length == 1;
                        listener.contextChanged(toContextArray(args[0]));
                    }
                    else if (name.equals("contextRemoved")) {
                        assert args.length == 1;
                        listener.contextRemoved(toStringArray(args[0]));
                    }
                    else if (name.equals("contextException")) {
                        assert args.length == 2;
                        listener.contextException((String)args[0], (String)args[1]);
                    }
                    else if (name.equals("containerSuspended")) {
                        assert args.length == 5;
                        listener.containerSuspended(
                                (String)args[0],
                                args[1] == null ? null : ((Number)args[1]).toString(),
                                        (String)args[2], (Map)args[3],
                                        toStringArray(args[4]));
                    }
                    else if (name.equals("containerResumed")) {
                        assert args.length == 1;
                        listener.containerResumed(toStringArray(args[0]));
                    }
                    else {
                        throw new IOException("RunControl service: unknown event: " + name);
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
    
    public void removeListener(RunControlListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }

    public IToken getContext(String context_id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[]{ context_id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                RunControlContext ctx = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    if (args[2] != null) { 
                        ctx = new RunContext((Map<String, Object>)args[2]);
                    }
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public IToken getChildren(String parent_context_id, final DoneGetChildren done) {
        return new Command(channel, this, "getChildren", new Object[]{ parent_context_id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    arr = toStringArray(args[2]);
                }
                done.doneGetChildren(token, error, arr);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private RunControlContext[] toContextArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        int n = 0;
        RunControlContext[] ctx = new RunControlContext[c.size()];
        for (Map<String, Object> m : c) ctx[n++] = new RunContext(m);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        Collection<String> c = (Collection<String>)o;
        if (c == null) return new String[0];
        return (String[])c.toArray(new String[c.size()]);
    }
}
