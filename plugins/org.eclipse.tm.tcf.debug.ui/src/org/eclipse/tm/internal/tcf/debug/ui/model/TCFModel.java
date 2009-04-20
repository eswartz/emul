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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.contexts.ISuspendTriggerListener;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Device;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.commands.DisconnectCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.ResumeCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepIntoCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepOverCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.StepReturnCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.SuspendCommand;
import org.eclipse.tm.internal.tcf.debug.ui.commands.TerminateCommand;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * TCFModel represents remote target state as it is known to host.
 * The main job of the model is caching remote data,
 * keeping the cache in a coherent state, and feeding UI with up-to-date data.
 */
public class TCFModel implements IElementContentProvider, IElementLabelProvider,
        IModelProxyFactory, IColumnPresentationFactory, ISourceDisplay, ISuspendTrigger {

    private final TCFLaunch launch;
    private final Display display;
    
    private final List<ISuspendTriggerListener> suspend_trigger_listeners =
        new LinkedList<ISuspendTriggerListener>();
    
    private int suspend_trigger_generation;
    
    private final Map<IPresentationContext,TCFModelProxy> model_proxies =
        new HashMap<IPresentationContext,TCFModelProxy>();
    
    private final Map<String,TCFNode> id2node = new HashMap<String,TCFNode>();
    
    @SuppressWarnings("unchecked")
    private final Map<Class,Object> commands = new HashMap<Class,Object>();
    
    private final Map<String,IMemoryBlockRetrievalExtension> mem_retrieval =
        new HashMap<String,IMemoryBlockRetrievalExtension>();

    private final Map<String,Map<Integer,MessageConsoleStream>> consoles =
        new HashMap<String,Map<Integer,MessageConsoleStream>>();

    private static final Map<ILaunchConfiguration,IEditorInput> editor_not_found = 
        new HashMap<ILaunchConfiguration,IEditorInput>();
    
    private final Map<String,Map<String,TCFDataCache<ISymbols.Symbol>>> symbols =
        new HashMap<String,Map<String,TCFDataCache<ISymbols.Symbol>>>();
    
    private final Map<String,Map<String,TCFDataCache<String[]>>> symbol_children =
        new HashMap<String,Map<String,TCFDataCache<String[]>>>();
    
    private final IModelSelectionPolicyFactory model_selection_factory = new IModelSelectionPolicyFactory() {

        public IModelSelectionPolicy createModelSelectionPolicyAdapter(
                Object element, IPresentationContext context) {
            return selection_policy;
        }
    };

    private final IModelSelectionPolicy selection_policy;

    private TCFNodeLaunch launch_node;
    private boolean disposed;
    private boolean debug_view_selection_set;

    private static int debug_view_selection_cnt;
    private static int display_source_cnt;

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
                Map<String,TCFDataCache<ISymbols.Symbol>> m = symbols.remove(contexts[i].getID());
                if (m != null) {
                    for (TCFDataCache<ISymbols.Symbol> s : m.values()) s.cancel();
                }
            }
            fireModelChanged();
        }

        public void contextRemoved(final String[] context_ids) {
            onContextRemoved(context_ids);
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
            runSuspendTrigger();
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
            onContextRemoved(context_ids);
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
                final TCFNodeExecContext exe = (TCFNodeExecContext)node;
                exe.onContextSuspended(pc, reason, params);
                setDebugViewSelection(context, false);
            }
            fireModelChanged();
            runSuspendTrigger();
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
    
    private final IProcesses.ProcessesListener prs_listener = new IProcesses.ProcessesListener() {

        public void exited(String process_id, int exit_code) {
            Map<Integer,MessageConsoleStream> streams = consoles.get(process_id);
            if (streams!= null) {
                for (MessageConsoleStream stream : streams.values()) {
                    try {
                        stream.close();
                    }
                    catch (IOException x) {
                        Activator.log("Cannot close console stream", x);
                    }
                }
                onLastContextRemoved();
            }
        }
    };
    
    TCFModel(TCFLaunch launch) {
        this.launch = launch;
        display = PlatformUI.getWorkbench().getDisplay();
        selection_policy = new TCFModelSelectionPolicy(this);
        commands.put(ISuspendHandler.class, new SuspendCommand(this));
        commands.put(IResumeHandler.class, new ResumeCommand(this));
        commands.put(ITerminateHandler.class, new TerminateCommand(this));
        commands.put(IDisconnectHandler.class, new DisconnectCommand(this));
        commands.put(IStepIntoHandler.class, new StepIntoCommand(this));
        commands.put(IStepOverHandler.class, new StepOverCommand(this));
        commands.put(IStepReturnHandler.class, new StepReturnCommand(this));
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(final Class adapter, final TCFNode node) {
        if (adapter == ILaunch.class) return launch;
        if (adapter == IModelSelectionPolicy.class) return selection_policy;
        if (adapter == IModelSelectionPolicyFactory.class) return model_selection_factory;
        return new TCFTask<Object>() {
            public void run() {
                Object o = null;
                if (adapter == IMemoryBlockRetrieval.class || adapter == IMemoryBlockRetrievalExtension.class) {
                    TCFNode n = node;
                    while (n != null && !n.isDisposed()) {
                        if (n instanceof TCFNodeExecContext) {
                            TCFNodeExecContext e = (TCFNodeExecContext)n;
                            if (!e.validateNode(this)) return;
                            if (e.getMemoryContext() != null) {
                                o = mem_retrieval.get(e.id);
                                if (o == null) {
                                    TCFMemoryBlockRetrieval m = new TCFMemoryBlockRetrieval(e);
                                    mem_retrieval.put(e.id, m);
                                    o = m;
                                }
                                break;
                            }
                        }
                        n = n.parent;
                    }
                }
                if (o == null) o = commands.get(adapter);
                assert o == null || adapter.isInstance(o);
                done(o);
            }
        }.getE();
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
        IProcesses prs = launch.getService(IProcesses.class);
        if (prs != null) prs.addListener(prs_listener);
        launchChanged();
    }

    void onDisconnected() {
        assert Protocol.isDispatchThread();
        if (launch_node != null) {
            launch_node.dispose();
            launch_node = null;
        }
        refreshLaunchView();
        assert id2node.size() == 0;
    }
    
    void onProcessOutput(String process_id, final int stream_id, byte[] data) {
        try {
            IProcesses.ProcessContext prs = launch.getProcessContext();
            if (prs == null || !process_id.equals(prs.getID())) return;
            Map<Integer,MessageConsoleStream> streams = consoles.get(process_id);
            if (streams == null) consoles.put(process_id, streams = new HashMap<Integer,MessageConsoleStream>());
            MessageConsoleStream stream = streams.get(stream_id);
            if (stream == null) {
                MessageConsole console = null;
                for (MessageConsoleStream s : streams.values()) console = s.getConsole();
                if (console == null) {
                    final MessageConsole c = console = new MessageConsole("TCF " + process_id,
                            ImageCache.getImageDescriptor(ImageCache.IMG_TCF));
                    final MessageConsoleStream s = stream = console.newMessageStream();
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                                int color_id = SWT.COLOR_BLACK;
                                switch (stream_id) {
                                case 1: color_id = SWT.COLOR_RED; break;
                                case 2: color_id = SWT.COLOR_BLUE; break;
                                case 3: color_id = SWT.COLOR_GREEN; break;
                                }
                                s.setColor(display.getSystemColor(color_id));
                                IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                                manager.addConsoles(new IConsole[]{ c });
                                IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                if (w == null) return;
                                IWorkbenchPage page = w.getActivePage();
                                if (page == null) return;
                                IConsoleView view = (IConsoleView)page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
                                view.display(c);
                            }
                            catch (Throwable x) {
                                Activator.log("Cannot open console view", x);
                            }
                        }
                    });
                }
                streams.put(stream_id, stream);
            }
            stream.print(new String(data, 0, data.length, "UTF-8"));
        }
        catch (Throwable x) {
            Activator.log("Cannot write to console", x);
        }
    }
    
    void onContextActionsStart() {
    }

    void onContextActionsDone() {
        fireModelChanged();
    }

    void onProxyInstalled(final IPresentationContext p, final TCFModelProxy mp) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                model_proxies.put(p, mp);
            }
        });
    }

    void onProxyDisposed(final IPresentationContext p) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert model_proxies.containsKey(p);
                model_proxies.remove(p);
            }
        });
    }
    
    private void onContextRemoved(final String[] context_ids) {
        boolean close_channel = false;
        for (String id : context_ids) {
            launch.removeContextActions(id);
            TCFNode node = getNode(id);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextRemoved();
                if (node.parent == launch_node) close_channel = true;
            }
        }
        fireModelChanged();
        if (close_channel) {
            // Close debug session if the last context is removed:
            onLastContextRemoved();
        }
        display.asyncExec(new Runnable() {
            public void run() {
                for (String id : context_ids) {
                    Activator.getAnnotationManager().onContextRemoved(TCFModel.this, id);
                }
            }
        });
    }
    
    private void onLastContextRemoved() {
        Protocol.invokeLater(1000, new Runnable() {
            public void run() {
                for (Map<Integer,MessageConsoleStream> streams : consoles.values()) {
                    for (MessageConsoleStream stream : streams.values()) {
                        if (!stream.isClosed()) return;
                    }
                }
                if (launch_node == null) return;
                if (launch_node.isDisposed()) return;
                if (!launch_node.validateNode(this)) return;
                if (launch_node.getContextCount() != 0) return;
                launch.onLastContextRemoved();
            }
        });
    }

    Collection<TCFModelProxy> getModelProxyList() {
        return model_proxies.values();
    }

    void launchChanged() {
        if (launch_node != null) {
            launch_node.addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
            fireModelChanged();
        }
        else {
            refreshLaunchView();
        }
    }

    void dispose() {
        final HashSet<MessageConsole> set = new HashSet<MessageConsole>();
        for (Map<Integer,MessageConsoleStream> streams : consoles.values()) {
            for (MessageConsoleStream stream : streams.values()) {
                set.add(stream.getConsole());
                if (!stream.isClosed()) {
                    try {
                        stream.close();
                    }
                    catch (IOException x) {
                        Activator.log("Cannot close console stream", x);
                    }
                }
            }
        }
        consoles.clear();
        display.asyncExec(new Runnable() {
            public void run() {
                IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                manager.removeConsoles(set.toArray(new MessageConsole[set.size()]));
            }
        });
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
        mem_retrieval.remove(id);
    }
    
    void fireModelChanged() {
        assert Protocol.isDispatchThread();
        if (launch.hasPendingContextActions()) return;
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
    
    public TCFDataCache<ISymbols.Symbol> getSymbolInfoCache(String mem_id, final String sym_id) {
        Map<String,TCFDataCache<ISymbols.Symbol>> m = symbols.get(mem_id);
        if (m == null) symbols.put(mem_id, m = new HashMap<String,TCFDataCache<ISymbols.Symbol>>());
        TCFDataCache<ISymbols.Symbol> s = m.get(sym_id);
        if (s == null) m.put(sym_id, s = new TCFDataCache<ISymbols.Symbol>(launch.getChannel()) {
            @Override
            protected boolean startDataRetrieval() {
                ISymbols syms = getLaunch().getService(ISymbols.class);
                if (sym_id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getContext(sym_id, new ISymbols.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, ISymbols.Symbol sym) {
                        set(token, error, sym);
                    }
                });
                return false;
            }
        });
        return s;
    }

    public TCFDataCache<String[]> getSymbolChildrenCache(String mem_id, final String sym_id) {
        Map<String,TCFDataCache<String[]>> m = symbol_children.get(mem_id);
        if (m == null) symbol_children.put(mem_id, m = new HashMap<String,TCFDataCache<String[]>>());
        TCFDataCache<String[]> s = m.get(sym_id);
        if (s == null) m.put(sym_id, s = new TCFDataCache<String[]>(launch.getChannel()) {
            @Override
            protected boolean startDataRetrieval() {
                ISymbols syms = getLaunch().getService(ISymbols.class);
                if (sym_id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getChildren(sym_id, new ISymbols.DoneGetChildren() {
                    public void doneGetChildren(IToken token, Exception error, String[] ids) {
                        set(token, error, ids);
                    }
                });
                return false;
            }
        });
        return s;
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
            // Launch label is provided by TCFLaunchLabelProvider class.
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
        if (id.equals(TCFColumnPresentationExpression.PRESENTATION_ID)) return new TCFColumnPresentationExpression();
        return null;
    }

    public String getColumnPresentationId(IPresentationContext context, Object element) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
            return TCFColumnPresentationRegister.PRESENTATION_ID; 
        }
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
            return TCFColumnPresentationExpression.PRESENTATION_ID; 
        }
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId())) {
            return TCFColumnPresentationExpression.PRESENTATION_ID; 
        }
        return null;
    }
    
    public void setDebugViewSelection(final String node_id, boolean initial_selection) {
        assert Protocol.isDispatchThread();
        if (initial_selection && debug_view_selection_set) return;
        debug_view_selection_set = true;
        final int cnt = ++debug_view_selection_cnt;
        display.asyncExec(new Runnable() {
            public void run() {
                if (cnt != debug_view_selection_cnt) return;
                final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) return;
                final IDebugView view = (IDebugView)window.getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
                if (view == null) return;
                if (!((AbstractDebugView)view).isAvailable()) return;
                Protocol.invokeLater(100, new Runnable() {
                    public void run() {
                        TCFNode node = getNode(node_id);
                        if (node == null) return;
                        if (node.disposed) return;
                        if (!node.validateNode(this)) return;
                        if (node instanceof TCFNodeExecContext) {
                            TCFNode frame = ((TCFNodeExecContext)node).getTopFrame();
                            if (frame != null && !frame.disposed) {
                                if (!frame.validateNode(this)) return;
                                node = frame;
                            }
                        }
                        final TreeModelViewer viewer = (TreeModelViewer)view.getViewer();
                        for (TCFModelProxy proxy : model_proxies.values()) {
                            if (proxy.getProxyViewer() == viewer) {
                                proxy.fireModelChanged();
                                proxy.addDelta(node, IModelDelta.REVEAL);
                                proxy.fireModelChanged();
                                proxy.addDelta(node, IModelDelta.SELECT);
                                proxy.fireModelChanged();
                            }
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Reveal source code associated with given model element.
     * The method is part of ISourceDisplay interface.
     * The method is normally called from SourceLookupService.
     */
    public void displaySource(Object element, final IWorkbenchPage page, boolean forceSourceLookup) {
        final int cnt = ++display_source_cnt;
        if (element instanceof TCFNodeExecContext) {
            final TCFNodeExecContext node = (TCFNodeExecContext)element;
            element = new TCFTask<TCFNode>() {
                public void run() {
                    if (node.disposed) {
                        done(null);
                    }
                    else {
                        if (!node.validateNode(this)) return;
                        if (!node.isSuspended()) {
                            done(null);
                        }
                        else {
                            TCFNodeStackFrame f = node.getTopFrame();
                            done(f == null ? node : f);
                        }
                    }
                }
            }.getE();
        }
        if (element instanceof TCFNodeStackFrame) {
            final TCFNodeStackFrame stack_frame = (TCFNodeStackFrame)element;
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
                                    ILaunchConfiguration cfg = launch.getLaunchConfiguration();
                                    editor_input = editor_not_found.get(cfg);
                                    if (editor_input == null) {
                                        editor_not_found.put(cfg, editor_input = new CommonSourceNotFoundEditorInput(cfg));
                                    }
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
                            displaySource(cnt, editor_id, editor_input, page,
                                    stack_frame.parent.id, stack_frame.getFrameNo() == 0, line);
                        }
                    }
                    else {
                        displaySource(cnt, null, null, page, null, false, 0);
                    }
                }
            });
        }
        else {
            displaySource(cnt, null, null, page, null, false, 0);
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
    
    private void refreshLaunchView() {
        final Throwable error = launch.getError();
        if (error != null) launch.setError(null);
        synchronized (Device.class) {
            if (display.isDisposed()) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                    if (windows == null) return;
                    for (IWorkbenchWindow window : windows) {
                        IDebugView view = (IDebugView)window.getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
                        if (view != null) ((StructuredViewer)view.getViewer()).refresh(launch);
                    }
                }
            });
            if (error != null) showMessageBox("TCF Launch Error", error);
        }
    }
    
    /*
     * Show error message box in active workbench window. 
     * @param title - message box title.
     * @param error - error to be shown.
     */
    public void showMessageBox(final String title, final Throwable error) {
        display.asyncExec(new Runnable() {
            public void run() {
                Shell shell = display.getActiveShell();
                if (shell == null) {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if (window == null) {
                        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                        if (windows == null || windows.length == 0) return;
                        window = windows[0];
                    }
                    shell = window.getShell();
                }
                StringBuffer buf = new StringBuffer();
                Throwable err = error;
                while (err != null) {
                    String msg = err.getLocalizedMessage();
                    if (msg == null || msg.length() == 0) msg = err.getClass().getName();
                    buf.append(msg);
                    err = err.getCause();
                    if (err != null) {
                        buf.append('\n');
                        buf.append("Caused by:\n");
                    }
                }
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setText(title);
                mb.setMessage(buf.toString());
                mb.open();
            }
        });
    }
    
    /*
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

    /*
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
                return document.getLineInformation(lineNumber - 1);
        }
        catch (BadLocationException e) {
        }
        finally {
            provider.disconnect(input);
        }
        return null;
    }

    public synchronized void addSuspendTriggerListener(ISuspendTriggerListener listener) {
        suspend_trigger_listeners.add(listener);
    }

    public synchronized void removeSuspendTriggerListener(ISuspendTriggerListener listener) {
        suspend_trigger_listeners.remove(listener);
    }       
    
    private synchronized void runSuspendTrigger() {
        final int generation = ++suspend_trigger_generation;
        final ISuspendTriggerListener[] listeners = suspend_trigger_listeners.toArray(
                new ISuspendTriggerListener[suspend_trigger_listeners.size()]);
        if (listeners.length == 0) return;
        display.asyncExec(new Runnable() {
            public void run() {
                synchronized (TCFModel.this) {
                    if (generation != suspend_trigger_generation) return;
                }
                for (final ISuspendTriggerListener listener : listeners) {
                    try {
                        listener.suspended(launch, null);
                    }
                    catch (Throwable x) {
                        Activator.log(x);
                    };             
                }        
            }
        });
    }
}
