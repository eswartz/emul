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
package com.windriver.debug.tcf.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;

import com.windriver.debug.tcf.core.TCFCore;

public class TCFError extends DebugException {

    private static final long serialVersionUID = -4261097789666829020L;

    public TCFError(Throwable exception) {
        super(new Status(exception));
    }

    private static class Status implements IStatus {

        private final Throwable exception;

        private Status(Throwable exception) {
            this.exception = exception;
        }

        public IStatus[] getChildren() {
            return null;
        }

        public int getCode() {
            return 1;
        }

        public Throwable getException() {
            return exception;
        }

        public String getMessage() {
            return exception.getMessage();
        }

        public String getPlugin() {
            return TCFCore.PLUGIN_ID;
        }

        public int getSeverity() {
            return ERROR;
        }

        public boolean isMultiStatus() {
            return false;
        }

        public boolean isOK() {
            return false;
        }

        public boolean matches(int severityMask) {
            return false;
        }
    }
}
