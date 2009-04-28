/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IErrorReport;

class ErrorReport extends Exception implements IErrorReport {
    
    private static final long serialVersionUID = 3687543884858739977L;
    private final Map<String,Object> attrs;
    
    @SuppressWarnings("unchecked")
    ErrorReport(String msg, Map<String,Object> attrs) {
        super(msg);
        this.attrs = attrs;
        Object caused_by = attrs.get(IErrorReport.ERROR_CAUSED_BY);
        if (caused_by != null) {
            Map<String,Object> map = (Map<String,Object>)caused_by;
            StringBuffer bf = new StringBuffer();
            bf.append("TCF error report:");
            bf.append('\n');
            Command.appendErrorProps(bf, map);
            initCause(new ErrorReport(bf.toString(), map));
        }
    }
    
    ErrorReport(String msg, int code) {
        super(msg);
        attrs = new HashMap<String,Object>();
        attrs.put(ERROR_CODE, code);
        attrs.put(ERROR_TIME, System.currentTimeMillis());
        attrs.put(ERROR_FORMAT, msg);
        attrs.put(ERROR_SEVERITY, SEVERITY_ERROR);
    }

    public int getErrorCode() {
        Number n = (Number)attrs.get(ERROR_CODE);
        if (n == null) return 0;
        return n.intValue();
    }

    public int getAltCode() {
        Number n = (Number)attrs.get(ERROR_ALT_CODE);
        if (n == null) return 0;
        return n.intValue();
    }

    public String getAltOrg() {
        return (String)attrs.get(ERROR_ALT_ORG);
    }

    public Map<String, Object> getAttributes() {
        return attrs;
    }
}