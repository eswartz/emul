/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel - initial API and implementation
 *******************************************************************************/

package org.eclipse.tm.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * ITerminalsService allows to launch a new terminal on the remote target system.
 */
public interface ITerminals extends IService {

    /**
     * This service name, as it appears on the wire - a TCF name of the service.
     */
    static final String NAME = "Terminals";

    /**
     * Retrieve context info for given context ID.
     * A context corresponds to an terminal.
     * Context IDs are valid across TCF services, so it is allowed to issue
     * 'ITerminals.getContext' command with a context that was obtained,
     * for example, from Memory service.
     * However, 'ITerminals.getContext' is supposed to return only terminal specific data,
     * If the ID is not a terminal ID, 'ITerminals.getContext' may not return any
     * useful information
     *
     * @param id – context ID.
     * @param done - call back interface called when operation is completed.
     */
    IToken getContext(String id, DoneGetContext done);

    /**
     * Client call back interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when contexts data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context data.
         */
        void doneGetContext(IToken token, Exception error, TerminalContext context);
    }

    /**
     * Context property names.
     */
    static final String
        /** The TCF context ID of the terminal */
        PROP_ID = "ID",

        /** The process ID of the login process of the terminal */
        PROP_PROCESS_ID = "ProcessID",

        /** The PTY type */
        PROP_PTY_TYPE = "PtyType",

        /** The terminal streams encoding */
        PROP_ENCODING = "Encoding",

        /** Window width size */
        PROP_WIDTH = "Width",

        /** Window height size */
        PROP_HEIGHT = "Height",

        /** Terminal standard input stream ID */
        PROP_STDIN_ID = "StdInID",

        /** Terminal standard output stream ID */
        PROP_STDOUT_ID = "StdOutID",

        /** Terminal standard error stream ID */
        PROP_STDERR_ID = "StdErrID";

    interface TerminalContext {

        /**
         * Get context ID.
         * Same as getProperties().get(“ID”)
         */
        String getID();

        /**
         * Get process ID of the login process of the terminal.
         * Same as getProperties().get(“ProcessID”)
         */
        String getProcessID();

        /**
         * Get terminal type.
         * Same as getProperties().get(“PtyType”)
         */
        String getPtyType();

        /**
         * Get encoding.
         * Same as getProperties().get(“Encoding”)
         */
        String getEncoding();

        /**
         * Get width.
         * Same as getProperties().get(“Width”)
         */
        int getWidth();

        /**
         * Get height.
         * Same as getProperties().get(“Height”)
         */
        int getHeight();

        /**
         * Get standard input stream ID of the terminal.
         * Same as getProperties().get(“StdInID”)
         */
        String getStdInID();

        /**
         * Get standard output stream ID of the terminal.
         * Same as getProperties().get(“StdOutID”)
         */
        String getStdOutID();

        /**
         * Get standard error stream ID of the terminal.
         * Same as getProperties().get(“StdErrID”)
         */
        String getStdErrID();

        /**
         * Get all available terminal properties.
         * @return Map 'property name' -> 'property value'
         */
        Map<String, Object> getProperties();

        /**
         * Exit the terminal.
         * @param done - call back interface called when operation is completed.
         * @return pending command handle, can be used to cancel the command.
         */
        IToken exit(DoneCommand done);
    }

    interface DoneCommand {
        void doneCommand(IToken token, Exception error);
    }

    /**
     * Launch a new terminal to remote machine.
     * @param type - requested terminal type for the new terminal.
     * @param encoding - requested encoding for the new terminal.
     * @param environment - Array of environment variable strings.
     * if null then default set of environment variables will be used.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken launch(String type, String encoding, String[] environment, DoneLaunch done);

    /**
     * Call-back interface to be called when "start" command is complete.
     */
    interface DoneLaunch {
        void doneLaunch(IToken token, Exception error, TerminalContext terminal);
    }


    /**
     * Set the terminal widows size
     * @param context_id - context ID.
     * @param signal - signal code.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken setWinSize(String context_id, int newWidth, int newHeight, DoneCommand done);

    /**
     * Exit a terminal.
     * @param context_id - context ID.
     * @param done - call back interface called when operation is completed.
     * @return pending command handle, can be used to cancel the command.
     */
    IToken exit(String context_id, DoneCommand done);

    /**
     * Add terminals service event listener.
     * @param listener - event listener implementation.
     */
    void addListener(TerminalsListener listener);

    /**
     * Remove terminals service event listener.
     * @param listener - event listener implementation.
     */
    void removeListener(TerminalsListener listener);

    /**
     * Process event listener is notified when a terminal exits.
     * Event are reported only for terminals that were started by 'launch' command.
     */
    interface TerminalsListener {

        /**
         * Called when a terminal exits.
         * @param terminal_id - terminal context ID
         * @param exit_code - terminal exit code
         */
        void exited(String terminal_id, int exit_code);

        /**
         * Called when a terminal exits.
         * @param terminal_id - terminal context ID
         * @param newWidth – new terminal width
         * @param newHeight – new terminal height
         */
        void winSizeChanged (String terminal_id, int newWidth, int newHeight);
    }
}
