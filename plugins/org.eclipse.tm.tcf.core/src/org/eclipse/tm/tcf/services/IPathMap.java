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
 * IPathMap service manages file path translation across systems.
 */
public interface IPathMap extends IService {

    static final String NAME = "PathMap";

    /**
     * Path mapping rule property names.
     */
    static final String
        /** String, rule ID */
        PROP_ID = "ID",

        /** String, source, or compile-time file path */
        PROP_SOURCE = "Source",

        /** String, destination, or run-time file path */
        PROP_DESTINATION = "Destination",

        /** String, */
        PROP_HOST = "Host",

        /** String, file access protocol, see PROTOCOL_*, default is regular file */
        PROP_PROTOCOL = "Protocol";

    /**
     * PROP_PROTOCOL values.
     */
    static final String
        /** Regular file access using system calls */
        PROTOCOL_FILE = "file",

        /** File should be accessed using File System service on host */
        PROTOCOL_HOST = "host",

        /** File should be accessed using File System service on target */
        PROTOCOL_TARGET = "target";

    /**
     * PathMapRule interface represents a single file path mapping rule.
     */
    interface PathMapRule {

        /**
         * Get rule properties. See PROP_* definitions for property names.
         * Properties are read only, clients should not try to modify them.
         * @return Map of rule properties.
         */
        Map<String,Object> getProperties();

        /**
         * Get rule unique ID.
         * Same as getProperties().get(PROP_ID)
         * @return rule ID.
         */
        String getID();

        /**
         * Get compile-time file path.
         * Same as getProperties().get(PROP_SOURCE)
         * @return compile-time file path.
         */
        String getSource();

        /**
         * Get run-time file path.
         * Same as getProperties().get(PROP_DESTINATION)
         * @return run-time file path.
         */
        String getDestination();

        /**
         * Get host name of this rule.
         * Same as getProperties().get(PROP_HOST)
         * @return host name.
         */
        String getHost();

        /**
         * Get file access protocol name.
         * Same as getProperties().get(PROP_PROTOCOL)
         * @return protocol name.
         */
        String getProtocol();
    }

    /**
     * Retrieve file path mapping rules.
     *
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken get(DoneGet done);

    /**
     * Client call back interface for get().
     */
    interface DoneGet {
        /**
         * Called when file path mapping retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param map – file path mapping data.
         */
        void doneGet(IToken token, Exception error, PathMapRule[] map);
    }

    /**
     * Set file path mapping rules.
     *
     * @param map – file path mapping rules.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken set(PathMapRule[] map, DoneSet done);

    /**
     * Client call back interface for set().
     */
    interface DoneSet {
        /**
         * Called when file path mapping transmission is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param map – memory map data.
         */
        void doneSet(IToken token, Exception error);
    }
}
