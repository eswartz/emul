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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.windriver.tcf.api.Activator;
import com.windriver.tcf.api.internal.core.Token;
import com.windriver.tcf.api.internal.core.Transport;
import com.windriver.tcf.api.internal.services.local.DiagnosticsService;
import com.windriver.tcf.api.internal.services.local.LocatorService;
import com.windriver.tcf.api.internal.services.remote.GenericProxy;
import com.windriver.tcf.api.internal.services.remote.LocatorProxy;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IPeer;
import com.windriver.tcf.api.protocol.IService;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.JSON;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.ILocator;

public abstract class AbstractChannel implements IChannel {

    public interface TraceListener {
        
        public void onMessageReceived(char type, String token,
                String service, String name, byte[] data);
        
        public void onMessageSent(char type, String token,
                String service, String name, byte[] data);
        
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

    private static IChannelListener[] listeners_array = new IChannelListener[64];

    private final LinkedList<String> redirect_queue = new LinkedList<String>();
    private final Map<Class<?>,IService> local_service_by_class = new HashMap<Class<?>,IService>();
    private final Map<Class<?>,IService> remote_service_by_class = new HashMap<Class<?>,IService>();
    private final Map<String,IService> local_service_by_name = new HashMap<String,IService>();
    private final Map<String,IService> remote_service_by_name = new HashMap<String,IService>();
    private final LinkedList<Message> out_queue = new LinkedList<Message>();
    private final Collection<IChannelListener> channel_listeners = new ArrayList<IChannelListener>();
    private final Map<String,IChannel.IEventListener[]> event_listeners = new HashMap<String,IChannel.IEventListener[]>();
    private final Map<String,IChannel.ICommandServer> command_servers = new HashMap<String,IChannel.ICommandServer>();
    private final Map<String,Message> inp_tokens = new HashMap<String,Message>();
    private final Map<String,Message> out_tokens = new HashMap<String,Message>();
    private final Thread inp_thread;
    private final Thread out_thread;
    private boolean notifying_channel_opened;
    private boolean shutdown;
    private int state = STATE_OPENNING;
    private IToken redirect_command;
    private IPeer peer;

    private static final int pending_command_limit = 10;
    private int local_congestion_level = -100;
    private int remote_congestion_level = -100;
    private long local_congestion_time;
    private int inp_queue_size = 0;
    private Collection<TraceListener> trace_listeners;

    public static final int
        EOS = -1, // End Of Stream
        EOM = -2; // End Of Message

    protected AbstractChannel(IPeer peer) {
        assert Protocol.isDispatchThread();
        assert Protocol.getLocator().getPeers().get(peer.getID()) == peer;
        this.peer = peer;

        addLocalService(Protocol.getLocator());
        addLocalService(new DiagnosticsService(this));

        inp_thread = new Thread() {

            byte[] buf = new byte[1024];
            byte[] eos;

            private void error() throws IOException {
                throw new IOException("Protocol syntax error");
            }

            private byte[] readBytes(int end) throws IOException {
                int len = 0;
                for (;;) {
                    int n = read();
                    if (n == end) break;
                    if (n < 0) error();
                    if (len >= buf.length) {
                        byte[] tmp = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, tmp, 0, len);
                        buf = tmp;
                    }
                    buf[len++] = (byte)n;
                }
                byte[] res = new byte[len];
                System.arraycopy(buf, 0, res, 0, len);
                return res;
            }

            private String readString() throws IOException {
                return new String(readBytes(0), "UTF8");
            }

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
                        case 'R':
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
                        long delay = 0;
                        synchronized (out_queue) {
                            inp_queue_size++;
                            if (inp_queue_size > 32) delay = inp_queue_size;
                        }
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                handleInput(msg);
                            }
                        });
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
                                    int error_code = ((Number)args[0]).intValue();
                                    if (error_code != 0) {
                                        x = new IOException(Command.toErrorString(args[1]));
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
                                            Activator.log("Exception in channel listener", x);
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

        try {
            Object[] args = new Object[]{ local_service_by_name.keySet() };  
            sendEvent(Protocol.getLocator(), "Hello", JSON.toJSONSequence(args));
        }
        catch (IOException x) {
            throw new Error(x);
        }
    }

    protected void start() {
        assert Protocol.isDispatchThread();
        inp_thread.start();
        out_thread.start();
        LocatorService.channelStarted(this);
    }

    public void redirect(String peer_id) {
        assert Protocol.isDispatchThread();
        if (state == STATE_OPENNING) {
            assert redirect_command == null;
            redirect_queue.add(peer_id);
        }
        else {
            assert state == STATE_OPEN;
            state = STATE_OPENNING;
            try {
                onLocatorHello(new ArrayList<String>());
            }
            catch (Throwable x) {
                terminate(x);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onLocatorHello(Collection<String> c) throws IOException {
        if (state != STATE_OPENNING) throw new IOException("Invalid event: Locator.Hello");
        remote_service_by_class.clear();
        String pkg_name = LocatorProxy.class.getPackage().getName();
        for (String service_name : c) {
            try {
                Class<?> cls = Class.forName(pkg_name + "." + service_name + "Proxy");
                IService service = (IService)cls.getConstructor(IChannel.class).newInstance(this);
                for (Class<?> fs : cls.getInterfaces()) {
                    if (fs.equals(IService.class)) continue;
                    if (!IService.class.isAssignableFrom(fs)) continue;
                    remote_service_by_class.put(fs, service);
                }
                assert service_name.equals(service.getName());
                remote_service_by_name.put(service_name, service);
            }
            catch (Exception x) {
                IService service = new GenericProxy(this, service_name);
                remote_service_by_name.put(service_name, service);
            }
        }
        assert redirect_command == null;
        if (redirect_queue.size() > 0) {
            String id = redirect_queue.removeFirst();
            ILocator l = (ILocator)remote_service_by_class.get(ILocator.NAME);
            if (l == null) throw new IOException("Peer " + peer.getID() + " has no locator service");
            peer = l.getPeers().get(id);
            if (peer == null) throw new IOException("Unknown peer ID: " + id);
            redirect_command = l.redirect(id, new ILocator.DoneRedirect() {
                public void doneRedirect(IToken token, Exception x) {
                    assert redirect_command == token;
                    assert state == STATE_OPENNING;
                    redirect_command = null;
                    remote_congestion_level = 0;
                    if (x != null) terminate(x);
                    // Wait for next "Hello"
                }
            });
        }
        else {
            state = STATE_OPEN;
            notifying_channel_opened = true;
            Transport.channelOpened(this);
            listeners_array = channel_listeners.toArray(listeners_array);
            for (int i = 0; i < listeners_array.length && listeners_array[i] != null; i++) {
                try {
                    listeners_array[i].onChannelOpened();
                }
                catch (Throwable x) {
                    Activator.log("Exception in channel listener", x);
                }
            }
            notifying_channel_opened = false;
        }
    }

    public final int getState() {
        return state;
    }

    public void addChannelListener(IChannelListener listener) {
        assert Protocol.isDispatchThread();
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
                    System.arraycopy(list, 0, next, 0, i - 1);
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
        if (error != null) Activator.log("TCF channel terminated", error);
        state = STATE_CLOSED;
        Transport.channelClosed(this, error);
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (!out_tokens.isEmpty()) {
                    Exception x = null;
                    if (error instanceof Exception) x = (Exception)error;
                    else if (error != null) x = new Exception(error);
                    else x = new IOException("Channel is closed");
                    for (Message msg : out_tokens.values()) {
                        String s = msg.toString();
                        if (s.length() > 72) s = s.substring(0, 72) + "...]";
                        IOException y = new IOException("Command " + s + " aborted");
                        y.initCause(x);
                        msg.token.getListener().terminated(msg.token, y); 
                    }
                    out_tokens.clear();
                }
                listeners_array = channel_listeners.toArray(listeners_array);
                for (int i = 0; i < listeners_array.length && listeners_array[i] != null; i++) {
                    listeners_array[i].onChannelClosed(error);
                }
                if (trace_listeners != null) {
                    for (TraceListener l : trace_listeners) {
                        try {
                            l.onChannelClosed(error);
                        }
                        catch (Throwable x) {
                            Activator.log("Exception in channel listener", x);
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
        return LocatorService.getLocalPeer();
    }

    public IPeer getRemotePeer() {
        assert Protocol.isDispatchThread();
        return peer;
    }
    
    private void addLocalService(IService service) {
        for (Class<?> fs : service.getClass().getInterfaces()) {
            if (fs.equals(IService.class)) continue;
            if (!IService.class.isAssignableFrom(fs)) continue;
            local_service_by_class.put(fs, service);
        }
        local_service_by_name.put(service.getName(), service);
    }

    public Collection<String> getLocalServices() {
        assert Protocol.isDispatchThread();
        return local_service_by_name.keySet();
    }

    public Collection<String> getRemoteServices() {
        return remote_service_by_name.keySet();
    }
    
    @SuppressWarnings("unchecked")
    public <V extends IService> V getLocalService(Class<V> cls) {
        return (V)local_service_by_class.get(cls);
    }

    @SuppressWarnings("unchecked")
    public <V extends IService> V getRemoteService(Class<V> cls) {
        return (V)remote_service_by_class.get(cls);
    }
    
    public <V extends IService> void setServiceProxy(Class<V> service_interface, IService service_proxy) {
        if (!notifying_channel_opened) new Error("setServiceProxe() can be called only from channel open call-back");
        if (!(remote_service_by_name.get(service_proxy.getName()) instanceof GenericProxy)) throw new Error("Proxy already set"); 
        if (remote_service_by_class.get(service_interface) != null) throw new Error("Proxy already set");
        remote_service_by_class.put(service_interface, service_proxy);
    }
    
    public IService getLocalService(String service_name) {
        return local_service_by_name.get(service_name);
    }

    public IService getRemoteService(String service_name) {
        return remote_service_by_name.get(service_name);
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
        if (state != STATE_OPEN) throw new Error("Channel is closed");
        final Message msg = new Message('C');
        msg.service = service.getName();
        msg.name = name;
        msg.data = args;
        Token token = new Token(listener) {
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

    public void sendResult(IToken token, byte[] results) {
        assert Protocol.isDispatchThread();
        if (state != STATE_OPEN) {
            throw new Error("Channel is closed");
        }
        Message msg = new Message('R');
        msg.data = results;
        msg.token = (Token)token;
        inp_tokens.remove(((Token)token).getID());
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

    private void handleInput(Message msg) {
        assert Protocol.isDispatchThread();
        synchronized (out_queue) {
            inp_queue_size--;
        }
        if (state == STATE_CLOSED) return;
        if (trace_listeners != null) {
            for (TraceListener l : trace_listeners) {
                try {
                    l.onMessageReceived(msg.type,
                            msg.token != null ? msg.token.getID() : null,
                            msg.service, msg.name, msg.data);
                }
                catch (Throwable x) {
                    x.printStackTrace();
                }
            }
        }
        try {
            Token token = null;
            IChannel.IEventListener[] list = null;
            IChannel.ICommandServer cmds = null;
            switch (msg.type) {
            case 'C':
                token = msg.token;
                inp_tokens.put(token.getID(), msg);
                cmds = command_servers.get(msg.service);
                if (cmds != null) {
                    cmds.command(token, msg.name, msg.data);
                }
                else {
                    throw new IOException("Unknown command " + msg.service + "." + msg.name);
                }
                sendCongestionLevel();
                break;
            case 'P':
                token = out_tokens.get(msg.token.getID()).token;
                token.getListener().progress(token, msg.data);
                break;
            case 'R':
                token = out_tokens.remove(msg.token.getID()).token;
                token.getListener().result(token, msg.data);
                break;
            case 'E':
                list = event_listeners.get(msg.service);
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        list[i].event(msg.name, msg.data);
                    }
                }
                sendCongestionLevel();
                break;
            case 'F':
                remote_congestion_level = Integer.parseInt(new String(msg.data, "UTF8"));
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
        assert Protocol.isDispatchThread();
        if (state != STATE_OPEN) return;
        int level = Protocol.getEventQueue().getCongestion();
        int n = inp_tokens.size() * 100 / pending_command_limit - 100;
        if (n > level) level = n;
        if (level > 100) level = 100;
        if (level == local_congestion_level) return;
        long time = System.currentTimeMillis();
        if (level < local_congestion_level) {
            if (time - local_congestion_time < 500) return;
            int i = (local_congestion_level - level) / 4;
            if (i <= 0) i = 1;
            local_congestion_level -= i;
        }
        else {
            local_congestion_level = level;
        }
        local_congestion_time = time;
        synchronized (out_queue) {
            Message msg = out_queue.isEmpty() ? null : out_queue.get(0);
            if (msg == null || msg.type != 'F') {
                msg = new Message('F');
                out_queue.add(0, msg);
                out_queue.notify();
            }
            msg.data = Integer.toString(local_congestion_level).getBytes("UTF8");
            msg.trace = trace_listeners;
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
