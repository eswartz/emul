/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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
         * Get symbol owner ID.
         * The owner can a thread or memory space (process).
         * Certain changes in owner state can invalidate cached symbol properties,
         * see getUpdatePolicy() and UPDATE_*.
         */
        String getOwnerID();

        /**
         * Get symbol properties update policy ID.
         * Symbol properties can change during program execution.
         * If a client wants to cache symbols, it should invalidate cached data
         * according to update policies of cached symbols.
         * @return symbol update policy ID, see UPDATE_*
         */
        int getUpdatePolicy();

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
         * If symbol is an array type - return array index lower bound.
         * @return lower bound.
         */
        Number getLowerBound();

        /**
         * If symbol is an array type - return array index upper bound.
         * @return upper bound.
         */
        Number getUpperBound();

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
         * Get symbol values endianness.
         * @return true if symbol is big-endian.
         */
        boolean isBigEndian();

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
        PROP_OWNER_ID = "OwnerID",
        PROP_UPDATE_POLICY = "UpdatePolicy",
        PROP_NAME = "Name",
        PROP_SYMBOL_CLASS = "Class",
        PROP_TYPE_CLASS = "TypeClass",
        PROP_TYPE_ID = "TypeID",
        PROP_BASE_TYPE_ID = "BaseTypeID",
        PROP_INDEX_TYPE_ID = "IndexTypeID",
        PROP_SIZE = "Size",
        PROP_LENGTH = "Length",
        PROP_LOWER_BOUND = "LowerBound",
        PROP_UPPER_BOUND = "UpperBound",
        PROP_OFFSET = "Offset",
        PROP_ADDRESS = "Address",
        PROP_VALUE = "Value",
        PROP_BIG_ENDIAN = "BigEndian";

    /**
     * Symbol context properties update policies.
     */
    static final int
        /**
         * Update policy "Memory Map": symbol properties become invalid when
         * memory map changes - when modules are loaded or unloaded.
         * Symbol OwnerID indicates memory space (process) that is invalidation events source.
         * Most static variables and types have this update policy.
         */
        UPDATE_ON_MEMORY_MAP_CHANGES = 0,

        /**
         * Update policy "Execution State": symbol properties become invalid when
         * execution state changes - a thread is suspended, resumed or exited.
         * Symbol OwnerID indicates executable context (thread) that is invalidation events source.
         * Most stack (auto) variables have this update policy.
         */
        UPDATE_ON_EXE_STATE_CHANGES = 1;

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

    /**
     * Search symbol with given name in given context.
     * The context can be memory space, process, thread or stack frame.
     *
     * @param context_id – a search scope.
     * @param name – symbol name.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken find(String context_id, String name, DoneFind done);

    /**
     * Client call back interface for find().
     */
    interface DoneFind {
        /**
         * Called when symbol search is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param symbol_id - symbol ID.
         */
        void doneFind(IToken token, Exception error, String symbol_id);
    }

    /**
     * List all symbols in given context.
     * The context can be a stack frame.
     *
     * @param context_id – a scope.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken list(String context_id, DoneList done);

    /**
     * Client call back interface for list().
     */
    interface DoneList {
        /**
         * Called when symbol list retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param symbol_ids - array of symbol IDs.
         */
        void doneList(IToken token, Exception error, String[] symbol_ids);
    }

    /***********************************************************************************************/

    /**
     * Command codes that used to calculate frame pointer and register values during stack tracing.
     */
    static final int
        /** Load a number to the evaluation stack. Command argument is the number. */
        CMD_NUMBER      = 1,

        /** Load a register value to the evaluation stack. Command argument is the register ID. */
        CMD_REGISTER    = 2,

        /** Load frame address to the evaluation stack. */
        CMD_FP          = 3,

        /** Read memory at address on the top of the evaluation stack. Command arguments are
         *  the value size (Number) and endianness (Boolean, false - little-endian, true - big-endian). */
        CMD_DEREF       = 4,

        /** Add two values on top of the evaluation stack */
        CMD_ADD         = 5;

    /**
     * Retrieve stack tracing commands for given instruction address in a context memory.
     * @param context_id - exacutable context ID.
     * @param address - instruction address.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken findFrameInfo(String context_id, Number address, DoneFindFrameInfo done);

    /**
     * Client call back interface for findFrameInfo().
     */
    interface DoneFindFrameInfo {
        /**
         * Called when stack tracing information retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param address - start of instruction address range
         * @param size - size of instruction address range
         * @param fp_cmds - commands to calculate stack frame pointer
         * @param reg_cmds - map register IDs -> commands to calculate register values
         */
        void doneFindFrameInfo(IToken token, Exception error,
                Number address, Number size,
                Object[] fp_cmds, Map<String,Object[]> reg_cmds);
    }
}
