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
package com.windriver.tcf.api.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import com.windriver.tcf.api.internal.core.Token;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IService;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.JSON;
import com.windriver.tcf.api.protocol.Protocol;

/**
 * This is utility class that helps to implement sending a command and receiving
 * command result over TCF communication channel. The class uses JSON to encode
 * command arguments and to decode result data. Clients are expected to subclass
 * <code>Command</code> and override <code>done</code> method.
 * 
 * Note: most clients don't need to handle protocol commands directly and
 * can use service APIs instead. Service API does all command encoding/decoding
 * for a client.
 * 
 * Typical usage example:
 * 
 *  public IToken getContext(String id, final DoneGetContext done) {
 *      return new Command(channel, IService.this, "getContext", new Object[]{ id }) {
 *          @Override
 *          public void done(Exception error, Object[] args) {
 *              Context ctx = null;
 *              if (error == null) {
 *                  assert args.length == 3;
 *                  error = JSON.toError(args[0], args[1]);
 *                  if (args[2] != null) ctx = new Context(args[2]);
 *              }
 *              done.doneGetContext(token, error, ctx);
 *          }
 *      }.token;
 *  }
 */
public abstract class Command implements IChannel.ICommandListener {
    
    private final IService service;
    private final String command;
    private final Object[] args;
    
    public final IToken token;
    
    private boolean done;
    
    public Command(IChannel channel, IService service, String command, Object[] args) {
        this.service = service;
        this.command = command;
        this.args = args;
        IToken t = null;
        try {
            t = channel.sendCommand(service, command, JSON.toJSONSequence(args), this);
        }
        catch (Throwable y) {
            t = new Token();
            final Exception x = y instanceof Exception ? (Exception)y : new Exception(y);
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    assert !done;
                    done = true;
                    done(x, null);
                }
            });
        }
        token = t;
    }

    public void progress(IToken token, byte[] data) {
        assert this.token == token;
    }

    public void result(IToken token, byte[] data) {
        assert this.token == token;
        Exception error = null;
        Object[] args = null;
        try {
            args = JSON.parseSequence(data);
        }
        catch (Exception e) {
            error = e;
        }
        assert !done;
        done = true;
        done(error, args);
    }

    public void terminated(IToken token, Exception error) {
        assert this.token == token;
        assert !done;
        done = true;
        done(error, null);
    }
    
    public abstract void done(Exception error, Object[] args);
    
    public String getCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(service.getName());
        buf.append(' ');
        buf.append(command);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                buf.append(i == 0 ? " " : ", ");
                try {
                    buf.append(JSON.toJSON(args[i]));
                }
                catch (IOException x) {
                    buf.append("***");
                    buf.append(x.getMessage());
                    buf.append("***");
                }
            }
        }
        return buf.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static String toErrorString(Object data) {
        if (data instanceof String) {
            return (String)data;
        }
        else if (data != null) {
            Map<String,Object> map = (Map<String,Object>)data;
            Collection<Object> c = (Collection<Object>)map.get("params");
            return new MessageFormat((String)map.get("format")).format(c.toArray());
        }
        return null;
    }
    
    public Exception toError(Object code, Object data) {
        int error_code = ((Number)code).intValue();
        if (error_code == 0) return null;
        String cmd = getCommandString();
        if (cmd.length() > 72) cmd = cmd.substring(0, 72) + "...";
        return new Exception(
                "TCF command exception:" +
                "\nCommand: " + cmd +
                "\nException: " + toErrorString(data) +
                "\nError code: " + code);
    }
}
