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
package org.eclipse.tm.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;


/**
 * IProcesses service provides access to the target OS's process 
 * information, allows to start and terminate a process, and allows
 * to attach and detach a process for debugging. Debug services,
 * like IMemory and IRunControl, require a process to be attached
 * before they can access it. 
 */
public interface IProcesses extends IService {

    static final String NAME = "Processes";
    
    /**
     * Retrieve context info for given context ID.
     * A context corresponds to an execution thread, process, address space, etc.
     * Context IDs are valid across TCF services, so it is allowed to issue
     * 'IProcesses.getContext' command with a context that was obtained,
     * for example, from Memory service.
     * However, 'Processes.getContext' is supposed to return only process specific data,
     * If the ID is not a process ID, 'IProcesses.getContext' may not return any
     * useful information
     *    
     * @param id – context ID. 
     * @param done - call back interface called when operation is completed.
     */
    IToken getContext(String id, DoneGetContext done);

    /**
     * Client call back interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when context data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context data.
         */
        void doneGetContext(IToken token, Exception error, ProcessContext context);
    }

    /**
     * Retrieve children of given context.
     *   
     * @param parent_context_id – parent context ID. Can be null –
     * to retrieve top level of the hierarchy, or one of context IDs retrieved
     * by previous getContext or getChildren commands. 
     * @param done - call back interface called when operation is completed.
     */
    IToken getChildren(String parent_context_id, boolean attached_only, DoneGetChildren done);

    /**
     * Client call back interface for getChildren().
     */
    interface DoneGetChildren {
        /**
         * Called when context list retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context_ids – array of available context IDs.
         */
        void doneGetChildren(IToken token, Exception error, String[] context_ids);
    }
    
    /**
     * Context property names.
     */
    static final String
        /** The TCF context ID */
        PROP_ID = "ID",
        
        /** The TCF parent context ID */
        PROP_PARENTID = "ParentID",
        
        /** Is the context attached */
        PROP_ATTACHED = "Attached",
        
        /** Can terminate the context */
        PROP_CAN_TERMINATE = "CanTerminate",
        
        /** Process name. Client UI can show this name to a user */
        PROP_NAME = "Name";
    
    interface ProcessContext {
        
        /** 
         * Get context ID.
         * Same as getProperties().get(“ID”)
         */
        String getID();

        /**
         * Get parent context ID.
         * Same as getProperties().get(“ParentID”)
         */
        String getParentID();

        /**
         * Get process name.
         * Client UI can show this name to a user.
         * Same as getProperties().get(“Name”)
         */
        String getName();

        /**
         * Utility method to read context property PROP_ATTACHED.
         * Services like IRunControl, IMemory, IBreakpoints work only with attached processes.
         * @return value of PROP_ATTACHED.
         */
        boolean isAttached();

        /**
         * Utility method to read context property PROP_CAN_TERMINATE.
         * @return value of PROP_CAN_TERMINATE.
         */
        boolean canTerminate();

        /**
         * Get all available context properties.
         * @return Map 'property name' -> 'property value'
         */
        Map<String, Object> getProperties();
        
        /**
         * Attach debugger to a process.
         * Services like IRunControl, IMemory, IBreakpoints work only with attached processes.
         * @param done - call back interface called when operation is completed.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken attach(DoneCommand done);

        /**
         * Detach debugger from a process.
         * Process execution will continue without debugger supervision.
         * @param done - call back interface called when operation is completed.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken detach(DoneCommand done);
        
        /**
         * Terminate a process. 
         * @param done - call back interface called when operation is completed.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken terminate(DoneCommand done);
    
        /**
         * Send a signal to a process.
         * @param signal - signal ID.
         * @param done - call back interface called when operation is completed.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken signal(int signal, DoneCommand done);
    }
    
    interface DoneCommand {
        void doneCommand(IToken token, Exception error);
    }
    
    /**
     * Get default set of environment variables used to start a new process.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken getEnvironment(DoneGetEnvironment done);
    
    interface DoneGetEnvironment {
        void doneGetEnvironment(IToken token, Exception error, Map<String,String> environment);
    }

    /**
     * Start a new process on remote machine.
     * @param directory - initial value of working directory for the process.
     * @param file - process image file.
     * @param command_line - command line arguments for the process.
     * @param environment - map of environment variables for the process,
     * if null then default set of environment variables will be used. 
     * @param attach - if true debugger should be attached to the process.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken start(String directory, String file,
            String[] command_line, Map<String,String> environment,
            boolean attach, DoneStart done);
    
    interface DoneStart {
        void doneStart(IToken token, Exception error, ProcessContext process);
    }
}
