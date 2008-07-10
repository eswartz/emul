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

/**
 * Clients of stand-alone version the framework should implement this interface and call Protocol.setLogger.
 * Eclipse based clients don't need to implement ILogger since the implementation is provide by TCF bundle activator.
 */
public interface ILogger {

    /**
     * Add an entry into a log.
     * 
     * This method can be invoked from any thread.
     * 
     * @param msg - log entry text. 
     * @param x - a Java exception associated with the log entry or null.
     */
    void log(String msg, Throwable x);
}
