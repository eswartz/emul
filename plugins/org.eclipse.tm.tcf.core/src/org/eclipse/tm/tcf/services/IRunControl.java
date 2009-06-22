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


public interface IRunControl extends IService {

    static final String NAME = "RunControl";
    
    /**
     * Context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_PARENT_ID = "ParentID",
        PROP_PROCESS_ID = "ProcessID",
        PROP_IS_CONTAINER = "IsContainer",
        PROP_HAS_STATE = "HasState",
        PROP_CAN_RESUME = "CanResume",
        PROP_CAN_COUNT = "CanCount",
        PROP_CAN_SUSPEND = "CanSuspend",
        PROP_CAN_TERMINATE = "CanTerminate";
    
    /**
     * Context resume modes.  
     */
    static final int
        
        RM_RESUME = 0,
        
        /**
         * Step over a single instruction.
         * If the instruction is a function call then don't stop until the function returns.
         */
        RM_STEP_OVER = 1,
        
        /**
         * Step a single instruction.
         * If the instruction is a function call then stop at first instruction of the function.
         */
        RM_STEP_INTO = 2,
        
        /**
         * Step over a single source code line.
         * If the line contains a function call then don't stop until the function returns.
         */
        RM_STEP_OVER_LINE = 3,
        
        /**
         * Step a single source code line.
         * If the line contains a function call then stop at first line of the function.
         */
        RM_STEP_INTO_LINE = 4,
        
        /**
         * Run until control returns from current function.
         */
        RM_STEP_OUT = 5,
        
        /**
         * Start running backwards.
         * Execution will continue until suspended by command or breakpoint.
         */
        RM_REVERSE_RESUME = 6,
        
        /**
         * Reverse of RM_STEP_OVER - run backwards over a single instruction.
         * If the instruction is a function call then don't stop until get out of the function.
         */
        RM_REVERSE_STEP_OVER = 7,
        
        /**
         * Reverse of RM_STEP_INTO.
         * This effectively "un-executes" the previous instruction
         */
        RM_REVERSE_STEP_INTO = 8,
        
        /**
         * Reverse of RM_STEP_OVER_LINE.
         * Resume backward execution of given context until control reaches an instruction that belongs
         * to a different source line. 
         * If the line contains a function call then don't stop until get out of the function.
         * Error is returned if line number information not available.
         */
        RM_REVERSE_STEP_OVER_LINE = 9,
        
        /**
         * Reverse of RM_STEP_INTO_LINE,
         * Resume backward execution of given context until control reaches an instruction that belongs
         * to a different line of source code.
         * If a function is called, stop at the beginning of the last line of the function code.
         * Error is returned if line number information not available.
         */
        RM_REVERSE_STEP_INTO_LINE = 10,
        
        /**
         * Reverse of RM_STEP_OUT.
         * Resume backward execution of the given context until control reaches the point where the current function was called.
         */
        RM_REVERSE_STEP_OUT = 11,
        
        /**
         * Step over instructions until PC is outside the specified range.
         * If any function call within the range is considered to be in range.
         */
        RM_STEP_OVER_RANGE = 12,
        
        /**
         * Step instruction until PC is outside the specified range for any reason.
         */
        RM_STEP_INTO_RANGE = 13,
        
        /**
         * Reverse of RM_STEP_OVER_RANGE
         */
        RM_REVERSE_STEP_OVER_RANGE = 14,
        
        /**
         * Reverse of RM_STEP_INTO_RANGE
         */
        RM_REVERSE_STEP_INTO_RANGE = 15;
    
    /**
     * State change reason of a context.
     * Reason can be any text, but if it is one of predefined strings,
     * a generic client might be able to handle it better. 
     */
    static final String
        REASON_USER_REQUEST = "Suspended",
        REASON_STEP = "Step",
        REASON_BREAKPOINT = "Breakpoint",
        REASON_EXCEPTION = "Exception",
        REASON_CONTAINER = "Container",
        REASON_WATCHPOINT = "Watchpoint",
        REASON_SIGNAL = "Signal",
        REASON_SHAREDLIB = "Shared Library",
        REASON_ERROR = "Error";
    
    /**
     * Retrieve context properties for given context ID.
     *   
     * @param id – context ID. 
     * @param done - callback interface called when operation is completed.
     */
    IToken getContext(String id, DoneGetContext done);

    /**
     * Client callback interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when context data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context data.
         */
        void doneGetContext(IToken token, Exception error, RunControlContext context);
    }

    /**
     * Retrieve children of given context.
     *   
     * @param parent_context_id – parent context ID. Can be null –
     * to retrieve top level of the hierarchy, or one of context IDs retrieved
     * by previous getContext or getChildren commands. 
     * @param done - callback interface called when operation is completed.
     */
    IToken getChildren(String parent_context_id, DoneGetChildren done);

    /**
     * Client callback interface for getChildren().
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
     * A context corresponds to an execution thread, process, address space, etc.
     * A context can belong to a parent context. Contexts hierarchy can be simple
     * plain list or it can form a tree. It is up to target agent developers to choose
     * layout that is most descriptive for a given target. Context IDs are valid across
     * all services. In other words, all services access same hierarchy of contexts,
     * with same IDs, however, each service accesses its own subset of context's
     * attributes and functionality, which is relevant to that service. 
     */
    interface RunControlContext {

        /** 
         * Retrieve context ID.
         * Same as getProperties().get(“ID”)
         */
        String getID();

        /** 
         * Retrieve parent context ID.
         * Same as getProperties().get(“ParentID”)
         */
        String getParentID();

        /**
         * Get context properties. See PROP_* definitions for property names.
         * Context properties are read only, clients should not try to modify them.
         * @return Map of context properties.
         */
        Map<String,Object> getProperties();

        /**
         * Utility method to read context property PROP_IS_CONTAINER.
         * Executing resume or suspend command on a container causes all its children to resume or suspend.
         * @return value of PROP_IS_CONTAINER.
         */
        boolean isContainer();
        
        /**
         * Utility method to read context property PROP_HAS_STATE.
         * Only context that has a state can be resumed or suspended. 
         * @return value of PROP_HAS_STATE.
         */
        boolean hasState();
        
        /**
         * Utility method to read context property PROP_CAN_SUSPEND.
         * Value 'true' means suspend command is supported by the context,
         * however the method does not check that the command can be executed successfully in
         * the current state of the context. For example, the command still can fail if context is
         * already suspended.
         * @return value of PROP_CAN_SUSPEND.
         */
        boolean canSuspend();
        
        /**
         * Utility method to read a 'mode' bit in context property PROP_CAN_RESUME.
         * Value 'true' means resume command is supported by the context,
         * however the method does not check that the command can be executed successfully in
         * the current state of the context. For example, the command still can fail if context is
         * already resumed.
         * @param mode - resume mode, see RM_*. 
         * @return value of requested bit of PROP_CAN_RESUME. 
         */
        boolean canResume(int mode);
        
        /**
         * Utility method to read a 'mode' bit in context property PROP_CAN_COUNT.
         * Value 'true' means resume command with count other then 1 is supported by the context,
         * however the method does not check that the command can be executed successfully in
         * the current state of the context. For example, the command still can fail if context is
         * already resumed.
         * @param mode - resume mode, see RM_*. 
         * @return value of requested bit of PROP_CAN_COUNT. 
         */
        boolean canCount(int mode);
        
        /**
         * Utility method to read context property PROP_CAN_TERMINATE.
         * Value 'true' means terminate command is supported by the context,
         * however the method does not check that the command can be executed successfully in
         * the current state of the context. For example, the command still can fail if context is
         * already exited.
         * @return value of PROP_CAN_SUSPEND.
         */
        boolean canTerminate();

        /**
         * Send a command to retrieve current state of a context.
         * @param done - command result call back object.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken getState(DoneGetState done);

        /**
         * Send a command to suspend a context.
         * Also suspends children if context is a container.
         * @param done - command result call back object.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken suspend(DoneCommand done);
        
        /**
         * Send a command to resume a context.
         * Also resumes children if context is a container.
         * @param mode - defines how to resume the context, see RM_*.
         * @param count - if mode implies stepping, defines how many steps to perform.
         * @param done - command result call back object.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken resume(int mode, int count, DoneCommand done);
        
        /**
         * Send a command to resume a context.
         * Also resumes children if context is a container.
         * @param mode - defines how to resume the context, see RM_*.
         * @param count - if mode implies stepping, defines how many steps to perform.
         * @param params - resume parameters, for example, step range definition.
         * @param done - command result call back object.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken resume(int mode, int count, Map<String,Object> params, DoneCommand done);
        
        /**
         * Send a command to terminate a context.
         * @param done - command result call back object.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken terminate(DoneCommand done);
    }

    class RunControlError extends Exception {

        private static final long serialVersionUID = 1L;
    }

    interface DoneGetState {
        void doneGetState(IToken token, Exception error, boolean suspended, String pc,
                String reason, Map<String,Object> params);
    }

    interface DoneCommand {
        /**
         * Called when run control command execution is complete.
         * @param token - pending command handle.
         * @param error - command execution error or null.
         */
        void doneCommand(IToken token, Exception error);
    }

    /**
     * Add run control event listener.
     * @param listener - run control event listener to add.
     */
    void addListener(RunControlListener listener);
    
    /**
     * Remove run control event listener.
     * @param listener - run control event listener to remove.
     */
    void removeListener(RunControlListener listener);

    /**
     * Service events listener interface.
     */
    interface RunControlListener {

        /**
         * Called when new contexts are created.
         * @param contexts - array of new context properties.
         */
        void contextAdded(RunControlContext contexts[]);

        /**
         * Called when a context properties changed.
         * @param contexts - array of new context properties.
         */
        void contextChanged(RunControlContext contexts[]);

        /**
         * Called when contexts are removed.
         * @param context_ids - array of removed context IDs.
         */
        void contextRemoved(String context_ids[]);

        /**
         * Called when a thread is suspended.
         * @param context - ID of a context that was suspended.
         * @param pc - program counter of the context, can be null.
         * @param reason - human readable description of suspend reason.
         * @param params - additional, target specific data about suspended context.
         */
        void contextSuspended(String context, String pc,
                String reason, Map<String,Object> params);

        /**
         * Called when a thread is resumed.
         * @param context - ID of a context that was resumed.
         */
        void contextResumed(String context);

        /**
         * Called when target simultaneously suspends multiple threads in a container
         * (process, core, etc.).
         * 
         * @param context - ID of a context responsible for the event. It can be container ID or
         * any one of container children, for example, it can be thread that hit "suspend all" breakpoint.
         * Client expected to move focus (selection) to this context.
         * @param pc - program counter of the context.
         * @param reason - human readable description of suspend reason.
         * @param params - additional target specific data about suspended context.
         * @param suspended_ids - full list of all contexts that were suspended. 
         */
        void containerSuspended(String context, String pc,
                String reason, Map<String,Object> params, String[] suspended_ids);

        /**
         * Called when target simultaneously resumes multiple threads in a container (process,
         * core, etc.).
         * 
         * @param context_ids - full list of all contexts that were resumed. 
         */
        void containerResumed(String[] context_ids);

        /**
         * Called when an exception is detected in a target thread.
         * @param context - ID of a context that caused an exception.
         * @param msg - human readable description of the exception.
         */
        void contextException(String context, String msg);
    }
}
