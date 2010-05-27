/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.tm.internal.tcf.debug.Activator;
import org.eclipse.tm.internal.tcf.debug.actions.TCFAction;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate.PathMapRule;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IPathMap;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;


public class TCFLaunch extends Launch {

    public interface Listener {

        public void onCreated(TCFLaunch launch);

        public void onConnected(TCFLaunch launch);

        public void onDisconnected(TCFLaunch launch);

        public void onContextActionsStart(TCFLaunch launch, String ctx_id);

        public void onContextActionsDone(TCFLaunch launch, String ctx_id, String result);

        public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data);

        public void onProcessStreamError(
                TCFLaunch launch, String process_id, int stream_id,
                Exception error, int lost_size);
    }

    private abstract class LaunchStep implements Runnable {

        LaunchStep() {
            launch_steps.add(this);
        }

        abstract void start() throws Exception;

        void done() {
            if (channel.getState() != IChannel.STATE_OPEN) return;
            try {
                launch_steps.removeFirst().start();
            }
            catch (Throwable x) {
                channel.terminate(x);
            }
        }

        public void run() {
            done();
        }
    }

    private static final Collection<Listener> listeners = new ArrayList<Listener>();

    private IChannel channel;
    private Throwable error;
    private TCFBreakpointsStatus breakpoints_status;
    private String mode;
    private boolean connecting;
    private boolean disconnected;
    private boolean shutdown;
    private boolean last_context_exited;

    private ProcessContext process;
    private Collection<Map<String,Object>> process_signals;
    private IToken process_start_command;
    private String process_input_stream_id;
    private int process_exit_code;
    private final HashMap<String,String> process_env = new HashMap<String,String>();

    private final HashMap<String,LinkedList<Runnable>> context_action_queue = new HashMap<String,LinkedList<Runnable>>();
    private final HashMap<String,String> stream_ids = new HashMap<String,String>();
    private final LinkedList<LaunchStep> launch_steps = new LinkedList<LaunchStep>();
    private final LinkedList<String> redirection_path = new LinkedList<String>();

    private final IStreams.StreamsListener streams_listener = new IStreams.StreamsListener() {

        public void created(String stream_type, String stream_id, String context_id) {
            assert IProcesses.NAME.equals(stream_type);
            if (process_start_command == null) {
                disconnectStream(stream_id);
            }
            else {
                stream_ids.put(stream_id, context_id);
            }
        }

        public void disposed(String stream_type, String stream_id) {
        }
    };

    private final IProcesses.ProcessesListener prs_listener = new IProcesses.ProcessesListener() {

        public void exited(String process_id, int exit_code) {
            if (process_id.equals(process.getID())) process_exit_code = exit_code;
        }
    };

    public TCFLaunch(ILaunchConfiguration launchConfiguration, String mode) {
        super(launchConfiguration, mode, null);
        for (Listener l : listeners) l.onCreated(TCFLaunch.this);
    }

    private void onConnected() throws Exception {
        // The method is called when TCF channel is successfully connected.

        final ILaunchConfiguration cfg = getLaunchConfiguration();
        if (cfg != null) {
            // Send file path map:
            final String path_map = cfg.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, "");
            final IPathMap path_map_service = getService(IPathMap.class);
            if (path_map.length() != 0 && path_map_service != null) {
                new LaunchStep() {
                    @Override
                    void start() throws Exception {
                        ArrayList<PathMapRule> map = TCFLaunchDelegate.parsePathMapAttribute(path_map);
                        path_map_service.set(map.toArray(new IPathMap.PathMapRule[map.size()]), new IPathMap.DoneSet() {
                            public void doneSet(IToken token, Exception error) {
                                if (error != null) channel.terminate(error);
                                else done();
                            }
                        });
                    }
                };
            }
        }

        if (redirection_path.size() > 0) {
            // Connected to intermediate peer (value-add).
            // Redirect to next peer:
            new LaunchStep() {
                @Override
                void start() throws Exception {
                    channel.redirect(redirection_path.removeFirst());
                }
            };
        }
        else {
            final IStreams streams = getService(IStreams.class);
            if (streams != null) {
                // Subscribe Streams service:
                new LaunchStep() {
                    @Override
                    void start() {
                        streams.subscribe(IProcesses.NAME, streams_listener, new IStreams.DoneSubscribe() {
                            public void doneSubscribe(IToken token, Exception error) {
                                if (error != null) channel.terminate(error);
                                else done();
                            }
                        });
                    }
                };
            }

            if (mode.equals(ILaunchManager.DEBUG_MODE)) {
                // Send breakpoints:
                new LaunchStep() {
                    @Override
                    void start() throws Exception {
                        breakpoints_status = new TCFBreakpointsStatus(TCFLaunch.this);
                        Activator.getBreakpointsModel().downloadBreakpoints(channel, this);
                    }
                };
            }

            // Call client launch sequence:
            new LaunchStep() {
                @Override
                void start() {
                    runLaunchSequence(this);
                }
            };

            if (cfg != null) startRemoteProcess(cfg);

            // Final launch step.
            // Notify clients:
            new LaunchStep() {
                @Override
                void start() {
                    connecting = false;
                    for (Listener l : listeners) l.onConnected(TCFLaunch.this);
                    fireChanged();
                }
            };
        }

        launch_steps.removeFirst().start();
    }

    private void onDisconnected(Throwable error) {
        // The method is called when TCF channel is closed.
        assert !disconnected;
        assert !shutdown;
        this.error = error;
        breakpoints_status = null;
        connecting = false;
        disconnected = true;
        for (Listener l : listeners) l.onDisconnected(this);
        if (DebugPlugin.getDefault() != null) fireChanged();
        runShutdownSequence(new Runnable() {
            public void run() {
                shutdown = true;
                if (DebugPlugin.getDefault() != null) fireTerminate();
            }
        });
    }

    protected void runLaunchSequence(final Runnable done) {
        done.run();
    }

    private String[] toArgsArray(String file, String cmd) {
        // Create arguments list from a command line.
        int i = 0;
        int l = cmd.length();
        List<String> arr = new ArrayList<String>();
        arr.add(file);
        for (;;) {
            while (i < l && cmd.charAt(i) == ' ') i++;
            if (i >= l) break;
            String s = null;
            if (cmd.charAt(i) == '"') {
                i++;
                StringBuffer bf = new StringBuffer();
                while (i < l) {
                    char ch = cmd.charAt(i++);
                    if (ch == '"') break;
                    if (ch == '\\' && i < l) ch = cmd.charAt(i++);
                    bf.append(ch);
                }
                s = bf.toString();
            }
            else {
                int i0 = i;
                while (i < l && cmd.charAt(i) != ' ') i++;
                s = cmd.substring(i0, i);
            }
            arr.add(s);
        }
        return arr.toArray(new String[arr.size()]);
    }

    private void copyFileToRemoteTarget(String local_file, String remote_file, final Runnable done) {
        if (local_file == null) {
            channel.terminate(new Exception("Program does not exist"));
            return;
        }
        final IFileSystem fs = channel.getRemoteService(IFileSystem.class);
        if (fs == null) {
            channel.terminate(new Exception(
                    "Cannot download program file: target does not provide File System service"));
            return;
        }
        try {
            final InputStream inp = new FileInputStream(local_file);
            int flags = IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC;
            fs.open(remote_file, flags, null, new IFileSystem.DoneOpen() {

                IFileHandle handle;
                long offset = 0;
                final Set<IToken> cmds = new HashSet<IToken>();
                final byte[] buf = new byte[0x1000];

                public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
                    this.handle = handle;
                    if (error != null) {
                        TCFLaunch.this.error = new Exception("Cannot download program file", error);
                        fireChanged();
                        done.run();
                    }
                    else {
                        write_next();
                    }
                }

                private void write_next() {
                    try {
                        while (cmds.size() < 8) {
                            int rd = inp.read(buf);
                            if (rd < 0) {
                                close();
                                break;
                            }
                            cmds.add(fs.write(handle, offset, buf, 0, rd, new IFileSystem.DoneWrite() {

                                public void doneWrite(IToken token, FileSystemException error) {
                                    cmds.remove(token);
                                    if (error != null) channel.terminate(error);
                                    else write_next();
                                }
                            }));
                            offset += rd;
                        }
                    }
                    catch (Throwable x) {
                        channel.terminate(x);
                    }
                }

                private void close() {
                    if (cmds.size() > 0) return;
                    try {
                        inp.close();
                        fs.close(handle, new IFileSystem.DoneClose() {

                            public void doneClose(IToken token, FileSystemException error) {
                                if (error != null) channel.terminate(error);
                                else done.run();
                            }
                        });
                    }
                    catch (Throwable x) {
                        channel.terminate(x);
                    }
                }
            });
        }
        catch (Throwable x) {
            channel.terminate(x);
        }
    }

    @SuppressWarnings("unchecked")
    private void startRemoteProcess(final ILaunchConfiguration cfg) throws Exception {
        final String project = cfg.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, "");
        final String local_file = cfg.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, "");
        final String remote_file = cfg.getAttribute(TCFLaunchDelegate.ATTR_REMOTE_PROGRAM_FILE, "");
        if (local_file.length() != 0 && remote_file.length() != 0) {
            // Download executable file
            new LaunchStep() {
                @Override
                void start() throws Exception {
                    copyFileToRemoteTarget(TCFLaunchDelegate.getProgramPath(project, local_file), remote_file, this);
                }
            };
        }
        if (local_file.length() != 0 || remote_file.length() != 0) {
            final IProcesses ps = channel.getRemoteService(IProcesses.class);
            if (ps == null) throw new Exception("Target does not provide Processes service");
            final boolean append = cfg.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
            if (append) {
                // Get system environment variables
                new LaunchStep() {
                    @Override
                    void start() throws Exception {
                        ps.getEnvironment(new IProcesses.DoneGetEnvironment() {
                            public void doneGetEnvironment(IToken token, Exception error, Map<String,String> env) {
                                if (error != null) {
                                    channel.terminate(error);
                                }
                                else {
                                    if (env != null) process_env.putAll(env);
                                    done();
                                }
                            }
                        });
                    }
                };
            }
            final String dir = cfg.getAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, "");
            final String args = cfg.getAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, "");
            final Map<String,String> env = cfg.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map)null);
            // Start the process
            new LaunchStep() {
                @Override
                void start() {
                    if (env != null) process_env.putAll(env);
                    String file = remote_file;
                    if (file == null || file.length() == 0) file = TCFLaunchDelegate.getProgramPath(project, local_file);
                    if (file == null || file.length() == 0) {
                        channel.terminate(new Exception("Program file does not exist"));
                        return;
                    }
                    boolean attach = mode.equals(ILaunchManager.DEBUG_MODE);
                    process_start_command = ps.start(dir, file, toArgsArray(file, args),
                            process_env, attach, new IProcesses.DoneStart() {
                        public void doneStart(IToken token, final Exception error, ProcessContext process) {
                            process_start_command = null;
                            if (error != null) {
                                for (String id : new HashSet<String>(stream_ids.keySet())) disconnectStream(id);
                                Protocol.sync(new Runnable() {
                                    public void run() {
                                        channel.terminate(error);
                                    }
                                });
                            }
                            else {
                                TCFLaunch.this.process = process;
                                ps.addListener(prs_listener);
                                connectProcessStreams();
                                done();
                            }
                        }
                    });
                }
            };
            // Get process signal list
            new LaunchStep() {
                @Override
                void start() {
                    ps.getSignalList(process.getID(), new IProcesses.DoneGetSignalList() {
                        public void doneGetSignalList(IToken token, Exception error, Collection<Map<String,Object>> list) {
                            if (error != null) Activator.log("Can't get process signal list", error);
                            process_signals = list;
                            done();
                        }
                    });
                }
            };
            // Set process signal masks
            String dont_stop = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_STOP, "");
            String dont_pass = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_PASS, "");
            final int no_stop = dont_stop.length() > 0 ? Integer.parseInt(dont_stop, 16) : 0;
            final int no_pass = dont_pass.length() > 0 ? Integer.parseInt(dont_pass, 16) : 0;
            if (no_stop != 0 || no_pass != 0) {
                new LaunchStep() {
                    @Override
                    void start() {
                        final HashSet<IToken> cmds = new HashSet<IToken>();
                        final IProcesses.DoneCommand done_set_mask = new IProcesses.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                cmds.remove(token);
                                if (error != null) channel.terminate(error);
                                else if (cmds.size() == 0) done();
                            }
                        };
                        cmds.add(ps.setSignalMask(process.getID(), no_stop, no_pass, done_set_mask));
                        final IRunControl rc = channel.getRemoteService(IRunControl.class);
                        if (rc != null) {
                            final IRunControl.DoneGetChildren done_get_children = new IRunControl.DoneGetChildren() {
                                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                                    if (context_ids != null) {
                                        for (String id : context_ids) {
                                            cmds.add(ps.setSignalMask(id, no_stop, no_pass, done_set_mask));
                                            cmds.add(rc.getChildren(id, this));
                                        }
                                    }
                                    cmds.remove(token);
                                    if (error != null) channel.terminate(error);
                                    else if (cmds.size() == 0) done();
                                }
                            };
                            cmds.add(rc.getChildren(process.getID(), done_get_children));
                        }
                    }
                };
            }
        }
    }

    private void connectProcessStreams() {
        assert process_start_command == null;
        final IStreams streams = getService(IStreams.class);
        if (streams == null) return;
        final String inp_id = (String)process.getProperties().get(IProcesses.PROP_STDIN_ID);
        final String out_id = (String)process.getProperties().get(IProcesses.PROP_STDOUT_ID);
        final String err_id = (String)process.getProperties().get(IProcesses.PROP_STDERR_ID);
        for (final String id : stream_ids.keySet().toArray(new String[stream_ids.size()])) {
            if (id.equals(inp_id)) {
                process_input_stream_id = id;
            }
            else if (id.equals(out_id)) {
                connectStream(id, 0);
            }
            else if (id.equals(err_id)) {
                connectStream(id, 1);
            }
            else {
                disconnectStream(id);
            }
        }
    }

    private void connectStream(final String id, final int no) {
        final String peocess_id = process.getID();
        final IStreams streams = getService(IStreams.class);
        IStreams.DoneRead done = new IStreams.DoneRead() {
            public void doneRead(IToken token, Exception error, int lost_size, byte[] data, boolean eos) {
                if (stream_ids.get(id) == null) return;
                if (lost_size > 0) {
                    Exception x = new IOException("Process output data lost due buffer overflow");
                    for (Listener l : listeners) l.onProcessStreamError(TCFLaunch.this, peocess_id, no, x, lost_size);
                }
                if (data != null && data.length > 0) {
                    for (Listener l : listeners) l.onProcessOutput(TCFLaunch.this, peocess_id, no, data);
                }
                if (error != null) {
                    for (Listener l : listeners) l.onProcessStreamError(TCFLaunch.this, peocess_id, no, error, 0);
                }
                if (eos || error != null) {
                    disconnectStream(id);
                }
                else {
                    streams.read(id, 0x1000, this);
                }
            }
        };
        streams.read(id, 0x1000, done);
        streams.read(id, 0x1000, done);
        streams.read(id, 0x1000, done);
        streams.read(id, 0x1000, done);
    }

    private void disconnectStream(String id) {
        stream_ids.remove(id);
        if (channel.getState() != IChannel.STATE_OPEN) return;
        IStreams streams = getService(IStreams.class);
        streams.disconnect(id, new IStreams.DoneDisconnect() {
            public void doneDisconnect(IToken token, Exception error) {
                if (channel.getState() != IChannel.STATE_OPEN) return;
                if (error != null) channel.terminate(error);
            }
        });
    }

    protected void runShutdownSequence(final Runnable done) {
        done.run();
    }

    /*--------------------------------------------------------------------------------------------*/

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable x) {
        error = x;
        if (x != null) {
            if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
                channel.terminate(x);
            }
            else if (!connecting) {
                disconnected = true;
            }
        }
        fireChanged();
    }

    public TCFBreakpointsStatus getBreakpointsStatus() {
        return breakpoints_status;
    }

    public static void addListener(Listener listener) {
        assert Protocol.isDispatchThread();
        listeners.add(listener);
    }

    public static void removeListener(Listener listener) {
        assert Protocol.isDispatchThread();
        listeners.remove(listener);
    }

    /** Thread safe method */
    public IChannel getChannel() {
        return channel;
    }

    public IProcesses.ProcessContext getProcessContext() {
        return process;
    }

    public void writeProcessInputStream(byte[] buf, int pos, final int len) throws Exception {
        assert Protocol.isDispatchThread();
        final String id = process_input_stream_id;
        if (channel.getState() != IChannel.STATE_OPEN) throw new IOException("Connection closed");
        if (process == null) throw new IOException("No target process");
        final String prs = process.getID();
        IStreams streams = getService(IStreams.class);
        if (streams == null) throw new IOException("Streams service not available");
        if (stream_ids.get(id) == null) throw new IOException("Input stream not available");
        streams.write(id, buf, pos, len, new IStreams.DoneWrite() {
            public void doneWrite(IToken token, Exception error) {
                if (error == null) return;
                if (stream_ids.get(id) == null) return;
                for (Listener l : listeners) l.onProcessStreamError(TCFLaunch.this, prs, 0, error, len);
                disconnectStream(id);
            }
        });
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void onLastContextRemoved() {
        last_context_exited = true;
        closeChannel();
    }

    public void closeChannel() {
        assert Protocol.isDispatchThread();
        if (channel == null) return;
        if (channel.getState() == IChannel.STATE_CLOSED) return;
        IStreams streams = getService(IStreams.class);
        final Set<IToken> cmds = new HashSet<IToken>();
        for (String id : stream_ids.keySet()) {
            cmds.add(streams.disconnect(id, new IStreams.DoneDisconnect() {
                public void doneDisconnect(IToken token, Exception error) {
                    cmds.remove(token);
                    if (channel.getState() == IChannel.STATE_CLOSED) return;
                    if (error != null) channel.terminate(error);
                    else if (cmds.isEmpty()) channel.close();
                }
            }));
        }
        stream_ids.clear();
        process_input_stream_id = null;
        if (cmds.isEmpty()) channel.close();
    }

    public IPeer getPeer() {
        assert Protocol.isDispatchThread();
        return channel.getRemotePeer();
    }

    public <V extends IService> V getService(Class<V> cls) {
        assert Protocol.isDispatchThread();
        return channel.getRemoteService(cls);
    }

    public boolean canDisconnect() {
        return !disconnected;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void disconnect() throws DebugException {
        try {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    closeChannel();
                }
            });
        }
        catch (IllegalStateException x) {
            disconnected = true;
        }
    }

    public boolean canTerminate() {
        return false;
    }

    public boolean isTerminated() {
        return disconnected;
    }

    public void terminate() throws DebugException {
    }

    public boolean isExited() {
        return last_context_exited;
    }

    public int getExitCode() {
        return process_exit_code;
    }

    public Collection<Map<String,Object>> getSignalList() {
        return process_signals;
    }

    public void launchTCF(String mode, String id) {
        assert Protocol.isDispatchThread();
        this.mode = mode;
        try {
            if (id == null || id.length() == 0) throw new IOException("Invalid peer ID");
            redirection_path.clear();
            for (;;) {
                int i = id.indexOf('/');
                if (i <= 0) {
                    redirection_path.add(id);
                    break;
                }
                redirection_path.add(id.substring(0, i));
                id = id.substring(i + 1);
            }
            String id0 = redirection_path.removeFirst();
            IPeer peer = Protocol.getLocator().getPeers().get(id0);
            if (peer == null) throw new Exception("Cannot locate peer " + id0);
            channel = peer.openChannel();
            channel.addChannelListener(new IChannel.IChannelListener() {

                public void onChannelOpened() {
                    try {
                        onConnected();
                    }
                    catch (Throwable x) {
                        channel.terminate(x);
                    }
                }

                public void congestionLevel(int level) {
                }

                public void onChannelClosed(Throwable error) {
                    channel.removeChannelListener(this);
                    onDisconnected(error);
                }

            });
            assert channel.getState() == IChannel.STATE_OPENNING;
            connecting = true;
        }
        catch (Throwable e) {
            onDisconnected(e);
        }
    }

    public void addContextAction(TCFAction action, String context_id) {
        assert Protocol.isDispatchThread();
        LinkedList<Runnable> list = context_action_queue.get(context_id);
        if (list == null) {
            list = new LinkedList<Runnable>();
            context_action_queue.put(context_id, list);
        }
        list.add(action);
        if (list.getFirst() == action) {
            Protocol.invokeLater(action);
            for (Listener l : listeners) l.onContextActionsStart(this, context_id);
        }
    }

    public void removeContextAction(TCFAction action, String context_id, String result) {
        assert Protocol.isDispatchThread();
        LinkedList<Runnable> list = context_action_queue.get(context_id);
        if (list == null) return;
        assert list.getFirst() == action;
        list.removeFirst();
        if (!list.isEmpty()) {
            Protocol.invokeLater(list.getFirst());
        }
        else {
            for (Listener l : listeners) l.onContextActionsDone(this, context_id, result);
        }
    }

    public void removeContextActions(String context_id, String result) {
        assert Protocol.isDispatchThread();
        LinkedList<Runnable> list = context_action_queue.remove(context_id);
        if (list != null && list.size() > 0) {
            for (Listener l : listeners) l.onContextActionsDone(this, context_id, result);
        }
    }
}
