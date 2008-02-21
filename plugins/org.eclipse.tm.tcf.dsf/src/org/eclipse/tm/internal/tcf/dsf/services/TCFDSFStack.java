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
import org.eclipse.dd.dsf.datamodel.ServiceDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.osgi.framework.BundleContext;


public class TCFDSFStack extends AbstractDsfService implements IStack {
    
    private static final String TOP_FRAME = "TopFrame:";

    private class TCFFrameDMC extends AbstractDMContext implements IFrameDMContext, Comparable<TCFFrameDMC> {
        
        final String id;
        final TCFDSFExecutionDMC exe_dmc;
        final TCFDataCache<TCFFrameData> frame_data;
        
        int level;
        TCFFrameData prev_data;
        
        public TCFFrameDMC(final TCFDSFExecutionDMC exe_dmc, final String id) {
            super(TCFDSFStack.this.getSession().getId(), new IDMContext[] { exe_dmc });
            this.id = id;
            this.exe_dmc = exe_dmc;
            frame_data = new TCFDataCache<TCFFrameData>(channel) {
                
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    if (id == null || tcf_stk_service == null) {
                        data = null;
                        valid = true;
                        return true;
                    }
                    if (level == 0) {
                        assert id.startsWith(TOP_FRAME);
                        // Top frame is special case: most of its data is stored in CPU registers.
                        // Other frames are stored in memory - in thread stack area.
                        return getTopFrame();
                    }
                    command = tcf_stk_service.getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception err, IStackTrace.StackTraceContext[] context) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                error = err;
                                data = null;
                            }
                            else {
                                TCFAddress a = null;
                                Number n = context[0].getReturnAddress();
                                if (n != null) a = new TCFAddress(n);
                                // Optimization: skip source position lookup if same address
                                if (prev_data != null && prev_data.address.equals(a)) {
                                    data = prev_data;
                                    data.context = context[0];
                                    data.level = level;
                                }
                                else {
                                    data = new TCFFrameData();
                                    data.context = context[0];
                                    data.address = a;
                                    data.level = level;
                                    if (!getSourcePos()) return;
                                }
                            }
                            valid = true;
                            validate();
                        }
                    });
                    return false;
                }
                
                private boolean getTopFrame() {
                    assert level == 0;
                    if (!exe_dmc.validateState()) {
                        exe_dmc.addStateWaitingRequest(new IDataRequest() {
                            public void cancel() {
                                reset();
                            }
                            public void done() {
                                validate();
                            }
                        });
                        return false;
                    }
                    prev_data = data = new TCFFrameData();
                    data.address = exe_dmc.getPC();
                    data.level = level;
                    if (!getSourcePos()) return false;
                    valid = true;
                    return true;
                }
                
                private boolean getSourcePos() {
                    if (tcf_lns_service == null) return true;
                    if (data.address == null) return true;
                    BigInteger a1 = data.address.getValue();
                    BigInteger a2 = data.address.add(1).getValue();
                    command = tcf_lns_service.mapToSource(exe_dmc.getTcfContextId(), a1, a2, new ILineNumbers.DoneMapToSource() {

                        public void doneMapToSource(IToken token, Exception err, CodeArea[] areas) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                data.src_pos_error = err;
                            }
                            else if (areas != null && areas.length > 0) {
                                for (ILineNumbers.CodeArea area : areas) {
                                    if (data.code_area == null || area.start_line < data.code_area.start_line) {
                                        data.code_area = area;
                                    }
                                }
                                prev_data = data;
                            }
                            valid = true;
                            validate();
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
    
    private static class TCFFrameData implements IFrameDMData {
        
        IStackTrace.StackTraceContext context;
        IAddress address;
        int level;
        String function;
        Throwable src_pos_error;
        ILineNumbers.CodeArea code_area;

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
            return code_area.start_line + 1;
        }

        public int getColumn() {
            if (code_area == null) return -1;
            return code_area.start_column + 1;
        }
    }
    
    private class FramesCache extends TCFDataCache<Map<String,TCFFrameDMC>> implements TCFDSFExecutionDMC.DataCache {
        
        private final TCFDSFExecutionDMC dmc;
        private final Map<String,TCFFrameDMC> frame_pool;
        
        FramesCache(IChannel channel, TCFDSFExecutionDMC dmc) {
            super(channel);
            this.dmc = dmc;
            frame_pool = new HashMap<String,TCFFrameDMC>();
        }
        
        @Override
        public boolean startDataRetrieval() {
            assert command == null;
            if (tcf_stk_service == null) {
                data = null;
                valid = true;
                return true;
            }
            assert !dmc.isDisposed();
            command = tcf_stk_service.getChildren(dmc.getTcfContextId(), new IStackTrace.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception err, String[] contexts) {
                    if (command != token) return;
                    command = null;
                    if (err != null) {
                        data = null;
                        error = err;
                    }
                    else {
                        data = new HashMap<String,TCFFrameDMC>();
                        for (int i = 0; i < contexts.length; i++) {
                            String id = contexts[i];
                            TCFFrameDMC n = frame_pool.get(id);
                            if (n == null) frame_pool.put(id, n = new TCFFrameDMC(dmc, id));
                            n.level = contexts.length - i;
                            data.put(id, n);
                        }
                        String id = TOP_FRAME + dmc.getTcfContextId();
                        TCFFrameDMC n = frame_pool.get(id);
                        if (n == null) frame_pool.put(id, n = new TCFFrameDMC(dmc, id));
                        n.level = 0;
                        data.put(id, n);
                    }
                    valid = true;
                    validate();
                }
            });
            return false;
        }

        void invalidateFrames() {
            reset();
            for (TCFFrameDMC dmc : frame_pool.values()) dmc.frame_data.reset();
        }
        
        void dispose() {
        }
    }

    private final IChannel channel;
    private final IStackTrace tcf_stk_service;
    private final ILineNumbers tcf_lns_service;
    private IDMContext service_dmc;

    public TCFDSFStack(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.channel = channel;
        tcf_stk_service = channel.getRemoteService(IStackTrace.class);
        tcf_lns_service = channel.getRemoteService(ILineNumbers.class);
        service_dmc = new ServiceDMContext(this, "#stack_trace");
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleOK() {
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

    public IDMContext getServiceContext() {
        return service_dmc;
    }

    public void getArguments(IDMContext dmc, DataRequestMonitor<IVariableDMContext[]> rm) {
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
            TCFDataCache<TCFFrameData> cache = frame_dmc.frame_data;
            if (!cache.validate()) {
                cache.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getFrameData(dmc, rm);
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
            rm.setData(cache.getData());
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    public void getFrames(final IDMContext dmc, final DataRequestMonitor<IFrameDMContext[]> rm) {
        if (tcf_stk_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Stack trace service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            if (exe.isDisposed()) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (exe.stack_frames_cache == null) exe.stack_frames_cache = new FramesCache(channel, exe);
            FramesCache cache = (FramesCache)exe.stack_frames_cache;
            if (!cache.validate()) {
                cache.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
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
            Map<String,TCFFrameDMC> c = cache.getData();
            TCFFrameDMC[] arr = c.values().toArray(new TCFFrameDMC[c.size()]);
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

    public void getLocals(IDMContext dmc, DataRequestMonitor<IVariableDMContext[]> rm) {
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

    public void getStackDepth(DataRequestMonitor<Integer> rm) {
        // TODO don't know what getStackDepth() is supposed to return
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getStackDepth(int maxDepth, DataRequestMonitor<Integer> rm) {
        // TODO don't know what getStackDepth() is supposed to return
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getTopFrame(IDMContext dmc, DataRequestMonitor<IFrameDMContext> rm) {
        if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            if (exe.isDisposed()) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (exe.stack_frames_cache == null) exe.stack_frames_cache = new FramesCache(channel, exe);
            FramesCache cache = (FramesCache)exe.stack_frames_cache;
            String id = TOP_FRAME + exe.getTcfContextId();
            TCFFrameDMC n = cache.frame_pool.get(id);
            if (n == null) cache.frame_pool.put(id, n = new TCFFrameDMC(exe, id));
            n.level = 0;
            rm.setData(n);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
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
        else if (dmc == service_dmc) {
            ((DataRequestMonitor<TCFDSFStack>)rm).setData(this);
            rm.done();
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
        FramesCache cache = (FramesCache)((TCFDSFExecutionDMC)e.getExecutionContext()).stack_frames_cache;
        if (cache != null) cache.dispose();
    }
}
