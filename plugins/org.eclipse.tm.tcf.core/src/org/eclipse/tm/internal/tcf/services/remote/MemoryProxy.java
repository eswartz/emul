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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemory;


public class MemoryProxy implements IMemory {

    private final IChannel channel;
    private final Map<MemoryListener,IChannel.IEventListener> listeners =
        new HashMap<MemoryListener,IChannel.IEventListener>();
    
    private static class Range implements Comparable<Range> {
        int offs;
        int size;
        int stat;
        String msg;
        
        public int compareTo(Range o) {
            if (offs < o.offs) return -1;
            if (offs > o.offs) return +1;
            return 0;
        }
    }
    
    private class MemoryErrorReport extends MemoryError implements ErrorOffset, IErrorReport {
        
        private static final long serialVersionUID = 796525409870265390L;
        private final Map<String,Object> attrs;
        private final Range[] ranges;
        
        @SuppressWarnings("unchecked")
        MemoryErrorReport(String msg, Map<String,Object> attrs, Number addr, Object ranges) {
            super(msg);
            this.attrs = attrs;
            Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)ranges;
            this.ranges = c == null ? null : new Range[c.size()];
            if (c != null) {
                int n = 0;
                BigInteger addr_bi = addr instanceof BigInteger ?
                        (BigInteger)addr : new BigInteger(addr.toString());
                for (Map<String,Object> m : c) {
                    Range r = new Range();
                    Number x = (Number)m.get("addr");
                    BigInteger y = x instanceof BigInteger ?
                            (BigInteger)x : new BigInteger(x.toString());
                    r.offs = addr_bi.subtract(y).intValue();
                    r.size = ((Number)m.get("size")).intValue();
                    r.stat = ((Number)m.get("stat")).intValue();
                    r.msg = Command.toErrorString(m.get("msg"));
                    assert r.offs >= 0;
                    assert r.size >= 0;
                    this.ranges[n++] = r;
                }
                Arrays.sort(this.ranges);
            }
        }

        public int getErrorCode() {
            Number n = (Number)attrs.get(ERROR_CODE);
            if (n == null) return 0;
            return n.intValue();
        }

        public int getAltCode() {
            Number n = (Number)attrs.get(ERROR_ALT_CODE);
            if (n == null) return 0;
            return n.intValue();
        }

        public String getAltOrg() {
            return (String)attrs.get(ERROR_ALT_ORG);
        }

        public Map<String, Object> getAttributes() {
            return attrs;
        }

        public String getMessage(int offset) {
            if (ranges == null) return null;
            int l = 0;
            int h = ranges.length - 1;
            while (l <= h) {
                int n = (l + h) / 2;
                Range r = ranges[n];
                if (r.offs > offset) {
                    h = n - 1;
                }
                else if (offset >= r.offs + r.size) {
                    l = n + 1;
                }
                else {
                    return r.msg;
                }
            }
            return null;
        }

        public int getStatus(int offset) {
            if (ranges == null) return BYTE_UNKNOWN;
            int l = 0;
            int h = ranges.length - 1;
            while (l <= h) {
                int n = (l + h) / 2;
                Range r = ranges[n];
                if (r.offs > offset) {
                    h = n - 1;
                }
                else if (offset >= r.offs + r.size) {
                    l = n + 1;
                }
                else {
                    return r.stat;
                }
            }
            return BYTE_UNKNOWN;
        }
    }

    private class MemContext implements MemoryContext {

        private final Map<String,Object> props;

        MemContext(Map<String,Object> props) {
            this.props = props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getParentID() {
            return (String)props.get(PROP_PARENT_ID);
        }

        public int getAddressSize() {
            Number n = (Number)props.get(PROP_ADDRESS_SIZE);
            if (n == null) return 0;
            return n.intValue();
        }

        public String getProcessID() {
            return (String)props.get(PROP_PROCESS_ID);
        }

        public boolean isBigEndian() {
            Boolean n = (Boolean)props.get(PROP_BIG_ENDIAN);
            if (n == null) return false;
            return n.booleanValue();
        }

        @SuppressWarnings("unchecked")
        public Collection<String> getAccessTypes() {
            return (Collection<String>)props.get(PROP_ACCESS_TYPES);
        }

        public Number getEndBound() {
            return (Number)props.get(PROP_END_BOUND);
        }

        public String getName() {
            return (String)props.get(PROP_NAME);
        }

        public Number getStartBound() {
            return (Number)props.get(PROP_START_BOUND);
        }

        public Map<String, Object> getProperties() {
            return props;
        }

        public IToken fill(final Number addr, int word_size,
                byte[] value, int size, int mode, final DoneMemory done) {
            return new MemoryCommand("fill", new Object[] {
                getID(), addr, word_size, size, mode, value
            } ) {
                public void done(Exception error, Object[] args) {
                    MemoryError e = null;
                    if (error != null) {
                        e = new MemoryError(error.getMessage());
                    }
                    else {
                        assert args.length == 2;
                        e = toMemoryError(addr, args[0], args[1]);
                    }
                    done.doneMemory(token, e);
                }
            }.token;
        }

        public IToken get(final Number addr, int word_size,
                final byte[] buf, final int offs, final int size,
                int mode, final DoneMemory done) {
            return new MemoryCommand("get", new Object[] {
                    getID(), addr, word_size, size, mode
                } ) {
                    public void done(Exception error, Object[] args) {
                        MemoryError e = null;
                        if (error != null) {
                            e = new MemoryError(error.getMessage());
                        }
                        else {
                            assert args.length == 3;
                            JSON.toByteArray(buf, offs, size, args[0]);
                            e = toMemoryError(addr, args[1], args[2]);
                        }
                        done.doneMemory(token, e);
                    }
                }.token;
        }

        public IToken set(final Number addr, int word_size,
                byte[] buf, int offs, int size, int mode, final DoneMemory done) {
            return new MemoryCommand("set", new Object[] {
                    getID(), addr, word_size, size, mode, new JSON.Binary(buf, offs, size)
                } ) {
                    public void done(Exception error, Object[] args) {
                        MemoryError e = null;
                        if (error != null) {
                            e = new MemoryError(error.getMessage());
                        }
                        else {
                            assert args.length == 2;
                            e = toMemoryError(addr, args[0], args[1]);
                        }
                        done.doneMemory(token, e);
                    }
                }.token;
        }
        
        public String toString() {
            return "[Memory Context " + props.toString() + "]";
        }
    }

    public MemoryProxy(IChannel channel) {
        this.channel = channel;
    }

    public void addListener(final MemoryListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("contextAdded")) {
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
                    else if (name.equals("memoryChanged")) {
                        assert args.length == 2;
                        listener.memoryChanged((String)args[0],
                                toAddrArray(args[1]), toSizeArray(args[1]));
                    }
                    else {
                        throw new IOException("Memory service: unknown event: " + name);
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

    public void removeListener(MemoryListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }

    public IToken getContext(String context_id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[]{ context_id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                MemContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new MemContext((Map<String,Object>)args[1]);
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
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

    public String getName() {
        return NAME;
    }
    
    private abstract class MemoryCommand extends Command {
        
        MemoryCommand(String cmd, Object[] args) {
            super(channel, MemoryProxy.this, cmd, args);
        }

        @SuppressWarnings("unchecked")
        MemoryError toMemoryError(Number addr, Object data, Object ranges) {
            if (data == null) return null;
            Map<String,Object> map = (Map<String,Object>)data;
            Integer code = (Integer)map.get(IErrorReport.ERROR_CODE);
            String cmd = getCommandString();
            if (cmd.length() > 72) cmd = cmd.substring(0, 72) + "...";
            MemoryError e = new MemoryErrorReport(
                    "TCF command exception:" +
                    "\nCommand: " + cmd +
                    "\nException: " + toErrorString(data) +
                    "\nError code: " + code,
                    map, addr, ranges);
            Object caused_by = map.get(IErrorReport.ERROR_CAUSED_BY);
            if (caused_by != null) e.initCause(toError(caused_by, false));
            return e;
        }
    }

    @SuppressWarnings("unchecked")
    private MemoryContext[] toContextArray(Object o) {
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        if (c == null) return new MemoryContext[0];
        int n = 0;
        MemoryContext[] ctx = new MemoryContext[c.size()];
        for (Iterator<Map<String,Object>> i = c.iterator(); i.hasNext();) {
            ctx[n++] = new MemContext(i.next());
        }
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private long[] toSizeArray(Object o) {
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        if (c == null) return null;
        long[] a = new long[c.size()];
        int n = 0;
        for (Map<String,Object> m : c) {
            Number sz = (Number)m.get("size");
            a[n++] = sz == null ? 0 : sz.longValue();
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private Number[] toAddrArray(Object o) {
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        if (c == null) return null;
        Number[] a = new Number[c.size()];
        int n = 0;
        for (Map<String,Object> m : c) {
            a[n++] = (Number)m.get("addr");
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
}
