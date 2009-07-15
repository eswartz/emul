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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.ServiceManager;
import org.eclipse.tm.internal.tcf.core.Token;
import org.eclipse.tm.internal.tcf.core.TransportManager;
import org.eclipse.tm.internal.tcf.services.local.LocatorService;
import org.eclipse.tm.internal.tcf.services.remote.GenericProxy;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

/**
 * Abstract implementation of IChannel interface.
 *  
 * AbstractChannel implements communication link connecting two end points (peers).
 * The channel asynchronously transmits messages: commands, results and events.
 * 
 * Clients can subclass AbstractChannel to support particular transport (wire) protocol.
 * Also, see StreamChannel for stream oriented transport protocols.
 */
public abstract class AbstractChannel implements IChannel {

    public interface TraceListener {
        
        public void onMessageReceived(char type, String token,
                String service, String name, byte[] data);
        
        public void onMessageSent(char type, String token,
                String service, String name, byte[] data);
        
        public void onChannelClosed(Throwable error);
    }
    
    public interface Proxy {
        
        public void onCommand(IToken token, String service, String name, byte[] data);
        
        public void onEvent(String service, String name, byte[] data);

        public void onChannelClosed(Throwable error);
    }

    private static class Message {
        final char type;
        Token token;
        String service;
        String name;
        byte[] data;

        boolean is_sent;
        boolean is_canceled;

        Collection<TraceListener> trace;
        
        Message(char type) {
            this.type = type;
        }

        @Override
        public String toString() {
            try {
                StringBuffer bf = new StringBuffer();
                bf.append('[');;
                bf.append(type);
                if (token != null) {
                    bf.append(' ');
                    bf.append(token.getID());
                }
                if (service != null) {
                    bf.append(' ');
                    bf.append(service);
                }
                if (name != null) {
                    bf.append(' ');
                    bf.append(name);
                }
                if (data != null) {
                    int i = 0;
                    while (i < data.length) {
                        int j = i;
                        while (j < data.length && data[j] != 0) j++;
                        bf.append(' ');
                        bf.append(new String(data, i, j - i, "UTF8"));
                        if (j < data.length && data[j] == 0) j++;
                        i = j;
                    }
                }
                bf.append(']');
                return bf.toString();
            }
            catch (Exception x) {
                return x.toString();
            }
        }
    }

    private static IChannelListener[] listeners_array = new IChannelListener[4];

    private final LinkedList<String> redirect_queue = new LinkedList<String>();
    private final Map<Class<?>,IService> local_service_by_class = new HashMap<Class<?>,IService>();
    private final Map<Class<?>,IService> remote_service_by_class = new HashMap<Class<?>,IService>();
    private final Map<String,IService> local_service_by_name = new HashMap<String,IService>();
    private final Map<String,IService> remote_service_by_name = new HashMap<String,IService>();
    private final LinkedList<Message> out_queue = new LinkedList<Message>();
    private final Collection<IChannelListener> channel_listeners = new ArrayList<IChannelListener>();
    private final Map<String,IChannel.IEventListener[]> event_listeners = new HashMap<String,IChannel.IEventListener[]>();
    private final Map<String,IChannel.ICommandServer> command_servers = new HashMap<String,IChannel.ICommandServer>();
    private final Map<String,Message> out_tokens = new HashMap<String,Message>();
    private final Thread inp_thread;
    private final Thread out_thread;
    private boolean notifying_channel_opened;
    private boolean registered_with_trasport;
    private boolean shutdown;
    private int state = STATE_OPENNING;
    private IToken redirect_command;
    private final IPeer local_peer;
    private IPeer remote_peer;
    private Proxy proxy;
    private boolean zero_copy;

    private static final int pending_command_limit = 32;
    private int local_congestion_level = -100;
    private int remote_congestion_level = -100;
    private long local_congestion_time;
    private int local_congestion_cnt;
    private Collection<TraceListener> trace_listeners;
    
    public static final int
        EOS = -1, // End Of Stream
        EOM = -2; // End Of Message
    
    protected AbstractChannel(IPeer remote_peer) {
        this(LocatorService.getLocalPeer(), remote_peer);
    }

    protected AbstractChannel(IPeer local_peer, IPeer remote_peer) {
        assert Protocol.isDispatchThread();
        this.remote_peer = remote_peer;
        this.local_peer = local_peer;

        inp_thread = new Thread() {

            final byte[] empty_byte_array = new byte[0];
            byte[] buf = new byte[1024];
            byte[] eos;

            private void error() throws IOException {
                throw new IOException("Protocol syntax error");
            }

            private byte[] readBytes(int end) throws IOException {
                int len = 0;
                for (;;) {
                    int n = read();
                    if (n <= 0) {
                        if (n == end) break;
                        if (n == EOM) throw new IOException("Unexpected end of message");
                        if (n < 0) throw new IOException("Communication channel is closed by remote peer");
                    }
                    if (len >= buf.length) {
                        byte[] tmp = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, tmp, 0, len);
                        buf = tmp;
                    }
                    buf[len++] = (byte)n;
                }
                if (len == 0) return empty_byte_array;
                byte[] res = new byte[len];
                System.arraycopy(buf, 0, res, 0, len);
                return res;
            }

            private String readString() throws IOException {
                int len = 0;
                for (;;) {
                    int n = read();
                    if (n <= 0) {
                        if (n == 0) break;
                        if (n == EOM) throw new IOException("Unexpected end of message");
                        if (n < 0) throw new IOException("Communication channel is closed by remote peer");
                    }
                    if (len >= buf.length) {
                        byte[] tmp = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, tmp, 0, len);
                        buf = tmp;
                    }
                    buf[len++] = (byte)n;
                }
                return new String(buf, 0, len, "UTF8");
            }

            @Override
            public void run() {
                try {
                    while (true) {
                        int n = read();
                        if (n == EOM) continue;
                        if (n == EOS) {
                            eos = readBytes(EOM);
                            break;
                        }
                        final Message msg = new Message((char)n);
                        if (read() != 0) error();
                        switch (msg.type) {
                        case 'C':
                            msg.token = new Token(readBytes(0));
                            msg.service = readString();
                            msg.name = readString();
                            msg.data = readBytes(EOM);
                            break;
                        case 'P':
                        case 'R':
                        case 'N':
                            msg.token = new Token(readBytes(0));
                            msg.data = readBytes(EOM);
                            break;
                        case 'E':
                            msg.service = readString();
                            msg.name = readString();
                            msg.data = readBytes(EOM);
                            break;
                        case 'F':
                            msg.data = readBytes(EOM);
                            break;
                        default:
                            error();
                        }
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                handleInput(msg);
                            }
                        });
                        int delay = local_congestion_level;
                        if (delay > 0) sleep(delay);
                    }
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            if (out_tokens.isEmpty()) {
                                close();
                            }
                            else {
                                IOException x = new IOException("Connection reset by peer");
                                try {
                                    Object[] args = JSON.parseSequence(eos);
                                    if (args.length > 0 && args[0] != null) {
                                        x = new IOException(Command.toErrorString(args[0]));
                                    }
                                }
                                catch (IOException e) {
                                    x = e;
                                }
                                terminate(x);
                            }
                        }
                    });
                }
                catch (final Throwable x) {
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            terminate(x);
                        }
                    });
                }
            }
        };

        out_thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        Message msg = null;
                        boolean last = false;
                        synchronized (out_queue) {
                            while (out_queue.isEmpty()) out_queue.wait();
                            msg = out_queue.removeFirst();
                            if (msg == null) break;
                            last = out_queue.isEmpty();
                            if (msg.is_canceled) {
                                if (last) flush();
                                continue;
                            }
                            msg.is_sent = true;
                        }
                        if (msg.trace != null) {
                            final Message m = msg;
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    for (TraceListener l : m.trace) {
                                        try {
                                            l.onMessageSent(m.type, m.token == null ? null : m.token.getID(),
                                                    m.service, m.name, m.data);
                                        }
                                        catch (Throwable x) {
                                            Protocol.log("Exception in channel listener", x);
                                        }
                                    }
                                }
                            });
                        }
                        write(msg.type);
                        write(0);
                        if (msg.token != null) {
                            write(msg.token.getBytes());
                            write(0);
                        }
                        if (msg.service != null) {
                            write(msg.service.getBytes("UTF8"));
                            write(0);
                        }
                        if (msg.name != null) {
                            write(msg.name.getBytes("UTF8"));
                            write(0);
                        }
                        if (msg.data != null) {
                            write(msg.data);
                        }
                        write(EOM);
                        int delay = 0;
                        int level = remote_congestion_level;
                        if (level > 0) delay = level * 10;
                        if (last || delay > 0) flush();
                        if (delay > 0) sleep(delay);
                        else yield();
                    }
                    write(EOS);
                    write(EOM);
                    flush();
                }
                catch (final Throwable x) {
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            terminate(x);
                        }
                    });
                }
            }
        };
        inp_thread.setName("TCF Channel Receiver");
        out_thread.setName("TCF Channel Transmitter");
    }

    protected void start() {
        assert Protocol.isDispatchThread();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (proxy != null) return;
                    if (state == STATE_CLOSED) return;
                    ServiceManager.onChannelCreated(AbstractChannel.this, local_service_by_name);
                    makeServiceByClassMap(local_service_by_name, local_service_by_class);
                    Object[] args = new Object[]{ local_service_by_name.keySet() };  
                    sendEvent(Protocol.getLocator(), "Hello", JSON.toJSONSequence(args));
                }
                catch (IOException x) {
                    terminate(x);
                }
            }
        });
        inp_thread.start();
        out_thread.start();
    }

    /**
     * Redirect this channel to given peer using this channel remote peer locator service as a proxy.
     * @param peer_id - peer that will become new remote communication endpoint of this channel
     */
    public void redirect(final String peer_id) {
        assert Protocol.isDispatchThread();
        if (state == STATE_OPENNING) {
            redirect_queue.add(peer_id);
        }
        else {
            assert state == STATE_OPEN;
            assert redirect_command == null;
            try {
                final ILocator l = (ILocator)remote_service_by_class.get(ILocator.class);
                if (l == null) throw new IOException("Cannot redirect channel: peer " +
                        remote_peer.getID() + " has no locator service");
                final IPeer peer = l.getPeers().get(peer_id);
                if (peer == null) {
                    // Peer not found, must wait for a while until peer is discovered or time out
                    final boolean[] found = new boolean[1];
                    Protocol.invokeLater(ILocator.DATA_RETENTION_PERIOD / 3, new Runnable() {
                        public void run() {
                            if (found[0]) return;
                            terminate(new Exception("Peer " + peer_id + " not found"));
                        }
                    });
                    l.addListener(new ILocator.LocatorListener() {
                        public void peerAdded(IPeer peer) {
                            if (peer.getID().equals(peer_id)) {
                                found[0] = true;
                                state = STATE_OPEN;
                                l.removeListener(this);
                                redirect(peer_id);
                            }
                        }
                        public void peerChanged(IPeer peer) {
                        }

                        public void peerHeartBeat(String id) {
                        }

                        public void peerRemoved(String id) {
                        }
                    });
                }
                else {
                    redirect_command = l.redirect(peer_id, new ILocator.DoneRedirect() {
                        public void doneRedirect(IToken token, Exception x) {
                            assert redirect_command == token;
                            redirect_command = null;
                            if (state != STATE_OPENNING) return;
                            if (x != null) terminate(x);
                            remote_peer = peer;
                            remote_service_by_class.clear();
                            remote_service_by_name.clear();
                            event_listeners.clear();
                        }
                    });
                }
                state = STATE_OPENNING;
            }
            catch (Throwable x) {
                terminate(x);
            }
        }
    }

    private void makeServiceByClassMap(Map<String,IService> by_name, Map<Class<?>,IService> by_class) {
        for (IService service : by_name.values()) {
            for (Class<?> fs : service.getClass().getInterfaces()) {
                if (fs.equals(IService.class)) continue;
                if (!IService.class.isAssignableFrom(fs)) continue;
                by_class.put(fs, service);
            }
        }
    }

    public final int getState() {
        return state;
    }

    public void addChannelListener(IChannelListener listener) {
        assert Protocol.isDispatchThread();
        assert listener != null;
        channel_listeners.add(listener);
    }

    public void removeChannelListener(IChannelListener listener) {
        assert Protocol.isDispatchThread();
        channel_listeners.remove(listener);
    }
    
    public void addTraceListener(TraceListener listener) {
        if (trace_listeners == null) {
            trace_listeners = new ArrayList<TraceListener>();
        }
        else {
            trace_listeners = new ArrayList<TraceListener>(trace_listeners);
        }
        trace_listeners.add(listener);
    }
    
    public void removeTraceListener(TraceListener listener) {
        trace_listeners = new ArrayList<TraceListener>(trace_listeners);
        trace_listeners.remove(listener);
        if (trace_listeners.isEmpty()) trace_listeners = null;
    }

    public void addEventListener(IService service, IChannel.IEventListener listener) {
        assert Protocol.isDispatchThread();
        IChannel.IEventListener[] list = event_listeners.get(service.getName());
        IChannel.IEventListener[] next = new IChannel.IEventListener[list == null ? 1 : list.length + 1];
        if (list != null) System.arraycopy(list, 0, next, 0, list.length);
        next[next.length - 1] = listener;
        event_listeners.put(service.getName(), next);
    }

    public void removeEventListener(IService service, IChannel.IEventListener listener) {
        assert Protocol.isDispatchThread();
        IChannel.IEventListener[] list = event_listeners.get(service.getName());
        for (int i = 0; i < list.length; i++) {
            if (list[i] == listener) {
                if (list.length == 1) {
                    event_listeners.remove(service.getName());
                }
                else {
                    IChannel.IEventListener[] next = new IChannel.IEventListener[list.length - 1];
                    System.arraycopy(list, 0, next, 0, i);
                    System.arraycopy(list, i + 1, next, i, next.length - i);
                    event_listeners.put(service.getName(), next);
                }
                return;
            }
        }
    }

    public void addCommandServer(IService service, IChannel.ICommandServer listener) {
        assert Protocol.isDispatchThread();
        if (command_servers.put(service.getName(), listener) != null) {
            throw new Error("Only one command server per service is allowed");
        }
    }

    public void removeCommandServer(IService service, IChannel.ICommandServer listener) {
        assert Protocol.isDispatchThread();
        if (command_servers.remove(service.getName()) != listener) {
            throw new Error("Invalid command server");
        }
    }

    private void sendEndOfStream() {
        if (shutdown) return;
        shutdown = true;
        synchronized (out_queue) {
            out_queue.clear();
            out_queue.add(0, null);
            out_queue.notify();
        }
    }
    
    public void close() {
        assert Protocol.isDispatchThread();
        try {
            sendEndOfStream();
            out_thread.join(10000);
            stop();
            inp_thread.join(10000);
            terminate(null);
        }
        catch (Exception x) {
            terminate(x);
        }
    }

    public void terminate(final Throwable error) {
        assert Protocol.isDispatchThread();
        sendEndOfStream();
        if (state == STATE_CLOSED) return;
        state = STATE_CLOSED;
        if (error != null && remote_peer instanceof AbstractPeer) {
            ((AbstractPeer)remote_peer).onChannelTerminated();
        }
        if (registered_with_trasport) {
            registered_with_trasport = false;
            TransportManager.channelClosed(this, error);
        }
        if (proxy != null) {
            try {
                proxy.onChannelClosed(error);
            }
            catch (Throwable x) {
                Protocol.log("Exception in channel listener", x);
            }
        }
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (!out_tokens.isEmpty()) {
                    Exception x = null;
                    if (error instanceof Exception) x = (Exception)error;
                    else if (error != null) x = new Exception(error);
                    else x = new IOException("Channel is closed");
                    for (Message msg : out_tokens.values()) {
                        try {
                            String s = msg.toString();
                            if (s.length() > 72) s = s.substring(0, 72) + "...]";
                            IOException y = new IOException("Command " + s + " aborted");
                            y.initCause(x);
                            msg.token.getListener().terminated(msg.token, y);
                        }
                        catch (Throwable e) {
                            Protocol.log("Exception in command listener", e);
                        }
                    }
                    out_tokens.clear();
                }
                if (channel_listeners.isEmpty()) {
                    Protocol.log("TCF channel terminated", error);
                }
                else {
                    listeners_array = channel_listeners.toArray(listeners_array);
                    for (IChannelListener l : listeners_array) {
                        if (l == null) break;
                        try {
                            l.onChannelClosed(error);
                        }
                        catch (Throwable x) {
                            Protocol.log("Exception in channel listener", x);
                        }
                    }
                }
                if (trace_listeners != null) {
                    for (TraceListener l : trace_listeners) {
                        try {
                            l.onChannelClosed(error);
                        }
                        catch (Throwable x) {
                            Protocol.log("Exception in channel listener", x);
                        }
                    }
                }
            }
        });
    }

    public int getCongestion() {
        assert Protocol.isDispatchThread();
        int level = out_tokens.size() * 100 / pending_command_limit - 100;
        if (remote_congestion_level > level) level = remote_congestion_level;
        if (level > 100) level = 100;
        return level;
    }

    public IPeer getLocalPeer() {
        assert Protocol.isDispatchThread();
        return local_peer;
    }

    public IPeer getRemotePeer() {
        assert Protocol.isDispatchThread();
        return remote_peer;
    }
    
    public Collection<String> getLocalServices() {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return local_service_by_name.keySet();
    }

    public Collection<String> getRemoteServices() {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return remote_service_by_name.keySet();
    }
    
    @SuppressWarnings("unchecked")
    public <V extends IService> V getLocalService(Class<V> cls) {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return (V)local_service_by_class.get(cls);
    }

    @SuppressWarnings("unchecked")
    public <V extends IService> V getRemoteService(Class<V> cls) {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return (V)remote_service_by_class.get(cls);
    }
    
    public <V extends IService> void setServiceProxy(Class<V> service_interface, IService service_proxy) {
        if (!notifying_channel_opened) new Error("setServiceProxe() can be called only from channel open call-back");
        if (!(remote_service_by_name.get(service_proxy.getName()) instanceof GenericProxy)) throw new Error("Proxy already set"); 
        if (remote_service_by_class.get(service_interface) != null) throw new Error("Proxy already set");
        remote_service_by_class.put(service_interface, service_proxy);
        remote_service_by_name.put(service_proxy.getName(), service_proxy);
    }
    
    public IService getLocalService(String service_name) {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return local_service_by_name.get(service_name);
    }

    public IService getRemoteService(String service_name) {
        assert Protocol.isDispatchThread();
        assert state != STATE_OPENNING;
        return remote_service_by_name.get(service_name);
    }
    
    public void setProxy(Proxy proxy, Collection<String> services) throws IOException {
        this.proxy = proxy;
        sendEvent(Protocol.getLocator(), "Hello", JSON.toJSONSequence(new Object[]{ services }));
        local_service_by_class.clear();
        local_service_by_name.clear();
    }
    
    private void addToOutQueue(Message msg) {
        msg.trace = trace_listeners;
        synchronized (out_queue) {
            out_queue.add(msg);
            out_queue.notify();
        }
    }

    public IToken sendCommand(IService service, String name, byte[] args, ICommandListener listener) {
        assert Protocol.isDispatchThread();
        if (state == STATE_OPENNING) throw new Error("Channel is waiting for Hello message");
        if (state == STATE_CLOSED) throw new Error("Channel is closed");
        final Message msg = new Message('C');
        msg.service = service.getName();
        msg.name = name;
        msg.data = args;
        Token token = new Token(listener) {
            @Override
            public boolean cancel() {
                assert Protocol.isDispatchThread();
                if (state != STATE_OPEN) return false;
                synchronized (out_queue) {
                    if (msg.is_sent) return false;
                    msg.is_canceled = true;
                }
                out_tokens.remove(msg.token.getID());
                return true;
            }
        };
        msg.token = token;
        out_tokens.put(token.getID(), msg);
        addToOutQueue(msg);
        return token;
    }

    public void sendProgress(IToken token, byte[] results) {
        assert Protocol.isDispatchThread();
        if (state != STATE_OPEN) throw new Error("Channel is closed");
        Message msg = new Message('P');
        msg.data = results;
        msg.token = (Token)token;
        addToOutQueue(msg);
    }

    public void sendResult(IToken token, byte[] results) {
        assert Protocol.isDispatchThread();
        if (state != STATE_OPEN) throw new Error("Channel is closed");
        Message msg = new Message('R');
        msg.data = results;
        msg.token = (Token)token;
        addToOutQueue(msg);
    }

    public void rejectCommand(IToken token) {
        assert Protocol.isDispatchThread();
        if (state != STATE_OPEN) throw new Error("Channel is closed");
        Message msg = new Message('N');
        msg.token = (Token)token;
        addToOutQueue(msg);
    }

    public void sendEvent(IService service, String name, byte[] args) {
        assert Protocol.isDispatchThread();
        if (!(state == STATE_OPEN || state == STATE_OPENNING && service instanceof ILocator)) {
            throw new Error("Channel is closed");
        }
        Message msg = new Message('E');
        msg.service = service.getName();
        msg.name = name;
        msg.data = args;
        addToOutQueue(msg);
    }
    
    public boolean isZeroCopySupported() {
        return zero_copy;
    }
    
    @SuppressWarnings("unchecked")
    private void handleInput(Message msg) {
        assert Protocol.isDispatchThread();
        if (state == STATE_CLOSED) return;
        if (trace_listeners != null) {
            for (TraceListener l : trace_listeners) {
                try {
                    l.onMessageReceived(msg.type,
                            msg.token != null ? msg.token.getID() : null,
                            msg.service, msg.name, msg.data);
                }
                catch (Throwable x) {
                    Protocol.log("Exception in trace listener", x);
                }
            }
        }
        try {
            Token token = null;
            switch (msg.type) {
            case 'P':
            case 'R':
            case 'N':
                String token_id = msg.token.getID();
                Message cmd = msg.type == 'P' ? out_tokens.get(token_id) : out_tokens.remove(token_id);
                if (cmd == null) throw new Exception("Invalid token received: " + token_id);
                token = cmd.token;
                break;
            }
            switch (msg.type) {
            case 'C':
                if (state == STATE_OPENNING) {
                    throw new IOException("Received command " + msg.service + "." + msg.name + " before Hello message");
                }
                if (proxy != null) {
                    proxy.onCommand(msg.token, msg.service, msg.name, msg.data);
                }
                else {
                    token = msg.token;
                    IChannel.ICommandServer cmds = command_servers.get(msg.service);
                    if (cmds != null) {
                        cmds.command(token, msg.name, msg.data);
                    }
                    else {
                        rejectCommand(token);
                    }
                }
                break;
            case 'P':
                token.getListener().progress(token, msg.data);
                sendCongestionLevel();
                break;
            case 'R':
                token.getListener().result(token, msg.data);
                sendCongestionLevel();
                break;
            case 'N':
                token.getListener().terminated(token, new ErrorReport(
                        "Command is not recognized", IErrorReport.TCF_ERROR_INV_COMMAND));
                break;
            case 'E':
                boolean hello = msg.service.equals(ILocator.NAME) && msg.name.equals("Hello");
                if (hello) {
                    remote_service_by_name.clear();
                    remote_service_by_class.clear();
                    ServiceManager.onChannelOpened(this, (Collection<String>)JSON.parseSequence(msg.data)[0], remote_service_by_name);
                    makeServiceByClassMap(remote_service_by_name, remote_service_by_class);
                    zero_copy = remote_service_by_name.containsKey("ZeroCopy");
                }
                if (proxy != null && state == STATE_OPEN) {
                    proxy.onEvent(msg.service, msg.name, msg.data);
                }
                else if (hello) {
                    assert state == STATE_OPENNING;                    
                    state = STATE_OPEN;
                    assert redirect_command == null;
                    if (redirect_queue.size() > 0) {
                        redirect(redirect_queue.removeFirst());
                    }
                    else {
                        notifying_channel_opened = true;
                        if (!registered_with_trasport) {
                            TransportManager.channelOpened(this);
                            registered_with_trasport = true;
                        }
                        listeners_array = channel_listeners.toArray(listeners_array);
                        for (IChannelListener l : listeners_array) {
                            if (l == null) break;
                            try {
                                l.onChannelOpened();
                            }
                            catch (Throwable x) {
                                Protocol.log("Exception in channel listener", x);
                            }
                        }
                        notifying_channel_opened = false;
                    }
                }
                else {
                    IChannel.IEventListener[] list = event_listeners.get(msg.service);
                    if (list != null) {
                        for (int i = 0; i < list.length; i++) {
                            list[i].event(msg.name, msg.data);
                        }
                    }
                    sendCongestionLevel();
                }
                break;
            case 'F':
                int len = msg.data.length;
                if (len > 0 && msg.data[len - 1] == 0) len--;
                remote_congestion_level = Integer.parseInt(new String(msg.data, 0, len, "ASCII"));
                break;
            default:
                assert false;
                break;
            }
        }
        catch (Throwable x) {
            terminate(x);
        }
    }

    private void sendCongestionLevel() throws IOException {
        if (++local_congestion_cnt < 8) return;
        local_congestion_cnt = 0;
        if (state != STATE_OPEN) return;
        long time = System.currentTimeMillis();
        if (time - local_congestion_time < 500) return;
        assert Protocol.isDispatchThread();
        int level = Protocol.getCongestionLevel();
        if (level == local_congestion_level) return;
        int i = (level - local_congestion_level) / 8;
        if (i != 0) level = local_congestion_level + i;
        local_congestion_time = time;
        synchronized (out_queue) {
            Message msg = out_queue.isEmpty() ? null : out_queue.get(0);
            if (msg == null || msg.type != 'F') {
                msg = new Message('F');
                out_queue.add(0, msg);
                out_queue.notify();
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append(local_congestion_level);
            buffer.append((char)0); // 0 terminate
            msg.data = buffer.toString().getBytes("ASCII");
            msg.trace = trace_listeners;
            local_congestion_level = level;
        }
    }

    /**
     * Read one byte from the channel input stream.
     * @return next data byte or EOS (-1) if end of stream is reached,
     * or EOM (-2) if end of message is reached.
     * @throws IOException
     */
    protected abstract int read() throws IOException;

    /**
     * Write one byte into the channel output stream.
     * The method argument can be one of two special values:
     *   EOS (-1) end of stream marker;
     *   EOM (-2) end of message marker. 
     * The stream can put the byte into a buffer instead of transmitting it right away.
     * @param n - the data byte.
     * @throws IOException
     */
    protected abstract void write(int n) throws IOException;

    /**
     * Flush the channel output stream.
     * All buffered data should be transmitted immediately.
     * @throws IOException
     */
    protected abstract void flush() throws IOException;

    /**
     * Stop (close) channel underlying streams.
     * If a thread is blocked by read() or write(), it should be
     * resumed (or interrupted).  
     * @throws IOException
     */
    protected abstract void stop() throws IOException;

    /**
     * Write array of bytes into the channel output stream.
     * The stream can put bytes into a buffer instead of transmitting it right away.
     * @param buf
     * @throws IOException
     */
    protected void write(byte[] buf) throws IOException {
        assert Thread.currentThread() == out_thread;
        for (int i = 0; i < buf.length; i++) write(buf[i]);
    }
}
