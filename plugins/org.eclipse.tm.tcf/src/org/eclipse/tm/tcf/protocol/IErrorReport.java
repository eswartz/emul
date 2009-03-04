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
package org.eclipse.tm.tcf.protocol;

import java.util.Map;

/**
 * This interface defines TCF standard format of error reports.
 * 
 * Exception objects can implement this interface to make error report details
 * available for clients.
 * 
 * Usage example:
 * 
 * Exception x = ...
 * if (x instanceof IErrorReport) {
 *      int error_code = ((IErrorReport)x).getErrorCode();
 * ...
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IErrorReport {

    /** Error report attribute names */
    public static final String
        ERROR_CODE = "Code",            // integer
        ERROR_TIME = "Time",            // integer
        ERROR_SERVICE = "Service",      // string
        ERROR_FORMAT = "Format",        // string
        ERROR_PARAMS = "Params",        // array
        ERROR_SEVERITY = "Severity",    // integer
        ERROR_ALT_CODE = "AltCode",     // integer
        ERROR_ALT_ORG = "AltOrg",       // string
        ERROR_CAUSED_BY = "CausedBy";   // object
    
    /** Error severity codes */
    public static final int
        SEVERITY_ERROR = 0,
        SEVERITY_WARNING = 1,
        SEVERITY_FATAL = 2;
    
    /** Error code ranges */
    public static final int
        /** Standard TCF code range */
        CODE_STD_MIN = 0,
        CODE_STD_MAX = 0xffff,
        
        /** Service specific codes. Decoding requires service ID. */
        CODE_SERVICE_SPECIFIC_MIN = 0x10000,
        CODE_SERVICE_SPECIFIC_MAX = 0x1ffff,
        
        /** Reserved codes - will never be used by the TCF standard */
        CODE_RESERVED_MIN = 0x20000,
        CODE_RESERVED_MAX = 0x2ffff;
    
    /** Standard TCF error codes */ 
    public static final int
        TCF_ERROR_OTHER               = 1,
        TCF_ERROR_JSON_SYNTAX         = 2,
        TCF_ERROR_PROTOCOL            = 3,
        TCF_ERROR_BUFFER_OVERFLOW     = 4,
        TCF_ERROR_CHANNEL_CLOSED      = 5,
        TCF_ERROR_COMMAND_CANCELLED   = 6,
        TCF_ERROR_UNKNOWN_PEER        = 7,
        TCF_ERROR_BASE64              = 8,
        TCF_ERROR_EOF                 = 9,
        TCF_ERROR_ALREADY_STOPPED     = 10,
        TCF_ERROR_ALREADY_EXITED      = 11,
        TCF_ERROR_ALREADY_RUNNING     = 12,
        TCF_ERROR_ALREADY_ATTACHED    = 13,
        TCF_ERROR_IS_RUNNING          = 14,
        TCF_ERROR_INV_DATA_SIZE       = 15,
        TCF_ERROR_INV_CONTEXT         = 16,
        TCF_ERROR_INV_ADDRESS         = 17,
        TCF_ERROR_INV_EXPRESSION      = 18,
        TCF_ERROR_INV_FORMAT          = 19,
        TCF_ERROR_INV_NUMBER          = 20,
        TCF_ERROR_INV_DWARF           = 21,
        TCF_ERROR_SYM_NOT_FOUND       = 22,
        TCF_ERROR_UNSUPPORTED         = 23,
        TCF_ERROR_INV_DATA_TYPE       = 24,
        TCF_ERROR_INV_COMMAND         = 25;
    
    public int getErrorCode();
    
    public int getAltCode();
    
    public String getAltOrg();
    
    public Map<String,Object> getAttributes();
}
