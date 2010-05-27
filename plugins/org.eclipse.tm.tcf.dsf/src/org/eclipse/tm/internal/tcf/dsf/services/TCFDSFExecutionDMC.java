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
package org.eclipse.tm.internal.tcf.dsf.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;

public abstract class TCFDSFExecutionDMC extends AbstractDMContext
        implements IExecutionDMContext, IContainerDMContext, IMemoryDMContext, IBreakpointsTargetDMContext {

    public final TCFDataCache<IMemory.MemoryContext> memory_context_cache;
    public final TCFDataCache<RunControlContext> run_control_context_cache;
    public final TCFDataCache<Map<String,TCFDSFExecutionDMC>> run_control_children_cache;
    public final TCFDataCache<TCFContextState> run_control_state_cache;

    TCFDataCache<?> stack_frames_cache;
    TCFDataCache<?> registers_cache;

    TCFDSFExecutionDMC(IChannel channel, IDsfService service, IDMContext[] parents) {
        super(service, parents);
        final IMemory tcf_mem_service = channel.getRemoteService(IMemory.class);
        final IRunControl tcf_run_service = channel.getRemoteService(IRunControl.class);
        memory_context_cache = new TCFDataCache<IMemory.MemoryContext>(channel) {
            @Override
            public boolean startDataRetrieval() {
                assert command == null;
                String id = getTcfContextId();
                if (id == null || tcf_mem_service == null) {
                    reset(null);
                    return true;
                }
                command = tcf_mem_service.getContext(id,
                        new org.eclipse.tm.tcf.services.IMemory.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception err,
                            org.eclipse.tm.tcf.services.IMemory.MemoryContext ctx) {
                        set(token, err, ctx);
                    }
                });
                return false;
            }
        };
        run_control_context_cache = new TCFDataCache<RunControlContext>(channel) {
            @Override
            public boolean startDataRetrieval() {
                assert command == null;
                String id = getTcfContextId();
                if (id == null || tcf_run_service == null) {
                    reset(null);
                    return true;
                }
                command = tcf_run_service.getContext(id, new IRunControl.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception err, IRunControl.RunControlContext ctx) {
                        set(token, err, ctx);
                    }
                });
                return false;
            }
        };
        run_control_children_cache = new TCFDataCache<Map<String,TCFDSFExecutionDMC>>(channel) {
            @Override
            public boolean startDataRetrieval() {
                assert command == null;
                if (tcf_run_service == null) {
                    reset(null);
                    return true;
                }
                String id = getTcfContextId();
                command = tcf_run_service.getChildren(id, new IRunControl.DoneGetChildren() {
                    public void doneGetChildren(IToken token, Exception err, String[] contexts) {
                        if (command != token) return;
                        HashMap<String,TCFDSFExecutionDMC> data = new HashMap<String,TCFDSFExecutionDMC>();
                        if (contexts != null) {
                            for (int i = 0; i < contexts.length; i++) {
                                String id = contexts[i];
                                TCFDSFExecutionDMC n = addChild(id);
                                data.put(id, n);
                            }
                        }
                        set(token, err, data);
                    }
                });
                return false;
            }
        };
        run_control_state_cache = new TCFDataCache<TCFContextState>(channel) {
            @Override
            public boolean startDataRetrieval() {
                assert command == null;
                if (!run_control_context_cache.validate(this)) return false;
                RunControlContext c = run_control_context_cache.getData();
                if (c == null || !c.hasState()) {
                    reset(null);
                    return true;
                }
                command = c.getState(new IRunControl.DoneGetState() {
                    public void doneGetState(IToken token, Exception err, boolean suspend, String pc, String reason, Map<String,Object> params) {
                        if (command != token) return;
                        TCFContextState data = new TCFContextState();
                        data.is_suspended = suspend;
                        if (suspend) {
                            data.suspend_pc = pc;
                            data.suspend_reason = reason;
                            data.suspend_params = params;
                        }
                        set(token, err, data);
                    }
                });
                return false;
            }
        };
    }

    public abstract void dispose();

    /**
     * Get TCF ID of execution context.
     * @return TCF ID.
     */
    public abstract String getTcfContextId();

    /**
     * Check if this context object is disposed, because, for example, a thread has exited.
     * @return true if context object is disposed.
     */
    public abstract boolean isDisposed();

    protected abstract TCFDSFExecutionDMC addChild(String id);
}
