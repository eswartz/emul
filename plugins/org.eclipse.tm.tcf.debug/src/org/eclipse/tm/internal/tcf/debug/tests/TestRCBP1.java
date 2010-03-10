/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;

class TestRCBP1 implements ITCFTest,
        IDiagnostics.DoneGetTestList, IDiagnostics.DoneRunTest,
        IRunControl.DoneGetContext, IRunControl.DoneGetChildren,
        IRunControl.DoneGetState, IRunControl.RunControlListener,
        IDiagnostics.DoneGetSymbol {

    private final TCFTestSuite test_suite;
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
    private final Map<String,Map<String,Object>> bp_list = new HashMap<String,Map<String,Object>>();
    private final Random rnd = new Random();

    private String context_id; // Test process context ID
    private IRunControl.RunControlContext context;
    private String main_thread_id;
    private Runnable pending_cancel;
    private ISymbol func0;
    private ISymbol func1;
    private ISymbol func2;
    private ISymbol array;
    private int bp_cnt = 0;
    private boolean done_starting_test_process;
    private int resume_cnt = 0;
    private IToken cancel_test_cmd;
    private boolean bp_set_done;
    private boolean bp_change_done;

    private class SuspendedContext {
        final String id;
        final String pc;
        final String reason;
        final Map<String,Object> params;

        SuspendedContext(String id, String pc, String reason, Map<String,Object> params) {
            this.id = id;
            this.pc = pc;
            this.reason = reason;
            this.params = params;
        }
    }

    private final IBreakpoints.BreakpointsListener bp_listener = new IBreakpoints.BreakpointsListener() {

        public void breakpointStatusChanged(String id, Map<String,Object> status) {
        }

        public void contextAdded(Map<String,Object>[] bps) {
            for (Map<String,Object> m0 : bps) {
                String id = (String)m0.get(IBreakpoints.PROP_ID);
                Map<String,Object> m1 = bp_list.get(id);
                if (!checkBPData(m0, m1)) return;
            }
        }

        public void contextChanged(Map<String,Object>[] bps) {
            for (Map<String,Object> m0 : bps) {
                String id = (String)m0.get(IBreakpoints.PROP_ID);
                Map<String,Object> m1 = bp_list.get(id);
                if (!checkBPData(m0, m1)) return;
            }
        }

        public void contextRemoved(String[] ids) {
        }

        private boolean checkBPData(Map<String,Object> m0, Map<String,Object> m1) {
            if (m1 == null) return true;
            m0 = new HashMap<String,Object>(m0);
            if (m0.get(IBreakpoints.PROP_ENABLED) == null) m0.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
            if (m1.get(IBreakpoints.PROP_ENABLED) == null) m1.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
            if (!m1.equals(m0)) {
                exit(new Exception("Invalid data in Breakpoints event: " + m0 + " != " + m1));
                return false;
            }
            return true;
        }
    };

    TestRCBP1(TCFTestSuite test_suite, IChannel channel, int channel_id) {
        this.test_suite = test_suite;
        this.channel_id = channel_id;
        diag = channel.getRemoteService(IDiagnostics.class);
        mm = channel.getRemoteService(IMemory.class);
        rc = channel.getRemoteService(IRunControl.class);
        rg = channel.getRemoteService(IRegisters.class);
        bp = channel.getRemoteService(IBreakpoints.class);
        ln = channel.getRemoteService(ILineNumbers.class);
    }

    public void start() {
        if (diag == null || rc == null) {
            test_suite.done(this, null);
        }
        else if (bp == null) {
            exit(new Exception("Remote Breakpoints service not found"));
        }
        else {
            diag.getTestList(this);
        }
        if (bp != null) bp.addListener(bp_listener);
    }

    public void doneGetTestList(IToken token, Throwable error, String[] list) {
        assert test_suite.isActive(this);
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
            assert test_suite.isActive(this);
            assert this.context_id == null;
            this.context_id = context_id;
            if (pending_cancel != null) {
                exit(null);
            }
            else {
                diag.getSymbol(context_id, "tcf_test_func0", this);
                diag.getSymbol(context_id, "tcf_test_func1", this);
                diag.getSymbol(context_id, "tcf_test_func2", this);
                diag.getSymbol(context_id, "tcf_test_array", this);
            }
        }
    }

    public void doneGetSymbol(IToken token, Throwable error, ISymbol symbol) {
        if (error != null) {
            exit(error);
            return;
        }
        if (!test_suite.isActive(this)) return;
        assert this.context_id != null;
        if (!symbol.isAbs()) {
            exit(new Exception("Symbols 'tcf_test_*' must be absolute"));
        }
        else if (symbol.getValue().longValue() == 0) {
            exit(new Exception("Symbols 'tcf_test_*' must not be NULL"));
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
            // Reset breakpoint list (previous tests might left breakpoints)
            bp.set(null, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        // Create initial set of breakpoints
                        iniBreakpoints();
                    }
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void iniBreakpoints() {
        assert !bp_set_done;
        Map<String,Object> m[] = new Map[4];
        for (int i = 0; i < m.length; i++) {
            m[i] = new HashMap();
            m[i].put(IBreakpoints.PROP_ID, "TcfTestBP" + i + "" + channel_id);
            m[i].put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
            switch (i) {
            case 0:
                m[i].put(IBreakpoints.PROP_LOCATION, func0.getValue().toString());
                m[i].put(IBreakpoints.PROP_CONDITION, "$thread!=\"\"");
                break;
            case 1:
                m[i].put(IBreakpoints.PROP_LOCATION, "(31+1)/16+tcf_test_func1-2");
                m[i].put(IBreakpoints.PROP_CONDITION, "tcf_test_func0!=tcf_test_func1");
                break;
            case 2:
                // Disabled breakpoint
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func2");
                m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                break;
            case 3:
                // Breakpoint that will be enabled with "enable" command
                m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func2");
                break;
            }
            bp_list.put((String)m[i].get(IBreakpoints.PROP_ID), m[i]);
        }
        bp.set(m, new IBreakpoints.DoneCommand() {
            public void doneCommand(IToken token, Exception error) {
                bp_set_done = true;
                if (error != null) {
                    exit(error);
                }
                else {
                    get_state_cmds.put(rc.getContext(context_id, TestRCBP1.this), context_id);
                }
            }
        });
    }

    public void doneGetContext(IToken token, Exception error, RunControlContext context) {
        get_state_cmds.remove(token);
        if (test_suite.cancel) return;
        if (error != null) {
            exit(error);
            return;
        }
        if (this.context == null) {
            this.context = context;
            assert context_id.equals(context.getID());
            assert threads.isEmpty();
            assert running.isEmpty();
            assert suspended.isEmpty();
            rc.addListener(this);
        }
        get_state_cmds.put(rc.getChildren(context.getID(), this), context.getID());
        if (context.hasState()) {
            threads.put(context.getID(), context);
            get_state_cmds.put(context.getState(this), context.getID());
        }
    }

    public void doneGetChildren(IToken token, Exception error, String[] contexts) {
        get_state_cmds.remove(token);
        if (test_suite.cancel) return;
        if (error != null) {
            exit(error);
            return;
        }
        for (String id : contexts) {
            get_state_cmds.put(rc.getContext(id, this), id);
        }
        if (get_state_cmds.isEmpty()) {
            // No more pending commands
            doneStartingTestProcess();
        }
    }

    public void doneGetState(IToken token, Exception error,
            boolean suspended, String pc, String reason,
            Map<String, Object> params) {
        final String id = get_state_cmds.remove(token);
        if (test_suite.cancel) return;
        if (error != null) {
            exit(error);
            return;
        }
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
                        resume(id);
                    }
                }
                else {
                    // Receiving context state for the first time
                    if (resume_cnt > 0) {
                        exit(new Exception("Missing contextSuspended event for " + id));
                    }
                    else if ("Breakpoint".equals(reason)) {
                        exit(new Exception("Invalid suspend reason of main thread after test start: " + reason + " " + pc));
                    }
                    else {
                        this.suspended.put(id, new SuspendedContext(id, pc, reason, params));
                    }
                }
            }
        }
        if (get_state_cmds.isEmpty()) {
            // No more pending commands
            if (!test_suite.isActive(this)) return;
            doneStartingTestProcess();
        }
    }

    private void doneStartingTestProcess() {
        assert !done_starting_test_process;
        assert get_state_cmds.isEmpty();
        assert resume_cnt == 0;
        assert threads.size() == suspended.size() + running.size();
        assert bp_set_done;
        assert !bp_change_done;
        if (threads.size() == 0) return;
        done_starting_test_process = true;
        final String bp_id = "TcfTestBP3" + channel_id;
        final Map<String,Object> m = new HashMap<String,Object>();
        m.put(IBreakpoints.PROP_ID, bp_id);
        m.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
        m.put(IBreakpoints.PROP_LOCATION, "tcf_test_func2");
        ArrayList<String> l = new ArrayList<String>();
        l.add(context_id);
        m.put(IBreakpoints.PROP_CONTEXTIDS, l);
        m.put(IBreakpoints.PROP_STOP_GROUP, l);
        StringBuffer bf = new StringBuffer();
        for (String id : threads.keySet()) {
            if (bf.length() > 0) bf.append(" || ");
            bf.append("$thread==\"");
            bf.append(id);
            bf.append('"');
        }
        m.put(IBreakpoints.PROP_CONDITION, bf.toString());
        bp_list.put(bp_id, m);
        bp.change(m, new IBreakpoints.DoneCommand() {
            public void doneCommand(IToken token, Exception error) {
                bp_change_done = true;
                if (error != null) exit(error);
            }
        });
        bp.getIDs(new IBreakpoints.DoneGetIDs() {
            public void doneGetIDs(IToken token, Exception error, String[] ids) {
                if (error != null) {
                    exit(error);
                    return;
                }
                if (!bp_change_done) {
                    exit(new Exception("Invalid responce order"));
                    return;
                }
                HashSet<String> s = new HashSet<String>();
                for (String id : ids) s.add(id);
                if (ids.length != s.size()) {
                    exit(new Exception("Invalis BP list: " + ids));
                    return;
                }
                for (String id : bp_list.keySet()) {
                    if (!s.contains(id)) {
                        exit(new Exception("BP is not listed by Breakpoints.getIDs: " + id));
                        return;
                    }
                }
            }
        });
        for (final String id : bp_list.keySet()) {
            bp.getProperties(id, new IBreakpoints.DoneGetProperties() {
                public void doneGetProperties(IToken token, Exception error, Map<String,Object> properties) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    HashMap<String,Object> m0 = new HashMap<String,Object>(properties);
                    HashMap<String,Object> m1 = (HashMap<String,Object>)bp_list.get(id);
                    if (m0.get(IBreakpoints.PROP_ENABLED) == null) m0.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                    if (m1.get(IBreakpoints.PROP_ENABLED) == null) m1.put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                    if (!m1.equals(m0)) {
                        exit(new Exception("Invalid data returned by Breakpoints.getProperties: " + m0 + " != " + m1));
                        return;
                    }
                }
            });
            bp.getStatus(id, new IBreakpoints.DoneGetStatus() {
                public void doneGetStatus(IToken token, Exception error, Map<String,Object> status) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                }
            });
        }
        Protocol.sync(new Runnable() {
            public void run() {
                if (!test_suite.isActive(TestRCBP1.this)) return;
                if (!bp_change_done) {
                    exit(new Exception("Protocol.sync() test failed"));
                    return;
                }
                m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                bp.enable(new String[]{ bp_id }, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) exit(error);
                    }
                });
                for (SuspendedContext s : suspended.values()) resume(s.id);
            }
        });
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
            String p = contexts[i].getParentID();
            if (context.getID().equals(p) || threads.get(p) != null) {
                if (contexts[i].hasState()) {
                    threads.put(contexts[i].getID(), contexts[i]);
                    if (!done_starting_test_process) {
                        get_state_cmds.put(contexts[i].getState(this), contexts[i].getID());
                    }
                    else {
                        running.add(contexts[i].getID());
                    }
                }
            }
        }
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i].getID().equals(context.getID())) {
                context = contexts[i];
            }
            if (threads.get(contexts[i].getID()) != null) {
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
            running.remove(id);
            if (threads.remove(id) != null && threads.isEmpty()) {
                if (bp_cnt != 30) {
                    exit(new Exception("Test main thread breakpoint count = " + bp_cnt + ", expected 30"));
                }
                rc.removeListener(this);
                // Reset breakpoint list
                bp.set(null, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        exit(error);
                    }
                });
            }
        }
    }

    public void contextResumed(String id) {
        IRunControl.RunControlContext ctx = threads.get(id);
        if (ctx == null) return;
        if (!ctx.hasState()) {
            exit(new Exception("Resumed event for context that HasState = false"));
            return;
        }
        SuspendedContext sc = suspended.remove(id);
        if (isMyBreakpoint(sc)) suspended_prev.put(id, sc);
        running.add(id);
    }

    private String toSymName(long addr) {
        if (func0.getValue().longValue() == addr) return "tcf_test_func0";
        if (func1.getValue().longValue() == addr) return "tcf_test_func1";
        if (func2.getValue().longValue() == addr) return "tcf_test_func2";
        return "0x" + Long.toHexString(addr);
    }

    private void checkSuspendedContext(SuspendedContext sc, ISymbol sym) {
        long pc =  Long.parseLong(sc.pc);
        if (pc != sym.getValue().longValue() || !"Breakpoint".equals(sc.reason)) {
            exit(new Exception("Invalid contextSuspended event: " + sc.id + " '" + toSymName(pc) + "' " + sc.pc + " " + sc.reason +
                    ", expected breakpoint at '" + toSymName(sym.getValue().longValue()) + "' " + sym.getValue()));
        }
    }

    private boolean isMyBreakpoint(SuspendedContext sc) {
        // Check if context suspended by a one of our breakpoint
        if (!"Breakpoint".equals(sc.reason)) return false;
        long pc =  Long.parseLong(sc.pc);
        if (pc == func0.getValue().longValue()) return true;
        if (pc == func1.getValue().longValue()) return true;
        if (pc == func2.getValue().longValue()) return true;
        return false;
    }

    public void contextSuspended(final String id, String pc, String reason, Map<String, Object> params) {
        IRunControl.RunControlContext ctx = threads.get(id);
        if (ctx == null) return;
        if (!ctx.hasState()) {
            exit(new Exception("Suspended event for context that HasState = false"));
            return;
        }
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
        }
        if (main_thread_id == null && "Breakpoint".equals(reason) && isMyBreakpoint(sc)) {
            // Process main thread should be the first to hit a breakpoint in the test
            if (!done_starting_test_process) {
                exit(new Exception("Unexpeceted breakpoint hit"));
                return;
            }
            main_thread_id = id;
        }
        if (main_thread_id == null) {
            resume(id);
            return;
        }
        if (isMyBreakpoint(sc)) {
            if ("Breakpoint".equals(reason) && id.equals(main_thread_id)) bp_cnt++;
            SuspendedContext sp = suspended_prev.get(id);
            if (sp == null) {
                checkSuspendedContext(sc, func0);
            }
            else if (Long.parseLong(sp.pc) == func2.getValue().longValue()) {
                checkSuspendedContext(sc, func0);
            }
            else if (Long.parseLong(sp.pc) == func1.getValue().longValue()) {
                if (id.equals(main_thread_id)) {
                    checkSuspendedContext(sc, func2);
                }
                else {
                    checkSuspendedContext(sc, func0);
                }
            }
            else if (Long.parseLong(sp.pc) == func0.getValue().longValue()) {
                checkSuspendedContext(sc, func1);
            }
        }
        if (!test_suite.isActive(this)) return;
        final SuspendedContext sc0 = sc;
        ILineNumbers.DoneMapToSource ln_done = new ILineNumbers.DoneMapToSource() {
            public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                if (error != null) {
                    exit(error);
                    return;
                }
                runMemoryTest(sc0, new Runnable() {
                    public void run() {
                        runRegistersTest(sc0, new Runnable() {
                            public void run() {
                                resume(id);
                            }
                        });
                    }
                });
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

    private void resume(final String id) {
        assert done_starting_test_process || resume_cnt == 0;
        if (!done_starting_test_process) return;
        resume_cnt++;
        SuspendedContext sc = suspended.get(id);
        IRunControl.RunControlContext ctx = threads.get(id);
        if (ctx != null && sc != null) {
            int rm = rnd.nextInt(6);
            if (!ctx.canResume(rm)) rm = IRunControl.RM_RESUME;
            ctx.resume(rm, 1, new HashMap<String,Object>(), new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (test_suite.cancel) return;
                    if (!test_suite.isActive(TestRCBP1.this)) return;
                    if (threads.get(id) == null) return;
                    if (error != null) exit(error);
                }
            });
        }
    }

    private void runMemoryTest(final SuspendedContext sc, final Runnable done) {
        if (mm == null || test_suite.target_lock) {
            Protocol.invokeLater(done);
            return;
        }
        test_suite.target_lock = true;
        mm.getContext(context_id, new IMemory.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, final MemoryContext mem_ctx) {
                if (suspended.get(sc.id) != sc) {
                    test_suite.target_lock = false;
                    return;
                }
                if (error != null) {
                    exit(error);
                    return;
                }
                if (!context_id.equals(mem_ctx.getID())) {
                    exit(new Exception("Bad memory context data: invalid ID"));
                }
                Object pid = context.getProperties().get(IRunControl.PROP_PROCESS_ID);
                if (pid != null && !pid.equals(mem_ctx.getProcessID())) {
                    exit(new Exception("Bad memory context data: invalid ProcessID"));
                }
                final boolean big_endian = mem_ctx.isBigEndian();
                final int addr_size = mem_ctx.getAddressSize();
                final byte[] buf = new byte[0x1000];
                mem_ctx.get(array.getValue(), 1, buf, 0, addr_size, 0, new IMemory.DoneMemory() {
                    public void doneMemory(IToken token, MemoryError error) {
                        if (suspended.get(sc.id) != sc) {
                            test_suite.target_lock = false;
                            return;
                        }
                        if (error != null) {
                            exit(error);
                            return;
                        }
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
                        if (mem_address.longValue() == 0) {
                            exit(new Exception("Bad value of 'tcf_test_array': " + mem_address));
                        }
                        testSetMemoryCommand(sc, mem_ctx, mem_address, buf, done);
                    }
                });
            }
        });
    }

    private void testSetMemoryCommand(final SuspendedContext sc,
            final IMemory.MemoryContext mem_ctx,
            final Number addr, final byte[] buf,
            final Runnable done) {
        final byte[] data = new byte[buf.length];
        rnd.nextBytes(data);
        mem_ctx.set(addr, 1, data, 0, data.length, 0, new IMemory.DoneMemory() {
            public void doneMemory(IToken token, MemoryError error) {
                if (suspended.get(sc.id) != sc) {
                    test_suite.target_lock = false;
                    return;
                }
                if (error != null) {
                    exit(error);
                    return;
                }
                mem_ctx.get(addr, 1, buf, 0, buf.length, 0, new IMemory.DoneMemory() {
                    public void doneMemory(IToken token, MemoryError error) {
                        if (suspended.get(sc.id) != sc) {
                            test_suite.target_lock = false;
                            return;
                        }
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
                        testFillMemoryCommand(sc, mem_ctx, addr, buf, done);
                    }
                });
            }
        });
    }

    private void testFillMemoryCommand(final SuspendedContext sc,
            final IMemory.MemoryContext mem_ctx,
            final Number addr, final byte[] buf,
            final Runnable done) {
        final byte[] data = new byte[buf.length / 7];
        rnd.nextBytes(data);
        mem_ctx.fill(addr, 1, data, buf.length, 0, new IMemory.DoneMemory() {
            public void doneMemory(IToken token, MemoryError error) {
                if (suspended.get(sc.id) != sc) {
                    test_suite.target_lock = false;
                    return;
                }
                if (error != null) {
                    exit(error);
                    return;
                }
                mem_ctx.get(addr, 1, buf, 0, buf.length, 0, new IMemory.DoneMemory() {
                    public void doneMemory(IToken token, MemoryError error) {
                        if (suspended.get(sc.id) != sc) {
                            test_suite.target_lock = false;
                            return;
                        }
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
                        test_suite.target_lock = false;
                        done.run();
                    }
                });
            }
        });
    }

    private void runRegistersTest(final SuspendedContext sc, final Runnable done) {
        if (rg == null) {
            Protocol.invokeLater(done);
            return;
        }
        if (regs.get(sc.id) == null) {
            final Map<String,IRegisters.RegistersContext> reg_map =
                new HashMap<String,IRegisters.RegistersContext>();
            final Set<IToken> cmds = new HashSet<IToken>();
            regs.put(sc.id, reg_map);
            cmds.add(rg.getChildren(sc.id, new IRegisters.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) {
                        regs.remove(sc.id);
                        return;
                    }
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
                                if (suspended.get(sc.id) != sc) {
                                    regs.remove(sc.id);
                                    return;
                                }
                                if (error != null) {
                                    for (IToken t : cmds) t.cancel();
                                    exit(error);
                                    return;
                                }
                                reg_map.put(id, context);
                                if (cmds.isEmpty()) {
                                    testGetSetRegisterCommands(sc, done);
                                }
                            }
                        }));
                    }
                }
            }));
        }
        else {
            testGetSetRegisterCommands(sc, done);
        }
    }

    private void testGetSetRegisterCommands(final SuspendedContext sc, final Runnable done) {
        final Set<IToken> cmds = new HashSet<IToken>();
        Map<String,IRegisters.RegistersContext> reg_map = regs.get(sc.id);
        for (final IRegisters.RegistersContext ctx : reg_map.values()) {
            if (!ctx.isReadable()) continue;
            if (ctx.isReadOnce()) continue;
            cmds.add(ctx.get(new IRegisters.DoneGet() {
                public void doneGet(IToken token, Exception error, byte[] value) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) return;
                    if (error != null) {
                        for (IToken t : cmds) t.cancel();
                        exit(error);
                        return;
                    }
                    cmds.add(ctx.set(value, new IRegisters.DoneSet() {
                        public void doneSet(IToken token, Exception error) {
                            cmds.remove(token);
                            if (suspended.get(sc.id) != sc) return;
                            if (error != null) {
                                for (IToken t : cmds) t.cancel();
                                exit(error);
                                return;
                            }
                            if (cmds.isEmpty()) {
                                done.run();
                            }
                        }
                    }));
                }
            }));
        }
        if (!reg_map.isEmpty()) {
            List<IRegisters.Location> locs = new ArrayList<IRegisters.Location>();
            String[] ids = reg_map.keySet().toArray(new String[reg_map.size()]);
            for (int i = 0; i < rnd.nextInt(32); i++) {
                String id = ids[rnd.nextInt(ids.length)];
                IRegisters.RegistersContext ctx = reg_map.get(id);
                if (!ctx.isReadable()) continue;
                if (ctx.isReadOnce()) continue;
                int offs = rnd.nextInt(ctx.getSize());
                int size = rnd.nextInt(ctx.getSize() - offs) + 1;
                locs.add(new IRegisters.Location(id, offs, size));
            }
            final IRegisters.Location[] loc_arr = locs.toArray(new IRegisters.Location[locs.size()]);
            cmds.add(rg.getm(loc_arr, new IRegisters.DoneGet() {
                public void doneGet(IToken token, Exception error, byte[] value) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) return;
                    if (error != null) {
                        for (IToken t : cmds) t.cancel();
                        exit(error);
                        return;
                    }
                    cmds.add(rg.setm(loc_arr, value, new IRegisters.DoneSet() {
                        public void doneSet(IToken token, Exception error) {
                            cmds.remove(token);
                            if (suspended.get(sc.id) != sc) return;
                            if (error != null) {
                                for (IToken t : cmds) t.cancel();
                                exit(error);
                                return;
                            }
                            if (cmds.isEmpty()) {
                                done.run();
                            }
                        }
                    }));
                }
            }));
        }
        if (cmds.isEmpty()) {
            done.run();
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
        if (!test_suite.isActive(this)) return;
        if (pending_cancel != null) {
            Protocol.invokeLater(pending_cancel);
            pending_cancel = null;
        }
        else {
            if (rc != null) rc.removeListener(this);
        }
        if (bp != null) bp.removeListener(bp_listener);
        test_suite.done(this, x);
    }
}
