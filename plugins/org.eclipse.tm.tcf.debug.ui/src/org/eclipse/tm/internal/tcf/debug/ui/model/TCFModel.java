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
import java.util.Set;

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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.contexts.ISuspendTriggerListener;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Device;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
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

    private final Set<String> running_actions = new HashSet<String>();
    private final Map<String,String> finished_actions = new HashMap<String,String>();

    private final Map<IPresentationContext,TCFModelProxy> model_proxies =
        new HashMap<IPresentationContext,TCFModelProxy>();

    private final Map<String,TCFNode> id2node = new HashMap<String,TCFNode>();

    @SuppressWarnings("unchecked")
    private final Map<Class,Object> commands = new HashMap<Class,Object>();

    private final Map<String,IMemoryBlockRetrievalExtension> mem_retrieval =
        new HashMap<String,IMemoryBlockRetrievalExtension>();

    private class Console {
        final IOConsole console;
        final Map<Integer,IOConsoleOutputStream> out;

        Console(final IOConsole console) {
            this.console = console;
            out = new HashMap<Integer,IOConsoleOutputStream>();
            Thread t = new Thread() {
                public void run() {
                    try {
                        IOConsoleInputStream inp = console.getInputStream();
                        final byte[] buf = new byte[0x100];
                        for (;;) {
                            final int len = inp.read(buf);
                            if (len < 0) break;
                            Protocol.invokeAndWait(new Runnable() {
                                public void run() {
                                    launch.writeProcessInputStream(buf, 0, len);
                                }
                            });
                        }
                    }
                    catch (Throwable x) {
                        Activator.log("Cannot read console input", x);
                    }
                }
            };
            t.setName("TCF Launch Console Input");
            t.start();
        }

        void close() {
            for (IOConsoleOutputStream stream : out.values()) {
                try {
                    stream.close();
                }
                catch (IOException x) {
                    Activator.log("Cannot close console stream", x);
                }
            }
            try {
                console.getInputStream().close();
            }
            catch (IOException x) {
                Activator.log("Cannot close console stream", x);
            }
        }
    }

    private Console console;

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
        }

        public void contextRemoved(final String[] context_ids) {
            onContextRemoved(context_ids);
        }

        public void memoryChanged(String context_id, Number[] addr, long[] size) {
            TCFNode node = getNode(context_id);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onMemoryChanged(addr, size);
            }
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
            runSuspendTrigger(node);
            finished_actions.remove(context);
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
        }

        public void contextChanged(IRunControl.RunControlContext[] contexts) {
            for (int i = 0; i < contexts.length; i++) {
                TCFNode node = getNode(contexts[i].getID());
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onContextChanged(contexts[i]);
                }
            }
        }

        public void contextException(String context, String msg) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextException(msg);
            }
        }

        public void contextRemoved(final String[] context_ids) {
            onContextRemoved(context_ids);
        }

        public void contextResumed(final String context) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextResumed();
            }
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
            }
            if (!isContextActionRunning(context)) {
                setDebugViewSelection(context, false);
            }
            runSuspendTrigger(node);
            finished_actions.remove(context);
        }
    };

    private final IRegisters.RegistersListener reg_listener = new IRegisters.RegistersListener() {

        public void contextChanged() {
            for (TCFNode node : id2node.values()) {
                if (node instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)node).onRegistersChanged();
                }
            }
        }

        public void registerChanged(String context) {
            TCFNode node = getNode(context);
            if (node instanceof TCFNodeRegister) {
                ((TCFNodeRegister)node).onValueChanged();
            }
        }
    };

    private final IProcesses.ProcessesListener prs_listener = new IProcesses.ProcessesListener() {

        public void exited(String process_id, int exit_code) {
            IProcesses.ProcessContext prs = launch.getProcessContext();
            if (prs != null && process_id.equals(prs.getID())) onLastContextRemoved();
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
                            TCFDataCache<IMemory.MemoryContext> cache = e.getMemoryContext();
                            if (!cache.validate(this)) return;
                            if (cache.getData() != null) {
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
            if (console == null) {
                final IOConsole c = new IOConsole("TCF " + process_id, null,
                        ImageCache.getImageDescriptor(ImageCache.IMG_TCF), "UTF-8", true);
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
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
                console = new Console(c);
            }
            IOConsoleOutputStream stream = console.out.get(stream_id);
            if (stream == null) {
                final IOConsoleOutputStream s = stream = console.console.newOutputStream();
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
                        }
                        catch (Throwable x) {
                            Activator.log("Cannot open console view", x);
                        }
                    }
                });
                console.out.put(stream_id, stream);
            }
            stream.write(data, 0, data.length);
        }
        catch (Throwable x) {
            Activator.log("Cannot write to console", x);
        }
    }

    void onContextActionsStart(String id) {
        running_actions.add(id);
    }

    void onContextActionsDone(String id, String result) {
        running_actions.remove(id);
        finished_actions.put(id, result);
        TCFNode n = id2node.get(id);
        if (n instanceof TCFNodeExecContext) {
            ((TCFNodeExecContext)n).onContextActionDone();
        }
        setDebugViewSelection(id, false);
    }

    String getContextActionResult(String id) {
        return finished_actions.get(id);
    }

    boolean isContextActionRunning(String id) {
        return running_actions.contains(id);
    }

    boolean isContextActionResultAvailable(String id) {
        return finished_actions.containsKey(id);
    }

    void onProxyInstalled(final IPresentationContext p, final TCFModelProxy mp) {
        model_proxies.put(p, mp);
    }

    void onProxyDisposed(final IPresentationContext p) {
        assert model_proxies.containsKey(p);
        model_proxies.remove(p);
    }

    private void onContextRemoved(final String[] context_ids) {
        boolean close_channel = false;
        for (String id : context_ids) {
            launch.removeContextActions(id, null);
            TCFNode node = getNode(id);
            if (node instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)node).onContextRemoved();
                if (node.parent == launch_node) close_channel = true;
            }
            finished_actions.remove(id);
        }
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
                if (launch_node == null) return;
                if (launch_node.isDisposed()) return;
                TCFChildrenExecContext children = launch_node.getChildren();
                if (!children.validate(this)) return;
                if (children.size() != 0) return;
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
        }
        else {
            refreshLaunchView();
        }
    }

    void dispose() {
        if (console != null) {
            display.asyncExec(new Runnable() {
                public void run() {
                    console.close();
                    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                    manager.removeConsoles(new IOConsole[]{ console.console });
                }
            });
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
        mem_retrieval.remove(id);
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
        if (mem_id == null || sym_id == null) return null;
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
        if (mem_id == null || sym_id == null) return null;
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
        Protocol.invokeLater(100, new Runnable() {
            public void run() {
                TCFNode node = getNode(node_id);
                if (node == null) return;
                if (node.disposed) return;
                if (cnt != debug_view_selection_cnt) return;
                if (node instanceof TCFNodeExecContext) {
                    TCFDataCache<TCFContextState> state_cache = ((TCFNodeExecContext)node).getState();
                    if (!state_cache.validate(this)) return;
                    TCFContextState state_data = state_cache.getData();
                    if (state_data == null || !state_data.is_suspended) return;
                    TCFChildrenStackTrace stack_trace = ((TCFNodeExecContext)node).getStackTrace();
                    if (!stack_trace.validate(this)) return;
                    TCFNode frame = stack_trace.getTopFrame();
                    if (frame != null && !frame.disposed) node = frame;
                }
                for (TCFModelProxy proxy : model_proxies.values()) {
                    if (proxy.getPresentationContext().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
                        proxy.setSelection(node);
                    }
                }
            }
        });
    }

    /**
     * Reveal source code associated with given model element.
     * The method is part of ISourceDisplay interface.
     * The method is normally called from SourceLookupService.
     */
    public void displaySource(Object model_element, final IWorkbenchPage page, boolean forceSourceLookup) {
        final int cnt = ++display_source_cnt;
        /* Because of racing in Eclipse Debug infrastructure, 'model_element' value can be invalid.
         * As a workaround, get current debug view selection.
         */
        if (page != null) {
            ISelection context = DebugUITools.getDebugContextManager().getContextService(page.getWorkbenchWindow()).getActiveContext();
            if (context instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection)context;
                if (!selection.isEmpty()) model_element = selection.getFirstElement();
            }
        }
        final Object element = model_element;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (cnt != display_source_cnt) return;
                TCFNodeStackFrame stack_frame = null;
                IChannel channel = getLaunch().getChannel();
                if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                    if (element instanceof TCFNodeExecContext) {
                        TCFNodeExecContext node = (TCFNodeExecContext)element;
                        if (!node.disposed) {
                            TCFDataCache<TCFContextState> state_cache = node.getState();
                            if (!state_cache.validate(this)) return;
                            TCFContextState state_data = state_cache.getData();
                            if (state_data != null && state_data.is_suspended) {
                                TCFChildrenStackTrace stack_trace = ((TCFNodeExecContext)node).getStackTrace();
                                if (!stack_trace.validate(this)) return;
                                stack_frame = stack_trace.getTopFrame();
                            }
                        }
                    }
                    else if (element instanceof TCFNodeStackFrame) {
                        TCFNodeStackFrame node = (TCFNodeStackFrame)element;
                        if (!node.disposed) stack_frame = (TCFNodeStackFrame)element;
                    }
                }
                if (stack_frame != null) {
                    TCFDataCache<TCFSourceRef> line_info = stack_frame.getLineInfo();
                    if (!line_info.validate(this)) return;
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
                else {
                    displaySource(cnt, null, null, page, null, false, 0);
                }
            }
        });
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

    /*
     * Refresh Launch View.
     * Normally the view is updated by sending deltas through model proxy.
     * This method is used only when launch is not yet connected or already disconnected.
     */
    private void refreshLaunchView() {
        // TODO: there should be a better way to refresh Launch View
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

    private synchronized void runSuspendTrigger(final TCFNode node) {
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
                        listener.suspended(launch, node);
                    }
                    catch (Throwable x) {
                        Activator.log(x);
                    }
                }
            }
        });
    }
}
