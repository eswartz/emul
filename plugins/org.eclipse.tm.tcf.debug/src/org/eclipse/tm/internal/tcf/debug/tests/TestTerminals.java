package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.tcf.services.ITerminals.TerminalContext;

class TestTerminals implements ITCFTest {

    private final TCFTestSuite test_suite;
    private final ITerminals terminals;
    private final IProcesses processes;
    private final IStreams streams;
    private final Random rnd = new Random();
    private final HashSet<String> stream_ids = new HashSet<String>();
    private final StringBuffer stdout_buf = new StringBuffer();
    private final StringBuffer stderr_buf = new StringBuffer();
    private final HashSet<IToken> disconnect_cmds = new HashSet<IToken>();

    private IStreams.StreamsListener streams_listener;
    private IStreams.DoneRead stdout_read;
    private IStreams.DoneRead stderr_read;
    private String encoding;
    private TerminalContext terminal;
    private TerminalContext get_ctx;
    private Map<String,String> environment;
    private boolean delay_done;
    private Collection<Map<String,Object>> signal_list;
    private IToken get_signals_cmd;
    private IToken signal_cmd;
    private boolean signal_sent;
    private IToken unsubscribe_cmd;
    private boolean unsubscribe_done;
    private boolean exited;
    private boolean stdout_eos;

    private final ITerminals.TerminalsListener listener = new ITerminals.TerminalsListener() {

        public void exited(String id, int exit_code) {
            if (terminal != null && id.equals(terminal.getID())) {
                exited = true;
                if (!signal_sent) {
                    exit(new Exception("Terminal exited with code " + exit_code));
                }
                else {
                    run();
                }
            }
        }

        public void winSizeChanged(String id, int w, int h) {
        }
    };

    private final IStreams.DoneDisconnect disconnect_done = new IStreams.DoneDisconnect() {
        public void doneDisconnect(IToken token, Exception error) {
            assert disconnect_cmds.contains(token);
            disconnect_cmds.remove(token);
            if (error != null) exit(error);
            if (disconnect_cmds.size() == 0 && unsubscribe_done) exit(null);
        }
    };

    TestTerminals(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        terminals = channel.getRemoteService(ITerminals.class);
        processes = channel.getRemoteService(IProcesses.class);
        streams = channel.getRemoteService(IStreams.class);
    }

    public void start() {
        if (terminals == null || streams == null) {
            test_suite.done(this, null);
        }
        else {
            terminals.addListener(listener);
            run();
        }
    }

    private void run() {
        if (environment == null && processes != null) {
            processes.getEnvironment(new IProcesses.DoneGetEnvironment() {
                public void doneGetEnvironment(IToken token, Exception error, Map<String, String> environment) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (environment == null) {
                        exit(new Exception("Default process environment must not be null"));
                    }
                    else {
                        TestTerminals.this.environment = environment;
                        run();
                    }
                }
            });
            return;
        }
        if (streams_listener == null) {
            final IStreams.StreamsListener l = new IStreams.StreamsListener() {
                public void created(String stream_type, String stream_id, String context_id) {
                    if (!terminals.getName().equals(stream_type)) {
                        exit(new Exception("Invalid stream type in Streams.created event: " + stream_type));
                    }
                    else if (stream_id == null || stream_id.length() == 0 || stream_ids.contains(stream_id)) {
                        exit(new Exception("Invalid stream ID in Streams.created event: " + stream_id));
                    }
                    else if (terminal != null) {
                        if (stream_id.equals(terminal.getStdInID()) ||
                                stream_id.equals(terminal.getStdOutID()) ||
                                stream_id.equals(terminal.getStdErrID())) {
                            exit(new Exception("Invalid stream ID in Streams.created event: " + stream_id));
                        }
                        else {
                            disconnect_cmds.add(streams.disconnect(stream_id, disconnect_done));
                        }
                    }
                    else {
                        stream_ids.add(stream_id);
                    }
                }
                public void disposed(String stream_type, String stream_id) {
                }
            };
            streams.subscribe(terminals.getName(), l, new IStreams.DoneSubscribe() {
                public void doneSubscribe(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        streams_listener = l;
                        run();
                    }
                }
            });
            return;
        }
        if (terminal == null) {
            String[] types = { "ansi", "vt100", null };
            String[] langs = { "en_US", "en_US.UTF-8", null };
            String[] env = null;
            if (environment != null && rnd.nextBoolean()) {
                int i = 0;
                env = new String[environment.size() + 1];
                for (String s : environment.keySet()) {
                    env[i++] = s + "=" + environment.get(s);
                }
                env[i++] = "TCF_FOO=BAR";
            }
            terminals.launch(types[rnd.nextInt(types.length)], langs[rnd.nextInt(langs.length)], env, new ITerminals.DoneLaunch() {
                public void doneLaunch(IToken token, Exception error, final TerminalContext terminal) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (terminal == null) {
                        exit(new Exception("Terminal context must not be null"));
                    }
                    else if (terminal.getID() == null) {
                        exit(new Exception("Terminal context ID must not be null"));
                    }
                    else {
                        TestTerminals.this.terminal = terminal;
                        for (Iterator<String> i = stream_ids.iterator(); i.hasNext();) {
                            String stream_id = i.next();
                            if (stream_id.equals(terminal.getStdInID()) ||
                                    stream_id.equals(terminal.getStdOutID()) ||
                                    stream_id.equals(terminal.getStdErrID())) {
                                // keep connected
                            }
                            else {
                                i.remove();
                                disconnect_cmds.add(streams.disconnect(stream_id, disconnect_done));
                            }
                        }
                        Protocol.invokeLater(100, new Runnable() {
                            int cnt = 0;
                            public void run() {
                                if (!test_suite.isActive(TestTerminals.this)) return;
                                cnt++;
                                if (test_suite.cancel) {
                                    exit(null);
                                }
                                else if (cnt < 300) {
                                    Protocol.invokeLater(100, this);
                                }
                                else if (!exited) {
                                    exit(new Error("Timeout waiting for 'Terminals.exited' event. Context: " + terminal.getID()));
                                }
                                else {
                                    exit(new Error("Timeout waiting for end-of-stream. Context: " + terminal.getID()));
                                }
                            }
                        });
                        run();
                    }
                }
            });
            return;
        }
        if (get_ctx == null) {
            terminals.getContext(terminal.getID(), new ITerminals.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, TerminalContext terminal) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (terminal == null) {
                        exit(new Exception("Terminal context must not be null"));
                    }
                    else if (terminal.getID() == null) {
                        exit(new Exception("Terminal context ID must not be null"));
                    }
                    else if (!TestTerminals.this.terminal.getProperties().equals(terminal.getProperties())) {
                        exit(new Exception("Invalid result of Terminal.getContext"));
                    }
                    else {
                        TestTerminals.this.get_ctx = terminal;
                        run();
                    }
                }
            });
            return;
        }
        if (signal_list == null && processes != null && terminal.getProcessID() != null) {
            assert get_signals_cmd == null;
            get_signals_cmd = processes.getSignalList(terminal.getProcessID(), new IProcesses.DoneGetSignalList() {
                public void doneGetSignalList(IToken token, Exception error, Collection<Map<String,Object>> list) {
                    assert get_signals_cmd == token;
                    get_signals_cmd  = null;
                    if (error != null) {
                        exit(error);
                    }
                    else if (list == null) {
                        exit(new Exception("Signal list must not be null"));
                    }
                    else {
                        signal_list = list;
                        run();
                    }
                }
            });
            return;
        }
        if (encoding == null) {
            String lang = terminal.getEncoding();
            if (lang == null && environment != null) lang = environment.get("LC_ALL");
            if (lang == null && environment != null) lang = environment.get("LANG");
            if (lang == null) lang = "en_US.UTF-8";
            int i = lang.indexOf('.');
            int j = lang.indexOf('@');
            if (i < 0) {
                encoding = "UTF-8";
            }
            else if (j < i) {
                encoding = lang.substring(i + 1);
            }
            else {
                encoding = lang.substring(i + 1, j);
            }
        }
        if (stdout_read == null) {
            final String id = terminal.getStdOutID();
            if (id == null) {
                exit(new Exception("stdout stream ID is null"));
                return;
            }
            stdout_read = new IStreams.DoneRead() {
                public void doneRead(IToken token, Exception error, int lost_size, byte[] data, boolean eos) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (lost_size > 0) {
                        exit(new Exception("Lost bytes in terminal stream"));
                    }
                    else {
                        try {
                            if (data != null) stdout_buf.append(new String(data, encoding));
                            if (!eos) {
                                streams.read(id, 0x1000, this);
                            }
                            else {
                                stdout_eos = true;
                                run();
                            }
                        }
                        catch (Exception x) {
                            exit(x);
                        }
                    }
                }
            };
            streams.read(id, 0x1000, stdout_read);
        }
        if (stderr_read == null && terminal.getStdErrID() != null) {
            final String id = terminal.getStdErrID();
            stderr_read = new IStreams.DoneRead() {
                public void doneRead(IToken token, Exception error, int lost_size, byte[] data, boolean eos) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (lost_size > 0) {
                        exit(new Exception("Lost bytes in terminal stream"));
                    }
                    else {
                        try {
                            if (data != null) stderr_buf.append(new String(data, encoding));
                            if (!eos) streams.read(id, 0x1000, this);
                        }
                        catch (Exception x) {
                            exit(x);
                        }
                    }
                }
            };
            streams.read(id, 0x1000, stderr_read);
        }
        if (!delay_done) {
            Protocol.invokeLater(rnd.nextInt(500), new Runnable() {
                public void run() {
                    delay_done = true;
                    TestTerminals.this.run();
                }
            });
            return;
        }
        if (!signal_sent) {
            assert !exited;
            if (signal_cmd == null) {
                int code = 0;
                if (signal_list != null && rnd.nextBoolean()) {
                    for (Map<String,Object> m : signal_list) {
                        String nm = (String)m.get(IProcesses.SIG_NAME);
                        if (nm != null && nm.equals("SIGKILL")) {
                            Number n = (Number)m.get(IProcesses.SIG_CODE);
                            if (n != null) code = n.intValue();
                        }
                    }
                    if (code == 0) {
                        for (Map<String,Object> m : signal_list) {
                            String nm = (String)m.get(IProcesses.SIG_NAME);
                            if (nm != null && nm.equals("SIGTERM")) {
                                Number n = (Number)m.get(IProcesses.SIG_CODE);
                                if (n != null) code = n.intValue();
                            }
                        }
                    }
                }
                if (code > 0) {
                    signal_cmd = processes.signal(terminal.getProcessID(), code, new IProcesses.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            assert signal_cmd == token;
                            signal_cmd = null;
                            if (error != null) {
                                exit(error);
                            }
                            else {
                                signal_sent = true;
                                run();
                            }
                        }
                    });
                }
                else {
                    signal_cmd = terminals.exit(terminal.getID(), new ITerminals.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            assert signal_cmd == token;
                            signal_cmd = null;
                            if (error != null) {
                                exit(error);
                            }
                            else {
                                signal_sent = true;
                                run();
                            }
                        }
                    });
                }
            }
            return;
        }
        if (exited && stdout_eos) {
            if (!unsubscribe_done) {
                if (unsubscribe_cmd == null) {
                    unsubscribe_cmd = streams.unsubscribe(terminals.getName(), streams_listener, new IStreams.DoneUnsubscribe() {
                        public void doneUnsubscribe(IToken token, Exception error) {
                            unsubscribe_done = true;
                            if (error != null) exit(error);
                            else run();
                        }
                    });
                }
                return;
            }
            else {
                for (String stream_id : stream_ids) {
                    disconnect_cmds.add(streams.disconnect(stream_id, disconnect_done));
                }
                stream_ids.clear();
                checkTerminalOutput(stdout_buf);
                checkTerminalOutput(stderr_buf);
            }
        }
    }

    private void checkTerminalOutput(StringBuffer bf) {
        String s = bf.toString();
        if (s.indexOf("Cannot start") >= 0) {
            exit(new Exception("Unexpected terminal output:\n" + s));
        }
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        if (terminals != null) terminals.removeListener(listener);
        test_suite.done(this, x);
    }

    public boolean canResume(IRunControl.RunControlContext ctx) {
        return true;
    }
}
