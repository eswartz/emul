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
ITerminalsService allows to launch a new terminal on the remote target system.
"""

from tcf import services

# This service name, as it appears on the wire - a TCF name of the service.
NAME = "Terminals"

# Context property names.
# The TCF context ID
PROP_ID = "ID",

# The process ID of the login process of the terminal
PROP_PROCESS_ID = "ProcessID",

# The PTY type
PROP_PTY_TYPE = "PtyType",

# terminal encoding
PROP_ENCODING = "Encoding",

# window width size
PROP_WIDTH = "Width",

# window height size
PROP_HEIGHT = "Height",

# Process standard input stream ID
PROP_STDIN_ID = "StdInID",

# Process standard output stream ID
PROP_STDOUT_ID = "StdOutID",

# Process standard error stream ID
PROP_STDERR_ID = "StdErrID"

class TerminalContext(object):
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Terminals Context %s]" % str(self._props)

    def getID(self):
        """
        Get context ID.
        Same as getProperties().get(“ID”)
        """
        return self._props.get(PROP_ID)

    def getProcessID(self):
        """
        Get process ID of the login process of the terminal.
        Same as getProperties().get(“ProcessID”)
        """
        return self._props.get(PROP_PROCESS_ID)

    def getPtyType(self):
        """
        Get terminal type.
        Same as getProperties().get(“PtyType”)
        """
        return self._props.get(PROP_PTY_TYPE)

    def getEncoding(self):
        """
        Get encoding.
        Same as getProperties().get(“Encoding”)
        """
        return self._props.get(PROP_ENCODING)

    def getWidth(self):
        """
        Get width.
        Same as getProperties().get(“Width”)
        """
        return self._props.get(PROP_WIDTH)

    def getHeight(self):
        """
        Get height.
        Same as getProperties().get(“Height”)
        """
        return self._props.get(PROP_HEIGHT)

    def getProperties(self):
        """
        Get all available context properties.
        @return Map 'property name' -> 'property value'
        """
        return self._props

    def exit(self, done):
        """
        Exit the terminal.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")
        
class TerminalsService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve context info for given context ID.
        A context corresponds to an terminal.
        Context IDs are valid across TCF services, so it is allowed to issue
        'ITerminals.getContext' command with a context that was obtained,
        for example, from Memory service.
        However, 'ITerminals.getContext' is supposed to return only terminal specific data,
        If the ID is not a terminal ID, 'ITerminals.getContext' may not return any
        useful information
        
        @param id – context ID.
        @param done - call back interface called when operation is completed.
        """
        raise NotImplementedError("Abstract method")

    def launch(self, type, encoding, environment, done):
        """
        Launch a new terminal to remote machine.
        @param type - requested terminal type for the new terminal.
        @param encoding - requested encoding for the new terminal.
        @param environment - Array of environment variable strings.
        if null then default set of environment variables will be used.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

    def setWinSize(self, context_id, newWidth, newHeight, done):
        """
        Set the terminal widows size
        @param context_id - context ID.
        @param signal - signal code.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

    def exit(self, context_id, done):
        """
        Exit a terminal.
        @param context_id - context ID.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add terminals service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove terminals service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")


class DoneGetContext(object):
    """
    Client call back interface for getContext().
    """
    def doneGetContext(self, token, error, context):
        """
        Called when contexts data retrieval is done.
        @param error – error description if operation failed, null if succeeded.
        @param context – context data.
        """
        pass

class DoneCommand(object):
    def doneCommand(self, token, error):
        pass

class DoneLaunch(object):
    """
    Call-back interface to be called when "start" command is complete.
    """
    def doneLaunch(self, token, error, terminal):
        pass

class TerminalsListener(object):
    """
    Process event listener is notified when a terminal exits.
    Event are reported only for terminals that were started by 'launch' command.
    """
    def exited(self, terminal_id, exit_code):
        """
        Called when a terminal exits.
        @param terminal_id - terminal context ID
        @param exit_code - terminal exit code
        """
        pass

    def winSizeChanged (self, terminal_id, newWidth, newHeight):
        """
        Called when a terminal exits.
        @param terminal_id - terminal context ID
        @param newWidth – new terminal width
        @param newHeight – new terminal height
        """
        pass
