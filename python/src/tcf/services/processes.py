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
IProcesses service provides access to the target OS's process
information, allows to start and terminate a process, and allows
to attach and detach a process for debugging. Debug services,
like IMemory and IRunControl, require a process to be attached
before they can access it.

If a process is started by this service, its standard input/output streams are
available for client to read/write using Streams service. Stream type of such
streams is set to "Processes".
"""

import exceptions
from tcf import services

NAME = "Processes"

# Context property names.

# The TCF context ID
PROP_ID = "ID"

# The TCF parent context ID
PROP_PARENT_ID = "ParentID"

# Is the context attached
PROP_ATTACHED = "Attached"

# Can terminate the context
PROP_CAN_TERMINATE = "CanTerminate"

# Process name. Client UI can show this name to a user
PROP_NAME = "Name"

# Process standard input stream ID
PROP_STDIN_ID = "StdInID"

# Process standard output stream ID
PROP_STDOUT_ID = "StdOutID"

# Process standard error stream ID
PROP_STDERR_ID = "StdErrID"


# Signal property names used by "getSignalList" command.

# Number, bit position in the signal mask
SIG_INDEX = "Index"

#String, signal name, for example "SIGHUP"
SIG_NAME = "Name"

# Number, signal code, as defined by OS
SIG_CODE = "Code"

# String, human readable description of the signal
SIG_DESCRIPTION = "Description"


class ProcessesService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve context info for given context ID.
        A context corresponds to an execution thread, process, address space, etc.
        Context IDs are valid across TCF services, so it is allowed to issue
        'IProcesses.getContext' command with a context that was obtained,
        for example, from Memory service.
        However, 'Processes.getContext' is supposed to return only process specific data,
        If the ID is not a process ID, 'IProcesses.getContext' may not return any
        useful information
        
        @param id - context ID.
        @param done - call back interface called when operation is completed.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, attached_only, done):
        """
        Retrieve children of given context.
        
        @param parent_context_id - parent context ID. Can be None -
        to retrieve top level of the hierarchy, or one of context IDs retrieved
        by previous getContext or getChildren commands.
        @param attached_only - if True return only attached process IDs.
        @param done - call back interface called when operation is completed.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def getSignalList(self, context_id, done):
        """
        Get list of signals that can be send to the process.
        @param context_id - process context ID or None.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def getSignalMask(self, context_id, done):
        """
        Get process or thread signal mask.
        Bits in the mask control how signals should be handled by debug agent.
        When new context is created it inherits the mask from its parent.
        If context is not attached the command will return an error.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def setSignalMask(self, context_id, dont_stop, dont_pass, done):
        """
        Set process or thread signal mask.
        Bits in the mask control how signals should be handled by debug agent.
        If context is not attached the command will return an error.
        @param dont_stop - bit-set of signals that should not suspend execution of the context.
        By default, debugger suspends a context before it receives a signal.
        @param dont_pass - bit-set of signals that should not be delivered to the context.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def signal(self, context_id, signal, done):
        """
        Send a signal to a process or thread.
        @param context_id - context ID.
        @param signal - signal code.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def getEnvironment(self, done):
        """
        Get default set of environment variables used to start a new process.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def start(self, directory, file, command_line, environment, attach, done):
        """
        Start a new process on remote machine.
        @param directory - initial value of working directory for the process.
        @param file - process image file.
        @param command_line - command line arguments for the process.
        Note: the service does NOT add image file name as first argument for the process.
        If a client wants first parameter to be the file name, it should add it itself.
        @param environment - map of environment variables for the process,
        if None then default set of environment variables will be used.
        @param attach - if True debugger should be attached to the process.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add processes service event listener.
        @param listener - event listener implementation.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove processes service event listener.
        @param listener - event listener implementation.
        """
        raise exceptions.NotImplementedError("Abstract method")


class ProcessContext(object):
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Processes Context %s]" % self._props
        
    def getProperties(self):
        """
        Get context properties. See PROP_* definitions for property names.
        Context properties are read only, clients should not try to modify them.
        @return Map of context properties.
        """
        return self._props

    def getID(self):
        """
        Retrieve context ID.
        Same as getProperties().get('ID')
        """
        return self._props.get(PROP_ID)

    def getParentID(self):
        """
        Retrieve parent context ID.
        Same as getProperties().get('ParentID')
        """
        return self._props.get(PROP_PARENT_ID)

    def getName(self):
        """
        Retrieve human readable context name.
        Same as getProperties().get('Name')
        """
        return self._props.get(PROP_NAME)

    def isAttached(self):
        """
        Utility method to read context property PROP_ATTACHED.
        Services like IRunControl, IMemory, IBreakpoints work only with attached processes.
        @return value of PROP_ATTACHED.
        """
        return self._props.get(PROP_ATTACHED)

    def canTerminate(self):
        """
        Utility method to read context property PROP_CAN_TERMINATE.
        @return value of PROP_CAN_TERMINATE.
        """
        return self._props.get(PROP_CAN_TERMINATE)

    def attach(self, done):
        """
        Attach debugger to a process.
        Services like IRunControl, IMemory, IBreakpoints work only with attached processes.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def detach(self, done):
        """
        Detach debugger from a process.
        Process execution will continue without debugger supervision.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

    def terminate(self, done):
        """
        Terminate a process.
        @param done - call back interface called when operation is completed.
        @return pending command handle, can be used to cancel the command.
        """
        raise exceptions.NotImplementedError("Abstract method")

class DoneCommand(object):
    """
    Call-back interface to be called when command is complete.
    """
    def doneCommand(self, token, error):
        pass

class DoneGetContext(object):
    """
    Client call back interface for getContext().
    """
    def doneGetContext(self, token, error, context):
        """
        Called when context data retrieval is done.
        @param error - error description if operation failed, None if succeeded.
        @param context - context data.
        """
        pass

class DoneGetChildren(object):
    """
    Client call back interface for getChildren().
    """
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param error - error description if operation failed, None if succeeded.
        @param context_ids - array of available context IDs.
        """
        pass

class DoneGetSignalList(object):
    """
    Call-back interface to be called when "getSignalList" command is complete.
    """
    def doneGetSignalList(self, token, error, list):
        pass

class DoneGetSignalMask(object):
    """
    Call-back interface to be called when "getSignalMask" command is complete.
    """
    def doneGetSignalMask(self, token, error, dont_stop, dont_pass, pending):
        """
        @param token - command handle.
        @param dont_stop - bit-set of signals that should suspend execution of the context.
        @param dont_pass - bit-set of signals that should not be delivered to the context.
        @param pending - bit-set of signals that are generated but not delivered yet.
        Note: "pending" is meaningful only if the context is suspended.
        """
        pass

class DoneGetEnvironment(object):
    """
    Call-back interface to be called when "getEnvironment" command is complete.
    """
    def doneGetEnvironment(self, token, error, environment):
        pass

class DoneStart(object):
    """
    Call-back interface to be called when "start" command is complete.
    """
    def doneStart(self, token, error, process):
        pass

class ProcessesListener(object):
    """
    Process event listener is notified when a process exits.
    Event are reported only for processes that were started by 'start' command.
    """

    def exited(self, process_id, exit_code):
        """
        Called when a process exits.
        @param process_id - process context ID
        @param exit_code - if >= 0 - the process exit code,
        if < 0 - process was terminated by a signal, the signal code = -exit_code.
        """
        pass
