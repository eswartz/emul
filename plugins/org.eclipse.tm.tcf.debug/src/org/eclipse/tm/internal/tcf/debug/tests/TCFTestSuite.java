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
package org.eclipse.tm.internal.tcf.debug.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IPathMap.PathMapRule;

/**
 * TCF Test Suite implements stress testing of communication channels and capabilities of remote peer.
 * It is intended to be used before starting a debug session for a first time to make sure the selected
 * target is stable and reliable.
 */
public class TCFTestSuite {

    private final static int NUM_CHANNELS = 4;

    private final TestListener listener;
    private final IChannel[] channels;
    private final LinkedList<Runnable> pending_tests = new LinkedList<Runnable>();
    private final Collection<Throwable> errors = new ArrayList<Throwable>();
    private final Map<ITCFTest,IChannel> active_tests = new HashMap<ITCFTest,IChannel>();

    private int count_total;
    private int count_done;

    boolean cancel;
    boolean canceled;
    boolean target_lock;

    public interface TestListener {
        public void progress(String label, int done, int total);
        public void done(Collection<Throwable> errors);
    }

    public TCFTestSuite(final IPeer peer, final TestListener listener, final List<PathMapRule> path_map) throws IOException {
        this.listener = listener;
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Echo Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestEcho(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Echo FP Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestEchoFP(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Debugger Attach/Terminate Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestAttachTerminate(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Path Map Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestPathMap(TCFTestSuite.this, channel, path_map), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Expressions Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestExpressions(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Streams Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestStreams(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Sys monitor Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestSysMonitor(TCFTestSuite.this, channel), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                int i = 0;
                listener.progress("Running Run Control Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestRCBP1(TCFTestSuite.this, channel, i++), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                int i = 0;
                listener.progress("Running File System Test...", ++count_done, count_total);
                for (IChannel channel : channels) {
                    active_tests.put(new TestFileSystem(TCFTestSuite.this, channel, i++), channel);
                }
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                listener.progress("Running Interability Test...", ++count_done, count_total);
                for (int i = 0; i < channels.length; i++) {
                    ITCFTest test = null;
                    switch (i % 4) {
                    case 0: test = new TestEcho(TCFTestSuite.this, channels[i]); break;
                    case 1: test = new TestAttachTerminate(TCFTestSuite.this, channels[i]); break;
                    case 2: test = new TestRCBP1(TCFTestSuite.this, channels[i], i); break;
                    case 3: test = new TestFileSystem(TCFTestSuite.this, channels[i], i); break;
                    }
                    active_tests.put(test, channels[i]);
                }
            }
        });
        count_total = pending_tests.size() * 2;
        channels = new IChannel[NUM_CHANNELS];
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    openChannels(peer);
                }
                catch (Throwable x) {
                    errors.add(x);
                    int cnt = 0;
                    for (int i = 0; i < channels.length; i++) {
                        if (channels[i] == null) continue;
                        if (channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
                        cnt++;
                    }
                    if (cnt == 0) listener.done(errors);
                }
            }
        });
    }

    private void openChannels(IPeer peer) {
        listener.progress("Openning communication channels...", count_done, count_total);
        for (int i = 0; i < channels.length; i++) {
            final IChannel channel = channels[i] = peer.openChannel();
            channel.addChannelListener(new IChannel.IChannelListener() {

                public void onChannelOpened() {
                    for (int i = 0; i < channels.length; i++) {
                        if (channels[i] == null) return;
                        if (channels[i].getState() != IChannel.STATE_OPEN) return;
                    }
                    runNextTest();
                }

                public void congestionLevel(int level) {
                }

                public void onChannelClosed(Throwable error) {
                    channel.removeChannelListener(this);
                    if (error == null && errors.isEmpty() && (!active_tests.isEmpty() || !pending_tests.isEmpty()) && !cancel) {
                        error = new IOException("Remote peer closed connection before all tests finished");
                    }
                    int cnt = 0;
                    for (int i = 0; i < channels.length; i++) {
                        if (channels[i] == channel) {
                            channels[i] = null;
                            if (error != null) errors.add(error);
                            for (Iterator<ITCFTest> n = active_tests.keySet().iterator(); n.hasNext();) {
                                if (active_tests.get(n.next()) == channel) n.remove();
                            }
                        }
                        if (channels[i] == null) continue;
                        if ((error != null || active_tests.isEmpty() && pending_tests.isEmpty()) &&
                                channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
                        cnt++;
                    }
                    if (cnt == 0) listener.done(errors);
                }
            });
        }
    }

    public void cancel() {
        cancel = true;
        if (canceled) return;
        for (final ITCFTest t : active_tests.keySet()) {
            if (t instanceof TestRCBP1) {
                ((TestRCBP1)t).cancel(new Runnable() {
                    public void run() {
                        assert active_tests.get(t) == null;
                        cancel();
                    }
                });
                return;
            }
        }
        canceled = true;
        for (IChannel c : channels) {
            if (c != null && c.getState() != IChannel.STATE_CLOSED) c.close();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    boolean isActive(ITCFTest test) {
        return active_tests.get(test) != null;
    }

    void done(ITCFTest test, Throwable error) {
        assert active_tests.get(test) != null;
        if (error != null && !canceled) errors.add(error);
        active_tests.remove(test);
        if (active_tests.isEmpty()) runNextTest();
    }

    private void runNextTest() {
        while (active_tests.isEmpty()) {
            if (cancel || errors.size() > 0 || pending_tests.size() == 0) {
                for (IChannel channel : channels) {
                    if (channel != null && channel.getState() != IChannel.STATE_CLOSED) {
                        if (errors.size() > 0) channel.terminate(new Exception("Test failed"));
                        else channel.close();
                    }
                }
                return;
            }
            listener.progress(null, ++count_done, count_total);
            pending_tests.removeFirst().run();
            ITCFTest[] lst = active_tests.keySet().toArray(new ITCFTest[active_tests.size()]);
            for (ITCFTest test : lst) test.start();
        }
    }
}
