/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
 * Expressions service allows TCF client to perform expression evaluation on remote target.
 * The service can be used to retrieve or modify values of variables or any data structures in remote target memory.
 */
public interface IExpressions extends IService {

    /**
     * Service name.
     */
    static final String NAME = "Expressions";
    
    /**
     * Expression object represent an expression that can be evaluated by remote target.
     * It has a unique ID and contains all information necessary to compute a value.
     * The object data usually includes:
     *   1. process, thread or stack frame ID that should be used to resolve symbol names;
     *   2. a script that can compute a value, like "x.y + z"  
     */
    interface Expression {
        /**
         * Get context ID.
         * @return context ID.
         */
        String getID();
        
        /**
         * Get parent context ID.
         * @return parent context ID.
         */
        String getParentID();
                
        /**
         * Get expression script language ID.
         * @return language ID.
         */
        String getLanguage();
        
        /**
         * Return expression string - the script part of the context.
         * @return expression script string 
         */
        String getExpression();
        
        /**
         * Get size of expression value in bits.
         * Can be 0 if value size is even number of bytes, use getSize() in such case.
         * @return size in bits.
         */
        int getBits();
        
        /**
         * Get size in bytes. The size can include extra (unused) bits.
         * This is "static" or "declared" size - as determined by expression type.
         * @return size in bytes.
         */
        int getSize();
        
        /**
         * Get expression type ID. Symbols service can be used to get type properties.
         * This is "static" or "declared" type ID, actual type of a value can be different -
         * if expression language supports dynamic typing. 
         * @return type ID.
         */
        String getTypeID();
        
        /**
         * Check if the expression can be assigned a new value.
         * @return true if can assign.
         */
        boolean canAssign();
        
        /**
         * Get complete map of context properties.
         * @return map of context properties.
         */
        Map<String,Object> getProperties();
    }

    /**
     * Expression context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_PARENT_ID = "ParentID",
        PROP_LANGUAGE = "Language",
        PROP_EXPRESSION = "Expression",
        PROP_BITS = "Bits",
        PROP_SIZE = "Size",
        PROP_TYPE = "Type",
        PROP_CAN_ASSIGN = "CanAssign";

    /**
     * Value represents result of expression evaluation.
     * Note that same expression can be evaluated multiple times with different results.
     */
    interface Value {
        
        /**
         * Get value type class.
         * @see ISymbols.TypeClass
         * @return
         */
        ISymbols.TypeClass getTypeClass();
        
        /**
         * Get value type ID. Symbols service can be used to get type properties.
         * @return type ID.
         */
        String getTypeID();

        /**
         * Get execution context ID (thread or process) that owns type symbol for this value.
         * @return execution context ID.
         */
        String getExeContextID();

        /**
         * Check endianess of the values.
         * Big endian means decreasing numeric significance with increasing byte number. 
         * @return true if big endian.
         */
        boolean isBigEndian();
        
        /**
         * Get value as array of bytes.
         * @return value as array of bytes.
         */
        byte[] getValue();

        /**
         * Get complete map of value properties.
         * @return map of value properties.
         */
        Map<String,Object> getProperties();
    }

    /**
     * Expression value property names.
     */
    static final String
        VAL_CLASS = "Class",
        VAL_TYPE = "Type",
        VAL_EXE_ID = "ExeID",
        VAL_BIG_ENDIAN = "BigEndian";

    /**
     * Retrieve expression context info for given context ID.
     * @see Expression
     *   
     * @param id – context ID. 
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getContext(String id, DoneGetContext done);

    /**
     * Client call back interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when context data retrieval is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context properties.
         */
        void doneGetContext(IToken token, Exception error, Expression context);
    }

    /**
     * Retrieve children IDs for given parent ID.
     * Meaning of the operation depends on parent kind:
     * 1. expression with type of a struct, union, or class - fields; 
     * 2. expression with type of an enumeration - enumerators;
     * 3. expression with type of an array - array elements;
     * 4. stack frame - function arguments and local variables;
     * 5. thread - top stack frame function arguments and local variables;
     * 6. process - global variables;
     * 
     * Children list does *not* include IDs of expressions that were created by clients
     * using "create" command.
     * 
     * @param parent_context_id – parent context ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getChildren(String parent_context_id, DoneGetChildren done);

    /**
     * Client call back interface for getChildren().
     */
    interface DoneGetChildren {
        /**
         * Called when context list retrieval is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context_ids – array of available context IDs.
         */
        void doneGetChildren(IToken token, Exception error, String[] context_ids);
    }
    
    /**
     * Create an expression context.
     * The context should be disposed after use.
     * @param parent_id - a context ID that can be used to resolve symbol names.
     * @param language - language of expression script, null means default language
     * @param expression - expression script
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken create(String parent_id, String language, String expression, DoneCreate done);

    /**
     * Client call back interface for create().
     */
    interface DoneCreate {
        /**
         * Called when context create context command is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context properties.
         */
        void doneCreate(IToken token, Exception error, Expression context);
    }

    /**
     * Dispose an expression context that was created by create()
     * @param id - the expression context ID
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken dispose(String id, DoneDispose done);
    
    /**
     * Client call back interface for dispose().
     */
    interface DoneDispose {
        /**
         * Called when context dispose command is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         */
        void doneDispose(IToken token, Exception error);
    }

    /**
     * Evaluate value of an expression context.
     * @param id - the expression context ID
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken evaluate(String id, DoneEvaluate done);
    
    /**
     * Client call back interface for evaluate().
     */
    interface DoneEvaluate {
        /**
         * Called when context dispose command is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param value - expression evaluation result
         */
        void doneEvaluate(IToken token, Exception error, Value value);
    }
    
    /**
     * Assign a value to memory location determined by an expression.
     * @param id - expression ID.
     * @param value - value as an array of bytes.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken assign(String id, byte[] value, DoneAssign done);
    
    /**
     * Client call back interface for assign().
     */
    interface DoneAssign {
        /**
         * Called when assign command is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         */
        void doneAssign(IToken token, Exception error);
    }
    
    /**
     * Add expressions service event listener.
     * @param listener - event listener implementation.
     */
    void addListener(ExpressionsListener listener);

    /**
     * Remove expressions service event listener.
     * @param listener - event listener implementation.
     */
    void removeListener(ExpressionsListener listener);

    /**
     * Registers event listener is notified when registers context hierarchy
     * changes, and when a register is modified by the service commands. 
     */
    interface ExpressionsListener {

        /**
         * Called when expression value was changed and clients 
         * need to update themselves. Clients, at least, should invalidate
         * corresponding cached expression data.
         * Not every change is notified - it is not possible,
         * only those, which are not caused by normal execution of the debuggee.
         * At least, changes caused by "assign" command should be notified.
         * @param id - expression context ID.
         */
        void valueChanged(String id);
    }
}
