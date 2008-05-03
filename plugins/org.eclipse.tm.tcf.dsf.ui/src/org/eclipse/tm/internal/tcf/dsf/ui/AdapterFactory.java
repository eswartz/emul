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
package org.eclipse.tm.internal.tcf.dsf.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.dd.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.DisplayDsfExecutor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.dsf.launch.TCFDSFLaunch;
import org.eclipse.tm.internal.tcf.dsf.ui.actions.TcfTerminateCommand;
import org.eclipse.tm.internal.tcf.dsf.ui.viewmodel.ViewModelAdapter;
import org.eclipse.tm.tcf.protocol.Protocol;


@SuppressWarnings("restriction")
public class AdapterFactory implements IAdapterFactory, DsfSession.SessionEndedListener, ILaunchesListener2 {

    @Immutable
    private final class SessionAdapterSet {

        private final DsfSession session;
        final ViewModelAdapter view_model_adapter;
        final DsfSourceDisplayAdapter source_display_adapter;
        final DsfStepIntoCommand step_into_command;
        final DsfStepOverCommand step_over_command;
        final DsfStepReturnCommand step_return_command;
        final DsfSuspendCommand suspend_command;
        final DsfResumeCommand resume_command;
        final TcfTerminateCommand terminate_command;
        final IDebugModelProvider debug_model_provider;
        final TCFDSFLaunch lunch;
        //final BreakpointCommand breakpoint_command; 
        //final DsfMemoryBlockRetrieval memory_retrieval;

        SessionAdapterSet(DsfSession session, TCFDSFLaunch launch) {
            this.session = session;

            view_model_adapter = new ViewModelAdapter(session, launch);

            if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
                source_display_adapter = new DsfSourceDisplayAdapter(session,
                        (ISourceLookupDirector)launch.getSourceLocator());
            }
            else {
                source_display_adapter = null;
            }
            session.registerModelAdapter(ISourceDisplay.class, source_display_adapter);

            step_into_command = new DsfStepIntoCommand(session);
            step_over_command = new DsfStepOverCommand(session);
            step_return_command = new DsfStepReturnCommand(session);
            suspend_command = new DsfSuspendCommand(session);
            resume_command = new DsfResumeCommand(session);
            terminate_command = new TcfTerminateCommand(session);
            //breakpoint_command = new BreakpointCommand();
            //memory_retrieval = new DsfMemoryBlockRetrieval(ITCFConstants.ID_TCF_DEBUG_MODEL, );
            session.registerModelAdapter(IStepIntoHandler.class, step_into_command);
            session.registerModelAdapter(IStepOverHandler.class, step_over_command);
            session.registerModelAdapter(IStepReturnHandler.class, step_return_command);
            session.registerModelAdapter(ISuspendHandler.class, suspend_command);
            session.registerModelAdapter(IResumeHandler.class, resume_command);
            session.registerModelAdapter(ITerminateHandler.class, terminate_command);
            //session.registerModelAdapter(IToggleBreakpointsTarget.class, breakpoint_command);

            debug_model_provider = new IDebugModelProvider() {
                // @see org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
                public String[] getModelIdentifiers() {
                    return new String[] { ITCFConstants.ID_TCF_DEBUG_MODEL };
                }
            };
            session.registerModelAdapter(IDebugModelProvider.class, debug_model_provider);

            lunch = launch;

            /*
             * Registering the launch as an adapter, ensures that this launch, 
             * and debug model ID will be associated with all DMContexts from this 
             * session.  
             */  
            session.registerModelAdapter(ILaunch.class, lunch);
        }

        void dispose() {
            view_model_adapter.dispose();

            session.unregisterModelAdapter(ISourceDisplay.class);
            if (source_display_adapter != null) source_display_adapter.dispose();

            session.unregisterModelAdapter(IStepIntoHandler.class);
            session.unregisterModelAdapter(IStepOverHandler.class);
            session.unregisterModelAdapter(IStepReturnHandler.class);
            session.unregisterModelAdapter(ISuspendHandler.class);
            session.unregisterModelAdapter(IResumeHandler.class);
            session.unregisterModelAdapter(ITerminateHandler.class);            
            step_into_command.dispose();
            step_over_command.dispose();
            step_return_command.dispose();
            suspend_command.dispose();
            resume_command.dispose();
            terminate_command.dispose();
        }        
    }

    private static final Class<?>[] adapter_list = {
        IElementContentProvider.class,
        IColumnPresentationFactory.class,
        IModelProxyFactory.class,
        ITerminateHandler.class
    };

    private static final Map<String,SessionAdapterSet> session_adapter_set_map = 
        Collections.synchronizedMap(new HashMap<String,SessionAdapterSet>());

    public AdapterFactory() {
        assert session_adapter_set_map.isEmpty();
        DsfSession.addSessionEndedListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
        final Display display = Display.getDefault();
        display.asyncExec(new Runnable() {
            public void run() {
                final DisplayDsfExecutor executer = DisplayDsfExecutor.getDisplayDsfExecutor(display);
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        Protocol.addCongestionMonitor(new Protocol.CongestionMonitor() {
                            public int getCongestionLevel() {
                                int level = executer.getQueue().size() / 4 - 100;
                                if (level > 100) level = 100;
                                return level;
                            }
                        });
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof TCFDSFLaunch)) return null; 

        TCFDSFLaunch launch = (TCFDSFLaunch)adaptableObject;

        // Find the correct set of adapters based on the launch session-ID.  If not found
        // it means that we have a new launch and new session, and we have to create a 
        // new set of adapters. 
        DsfSession session = launch.getSession();
        if (session == null) return null;

        SessionAdapterSet adapter_set;
        synchronized(session_adapter_set_map) {
            adapter_set = session_adapter_set_map.get(session.getId());
            if (adapter_set == null) {
                adapter_set = new SessionAdapterSet(session, launch);
                session_adapter_set_map.put(session.getId(), adapter_set);
            }
        }

        // Returns the adapter type for the launch object.
        if (adapterType.equals(IElementContentProvider.class)) return adapter_set.view_model_adapter;
        if (adapterType.equals(IModelProxyFactory.class)) return adapter_set.view_model_adapter;
        if (adapterType.equals(IColumnPresentationFactory.class)) return adapter_set.view_model_adapter;
        if (adapterType.equals(ITerminateHandler.class)) return adapter_set.terminate_command;
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return adapter_list;
    }

    public void sessionEnded(DsfSession session) {
    }

    public void launchesTerminated(ILaunch[] launches) {
    }

    public void launchesAdded(ILaunch[] launches) {
    }

    public void launchesChanged(ILaunch[] launches) {
    }

    public void launchesRemoved(ILaunch[] launches) {
        // Dispose the set of adapters for a launch only after the launch is removed.
        for (ILaunch launch : launches) {
            if (launch instanceof TCFDSFLaunch) {
                String id = ((TCFDSFLaunch)launch).getSession().getId();
                synchronized (session_adapter_set_map) {
                    if (session_adapter_set_map.containsKey(id)) {
                        session_adapter_set_map.remove(id).dispose();
                    }
                }
            }                
        }
    }
}
