package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.tcf.services.ITerminals.TerminalContext;

class TestTerminals implements ITCFTest {

    private final TCFTestSuite test_suite;
    private final ITerminals terminals;
    private final IProcesses processes;
    private final Random rnd = new Random();

    private TerminalContext terminal;
    private TerminalContext get_ctx;
    private Map<String,String> environment;
    private Collection<Map<String,Object>> signal_list;
    private boolean signal_sent;

    private final ITerminals.TerminalsListener listener = new ITerminals.TerminalsListener() {

        public void exited(String id, int exit_code) {
            if (terminal != null && id.equals(terminal.getID()) && !signal_sent) {
                exit(new Exception("Terminal exited with code " + exit_code));
            }
        }

        public void winSizeChanged(String id, int w, int h) {
        }
    };

    TestTerminals(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        terminals = channel.getRemoteService(ITerminals.class);
        processes = channel.getRemoteService(IProcesses.class);
    }

    public void start() {
        if (terminals == null) {
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
                public void doneLaunch(IToken token, Exception error, TerminalContext terminal) {
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
            processes.getSignalList(terminal.getProcessID(), new IProcesses.DoneGetSignalList() {
                public void doneGetSignalList(IToken token, Exception error, Collection<Map<String,Object>> list) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (list == null) {
                        exit(new Exception("Signal list must not be null"));
                    }
                    else {
                        TestTerminals.this.signal_list = list;
                        run();
                    }
                }
            });
        }
        if (!signal_sent) {
            if (signal_list != null && rnd.nextBoolean()) {
                int code = 15;
                for (Map<String,Object> m : signal_list) {
                    String nm = (String)m.get(IProcesses.SIG_NAME);
                    if (nm != null && nm.equals("SIGTERM")) {
                        Number n = (Number)m.get(IProcesses.SIG_CODE);
                        if (n != null) code = n.intValue();
                    }
                }
                processes.signal(terminal.getProcessID(), code, new IProcesses.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
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
                terminals.exit(terminal.getID(), new ITerminals.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
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
            return;
        }
        exit(null);
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
