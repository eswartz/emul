/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 * IMemoryMap service provides information about executable modules (files) mapped (loaded) into target memory.
 */
public interface IMemoryMap extends IService {

    static final String NAME = "MemoryMap";

    /**
     * Memory region property names.
     */
    static final String
        /** Number, region address in memory */
        PROP_ADDRESS = "Addr",

        /** Number, region size */
        PROP_SIZE = "Size",

        /** Number, region offset in the file */
        PROP_OFFSET = "Offs",

        /** Number, region flags, see FLAG_* */
        PROP_FLAGS = "Flags",

        /** String, name of the file */
        PROP_FILE_NAME = "FileName";

    /**
     * Memory region flags.
     */
    static final int
        /** Read access is allowed */
        FLAG_READ = 1,

        /** Write access is allowed */
        FLAG_WRITE = 2,

        /** Instruction fetch access is allowed */
        FLAG_EXECUTE = 4;

    /**
     * Memory region interface.
     */
    interface MemoryRegion {

        /**
         * Get region properties. See PROP_* definitions for property names.
         * Properties are read only, clients should not try to modify them.
         * @return Map of region properties.
         */
        Map<String,Object> getProperties();

        /**
         * Get memory region address.
         * @return region address.
         */
        Number getAddress();

        /**
         * Get memory region size.
         * @return region size.
         */
        Number getSize();

        /**
         * Get memory region file offset.
         * @return file offset.
         */
        Number getOffset();

        /**
         * Get memory region flags.
         * @return region flags.
         */
        int getFlags();

        /**
         * Get memory region file name.
         * @return file name.
         */
        String getFileName();
    }

    /**
     * Retrieve memory map for given context ID.
     *
     * @param id – context ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken get(String id, DoneGet done);

    /**
     * Client call back interface for get().
     */
    interface DoneGet {
        /**
         * Called when memory map data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param map – memory map data.
         */
        void doneGet(IToken token, Exception error, MemoryRegion[] map);
    }

    /**
     * Add memory map event listener.
     * @param listener - memory map event listener to add.
     */
    void addListener(MemoryMapListener listener);

    /**
     * Remove memory map event listener.
     * @param listener - memory map event listener to remove.
     */
    void removeListener(MemoryMapListener listener);

    /**
     * Service events listener interface.
     */
    interface MemoryMapListener {

        /**
         * Called when context memory map changes.
         * @param context_id - context ID.
         */
        void changed(String context_id);
    }
}
