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
import org.eclipse.tm.tcf.services.IRegisters;


public class RegistersProxy implements IRegisters {

    private final IChannel channel;
    private final Map<RegistersListener,IChannel.IEventListener> listeners =
        new HashMap<RegistersListener,IChannel.IEventListener>();
    
    private class Context implements RegistersContext {
        
        private final Map<String,Object> props;
        
        Context(Map<String,Object> props) {
            this.props = props;
        }

        public String[] getAvailableFormats() {
            return toStringArray(props.get(PROP_FORMATS));
        }

        public int[] getBitNumbers() {
            return toIntArray(props.get(PROP_BITS));
        }

        public String getDescription() {
            return (String)props.get(PROP_DESCRIPTION);
        }

        public int getFirstBitNumber() {
            Number n = (Number)props.get(PROP_FIST_BIT);
            if (n == null) return 0;
            return n.intValue();
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getName() {
            return (String)props.get(PROP_NAME);
        }

        public NamedValue[] getNamedValues() {
            return toValuesArray(props.get(PROP_VALUES));
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENT_ID);
        }

        public Map<String, Object> getProperties() {
            return props;
        }

        public boolean hasSideEffects() {
            Boolean n = (Boolean)props.get(PROP_SIDE_EFFECTS);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isBigEndian() {
            Boolean n = (Boolean)props.get(PROP_BIG_ENDIAN);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isFloat() {
            Boolean n = (Boolean)props.get(PROP_FLOAT);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isLeftToRight() {
            Boolean n = (Boolean)props.get(PROP_LEFT_TO_RIGHT);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isReadOnce() {
            Boolean n = (Boolean)props.get(PROP_READ_ONCE);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isReadable() {
            Boolean n = (Boolean)props.get(PROP_READBLE);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isVolatile() {
            Boolean n = (Boolean)props.get(PROP_VOLATILE);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isWriteOnce() {
            Boolean n = (Boolean)props.get(PROP_WRITE_ONCE);
            if (n == null) return false;
            return n.booleanValue();
        }

        public boolean isWriteable() {
            Boolean n = (Boolean)props.get(PROP_WRITEABLE);
            if (n == null) return false;
            return n.booleanValue();
        }

        public IToken get(String format, final DoneGet done) {
            return new Command(channel, RegistersProxy.this, "get",
                    new Object[]{ getID(), format }) {
                @Override
                public void done(Exception error, Object[] args) {
                    String val = null;
                    if (error == null) {
                        assert args.length == 3;
                        error = toError(args[0], args[1]);
                        val = (String)args[2];
                    }
                    done.doneGet(token, error, val);
                }
            }.token;
        }

        public IToken set(String format, String value, final DoneSet done) {
            return new Command(channel, RegistersProxy.this, "set",
                    new Object[]{ getID(), format, value }) {
                @Override
                public void done(Exception error, Object[] args) {
                    if (error == null) {
                        assert args.length == 2;
                        error = toError(args[0], args[1]);
                    }
                    done.doneSet(token, error);
                }
            }.token;
        }
        
        public String toString() {
            return "[Registers Context " + props.toString() + "]";
        }
    }
    
    public RegistersProxy(IChannel channel) {
        this.channel = channel;
    }
    
    public String getName() {
        return NAME;
    }

    public IToken getChildren(String parent_context_id, final DoneGetChildren done) {
        return new Command(channel, this, "getChildren", new Object[]{ parent_context_id }) {
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

    public IToken getContext(String id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                Context ctx = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    if (args[2] != null) {
                        ctx = new Context((Map<String,Object>)args[2]);
                    }
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public void addListener(final RegistersListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("contextChanged")) {
                        listener.contextChanged();
                    }
                    else if (name.equals("registerChanged")) {
                        assert args.length == 1;
                        listener.registerChanged((String)args[0]);
                    }
                    else {
                        throw new IOException("Registers service: unknown event: " + name);
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

    public void removeListener(RegistersListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        Collection<String> c = (Collection<String>)o;
        if (c == null) return new String[0];
        return (String[])c.toArray(new String[c.size()]);
    }
    
    @SuppressWarnings("unchecked")
    private int[] toIntArray(Object o) {
        Collection<Number> c = (Collection<Number>)o;
        if (c == null) return null;
        int i = 0;
        int[] arr = new int[c.size()];
        for (Number n : c) arr[i++] = n.intValue();
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    private NamedValue[] toValuesArray(Object o) {
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        if (c == null) return null;
        int i = 0;
        NamedValue[] arr = new NamedValue[c.size()];
        for (final Map<String,Object> m : c) {
            arr[i++] = new NamedValue() {

                public String getDescription() {
                    return (String)m.get("Description");
                }

                public String getName() {
                    return (String)m.get("Name");
                }

                public Number getValue() {
                    return (Number)m.get("Value");
                }
            };
        }
        return arr;
    }
}
