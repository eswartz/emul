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

package org.eclipse.tm.internal.tcf.examples.daytime;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * IDaytimeService allows retrieving the time of day from a remote system.
 */
public interface IDaytimeService extends IService {

    /**
     * This service name, as it appears on the wire - a TCF name of the service.
     */
    public static final String NAME = "Daytime";

    /**
     * Retrieve the time of day from remote system.
     * The method sends the command to remote server and returns -
     * it does not wait for the server response. Instead a client should provide
     * a call-back object that will be called when the server answers the command
     * or when the command is aborted by communication error.
     * @param tz - time zone name.
     * @param done - a call-back object.
     * @return a handle for the pending command. The handle can be used to cancel the command,
     * and to match responses to requests - if same call-back object is used for 
     * multiple requests. 
     */
    IToken getTimeOfDay(String tz, DoneGetTimeOfDay done);

    /**
     * Call-back interface for getTimeOfDay() command.  
     */
    interface DoneGetTimeOfDay {
        /**
         * This method is called when getTimeOfDay() command is completed.
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - a String of the form "01 MAR 2006 11:25:12 CET"
         */
        void doneGetTimeOfDay(IToken token, Exception error, String str);
    }
}
