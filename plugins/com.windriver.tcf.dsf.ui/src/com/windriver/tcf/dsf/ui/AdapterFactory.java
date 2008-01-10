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
package com.windriver.tcf.dsf.ui;

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
import org.eclipse.dd.dsf.debug.ui.actions.DsfTerminateCommand;
import org.eclipse.dd.dsf.debug.ui.sourcelookup.MISourceDisplayAdapter;
import org.eclipse.dd.dsf.service.DsfSession;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

import com.windriver.debug.tcf.core.model.ITCFConstants;
import com.windriver.tcf.dsf.core.launch.TCFDSFLaunch;

@SuppressWarnings("restriction")
public class AdapterFactory implements IAdapterFactory, DsfSession.SessionEndedListener, ILaunchesListener2 {

    @Immutable
    private final class SessionAdapterSet {
        
        private final DsfSession fSession;
        final ViewModelAdapter fViewModelAdapter;
        final MISourceDisplayAdapter fSourceDisplayAdapter;
        final DsfStepIntoCommand fStepIntoCommand;
        final DsfStepOverCommand fStepOverCommand;
        final DsfStepReturnCommand fStepReturnCommand;
        final DsfSuspendCommand fSuspendCommand;
        final DsfResumeCommand fResumeCommand;
        final DsfTerminateCommand fTerminateCommand;
        final IDebugModelProvider fDebugModelProvider;
        final TCFDSFLaunch fLaunch;

        SessionAdapterSet(DsfSession session, TCFDSFLaunch launch) {
            fSession = session;
            
            fViewModelAdapter = new ViewModelAdapter(session, launch);

            if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
                fSourceDisplayAdapter = new MISourceDisplayAdapter(session, (ISourceLookupDirector)launch.getSourceLocator());
            } else {
                fSourceDisplayAdapter = null;
            }
            session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);
            
            fStepIntoCommand = new DsfStepIntoCommand(session);
            fStepOverCommand = new DsfStepOverCommand(session);
            fStepReturnCommand = new DsfStepReturnCommand(session);
            fSuspendCommand = new DsfSuspendCommand(session);
            fResumeCommand = new DsfResumeCommand(session);
            fTerminateCommand = new DsfTerminateCommand(session);
            session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
            session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
            session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
            session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
            session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
            session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);

            fDebugModelProvider = new IDebugModelProvider() {
                // @see org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
                public String[] getModelIdentifiers() {
                    return new String[] { ITCFConstants.ID_TCF_DEBUG_MODEL };
                }
            };
            session.registerModelAdapter(IDebugModelProvider.class, fDebugModelProvider);

            fLaunch = launch;
            
            /*
             * Registering the launch as an adapter, ensures that this launch, 
             * and debug model ID will be associated with all DMContexts from this 
             * session.  
             */  
            session.registerModelAdapter(ILaunch.class, fLaunch);
        }
        
        void dispose() {
            fViewModelAdapter.dispose();

            fSession.unregisterModelAdapter(ISourceDisplay.class);
            if (fSourceDisplayAdapter != null) fSourceDisplayAdapter.dispose();
            
            fSession.unregisterModelAdapter(IStepIntoHandler.class);
            fSession.unregisterModelAdapter(IStepOverHandler.class);
            fSession.unregisterModelAdapter(IStepReturnHandler.class);
            fSession.unregisterModelAdapter(ISuspendHandler.class);
            fSession.unregisterModelAdapter(IResumeHandler.class);
            fSession.unregisterModelAdapter(ITerminateHandler.class);            
            fStepIntoCommand.dispose();
            fStepOverCommand.dispose();
            fStepReturnCommand.dispose();
            fSuspendCommand.dispose();
            fResumeCommand.dispose();
            fTerminateCommand.dispose();
        }        
    }
    
    @SuppressWarnings({ "unchecked", "restriction" })
    private final Class[] adapter_list = {
        IElementLabelProvider.class,
        IElementContentProvider.class,
        IColumnPresentationFactory.class,
        IModelProxyFactory.class,
        ITerminateHandler.class
    };

    private Map<String,SessionAdapterSet> fSessionAdapterSetMap = 
        Collections.synchronizedMap(new HashMap<String,SessionAdapterSet>());
    
    public AdapterFactory() {
        DsfSession.addSessionEndedListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }

    @SuppressWarnings({ "restriction", "unchecked" })
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof TCFDSFLaunch)) return null; 

        TCFDSFLaunch launch = (TCFDSFLaunch)adaptableObject;

        // Find the correct set of adapters based on the launch session-ID.  If not found
        // it means that we have a new launch and new session, and we have to create a 
        // new set of adapters. 
        DsfSession session = launch.getSession();
        if (session == null) return null;

        SessionAdapterSet adapter_set;
        synchronized(fSessionAdapterSetMap) {
            adapter_set = fSessionAdapterSetMap.get(session.getId());
            if (adapter_set == null) {
                adapter_set = new SessionAdapterSet(session, launch);
                fSessionAdapterSetMap.put(session.getId(), adapter_set);
            }
        }
        
        // Returns the adapter type for the launch object.
        if (adapterType.equals(IElementLabelProvider.class)) return adapter_set.fViewModelAdapter;
        if (adapterType.equals(IElementContentProvider.class)) return adapter_set.fViewModelAdapter;
        if (adapterType.equals(IModelProxyFactory.class)) return adapter_set.fViewModelAdapter;
        if (adapterType.equals(IColumnPresentationFactory.class)) return adapter_set.fViewModelAdapter;
        if (adapterType.equals(ITerminateHandler.class)) return adapter_set.fTerminateCommand;
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
                DsfSession session = ((TCFDSFLaunch)launch).getSession();
                synchronized (fSessionAdapterSetMap) {
                    if (fSessionAdapterSetMap.containsKey(session.getId())) {
                        fSessionAdapterSetMap.get(session.getId()).dispose();
                        fSessionAdapterSetMap.remove(session);
                    }
                }
            }                
        }
    }
}
