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
package org.eclipse.tm.internal.tcf.cdt.ui.breakpoints;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.tcf.cdt.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsStatus;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.dialogs.PropertyPage;

public class TCFBreakpointStatusPage extends PropertyPage {

    private TreeViewer viewer;
    private List<StatusItem> status;

    private static class StatusItem implements Comparable<StatusItem> {
        Object object;
        String text;
        boolean has_state;
        boolean planted_ok;
        List<StatusItem> children;
        StatusItem parent;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public int compareTo(StatusItem n) {
            if (object instanceof TCFNode) {
                if (n.object instanceof TCFNode) {
                    return ((Comparable)object).compareTo(n.object);
                }
                return 1;
            }
            if (n.object instanceof TCFNode) return -1;
            return 0;
        }
    }

    private class StatusCache extends TCFDataCache<StatusItem> {

        final TCFLaunch launch;

        TCFDataCache<?> pending;

        public StatusCache(TCFLaunch launch) {
            super(launch.getChannel());
            this.launch = launch;
        }

        @Override
        protected boolean startDataRetrieval() {
            pending = null;
            TCFBreakpointsStatus status = launch.getBreakpointsStatus();
            if (status == null) {
                set(null, null, null);
                return true;
            }
            Map<String,Object> map = status.getStatus(getBreakpoint());
            if (map == null || map.size() == 0) {
                set(null, null, null);
                return true;
            }
            StatusItem x = new StatusItem();
            x.object = launch;
            Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
            if (planted != null) {
                TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                for (Object o : toObjectArray(planted)) {
                    Map<String,Object> m = toObjectMap(o);
                    String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                    if (!model.createNode(ctx_id, this)) return false;
                    if (isValid()) {
                        reset();
                    }
                    else {
                        StatusItem y = getNodeItem(x, model.getNode(ctx_id));
                        if (y != null) {
                            StatusItem z = new StatusItem();
                            z.text = (String)m.get(IBreakpoints.INSTANCE_ERROR);
                            if (z.text == null) {
                                Number addr = (Number)m.get(IBreakpoints.INSTANCE_ADDRESS);
                                if (addr != null) {
                                    BigInteger i = JSON.toBigInteger(addr);
                                    z.text = "Planted at 0x" + i.toString(16);
                                    z.planted_ok = true;
                                    Number size = (Number)m.get(IBreakpoints.INSTANCE_SIZE);
                                    if (size != null) z.text += "; Size " + size;
                                    String type = (String)m.get(IBreakpoints.INSTANCE_TYPE);
                                    if (type != null) z.text += "; Type: " + type;
                                }
                            }
                            z.parent = y;
                            if (y.children == null) y.children = new ArrayList<StatusItem>();
                            y.children.add(z);
                        }
                    }
                }
            }
            if (pending != null) {
                pending.wait(this);
                return false;
            }
            String error = (String)map.get(IBreakpoints.STATUS_ERROR);
            if (error != null) {
                StatusItem y = new StatusItem();
                y.text = error;
                y.parent = x;
                x.children = new ArrayList<StatusItem>();
                x.children.add(y);
            }
            set(null, null, x);
            return true;
        }

        private StatusItem getNodeItem(StatusItem root, TCFNode node) {
            TCFNode parent = node.getParent();
            if (parent == null) return root;
            StatusItem x = null; // parent status item
            Set<String> filter = launch.getContextFilter();
            if (filter != null) {
                if (filter.contains(node.getID())) x = root;
                else if (parent instanceof TCFNodeLaunch) return null;
            }
            if (x == null) x = getNodeItem(root, parent);
            if (x == null) return null;
            if (x.children == null) x.children = new ArrayList<StatusItem>();
            for (StatusItem y : x.children) {
                if (y.object == node) return y;
            }
            StatusItem y = new StatusItem();
            y.object = node;
            TCFDataCache<IRunControl.RunControlContext> cache = ((TCFNodeExecContext)node).getRunContext();
            if (!cache.validate()) {
                pending = cache;
            }
            else {
                IRunControl.RunControlContext ctx = cache.getData();
                if (ctx != null) {
                    y.text = ctx.getName();
                    y.has_state = ctx.hasState();
                }
                if (y.text == null) y.text = node.getID();
            }
            y.parent = x;
            x.children.add(y);
            return y;
        }
    }

    private final ITreeContentProvider content_provider = new ITreeContentProvider() {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object input) {
            return status.toArray(new StatusItem[status.size()]);
        }

        public Object[] getChildren(Object parent) {
            StatusItem x = (StatusItem)parent;
            if (x.children == null) return null;
            Object[] arr = x.children.toArray(new StatusItem[x.children.size()]);
            Arrays.sort(arr);
            return arr;
        }

        public Object getParent(Object element) {
            StatusItem x = (StatusItem)element;
            return x.parent;
        }

        public boolean hasChildren(Object element) {
            StatusItem x = (StatusItem)element;
            return x.children != null && x.children.size() > 0;
        }
    };

    private final LabelProvider label_provider = new LabelProvider() {

        @Override
        public Image getImage(Object element) {
            StatusItem x = (StatusItem)element;
            if (x.object instanceof ILaunch) {
                ImageDescriptor desc = DebugUITools.getDefaultImageDescriptor(x.object);
                if (desc != null) return ImageCache.getImage(desc);
            }
            if (x.has_state) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
            if (x.object != null) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
            if (x.planted_ok) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
            return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
        }

        @Override
        public String getText(Object element) {
            StatusItem x = (StatusItem)element;
            if (x.object instanceof ILaunch) {
                ILaunchConfiguration cfg = ((ILaunch)x.object).getLaunchConfiguration();
                if (cfg != null) return cfg.getName();
            }
            return x.text;
        }
    };

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        status = getCurrentStatus();
        createStatusViewer(composite);
        setValid(true);
        return composite;
    }

    private void createStatusViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Breakpoint planting status:");
        label.setFont(parent.getFont());
        label.setLayoutData(new GridData());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        viewer = new TreeViewer(parent, SWT.BORDER);
        viewer.getTree().setLayoutData(data);
        viewer.getTree().setFont(parent.getFont());
        viewer.setContentProvider(content_provider);
        viewer.setLabelProvider(label_provider);
        viewer.setInput(this);
        viewer.expandAll();
    }

    private ICBreakpoint getBreakpoint() {
        return (ICBreakpoint)getElement().getAdapter(ICBreakpoint.class);
    }

    private List<StatusItem> getCurrentStatus() {
        final List<StatusCache> caches = new ArrayList<StatusCache>();
        final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch launch : launches) {
            if (!(launch instanceof TCFLaunch)) continue;
            TCFLaunch tcf_launch = (TCFLaunch)launch;
            if (tcf_launch.isConnecting()) continue;
            if (tcf_launch.isDisconnected()) continue;
            caches.add(new StatusCache(tcf_launch));
        }
        List<StatusItem> status = new TCFTask<List<StatusItem>>(10000) {
            public void run() {
                StatusCache pending = null;
                for (StatusCache cache : caches) {
                    if (!cache.validate()) pending = cache;
                }
                if (pending != null) {
                    pending.wait(this);
                    return;
                }
                List<StatusItem> roots = new ArrayList<StatusItem>();
                for (StatusCache cache : caches) {
                    StatusItem x = cache.getData();
                    if (x != null) roots.add(x);
                }
                for (StatusCache cache : caches) cache.dispose();
                if (roots.size() == 0) {
                    StatusItem x = new StatusItem();
                    x.text = "Not planted";
                    roots.add(x);
                }
                done(roots);
            }
        }.getE();
        return status;
    }

    @SuppressWarnings("unchecked")
    private Object[] toObjectArray(Object o) {
        if (o == null) return null;
        Collection<Object> c = (Collection<Object>)o;
        return (Object[])c.toArray(new Object[c.size()]);
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> toObjectMap(Object o) {
        return (Map<String,Object>)o;
    }
}
