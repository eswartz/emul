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

import java.math.BigDecimal;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * This is optional service that can be implemented by a peer. 
 * If implemented, the service can be used for testing of the peer and
 * communication channel functionality and reliability.
 */

public interface IDiagnostics extends IService {

    static final String NAME = "Diagnostics";
    
    /**
     * 'echo' command result returns same string that was given as command argument.
     * The command is used to test communication channel ability to transmit arbitrary strings in
     * both directions.  
     * @param s - any string.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken echo(String s, DoneEcho done);
    
    /**
     * Call back interface for 'echo' command.
     */
    interface DoneEcho {
        /**
         * Called when 'echo' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param s - same string as the command argument.
         */
        void doneEcho(IToken token, Throwable error, String s);
    }

    /**
     * 'echoFP' command result returns same floating point number that was given as command argument.
     * The command is used to test communication channel ability to transmit arbitrary floating point numbers in
     * both directions.  
     * @param n - any floating point number.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken echoFP(BigDecimal n, DoneEchoFP done);
    
    /**
     * Call back interface for 'echoFP' command.
     */
    interface DoneEchoFP {
        /**
         * Called when 'echoFP' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param n - same number as the command argument.
         */
        void doneEchoFP(IToken token, Throwable error, BigDecimal n);
    }

    /**
     * Get list of test names that are implemented by the service.
     * Clients can request remote peer to run a test from the list.
     * When started, a test performs a predefined set actions.
     * Nature of test actions is uniquely identified by test name.
     * Exact description of test actions is a contract between client and remote peer,
     * and it is not part of Diagnostics service specifications.
     * Clients should not attempt to run a test if they don't recognize the test name.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken getTestList(DoneGetTestList done);
    
    /**
     * Call back interface for 'getTestList' command.
     */
    interface DoneGetTestList {
        /**
         * Called when 'getTestList' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param list - names of tests that are supported by the peer. 
         */
        void doneGetTestList(IToken token, Throwable error, String[] list);
    }
    
    /**
     * Run a test. When started, a test performs a predefined set actions.
     * Nature of test actions is uniquely identified by test name.
     * Running test usually has associated execution context ID.
     * Depending on the test, the ID can be used with services RunControl and/or Processes services to control
     * test execution, and to obtain test results.
     * @param name - test name
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken runTest(String name, DoneRunTest done);
    
    /**
     * Call back interface for 'runTest' command.
     */
    interface DoneRunTest {
        /**
         * Called when 'runTest' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param context_id - test execution contest ID.
         */
        void doneRunTest(IToken token, Throwable error, String context_id);
    }

    /**
     * Cancel execution of a test.
     * @param context_id - text execution context ID.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken cancelTest(String context_id, DoneCancelTest done);
    
    /**
     * Call back interface for 'cancelTest' command.
     */
    interface DoneCancelTest {
        /**
         * Called when 'cancelTest' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         */
        void doneCancelTest(IToken token, Throwable error);
    }
    
    /**
     * Get information about a symbol in text execution context.
     * @param context_id
     * @param symbol_name
     * @param done
     * @return
     */
    IToken getSymbol(String context_id, String symbol_name, DoneGetSymbol done);
    
    /**
     * Call back interface for 'getSymbol' command.
     */
    interface DoneGetSymbol {
        /**
         * Called when 'getSymbol' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param symbol
         */
        void doneGetSymbol(IToken token, Throwable error, ISymbol symbol);
    }
    
    /**
     * Interface to access result value of 'getSymbol' command.
     */
    interface ISymbol {
        String getSectionName();
        Number getValue();
        boolean isUndef();
        boolean isCommon();
        boolean isGlobal();
        boolean isLocal();
        boolean isAbs();
    }
    
    /**
     * Create a pair of virtual streams, @see IStreams service.
     * Remote ends of the streams are connected, so any data sent into 'inp' stream
     * will become for available for reading from 'out' stream.
     * The command is used for testing virtual streams.
     * @param inp_buf_size - buffer size in bytes of the input stream.
     * @param out_buf_size - buffer size in bytes of the output stream.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken createTestStreams(int inp_buf_size, int out_buf_size, DoneCreateTestStreams done);
    
    /**
     * Call back interface for 'createTestStreams' command.
     */
    interface DoneCreateTestStreams {
        
        /**
         * Called when 'createTestStreams' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param inp_id - the input stream ID.
         * @param out_id - the output stream ID.
         */
        void doneCreateTestStreams(IToken token, Throwable error, String inp_id, String out_id);
    }
    
    /**
     * Dispose a virtual stream that was created by 'createTestStreams' command.
     * @param id - the stream ID.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken disposeTestStream(String id, DoneDisposeTestStream done);
    
    /**
     * Call back interface for 'disposeTestStream' command.
     */
    interface DoneDisposeTestStream {
        
        /**
         * Called when 'createTestStreams' command is done.
         * @param token - command handle.
         * @param error - error object or null.
         */
        void doneDisposeTestStream(IToken token, Throwable error);
    }
    
    /**
     * Send a command that is not implemented by peer.
     * Used to test handling of 'N' messages by communication channel.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken not_implemented_command(DoneNotImplementedCommand done);
    
    interface DoneNotImplementedCommand {
        
        /**
         * Called when 'not_implemented_command' command is done.
         * @param token - command handle.
         * @param error - error object.
         */
        void doneNotImplementedCommand(IToken token, Throwable error);
    }
}
