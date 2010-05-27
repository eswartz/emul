/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.HashSet;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IStreams;

class TestStreams implements ITCFTest, IStreams.StreamsListener {

    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;
    private final IStreams streams;
    private final Random rnd = new Random();
    private final HashSet<String> stream_ids = new HashSet<String>();

    private String inp_id;
    private String out_id;

    private int test_count;
    private long start_time;

    TestStreams(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
        streams = channel.getRemoteService(IStreams.class);
    }

    public void start() {
        if (diag == null ||streams == null) {
            test_suite.done(this, null);
        }
        else {
            start_time = System.currentTimeMillis();
            connect();
        }
    }

    private void connect() {
        diag.createTestStreams(1001, 771, new IDiagnostics.DoneCreateTestStreams() {
            public void doneCreateTestStreams(IToken token, Throwable error, final String inp_id, final String out_id) {
                if (error != null) {
                    exit(error);
                }
                else {
                    TestStreams.this.inp_id = inp_id;
                    TestStreams.this.out_id = out_id;
                    if (stream_ids.size() != 0) {
                        exit(new Exception("Stream events without subscription"));
                        return;
                    }
                    streams.connect(inp_id, new IStreams.DoneConnect() {
                        public void doneConnect(IToken token, Exception error) {
                            if (error != null) {
                                exit(error);
                            }
                            else {
                                // write some data (zeros)
                                // this data can be dropped by Streams since we are not connected yet
                                final byte[] data_out = new byte[rnd.nextInt(10000) + 1000];
                                IStreams.DoneWrite done_write = new IStreams.DoneWrite() {
                                    public void doneWrite(IToken token, Exception error) {
                                        if (error != null) exit(error);
                                    }
                                };
                                int offs = 0;
                                while (offs < data_out.length) {
                                    int size = rnd.nextInt(400);
                                    if (size > data_out.length - offs) size = data_out.length - offs;
                                    streams.write(inp_id, data_out, offs, size, done_write);
                                    offs += size;
                                }
                                streams.connect(out_id, new IStreams.DoneConnect() {
                                    public void doneConnect(IToken token, Exception error) {
                                        if (error != null) {
                                            exit(error);
                                        }
                                        else {
                                            testReadWrite(true, new Runnable() {
                                                public void run() {
                                                    TestStreams.this.inp_id = null;
                                                    TestStreams.this.out_id = null;
                                                    subscribe();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void subscribe() {
        streams.subscribe(IDiagnostics.NAME, this, new IStreams.DoneSubscribe() {
            public void doneSubscribe(IToken token, Exception error) {
                if (error != null) {
                    exit(error);
                }
                else {
                    createStreams();
                }
            }
        });
    }

    private void createStreams() {
        diag.createTestStreams(1153, 947, new IDiagnostics.DoneCreateTestStreams() {
            public void doneCreateTestStreams(IToken token, Throwable error, String inp_id, String out_id) {
                if (error != null) {
                    exit(error);
                }
                else {
                    TestStreams.this.inp_id = inp_id;
                    TestStreams.this.out_id = out_id;
                    for (String id : stream_ids) {
                        if (id.equals(inp_id)) continue;
                        if (id.equals(out_id)) continue;
                        streams.disconnect(id, new IStreams.DoneDisconnect() {
                            public void doneDisconnect(IToken token, Exception error) {
                                if (error != null) exit(error);
                            }
                        });
                    }
                    testReadWrite(false, new Runnable() {
                        public void run() {
                            unsubscribe();
                        }
                    });
                }
            }
        });
    }

    private void testReadWrite(final boolean skip_zeros, final Runnable done) {
        final byte[] data_out = new byte[rnd.nextInt(10000) + 1000];
        new Random().nextBytes(data_out);
        if (skip_zeros) data_out[0] = 1;
        final HashSet<IToken> cmds = new HashSet<IToken>();
        IStreams.DoneRead done_read = new IStreams.DoneRead() {

            private int offs = 0;
            private boolean eos;

            public void doneRead(IToken token, Exception error, int lost_size, byte[] data, boolean eos) {
                cmds.remove(token);
                if (error != null) {
                    if (!this.eos) exit(error);
                }
                else if (lost_size != 0) {
                    exit(new Exception("Streams service: unexpected data loss"));
                }
                else if (this.eos) {
                    if (!eos || data != null && data.length > 0) {
                        exit(new Exception("Streams service: unexpected successful read after EOS"));
                    }
                }
                else {
                    if (data != null) {
                        if (offs + data.length > data_out.length) {
                            exit(new Exception("Streams service: read returns more data then expected"));
                            return;
                        }
                        for (int n = 0; n < data.length; n++) {
                            if (!skip_zeros || offs > 0 || data[n] != 0) {
                                if (data[n] != data_out[offs]) {
                                    exit(new Exception("Streams service: data error: " + data[n] + " != " + data_out[offs]));
                                    return;
                                }
                                offs++;
                            }
                        }
                    }
                    if (eos) {
                        if (offs != data_out.length) {
                            exit(new Exception("Streams service: unexpected EOS"));
                        }
                        this.eos = true;
                    }
                    else if (cmds.size() < 8) {
                        cmds.add(streams.read(out_id, 241, this));
                    }
                }
                if (cmds.isEmpty()) disposeStreams(done);
            }
        };
        cmds.add(streams.read(out_id, 223, done_read));
        cmds.add(streams.read(out_id, 227, done_read));
        cmds.add(streams.read(out_id, 229, done_read));
        cmds.add(streams.read(out_id, 233, done_read));

        IStreams.DoneWrite done_write = new IStreams.DoneWrite() {
            public void doneWrite(IToken token, Exception error) {
                if (error != null) exit(error);
            }
        };
        int offs = 0;
        while (offs < data_out.length) {
            int size = rnd.nextInt(400);
            if (size > data_out.length - offs) size = data_out.length - offs;
            streams.write(inp_id, data_out, offs, size, done_write);
            offs += size;
        }
        streams.eos(inp_id, new IStreams.DoneEOS() {
            public void doneEOS(IToken token, Exception error) {
                if (error != null) exit(error);
            }
        });
    }

    private void disposeStreams(final Runnable done) {
        final HashSet<IToken> cmds = new HashSet<IToken>();
        IStreams.DoneDisconnect done_disconnect = new IStreams.DoneDisconnect() {
            public void doneDisconnect(IToken token, Exception error) {
                if (error != null) {
                    exit(error);
                }
                else {
                    cmds.remove(token);
                    if (cmds.isEmpty() && test_suite.isActive(TestStreams.this)) done.run();
                }
            }
        };
        IDiagnostics.DoneDisposeTestStream done_dispose = new IDiagnostics.DoneDisposeTestStream() {
            public void doneDisposeTestStream(IToken token, Throwable error) {
                if (error != null) {
                    exit(error);
                }
                else {
                    cmds.remove(token);
                    if (cmds.isEmpty() && test_suite.isActive(TestStreams.this)) done.run();
                }
            }
        };
        cmds.add(streams.disconnect(inp_id, done_disconnect));
        cmds.add(diag.disposeTestStream(inp_id, done_dispose));
        cmds.add(diag.disposeTestStream(out_id, done_dispose));
        cmds.add(streams.disconnect(out_id, done_disconnect));
    }

    private void unsubscribe() {
        streams.unsubscribe(IDiagnostics.NAME, this, new IStreams.DoneUnsubscribe() {
            public void doneUnsubscribe(IToken token, Exception error) {
                if (error != null || test_count >= 10 || System.currentTimeMillis() - start_time >= 4000) {
                    exit(error);
                }
                else {
                    test_count++;
                    stream_ids.clear();
                    inp_id = null;
                    out_id = null;
                    connect();
                }
            }
        });
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        test_suite.done(this, x);
    }

    /************************** StreamsListener **************************/

    public void created(String stream_type, String stream_id, String context_id) {
        if (!IDiagnostics.NAME.equals(stream_type)) exit(new Exception("Invalid stream type in Streams.created event"));
        if (stream_ids.contains(stream_id)) exit(new Exception("Invalid stream ID in Streams.created event"));
        stream_ids.add(stream_id);
        if (inp_id != null) {
            if (inp_id.equals(stream_id)) exit(new Exception("Invalid stream ID in Streams.created event"));
            if (out_id.equals(stream_id)) exit(new Exception("Invalid stream ID in Streams.created event"));
            streams.disconnect(stream_id, new IStreams.DoneDisconnect() {
                public void doneDisconnect(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                }
            });
        }
    }

    public void disposed(String stream_type, String stream_id) {
        if (!IDiagnostics.NAME.equals(stream_type)) exit(new Exception("Invalid stream type in Streams.disposed event"));
        if (!stream_ids.remove(stream_id)) exit(new Exception("Invalid stream ID in Streams.disposed event"));
    }
}
