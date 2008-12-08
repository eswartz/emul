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

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.ISysMonitor;


public class SysMonitorProxy implements ISysMonitor {

    private final IChannel channel;

    private class SysMonitorContext implements ISysMonitor.SysMonitorContext {

        private final Map<String, Object> props;

        SysMonitorContext(Map<String, Object> props) {
            this.props = props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getCurrentWorkingDirectory() {
            return (String)props.get(PROP_CWD);
        }

        public String getFile() {
            return (String)props.get(PROP_FILE);
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENTID);
        }

        public String getRoot() {
            return (String)props.get(PROP_ROOT);
        }

        public String getGroupName() {
            return (String)props.get(PROP_GROUPNAME);
        }

        public long getPGRP() {
            if (!props.containsKey(PROP_PGRP)) return -1;
            return ((Number)props.get(PROP_PGRP)).longValue();
        }

        public long getPID() {
            if (!props.containsKey(PROP_PID)) return -1;
            return ((Number)props.get(PROP_PID)).longValue();
        }

        public long getPPID() {
            if (!props.containsKey(PROP_PPID)) return -1;
            return ((Number)props.get(PROP_PPID)).longValue();
        }

        public long getRSS() {
            if (!props.containsKey(PROP_RSS)) return -1;
            return ((Number)props.get(PROP_RSS)).longValue();
        }

        public String getState() {
            return (String)props.get(PROP_STATE);
        }

        public long getTGID() {
            if (!props.containsKey(PROP_TGID)) return -1;
            return ((Number)props.get(PROP_TGID)).longValue();
        }

        public long getTracerPID() {
            if (!props.containsKey(PROP_TRACERPID)) return -1;
            return ((Number)props.get(PROP_TRACERPID)).longValue();
        }

        public long getUGID() {
            if (!props.containsKey(PROP_UGID)) return -1;
            return ((Number)props.get(PROP_UGID)).longValue();
        }

        public long getUID() {
            if (!props.containsKey(PROP_UID)) return -1;
            return ((Number)props.get(PROP_UID)).longValue();
        }

        public String getUserName() {
            return (String)props.get(PROP_USERNAME);
        }

        public long getVSize() {
            if (!props.containsKey(PROP_VSIZE)) return -1;
            return ((Number)props.get(PROP_VSIZE)).longValue();
        }

        public long getPSize() {
            if (!props.containsKey(PROP_PSIZE)) return -1;
            return ((Number)props.get(PROP_PSIZE)).longValue();
        }

        public Map<String, Object> getProperties() {
            return props;
        }
        
        public String toString() {
            return "[Sys Monitor Context " + props.toString() + "]";
        }
    }

    public SysMonitorProxy(IChannel channel) {
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
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toStringArray(args[1]);
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
                SysMonitorContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new SysMonitorContext((Map<String, Object>)args[1]);
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public IToken getCommandLine(String id, final DoneGetCommandLine done) {
        return new Command(channel, this, "getCommandLine", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toStringArray(args[1]);
                }
                done.doneGetCommandLine(token, error, arr);
            }
        }.token;
    }

    public IToken getEnvironment(String id, final DoneGetEnvironment done) {
        return new Command(channel, this, "getEnvironment", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toStringArray(args[1]);
                }
                done.doneGetEnvironment(token, error, arr);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private static String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
}
