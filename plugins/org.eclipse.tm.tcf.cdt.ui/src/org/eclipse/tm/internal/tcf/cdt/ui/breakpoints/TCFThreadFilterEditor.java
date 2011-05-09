/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Adapted to TCF
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildren;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFThreadFilterEditor {

    private static class Context {
        private final String fName;
        private final String fId;
        private final String fParentId;
        private final boolean fIsContainer;
        private final String fScopeId;
        private final String fSessionId;

        Context(IRunControl.RunControlContext ctx, Context parent) {
            this(ctx, parent.fSessionId);
        }
        Context(IRunControl.RunControlContext ctx, String sessionId) {
            String name = ctx.getName() != null ? ctx.getName() : ctx.getID();
            if (sessionId != null) {
                name += " - " + sessionId;
            }
            fName = name;
            fSessionId = sessionId;
            fScopeId = sessionId != null ? sessionId + '/' + ctx.getID() : ctx.getID();
            fId = ctx.getID();
            fParentId = ctx.getParentID();
            fIsContainer = ctx.isContainer();
        }
    }
    
    public class CheckHandler implements ICheckStateListener {
        public void checkStateChanged(CheckStateChangedEvent event) {
            Object element = event.getElement();
            if (element instanceof Context) {
                Context ctx = (Context) element;
                getThreadViewer().expandToLevel(ctx, 1);
                checkContext(ctx, event.getChecked());
                updateParentCheckState(ctx);
            }
        }

        /**
         * Check or uncheck a context in the tree viewer. When a container
         * is checked, attempt to check all of the containers threads by
         * default. When a container is unchecked, uncheck all its threads.
         */
        protected void checkContext(Context ctx, boolean checked) {
            if (ctx.fIsContainer) {
                Context[] threads = syncGetThreads(ctx);
                for (int i = 0; i < threads.length; i++) {
                    checkContext(threads[i], checked);
                }
            }
            checkThread(ctx, checked);
        }

        /**
         * Check or uncheck a thread.
         */
        protected void checkThread(Context thread, boolean checked) {
            getThreadViewer().setChecked(thread, checked);
            getThreadViewer().setGrayed(thread, false);
        }

        protected void updateParentCheckState(Context thread) {
            Context parent = getContainer(thread);
            if (parent == null) {
                return;
            }
            Context[] threads = syncGetThreads(parent);
            int checkedNumber = 0;
            int grayedNumber = 0;
            for (int i = 0; i < threads.length; i++) {
                if (getThreadViewer().getGrayed(threads[i])) {
                    ++grayedNumber;
                } else if (getThreadViewer().getChecked(threads[i])) {
                    ++checkedNumber;
                }
            }
            if (checkedNumber + grayedNumber == 0) {
                getThreadViewer().setChecked(parent, false);
                getThreadViewer().setGrayed(parent, false);
            } else if (checkedNumber == threads.length) {
                getThreadViewer().setChecked(parent, true);
                getThreadViewer().setGrayed(parent, false);
            } else {
                getThreadViewer().setGrayChecked(parent, true);
            }
            updateParentCheckState(parent);
        }
    }

    public class ThreadFilterContentProvider implements ITreeContentProvider {

        public Object[] getChildren(Object parent) {
            if (parent instanceof Context) {
                return syncGetThreads((Context) parent);
            }

            if (parent instanceof ILaunchManager) {
                List<Object> children = new ArrayList<Object>();
                ILaunch[] launches = ((ILaunchManager) parent).getLaunches();
                Context[] targetArray;
                for (int i = 0; i < launches.length; i++) {
                    if (launches[i] instanceof TCFLaunch) {
                        targetArray = syncGetContainers((TCFLaunch) launches[i]);
                        children.addAll(Arrays.asList(targetArray));
                    }
                }
                return children.toArray();
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                if (ctx.fParentId == null) {
                    return DebugPlugin.getDefault().getLaunchManager();
                } else {
                    return getContainer(ctx);
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public class ThreadFilterLabelProvider extends LabelProvider  {
        
        @Override
        public Image getImage(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                if (ctx.fIsContainer) {
                    return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
                } else {
                    return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
                }
            }
            return null;
        }
        
        @Override
        public String getText(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                return ctx.fName;
            }
            return "?";
        }
    }

    
    private TCFBreakpointThreadFilterPage fPage;
    private CheckboxTreeViewer fThreadViewer;
    private final ThreadFilterContentProvider fContentProvider;
    private final CheckHandler fCheckHandler;
    private final List<Context> fContexts = new ArrayList<Context>();
    private final Map<TCFLaunch, Context[]> fContainersPerLaunch = new HashMap<TCFLaunch, Context[]>();
    private final Map<Context, Context[]> fContextsPerContainer = new HashMap<Context, Context[]>();

    public TCFThreadFilterEditor(Composite parent, TCFBreakpointThreadFilterPage page) {
        fPage = page;
        fContentProvider = new ThreadFilterContentProvider();
        fCheckHandler = new CheckHandler();
        createThreadViewer(parent);
    }

    protected TCFBreakpointThreadFilterPage getPage() {
        return fPage;
    }

    private void createThreadViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Restrict to Selected Contexts:"); //$NON-NLS-1$
        label.setFont(parent.getFont());
        label.setLayoutData(new GridData());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        fThreadViewer = new CheckboxTreeViewer(parent, SWT.BORDER);
        fThreadViewer.addCheckStateListener(fCheckHandler);
        fThreadViewer.getTree().setLayoutData(data);
        fThreadViewer.getTree().setFont(parent.getFont());
        fThreadViewer.setContentProvider(fContentProvider);
        fThreadViewer.setLabelProvider(new ThreadFilterLabelProvider());
        fThreadViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
        setInitialCheckedState();
    }

    /**
     * Returns the root contexts that appear in the tree
     */
    protected Context[] getRootContexts() {
        Object input = fThreadViewer.getInput();
        if (!(input instanceof ILaunchManager)) {
            return new Context[0];
        }
        List<Object> targets = new ArrayList<Object>();
        ILaunch[] launches = ((ILaunchManager) input).getLaunches();
        for (int i = 0; i < launches.length; i++) {
            ILaunch launch = launches[i];
            if (launch instanceof TCFLaunch && !launch.isTerminated()) {
                Context[] targetArray = syncGetContainers((TCFLaunch) launch);
                targets.addAll(Arrays.asList(targetArray));
            }
        }
        return targets.toArray(new Context[targets.size()]);
    }

    protected final CheckboxTreeViewer getThreadViewer() {
        return fThreadViewer;
    }

    /**
     * Sets the initial checked state of the tree viewer. The initial state
     * should reflect the current state of the breakpoint. If the breakpoint has
     * a thread filter in a given thread, that thread should be checked.
     */
    protected void setInitialCheckedState() {
        TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
        if (filterExtension == null) {
            return;
        }
        String[] ctxIds = filterExtension.getThreadFilters();

        if (ctxIds == null) {
            Context[] contexts = getRootContexts();
            for (Context context : contexts) {
                fCheckHandler.checkContext(context, true);
            }
        } else if (ctxIds.length != 0) {
            // expand all to realize tree items
            getThreadViewer().expandAll();
            for (int i = 0; i < ctxIds.length; i++) {
                String id = ctxIds[i];
                Context ctx = getContext(id);
                if (ctx != null) {
                    fCheckHandler.checkContext(ctx, true);
                    fCheckHandler.updateParentCheckState(ctx);
                } else if (id.indexOf('/') < 0) {
                    for (Context context : fContexts) {
                        if (id.equals(context.fId)) {
                            fCheckHandler.checkContext(context, true);
                            fCheckHandler.updateParentCheckState(context);
                        }
                    }
                }
            }
            // expand checked items only
            getThreadViewer().setExpandedElements(getThreadViewer().getCheckedElements());
        }
    }

    private Context getContainer(Context child) {
        String parentId = child.fSessionId != null ? child.fSessionId + '/' + child.fParentId : child.fParentId;
        return getContext(parentId);
    }

    private Context getContext(String id) {
        for (Context ctx : fContexts) {
            if (ctx.fScopeId.equals(id))
                return ctx;
        }
        return null;
    }

    protected void doStore() {
        CheckboxTreeViewer viewer = getThreadViewer();
        Object[] elements = viewer.getCheckedElements();
        String[] threadIds;
        if (elements.length == fContexts.size()) {
            threadIds = null;
        } else {
            List<String> checkedIds = new ArrayList<String>();
            for (int i = 0; i < elements.length; ++i) {
                Context ctx = (Context) elements[i];
                if (!viewer.getGrayed(ctx)) {
                    checkedIds.add(ctx.fScopeId);
                }
            }
            threadIds = (String[]) checkedIds.toArray(new String[checkedIds.size()]);
        }
        TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
        if (filterExtension == null) {
            return;
        }
        filterExtension.setThreadFilter(threadIds);
        DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(fPage.getBreakpoint());
    }

    private Context[] syncGetContainers(final TCFLaunch launch) {
        Context[] result = fContainersPerLaunch.get(launch);
        if (result != null) {
            return result;
        }
        final String launchCfgName = launch.getLaunchConfiguration().getName();
        result = new TCFTask<Context[]>(launch.getChannel()) {
            public void run() {
                List<Context> containers = new ArrayList<Context>();
                TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                TCFChildren children = model.getRootNode().getChildren();
                if (!children.validate(this)) return;
                Map<String, TCFNode> childMap = children.getData();
                for (TCFNode node : childMap.values()) {
                    if (node instanceof TCFNodeExecContext) {
                        TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                        TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                        if (!runCtxCache.validate(this)) return;
                        IRunControl.RunControlContext runCtx = runCtxCache.getData();
                        containers.add(new Context(runCtx, launchCfgName));
                    }
                }
                done((Context[]) containers.toArray(new Context[containers.size()]));
            }
        }.getE();
        fContexts.addAll(Arrays.asList(result));
        fContainersPerLaunch.put(launch, result);
        return result;
    }

    private Context[] syncGetThreads(final Context container) {
        Context[] result = fContextsPerContainer.get(container);
        if (result != null) {
            return result;
        }
        final TCFLaunch launch = getLaunch(container);
        result = new TCFTask<Context[]>(launch.getChannel()) {
            public void run() {
                List<Context> contexts = new ArrayList<Context>();
                TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                TCFChildren children = ((TCFNodeExecContext) model.getNode(container.fId)).getChildren();
                if (!children.validate(this)) return;
                Map<String, TCFNode> childMap = children.getData();
                for (TCFNode node : childMap.values()) {
                    if (node instanceof TCFNodeExecContext) {
                        TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                        TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                        if (!runCtxCache.validate(this)) return;
                        IRunControl.RunControlContext runCtx = runCtxCache.getData();
                        contexts.add(new Context(runCtx, container));
                    }
                }
                done((Context[]) contexts.toArray(new Context[contexts.size()]));
            }
        }.getE();
        fContextsPerContainer.put(container, result);
        fContexts.addAll(Arrays.asList(result));
        return result;
    }

    private TCFLaunch getLaunch(Context container) {
        Context parent = getContainer(container);
        while (parent != null) {
            container = parent;
            parent = getContainer(container);
        }
        for (TCFLaunch launch : fContainersPerLaunch.keySet()) {
            Context[] containers = fContainersPerLaunch.get(launch);
            for (Context context : containers) {
                if (context.fScopeId.equals(container.fScopeId)) {
                    return launch;
                }
            }
        }
        return null;
    }

}
