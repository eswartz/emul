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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStackTrace;


public class StackTraceProxy implements IStackTrace {

    private final IChannel channel;

    private class Context implements StackTraceContext {

        private final Map<String,Object> props;

        Context(Map<String,Object> props) {
            if (props == null) props = new HashMap<String,Object>();
            this.props = props;
        }

        public Number getArgumentsAddress() {
            return (Number)props.get(PROP_ARGUMENTS_ADDRESS);
        }

        public int getArgumentsCount() {
            Number n = (Number)props.get(PROP_ARGUMENTS_COUNT);
            if (n == null) return 0;
            return n.intValue();
        }

        public Number getFrameAddress() {
            return (Number)props.get(PROP_FRAME_ADDRESS);
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getName() {
            return (String)props.get(PROP_NAME);
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENT_ID);
        }

        public Number getReturnAddress() {
            return (Number)props.get(PROP_RETURN_ADDRESS);
        }

        public Number getInstructionAddress() {
            return (Number)props.get(PROP_INSTRUCTION_ADDRESS);
        }

        public Map<String, Object> getProperties() {
            return props;
        }
    }

    public StackTraceProxy(IChannel channel) {
        this.channel = channel;
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

    public IToken getContext(String[] id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                StackTraceContext[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[1]);
                    arr = toContextArray(args[0]);
                }
                done.doneGetContext(token, error, arr);
            }
        }.token;
    }

    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    private StackTraceContext[] toContextArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        int n = 0;
        StackTraceContext[] ctx = new StackTraceContext[c.size()];
        for (Map<String,Object> m : c) ctx[n++] = new Context(m);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
}
