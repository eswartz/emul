/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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

import org.eclipse.tm.tcf.protocol.IToken;

/**
 * Extension of Processes service.
 * It provides new "start" command that supports additional parameters.
 */
public interface IProcessesV1 extends IProcesses {

    static final String NAME = "ProcessesV1";

    /** Process start parameters */
    static final String
        /** Boolean, attach the debugger to the process */
        START_ATTACH = "Attach",
        /** Boolean, auto-attach process children */
        START_ATTACH_CHILDREN = "AttachChildren",
        /** Boolean, Use pseudo-terminal for the process standard I/O */
        START_USE_TERMINAL = "UseTerminal";

    /**
     * Start a new process on remote machine.
     * @param directory - initial value of working directory for the process.
     * @param file - process image file.
     * @param command_line - command line arguments for the process.
     * Note: the service does NOT add image file name as first argument for the process.
     * If a client wants first parameter to be the file name, it should add it itself.
     * @param environment - map of environment variables for the process,
     * if null then default set of environment variables will be used.
     * @param params - additional process start parameters, see START_*.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken start(String directory, String file,
            String[] command_line, Map<String,String> environment,
            Map<String,Object> params, DoneStart done);

}
