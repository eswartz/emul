/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.tm.internal.tcf.debug.model.TCFMemoryRegion;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IDisassembly;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.services.IPathMap;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tm.tcf.services.IDisassembly.IDisassemblyLine;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.services.IPathMap.PathMapRule;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.services.ISymbols.Symbol;

class TestRCBP1 implements ITCFTest, IRunControl.RunControlListener {

    private final TCFTestSuite test_suite;
    private final int channel_id;
    private final List<PathMapRule> path_map;
    private final Map<String,ArrayList<TCFMemoryRegion>> mem_map;
    private final IDiagnostics srv_diag;
    private final ISymbols srv_syms;
    private final IMemory srv_memory;
    private final IRunControl srv_run_ctrl;
    private final IRegisters srv_registers;
    private final IBreakpoints srv_breakpoints;
    private final ILineNumbers srv_line_numbers;
    private final IDisassembly srv_disassembly;
    private final IPathMap srv_path_map;
    private final IMemoryMap srv_memory_map;
    private final Map<String,IRunControl.RunControlContext> threads = new HashMap<String,IRunControl.RunControlContext>();
    private final Map<String,SuspendedContext> suspended = new HashMap<String,SuspendedContext>();
    private final Map<String,SuspendedContext> suspended_prev = new HashMap<String,SuspendedContext>();
    private final Map<String,IDisassemblyLine[]> disassembly_lines = new HashMap<String,IDisassemblyLine[]>();
    private final Map<String,Map<String,Object>[]> disassembly_capabilities = new HashMap<String,Map<String,Object>[]>();
    private final Set<String> running = new HashSet<String>();
    private final Set<IToken> get_state_cmds = new HashSet<IToken>();
    private final HashMap<String,IToken> resume_cmds = new HashMap<String,IToken>();
    private final Map<String,Map<String,IRegisters.RegistersContext>> regs =
        new HashMap<String,Map<String,IRegisters.RegistersContext>>();
    private final Map<String,Map<String,Object>> bp_list = new HashMap<String,Map<String,Object>>();
    private final Map<String,IDiagnostics.ISymbol> sym_list = new HashMap<String,IDiagnostics.ISymbol>();
    private final Random rnd = new Random();

    private String[] test_list;
    private String test_id;
    private boolean path_map_done;
    private boolean mem_map_done;
    private String test_ctx_id; // Test context ID
    private IRunControl.RunControlContext test_context;
    private String main_thread_id;
    private Map<String,Object> bp_capabilities;
    private Runnable pending_cancel;
    private int bp_cnt;
    private boolean done_get_state;
    private boolean done_disassembly;
    private int resume_cnt = 0;
    private IToken cancel_test_cmd;
    private boolean bp_reset_done;
    private boolean bp_set_done;
    private boolean bp_change_done;
    private boolean bp_sync_done;
    private String data_bp_id;
    private int data_bp_cnt;

    private static class SuspendedContext {
        final String id;
        final String pc;
        final String reason;
        final Map<String,Object> params;

        boolean get_state_pending;
        boolean ok_to_resume;

        SuspendedContext(String id, String pc, String reason, Map<String,Object> params) {
            this.id = id;
            this.pc = pc;
            this.reason = reason;
            this.params = params;
        }
    }

    private final IBreakpoints.BreakpointsListener bp_listener = new IBreakpoints.BreakpointsListener() {

        @SuppressWarnings("unchecked")
        public void breakpointStatusChanged(String id, Map<String,Object> status) {
            if (bp_list.get(id) != null && test_context != null && bp_cnt < 40) {
                String s = (String)status.get(IBreakpoints.STATUS_ERROR);
                if (s != null) exit(new Exception("Invalid BP status: " + s));
                Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
                if (list == null) return;
                String err = null;
                for (Map<String,Object> map : list) {
                    String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                    if (test_context.getProcessID().equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                        err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
                }
                if (err != null) {
                    if (bp_cnt == 0 && id.equals(data_bp_id)) return;
                    exit(new Exception("Invalid BP status: " + err));
                }
            }
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
            if (!bp_change_done) return;
            for (String id : ids) {
                if (bp_list.get(id) != null) {
                    exit(new Exception("Invalid Breakpoints.contextRemoved event"));
                    return;
                }
            }
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

    TestRCBP1(TCFTestSuite test_suite, IChannel channel, int channel_id,
            List<PathMapRule> path_map, Map<String,ArrayList<TCFMemoryRegion>> mem_map) {
        this.test_suite = test_suite;
        this.channel_id = channel_id;
        this.path_map = path_map;
        this.mem_map = mem_map;
        srv_diag = channel.getRemoteService(IDiagnostics.class);
        srv_syms = channel.getRemoteService(ISymbols.class);
        srv_memory = channel.getRemoteService(IMemory.class);
        srv_run_ctrl = channel.getRemoteService(IRunControl.class);
        srv_registers = channel.getRemoteService(IRegisters.class);
        srv_breakpoints = channel.getRemoteService(IBreakpoints.class);
        srv_line_numbers = channel.getRemoteService(ILineNumbers.class);
        srv_disassembly = channel.getRemoteService(IDisassembly.class);
        srv_path_map = channel.getRemoteService(IPathMap.class);
        srv_memory_map = channel.getRemoteService(IMemoryMap.class);
    }

    public void start() {
        if (srv_run_ctrl == null) {
            test_suite.done(this, null);
        }
        else {
            if (srv_breakpoints != null) srv_breakpoints.addListener(bp_listener);
            runTest();
        }
    }

    private void runTest() {
        if (!test_suite.isActive(this)) return;
        if (!path_map_done) {
            setPathMap();
            return;
        }
        if (!mem_map_done) {
            setMemMap();
            return;
        }
        if (test_list == null) {
            getTestList();
            return;
        }
        if (!bp_reset_done) {
            resetBreakpoints();
            return;
        }
        if (test_id != null) {
            if (test_ctx_id == null) {
                startTestContext();
                return;
            }
            if (test_context == null) {
                getTestContext();
                return;
            }
            if (sym_list.isEmpty()) {
                getSymbols();
                return;
            }
            if (bp_capabilities == null) {
                getBreakpointCapabilities();
                return;
            }
            if (!bp_set_done) {
                iniBreakpoints();
                return;
            }
        }
        if (!done_get_state) {
            assert get_state_cmds.isEmpty();
            assert threads.isEmpty();
            assert running.isEmpty();
            assert suspended.isEmpty();
            getContextState(test_ctx_id);
            return;
        }
        if (srv_disassembly != null && !done_disassembly) {
            assert get_state_cmds.isEmpty();
            assert disassembly_lines.isEmpty();
            getDisassemlyLines();
            return;
        }
        if (test_id != null) {
            if (!bp_change_done) {
                changeBreakpoints();
                return;
            }
            assert resume_cnt == 0;
            for (SuspendedContext s : suspended.values()) resume(s.id);
        }
        else if (suspended.size() > 0) {
            final int test_cnt = suspended.size();
            Runnable done = new Runnable() {
                int done_cnt;
                public void run() {
                    done_cnt++;
                    if (done_cnt == test_cnt) {
                        exit(null);
                    }
                }
            };
            for (SuspendedContext sc : suspended.values()) runRegistersTest(sc, done);
        }
        else {
            exit(null);
        }
    }

    private void getTestList() {
        if (srv_diag == null) {
            test_list = new String[0];
            runTest();
            return;
        }
        srv_diag.getTestList(new IDiagnostics.DoneGetTestList() {
            public void doneGetTestList(IToken token, Throwable error, String[] list) {
                if (error != null) {
                    exit(error);
                }
                else {
                    if (list == null) list = new String[0];
                    test_list = list;
                    for (String s : list) {
                        if (s.equals("RCBP1")) test_id = s;
                    }
                    if (test_id == null && list.length > 0) {
                        test_id = list[rnd.nextInt(list.length)];
                    }
                    runTest();
                }
            }
        });
    }

    private void setPathMap() {
        if (srv_path_map == null || path_map == null) {
            path_map_done = true;
            runTest();
            return;
        }
        srv_path_map.set(path_map.toArray(new PathMapRule[path_map.size()]), new IPathMap.DoneSet() {
            public void doneSet(IToken token, Exception error) {
                if (error != null) {
                    exit(error);
                }
                else {
                    path_map_done = true;
                    runTest();
                }
            }
        });
    }

    private void setMemMap() {
        if (srv_path_map == null || mem_map == null || mem_map.size() == 0) {
            mem_map_done = true;
            runTest();
            return;
        }
        final Set<IToken> cmds = new HashSet<IToken>();
        for (String id : mem_map.keySet()) {
            ArrayList<TCFMemoryRegion> l = mem_map.get(id);
            cmds.add(srv_memory_map.set(id, l.toArray(new TCFMemoryRegion[l.size()]), new IMemoryMap.DoneSet() {
                public void doneSet(IToken token, Exception error) {
                    cmds.remove(token);
                    if (error instanceof IErrorReport) {
                        IErrorReport e = (IErrorReport)error;
                        if (e.getErrorCode() == IErrorReport.TCF_ERROR_INV_CONTEXT) error = null;
                    }
                    if (error != null) {
                        exit(error);
                    }
                    else if (cmds.size() == 0) {
                        mem_map_done = true;
                        runTest();
                    }
                }
            }));
        }
        assert cmds.size() > 0;
    }

    private void resetBreakpoints() {
        if (srv_breakpoints == null) {
            bp_reset_done = true;
            runTest();
            return;
        }
        // Reset breakpoint list (previous tests might left breakpoints)
        srv_breakpoints.set(null, new IBreakpoints.DoneCommand() {
            public void doneCommand(IToken token, Exception error) {
                if (error != null) {
                    exit(error);
                    return;
                }
                bp_reset_done = true;
                runTest();
            }
        });
    }

    private void getBreakpointCapabilities() {
        if (srv_breakpoints == null) {
            bp_capabilities = new HashMap<String,Object>();
            runTest();
            return;
        }
        srv_breakpoints.getCapabilities(test_ctx_id, new IBreakpoints.DoneGetCapabilities() {
            public void doneGetCapabilities(IToken token, Exception error, Map<String,Object> capabilities) {
                if (error != null) {
                    exit(error);
                    return;
                }
                Boolean l = (Boolean)capabilities.get(IBreakpoints.CAPABILITY_LOCATION);
                Boolean c = (Boolean)capabilities.get(IBreakpoints.CAPABILITY_CONDITION);
                if (l == null || !l) {
                    exit(new Exception("Breakpoints service does not support \"Location\" attribute"));
                    return;
                }
                if (c == null || !c) {
                    exit(new Exception("Breakpoints service does not support \"Condition\" attribute"));
                    return;
                }
                bp_capabilities = capabilities;
                runTest();
            }
        });
    }

    private void startTestContext() {
        srv_diag.runTest(test_id, new IDiagnostics.DoneRunTest() {
            public void doneRunTest(IToken token, Throwable error, String context_id) {
                if (error != null) {
                    exit(error);
                }
                else if (test_suite.isActive(TestRCBP1.this)) {
                    assert test_ctx_id == null;
                    test_ctx_id = context_id;
                    if (pending_cancel != null) {
                        exit(null);
                    }
                    else {
                        runTest();
                    }
                }
            }
        });
    }

    private void getTestContext() {
        srv_run_ctrl.getContext(test_ctx_id, new IRunControl.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, RunControlContext context) {
                if (test_suite.cancel) return;
                if (error != null) {
                    exit(error);
                    return;
                }
                test_context = context;
                assert test_ctx_id.equals(context.getID());
                srv_run_ctrl.addListener(TestRCBP1.this);
                runTest();
            }
        });
    }

    private void getSymbols() {
        final HashMap<IToken,String> cmds = new HashMap<IToken,String>();
        IDiagnostics.DoneGetSymbol done = new IDiagnostics.DoneGetSymbol() {
            public void doneGetSymbol(IToken token, Throwable error, ISymbol symbol) {
                String name = cmds.remove(token);
                if (error != null) {
                    exit(error);
                    return;
                }
                if (!test_suite.isActive(TestRCBP1.this)) return;
                assert test_ctx_id != null;
                if (symbol == null) {
                    exit(new Exception("Symbol must not be NULL: " + name));
                }
                else if (!symbol.isAbs()) {
                    exit(new Exception("Symbol must be absolute: " + name));
                }
                else if (symbol.getValue() == null || symbol.getValue().longValue() == 0) {
                    exit(new Exception("Symbol value must not be NULL: " + name));
                }
                else {
                    sym_list.put(name, symbol);
                    if (cmds.isEmpty()) runTest();
                }
            }
        };
        String[] syms = {
                "tcf_test_func0",
                "tcf_test_func1",
                "tcf_test_func2",
                "tcf_test_func3",
                "tcf_test_array"
        };
        String prs = test_context.getProcessID();
        for (String name : syms) cmds.put(srv_diag.getSymbol(prs, name, done), name);
    }

    @SuppressWarnings("unchecked")
    private void iniBreakpoints() {
        assert !bp_set_done;
        assert bp_list.isEmpty();
        Map<String,Object> m[] = new Map[8];
        for (int i = 0; i < m.length; i++) {
            m[i] = new HashMap<String,Object>();
            m[i].put(IBreakpoints.PROP_ID, "TcfTestBP" + i + "" + channel_id);
            m[i].put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
            switch (i) {
            case 0:
                m[i].put(IBreakpoints.PROP_LOCATION, sym_list.get("tcf_test_func0").getValue().toString());
                // Condition is always true
                m[i].put(IBreakpoints.PROP_CONDITION, "$thread!=\"\"");
                break;
            case 1:
                m[i].put(IBreakpoints.PROP_LOCATION, sym_list.get("tcf_test_func0").getValue().toString());
                // Condition is always false
                m[i].put(IBreakpoints.PROP_CONDITION, "$thread==\"\"");
                break;
            case 2:
                // Second breakpoint at same address
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func0");
                break;
            case 3:
                // Location is an expression
                m[i].put(IBreakpoints.PROP_LOCATION, "(31+1)/16+tcf_test_func1-2");
                // Condition is always true
                m[i].put(IBreakpoints.PROP_CONDITION, "tcf_test_func0!=tcf_test_func1");
                break;
            case 4:
                // Disabled breakpoint
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func2");
                m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                break;
            case 5:
                // Breakpoint that will be enabled with "enable" command
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func2");
                m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                break;
            case 6:
                m[i].put(IBreakpoints.PROP_LOCATION, "tcf_test_func3");
                break;
            case 7:
                // Data breakpoint
                m[i].put(IBreakpoints.PROP_LOCATION, "&tcf_test_char");
                m[i].put(IBreakpoints.PROP_ACCESSMODE, IBreakpoints.ACCESSMODE_WRITE);
                Number ca = (Number)bp_capabilities.get(IBreakpoints.CAPABILITY_ACCESSMODE);
                if (ca != null && (ca.intValue() & (1 << IBreakpoints.ACCESSMODE_WRITE)) != 0) {
                    data_bp_id = (String)m[i].get(IBreakpoints.PROP_ID);
                }
                else {
                    m[i].put(IBreakpoints.PROP_ENABLED, Boolean.FALSE);
                }
                break;
            }
            bp_list.put((String)m[i].get(IBreakpoints.PROP_ID), m[i]);
        }
        srv_breakpoints.set(m, new IBreakpoints.DoneCommand() {
            public void doneCommand(IToken token, Exception error) {
                assert !bp_set_done;
                bp_set_done = true;
                if (error != null) {
                    exit(error);
                    return;
                }
                runTest();
            }
        });
    }

    private void getContextState(final String id) {
        get_state_cmds.add(srv_run_ctrl.getChildren(id, new IRunControl.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                get_state_cmds.remove(token);
                if (test_suite.cancel) return;
                if (error != null) {
                    exit(error);
                    return;
                }
                for (String s : contexts) getContextState(s);
                if (get_state_cmds.isEmpty()) doneContextState();
            }
        }));
        if (id == null) return;
        get_state_cmds.add(srv_run_ctrl.getContext(id, new IRunControl.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, RunControlContext context) {
                get_state_cmds.remove(token);
                if (test_suite.cancel) return;
                if (error != null) {
                    exit(error);
                    return;
                }
                if (context.hasState()) {
                    threads.put(id, context);
                    get_state_cmds.add(context.getState(new IRunControl.DoneGetState() {
                        public void doneGetState(IToken token, Exception error,
                                boolean susp, String pc, String reason,
                                Map<String, Object> params) {
                            get_state_cmds.remove(token);
                            if (test_suite.cancel) return;
                            if (error != null) {
                                exit(error);
                                return;
                            }
                            if (!susp) {
                                if (suspended.get(id) != null) {
                                    exit(new Exception("Invalid result of getState command"));
                                    return;
                                }
                                running.add(id);
                            }
                            else {
                                assert threads.get(id) != null;
                                if (running.contains(id)) {
                                    exit(new Exception("Invalid result of getState command"));
                                    return;
                                }
                                SuspendedContext sc = suspended.get(id);
                                if (sc != null && sc.pc != null && !sc.pc.equals(pc)) {
                                    exit(new Exception("Invalid result of getState command: invalid PC"));
                                    return;
                                }
                                if (sc != null && sc.reason != null && !sc.reason.equals(reason)) {
                                    exit(new Exception("Invalid result of getState command: invalid suspend reason"));
                                    return;
                                }
                                if (test_id != null && "Breakpoint".equals(reason)) {
                                    exit(new Exception("Invalid suspend reason of main thread after test start: " + reason + " " + pc));
                                    return;
                                }
                                assert !done_get_state;
                                suspended.put(id, new SuspendedContext(id, pc, reason, params));
                            }
                            if (get_state_cmds.isEmpty()) doneContextState();
                        }
                    }));
                }
                if (get_state_cmds.isEmpty()) doneContextState();
            }
        }));
    }

    private void doneContextState() {
        assert !done_get_state;
        assert get_state_cmds.isEmpty();
        assert resume_cnt == 0;
        assert threads.size() == suspended.size() + running.size();
        done_get_state = true;
        runTest();
    }

    private void getDisassemlyLines() {
        for (final String id : suspended.keySet()) {
            SuspendedContext sc = suspended.get(id);
            get_state_cmds.add(srv_disassembly.getCapabilities(id, new IDisassembly.DoneGetCapabilities() {
                public void doneGetCapabilities(IToken token, Throwable error, Map<String,Object>[] arr) {
                    get_state_cmds.remove(token);
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        disassembly_capabilities.put(id, arr);
                        if (get_state_cmds.isEmpty()) doneDisassembly();
                    }
                }
            }));
            if (sc.pc == null) {
                disassembly_lines.put(id, new IDisassemblyLine[0]);
                continue;
            }
            BigInteger pc = new BigInteger(sc.pc);
            get_state_cmds.add(srv_disassembly.disassemble(id, pc, 1, null, new IDisassembly.DoneDisassemble() {
                public void doneDisassemble(IToken token, Throwable error, IDisassemblyLine[] arr) {
                    get_state_cmds.remove(token);
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        disassembly_lines.put(id, arr);
                        if (get_state_cmds.isEmpty()) doneDisassembly();
                    }
                }
            }));
        }
        if (get_state_cmds.isEmpty()) doneDisassembly();
    }

    private void doneDisassembly() {
        assert !done_disassembly;
        assert get_state_cmds.isEmpty();
        if (!test_suite.isActive(TestRCBP1.this)) return;
        assert suspended.size() == disassembly_lines.size();
        done_disassembly = true;
        runTest();
    }

    private void changeBreakpoints() {
        assert !bp_change_done;
        final String bp_id = "TcfTestBP5" + channel_id;
        final Map<String,Object> m = bp_list.get(bp_id);
        ArrayList<String> l = new ArrayList<String>();
        l.add(test_context.getProcessID());
        Boolean ci = (Boolean)bp_capabilities.get(IBreakpoints.CAPABILITY_CONTEXTIDS);
        if (ci != null && ci) m.put(IBreakpoints.PROP_CONTEXTIDS, l);
        Boolean sg = (Boolean)bp_capabilities.get(IBreakpoints.CAPABILITY_STOP_GROUP);
        if (sg != null && sg) m.put(IBreakpoints.PROP_STOP_GROUP, l);
        StringBuffer bf = new StringBuffer();
        for (String id : threads.keySet()) {
            if (bf.length() > 0) bf.append(" || ");
            bf.append("$thread==\"");
            bf.append(id);
            bf.append('"');
        }
        m.put(IBreakpoints.PROP_CONDITION, bf.toString());
        bp_list.put(bp_id, m);
        srv_breakpoints.change(m, new IBreakpoints.DoneCommand() {
            public void doneCommand(IToken token, Exception error) {
                bp_change_done = true;
                if (error != null) exit(error);
            }
        });
        srv_breakpoints.getIDs(new IBreakpoints.DoneGetIDs() {
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
            srv_breakpoints.getProperties(id, new IBreakpoints.DoneGetProperties() {
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
            srv_breakpoints.getStatus(id, new IBreakpoints.DoneGetStatus() {
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
                srv_breakpoints.enable(new String[]{ bp_id }, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) exit(error);
                    }
                });
                bp_sync_done = true;
                runTest();
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
        for (RunControlContext ctx : contexts) {
            String id = ctx.getID();
            if (threads.get(id) != null) {
                exit(new Exception("Invalid contextAdded event:\nContext: " + ctx));
                return;
            }
            String p = ctx.getParentID();
            String c = ctx.getCreatorID();
            if (test_ctx_id.equals(c) || test_ctx_id.equals(p)) {
                if (!bp_change_done) {
                    exit(new Exception("Unexpected contextAdded event\nContext: " + ctx));
                    return;
                }
                if (ctx.hasState()) {
                    threads.put(id, ctx);
                    if (!done_get_state) {
                        getContextState(id);
                    }
                    else {
                        running.add(id);
                    }
                }
            }
        }
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlContext ctx : contexts) {
            String id = ctx.getID();
            if (id.equals(test_ctx_id)) test_context = ctx;
            if (threads.get(id) != null) threads.put(id, ctx);
        }
    }

    public void contextException(String id, String msg) {
        if (threads.get(id) != null) exit(new Exception(msg));
    }

    public void contextRemoved(String[] contexts) {
        for (String id : contexts) {
            if (suspended.get(id) != null) {
                exit(new Exception("Invalid contextRemoved event"));
                return;
            }
            running.remove(id);
            if (threads.remove(id) != null && threads.isEmpty()) {
                if (bp_cnt != 40) {
                    exit(new Exception("Test main thread breakpoint count = " + bp_cnt + ", expected 40"));
                }
                if (data_bp_id != null && data_bp_cnt != 10) {
                    exit(new Exception("Test main thread data breakpoint count = " + data_bp_cnt + ", expected 10"));
                }
                srv_run_ctrl.removeListener(this);
                // Reset breakpoint list
                bp_list.clear();
                srv_breakpoints.set(null, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        exit(error);
                    }
                });
            }
        }
    }

    public void contextResumed(String id) {
        resume_cmds.remove(id);
        IRunControl.RunControlContext ctx = threads.get(id);
        if (ctx == null) return;
        if (!ctx.hasState()) {
            exit(new Exception("Resumed event for context that HasState = false"));
            return;
        }
        SuspendedContext sc = suspended.remove(id);
        if (sc == null || sc.get_state_pending && ctx.getRCGroup() == null) {
            exit(new Exception("Unexpected contextResumed event: " + id));
            return;
        }
        if (isMyBreakpoint(sc)) suspended_prev.put(id, sc);
        running.add(id);
    }

    private long getSymAddr(String sym) {
        return sym_list.get(sym).getValue().longValue();
    }

    private String toSymName(long addr) {
        for (String name : sym_list.keySet()) {
            if (getSymAddr(name) == addr) return name;
        }
        return "0x" + Long.toHexString(addr);
    }

    private void checkSuspendedContext(SuspendedContext sc, String sym) {
        long pc = Long.parseLong(sc.pc);
        long ss = getSymAddr(sym);
        if (pc != ss || !"Breakpoint".equals(sc.reason)) {
            exit(new Exception("Invalid contextSuspended event: " +
                    sc.id + " '" + toSymName(pc) + "' " + sc.pc + " " + sc.reason +
                    ", expected breakpoint at '" + sym + "' " + ss));
        }
        String bp_id = null;
        if (sc.params != null) {
            Object ids = sc.params.get(IRunControl.STATE_BREAKPOINT_IDS);
            if (ids != null) {
                @SuppressWarnings("unchecked")
                Collection<String> c = (Collection<String>)ids;
                for (String id : c) {
                    if (bp_list.get(id) != null) {
                        bp_id = id;
                        break;
                    }
                }
                if (bp_id == null) {
                    exit(new Exception("Invalid value of 'BPs' attribute in a context state"));
                }
            }
        }
    }

    private void checkSuspendedContext(final SuspendedContext sc) {
        boolean my_breakpoint = isMyBreakpoint(sc);
        if (main_thread_id == null && my_breakpoint) {
            // Process main thread should be the first to hit a breakpoint in the test
            if (!done_get_state) {
                exit(new Exception("Unexpeceted breakpoint hit"));
                return;
            }
            main_thread_id = sc.id;
        }
        if (main_thread_id == null) {
            resume(sc.id);
            return;
        }
        if (my_breakpoint) {
            if (sc.id.equals(main_thread_id)) bp_cnt++;
            SuspendedContext sp = suspended_prev.get(sc.id);
            String sp_sym = sp == null ? null : toSymName(Long.parseLong(sp.pc));
            if (sp == null) {
                checkSuspendedContext(sc, "tcf_test_func0");
            }
            else if ("tcf_test_func0".equals(sp_sym)) {
                checkSuspendedContext(sc, "tcf_test_func1");
            }
            else if ("tcf_test_func1".equals(sp_sym)) {
                if (sc.id.equals(main_thread_id)) {
                    checkSuspendedContext(sc, "tcf_test_func2");
                }
                else {
                    checkSuspendedContext(sc, "tcf_test_func3");
                }
            }
            else if ("tcf_test_func2".equals(sp_sym)) {
                checkSuspendedContext(sc, "tcf_test_func3");
            }
            else if ("tcf_test_func3".equals(sp_sym)) {
                checkSuspendedContext(sc, "tcf_test_func0");
            }
        }
        else if (isMyDataBreakpoint(sc)) {
            if (sc.id.equals(main_thread_id)) data_bp_cnt++;
        }
        if (!test_suite.isActive(this)) return;
        assert resume_cmds.get(sc.id) == null;
        Runnable done = new Runnable() {
            public void run() {
                if (suspended.get(sc.id) == sc) resume(sc.id);
            }
        };
        if (my_breakpoint) {
            switch (rnd.nextInt(5)) {
            case 0:
                runMemoryTest(sc, done);
                break;
            case 1:
                runRegistersTest(sc, done);
                break;
            case 2:
                runLineNumbersTest(sc, done);
                break;
            case 3:
                runSymbolsTest(sc, done);
                break;
            default:
                done.run();
                break;
            }
        }
        else {
            done.run();
        }
    }

    private boolean isMyBreakpoint(SuspendedContext sc) {
        // Check if the context is suspended by one of our breakpoints
        if (!"Breakpoint".equals(sc.reason)) return false;
        long pc = Long.parseLong(sc.pc);
        for (IDiagnostics.ISymbol sym : sym_list.values()) {
            if (pc == sym.getValue().longValue()) return true;
        }
        return false;
    }

    private boolean isMyDataBreakpoint(SuspendedContext sc) {
        // Check if the context is suspended by our data breakpoints
        if (data_bp_id == null) return false;
        if (!"Breakpoint".equals(sc.reason)) return false;
        if (sc.params == null) return false;
        Object ids = sc.params.get(IRunControl.STATE_BREAKPOINT_IDS);
        if (ids != null) {
            @SuppressWarnings("unchecked")
            Collection<String> c = (Collection<String>)ids;
            if (c.contains(data_bp_id)) return true;
        }
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
            if (done_get_state || pc != null && !sc.pc.equals(pc) || reason != null && !sc.reason.equals(reason)) {
                exit(new Exception("Invalid contextSuspended event"));
                return;
            }
        }
        else {
            sc = new SuspendedContext(id, pc, reason, params);
            assert !done_get_state || done_disassembly || srv_disassembly == null;
            suspended.put(id, sc);
        }
        if (!bp_sync_done) return;
        assert get_state_cmds.size() == 0;
        assert resume_cmds.get(id) == null;
        assert suspended.get(id) == sc;
        assert !sc.get_state_pending;
        sc.get_state_pending = true;
        final SuspendedContext sc0 = sc;
        ctx.getState(new IRunControl.DoneGetState() {
            public void doneGetState(IToken token, Exception error, boolean susp,
                    String pc, String reason, Map<String, Object> params) {
                if (error != null) {
                    exit(error);
                }
                else if (suspended.get(id) != sc0) {
                    exit(new Exception("Context resumed before RunControl.getState result"));
                }
                else if (!susp) {
                    exit(new Exception("Invalid RunControl.getState result"));
                }
                else if (pc == null || pc.equals("0")) {
                    exit(new Exception("Invalid PC returned by RunControl.getState"));
                }
                else if (test_suite.isActive(TestRCBP1.this)) {
                    SuspendedContext sc = suspended.get(id);
                    assert sc.get_state_pending;
                    sc.get_state_pending = false;
                    if (sc.pc == null || sc.reason == null) {
                        sc = new SuspendedContext(id, pc, reason, params);
                        assert !done_get_state || done_disassembly || srv_disassembly == null;
                        suspended.put(id, sc);
                    }
                    else if (!sc.pc.equals(pc) || !sc.reason.equals(reason)) {
                        exit(new Exception("Invalid RunControl.getState result"));
                        return;
                    }
                    checkSuspendedContext(sc);
                }
            }
        });
    }

    private boolean canResume(IRunControl.RunControlContext ctx) {
        String grp = ctx.getRCGroup();
        if (grp != null) {
            for (ITCFTest test : test_suite.getActiveTests()) {
                if (test instanceof TestRCBP1) {
                    TestRCBP1 rcbp = (TestRCBP1)test;
                    for (IRunControl.RunControlContext x : rcbp.threads.values()) {
                        if (!grp.equals(x.getRCGroup())) continue;
                        SuspendedContext sc = rcbp.suspended.get(x.getID());
                        if (sc == null) {
                            if (rcbp != TestRCBP1.this) return false;
                            exit(new Exception("Invalid value of run control group ID"));
                            return false;
                        }
                        if (!sc.ok_to_resume) return false;
                    }
                }
            }
        }
        return true;
    }

    private void resume(final String id) {
        assert done_get_state || resume_cnt == 0;
        assert resume_cmds.get(id) == null;
        assert bp_sync_done;
        resume_cnt++;
        final SuspendedContext sc = suspended.get(id);
        final IRunControl.RunControlContext ctx = threads.get(id);
        if (ctx != null && sc != null) {
            assert !sc.get_state_pending;
            assert !sc.ok_to_resume;
            sc.ok_to_resume = true;
            if (!canResume(ctx)) return;
            int rm = IRunControl.RM_RESUME;
            if (isMyBreakpoint(sc)) {
                rm = rnd.nextInt(6);
                if (!ctx.canResume(rm)) rm = IRunControl.RM_RESUME;
            }
            resume_cmds.put(id, ctx.resume(rm, 1, new HashMap<String,Object>(), new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (test_suite.cancel) return;
                    if (!test_suite.isActive(TestRCBP1.this)) return;
                    if (threads.get(id) == null) return;
                    if (error instanceof IErrorReport &&
                            ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_ALREADY_RUNNING &&
                            ctx.getRCGroup() != null) {
                        error = null;
                    }
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    if (suspended.get(id) == sc || resume_cmds.get(id) == token) {
                        exit(new Exception("Missing contextResumed event after resume command"));
                        return;
                    }
                }
            }));
        }
    }

    private void runMemoryTest(final SuspendedContext sc, final Runnable done) {
        if (srv_memory == null || test_suite.target_lock) {
            Protocol.invokeLater(done);
            return;
        }
        test_suite.target_lock = true;
        srv_memory.getContext(test_context.getProcessID(), new IMemory.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, final MemoryContext mem_ctx) {
                if (suspended.get(sc.id) != sc) {
                    test_suite.target_lock = false;
                    return;
                }
                if (error != null) {
                    exit(error);
                    return;
                }
                if (!test_context.getProcessID().equals(mem_ctx.getID())) {
                    exit(new Exception("Bad memory context data: invalid ID"));
                }
                final boolean big_endian = mem_ctx.isBigEndian();
                final int addr_size = mem_ctx.getAddressSize();
                final byte[] buf = new byte[0x1000];
                mem_ctx.get(sym_list.get("tcf_test_array").getValue(), 1, buf, 0, addr_size, 0, new IMemory.DoneMemory() {
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
        if (srv_registers == null) {
            Protocol.invokeLater(done);
            return;
        }
        if (regs.get(sc.id) == null) {
            final Map<String,IRegisters.RegistersContext> reg_map =
                new HashMap<String,IRegisters.RegistersContext>();
            regs.put(sc.id, reg_map);
            final Set<IToken> cmds = new HashSet<IToken>();
            cmds.add(srv_registers.getChildren(sc.id, new IRegisters.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) {
                        regs.remove(sc.id);
                        return;
                    }
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    for (final String id : context_ids) {
                        cmds.add(srv_registers.getChildren(id, this));
                        cmds.add(srv_registers.getContext(id, new IRegisters.DoneGetContext() {
                            public void doneGetContext(IToken token, Exception error, RegistersContext context) {
                                cmds.remove(token);
                                if (suspended.get(sc.id) != sc) {
                                    regs.remove(sc.id);
                                    return;
                                }
                                if (error != null) {
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
        Map<String,IRegisters.RegistersContext> reg_map = regs.get(sc.id);
        final Set<IToken> cmds = new HashSet<IToken>();
        for (final IRegisters.RegistersContext ctx : reg_map.values()) {
            if (!ctx.isReadable()) continue;
            if (ctx.isReadOnce()) continue;
            cmds.add(ctx.get(new IRegisters.DoneGet() {
                public void doneGet(IToken token, Exception error, final byte[] value) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) return;
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    assert resume_cmds.get(sc.id) == null;
                    if (ctx.getSize() != value.length) {
                        exit(new Exception("Invalid register value size"));
                        return;
                    }
                    if (ctx.isWriteable() && !ctx.isWriteOnce()) {
                        cmds.add(ctx.set(value, new IRegisters.DoneSet() {
                            public void doneSet(IToken token, Exception error) {
                                cmds.remove(token);
                                if (suspended.get(sc.id) != sc) return;
                                if (error != null) {
                                    exit(error);
                                    return;
                                }
                                cmds.add(ctx.get(new IRegisters.DoneGet() {
                                    public void doneGet(IToken token, Exception error, byte[] value1) {
                                        cmds.remove(token);
                                        if (suspended.get(sc.id) != sc) return;
                                        if (error != null) {
                                            exit(error);
                                            return;
                                        }
                                        for (int i = 0; i < value.length; i++) {
                                            if (value[i] != value1[i]) {
                                                exit(new Exception("Invalid register value"));
                                                return;
                                            }
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
            }));
        }
        if (!reg_map.isEmpty()) {
            int data_size = 0;
            List<IRegisters.Location> locs = new ArrayList<IRegisters.Location>();
            String[] ids = reg_map.keySet().toArray(new String[reg_map.size()]);
            for (int i = 0; i < rnd.nextInt(32); i++) {
                String id = ids[rnd.nextInt(ids.length)];
                IRegisters.RegistersContext ctx = reg_map.get(id);
                if (!ctx.isReadable()) continue;
                if (!ctx.isWriteable()) continue;
                if (ctx.isReadOnce()) continue;
                if (ctx.isWriteOnce()) continue;
                int offs = rnd.nextInt(ctx.getSize());
                int size = rnd.nextInt(ctx.getSize() - offs) + 1;
                locs.add(new IRegisters.Location(id, offs, size));
                data_size += size;
            }
            final int total_size = data_size;
            final IRegisters.Location[] loc_arr = locs.toArray(new IRegisters.Location[locs.size()]);
            cmds.add(srv_registers.getm(loc_arr, new IRegisters.DoneGet() {
                public void doneGet(IToken token, Exception error, byte[] value) {
                    cmds.remove(token);
                    if (suspended.get(sc.id) != sc) return;
                    if (error == null && value.length != total_size) {
                        error = new Exception("Invalid data size in Registers.getm reply");
                    }
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    cmds.add(srv_registers.setm(loc_arr, value, new IRegisters.DoneSet() {
                        public void doneSet(IToken token, Exception error) {
                            cmds.remove(token);
                            if (suspended.get(sc.id) != sc) return;
                            if (error != null) {
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

    private void runLineNumbersTest(SuspendedContext sc, final Runnable done) {
        if (srv_line_numbers != null && sc.pc != null) {
            BigInteger x = new BigInteger(sc.pc);
            BigInteger y = x.add(BigInteger.valueOf(1));
            srv_line_numbers.mapToSource(sc.id, x, y, new ILineNumbers.DoneMapToSource() {
                public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                    if (error != null) {
                        exit(error);
                        return;
                    }
                    done.run();
                }
            });
        }
        else {
            done.run();
        }
    }

    private void runSymbolsTest(final SuspendedContext sc, final Runnable done) {
        if (srv_syms != null && sc.pc != null) {
            final BigInteger x = new BigInteger(sc.pc);
            srv_syms.findByAddr(sc.id, x, new ISymbols.DoneFind() {
                public void doneFind(IToken token, Exception error, String symbol_id) {
                    if (error != null) {
                        int code = IErrorReport.TCF_ERROR_OTHER;
                        if (error instanceof IErrorReport) code = ((IErrorReport)error).getErrorCode();
                        switch (code) {
                        case IErrorReport.TCF_ERROR_INV_COMMAND:
                        case IErrorReport.TCF_ERROR_SYM_NOT_FOUND:
                            done.run();
                            return;
                        default:
                            exit(error);
                            return;
                        }
                    }
                    srv_syms.getContext(symbol_id, new ISymbols.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception error, Symbol context) {
                            if (error != null) {
                                exit(error);
                                return;
                            }
                            Number addr = context.getAddress();
                            int size = context.getSize();
                            if (addr == null) {
                                exit(new Exception("Missing symbol address attribute"));
                                return;
                            }
                            if (size <= 0) {
                                exit(new Exception("Invalid symbol size attribute"));
                                return;
                            }
                            BigInteger y = new BigInteger(addr.toString());
                            BigInteger z = y.add(BigInteger.valueOf(size));
                            if (x.compareTo(y) < 0 || x.compareTo(z) >= 0) {
                                exit(new Exception("Invalid symbol address attribute"));
                                return;
                            }
                            String name = context.getName();
                            if (name == null) {
                                done.run();
                                return;
                            }
                            srv_syms.find(sc.id, 0, name, new ISymbols.DoneFind() {
                                public void doneFind(IToken token, Exception error, String symbol_id) {
                                    if (error != null) {
                                        exit(error);
                                        return;
                                    }
                                    done.run();
                                }
                            });
                        }
                    });
                }
            });
        }
        else {
            done.run();
        }
    }

    void cancel(final Runnable done) {
        if (srv_run_ctrl != null) srv_run_ctrl.removeListener(this);
        if (test_ctx_id == null) {
            if (pending_cancel != null) {
                exit(null);
            }
            else {
                pending_cancel = done;
            }
        }
        else if (cancel_test_cmd == null) {
            cancel_test_cmd = srv_diag.cancelTest(test_ctx_id, new IDiagnostics.DoneCancelTest() {
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
            if (srv_run_ctrl != null) srv_run_ctrl.removeListener(this);
        }
        if (srv_breakpoints != null) srv_breakpoints.removeListener(bp_listener);
        test_suite.done(this, x);
    }
}
