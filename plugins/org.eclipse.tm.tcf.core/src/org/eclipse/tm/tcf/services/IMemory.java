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
 * IMemory service provides basic operations to read/write memory on a target.
 */
public interface IMemory extends IService {

    static final String NAME = "Memory";

    /**
     * Context property names.
     */
    static final String
        PROP_ID = "ID",                         /** String, ID of the context, same as getContext command argument */
        PROP_PARENT_ID = "ParentID",            /** String, ID of a parent context */
        PROP_PROCESS_ID = "ProcessID",          /** String, process ID, see Processes service */
        PROP_BIG_ENDIAN = "BigEndian",          /** Boolean, true if memory is big-endian */
        PROP_ADDRESS_SIZE = "AddressSize",      /** Number, size of memory address in bytes */
        PROP_NAME = "Name",                     /** String, name of the context, can be used for UI purposes */
        PROP_START_BOUND = "StartBound",        /** Number, lowest address (inclusive) which is valid for the context */
        PROP_END_BOUND = "EndBound",            /** Number, highest address (inclusive) which is valid for the context */
        PROP_ACCESS_TYPES = "AccessTypes";      /** Array of String, the access types allowed for this context */
    
    /**
     * Values of "AccessTypes".
     * Target system can support multiple different memory access types, like instruction and data access.
     * Different access types can use different logic for address translation and memory mapping, so they can
     * end up accessing different data bits, even if address is the same.
     * Each distinct access type should be represented by separate memory context.
     * A memory context can represent multiple access types if they are equivalent - all access same memory bits.
     * Same data bits can be exposed through multiple memory contexts.
     */
    static final String
        ACCESS_INSTRUCTION = "instruction",     /** Context represent instructions fetch access */
        ACCESS_DATA = "data",                   /** Context represents data access */
        ACCESS_IO = "io",                       /** Context represents IO peripherals */
        ACCESS_USER = "user",                   /** Context represents a user (e.g. application running in Linux) view to memory */
        ACCESS_SUPERVISOR = "supervisor",       /** Context represents a supervisor (e.g. Linux kernel) view to memory */
        ACCESS_HYPERVISOR = "hypervisor",       /** Context represents a hypervisor view to memory */
        ACCESS_VIRTUAL = "virtual",             /** Context uses virtual addresses */
        ACCESS_PHYSICAL = "physical",           /** Context uses physical addresses */
        ACCESS_CACHE = "cache",                 /** Context is a cache */
        ACCESS_TLB = "tlb";                     /** Context is a TLB memory */
    
    /**
     * Retrieve context info for given context ID.
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
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context data.
         */
        void doneGetContext(IToken token, Exception error, MemoryContext context);
    }

    /**
     * Retrieve contexts available for memory commands.
     * A context corresponds to an execution thread, process, address space, etc.
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
     * @return - pending command handle.
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
         */
        void doneGetChildren(IToken token, Exception error, String[] context_ids);
    }

    /**
     * Memory access mode:
     * Carry on when some of the memory cannot be accessed and
     * return MemoryError at the end if any of the bytes
     * were not processed correctly.
     */
    final static int MODE_CONTINUEONERROR = 0x1;

    /**
     * Memory access mode:
     * Verify result of memory operations (by reading and comparing).
     */
    final static int MODE_VERIFY = 0x2;

    interface MemoryContext {

        /** 
         * Get context ID.
         * @return context ID.
         */
        String getID();

        /** 
         * Get parent context ID.
         * @return parent ID.
         */
        String getParentID();
        
        /**
         * Get process ID, if applicable.
         * @return process ID.
         */
        String getProcessID();
        
        /**
         * Get memory endianess.
         * @return true if memory id big-endian.
         */
        boolean isBigEndian();
        
        /**
         * Get memory address size.
         * @return number of bytes used to store memory address value.
         */
        int getAddressSize();
        
        /**
         * Get memory context name.
         * The name can be used for UI purposes.
         * @return context name.
         */
        String getName();
        
        /**
         * Get lowest address (inclusive) which is valid for the context.
         * @return lowest address.
         */
        Number getStartBound();
        
        /**
         * Get highest address (inclusive) which is valid for the context.
         * @return highest address.
         */
        Number getEndBound();
        
        /**
         * Get the access types allowed for this context.
         * @return collection of access type names.
         */
        Collection<String> getAccessTypes();

        /** 
         * Get context properties.
         * @return all available context properties.
         */
        Map<String,Object> getProperties();

        /**
         * Set target memory.
         * If 'word_size' is 0 it means client does not care about word size.
         */
        IToken set(Number addr, int word_size, byte[] buf,
                int offs, int size, int mode, DoneMemory done);

        /**
         * Read target memory.
         */
        IToken get(Number addr, int word_size, byte[] buf,
                int offs, int size, int mode, DoneMemory done);

        /**
         * Fill target memory with given pattern.
         * 'size' is number of bytes to fill.
         */
        IToken fill(Number addr, int word_size, byte[] value,
                int size, int mode, DoneMemory done);
    }

    /**
     * Client call back interface for set(), get() and fill() commands.
     */
    interface DoneMemory {
        public void doneMemory(IToken token, MemoryError error);
    }

    class MemoryError extends Exception {

        private static final long serialVersionUID = 1L;
        
        public MemoryError(String msg) {
            super(msg);
        }
    }

    /**
     * ErrorOffset interface can be implemented by MemoryError object,
     * which is returned by get, set and fill commands.
     *
     * get/set/fill () returns this exception when reading failed
     * for some but not all bytes, and MODE_CONTINUEONERROR
     * has been set in mode. (For example, when only part of the request
     * translates to valid memory addresses.)
     * Exception.getMessage can be used for generalized message of the
     * possible reasons of partial memory operation.
     */
    interface ErrorOffset {

        // Error may have per byte information
        final static int 
            BYTE_VALID        = 0x00,
            BYTE_UNKNOWN      = 0x01, // e.g. out of range
            BYTE_INVALID      = 0x02,
            BYTE_CANNOT_READ  = 0x04,
            BYTE_CANNOT_WRITE = 0x08;

        int getStatus(int offset);

        /**
         * Returns the detail message string about the
         * byte associated with specified location.
         * @return  the detail error message string.
         */
        String getMessage(int offset);

    }

    /**
     * Add memory service event listener.
     * @param listener - event listener implementation.
     */
    void addListener(MemoryListener listener);

    /**
     * Remove memory service event listener.
     * @param listener - event listener implementation.
     */
    void removeListener(MemoryListener listener);

    /**
     * Memory event listener is notified when memory context hierarchy
     * changes, and when memory is modified by memory service commands. 
     */
    interface MemoryListener {

        /**
         * Called when a new memory access context(s) is created.
         */
        void contextAdded(MemoryContext[] contexts);

        /**
         * Called when a memory access context(s) properties changed.
         */
        void contextChanged(MemoryContext[] contexts);

        /**
         * Called when memory access context(s) is removed.
         */
        void contextRemoved(String[] context_ids);

        /**
         * Called when target memory content was changed and clients 
         * need to update themselves. Clients, at least, should invalidate
         * corresponding cached memory data.
         * Not every change is notified - it is not possible,
         * only those, which are not caused by normal execution of the debuggee.
         * ‘addr’ and ‘size’ can be null if unknown.
         */
        void memoryChanged(String context_id, Number[] addr, long[] size);
    }
}
