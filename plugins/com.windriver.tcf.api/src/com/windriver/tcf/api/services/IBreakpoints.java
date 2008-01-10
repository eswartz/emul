/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.services;

import java.util.Map;

import com.windriver.tcf.api.protocol.IService;
import com.windriver.tcf.api.protocol.IToken;

/**
 * Breakpoint is represented by unique identifier and set of properties.
 * Breakpoint identifier (String id) needs to be unique across all hosts and targets.
 * 
 * Breakpoint properties (Map<String,Object>) is extendable collection of named attributes,
 * which define breakpoint location and behavior. This module defines some common
 * attribute names (see PROP_*), host tools and target agents may support additional attributes.
 * 
 * For each breakpoint a target agent maintains another extendable collection of named attributes:
 * breakpoint status (Map<String,Object>, see STATUS_*). While breakpoint properties are
 * persistent and represent user input, breakpoint status reflects dynamic target agent reports
 * about breakpoint current state, like actual addresses where breakpoint is planted or planting errors.
 */
public interface IBreakpoints extends IService {

    /**
     * Service name.
     */
    static final String NAME = "Breakpoints";

    /**
     * Breakpoint property names.
     */
    static final String   
        PROP_ID = "ID",                 // String
        PROP_ENABLED = "Enabled",       // Boolean
        PROP_ADDRESS = "Address",       // String
        PROP_CONDITION = "Condition",   // String
        PROP_FILE = "File",             // String
        PROP_LINE = "Line",             // Number
        PROP_COLUMN = "Column";         // Number

    /**
     * Breakpoint status field names.
     */
    static final String
        STATUS_PLANTED = "Planted",     // Array of addresses
        STATUS_ERROR = "Error",         // String
        STATUS_FILE = "File",           // String
        STATUS_LINE = "Line",           // Number
        STATUS_COLUMN = "Column";       // Number
    
    /**
     * Call back interface for breakpoint service commands.
     */
    interface DoneCommand {
        void doneCommand(IToken token, Exception error);
    }

    /**
     * Download breakpoints data to target agent.
     * The command is intended to be used only to initialize target breakpoints table 
     * when communication channel is open. After that, host should 
     * notify target about (incremental) changes in breakpoint data by sending
     * add, change and remove commands.
     * 
     * @param properties - array of breakpoints.
     * @param done - command result call back object.
     */
    IToken set(Map<String,Object>[] properties, DoneCommand done);

    /**
     * Called when breakpoint is added into breakpoints table.
     * @param properties - breakpoint properties.
     * @param done - command result call back object.
     */
    IToken add(Map<String,Object> properties, DoneCommand done);

    /**
     * Called when breakpoint properties are changed.
     * @param properties - breakpoint properties.
     * @param done - command result call back object.
     */
    IToken change(Map<String,Object> properties, DoneCommand done);

    /**
     * Tell target to change (only) PROP_ENABLED breakpoint property 'true'.
     * @param ids - array of enabled breakpoint identifiers.
     * @param done - command result call back object.
     */
    IToken enable(String[] ids, DoneCommand done);

    /**
     * Tell target to change (only) PROP_ENABLED breakpoint property to 'false'.
     * @param ids - array of disabled breakpoint identifiers.
     * @param done - command result call back object.
     */
    IToken disable(String[] ids, DoneCommand done);

    /**
     * Tell target to remove breakpoint.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     */
    IToken remove(String[] ids, DoneCommand done);
    
    /**
     * Upload IDs of breakpoints known to target agent.
     * @param done - command result call back object.
     */
    IToken getIDs(DoneGetIDs done);

    interface DoneGetIDs {
        void doneGetIDs(IToken token, Exception error, String[] ids);
    }

    /**
     * Upload properties of given breakpoint from target agent breakpoint table.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     */
    IToken getProperties(String id, DoneGetProperties done);

    interface DoneGetProperties {
        void doneGetProperties(IToken token, Exception error, Map<String,Object> properties);
    }

    /**
     * Upload status of given breakpoint from target agent.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     */
    IToken getStatus(String id, DoneGetStatus done);

    interface DoneGetStatus {
        void doneGetStatus(IToken token, Exception error, Map<String,Object> status);
    }

    /**
     * Breakpoints service events listener.
     */
    interface BreakpointsListener {

        /**
         * Called when breakpoint status changes.
         * @param id - unique breakpoint identifier.
         * @param status - breakpoint status.
         */
        void breakpointStatusChanged(String id, Map<String,Object> status);
    }

    void addListener(BreakpointsListener listener);

    void removeListener(BreakpointsListener listener);
}
