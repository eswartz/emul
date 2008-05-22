/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.commands.DisconnectCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.ResumeCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepIntoCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepOverCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepReturnCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.SuspendCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.TerminateCommand;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class TCFModel implements IElementContentProvider, IElementLabelProvider,
        IModelProxyFactory, IColumnPresentationFactory, ISourceDisplay {

    private final Display display;
    private final TCFLaunch launch;
    private final Map<IPresentationContext,TCFModelProxy> model_proxies =
        new HashMap<IPresentationContext,TCFModelProxy>();
    private final Map<String,TCFNode> id2node = new HashMap<String,TCFNode>();
    @SuppressWarnings("unchecked")
    private final Map<Class,Object> commands = new HashMap<Class,Object>();
    private final TreeSet<FutureTask> queue = new TreeSet<FutureTask>();

    private TCFNodeLaunch launch_node;
    private boolean disposed;

    private int future_task_cnt;
    
    private int display_source_cnt;

    private static class FutureTask implements Comparable<FutureTask>{
        final int id;
        final long time;
        final Runnable run;

        FutureTask(int id, long time, Runnable run) {
            this.id = id;
            this.time = time;
            this.run = run;
        }

        public int compareTo(FutureTask x) {
            if (x == this) return 0;
            if (time < x.time) return -1;
            if (time > x.time) return +1;
            if (id < x.id) return -1;
            if (id > x.id) return +1;
            assert false;
            return 0;
        }
    }

    private final Thread future_task_dispatcher = new Thread() {
        public void run() {
            try {
                synchronized (queue) {
                    while (!disposed) {
                        if (queue.isEmpty()) {
                            queue.wait();
                        }
                        else {
                            long time = System.currentTimeMillis();
                            FutureTask t = queue.first();
                            if (t.time > time) {
                                queue.wait(t.time - time);
                            }
                            else {
                                queue.remove(t);
                                Protocol.invokeLater(t.run);
                            }
                        }
                    }
                }
            }
            catch (Throwable x) {
                x.printStackTrace();
            }
        }
    };

    private final IMemory.MemoryListener mem_listener = new IMemory.MemoryListener() {

        public void contextAdded(IMemory.MemoryContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                String id = contexts[i].getParentID();
                if (id == null) {
                    launch_node.onContextAdded(contexts[i]);
                }
                else {
                    TCFNode node = getNode(id);
                    if (node instanceof TCFNodeExecContext) {
                        ((TCFNodeExecContext)node).onContextAdded(contexts[i]);
                    }
                }
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

        public void contextRemoved(final String[] context_ids) {
            for (String id : context_ids) {
                TCFNode node = getNode(id);
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
                String id = contexts[i].getParentID();
                if (id == null) {
                    launch_node.onContextAdded(contexts[i]);
                }
                else {
                    TCFNode node = getNode(id);
                    if (node instanceof TCFNodeExecContext) {
                        ((TCFNodeExecContext)node).onContextAdded(contexts[i]);
                    }
                }
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

        public void contextRemoved(final String[] context_ids) {
            for (String id : context_ids) {
                TCFNode node = getNode(id);
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextRemoved();
                }
            }
            fireModelChanged();
            display.asyncExec(new Runnable() {
                public void run() {
                    for (String id : context_ids) {
                        Activator.getAnnotationManager().onContextRemoved(TCFModel.this, id);
                    }
                }
            });
        }

        public void contextResumed(final String context) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextResumed();
            }
            fireModelChanged();
            display.asyncExec(new Runnable() {
                public void run() {
                    Activator.getAnnotationManager().onContextResumed(TCFModel.this, context);
                }
            });
        }

        public void contextSuspended(final String context, String pc, String reason, Map<String,Object> params) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextSuspended(pc, reason, params);
            }
            fireModelChanged();
            display.asyncExec(new Runnable() {
                public void run() {
                    Activator.getAnnotationManager().onContextSuspended(TCFModel.this, context);
                }
            });
        }
    };

    private final IRegisters.RegistersListener reg_listener = new IRegisters.RegistersListener() {

        public void contextChanged() {
            for (TCFNode node : id2node.values()) {
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onRegistersChanged();
                }
            }
            fireModelChanged();
        }

        public void registerChanged(String context) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeRegister) {
                ((TCFNodeRegister)node).onValueChanged();
            }
            fireModelChanged();
        }
    };

    TCFModel(Display display, TCFLaunch launch) {
        this.display = display;
        this.launch = launch;
        commands.put(ISuspendHandler.class, new SuspendCommand(this));
        commands.put(IResumeHandler.class, new ResumeCommand(this));
        commands.put(ITerminateHandler.class, new TerminateCommand(this));
        commands.put(IDisconnectHandler.class, new DisconnectCommand(this));
        commands.put(IStepIntoHandler.class, new StepIntoCommand(this));
        commands.put(IStepOverHandler.class, new StepOverCommand(this));
        commands.put(IStepReturnHandler.class, new StepReturnCommand(this));
        future_task_dispatcher.setName("TCF Future Task Dispatcher");
        future_task_dispatcher.start();
    }

    @SuppressWarnings("unchecked")
    public Object getCommand(Class c) {
        Object o = commands.get(c);
        assert o == null || c.isInstance(o);
        return o;
    }

    void onConnected() {
        assert Protocol.isDispatchThread();
        assert launch_node == null;
        launch_node = new TCFNodeLaunch(this);
        IMemory mem = launch.getService(IMemory.class);
        if (mem != null) mem.addListener(mem_listener);
        IRunControl run = launch.getService(IRunControl.class);
        if (run != null) run.addListener(run_listener);
        IRegisters reg = launch.getService(IRegisters.class);
        if (reg != null) reg.addListener(reg_listener);
        launchChanged();
    }

    void onDisconnected() {
        assert Protocol.isDispatchThread();
        TCFNode[] a = id2node.values().toArray(new TCFNode[id2node.size()]);
        for (int i = 0; i < a.length; i++) {
            if (!a[i].isDisposed()) a[i].dispose();
        }
        launchChanged();
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

    Collection<TCFModelProxy> getModelProxyList() {
        return model_proxies.values();
    }

    void launchChanged() {
        if (launch_node != null) {
            launch_node.makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
            fireModelChanged();
        }
    }

    void dispose() {
        synchronized (queue) {
            disposed = true;
            queue.notify();
        }
        try {
            future_task_dispatcher.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    
    void fireModelChanged() {
        assert Protocol.isDispatchThread();
        for (TCFModelProxy p : model_proxies.values()) p.fireModelChanged();
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

    public void invokeLater(long delay, Runnable run) {
        synchronized (queue) {
            queue.add(new FutureTask(future_task_cnt++, System.currentTimeMillis() + delay, run));
            queue.notify();
        }
    }

    public void update(IChildrenCountUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) {
                if (launch_node != null) {
                    launch_node.update(updates[i]);
                }
                else {
                    updates[i].setChildCount(0);
                    updates[i].done();
                }
            }
            else {
                ((TCFNode)o).update(updates[i]);
            }
        }
    }

    public void update(IChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) {
                if (launch_node != null) {
                    launch_node.update(updates[i]);
                }
                else {
                    updates[i].done();
                }
            }
            else {
                ((TCFNode)o).update(updates[i]);
            }
        }
    }

    public void update(IHasChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            if (o instanceof TCFLaunch) {
                if (launch_node != null) { 
                    launch_node.update(updates[i]);
                }
                else {
                    updates[i].setHasChilren(false);
                    updates[i].done();
                }
            }
            else {
                ((TCFNode)o).update(updates[i]);
            }
        }
    }

    public void update(ILabelUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            Object o = updates[i].getElement();
            assert !(o instanceof TCFLaunch);
            ((TCFNode)o).update(updates[i]);
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
    
    /**
     * Reveal source code associated with given model element.
     */
    public void displaySource(Object element, final IWorkbenchPage page, boolean forceSourceLookup) {
        if (element instanceof TCFNodeStackFrame) {
            final TCFNodeStackFrame stack_frame = (TCFNodeStackFrame)element;
            final int cnt = ++display_source_cnt;
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    if (cnt != display_source_cnt) return;
                    IChannel channel = getLaunch().getChannel();
                    if (!disposed && channel.getState() == IChannel.STATE_OPEN && !stack_frame.disposed) {
                        TCFDataCache<TCFSourceRef> line_info = stack_frame.getLineInfo();
                        if (!line_info.validate()) {
                            line_info.wait(this);
                        }
                        else {
                            String editor_id = null;
                            IEditorInput editor_input = null;
                            Throwable error = line_info.getError();
                            TCFSourceRef src_ref = line_info.getData();
                            int line = 0;
                            if (error == null && src_ref != null) error = src_ref.error;
                            if (error != null) Activator.log("Error retrieving source mapping for a stack frame", error);
                            if (src_ref != null && src_ref.area != null) {
                                ISourceLocator locator = getLaunch().getSourceLocator();
                                Object source_element = null;
                                if (locator instanceof ISourceLookupDirector) {
                                    source_element = ((ISourceLookupDirector)locator).getSourceElement(src_ref.area);
                                }
                                if (source_element == null) {
                                    editor_input = new CommonSourceNotFoundEditorInput(src_ref.area);
                                    editor_id = IDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR;
                                }
                                else {
                                    ISourcePresentation presentation = TCFModelPresentation.getDefault();
                                    if (presentation != null) {
                                        editor_input = presentation.getEditorInput(source_element);
                                    }
                                    if (editor_input != null) {
                                        editor_id = presentation.getEditorId(editor_input, source_element);
                                    }                               
                                    line = src_ref.area.start_line;
                                }
                            }
                            displaySource(cnt, editor_id, editor_input, page, stack_frame.getRunContext().getID(),
                                    stack_frame.getFrameNo() == 0, line);
                        }
                    }
                }
            });
        }
    }
    
    private void displaySource(final int cnt,
            final String id, final IEditorInput input, final IWorkbenchPage page,
            final String exe_id, final boolean top_frame, final int line) {
        display.asyncExec(new Runnable() {
            public void run() {
                if (cnt != display_source_cnt) return;
                ITextEditor text_editor = null;
                IRegion region = null;
                if (input != null && id != null && page != null) {
                    IEditorPart editor = openEditor(input, id, page);
                    if (editor instanceof ITextEditor) {                                    
                        text_editor = (ITextEditor)editor;
                    }
                    else {
                        text_editor = (ITextEditor)editor.getAdapter(ITextEditor.class);
                    }
                }
                if (text_editor != null) {
                    region = getLineInformation(text_editor, line);
                    if (region != null) text_editor.selectAndReveal(region.getOffset(), 0);
                }
                Activator.getAnnotationManager().addStackFrameAnnotation(TCFModel.this,
                        exe_id, top_frame, page, text_editor, region);
            }
        });
    }
    
    /**
     * Open an editor for given editor input.
     * @param input - IEditorInput representing a source file to be shown in the editor 
     * @param id - editor type ID
     * @param page - workbench page that will contain the editor
     * @return - IEditorPart if the editor was opened successfully, or null otherwise.
     */
    private IEditorPart openEditor(final IEditorInput input, final String id, final IWorkbenchPage page) {
        final IEditorPart[] editor = new IEditorPart[]{ null };
        Runnable r = new Runnable() {
            public void run() {
                if (!page.getWorkbenchWindow().getWorkbench().isClosing()) {
                    try {
                        editor[0] = page.openEditor(input, id, false, IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
                    }
                    catch (PartInitException e) {
                        Activator.log("Cannot open editor", e);
                    }
                }
            }
        }; 
        BusyIndicator.showWhile(display, r);
        return editor[0];
    }   

    /**
     * Returns the line information for the given line in the given editor
     */
    private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
        IDocumentProvider provider = editor.getDocumentProvider();
        IEditorInput input = editor.getEditorInput();
        try {
            provider.connect(input);
        }
        catch (CoreException e) {
            return null;
        }
        try {
            IDocument document = provider.getDocument(input);
            if (document != null)
                return document.getLineInformation(lineNumber);
        }
        catch (BadLocationException e) {
        }
        finally {
            provider.disconnect(input);
        }
        return null;
    }       
}
