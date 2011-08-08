/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildrenLogExpressions;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFBreakpointActions {
    
    private class BreakpointActionAdapter implements IAdaptable, ILogActionEnabler, IResumeActionEnabler {
        
        private final Job job;
        private final TCFNodeExecContext node;
        
        private boolean resumed;
        
        BreakpointActionAdapter(final IBreakpoint breakpoint, TCFNodeExecContext node) {
            this.node = node;
            job = new Job("Breakpoint actions") { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    BreakpointActionManager manager = CDebugCorePlugin.getDefault().getBreakpointActionManager();
                    manager.executeActions(breakpoint, BreakpointActionAdapter.this);
                    return Status.OK_STATUS;
                };
            };
            job.setSystem(true);
            job.schedule();
        }
        
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            if (adapter.equals(ILogActionEnabler.class)) {
                return this;
            }
            if (adapter.equals(IResumeActionEnabler.class)) {
                return this;
            }
            return null;
        }

        public void resume() throws Exception {
            Runnable r = new Runnable() {
                public void run() {
                    if (resumed) return;
                    if (node.isDisposed()) return;
                    TCFDataCache<TCFContextState> state_cache = node.getState();
                    if (!state_cache.validate(this)) return;
                    TCFContextState state_data = state_cache.getData();
                    if (state_data == null) return;
                    if (!state_data.is_suspended) return;
                    TCFDataCache<IRunControl.RunControlContext> ctx_cache = node.getRunContext();
                    if (!ctx_cache.validate(this)) return;
                    IRunControl.RunControlContext ctx_data = ctx_cache.getData();
                    if (ctx_data == null) return;
                    resumed = true;
                    ctx_data.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            // TODO: need to report resume action error
                        }
                    });
                }
            };
            Protocol.invokeLater(r);
        }

        public String evaluateExpression(final String expression) throws Exception {
            return new TCFTask<String>(node.getChannel()) {
                public void run() {
                    if (node.isDisposed()) {
                        done("");
                        return;
                    }
                    TCFChildrenLogExpressions cache = node.getLogExpressionCache();
                    cache.addScript(expression);
                    if (!cache.validate(this)) return;
                    TCFNodeExpression expr = cache.findScript(expression);
                    String s = expr.getValueText(this);
                    if (s != null) done(expression + " = " + s);
                }
            }.get();
        }
    }
    
    private class RunControlListener implements IRunControl.RunControlListener {
        
        private final IRunControl rc;
        private final TCFModel model;
        
        RunControlListener(TCFModel model) {
            this.model = model;
            rc = model.getLaunch().getService(IRunControl.class);
            if (rc != null) rc.addListener(this);
        }
        
        void dispose() {
            if (rc != null) rc.removeListener(this);
        }

        public void contextAdded(RunControlContext[] contexts) {
        }

        public void contextChanged(RunControlContext[] contexts) {
        }

        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                BreakpointActionAdapter a = active_actions.remove(id);
                if (a != null) a.resumed = true;
            }
        }

        public void contextSuspended(String context, String pc, String reason,
                Map<String, Object> params) {
            final TCFNodeExecContext node = (TCFNodeExecContext)model.getNode(context);
            if (node == null) return;
            Runnable r = new Runnable() {
                public void run() {
                    TCFDataCache<TCFContextState> state_cache = node.getState();
                    if (!state_cache.validate(this)) return;
                    TCFContextState state_data = state_cache.getData();
                    if (state_data == null) return;
                    if (state_data.suspend_params == null) return;
                    Object ids = state_data.suspend_params.get(IRunControl.STATE_BREAKPOINT_IDS);
                    if (ids == null) return;
                    @SuppressWarnings("unchecked")
                    Collection<String> c = (Collection<String>)ids;
                    for (String bp_id : c) {
                        IBreakpoint breakpoint = findPlatformBreakpoint(bp_id);
                        if (breakpoint == null) continue;
                        active_actions.put(node.getID(), new BreakpointActionAdapter(breakpoint, node));
                    }
                }
            };
            Protocol.invokeLater(r);
        }

        public void contextResumed(String context) {
            BreakpointActionAdapter a = active_actions.remove(context);
            if (a != null) a.resumed = true;
        }

        public void containerSuspended(String context, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            contextSuspended(context, pc, reason, params);
            for (String id : suspended_ids) {
                if (id.equals(context)) continue;
                contextSuspended(id, null, null, null);
            }
        }

        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) {
                contextResumed(id);
            }
        }

        public void contextException(String context, String msg) {
        }
    }
    
    private final TCFModelManager.ModelManagerListener launch_listener = new TCFModelManager.ModelManagerListener() {

        public void onConnected(TCFLaunch launch, TCFModel model) {
            assert rc_listeners.get(launch) == null;
            if (launch.getBreakpointsStatus() != null) new RunControlListener(model);
        }

        public void onDisconnected(TCFLaunch launch, TCFModel model) {
            RunControlListener l = rc_listeners.remove(launch);
            if (l != null) l.dispose();
        }
    };
    
    private final TCFModelManager model_manager;
    private final Map<TCFLaunch,RunControlListener> rc_listeners;;
    private final HashMap<String,BreakpointActionAdapter> active_actions = new HashMap<String,BreakpointActionAdapter>();

    TCFBreakpointActions() {
        model_manager = TCFModelManager.getModelManager();
        model_manager.addListener(launch_listener);
        rc_listeners = new HashMap<TCFLaunch,RunControlListener>();
        // handle already connected launches
        for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
            if (launch instanceof TCFLaunch) {
                TCFLaunch tcfLaunch = (TCFLaunch) launch;
                if (!tcfLaunch.isDisconnected() && !tcfLaunch.isConnecting() && tcfLaunch.getBreakpointsStatus() != null) {
                    launch_listener.onConnected(tcfLaunch, model_manager.getModel(tcfLaunch));
                }
            }
        }
    }
    
    void dispose() {
        model_manager.removeListener(launch_listener);
        for (RunControlListener l : rc_listeners.values()) l.dispose();
        rc_listeners.clear();
    }

    private IBreakpoint findPlatformBreakpoint(String id) {
        IBreakpoint[] bps = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
        for (IBreakpoint bp : bps) {
            try {
                if (id.equals(TCFBreakpointsModel.getBreakpointID(bp))) {
                    IMarker marker = bp.getMarker();
                    if (marker == null) return null;
                    String s = marker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, "");
                    if (s.length() == 0) return null;
                    return bp;
                }
            }
            catch (CoreException x) {
            }
        }
        return null;
    }
}
