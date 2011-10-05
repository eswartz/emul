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

package org.eclipse.tm.internal.tcf.cdt.ui.commands;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPart;

@SuppressWarnings("restriction")
public class TCFPinViewCommand implements IPinProvider {

    private final TCFModel model;
    private final ArrayList<PinnedView> list = new ArrayList<PinnedView>();

    private class PinnedView implements IPinElementHandle {

        @SuppressWarnings("unused")
        private final IPinModelListener listener;
        @SuppressWarnings("unused")
        private final IWorkbenchPart part;
        private final TCFNode node;
        private final IPresentationContext ctx;

        final IRunControl.RunControlListener rc_listener = new IRunControl.RunControlListener() {

            public void contextAdded(IRunControl.RunControlContext[] contexts) {
            }

            public void contextChanged(IRunControl.RunControlContext[] contexts) {
                for (IRunControl.RunControlContext ctx : contexts) {
                    if (node.getID().equals(ctx.getID())) updateLabel();
                }
            }

            public void contextRemoved(String[] context_ids) {
                for (String id : context_ids) {
                    if (node.getID().equals(id)) updateLabel();
                }
            }

            public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
                if (node.getID().equals(id)) updateLabel();
            }

            public void contextResumed(String id) {
                if (node.getID().equals(id)) updateLabel();
            }

            public void containerSuspended(String context, String pc, String reason, Map<String, Object> params, String[] suspended_ids) {
                for (String id : suspended_ids) {
                    if (node.getID().equals(id)) updateLabel();
                }
            }

            public void containerResumed(String[] context_ids) {
                for (String id : context_ids) {
                    if (node.getID().equals(id)) updateLabel();
                }
            }

            public void contextException(String id, String msg) {
                if (node.getID().equals(id)) updateLabel();
            }
        };

        PinnedView(IWorkbenchPart part, TCFNode node, IPinModelListener listener) {
            this.part = part;
            this.node = node;
            this.listener = listener;
            ctx = new PresentationContext(TCFModel.ID_PINNED_VIEW, part);
            IRunControl rc = model.getChannel().getRemoteService(IRunControl.class);
            if (rc != null) rc.addListener(rc_listener);
        }

        void updateLabel() {
            // TODO: CDT does not support label update
            /*
            model.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    listener.modelChanged(new StructuredSelection(node));
                }
            });
            */
        }

        void dispose() {
            IRunControl rc = model.getChannel().getRemoteService(IRunControl.class);
            if (rc != null) rc.removeListener(rc_listener);
        }

        public Object getDebugContext() {
            return node;
        }

        public String getLabel() {
            return new TCFTask<String>() {
                public void run() {
                    model.update(new ILabelUpdate[]{ new ILabelUpdate() {

                        String text;

                        public IPresentationContext getPresentationContext() {
                            return ctx;
                        }

                        public Object getElement() {
                            return node;
                        }

                        public TreePath getElementPath() {
                            return null;
                        }

                        public Object getViewerInput() {
                            return null;
                        }

                        public void setStatus(IStatus status) {
                        }

                        public IStatus getStatus() {
                            return null;
                        }

                        public void done() {
                            done_update(text);
                        }

                        public void cancel() {
                        }

                        public boolean isCanceled() {
                            return false;
                        }

                        public String[] getColumnIds() {
                            return null;
                        }

                        public void setLabel(String text, int columnIndex) {
                            if (columnIndex == 0) this.text = text;
                        }

                        public void setFontData(FontData fontData, int columnIndex) {
                        }

                        public void setImageDescriptor(ImageDescriptor image, int columnIndex) {
                        }

                        public void setForeground(RGB foreground, int columnIndex) {
                        }

                        public void setBackground(RGB background, int columnIndex) {
                        }
                    }});
                }

                private void done_update(String text) {
                    if (text == null) text = node.getID();
                    done(text);
                }
            }.getE();
        }

        public IPinElementColorDescriptor getPinElementColorDescriptor() {
            return null;
        }
    }

    public TCFPinViewCommand(TCFModel model) {
        this.model = model;
    }

    public boolean isPinnable(IWorkbenchPart part, final Object obj) {
        if (obj instanceof TCFNode) {
            try {
                final String id = part.getSite().getId();
                return new TCFTask<Boolean>(model.getChannel()) {
                    public void run() {
                        boolean mem = false;
                        boolean vars = false;
                        if (obj instanceof TCFNodeExecContext) {
                            TCFNodeExecContext node = (TCFNodeExecContext)obj;
                            TCFDataCache<IRunControl.RunControlContext> ctx_cache = node.getRunContext();
                            if (!ctx_cache.validate(this)) return;
                            IRunControl.RunControlContext ctx_data = ctx_cache.getData();
                            if (ctx_data != null) {
                                vars = ctx_data.hasState();
                                mem = vars || ctx_data.getProcessID() != null;
                            }
                        }
                        if (obj instanceof TCFNodeStackFrame) {
                            vars = true;
                            mem = true;
                        }
                        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) done(vars);
                        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id)) done(vars);
                        else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) done(mem);
                        else done(false);
                    }
                }.getE();
            }
            catch (Throwable x) {
                return false;
            }
        }
        return false;
    }

    public IPinElementHandle pin(final IWorkbenchPart part, Object obj, final IPinModelListener listener) {
        if (obj instanceof TCFNode) {
            final TCFNode node = (TCFNode)obj;
            return new TCFTask<IPinElementHandle>() {
                public void run() {
                    PinnedView p = new PinnedView(part, node, listener);
                    model.setPin(part, node);
                    list.add(p);
                    done(p);
                }
            }.getE();
        }
        return null;
    }

    public void unpin(final IWorkbenchPart part, final IPinElementHandle handle) {
        new TCFTask<Object>() {
            public void run() {
                model.setPin(part, null);
                if (list.remove(handle)) {
                    ((PinnedView)handle).dispose();
                }
                done(null);
            }
        };
    }

    public boolean isPinnedTo(Object obj, final IPinElementHandle handle) {
        if (obj instanceof TCFNode) {
            return new TCFTask<Boolean>() {
                public void run() {
                    done(list.contains(handle));
                }
            }.getE();
        }
        return false;
    }
}
