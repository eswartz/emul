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
import org.eclipse.tm.tcf.services.IProcesses;


public class ProcessesProxy implements IProcesses {
    
    private final IChannel channel;
    private final Map<ProcessesListener,IChannel.IEventListener> listeners =
        new HashMap<ProcessesListener,IChannel.IEventListener>();
    
    private class ProcessContext implements IProcesses.ProcessContext {

        private final Map<String,Object> props;
        
        ProcessContext(Map<String,Object> props) {
            this.props = props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENTID);
        }

        public boolean canTerminate() {
            Boolean b = (Boolean)props.get(PROP_CAN_TERMINATE);
            return b != null && b.booleanValue();
        }

        public String getName() {
            return (String)props.get(PROP_NAME);
        }

        public boolean isAttached() {
            Boolean b = (Boolean)props.get(PROP_ATTACHED);
            return b != null && b.booleanValue();
        }

        public IToken attach(final DoneCommand done) {
            return new Command(channel, ProcessesProxy.this,
                    "attach", new Object[]{ getID() }) {
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

        public IToken detach(final DoneCommand done) {
            return new Command(channel, ProcessesProxy.this,
                    "detach", new Object[]{ getID() }) {
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

        public IToken terminate(final DoneCommand done) {
            return new Command(channel, ProcessesProxy.this,
                    "terminate", new Object[]{ getID() }) {
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

        public Map<String, Object> getProperties() {
            return props;
        }

        public String toString() {
            return "[Processes Context " + props.toString() + "]";
        }
    }
    
    public ProcessesProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public void addListener(final ProcessesListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("exited")) {
                        assert args.length == 2;
                        listener.exited((String)args[0], ((Number)args[1]).intValue());
                    }
                    else {
                        throw new IOException("Processes service: unknown event: " + name);
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

    public void removeListener(ProcessesListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }

    public IToken getChildren(String parent_context_id, boolean attached_only, final DoneGetChildren done) {
        return new Command(channel, this,
                "getChildren", new Object[]{ parent_context_id, attached_only }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] ids = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    ids = toStringArray(args[1]);
                }
                done.doneGetChildren(token, error, ids);
            }
        }.token;
    }

    public IToken getContext(String id, final DoneGetContext done) {
        return new Command(channel, this,
                "getContext", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                ProcessContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new ProcessContext((Map<String, Object>)args[1]);
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public IToken getEnvironment(final DoneGetEnvironment done) {
        return new Command(channel, this, "getEnvironment", null) {
            @Override
            public void done(Exception error, Object[] args) {
                Map<String,String> env = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    env = toEnvMap(args[1]);
                }
                done.doneGetEnvironment(token, error, env);
            }
        }.token;
    }

    public IToken start(String directory, String file,
            String[] command_line, Map<String,String> environment,
            boolean attach, final DoneStart done) {
        return new Command(channel, this,
                "start", new Object[]{ directory, file, command_line,
                toEnvStringArray(environment), attach }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                ProcessContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new ProcessContext((Map<String, Object>)args[1]);
                }
                done.doneStart(token, error, ctx);
            }
        }.token;
    }

    public IToken getSignalList(String context_id, final DoneGetSignalList done) {
        return new Command(channel, ProcessesProxy.this,
                "getSignalList", new Object[]{ context_id }) {
            @Override
            public void done(Exception error, Object[] args) {
                Collection<Map<String,Object>> list = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    list = toSignalList(args[1]);
                }
                done.doneGetSignalList(token, error, list);
            }
        }.token;
    }

    public IToken getSignalMask(String context_id, final DoneGetSignalMask done) {
        return new Command(channel, ProcessesProxy.this,
                "getSignalMask", new Object[]{ context_id }) {
            @Override
            public void done(Exception error, Object[] args) {
                int dont_stop = 0;
                int dont_pass = 0;
                int pending = 0;
                if (error == null) {
                    assert args.length == 4;
                    error = toError(args[0]);
                    if (args[1] != null) dont_stop = ((Number)args[1]).intValue();
                    if (args[2] != null) dont_pass = ((Number)args[2]).intValue();
                    if (args[3] != null) pending = ((Number)args[3]).intValue();
                }
                done.doneGetSignalMask(token, error, dont_stop, dont_pass, pending);
            }
        }.token;
    }

    public IToken setSignalMask(String context_id, int dont_stop, int dont_pass, final DoneCommand done) {
        return new Command(channel, ProcessesProxy.this,
                "setSignalMask", new Object[]{ context_id, dont_stop, dont_pass }) {
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

    public IToken signal(String context_id, long signal, final DoneCommand done) {
        return new Command(channel, ProcessesProxy.this,
                "signal", new Object[]{ context_id, signal }) {
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

    @SuppressWarnings("unchecked")
    private static String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }

    private static String[] toEnvStringArray(Map<String,String> m) {
        if (m == null) return new String[0];
        int n = 0;
        String[] arr = new String[m.size()];
        for (String s : m.keySet()) {
            arr[n++] = s + "=" + m.get(s);
        }
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String,String> toEnvMap(Object o) {
        Map<String,String> m = new HashMap<String,String>();
        if (o == null) return m;
        Collection<String> c = (Collection<String>)o;
        for (String s : c) {
            int i = s.indexOf('=');
            if (i >= 0) m.put(s.substring(0, i), s.substring(i + 1));
            else m.put(s, "");
        }
        return m;
    }
    
    @SuppressWarnings("unchecked")
    private static Collection<Map<String,Object>> toSignalList(Object o) {
        if (o == null) return null;
        return (Collection<Map<String,Object>>)o;
    }
}
