/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;

import com.windriver.debug.tcf.core.model.TCFLaunch;
import com.windriver.debug.tcf.ui.commands.DisconnectCommand;
import com.windriver.debug.tcf.ui.commands.ResumeCommand;
import com.windriver.debug.tcf.ui.commands.StepIntoCommand;
import com.windriver.debug.tcf.ui.commands.StepOverCommand;
import com.windriver.debug.tcf.ui.commands.StepReturnCommand;
import com.windriver.debug.tcf.ui.commands.SuspendCommand;
import com.windriver.debug.tcf.ui.commands.TerminateCommand;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;

public class TCFModel implements IElementContentProvider, IElementLabelProvider,
        IModelProxyFactory, IColumnPresentationFactory {

    private final Display display;
    private final TCFLaunch launch;
    private final TCFNode launch_node;
    private final Map<IPresentationContext,TCFModelProxy> model_proxies =
        new HashMap<IPresentationContext,TCFModelProxy>();
    private final Map<String,TCFNode> id2node = new HashMap<String,TCFNode>();
    private final Map<TCFNode,ModelDelta> deltas = new HashMap<TCFNode,ModelDelta>();
    @SuppressWarnings("unchecked")
    private final Map<Class,Object> commands = new HashMap<Class,Object>();

    private final IMemory.MemoryListener mem_listener = new IMemory.MemoryListener() {

        public void contextAdded(IMemory.MemoryContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                TCFNode node = getNode(contexts[i].getParentID());
                if (node != null) node.onContextAdded(contexts[i]);
            }
            fireModelChanged();
        }

        public void contextChanged(IMemory.MemoryContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                TCFNode node = getNode(contexts[i].getID());
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextChanged(contexts[i]);
                }
            }
            fireModelChanged();
        }

        public void contextRemoved(String[] context_ids) {
            for (int i = 0; i < context_ids.length; i++) {
                TCFNode node = getNode(context_ids[i]);
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextRemoved();
                }
            }
            fireModelChanged();
        }

        public void memoryChanged(String context_id, Number[] addr, long[] size) {
            TCFNode node = getNode(context_id);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onMemoryChanged(addr, size);
            }
            fireModelChanged();
        }
    };

    private final IRunControl.RunControlListener run_listener = new IRunControl.RunControlListener() {

        public void containerResumed(String[] context_ids) {
            for (int i = 0; i < context_ids.length; i++) {
                TCFNode node = getNode(context_ids[i]);
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContainerResumed();
                }
            }
            fireModelChanged();
        }

        public void containerSuspended(String context, String pc, String reason,
                Map<String,Object> params, String[] suspended_ids) {
            for (int i = 0; i < suspended_ids.length; i++) {
                TCFNode node = getNode(suspended_ids[i]);
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContainerSuspended();
                }
            }
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextSuspended(pc, reason, params);
            }
            fireModelChanged();
        }

        public void contextAdded(IRunControl.RunControlContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                TCFNode node = getNode(contexts[i].getParentID());
                if (node != null) node.onContextAdded(contexts[i]);
            }
            fireModelChanged();
        }

        public void contextChanged(IRunControl.RunControlContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                TCFNode node = getNode(contexts[i].getID());
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextChanged(contexts[i]);
                }
            }
            fireModelChanged();
        }

        public void contextException(String context, String msg) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextException(msg);
            }
            fireModelChanged();
        }

        public void contextRemoved(String[] context_ids) {
            for (int i = 0; i < context_ids.length; i++) {
                TCFNode node = getNode(context_ids[i]);
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextRemoved();
                }
            }
            fireModelChanged();
        }

        public void contextResumed(String context) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextResumed();
            }
            fireModelChanged();
        }

        public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextSuspended(pc, reason, params);
            }
            fireModelChanged();
        }
    };

    TCFModel(Display display, TCFLaunch launch) {
        this.display = display;
        this.launch = launch;
        launch_node = new TCFNodeLaunch(TCFModel.this);
        commands.put(ISuspendHandler.class, new SuspendCommand(this));
        commands.put(IResumeHandler.class, new ResumeCommand(this));
        commands.put(ITerminateHandler.class, new TerminateCommand(this));
        commands.put(IDisconnectHandler.class, new DisconnectCommand(this));
        commands.put(IStepIntoHandler.class, new StepIntoCommand(this));
        commands.put(IStepOverHandler.class, new StepOverCommand(this));
        commands.put(IStepReturnHandler.class, new StepReturnCommand(this));
    }

    @SuppressWarnings("unchecked")
    public Object getCommand(Class c) {
        Object o = commands.get(c);
        assert o == null || c.isInstance(o);
        return o;
    }

    void onConnected() {
        assert Protocol.isDispatchThread();
        IMemory mem = launch.getService(IMemory.class);
        if (mem != null) mem.addListener(mem_listener);
        IRunControl run = launch.getService(IRunControl.class);
        if (run != null) run.addListener(run_listener);
        launch_node.invalidateNode();
        launch_node.makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
        fireModelChanged();
    }

    void onDisconnected() {
        assert Protocol.isDispatchThread();
        TCFNode[] a = id2node.values().toArray(new TCFNode[id2node.size()]);
        for (int i = 0; i < a.length; i++) {
            if (!a[i].isDisposed()) a[i].dispose();
        }
        launch_node.makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
        fireModelChanged();
    }

    void onProxyInstalled(final TCFModelProxy p) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                model_proxies.put(p.getPresentationContext(), p);
            }
        });
    }

    void onProxyDisposed(final TCFModelProxy p) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                model_proxies.remove(p.getPresentationContext());
            }
        });
    }
    
    void launchChanged() {
        launch_node.makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
        fireModelChanged();
    }

    void dispose() {
    }

    void addNode(String id, TCFNode node) {
        assert id != null;
        assert Protocol.isDispatchThread();
        assert id2node.get(id) == null;
        assert !node.isDisposed();
        id2node.put(id, node);
    }

    void removeNode(String id) {
        assert id != null;
        assert Protocol.isDispatchThread();
        id2node.remove(id);
    }

    ModelDelta getDelta(TCFNode node) {
        return deltas.get(node);
    }

    void addDelta(TCFNode node, ModelDelta delta) {
        assert deltas.get(node) == null;
        deltas.put(node, delta);
    }

    void fireModelChanged() {
        assert Protocol.isDispatchThread();
        ModelDelta delta = deltas.get(launch_node);
        assert (delta == null) == deltas.isEmpty();
        if (delta != null) {
            deltas.clear();
            IModelDelta top = delta.getParentDelta();
            for (TCFModelProxy p : model_proxies.values()) p.fireModelChanged(top);
        }
    }
    
    public Display getDisplay() {
        return display;
    }

    public TCFLaunch getLaunch() {
        return launch;
    }

    public TCFNode getRootNode() {
        return launch_node;
    }

    public TCFNode getNode(String id) {
        if (id == null) return null;
        if (id.equals("")) return launch_node;
        assert Protocol.isDispatchThread();
        return id2node.get(id);
    }

    public void update(IChildrenCountUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) launch_node.update(updates[i]);
            else ((TCFNode)o).update(updates[i]);
        }
    }

    public void update(IChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) launch_node.update(updates[i]);
            else ((TCFNode)o).update(updates[i]);
        }
    }

    public void update(IHasChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) launch_node.update(updates[i]);
            else ((TCFNode)o).update(updates[i]);
        }
    }

    public void update(ILabelUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            assert o != launch_node;
            if (o instanceof TCFLaunch) launch_node.update(updates[i]);
            else ((TCFNode)o).update(updates[i]);
        }
    }

    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        return new TCFModelProxy(this);
    }

    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        String id = getColumnPresentationId(context, element);
        if (id == null) return null;
        if (id.equals(TCFColumnPresentationRegister.PRESENTATION_ID)) return new TCFColumnPresentationRegister();
        return null;
    }

    public String getColumnPresentationId(IPresentationContext context, Object element) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
            return TCFColumnPresentationRegister.PRESENTATION_ID; 
        }
        return null;
    }
}
