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

import java.util.Collection;
import java.util.Map;

import com.windriver.tcf.api.core.Command;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IDiagnostics;

public class DiagnosticsProxy implements IDiagnostics {
    
    private final IChannel channel;
    
    private static class Symbol implements ISymbol {
        
        private final Map<String,Object> props;
        
        Symbol(Map<String,Object> props) {
            this.props = props;
        }

        public String getSectionName() {
            return (String)props.get("Section");
        }

        public Number getValue() {
            return (Number)props.get("Value");
        }

        public boolean isAbs() {
            Boolean b = (Boolean)props.get("Abs");
            return b != null && b.booleanValue();
        }

        public boolean isCommon() {
            String s = (String)props.get("Storage");
            return s != null && s.equals("COMMON");
        }

        public boolean isGlobal() {
            String s = (String)props.get("Storage");
            return s != null && s.equals("GLOBAL");
        }

        public boolean isLocal() {
            String s = (String)props.get("Storage");
            return s != null && s.equals("LOCAL");
        }

        public boolean isUndef() {
            String s = (String)props.get("Storage");
            return s != null && s.equals("UNDEF");
        }
    }
    
    public DiagnosticsProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken echo(String s, final DoneEcho done) {
        return new Command(channel, this, "echo", new Object[]{ s }) {
            @Override
            public void done(Exception error, Object[] args) {
                String str = null;
                if (error == null) {
                    assert args.length == 1;
                    str = (String)args[0];
                }
                done.doneEcho(token, error, str);
            }
        }.token;
    }

    public IToken getTestList(final DoneGetTestList done) {
        return new Command(channel, this, "getTestList", null) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    arr = toStringArray((Collection<String>)args[2]);
                }
                done.doneGetTestList(token, error, arr);
            }
        }.token;
    }

    public IToken runTest(String s, final DoneRunTest done) {
        return new Command(channel, this, "runTest", new Object[]{ s }) {
            @Override
            public void done(Exception error, Object[] args) {
                String str = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    str = (String)args[2];
                }
                done.doneRunTest(token, error, str);
            }
        }.token;
    }

    public IToken cancelTest(String s, final DoneCancelTest done) {
        return new Command(channel, this, "cancelTest", new Object[]{ s }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0], args[1]);
                }
                done.doneCancelTest(token, error);
            }
        }.token;
    }

    public IToken getSymbol(String context_id, String symbol_name, final DoneGetSymbol done) {
        return new Command(channel, this, "getSymbol", new Object[]{ context_id, symbol_name }) {
            @Override
            public void done(Exception error, Object[] args) {
                ISymbol sym = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0], args[1]);
                    sym = toSymbol(args[2]);
                }
                done.doneGetSymbol(token, error, sym);
            }
        }.token;
    }

    private String[] toStringArray(Collection<String> c) {
        if (c == null) return new String[0];
        return (String[])c.toArray(new String[c.size()]);
    }
    
    @SuppressWarnings("unchecked")
    private ISymbol toSymbol(Object o) {
        if (o == null) return null;
        return new Symbol((Map<String,Object>)o);
    }
}
