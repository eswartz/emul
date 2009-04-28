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


public interface IStackTrace extends IService {

    static final String NAME = "StackTrace";

    /**
     * Context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_PARENT_ID = "ParentID",
        PROP_PROCESS_ID = "ProcessID",
        PROP_NAME = "Name",
        PROP_FRAME_ADDRESS = "FP",
        PROP_RETURN_ADDRESS = "RP",
        PROP_INSTRUCTION_ADDRESS = "IP", 
        PROP_ARGUMENTS_COUNT = "ArgsCnt",
        PROP_ARGUMENTS_ADDRESS = "ArgsAddr";

    /**
     * Retrieve context info for given context IDs.
     * 
     * The command will fail if parent thread is not suspended.
     * Client can use Run Control service to suspend a thread.
     *   
     * @param id – array of context IDs. 
     * @param done - call back interface called when operation is completed.
     */
    IToken getContext(String[] id, DoneGetContext done);

    /**
     * Client call back interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when context data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context – array of context data or null if error.
         */
        void doneGetContext(IToken token, Exception error, StackTraceContext[] context);
    }

    /**
     * Retrieve stack trace context list.
     * Parent context usually corresponds to an execution thread.
     * Some targets have more then one stack. In such case children of a thread
     * are stacks, and stack frames are deeper in the hierarchy - they can be
     * retrieved with additional getChildren commands.
     * 
     * The command will fail if parent thread is not suspended.
     * Client can use Run Control service to suspend a thread.
     *  
     * @param parent_context_id – parent context ID.
     * @param done - call back interface called when operation is completed.
     */
    IToken getChildren(String parent_context_id, DoneGetChildren done);

    /**
     * Client call back interface for getChildren().
     */
    interface DoneGetChildren {
        /**
         * Called when context list retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context_ids – array of available context IDs.
         * Stack frames are ordered from stack bottom to top.
         */
        void doneGetChildren(IToken token, Exception error, String[] context_ids);
    }
    
    /**
     * StackTraceContext represents stack trace objects - stacks and stack frames. 
     */
    interface StackTraceContext {
        
        /**
         * Get Context ID.
         * @return context ID.
         */
        String getID();
        
        /**
         * Get parent context ID.
         * @return parent context ID.
         */
        String getParentID();
        
        /**
         * Get context name - if context represents a stack.
         * @return context name or null.
         */
        String getName();
        
        /**
         * Get memory address of this frame.
         * @return address or null if not a stack frame.
         */
        Number getFrameAddress();
        
        /**
         * Get program counter saved in this stack frame -
         * it is address of instruction to be executed when the function returns.
         * @return return address or null if not a stack frame.
         */
        Number getReturnAddress();
        
        /**
         * Get address of the next instruction to be executed in this stack frame.
         * For top frame it is same as PC register value.
         * For other frames it is same as return address of the next frame.
         * @return instruction address or null if not a stack frame.
         */
        Number getInstructionAddress();
        
        /**
         * Get number of function arguments for this frame.
         * @return function arguments count.
         */
        int getArgumentsCount();
        
        /**
         * Get address of function arguments area in memory.
         * @return function arguments address or null if not available.
         */
        Number getArgumentsAddress();
        
        /**
         * Get complete map of context properties.
         * @return map of context properties.
         */
        Map<String,Object> getProperties();
    }
}
