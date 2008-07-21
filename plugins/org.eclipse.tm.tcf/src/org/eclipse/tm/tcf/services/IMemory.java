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
 * IMemory service provides basic operations to read/write memory on a target.
 */
public interface IMemory extends IService {

    static final String NAME = "Memory";

    /**
     * Context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_PARENT_ID = "ParentID",
        PROP_PROCESS_ID = "ProcessID",
        PROP_BIG_ENDIAN = "BigEndian",
        PROP_ADDRESS_SIZE = "AddressSize";
    
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
         * Retrieve context ID.
         * Same as (String)getProperties().get(“ID”)
         */
        String getID();

        /** 
         * Retrieve parent context ID.
         * Same as (String)getProperties().get(“ParentID”)
         */
        String getParentID();
        
        /**
         * Retrieves process ID, if applicable.
         * @return process ID.
         */
        int getProcessID();
        
        /**
         * Retrieve memory endianess.
         * @return true if memory id big-endian.
         */
        boolean isBigEndian();
        
        /**
         * Retrieve memory address size.
         * @return number of bytes used to store memory address value.
         */
        int getAddressSize();

        /** 
         * Retrieve context properties.
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
