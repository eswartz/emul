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
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFFileInputStream;
import org.eclipse.tm.tcf.util.TCFFileOutputStream;


class TCFSelfTest {
    
    private final static int NUM_CHANNELS = 4;
    
    private final TestListener listener;
    private final IChannel[] channels;
    private final LinkedList<Runnable> pending_tests = new LinkedList<Runnable>(); 
    private final Map<Test,IChannel> active_tests = new HashMap<Test,IChannel>();
    private final Collection<Throwable> errors = new ArrayList<Throwable>();
    
    private int count_total;
    private int count_done;
    private boolean cancel;
    private boolean canceled;
    private boolean memory_lock;
    
    public interface TestListener {
        public void progress(String label, int done, int total);
        public void done(Collection<Throwable> errors);
    }
    
    @SuppressWarnings("serial")
    private static class CancelException extends Exception {
        CancelException() {
            super("Canceled");
        }
    }
    
    private interface Test {
    }
    
    TCFSelfTest(IPeer peer, TestListener listener) throws IOException {
        this.listener = listener;
        pending_tests.add(new Runnable() {
            public void run() {
                for (IChannel channel : channels) new TestEcho(channel);
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                int i = 0;
                for (IChannel channel : channels) new TestRCBP1(channel, i++);
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                for (IChannel channel : channels) new TestFileSystem(channel);
            }
        });
        pending_tests.add(new Runnable() {
            public void run() {
                for (int i = 0; i < channels.length; i++) {
                    switch (i % 3) {
                    case 0: new TestEcho(channels[i]); break;
                    case 1: new TestRCBP1(channels[i], i); break;
                    case 2: new TestFileSystem(channels[i]); break;
                    }
                }
            }
        });
        count_total = NUM_CHANNELS * pending_tests.size() * 2;
        channels = new IChannel[NUM_CHANNELS];
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
                    if (error == null && (!active_tests.isEmpty() || !pending_tests.isEmpty()) && !cancel) {
                        error = new IOException("Remote peer closed connection before all tests finished");
                    }
                    int cnt = 0;
                    for (int i = 0; i < channels.length; i++) {
                        if (channels[i] == channel) {
                            channels[i] = null;
                            if (error != null && !(error instanceof CancelException)) errors.add(error);
                            for (Iterator<Test> n = active_tests.keySet().iterator(); n.hasNext();) {
                                if (active_tests.get(n.next()) == channel) n.remove();
                            }
                        }
                        else if (channels[i] != null) {
                            cnt++;
                        }
                    }
                    if (cnt == 0) {
                        TCFSelfTest.this.listener.done(errors);
                    }
                    else if (active_tests.isEmpty()) {
                        for (int i = 0; i < channels.length; i++) {
                            if (channels[i] != null && channels[i].getState() != IChannel.STATE_CLOSED) {
                                if (errors.isEmpty()) channels[i].close();
                                else channels[i].terminate(new CancelException());
                            }
                        }
                    }
                }
            });
        }
    }
    
    void cancel() {
        cancel = true;
        if (canceled) return;
        for (final Test t : active_tests.keySet()) {
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
            if (c != null && c.getState() != IChannel.STATE_CLOSED) {
                c.terminate(new CancelException());
            }
        }
    }
    
    boolean isCanceled() {
        return canceled;
    }
    
    private class TestEcho implements Test, IDiagnostics.DoneEcho {

        private final IDiagnostics diag;
        private final LinkedList<String> msgs = new LinkedList<String>();
        private int count = 0;

        TestEcho(IChannel channel) {
            diag = channel.getRemoteService(IDiagnostics.class);
            listener.progress("Running Echo Test...", ++count_done, count_total);
            active_tests.put(this, channel);
            if (diag == null) {
                done(this);
            }
            else {
                for (int i = 0; i < 32; i++) sendMessage();
            }
        }
        
        private void sendMessage() {
            StringBuffer buf = new StringBuffer();
            buf.append(Integer.toHexString(count));
            for (int i = 0; i < 64; i++) {
                buf.append('-');
                buf.append((char)(0x400 * i + count));
            }
            String s =  buf.toString();
            msgs.add(s);
            diag.echo(s, this);
            count++;
        }
        
        public void doneEcho(IToken token, Throwable error, String b) {
            String s = msgs.removeFirst();
            if (active_tests.get(this) == null) return;
            if (error != null) {
                errors.add(error);
                done(this);
            }
            else if (!s.equals(b)) {
                errors.add(new Exception("Echo test failed: " + s + " != " + b));
                done(this);
            }
            else if (count < 0x400) {
                sendMessage();
            }
            else if (msgs.isEmpty()){
                done(this);
            }
        }
    }
    
    private class TestRCBP1 implements Test,
            IDiagnostics.DoneGetTestList, IDiagnostics.DoneRunTest,
            IRunControl.DoneGetContext, IRunControl.DoneGetChildren,
            IRunControl.DoneGetState, IRunControl.RunControlListener,
            IDiagnostics.DoneGetSymbol {
        
        private final int channel_id;
        private final IDiagnostics diag;
        private final IMemory mm;
        private final IRunControl rc;
        private final IRegisters rg;
        private final IBreakpoints bp;
        private final ILineNumbers ln;
        private final Map<String,IRunControl.RunControlContext> threads = new HashMap<String,IRunControl.RunControlContext>();
        private final Map<String,SuspendedContext> suspended = new HashMap<String,SuspendedContext>();
        private final Map<String,SuspendedContext> suspended_prev = new HashMap<String,SuspendedContext>();
        private final Set<String> running = new HashSet<String>();
        private final Map<IToken,String> get_state_cmds = new HashMap<IToken,String>();
        private final Map<String,Map<String,IRegisters.RegistersContext>> regs =
            new HashMap<String,Map<String,IRegisters.RegistersContext>>();
        
        private String context_id; // Test process context ID
        private IRunControl.RunControlContext context;
        private String main_thread_id;
        private Runnable pending_cancel;
        private ISymbol func0;
        private ISymbol func1;
        private ISymbol func2;
        private ISymbol array;
        private int bp_cnt = 0;
        private IToken cancel_test_cmd;

        private class SuspendedContext {
            final String id;
            final String pc;
            final String reason;
            final Map<String,Object> params;
            boolean resumed;
            
            SuspendedContext(String id, String pc, String reason, Map<String,Object> params) {
                this.id = id;
                this.pc = pc;
                this.reason = reason;
                this.params = params;
            }
        }

        TestRCBP1(IChannel channel, int channel_id) {
            this.channel_id = channel_id;
            diag = channel.getRemoteService(IDiagnostics.class);
            mm = channel.getRemoteService(IMemory.class);
            rc = channel.getRemoteService(IRunControl.class);
            rg = channel.getRemoteService(IRegisters.class);
            bp = channel.getRemoteService(IBreakpoints.class);
            ln = channel.getRemoteService(ILineNumbers.class);
            active_tests.put(this, channel);
            listener.progress("Running Run Control Test...", ++count_done, count_total);
            if (diag == null || rc == null) {
                done(this);
            }
            else if (bp == null) {
                exit(new Exception("Remote Breakpoints service not found"));
            }
            else {
                diag.getTestList(this);
            }
        }
        
        public void doneGetTestList(IToken token, Throwable error, String[] list) {
            assert active_tests.get(this) != null;
            if (error != null) {
                exit(error);
            }
            else {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].equals("RCBP1")) {
                        diag.runTest("RCBP1", this);
                        return;
                    }
                }
            }
            exit(null);
        }
        
        public void doneRunTest(IToken token, Throwable error, String context_id) {
            if (error != null) {
                exit(error);
            }
            else {
                assert active_tests.get(this) != null;
                assert this.context_id == null;
                this.context_id = context_id;
                if (pending_cancel != null) {
                    Protocol.invokeLater(pending_cancel);
                    pending_cancel = null;
                }
                else {
                    diag.getSymbol(context_id, "tcf_test_func0", this);
                    diag.getSymbol(context_id, "tcf_test_func1", this);
                    diag.getSymbol(context_id, "tcf_test_func2", this);
                    diag.getSymbol(context_id, "tcf_test_array", this);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void doneGetSymbol(IToken token, Throwable error, ISymbol symbol) {
            if (error != null) {
                exit(error);
                return;
            }
            assert active_tests.get(this) != null;
            assert this.context_id != null;
            if (!symbol.isGlobal()) {
                exit(new Exception("Symbols 'tcf_test_*' must be global"));
            }
            else if (!symbol.isAbs()) {
                exit(new Exception("Symbols 'tcf_test_*' must be absolute"));
            }
            else if (func0 == null) {
                func0 = symbol;
            }
            else if (func1 == null) {
                func1 = symbol;
            }
            else if (func2 == null) {
                func2 = symbol;
            }
            else {
                array = symbol;
                Map<String,Object> m[] = new Map[4];
                for (int i = 0; i < m.length; i++) {
                    m[i] = new HashMap();
                    m[i].put(IBreakpoints.PROP_ID, "TcfTestBP" + i);
                    m[i].put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                    switch (i) {
                    case 0:
                        m[i].put(IBreakpoints.PROP_ADDRESS, func0.getValue().toString());
                        m[i].put(IBreakpoints.PROP_CONDITION, "$thread!=\"\"");
                        break;
                    case 1:
                        m[i].put(IBreakpoints.PROP_ADDRESS, "(31+1)/16+tcf_test_func1-2");
                        m[i].put(IBreakpoints.PROP_CONDITION, "tcf_test_func0!=tcf_test_func1");
                        break;
                    case 2:
                        m[i].put(IBreakpoints.PROP_ADDRESS, "tcf_test_func2");
                        m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                        break;
                    case 3:
                        m[i].put(IBreakpoints.PROP_ID, "TcfTestBP3" + channel_id);
                        m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                        m[i].put(IBreakpoints.PROP_ADDRESS, "tcf_test_func2");
                        break;
                    }
                }
                bp.set(m, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            rc.getContext(context_id, TestRCBP1.this);
                        }
                    }
                });
            }
        }

        public void doneGetContext(IToken token, Exception error, RunControlContext context) {
            if (cancel) return;
            if (error != null) {
                exit(error);
            }
            else {
                if (this.context == null) {
                    this.context = context;
                    assert context_id.equals(context.getID());
                    assert threads.isEmpty();
                    assert running.isEmpty();
                    assert suspended.isEmpty();
                    rc.addListener(this);
                }
                rc.getChildren(context.getID(), this);
                if (context.hasState()) {
                    threads.put(context.getID(), context);
                    get_state_cmds.put(context.getState(this), context.getID());
                }
            }
        }

        public void doneGetChildren(IToken token, Exception error, String[] contexts) {
            if (cancel) return;
            if (error != null) {
                exit(error);
            }
            else {
                for (String id : contexts) rc.getContext(id, this);
            }
        }

        public void doneGetState(IToken token, Exception error,
                boolean suspended, String pc, String reason,
                Map<String, Object> params) {
            final String id = get_state_cmds.remove(token);
            if (cancel) return;
            if (id == null) {
                exit(new Exception("Invalid getState responce"));
            }
            else if (!suspended) {
                if (this.suspended.get(id) != null) {
                    exit(new Exception("Invalid result of getState command"));
                }
                else {
                    running.add(id);
                }
            }
            else {
                assert threads.get(id) != null;
                if (running.contains(id)) {
                    exit(new Exception("Invalid result of getState command"));
                }
                else {
                    SuspendedContext sc = this.suspended.get(id);
                    if (sc != null) {
                        if (!sc.pc.equals(pc) || !sc.reason.equals(reason)) {
                            exit(new Exception("Invalid result of getState command"));
                        }
                        else {
                            resume(sc);
                        }
                    }
                    else {
                        if (main_thread_id != null) {
                            exit(new Exception("Missing contextSuspended event for " + id));
                        }
                        else if ("Breakpoint".equals(reason)) {
                            exit(new Exception("Invalid suspend reason of main thread after test start: " + reason + " " + pc));
                        }
                        else {
                            main_thread_id = id;
                            final SuspendedContext sx = new SuspendedContext(id, pc, reason, params);
                            this.suspended.put(id, sx);
                            final String bp_id = "TcfTestBP3" + channel_id;
                            Map<String,Object> m = new HashMap<String,Object>();
                            m.put(IBreakpoints.PROP_ID, bp_id);
                            m.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                            m.put(IBreakpoints.PROP_ADDRESS, "tcf_test_func2");
                            m.put(IBreakpoints.PROP_CONDITION, "$thread==\"" + id + "\"");
                            bp.change(m, new IBreakpoints.DoneCommand() {
                                public void doneCommand(IToken token, Exception error) {
                                    if (error != null) exit(error);
                                }
                            });
                            Protocol.sync(new Runnable() {
                                public void run() {
                                    bp.enable(new String[]{ bp_id }, new IBreakpoints.DoneCommand() {
                                        public void doneCommand(IToken token, Exception error) {
                                            if (error != null) exit(error);
                                        }
                                    });
                                    resume(sx);
                                }
                            });
                        }
                    }
                }
            }
        }
        
        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) contextResumed(id);
        }

        public void containerSuspended(String context, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            for (String id : suspended_ids) {
                if (id.equals(context)) continue;
                contextSuspended(id, null, null, null);
            }
            contextSuspended(context, pc, reason, params);
        }

        public void contextAdded(RunControlContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                if (threads.get(contexts[i].getID()) != null) {
                    exit(new Exception("Invalid contextAdded event"));
                    return;
                }
                if (context.getID().equals(contexts[i].getProperties().get(IRunControl.PROP_PROCESS_ID))) {
                    threads.put(contexts[i].getID(), contexts[i]);
                    running.add(contexts[i].getID());
                }
            }
        }

        public void contextChanged(RunControlContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                if (contexts[i].getID().equals(context.getID())) {
                    context = contexts[i];
                }
                if (context.getID().equals(contexts[i].getProperties().get(IRunControl.PROP_PROCESS_ID))) {
                    threads.put(contexts[i].getID(), contexts[i]);
                }
            }
        }

        public void contextException(String context, String msg) {
            if (context.equals(this.context.getID()) || threads.get(context) != null) {
                exit(new Exception(msg));
            }
        }

        public void contextRemoved(String[] contexts) {
            for (String id : contexts) {
                if (suspended.get(id) != null) {
                    exit(new Exception("Invalid contextRemoved event"));
                    return;
                }
                threads.remove(id);
                running.remove(id);
                if (threads.isEmpty()) {
                    if (bp_cnt != 30) {
                        exit(new Exception("Test main thread breakpoint count = " + bp_cnt + ", expected 30"));
                    }
                    rc.removeListener(this);
                    // Flush communication channel of pending commands 
                    Protocol.sync(new Runnable() {
                        public void run() {
                            exit(null);
                        }
                    });
                }
            }
        }

        public void contextResumed(String id) {
            if (threads.get(id) == null) return;
            SuspendedContext sc = suspended.remove(id);
            if (!isAlienBreakpoint(sc)) suspended_prev.put(id, sc);
            running.add(id);
        }
        
        private String toSymName(long addr) {
            if (func0.getValue().longValue() == addr) return "tcf_test_func0";
            if (func1.getValue().longValue() == addr) return "tcf_test_func1";
            if (func2.getValue().longValue() == addr) return "tcf_test_func2";
            return "*no name*";
        }
        
        private void checkSuspendedContext(SuspendedContext sp, ISymbol sym) {
            long pc =  Long.parseLong(sp.pc);
            if (pc != sym.getValue().longValue() || !"Breakpoint".equals(sp.reason)) {
                exit(new Exception("Invalid contextSuspended event: " + sp.id + " '" + toSymName(pc) + "' " + sp.pc + " " + sp.reason +
                        ", expected breakpoint at '" + toSymName(sym.getValue().longValue()) + "' " + sym.getValue()));
            }
        }
        
        private boolean isAlienBreakpoint(SuspendedContext sc) {
            // Check if context suspended by a breakpoint from another debug session
            // Test should ignore such breakpoints.
            if (!"Breakpoint".equals(sc.reason)) return false;
            long pc =  Long.parseLong(sc.pc);
            if (pc == func0.getValue().longValue()) return false;
            if (pc == func1.getValue().longValue()) return false;
            if (pc == func2.getValue().longValue()) return false;
            return true;
        }

        public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
            if (threads.get(id) == null) return;
            assert main_thread_id != null;
            running.remove(id);
            SuspendedContext sc = suspended.get(id);
            if (sc != null) {
                if (!sc.pc.equals(pc) || !sc.reason.equals(reason)) {
                    exit(new Exception("Invalid contextSuspended event"));
                }
            }
            else {
                sc = new SuspendedContext(id, pc, reason, params);
                suspended.put(id, sc);
                if (!isAlienBreakpoint(sc)) {
                    if ("Breakpoint".equals(reason) && id.equals(main_thread_id)) bp_cnt++;
                    SuspendedContext sp = suspended_prev.get(id);
                    if (sp != null) {
                        if (Long.parseLong(sc.pc) == func2.getValue().longValue()) {
                            checkSuspendedContext(sp, func1);
                        }
                        else if (Long.parseLong(sc.pc) == func1.getValue().longValue()) {
                            checkSuspendedContext(sp, func0);
                        }
                        else if (Long.parseLong(sc.pc) == func0.getValue().longValue()) {
                            if (id.equals(main_thread_id)) {
                                if ("Breakpoint".equals(sp.reason)) {
                                    checkSuspendedContext(sp, func2);
                                }
                            }
                            else {
                                checkSuspendedContext(sp, func1);
                            }
                        }
                    }
                }
            }
            final SuspendedContext sc0 = sc;
            ILineNumbers.DoneMapToSource ln_done = new ILineNumbers.DoneMapToSource() {
                public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                    if (error != null) exit(error);
                    if (mm != null) runMemoryTest(sc0);
                    else if (rg != null) runRegistersTest(sc0);
                    else resume(sc0);
                }
            };
            if (ln != null) {
                BigInteger x = new BigInteger(pc);
                BigInteger y = x.add(BigInteger.valueOf(1));
                ln.mapToSource(id, x, y, ln_done);
            }
            else {
                ln_done.doneMapToSource(null, null, null);
            }
        }
        
        private void resume(final SuspendedContext sc) {
            IRunControl.RunControlContext ctx = threads.get(sc.id);
            if (ctx != null && !sc.resumed) {
                sc.resumed = true;
                ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (cancel) return;
                        if (active_tests.get(this) == null) return;
                        if (threads.get(sc.id) == null) return;
                        if (error != null) exit(error);
                    }
                });
            }
        }
        
        private void runMemoryTest(final SuspendedContext sc) {
            if (memory_lock) {
                resume(sc);
                return;
            }
            memory_lock = true;
            mm.getContext(context_id, new IMemory.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, final MemoryContext mem_ctx) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    if (!context_id.equals(mem_ctx.getID())) {
                        exit(new Exception("Bad memory context data: invalid ID"));
                    }
                    Object pid = context.getProperties().get(IRunControl.PROP_PROCESS_ID);
                    if (pid != null && !pid.equals(mem_ctx.getProperties().get(IMemory.PROP_PROCESS_ID))) {
                        exit(new Exception("Bad memory context data: invalid ProcessID"));
                    }
                    final boolean big_endian = mem_ctx.isBigEndian();
                    final int addr_size = mem_ctx.getAddressSize();
                    final byte[] buf = new byte[0x1000];
                    mem_ctx.get(array.getValue(), 1, buf, 0, addr_size, 0, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            byte[] tmp = new byte[addr_size + 1];
                            tmp[0] = 0; // Extra byte to avoid sign extension by BigInteger
                            if (big_endian) {
                                System.arraycopy(buf, 0, tmp, 1, addr_size);
                            }
                            else {
                                for (int i = 0; i < addr_size; i++) {
                                    tmp[i + 1] = buf[addr_size - i - 1];
                                }
                            }
                            Number mem_address = new BigInteger(tmp);
                            testSetMemoryCommand(sc, mem_ctx, mem_address, buf);
                        }
                    });
                }
            });
        }
        
        private void testSetMemoryCommand(final SuspendedContext sc,
                final IMemory.MemoryContext mem_ctx,
                final Number addr, final byte[] buf) {
            final byte[] data = new byte[buf.length];
            new Random().nextBytes(data);
            mem_ctx.set(addr, 1, data, 0, data.length, 0, new IMemory.DoneMemory() {
                public void doneMemory(IToken token, MemoryError error) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    mem_ctx.get(addr, 1, buf, 0, buf.length, 0, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            if (error != null) {
                                exit(error);
                                return;
                            }
                            for (int i = 0; i < data.length; i++) {
                                if (data[i] != buf[i]) {
                                    exit(new Exception(
                                            "Invalid Memory.get responce: wrong data at offset " + i +
                                            ", expected " + data[i] + ", actual " + buf[i]));
                                    return;
                                }
                            }
                            testFillMemoryCommand(sc, mem_ctx, addr, buf);
                        }
                    });
                }
            });
        }
        
        private void testFillMemoryCommand(final SuspendedContext sc,
                final IMemory.MemoryContext mem_ctx,
                final Number addr, final byte[] buf) {
            final byte[] data = new byte[buf.length / 7];
            new Random().nextBytes(data);
            mem_ctx.fill(addr, 1, data, buf.length, 0, new IMemory.DoneMemory() {
                public void doneMemory(IToken token, MemoryError error) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    mem_ctx.get(addr, 1, buf, 0, buf.length, 0, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            if (error != null) {
                                exit(error);
                                return;
                            }
                            for (int i = 0; i < data.length; i++) {
                                if (data[i % data.length] != buf[i]) {
                                    exit(new Exception(
                                            "Invalid Memory.get responce: wrong data at offset " + i +
                                            ", expected " + data[i % data.length] + ", actual " + buf[i]));
                                    return;
                                }
                            }
                            memory_lock = false;
                            if (rg != null) runRegistersTest(sc);
                            else resume(sc);
                        }
                    });
                }
            });
        }
        
        private void runRegistersTest(final SuspendedContext sc) {
            if (regs.get(sc.id) == null) {
                final Map<String,IRegisters.RegistersContext> reg_map =
                    new HashMap<String,IRegisters.RegistersContext>();
                final Set<IToken> cmds = new HashSet<IToken>();
                regs.put(sc.id, reg_map);
                cmds.add(rg.getChildren(sc.id, new IRegisters.DoneGetChildren() {
                    public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                        cmds.remove(token);
                        if (error != null) {
                            for (IToken t : cmds) t.cancel();
                            exit(error);
                            return;
                        }
                        for (final String id : context_ids) {
                            cmds.add(rg.getChildren(id, this));
                            cmds.add(rg.getContext(id, new IRegisters.DoneGetContext() {
                                public void doneGetContext(IToken token,
                                        Exception error,
                                        RegistersContext context) {
                                    cmds.remove(token);
                                    if (error != null) {
                                        for (IToken t : cmds) t.cancel();
                                        exit(error);
                                        return;
                                    }
                                    reg_map.put(id, context);
                                    if (cmds.isEmpty()) {
                                        testGetSetRegisterCommands(sc);
                                    }
                                }
                            }));
                        }
                    }
                }));
            }
            else {
                testGetSetRegisterCommands(sc);
            }
        }
        
        private void testGetSetRegisterCommands(final SuspendedContext sc) {
            final Set<IToken> cmds = new HashSet<IToken>();
            Map<String,IRegisters.RegistersContext> reg_map = regs.get(sc.id);
            for (final IRegisters.RegistersContext ctx : reg_map.values()) {
                if (!ctx.isReadable()) continue;
                if (ctx.isReadOnce()) continue;
                String[] fmts = ctx.getAvailableFormats();
                for (final String fmt : fmts) {
                    cmds.add(ctx.get(fmt, new IRegisters.DoneGet() {
                        public void doneGet(IToken token, Exception error, String value) {
                            cmds.remove(token);
                            if (error != null) {
                                for (IToken t : cmds) t.cancel();
                                exit(error);
                                return;
                            }
                            cmds.add(ctx.set(fmt, value, new IRegisters.DoneSet() {
                                public void doneSet(IToken token, Exception error) {
                                    cmds.remove(token);
                                    if (error != null) {
                                        for (IToken t : cmds) t.cancel();
                                        exit(error);
                                        return;
                                    }
                                    if (cmds.isEmpty()) {
                                        resume(sc);
                                    }
                                }
                            }));
                        }
                    }));
                }
            }
            if (cmds.isEmpty()) {
                resume(sc);
            }
        }
        
        void cancel(final Runnable done) {
            if (rc != null) rc.removeListener(this);
            if (context_id == null) {
                if (pending_cancel != null) {
                    exit(null);
                }
                else {
                    pending_cancel = done;
                }
            }
            else if (cancel_test_cmd == null) {
                cancel_test_cmd = diag.cancelTest(context_id, new IDiagnostics.DoneCancelTest() {
                    public void doneCancelTest(IToken token, Throwable error) {
                        cancel_test_cmd = null;
                        exit(error);
                        done.run();
                    }
                });
            }
            else {
                exit(new Exception("Cannot terminate remote test process"));
                done.run();
            }
        }

        private void exit(Throwable x) {
            if (active_tests.get(this) == null) return;
            if (pending_cancel != null) {
                Protocol.invokeLater(pending_cancel);
                pending_cancel = null;
            }
            else {
                if (x != null) errors.add(x);
                if (rc != null) rc.removeListener(this);
            }
            done(this);
        }
    }
    
    private int file_count = 0;

    private class TestFileSystem implements Test, IFileSystem.DoneStat,
            IFileSystem.DoneOpen, IFileSystem.DoneClose,
            IFileSystem.DoneWrite, IFileSystem.DoneRead,
            IFileSystem.DoneRename, IFileSystem.DoneRealPath,
            IFileSystem.DoneRemove, IFileSystem.DoneRoots,
            IFileSystem.DoneReadDir {
        
        private static final int
            STATE_PRE = 0,
            STATE_WRITE = 1,
            STATE_READ = 2,
            STATE_OUT = 3,
            STATE_INP = 4,
            STATE_EXIT = 5;
        
        private final IFileSystem files;
        private final byte[] data = new byte[0x1000];
        private String root;
        private String tmp_path;
        private String file_name;
        private IFileHandle handle;
        private int state = STATE_PRE;
        
        TestFileSystem(IChannel channel) {
            files = channel.getRemoteService(IFileSystem.class);
            active_tests.put(this, channel);
            listener.progress("Running File System Test...", ++count_done, count_total);
            if (files == null) {
                done(this);
            }
            else {
                files.roots(this);
            }
        }

        public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
            assert state == STATE_PRE;
            if (error != null) {
                error(error);
            }
            else if (entries == null || entries.length == 0) {
                error(new Exception("Invalid FileSysrem.roots responce: empty roots array"));
            }
            else {
                root = entries[0].filename;
                files.opendir(root, this);
            }
        }

        public void doneReadDir(IToken token, FileSystemException error,
                DirEntry[] entries, boolean eof) {
            assert state == STATE_PRE;
            if (error != null) {
                error(error);
            }
            else {
                if (entries != null && tmp_path == null) {
                    for (DirEntry e : entries) {
                        if (e.filename.equals("tmp") || e.filename.equalsIgnoreCase("temp")) {
                            tmp_path = root + "/" + e.filename;
                            break;
                        }
                    }
                }
                if (eof) {
                    if (tmp_path == null) {
                        error(new Exception("File system test filed: cannot find temporary directory"));
                        return;
                    }
                    files.close(handle, this);
                }
                else {
                    files.readdir(handle, this);
                }
            }
        }

        public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
            if (error != null) {
                error(error);
            }
            else if (state == STATE_READ) {
                if (attrs.size != data.length) {
                    error(new Exception("Invalid FileSysrem.fstat responce: wrong file size"));
                }
                else {
                    files.close(handle, this);
                }
            }
            else {
                file_name = tmp_path + "/tcf-test-" + (file_count++) + ".tmp";
                files.open(file_name, IFileSystem.O_CREAT | IFileSystem.O_TRUNC | IFileSystem.O_WRITE, null, this);
            }
        }

        public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
            if (error != null) {
                error(error);
            }
            else {
                this.handle = handle;
                if (state == STATE_READ) {
                    files.read(handle, 0, data.length + 1, this);
                }
                else if (state == STATE_WRITE) {
                    new Random().nextBytes(data);
                    files.write(handle, 0, data, 0, data.length, this);
                }
                else if (state == STATE_INP) {
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                InputStream inp = new TCFFileInputStream(handle);
                                int i = 0;
                                for (;;) {
                                    int ch = inp.read();
                                    if (ch < 0) break;
                                    int dt = data[i % data.length] & 0xff;
                                    if (ch != dt) {
                                        error(new Exception("Invalid TCFFileInputStream.read responce: wrong data at offset " + i +
                                                ", expected " + dt + ", actual " + ch));
                                    }
                                    i++;
                                }
                                if (i != data.length * 16) {
                                    error(new Exception("Invalid TCFFileInputStream.read responce: wrong file length: " +
                                            "expected " + data.length + ", actual " + i));
                                }
                                inp.close();
                                Protocol.invokeLater(new Runnable() {
                                    public void run() {
                                        state = STATE_EXIT;
                                        files.rename(file_name, file_name + ".rnm", TestFileSystem.this);
                                    }
                                });
                            }
                            catch (Throwable x) {
                                error(x);
                            }
                        }
                        private void error(final Throwable x) {
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    TestFileSystem.this.error(x);
                                }
                            });
                        }
                    };
                    thread.setName("TCF FileSystem Test");
                    thread.start();
                }
                else if (state == STATE_OUT) {
                    new Random().nextBytes(data);
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                OutputStream out = new TCFFileOutputStream(handle);
                                for (int i = 0; i < data.length * 16; i++) {
                                    out.write(data[i % data.length] & 0xff);
                                }
                                out.close();
                                Protocol.invokeLater(new Runnable() {
                                    public void run() {
                                        state = STATE_INP;
                                        files.open(file_name, IFileSystem.O_READ, null, TestFileSystem.this);
                                    }
                                });
                            }
                            catch (Throwable x) {
                                error(x);
                            }
                        }
                        private void error(final Throwable x) {
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    TestFileSystem.this.error(x);
                                }
                            });
                        }
                    };
                    thread.setName("TCF FileSystem Test");
                    thread.start();
                }
                else {
                    assert state == STATE_PRE;
                    files.readdir(handle, this);
                }
            }
        }

        public void doneWrite(IToken token, FileSystemException error) {
            if (error != null) {
                error(error);
            }
            else {
                files.close(handle, this);
            }
        }

        public void doneRead(IToken token, FileSystemException error, byte[] data, boolean eof) {
            if (error != null) {
                error(error);
            }
            else if (!eof) {
                error(new Exception("Invalid FileSysrem.read responce: EOF expected"));
            }
            else if (data.length != this.data.length) {
                error(new Exception("Invalid FileSysrem.read responce: wrong data array size"));
            }
            else {
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != this.data[i]) {
                        error(new Exception("Invalid FileSysrem.read responce: wrong data at offset " + i +
                                ", expected " + this.data[i] + ", actual " + data[i]));
                        return;
                    }
                }
                files.fstat(handle, this);
            }
        }

        public void doneClose(IToken token, FileSystemException error) {
            if (error != null) {
                error(error);
            }
            else {
                handle = null;
                if (state == STATE_PRE) {
                    files.realpath(tmp_path, this);
                }
                else if (state == STATE_WRITE) {
                    state = STATE_READ;
                    files.open(file_name, IFileSystem.O_READ, null, this);
                }
                else if (state == STATE_READ) {
                    state = STATE_OUT;
                    files.open(file_name, IFileSystem.O_WRITE, null, this);
                }
                else {
                    assert false;
                }
            }
        }

        public void doneRename(IToken token, FileSystemException error) {
            assert state == STATE_EXIT;
            if (error != null) {
                error(error);
            }
            else {
                files.realpath(file_name + ".rnm", this);
            }
        }

        public void doneRealPath(IToken token, FileSystemException error, String path) {
            if (error != null) {
                error(error);
            }
            else if (state == STATE_PRE) {
                state = STATE_WRITE;
                tmp_path = path;
                files.stat(tmp_path, this);
            }
            else if (!path.equals(file_name + ".rnm")) {
                error(new Exception("Invalid FileSysrem.realpath responce: " + path));
            }
            else {
                files.remove(file_name + ".rnm", this);
            }
        }

        public void doneRemove(IToken token, FileSystemException error) {
            assert state == STATE_EXIT;
            if (error != null) {
                error(error);
            }
            else {
                done(this);
            }
        }

        private void error(Throwable x) {
            if (active_tests.get(this) == null) return;
            errors.add(x);
            done(this);
        }
    }
    
    private void done(Test test) {
        assert active_tests.get(test) != null;
        active_tests.remove(test);
        listener.progress(null, ++count_done, count_total);
        if (active_tests.isEmpty()) runNextTest();
    }

    private void runNextTest() {
        while (active_tests.isEmpty()) {
            if (cancel || errors.size() > 0 || pending_tests.size() == 0) {
                for (IChannel channel : channels) {
                    if (channel != null && channel.getState() != IChannel.STATE_CLOSED) {
                        if (errors.isEmpty()) channel.close();
                        else channel.terminate(new CancelException());
                    }
                }
                return;
            }
            pending_tests.removeFirst().run();
        }
    }
}
