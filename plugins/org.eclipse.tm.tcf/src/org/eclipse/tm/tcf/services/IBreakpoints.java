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
 * Breakpoint is represented by unique identifier and set of properties.
 * Breakpoint identifier (String id) needs to be unique across all hosts and targets.
 * 
 * Breakpoint properties (Map<String,Object>) is extendible collection of named attributes,
 * which define breakpoint location and behavior. This module defines some common
 * attribute names (see PROP_*), host tools and target agents may support additional attributes.
 * 
 * For each breakpoint a target agent maintains another extendible collection of named attributes:
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
        PROP_ID = "ID",                           // String
        PROP_ENABLED = "Enabled",                 // Boolean
        PROP_TYPE = "BreakpointType",             // String
        PROP_CONTEXTNAMES = "ContextNames",       // Array
        PROP_CONTEXTIDS = "ContextIds",           // Array
        PROP_EXECUTABLEPATHS = "ExecPaths",       // Array
        PROP_LOCATION = "Location",               // String
        PROP_SIZE = "Size",                       // Number
        PROP_ACCESSMODE = "AccessMode",           // Number
        PROP_FILE = "File",                       // String
        PROP_LINE = "Line",                       // Number
        PROP_COLUMN = "Column",                   // Number
        PROP_PATTERN = "MaskValue",               // Number
        PROP_MASK = "Mask",                       // Number
        PROP_STOP_GROUP = "StopGroup",            // Array
        PROP_IGNORECOUNT = "IgnoreCount",         // Number
        PROP_TIME = "Time",                       // Number
        PROP_SCALE = "TimeScale",                 // String
        PROP_UNITS = "TimeUnits",                 // String
        PROP_CONDITION = "Condition",             // String
        PROP_TEMPORARY = "Temporary";             // Boolean

    /**
     * BreakpointType values 
     */
    static final String
        TYPE_RELATIVE = "Software",
        TYPE_ABSOLUTE = "Hardware",
        TYPE_AUTO = "Auto";

    /** 
     * AccessMode values 
     */ 
    static final int 
        ACCESSMODE_READ    = 0x01,
        ACCESSMODE_WRITE   = 0x02, 
        ACCESSMODE_EXECUTE = 0x04,
        ACCESSMODE_CHANGE  = 0x08;

    /**
     * TimeScale values 
     */
    static final String 
        TIMESCALE_RELATIVE = "Relative",
        TIMESCALE_ABSOLUTE = "Absolute";
    
    /**
     * TimeUnits values 
     */
    static final String
        TIMEUNIT_NSECS = "Nanoseconds",
        TIMEUNIT_CYCLE_COUNT = "CycleCount",
        TIMEUNIT_INSTRUCTION_COUNT = "InstructionCount";

    /**
     * Breakpoint status field names.
     */
    static final String
        STATUS_INSTANCES = "Instances", // Array of Map<String,Object>
        STATUS_ERROR = "Error",         // String
        STATUS_FILE = "File",           // String
        STATUS_LINE = "Line",           // Number
        STATUS_COLUMN = "Column";       // Number
    
    /**
     * Breakpoint instance field names.
     */
    static final String
        INSTANCE_ERROR = "Error",       // String
        INSTANCE_CONTEXT = "LocationContext", // String
        INSTANCE_ADDRESS = "Address";   // Number
    
    /**
     * Breakpoint service capabilities.
     */
    static final String
        CAPABILITY_CONTEXT_ID = "ID",                   // String
        CAPABILITY_HAS_CHILDREN = "HasChildren",        // Boolean
        CAPABILITY_ADDRESS = "Location",                // Boolean
        CAPABILITY_CONDITION = "Condition",             // Boolean
        CAPABILITY_FILE_LINE = "FileLine";              // Boolean

    /**
     * Call back interface for breakpoint service commands.
     */
    interface DoneCommand {
        /**
         * Called when command is done.
         * @param token - command handle.
         * @param error - error object or null.
         */
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
     * @return - pending command handle.
     */
    IToken set(Map<String,Object>[] properties, DoneCommand done);

    /**
     * Called when breakpoint is added into breakpoints table.
     * @param properties - breakpoint properties.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken add(Map<String,Object> properties, DoneCommand done);

    /**
     * Called when breakpoint properties are changed.
     * @param properties - breakpoint properties.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken change(Map<String,Object> properties, DoneCommand done);

    /**
     * Tell target to change (only) PROP_ENABLED breakpoint property to 'true'.
     * @param ids - array of enabled breakpoint identifiers.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken enable(String[] ids, DoneCommand done);

    /**
     * Tell target to change (only) PROP_ENABLED breakpoint property to 'false'.
     * @param ids - array of disabled breakpoint identifiers.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken disable(String[] ids, DoneCommand done);

    /**
     * Tell target to remove breakpoints.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken remove(String[] ids, DoneCommand done);
    
    /**
     * Upload IDs of breakpoints known to target agent.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken getIDs(DoneGetIDs done);

    /**
     * Call back interface for 'getIDs' command.
     */
    interface DoneGetIDs {
        /**
         * Called when 'getIDs' command is done. 
         * @param token - command handle.
         * @param error - error object or null.
         * @param ids - IDs of breakpoints known to target agent.
         */
        void doneGetIDs(IToken token, Exception error, String[] ids);
    }

    /**
     * Upload properties of given breakpoint from target agent breakpoint table.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     */
    IToken getProperties(String id, DoneGetProperties done);

    /**
     * Call back interface for 'getProperties' command.
     */
    interface DoneGetProperties {
        /**
         * Called when 'getProperties' command is done. 
         * @param token - command handle.
         * @param error - error object or null.
         * @param properties - properties of the breakpoint.
         */
        void doneGetProperties(IToken token, Exception error, Map<String,Object> properties);
    }

    /**
     * Upload status of given breakpoint from target agent.
     * @param id - unique breakpoint identifier.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken getStatus(String id, DoneGetStatus done);

    /**
     * Call back interface for 'getStatus' command.
     */
    interface DoneGetStatus {
        /**
         * Called when 'getStatus' command is done. 
         * @param token - command handle.
         * @param error - error object or null.
         * @param status - status of the breakpoint.
         */
        void doneGetStatus(IToken token, Exception error, Map<String,Object> status);
    }

    /**
     * Report breakpoint service capabilities to clients so they
     * can adjust to different implementations of the service.
     * When called with a null ("") context ID the global capabilities are returned,
     * otherwise context specific capabilities are returned.  A special capability
     * property is used to indicate that all child contexts have the same
     * capabilities.
     * @param id - a context ID or null.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken getCapabilities(String id, DoneGetCapabilities done);

    /**
     * Call back interface for 'getCapabilities' command.
     */
    interface DoneGetCapabilities {
        /**
         * Called when 'getCapabilities' command is done. 
         * @param token - command handle.
         * @param error - error object or null.
         * @param capabilities - breakpoints service capabilities description.
         */
        void doneGetCapabilities(IToken token, Exception error, Map<String,Object> capabilities);
    }

    /**
     * Breakpoints service events listener.
     * Note that contextAdded, contextChanged and contextRemoved events carry exactly same set
     * of breakpoint properties that was sent by a client to a target. The purpose of these events is to
     * let all clients know about breakpoints that were created by other clients.
     */
    interface BreakpointsListener {

        /**
         * Called when breakpoint status changes.
         * @param id - unique breakpoint identifier.
         * @param status - breakpoint status.
         */
        void breakpointStatusChanged(String id, Map<String,Object> status);

        /**
         * Called when a new breakpoints are added.
         * @param bps - array of breakpoints.
         */
        void contextAdded(Map<String,Object>[] bps);

        /**
         * Called when breakpoint properties change.
         * @param bps - array of breakpoints.
         */
        void contextChanged(Map<String,Object>[] bps);

        /**
         * Called when breakpoints are removed .
         * @param ids - array of breakpoint IDs.
         */
        void contextRemoved(String[] ids);
    }

    /**
     * Add breakpoints service event listener.
     * @param listener - object that implements BreakpointsListener interface.
     */
    void addListener(BreakpointsListener listener);

    /**
     * Remove breakpoints service event listener.
     * @param listener - object that implements BreakpointsListener interface.
     */
    void removeListener(BreakpointsListener listener);
}
