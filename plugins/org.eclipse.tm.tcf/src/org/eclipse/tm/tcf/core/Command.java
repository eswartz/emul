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
package org.eclipse.tm.tcf.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.Token;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;


/**
 * This is utility class that helps to implement sending a command and receiving
 * command result over TCF communication channel. The class uses JSON to encode
 * command arguments and to decode result data.
 * 
 * The class also provides support for TCF standard error report encoding.
 * 
 * Clients are expected to subclass <code>Command</code> and override <code>done</code> method.
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
 *                  assert args.length == 2;
 *                  error = toError(args[0]);
 *                  if (args[1] != null) ctx = new Context(args[1]);
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
    
    private static final SimpleDateFormat timestamp_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private class ErrorReport extends Exception implements IErrorReport {
        
        private static final long serialVersionUID = 3687543884858739977L;
        private final Map<String,Object> attrs;
        
        ErrorReport(String msg, Map<String,Object> attrs) {
            super(msg);
            this.attrs = attrs;
            Object caused_by = attrs.get(IErrorReport.ERROR_CAUSED_BY);
            if (caused_by != null) initCause(toError(caused_by, false));
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
    }
    
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
    
    @SuppressWarnings({ "unchecked" })
    public static String toErrorString(Object data) {
        if (data == null) return null;
        Map<String,Object> map = (Map<String,Object>)data;
        String fmt = (String)map.get(IErrorReport.ERROR_FORMAT);
        if (fmt != null) {
            Collection<Object> c = (Collection<Object>)map.get(IErrorReport.ERROR_PARAMS);
            if (c != null) return new MessageFormat(fmt).format(c.toArray());
            return fmt;
        }
        Number code = (Number)map.get(IErrorReport.ERROR_CODE);
        if (code != null) {
            if (code.intValue() == IErrorReport.TCF_ERROR_OTHER) {
                String alt_org = (String)map.get(IErrorReport.ERROR_ALT_ORG);
                Number alt_code = (Number)map.get(IErrorReport.ERROR_ALT_CODE);
                if (alt_org != null && alt_code != null) {
                    return alt_org + " Error " + alt_code;
                }
            }
            return "TCF Error " + code;
        }
        return "Invalid error report format";
    }
    
    private static void appendErrorProps(StringBuffer bf, Map<String,Object> map) {
        Number time = (Number)map.get(IErrorReport.ERROR_TIME);
        Number code = (Number)map.get(IErrorReport.ERROR_CODE);
        String service = (String)map.get(IErrorReport.ERROR_SERVICE);
        Number severity = (Number)map.get(IErrorReport.ERROR_SEVERITY);
        Number alt_code = (Number)map.get(IErrorReport.ERROR_ALT_CODE);
        String alt_org = (String)map.get(IErrorReport.ERROR_ALT_ORG);
        if (time != null) {
            bf.append('\n');
            bf.append("Time: ");
            bf.append(timestamp_format.format(new Date(time.longValue())));
        }
        if (severity != null) {
            bf.append('\n');
            bf.append("Severity: ");
            bf.append(toErrorString(map));
            switch (severity.intValue()) {
            case IErrorReport.SEVERITY_ERROR: bf.append("Error");
            case IErrorReport.SEVERITY_FATAL: bf.append("Fatal");
            case IErrorReport.SEVERITY_WARNING: bf.append("Warning");
            default: bf.append("Unknown");
            }
        }
        bf.append('\n');
        bf.append("Error text: ");
        bf.append(toErrorString(map));
        bf.append('\n');
        bf.append("Error code: ");
        bf.append(code);
        if (service != null) {
            bf.append('\n');
            bf.append("Service: ");
            bf.append(service);
        }
        if (alt_code != null) {
            bf.append('\n');
            bf.append("Alt code: ");
            bf.append(alt_code);
            if (alt_org != null) {
                bf.append('\n');
                bf.append("Alt org: ");
                bf.append(alt_org);
            }
        }
    }
    
    public Exception toError(Object data) {
        return toError(data, true);
    }
    
    @SuppressWarnings("unchecked")
    public Exception toError(Object data, boolean include_command_text) {
        if (data == null) return null;
        Map<String,Object> map = (Map<String,Object>)data;
        StringBuffer bf = new StringBuffer();
        bf.append("TCF error report:");
        bf.append('\n');
        if (include_command_text) {
            String cmd = getCommandString();
            if (cmd.length() > 72) cmd = cmd.substring(0, 72) + "...";
            bf.append("Command: ");
            bf.append(cmd);
        }
        appendErrorProps(bf, map);
        return new ErrorReport(bf.toString(), map);
    }
}
