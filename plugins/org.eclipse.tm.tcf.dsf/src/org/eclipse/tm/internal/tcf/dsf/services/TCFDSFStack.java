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
package org.eclipse.tm.internal.tcf.dsf.services;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.osgi.framework.BundleContext;


public class TCFDSFStack extends AbstractDsfService implements IStack {
    
    public class TCFFrameDMC extends AbstractDMContext implements IFrameDMContext, Comparable<TCFFrameDMC> {
        
        public final String id;
        public final TCFDSFExecutionDMC exe_dmc;
        public final TCFDataCache<IStackTrace.StackTraceContext> context_cache;
        public final TCFDataCache<TCFSourceRef> source_cache;
        
        int level;
        TCFSourceRef prev_data;
        
        public TCFFrameDMC(final TCFDSFExecutionDMC exe_dmc, final String id) {
            super(TCFDSFStack.this.getSession().getId(), new IDMContext[] { exe_dmc });
            this.id = id;
            this.exe_dmc = exe_dmc;
            context_cache = new TCFDataCache<IStackTrace.StackTraceContext>(channel) {
                
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    if (id == null || tcf_stk_service == null) {
                        reset(null);
                        return true;
                    }
                    command = tcf_stk_service.getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception err, IStackTrace.StackTraceContext[] context) {
                            if (command != token) return;
                            IStackTrace.StackTraceContext ctx = null;
                            if (context != null && context.length > 0) ctx = context[0];
                            set(token, err, ctx);
                        }
                    });
                    return false;
                }
            };
            
            source_cache = new TCFDataCache<TCFSourceRef>(channel) {

                @Override
                protected boolean startDataRetrieval() {
                    if (!context_cache.validate()) {
                        context_cache.wait(this);
                        return false;
                    }
                    IStackTrace.StackTraceContext ctx = context_cache.getData();
                    Number n = ctx.getInstructionAddress();
                    BigInteger a = null;
                    if (n != null) a = new BigInteger(n.toString());
                    // Optimization: skip source position lookup if same address
                    TCFSourceRef data = null;
                    if (prev_data != null && prev_data.address != null && prev_data.address.equals(a)) {
                        data = prev_data;
                    }
                    else {
                        data = new TCFSourceRef();
                        data.address = a;
                        if (!getSourcePos(data)) return false;
                    }
                    set(null, null, prev_data = data);
                    return true;
                }
                
                private boolean getSourcePos(final TCFSourceRef data) {
                    if (tcf_lns_service == null) return true;
                    if (data.address == null) return true;
                    BigInteger a1 = data.address;
                    BigInteger a2 = data.address.add(BigInteger.valueOf(1));
                    command = tcf_lns_service.mapToSource(exe_dmc.getTcfContextId(), a1, a2, new ILineNumbers.DoneMapToSource() {
                        public void doneMapToSource(IToken token, Exception err, CodeArea[] areas) {
                            if (command != token) return;
                            if (areas != null && areas.length > 0) {
                                for (ILineNumbers.CodeArea area : areas) {
                                    if (data.area == null || area.start_line < data.area.start_line) {
                                        data.area = area;
                                    }
                                }
                            }
                            set(token, err, prev_data = data);
                        }
                    });
                    return false;
                }
            };
        }
        
        public int getLevel() {
            return level;
        }
        
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((TCFFrameDMC)other).id.equals(id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
        @Override
        public String toString() { 
            return baseToString() + ".frame[" + id + "]";  //$NON-NLS-1$ //$NON-NLS-2$
        }

        public int compareTo(TCFFrameDMC f) {
            if (level < f.level) return -1;
            if (level > f.level) return +1;
            return 0;
        }    
    }
    
    public static class TCFFrameData implements IFrameDMData {
        
        public final IStackTrace.StackTraceContext context;
        public final IAddress address;
        public final int level;
        public final String function;
        public final ILineNumbers.CodeArea code_area;
        
        TCFFrameData(TCFFrameDMC dmc) {
            context = dmc.context_cache.getData();
            TCFSourceRef ref = dmc.source_cache.getData();
            address = new TCFAddress(ref.address);
            level = dmc.getLevel();
            function = null;
            code_area = ref.area;
        }

        public IAddress getAddress() {
            return address;
        }
        
        public String getFunction() {
            return function;
        }

        public int getLevel() {
            return level;
        }

        public String getFile() {
            if (code_area == null) return null;
            return code_area.file;
        }

        public int getLine() {
            if (code_area == null) return -1;
            return code_area.start_line;
        }

        public int getColumn() {
            if (code_area == null) return -1;
            return code_area.start_column;
        }
    }
    
    private class FramesCache extends TCFDataCache<Map<String,TCFFrameDMC>> {
        
        private final TCFDSFExecutionDMC dmc;
        private final Map<String,TCFFrameDMC> frame_pool;
        
        private String top_frame_id;
        
        FramesCache(IChannel channel, TCFDSFExecutionDMC dmc) {
            super(channel);
            this.dmc = dmc;
            frame_pool = new HashMap<String,TCFFrameDMC>();
        }
        
        @Override
        public boolean startDataRetrieval() {
            assert command == null;
            if (!dmc.run_control_context_cache.validate()) {
                dmc.run_control_context_cache.wait(this);
                return false;
            }
            if (dmc.run_control_context_cache.getError() != null) {
                set(null, dmc.run_control_context_cache.getError(), null);
                return true;
            }
            org.eclipse.tm.tcf.services.IRunControl.RunControlContext ctx = dmc.run_control_context_cache.getData();
            if (ctx == null || !ctx.hasState()) {
                set(null, new Exception("DMC does not have a stack"), null); //$NON-NLS-1$
                return true;
            }
            if (tcf_stk_service == null) {
                HashMap<String,TCFFrameDMC> data = new HashMap<String,TCFFrameDMC>();
                top_frame_id = "TopFrame:" + dmc.getTcfContextId();
                data.put(top_frame_id, createFrameDMC(top_frame_id, 0));
                set(null, null, data);
                return true;
            }
            assert !dmc.isDisposed();
            command = tcf_stk_service.getChildren(dmc.getTcfContextId(), new IStackTrace.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception err, String[] contexts) {
                    if (command != token) return;
                    HashMap<String,TCFFrameDMC> data = new HashMap<String,TCFFrameDMC>();
                    if (contexts != null) {
                        for (int i = 0; i < contexts.length; i++) {
                            String id = contexts[i];
                            data.put(id, createFrameDMC(id, contexts.length - i - 1));
                        }
                    }
                    set(token, err, data);
                }
            });
            return false;
        }
        
        TCFFrameDMC createFrameDMC(String id, int level) {
            TCFFrameDMC n = frame_pool.get(id);
            if (n == null) frame_pool.put(id, n = new TCFFrameDMC(dmc, id));
            n.level = level;
            if (n.level == 0) top_frame_id = id;
            return n;
        }

        void invalidateFrames() {
            reset();
            for (TCFFrameDMC dmc : frame_pool.values()) {
                dmc.context_cache.reset();
                dmc.source_cache.reset();
            }
        }
        
        void dispose() {
        }
    }

    private final IChannel channel;
    private final IStackTrace tcf_stk_service;
    private final ILineNumbers tcf_lns_service;

    public TCFDSFStack(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.channel = channel;
        tcf_stk_service = channel.getRemoteService(IStackTrace.class);
        tcf_lns_service = channel.getRemoteService(ILineNumbers.class);
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleSuccess() {
                String[] class_names = {
                        IStack.class.getName(),
                        TCFDSFStack.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                getSession().addServiceEventListener(TCFDSFStack.this, null);
                monitor.done();
            }
        });
    }

    @Override 
    public void shutdown(RequestMonitor monitor) {
        getSession().removeServiceEventListener(this);
        unregister();
        super.shutdown(monitor);
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    public void getArguments(IFrameDMContext dmc, DataRequestMonitor<IVariableDMContext[]> rm) {
        if (dmc instanceof TCFFrameDMC) {
            // TODO function arguments
            rm.setData(new IVariableDMContext[0]);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getFrameData(final IFrameDMContext dmc, final DataRequestMonitor<IFrameDMData> rm) {
        if (dmc instanceof TCFFrameDMC) {
            final TCFFrameDMC frame_dmc = (TCFFrameDMC)dmc;
            if (!frame_dmc.context_cache.validate()) {
                frame_dmc.context_cache.wait(new Runnable() {
                    public void run() {
                        getFrameData(dmc, rm);
                    }
                });
                return;
            }
            if (frame_dmc.context_cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", frame_dmc.context_cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (!frame_dmc.source_cache.validate()) {
                frame_dmc.source_cache.wait(new Runnable() {
                    public void run() {
                        getFrameData(dmc, rm);
                    }
                });
                return;
            }
            if (frame_dmc.source_cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", frame_dmc.source_cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            rm.setData(new TCFFrameData(frame_dmc));
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    public TCFDataCache<?> getFramesCache(TCFDSFExecutionDMC exe, DataRequestMonitor<?> rm) {
        if (tcf_stk_service == null) {
            if (rm != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Stack trace service is not available", null)); //$NON-NLS-1$
                rm.done();
            }
            return null;
        }
        if (exe.isDisposed()) {
            if (rm != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
            }
            return null;
        }
        if (exe.stack_frames_cache == null) exe.stack_frames_cache = new FramesCache(channel, exe);
        return (FramesCache)exe.stack_frames_cache;
    }
    
    public void getFrames(final IDMContext dmc, final DataRequestMonitor<IFrameDMContext[]> rm) {
        if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            FramesCache cache = (FramesCache)getFramesCache(exe, rm);
            if (cache == null) return;
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getFrames(dmc, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,TCFFrameDMC> map = cache.getData();
            TCFFrameDMC[] arr = map.values().toArray(new TCFFrameDMC[map.size()]);
            Arrays.sort(arr);
            rm.setData(arr);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getLocals(IFrameDMContext dmc, DataRequestMonitor<IVariableDMContext[]> rm) {
        if (dmc instanceof TCFFrameDMC) {
            // TODO function local variables
            rm.setData(new IVariableDMContext[0]);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getStackDepth(final IDMContext dmc, final int maxDepth, final DataRequestMonitor<Integer> rm) {
        if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            FramesCache cache = (FramesCache)getFramesCache(exe, rm);
            if (cache == null) return;
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getStackDepth(dmc, maxDepth, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            rm.setData(cache.getData().size());
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getTopFrame(final IDMContext dmc, final DataRequestMonitor<IFrameDMContext> rm) {
        if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            FramesCache cache = (FramesCache)getFramesCache(exe, rm);
            if (cache == null) return;
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getTopFrame(dmc, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            rm.setData(cache.createFrameDMC(cache.top_frame_id, 0));
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    public TCFFrameDMC getTopFrame(TCFDSFExecutionDMC exe) {
        FramesCache cache = (FramesCache)getFramesCache(exe, null);
        assert cache != null;
        assert cache.isValid();
        assert cache.getError() == null;
        return cache.createFrameDMC(cache.top_frame_id, 0);
    }

    public void getVariableData(IVariableDMContext variableDmc, DataRequestMonitor<IVariableDMData> rm) {
        // TODO model data for local variables
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        rm.done();
    }

    public boolean isStackAvailable(IDMContext dmc) {
        return tcf_stk_service != null && dmc instanceof TCFDSFExecutionDMC;
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof IFrameDMContext) {
            getFrameData((IFrameDMContext)dmc, (DataRequestMonitor<IFrameDMData>)rm);
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
        if (e.getReason() != StateChangeReason.STEP) {
            FramesCache cache = (FramesCache)((TCFDSFExecutionDMC)e.getDMContext()).stack_frames_cache;
            if (cache != null) cache.invalidateFrames();
        }
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
        FramesCache cache = (FramesCache)((TCFDSFExecutionDMC)e.getDMContext()).stack_frames_cache;
        if (cache != null) cache.invalidateFrames();
    }

    @DsfServiceEventHandler
    public void eventDispatched(IRunControl.IExitedDMEvent e) {
        FramesCache cache = (FramesCache)((TCFDSFExecutionDMC)e.getDMContext()).stack_frames_cache;
        if (cache != null) cache.dispose();
    }
}
