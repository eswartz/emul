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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IDiagnostics;


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

    public IToken echoFP(BigDecimal n, final DoneEchoFP done) {
        return new Command(channel, this, "echoFP", new Object[]{ n }) {
            @Override
            public void done(Exception error, Object[] args) {
                BigDecimal n = null;
                if (error == null) {
                    assert args.length == 1;
                    Number x = (Number)args[0];
                    if (x instanceof BigDecimal) {
                        n = (BigDecimal)x;
                    }
                    else {
                        n = new BigDecimal(x.toString());
                    }
                }
                done.doneEchoFP(token, error, n);
            }
        }.token;
    }

    public IToken getTestList(final DoneGetTestList done) {
        return new Command(channel, this, "getTestList", null) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toStringArray(args[1]);
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
                    assert args.length == 2;
                    error = toError(args[0]);
                    str = (String)args[1];
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
                    assert args.length == 1;
                    error = toError(args[0]);
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
                    assert args.length == 2;
                    error = toError(args[0]);
                    sym = toSymbol(args[1]);
                }
                done.doneGetSymbol(token, error, sym);
            }
        }.token;
    }

    public IToken createTestStreams(int inp_buf_size, int out_buf_size, final DoneCreateTestStreams done) {
        return new Command(channel, this, "createTestStreams", new Object[]{ inp_buf_size, out_buf_size }) {
            @Override
            public void done(Exception error, Object[] args) {
                String inp_id = null;
                String out_id = null;
                if (error == null) {
                    assert args.length == 3;
                    error = toError(args[0]);
                    inp_id = (String)args[1];
                    out_id = (String)args[2];
                }
                done.doneCreateTestStreams(token, error, inp_id, out_id);
            }
        }.token;
    }

    public IToken disposeTestStream(String id, final DoneDisposeTestStream done) {
        return new Command(channel, this, "disposeTestStream", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneDisposeTestStream(token, error);
            }
        }.token;
    }

    public IToken not_implemented_command(final DoneNotImplementedCommand done) {
        return new Command(channel, this, "not implemented command", null) {
            @Override
            public void done(Exception error, Object[] args) {
                done.doneNotImplementedCommand(token, error);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
    
    @SuppressWarnings("unchecked")
    private ISymbol toSymbol(Object o) {
        if (o == null) return null;
        return new Symbol((Map<String,Object>)o);
    }
}
