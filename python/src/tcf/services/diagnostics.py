# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

"""
This is an optional service that can be implemented by a peer.
If implemented, the service can be used for testing of the peer and
communication channel functionality and reliability.
"""

from tcf import services

NAME = "Diagnostics"

class DiagnosticsService(services.Service):
    def getName(self):
        return NAME

    def echo(self, s, done):
        """
        'echo' command result returns same string that was given as command argument.
        The command is used to test communication channel ability to transmit arbitrary strings in
        both directions.
        @param s - any string.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def echoFP(self, n, done):
        """
        'echoFP' command result returns same floating point number that was given as command argument.
        The command is used to test communication channel ability to transmit arbitrary floating point numbers in
        both directions.
        @param n - any floating point number.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def echoERR(self, error, done):
        """
        'echoERR' command result returns same error report that was given as command argument.
        The command is used to test remote agent ability to receive and transmit TCF error reports.
        @param error - an error object.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def getTestList(self, done):
        """
        Get list of test names that are implemented by the service.
        Clients can request remote peer to run a test from the list.
        When started, a test performs a predefined set actions.
        Nature of test actions is uniquely identified by test name.
        Exact description of test actions is a contract between client and remote peer,
        and it is not part of Diagnostics service specifications.
        Clients should not attempt to run a test if they don't recognize the test name.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def runTest(self, name, done):
        """
        Run a test. When started, a test performs a predefined set actions.
        Nature of test actions is uniquely identified by test name.
        Running test usually has associated execution context ID.
        Depending on the test, the ID can be used with services RunControl and/or Processes services to control
        test execution, and to obtain test results.
        @param name - test name
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def cancelTest(self, context_id, done):
        """
        Cancel execution of a test.
        @param context_id - text execution context ID.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def getSymbol(self, context_id, symbol_name, done):
        """
        Get information about a symbol in text execution context.
        @param context_id
        @param symbol_name
        @param done
        @return
        """
        return NotImplementedError("Abstract method")

    def createTestStreams(self, inp_buf_size, out_buf_size, done):
        """
        Create a pair of virtual streams, @see IStreams service.
        Remote ends of the streams are connected, so any data sent into 'inp' stream
        will become for available for reading from 'out' stream.
        The command is used for testing virtual streams.
        @param inp_buf_size - buffer size in bytes of the input stream.
        @param out_buf_size - buffer size in bytes of the output stream.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def disposeTestStream(self, id, done):
        """
        Dispose a virtual stream that was created by 'createTestStreams' command.
        @param id - the stream ID.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def not_implemented_command(self, done):
        """
        Send a command that is not implemented by peer.
        Used to test handling of 'N' messages by communication channel.
        @param done - command result call back object.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")


class DoneEcho(object):
    """
    Call back interface for 'echo' command.
    """
    def doneEcho(self, token, error, s):
        """
        Called when 'echo' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param s - same string as the command argument.
        """
        pass

class DoneEchoFP(object):
    """
    Call back interface for 'echoFP' command.
    """
    def doneEchoFP(self, token, error, n):
        """
        Called when 'echoFP' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param n - same number as the command argument.
        """
        pass

class DoneEchoERR(object):
    """
    Call back interface for 'echoERR' command.
    """
    def doneEchoERR(self, token, error, error_obj, error_msg):
        """
        Called when 'echoERR' command is done.
        @param token - command handle.
        @param error - communication error report or None.
        @param error_obj - error object, should be equal to the command argument.
        @param error_msg - error object converted to a human readable string.
        """
        pass

class DoneGetTestList(object):
    """
    Call back interface for 'getTestList' command.
    """
    def doneGetTestList(self, token, error, list):
        """
        Called when 'getTestList' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param list - names of tests that are supported by the peer.
        """
        pass

class DoneRunTest(object):
    """
    Call back interface for 'runTest' command.
    """
    def doneRunTest(self, token, error, context_id):
        """
        Called when 'runTest' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param context_id - test execution contest ID.
        """
        pass

class DoneCancelTest(object):
    """
    Call back interface for 'cancelTest' command.
    """
    def doneCancelTest(self, token, error):
        """
        Called when 'cancelTest' command is done.
        @param token - command handle.
        @param error - error object or None.
        """
        pass

class DoneGetSymbol(object):
    """
    Call back interface for 'getSymbol' command.
    """
    def doneGetSymbol(self, token, error, symbol):
        """
        Called when 'getSymbol' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param symbol
        """
        pass

class Symbol(object):
    """
    Represents result value of 'getSymbol' command.
    """
    def __init__(self, props):
        self._props = props or {}
    def getSectionName(self):
        return self._props.get("Section")
    def getValue(self):
        return self._props.get("Value")
    def isUndef(self):
        val = self._props.get("Storage")
        return val == "UNDEF"
    def isCommon(self):
        val = self._props.get("Storage")
        return val == "COMMON"
    def isGlobal(self):
        val = self._props.get("Storage")
        return val == "GLOBAL"
    def isLocal(self):
        val = self._props.get("Storage")
        return val == "LOCAL"
    def isAbs(self):
        return self._props.get("Abs", False)

class DoneCreateTestStreams(object):
    """
    Call back interface for 'createTestStreams' command.
    """
    def doneCreateTestStreams(self, token, error, inp_id, out_id):
        """
        Called when 'createTestStreams' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param inp_id - the input stream ID.
        @param out_id - the output stream ID.
        """
        pass

class DoneDisposeTestStream(object):
    """
    Call back interface for 'disposeTestStream' command.
    """
    def doneDisposeTestStream(self, token, error):
        """
        Called when 'createTestStreams' command is done.
        @param token - command handle.
        @param error - error object or None.
        """
        pass

class DoneNotImplementedCommand(object):
    def doneNotImplementedCommand(self, token, error):
        """
        Called when 'not_implemented_command' command is done.
        @param token - command handle.
        @param error - error object.
        """
        pass
