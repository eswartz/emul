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

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;


/**
 * IRegisters service provides access to target CPU register values and properties.
 */
public interface IRegisters extends IService {

    static final String NAME = "Registers";

    /**
     * Context property names.
     */
    static final String
        PROP_ID = "ID",                         /** String, ID of the context */
        PROP_PARENT_ID = "ParentID",            /** String, ID of a parent context */
        PROP_PROCESS_ID = "ProcessID",          /** String, process ID */
        PROP_NAME = "Name",                     /** String, context name */
        PROP_DESCRIPTION = "Description",       /** String, context description */
        PROP_SIZE = "Size",                     /** Number, context size in bytes. Byte arrays in get/set commands should be same size */
        PROP_READBLE = "Readable",              /** Boolean, true if context value can be read */
        PROP_READ_ONCE = "ReadOnce",            /** Boolean, true if reading the context (register) destroys its current value */
        PROP_WRITEABLE = "Writeable",           /** Boolean, true if context value can be written */
        PROP_WRITE_ONCE = "WriteOnce",          /** Boolean, true if register value can not be overwritten - every write counts */
        PROP_SIDE_EFFECTS = "SideEffects",      /** Boolean, true if writing the context can change values of other registers */
        PROP_VOLATILE = "Volatile",             /** Boolean, true if the register value can change even when target is stopped */
        PROP_FLOAT = "Float",                   /** Boolean, true if the register value is a floating-point value */
        PROP_BIG_ENDIAN = "BigEndian",          /** Boolean, true if big endian */
        PROP_LEFT_TO_RIGHT = "LeftToRight",     /** Boolean, true if the lowest numbered bit should be shown to user as the left-most bit */
        PROP_FIST_BIT = "FirstBit",             /** Number, bit numbering base (0 or 1) to use when showing bits to user */ 
        PROP_BITS = "Bits",                     /** Number, if context is a bit field, contains the field bit numbers in the parent context */
        PROP_VALUES = "Values",                 /** Array of Map, predefined names (mnemonics) for some of context values */
        PROP_MEMORY_ADDRESS = "MemoryAddress",  /** Number, the address of a memory mapped register */
        PROP_MEMORY_CONTEXT = "MemoryContext",  /** String, the context ID of a memory context in which a memory mapped register is located */
        PROP_CAN_SEARCH = "CanSearch",          /** Array of String, a list of attribute names which can be searched for starting on this context */
        PROP_ROLE = "Role";                     /** String, the role the register plays in a program execution */
    
    /**
     * Values of context property "Role".
     */
    static final String
        ROLE_PC = "PC",                         /** Program counter. Defines instruction to execute next */
        ROLE_SP = "SP",                         /** Register defining the current stack pointer location */
        ROLE_FP = "FP",                         /** Register defining the current frame pointer location */
        ROLE_RET = "RET",                       /** Register used to store the return address for calls */
        ROLE_CORE = "CORE";                     /** Indicates register or register groups which belong to the core state */
    
    /**
     * Search filter properties.
     */
    static final String
        SEARCH_NAME = "Name",                   /** The name of the property this filter applies too */
        SEARCH_EQUAL_VALUE = "EqualValue";      /** The value which is searched for */
        
    /**
     * Retrieve context info for given context ID.
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
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context data.
         */
        void doneGetContext(IToken token, Exception error, RegistersContext context);
    }

    /**
     * Retrieve contexts available for registers commands.
     * A context corresponds to an execution thread, stack frame, registers group, etc.
     * A context can belong to a parent context. Contexts hierarchy can be simple
     * plain list or it can form a tree. It is up to target agent developers to choose
     * layout that is most descriptive for a given target. Context IDs are valid across
     * all services. In other words, all services access same hierarchy of contexts,
     * with same IDs, however, each service accesses its own subset of context's
     * attributes and functionality, which is relevant to that service.
     *  
     * @param parent_context_id – parent context ID. Can be null –
     * to retrieve top level of the hierarchy, or one of context IDs retrieved
     * by previous getChildren commands.
     * @param done - call back interface called when operation is completed.
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
     * RegistersContext objects represent register groups, registers and bit fields. 
     */
    interface RegistersContext {
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
         * Get process ID, if applicable.
         * @return process ID.
         */
        String getProcessID();
        
        /**
         * Get context (register, register group, bit field) name.
         * @return context name.
         */
        String getName();
        
        /**
         * Get context description.
         * @return context description.
         */
        String getDescription();
        
        /**
         * Get context size in bytes.
         * Byte arrays in get()/set() methods should be same size.
         * Hardware register can be smaller then this size, for example in case
         * when register size is not an even number of bytes. In such case implementation
         * should add/remove padding that consist of necessary number of zero bits.
         * @return context size in bytes.
         */
        int getSize();
        
        /**
         * Check if context value can be read.
         * @return true if can read value of the context.
         */
        boolean isReadable();
        
        /**
         * Check if reading the context (register) destroys its current value -
         * it can be read only once.
         * @return true if read-once register.
         */
        boolean isReadOnce();
        
        /**
         * Check if context value can be written.
         * @return true if can write value of the context.
         */
        boolean isWriteable();
        
        /**
         * Check if register value can not be overwritten - every write counts.
         * @return true if write-once register.
         */
        boolean isWriteOnce();
        
        /**
         * Check if writing the context can change values of other registers. 
         * @return true if has side effects.
         */
        boolean hasSideEffects();
        
        /**
         * Check if the register value can change even when target is stopped.
         * @return true if the register value can change at any time.
         */
        boolean isVolatile();
        
        /**
         * Check if the register value is a floating-point value.
         * @return true if a floating-point register.
         */
        boolean isFloat();
        
        /**
         * Check endianess of the context.
         * Big endian means decreasing numeric significance with increasing bit number. 
         * The endianess is used to encode and decode values of get, getm, set and setm commands.
         * @return true if big endian.
         */
        boolean isBigEndian();
        
        /**
         * Check if the lowest numbered bit (i.e. bit #0 or bit #1 depending on
         * getFirstBitNumber() value) should be shown to user as the left-most bit or
         * the right-most bit.
         * @return true if the first bit is left-most bit.
         */
        boolean isLeftToRight();
        
        /**
         * If the context has bit field children, bit positions of the fields
         * can be zero-based or 1-based.
         * @return first bit position - 0 or 1.
         */
        int getFirstBitNumber();
        
        /**
         * If context is a bit field, get the field bit numbers in parent context. 
         * @return array of bit numbers.
         */
        int[] getBitNumbers();
        
        /**
         * A context can have predefined names (mnemonics) for some its values.
         * This method returns a list of such named values.
         * @return array of named values or null.
         */
        NamedValue[] getNamedValues();
        
        /**
         * Get the address of a memory mapped register.
         * @return address.
         */
        Number getMemoryAddress();
        
        /**
         * Get the context ID of a memory context in which a memory mapped register is located. 
         * @return memory context ID.
         */
        String getMemoryContext();
        
        /**
         * Get a list of property names which can be searched for starting on this context
         * @return collection of property names.
         */
        Collection<String> canSearch();
        
        /**
         * Get the role the register plays in a program execution.
         * @return role name.
         */
        String getRole();

        /**
         * Get complete map of context properties.
         * @return map of all available context properties.
         */
        Map<String,Object> getProperties();
        
        /**
         * Read value of the context.
         * @param done - call back object.
         * @return - pending command handle.
         */
        IToken get(DoneGet done);
        
        /**
         * Set value of the context.
         * @param value - value to write into the context.
         * @param done - call back object.
         * @return - pending command handle.
         */
        IToken set(byte[] value, DoneSet done);
        
        /**
         * Search register contexts that passes given search filter.
         * Search is only supported for properties listed in the "CanSearch" property.
         * @param filter - properties bag that defines search filter.
         * @param done - call back object.
         * @return - pending command handle.
         */
        IToken search(Map<String,Object> filter, DoneSearch done);
    }
    
    /**
     * A register context can have predefined names (mnemonics) for some its values.
     * NamedValue objects represent such values. 
     */
    interface NamedValue {
        /**
         * Get value associated with the name.
         * @return the value as an array of bytes.
         */
        byte[] getValue();
        
        /**
         * Get name (mnemonic) of the value.
         * @return value name.
         */
        String getName();
        
        /**
         * Get human readable description of the value.
         * @return value description.
         */
        String getDescription();
    }
    
    /**
     * Read values of multiple locations in registers.
     * @param locs - array of data locations.
     * @param done - call back object.
     * @return - pending command handle.
     */
    IToken getm(Location[] locs, DoneGet done);
    
    /**
     * Set values of multiple locations in registers.
     * @param locs - array of data locations.
     * @param value - value to write into the context.
     * @param done - call back object.
     * @return - pending command handle.
     */
    IToken setm(Location[] locs, byte[] value, DoneSet done);

    /**
     * Class Location represents value location in register context
     */
    final class Location {
        /** Register context ID */
        public final String id; 

        /** offset in the context, in bytes */
        public final int offs;

        /** value size in byte */
        public final int size;

        public Location(String id, int offs, int size) {
            this.id = id;
            this.offs = offs;
            this.size = size;
        }
    }

    /**
     * 'get' command call back interface.
     */
    interface DoneGet {
        /**
         * Called when value retrieval is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param value – context value as array of bytes.
         */
        void doneGet(IToken token, Exception error, byte[] value);
    }
    
    /**
     * 'set' command call back interface.
     */
    interface DoneSet {
        /**
         * Called when value setting is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         */
        void doneSet(IToken token, Exception error);
    }
    
    /**
     * 'search' command call back interface.
     */
    interface DoneSearch {
        /**
         * Called when context search is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param paths - array of paths to each context with properties matching the filter
         */
        void doneSearch(IToken token, Exception error, String[][] paths);
    }

    /**
     * Add registers service event listener.
     * @param listener - event listener implementation.
     */
    void addListener(RegistersListener listener);

    /**
     * Remove registers service event listener.
     * @param listener - event listener implementation.
     */
    void removeListener(RegistersListener listener);

    /**
     * Registers event listener is notified when registers context hierarchy
     * changes, and when a register is modified by the service commands. 
     */
    interface RegistersListener {

        /**
         * Called when register context properties changed.
         * Most targets have static set of registers and register properties.
         * Such targets never generate this event. However, some targets,
         * for example, JTAG probes, allow user to modify register definitions.
         * Clients should flush all cached register context data. 
         */
        void contextChanged();

        /**
         * Called when register content was changed and clients 
         * need to update themselves. Clients, at least, should invalidate
         * corresponding cached registers data.
         * Not every change is notified - it is not possible,
         * only those, which are not caused by normal execution of the debuggee.
         * At least, changes caused by "set" command should be notified.
         * @param id - register context ID.
         */
        void registerChanged(String id);
    }
}
