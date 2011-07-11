/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel - initial API and implementation
 *******************************************************************************/

package org.eclipse.tm.internal.tcf.services.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.ITerminals;

public class TerminalsProxy implements ITerminals {

    private final IChannel channel;

    private final Map<TerminalsListener, IChannel.IEventListener> listeners =
        new HashMap<TerminalsListener, IChannel.IEventListener>();

    private class TerminalContext implements ITerminals.TerminalContext {

        private final Map<String, Object> props;

        TerminalContext(Map<String, Object> props) {
            this.props = props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getProcessID() {
            return (String)props.get(PROP_PROCESS_ID);
        }

        public String getPtyType() {
            return (String)props.get(PROP_PTY_TYPE);
        }

        public String getEncoding() {
            return (String)props.get(PROP_ENCODING);
        }

        public int getWidth() {
            return ((Integer)props.get(PROP_WIDTH)).intValue();
        }

        public int getHeight() {
            return ((Integer)props.get(PROP_HEIGHT)).intValue();
        }

        public String getStdInID() {
            return (String)props.get(PROP_STDIN_ID);
        }

        public String getStdOutID() {
            return (String)props.get(PROP_STDOUT_ID);
        }

        public String getStdErrID() {
            return (String)props.get(PROP_STDERR_ID);
        }

        public IToken exit(final DoneCommand done) {
            return new Command(channel, TerminalsProxy.this, "exit", new Object[] { getID() }) {
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
            return "[Terminals Context " + props.toString() + "]";
        }
    }

    public TerminalsProxy(IChannel channel) {
        this.channel = channel;
    }

    /**
     * Return service name, as it appears on the wire - a TCF name of the
     * service.
     */
    public String getName() {
        return NAME;
    }

    public IToken getContext(String id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[] { id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                TerminalContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null)
                        ctx = new TerminalContext((Map<String, Object>) args[1]);
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public IToken launch(String type, String encoding, String[] environment, final DoneLaunch done) {
        return new Command(channel, this, "launch", new Object[] { type, encoding, environment }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                TerminalContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null)
                        ctx = new TerminalContext((Map<String, Object>) args[1]);
                }
                done.doneLaunch(token, error, ctx);
            }
        }.token;
    }

    public IToken setWinSize(String context_id, int newWidth, int newHeight, final DoneCommand done) {
        return new Command(channel, this, "setWinSize", new Object[] { context_id, newWidth, newHeight }) {
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

    public IToken exit(String context_id, final DoneCommand done) {
        return new Command(channel, this, "exit", new Object[] { context_id }) {
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

    public void addListener(final TerminalsListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {
            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("exited")) {
                        assert args.length == 2;
                        listener.exited((String) args[0], ((Number) args[1]).intValue());
                    }
                    else if (name.equals("winSizeChanged")) {
                        assert args.length == 3;
                        listener.winSizeChanged((String) args[0],
                                ((Number) args[1]).intValue(),
                                ((Number) args[2]).intValue());
                    }
                    else {
                        throw new IOException("Terminals service: unknown event: " + name);
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

    public void removeListener(TerminalsListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }
}
