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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.launch.TCFSourceLookupDirector;
import org.eclipse.tm.internal.tcf.debug.launch.TCFSourceLookupParticipant;
import org.eclipse.tm.internal.tcf.debug.model.ITCFBreakpointListener;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsStatus;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class TCFAnnotationManager {

    private static final String
        TYPE_BP_INSTANCE = "org.eclipse.tm.tcf.debug.breakpoint_instance",
        TYPE_TOP_FRAME = "org.eclipse.tm.tcf.debug.top_frame",
        TYPE_STACK_FRAME = "org.eclipse.tm.tcf.debug.stack_frame";

    class TCFAnnotation extends Annotation {

        final ILineNumbers.CodeArea area;
        final Image image;
        final String text;
        final String type;
        final int hash_code;

        IAnnotationModel model;

        TCFAnnotation(ILineNumbers.CodeArea area, Image image, String text, String type) {
            this.area = area;
            this.image = image;
            this.text = text;
            this.type = type;
            hash_code = area.hashCode() + image.hashCode() + text.hashCode() + type.hashCode();
            setText(text);
            setType(type);
        }

        protected Image getImage() {
            return image;
        }

        void dispose() {
            assert Thread.currentThread() == display.getThread();
            if (model != null) {
                model.removeAnnotation(this);
                model = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TCFAnnotation)) return false;
            TCFAnnotation a = (TCFAnnotation)o;
            if (!area.equals(a.area)) return false;
            if (!image.equals(a.image)) return false;
            if (!text.equals(a.text)) return false;
            if (!type.equals(a.type)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return hash_code;
        }
    }

    private class WorkbenchWindowInfo {
        final LinkedList<TCFAnnotation> annotations = new LinkedList<TCFAnnotation>();

        void dispose() {
            for (TCFAnnotation a : annotations) a.dispose();
            annotations.clear();
        }
    }

    private TCFLaunch active_launch;
    private final HashMap<IWorkbenchWindow,WorkbenchWindowInfo> windows =
        new HashMap<IWorkbenchWindow,WorkbenchWindowInfo>();

    private final HashSet<IWorkbenchWindow> dirty_windows = new HashSet<IWorkbenchWindow>();
    private final HashSet<TCFLaunch> dirty_launches = new HashSet<TCFLaunch>();
    private final HashSet<TCFLaunch> changed_launch_cfgs = new HashSet<TCFLaunch>();

    private final TCFLaunch.LaunchListener launch_listener = new TCFLaunch.LaunchListener() {

        public void onCreated(TCFLaunch launch) {
        }

        public void onConnected(final TCFLaunch launch) {
            updateActiveLaunch();
            updateAnnotations(null, launch);
            TCFBreakpointsStatus bps = launch.getBreakpointsStatus();
            if (bps == null) return;
            bps.addListener(new ITCFBreakpointListener() {

                public void breakpointStatusChanged(String id) {
                    updateAnnotations(null, launch);
                }

                public void breakpointRemoved(String id) {
                    updateAnnotations(null, launch);
                }

                public void breakpointChanged(String id) {
                }
            });
        }

        public void onDisconnected(final TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            updateActiveLaunch();
            updateAnnotations(null, launch);
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
            updateAnnotations(part.getSite().getWorkbenchWindow(), (TCFLaunch)null);
            if (selection instanceof IStructuredSelection) {
                final Object obj = ((IStructuredSelection)selection).getFirstElement();
                if (obj instanceof TCFNodeStackFrame && ((TCFNodeStackFrame)obj).isTraceLimit()) {
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            ((TCFNodeStackFrame)obj).riseTraceLimit();
                        }
                    });
                }
            }
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
            updateAnnotations(window, (TCFLaunch)null);
        }
    };

    private final ILaunchConfigurationListener launch_conf_listener = new ILaunchConfigurationListener() {

        public void launchConfigurationAdded(ILaunchConfiguration cfg) {
        }

        public void launchConfigurationChanged(final ILaunchConfiguration cfg) {
            displayExec(new Runnable() {
                public void run() {
                    ILaunch[] arr = launch_manager.getLaunches();
                    for (ILaunch l : arr) {
                        if (l instanceof TCFLaunch) {
                            TCFLaunch t = (TCFLaunch)l;
                            if (cfg.equals(t.getLaunchConfiguration())) {
                                changed_launch_cfgs.add(t);
                                updateAnnotations(null, t);
                            }
                        }
                    }
                }
            });
        }

        public void launchConfigurationRemoved(ILaunchConfiguration cfg) {
        }
    };

    private final Display display = Display.getDefault();
    private final ILaunchManager launch_manager = DebugPlugin.getDefault().getLaunchManager();
    private int update_active_launch_cnt = 0;
    private int update_unnotations_cnt = 0;
    private boolean started;
    private boolean disposed;

    public TCFAnnotationManager() {
        assert Protocol.isDispatchThread();
        TCFLaunch.addListener(launch_listener);
        launch_manager.addLaunchConfigurationListener(launch_conf_listener);
        displayExec(new Runnable() {
            public void run() {
                if (!PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().isStarting()) {
                    display.timerExec(200, this);
                }
                else if (!PlatformUI.getWorkbench().isClosing()) {
                    started = true;
                    PlatformUI.getWorkbench().addWindowListener(window_listener);
                    for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                        window_listener.windowOpened(window);
                    }
                    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if (w != null) window_listener.windowActivated(w);
                }
            }
        });
    }

    public void dispose() {
        if (disposed) return;
        assert Protocol.isDispatchThread();
        disposed = true;
        launch_manager.removeLaunchConfigurationListener(launch_conf_listener);
        TCFLaunch.removeListener(launch_listener);
        displayExec(new Runnable() {
            public void run() {
                if (!started) return;
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
                        TCFLaunch l = (TCFLaunch)x;
                        IChannel channel = l.getChannel();
                        if (channel != null && channel.getState() == IChannel.STATE_OPEN) launch = l;
                    }
                }
                active_launch = launch;
            }
        });
    }

    String getBreakpointStatus(final TCFBreakpoint breakpoint) {
        assert Protocol.isDispatchThread();
        if (disposed) return null;
        final TCFLaunch launch = active_launch;
        if (launch != null) {
            TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
            if (bs != null) {
                Map<String,Object> map = bs.getStatus(breakpoint);
                if (map != null) {
                    String status = null;
                    String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                    Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                    if (error != null) status = error;
                    else if (planted != null) status = "Planted";
                    return status;
                }
            }
        }
        return null;
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

    private void addBreakpointErrorAnnotation(List<TCFAnnotation> set, TCFLaunch launch, String id, String error) {
        Map<String,Object> props = launch.getBreakpointsStatus().getProperties(id);
        if (props != null) {
            String file = (String)props.get(IBreakpoints.PROP_FILE);
            Number line = (Number)props.get(IBreakpoints.PROP_LINE);
            if (file != null && line != null) {
                ILineNumbers.CodeArea area = new ILineNumbers.CodeArea(null, file,
                        line.intValue(), 0, line.intValue() + 1, 0,
                        null, null, 0, false, false, false, false);
                TCFAnnotation a = new TCFAnnotation(area,
                        ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                        "Cannot plant breakpoint: " + error,
                        TYPE_BP_INSTANCE);
                set.add(a);
            }
        }
    }

    private void updateAnnotations(IWorkbenchWindow window, final TCFNode node) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        final WorkbenchWindowInfo win_info = windows.get(window);
        if (win_info == null) return;
        List<TCFAnnotation> set = null;
        if (node != null) {
            set = new TCFTask<List<TCFAnnotation>>(node.getChannel()) {
                public void run() {
                    if (node.isDisposed()) {
                        done(null);
                        return;
                    }
                    TCFNodeExecContext thread = null;
                    TCFNodeExecContext memory = null;
                    TCFNodeStackFrame frame = null;
                    TCFNodeStackFrame last_top_frame = null;
                    String bp_group = null;
                    boolean suspended = false;
                    if (node instanceof TCFNodeStackFrame) {
                        thread = (TCFNodeExecContext)node.parent;
                        frame = (TCFNodeStackFrame)node;
                    }
                    else if (node instanceof TCFNodeExecContext) {
                        thread = (TCFNodeExecContext)node;
                        TCFChildrenStackTrace trace = thread.getStackTrace();
                        if (!trace.validate(this)) return;
                        frame = trace.getTopFrame();
                    }
                    if (thread != null) {
                        TCFDataCache<IRunControl.RunControlContext> rc_ctx_cache = thread.getRunContext();
                        if (!rc_ctx_cache.validate(this)) return;
                        IRunControl.RunControlContext rc_ctx_data = rc_ctx_cache.getData();
                        if (rc_ctx_data != null) bp_group = rc_ctx_data.getBPGroup();
                        TCFDataCache<TCFNodeExecContext> mem_cache = thread.getMemoryNode();
                        if (!mem_cache.validate(this)) return;
                        memory = mem_cache.getData();
                        if (bp_group == null && memory != null && rc_ctx_data != null && rc_ctx_data.hasState()) bp_group = memory.id;
                        last_top_frame = thread.getLastTopFrame();
                        TCFDataCache<TCFContextState> state_cache = thread.getState();
                        if (!state_cache.validate(this)) return;
                        suspended = state_cache.getData() != null && state_cache.getData().is_suspended;
                    }
                    List<TCFAnnotation> set = new ArrayList<TCFAnnotation>();
                    if (memory != null) {
                        TCFLaunch launch = node.launch;
                        TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                        if (bs != null) {
                            for (String id : bs.getStatusIDs()) {
                                Map<String,Object> map = bs.getStatus(id);
                                if (map == null) continue;
                                String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                                if (error != null) addBreakpointErrorAnnotation(set, launch, id, error);
                                Object[] arr = toObjectArray(map.get(IBreakpoints.STATUS_INSTANCES));
                                if (arr == null) continue;
                                for (Object o : arr) {
                                    Map<String,Object> m = toObjectMap(o);
                                    String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                                    if (ctx_id == null) continue;
                                    if (!ctx_id.equals(node.id) && !ctx_id.equals(bp_group)) continue;
                                    error = (String)m.get(IBreakpoints.INSTANCE_ERROR);
                                    BigInteger addr = JSON.toBigInteger((Number)m.get(IBreakpoints.INSTANCE_ADDRESS));
                                    if (addr != null) {
                                        ILineNumbers.CodeArea area = null;
                                        TCFDataCache<TCFSourceRef> line_cache = memory.getLineInfo(addr);
                                        if (line_cache != null) {
                                            if (!line_cache.validate(this)) return;
                                            TCFSourceRef line_data = line_cache.getData();
                                            if (line_data != null && line_data.area != null) area = line_data.area;
                                        }
                                        if (area == null) {
                                            Map<String,Object> props = launch.getBreakpointsStatus().getProperties(id);
                                            if (props != null) {
                                                String file = (String)props.get(IBreakpoints.PROP_FILE);
                                                Number line = (Number)props.get(IBreakpoints.PROP_LINE);
                                                if (file != null && line != null) {
                                                    area = new ILineNumbers.CodeArea(null, file,
                                                            line.intValue(), 0, line.intValue() + 1, 0,
                                                            null, null, 0, false, false, false, false);
                                                }
                                            }
                                        }
                                        if (area != null) {
                                            if (error != null) {
                                                TCFAnnotation a = new TCFAnnotation(area,
                                                        ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                                                        "Cannot plant breakpoint at 0x" + addr.toString(16) + ": " + error,
                                                        TYPE_BP_INSTANCE);
                                                set.add(a);
                                                error = null;
                                            }
                                            else {
                                                TCFAnnotation a = new TCFAnnotation(area,
                                                        ImageCache.getImage(ImageCache.IMG_BREAKPOINT_INSTALLED),
                                                        "Breakpoint planted at 0x" + addr.toString(16),
                                                        TYPE_BP_INSTANCE);
                                                set.add(a);
                                            }
                                        }
                                    }
                                    if (error != null) addBreakpointErrorAnnotation(set, launch, id, error);
                                }
                            }
                        }
                    }
                    if (suspended && frame != null && frame.getFrameNo() >= 0) {
                        TCFDataCache<TCFSourceRef> line_cache = frame.getLineInfo();
                        if (!line_cache.validate(this)) return;
                        TCFSourceRef line_data = line_cache.getData();
                        if (line_data != null && line_data.area != null) {
                            TCFAnnotation a = null;
                            if (frame.getFrameNo() == 0) {
                                a = new TCFAnnotation(line_data.area,
                                        DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP),
                                        "Current Instruction Pointer",
                                        TYPE_TOP_FRAME);
                            }
                            else {
                                a = new TCFAnnotation(line_data.area,
                                        DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                        "Stack Frame",
                                        TYPE_STACK_FRAME);
                            }
                            set.add(a);
                        }
                    }
                    if (!suspended && last_top_frame != null) {
                        TCFDataCache<TCFSourceRef> line_cache = last_top_frame.getLineInfo();
                        if (!line_cache.validate(this)) return;
                        TCFSourceRef line_data = line_cache.getData();
                        if (line_data != null && line_data.area != null) {
                            TCFAnnotation a = new TCFAnnotation(line_data.area,
                                    DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                    "Last Instruction Pointer position",
                                    TYPE_STACK_FRAME);
                            set.add(a);
                        }
                    }
                    done(set);
                }
            }.getE();
        }
        boolean flush_all = node == null || changed_launch_cfgs.contains(node.launch);
        Iterator<TCFAnnotation> i = win_info.annotations.iterator();
        while (i.hasNext()) {
            TCFAnnotation a = i.next();
            if (!flush_all && set != null && set.remove(a)) continue;
            a.dispose();
            i.remove();
        }
        if (set == null || set.size() == 0) return;
        Map<IEditorInput,ITextEditor> editors = new HashMap<IEditorInput,ITextEditor>();
        for (IEditorReference ref : window.getActivePage().getEditorReferences()) {
            IEditorPart part = ref.getEditor(false);
            if (!(part instanceof ITextEditor)) continue;
            ITextEditor editor = (ITextEditor)part;
            editors.put(editor.getEditorInput(), editor);
        }
        ISourceLocator locator = node.launch.getSourceLocator();
        ISourcePresentation presentation = TCFModelPresentation.getDefault();
        for (TCFAnnotation a : set) {
            Object source_element = null;
            if (locator instanceof TCFSourceLookupDirector) {
                source_element = ((TCFSourceLookupDirector)locator).getSourceElement(a.area);
            }
            else if (locator instanceof ISourceLookupDirector) {
                // support for foreign (CDT) source locator
                String filename = TCFSourceLookupParticipant.toFileName(a.area);
                if (filename != null) {
                    source_element = ((ISourceLookupDirector)locator).getSourceElement(filename);
                    if (source_element == null && !filename.equals(a.area.file)) {
                        // retry with relative path
                        source_element = ((ISourceLookupDirector)locator).getSourceElement(a.area.file);
                    }
                }
            }
            if (source_element == null) continue;
            IEditorInput editor_input = presentation.getEditorInput(source_element);
            ITextEditor editor = editors.get(editor_input);
            if (editor == null) continue;
            IDocumentProvider doc_provider = editor.getDocumentProvider();
            IAnnotationModel ann_model = doc_provider.getAnnotationModel(editor_input);
            if (ann_model == null) continue;
            IRegion region = null;
            try {
                doc_provider.connect(editor_input);
            }
            catch (CoreException e) {
            }
            try {
                IDocument document = doc_provider.getDocument(editor_input);
                if (document != null) region = document.getLineInformation(a.area.start_line - 1);
            }
            catch (BadLocationException e) {
            }
            finally {
                doc_provider.disconnect(editor_input);
            }
            if (region == null) continue;
            ann_model.addAnnotation(a, new Position(region.getOffset(), region.getLength()));
            a.model = ann_model;
            win_info.annotations.add(a);
        }
    }

    private void updateAnnotations(final int cnt) {
        displayExec(new Runnable() {
            public void run() {
                synchronized (TCFAnnotationManager.this) {
                    if (cnt != update_unnotations_cnt) return;
                }
                for (IWorkbenchWindow window : windows.keySet()) {
                    if (dirty_windows.contains(null) || dirty_windows.contains(window)) {
                        TCFNode node = null;
                        try {
                            ISelection active_context = DebugUITools.getDebugContextManager()
                                    .getContextService(window).getActiveContext();
                            if (active_context instanceof IStructuredSelection) {
                                IStructuredSelection selection = (IStructuredSelection)active_context;
                                if (!selection.isEmpty()) {
                                    Object first_element = selection.getFirstElement();
                                    if (first_element instanceof IAdaptable) {
                                        node = (TCFNode)((IAdaptable)first_element).getAdapter(TCFNode.class);
                                    }
                                }
                            }
                            if (dirty_launches.contains(null) || node != null && dirty_launches.contains(node.launch)) {
                                updateAnnotations(window, node);
                            }
                        }
                        catch (Throwable x) {
                            if (node == null || !node.isDisposed()) {
                                Activator.log("Cannot update editor annotations", x);
                            }
                        }
                    }
                }
                for (TCFLaunch launch : dirty_launches) {
                    if (launch != null) launch.removePendingClient(TCFAnnotationManager.this);
                }
                changed_launch_cfgs.clear();
                dirty_windows.clear();
                dirty_launches.clear();
            }
        });
    }

    synchronized void updateAnnotations(final IWorkbenchWindow window, final TCFLaunch launch) {
        final int cnt = ++update_unnotations_cnt;
        displayExec(new Runnable() {
            public void run() {
                dirty_windows.add(window);
                dirty_launches.add(launch);
                updateAnnotations(cnt);
            }
        });
    }
}
