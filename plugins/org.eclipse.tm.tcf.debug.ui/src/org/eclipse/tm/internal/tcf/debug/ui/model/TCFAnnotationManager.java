/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.model.ITCFBreakpointListener;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsStatus;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class TCFAnnotationManager {

    class TCFAnnotation extends Annotation {

        final TCFModel model;
        final String exe_id;
        final ITextEditor editor;
        final Image image;
        final Position position;

        TCFAnnotation(TCFModel model, String exe_id, ITextEditor editor, Image image, Position position) {
            this.model = model;
            this.exe_id = exe_id;
            this.editor = editor;
            this.image = image;
            this.position = position;
        }

        protected Image getImage() {
            return image;
        }

        void dispose() {
            assert Thread.currentThread() == display.getThread();
            IDocumentProvider doc_provider = editor.getDocumentProvider();
            IEditorInput editor_input = editor.getEditorInput();
            if (doc_provider != null) {
                IAnnotationModel ann_model = doc_provider.getAnnotationModel(editor_input);
                if (ann_model != null) ann_model.removeAnnotation(this);
            }
        }
    }

    private class WorkbenchWindowInfo {
        final ArrayList<TCFAnnotation> annotations = new ArrayList<TCFAnnotation>();

        void dispose() {
            for (TCFAnnotation a : annotations) a.dispose();
            annotations.clear();
        }
    }

    private TCFLaunch active_launch;
    private final HashMap<IWorkbenchWindow,WorkbenchWindowInfo> windows =
        new HashMap<IWorkbenchWindow,WorkbenchWindowInfo>();

    private final TCFLaunch.Listener launch_listener = new TCFLaunch.Listener() {

        public void onCreated(TCFLaunch launch) {
        }

        public void onConnected(final TCFLaunch launch) {
            updateActiveLaunch();
            TCFBreakpointsStatus bps = launch.getBreakpointsStatus();
            if (bps == null) return;
            bps.addListener(new ITCFBreakpointListener() {

                public void breakpointStatusChanged(String id) {
                    displayExec(new Runnable() {
                        public void run() {
                            if (active_launch != launch) return;
                            refreshBreakpointView();
                        }
                    });
                }

                public void breakpointRemoved(String id) {
                    displayExec(new Runnable() {
                        public void run() {
                            if (active_launch != launch) return;
                            refreshBreakpointView();
                        }
                    });
                }
            });
        }

        public void onDisconnected(final TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            synchronized (Device.class) {
                displayExec(new Runnable() {
                    public void run() {
                        for (WorkbenchWindowInfo info : windows.values()) {
                            for (Iterator<TCFAnnotation> i = info.annotations.iterator(); i.hasNext();) {
                                TCFAnnotation a = i.next();
                                if (a.model.getLaunch() == launch) {
                                    i.remove();
                                    a.dispose();
                                }
                            }
                        }
                    }
                });
            }
            updateActiveLaunch();
        }

        public void onContextActionsDone(TCFLaunch launch, String id, String result) {
        }

        public void onContextActionsStart(TCFLaunch launch, String id) {
        }

        public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data) {
        }

        public void onProcessStreamError(TCFLaunch launch, String process_id,
                int stream_id, Exception error, int lost_size) {
        }
    };

    private final ISelectionListener selection_listener = new ISelectionListener() {

        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateActiveLaunch();
        }
    };

    private final IWindowListener window_listener = new IWindowListener() {

        public void windowActivated(IWorkbenchWindow window) {
            updateActiveLaunch();
        }

        public void windowClosed(IWorkbenchWindow window) {
            assert windows.get(window) != null;
            window.getSelectionService().removeSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            windows.remove(window).dispose();
            updateActiveLaunch();
        }

        public void windowDeactivated(IWorkbenchWindow window) {
            updateActiveLaunch();
        }

        public void windowOpened(IWorkbenchWindow window) {
            if (windows.get(window) != null) return;
            window.getSelectionService().addSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            windows.put(window, new WorkbenchWindowInfo());
            updateActiveLaunch();
        }
    };

    private final Display display = PlatformUI.getWorkbench().getDisplay();
    private int refresh_breakpoint_view_cnt = 0;
    private int update_active_launch_cnt = 0;
    private boolean disposed;

    public TCFAnnotationManager() {
        assert Protocol.isDispatchThread();
        TCFLaunch.addListener(launch_listener);
        displayExec(new Runnable() {
            public void run() {
                if (PlatformUI.isWorkbenchRunning()) {
                    for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                        window_listener.windowOpened(window);
                    }
                }
                PlatformUI.getWorkbench().addWindowListener(window_listener);
                IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (w != null) window_listener.windowActivated(w);
            }
        });
    }

    public void dispose() {
        if (disposed) return;
        assert Protocol.isDispatchThread();
        disposed = true;
        TCFLaunch.removeListener(launch_listener);
        displayExec(new Runnable() {
            public void run() {
                PlatformUI.getWorkbench().removeWindowListener(window_listener);
                for (IWorkbenchWindow window : windows.keySet()) {
                    window.getSelectionService().removeSelectionListener(
                            IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
                    windows.get(window).dispose();
                }
                windows.clear();
            }
        });
    }

    private void displayExec(Runnable r) {
        synchronized (Device.class) {
            if (!display.isDisposed()) {
                display.asyncExec(r);
            }
        }
    }

    private void updateActiveLaunch() {
        assert !disposed;
        final int cnt = ++update_active_launch_cnt;
        displayExec(new Runnable() {
            public void run() {
                if (cnt != update_active_launch_cnt) return;
                TCFLaunch launch = null;
                IAdaptable adaptable = DebugUITools.getDebugContext();
                if (adaptable != null) {
                    ILaunch x = (ILaunch)adaptable.getAdapter(ILaunch.class);
                    if (x instanceof TCFLaunch) {
                        final TCFLaunch l = (TCFLaunch)x;
                        final boolean[] b = new boolean[1];
                        Protocol.invokeAndWait(new Runnable() {
                            public void run() {
                                IChannel channel = l.getChannel();
                                b[0] = channel != null && channel.getState() == IChannel.STATE_OPEN;
                            }
                        });
                        if (b[0]) launch = l;
                    }
                }
                if (active_launch != launch) {
                    active_launch = launch;
                    refreshBreakpointView();
                }
            }
        });
    }

    private void refreshBreakpointView() {
        assert !disposed;
        final int cnt = ++refresh_breakpoint_view_cnt;
        displayExec(new Runnable() {
            public void run() {
                if (cnt != refresh_breakpoint_view_cnt) return;
                for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                    IDebugView view = (IDebugView)window.getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
                    if (view != null) view.getViewer().refresh();
                }
            }
        });
    }

    String getBreakpointStatus(final TCFBreakpoint breakpoint) {
        if (disposed) return "";
        assert Thread.currentThread() == display.getThread();
        final TCFLaunch launch = active_launch;
        final String[] text = new String[1];
        text[0] = breakpoint.getText();
        if (launch != null && launch.getBreakpointsStatus() != null) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                    if (bs != null) {
                        Map<String,Object> map = bs.getStatus(breakpoint);
                        if (map != null) {
                            String status = null;
                            String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                            Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                            if (error != null) status = error;
                            else if (planted != null) status = "Planted";
                            if (status != null) text[0] += " (" + status + ")";
                        }
                    }
                }
            });
        }
        return text[0];
    }

    void addStackFrameAnnotation(TCFModel model, String exe_id, boolean top_frame,
            IWorkbenchPage page, ITextEditor editor, IRegion region) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        TCFAnnotation annotation = null;
        IAnnotationModel ann_model = null;

        if (editor != null && region != null) {
            IDocumentProvider doc_provider = editor.getDocumentProvider();
            IEditorInput editor_input = editor.getEditorInput();
            ann_model = doc_provider.getAnnotationModel(editor_input);
            if (ann_model != null) {
                String type;
                String text;
                Image image;
                if (top_frame) {
                    type = "org.eclipse.tm.tcf.debug.top_frame"; //$NON-NLS-1$
                    text = "Debug Current Instruction Pointer"; //$NON-NLS-1$
                    image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP);
                }
                else {
                    type = "org.eclipse.tm.tcf.debug.stack_frame"; //$NON-NLS-1$
                    text = "Debug Stack Frame"; //$NON-NLS-1$
                    image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER);
                }
                annotation = new TCFAnnotation(model, exe_id, editor, image,
                        new Position(region.getOffset(), region.getLength()));
                annotation.setType(type);
                annotation.setText(text);
            }
        }

        if (page != null) {
            WorkbenchWindowInfo info = windows.get(page.getWorkbenchWindow());
            if (annotation != null && info.annotations.size() == 1) {
                for (TCFAnnotation a : info.annotations) {
                    if (a.model != annotation.model) continue;
                    if (a.editor != annotation.editor) continue;
                    if (a.image != annotation.image) continue;
                    if (!a.exe_id.equals(annotation.exe_id)) continue;
                    if (!a.position.equals(annotation.position)) continue;
                    if (!a.getType().equals(annotation.getType())) continue;
                    if (!a.getText().equals(annotation.getText())) continue;
                    return;
                }
            }
            for (TCFAnnotation a : info.annotations) a.dispose();
            info.annotations.clear();
            if (annotation != null) {
                ann_model.addAnnotation(annotation, annotation.position);
                info.annotations.add(annotation);
            }
        }
    }

    public Annotation findAnnotation(TCFModel model, String id) {
        if (disposed) return null;
        assert Thread.currentThread() == display.getThread();
        for (WorkbenchWindowInfo info : windows.values()) {
            for (Iterator<TCFAnnotation> i = info.annotations.iterator(); i.hasNext();) {
                TCFAnnotation a = i.next();
                if (a.model == model && a.exe_id.equals(id)) {
                    return a;
                }
            }
        }
        return null;
    }

    void onContextResumed(TCFModel model, String id) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        for (WorkbenchWindowInfo info : windows.values()) {
            for (Iterator<TCFAnnotation> i = info.annotations.iterator(); i.hasNext();) {
                TCFAnnotation a = i.next();
                if (a.model == model && a.exe_id.equals(id)) {
                    i.remove();
                    a.dispose();
                }
            }
        }
    }

    void onContextRemoved(TCFModel model, String id) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        for (WorkbenchWindowInfo info : windows.values()) {
            for (Iterator<TCFAnnotation> i = info.annotations.iterator(); i.hasNext();) {
                TCFAnnotation a = i.next();
                if (a.model == model && a.exe_id.equals(id)) {
                    i.remove();
                    a.dispose();
                }
            }
        }
    }
}
