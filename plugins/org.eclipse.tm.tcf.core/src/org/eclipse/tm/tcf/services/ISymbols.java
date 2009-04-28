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

public interface ISymbols extends IService {

    /**
     * Service name.
     */
    static final String NAME = "Symbols";
    
    enum SymbolClass {
        unknown,                // unknown symbol class
        value,                  // constant value
        reference,              // variable data object
        function,               // function body
        type                    // a type
    }
    
    enum TypeClass {
        unknown,                // unknown type class
        cardinal,               // unsigned integer
        integer,                // signed integer
        real,                   // float, double
        pointer,                // pointer to anything.
        array,                  // array of anything.
        composite,              // struct, union, or class.
        enumeration,            // enumeration type.
        function                // function type.
    }
    
    /**
     * Symbol context interface.
     */
    interface Symbol {
        /**
         * Get symbol ID.
         * @return symbol ID.
         */
        String getID();
        
        /**
         * Get execution context ID (thread or process) that owns this instance of a symbol.
         * @return execution context ID.
         */
        String getExeContextID();
        
        /**
         * Get symbol name.
         * @return symbol name or null.
         */
        String getName();
        
        /**
         * Get symbol class.
         * @return symbol class.
         */
        SymbolClass getSymbolClass();
        
        /**
         * Get symbol type class.
         * @return type class.
         */
        TypeClass getTypeClass();
        
        /**
         * Get type ID.
         * If the symbol is a type and not a 'typedef', return same as getID(). 
         * @return type ID.
         */
        String getTypeID();
        
        /**
         * Get base type ID.
         * If this symbol is a
         *   pointer type - return pointed type;
         *   array type - return element type;
         *   function type - return function result type;
         *   class type - return base class;
         * otherwise return null.  
         * @return type ID.
         */
        String getBaseTypeID();
        
        /**
         * Get index type ID.
         * If this symbol is a
         *   array type - return array index type;
         * otherwise return null.  
         * @return type ID.
         */
        String getIndexTypeID();
        
        /**
         * Return value size of the symbol (or type).
         * @return size in bytes.
         */
        int getSize();
        
        /**
         * If symbol is an array type - return number of elements. 
         * @return number of elements.
         */
        int getLength();
        
        /**
         * Return offset from 'this' for member of class, struct or union.
         * @return offset in bytes.
         */
        int getOffset();
        
        /**
         * Return address of the symbol.
         * @return address or null.
         */
        Number getAddress();
        
        /**
         * If symbol is a constant object, return its value. 
         * @return symbol value as array of bytes.
         */
        byte[] getValue();

        /**
         * Get complete map of context properties.
         * @return map of context properties.
         */
        Map<String,Object> getProperties();
    }

    /**
     * Symbol context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_EXE_ID = "ExeID",
        PROP_NAME = "Name",
        PROP_SYMBOL_CLASS = "Class",
        PROP_TYPE_CLASS = "TypeClass",
        PROP_TYPE_ID = "TypeID",
        PROP_BASE_TYPE_ID = "BaseTypeID",
        PROP_INDEX_TYPE_ID = "IndexTypeID",
        PROP_SIZE = "Size",
        PROP_LENGTH = "Length",
        PROP_OFFSET = "Offset",
        PROP_ADDRESS = "Address",
        PROP_VALUE = "Value";

    /**
     * Retrieve symbol context info for given symbol ID.
     * @see Symbol
     *   
     * @param id – symbol context ID. 
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
        void doneGetContext(IToken token, Exception error, Symbol context);
    }

    /**
     * Retrieve children IDs for given parent ID.
     * Meaning of the operation depends on parent kind:
     * 1. struct, union, or class type - get fields; 
     * 2. enumeration type - get enumerators;
     * 
     * @param parent_context_id – parent symbol context ID.
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
}
