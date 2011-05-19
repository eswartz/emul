#******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
#******************************************************************************

from tcf import services

NAME = "StackTrace"

#
# Stack frame context property names.
#
PROP_ID = "ID"                         # String, stack frame ID
PROP_PARENT_ID = "ParentID"            # String, stack frame parent ID
PROP_PROCESS_ID = "ProcessID"          # String, stack frame process ID
PROP_NAME = "Name"                     # String, human readable name
PROP_TOP_FRAME = "TopFrame"            # Boolean, true if the frame is top frame on a stack
PROP_LEVEL = "Level"                   # Integer, stack frame level, starting from stack bottom
PROP_FRAME_ADDRESS = "FP"              # Number, stack frame memory address
PROP_RETURN_ADDRESS = "RP"             # Number, return address
PROP_INSTRUCTION_ADDRESS = "IP"        # Number, instruction pointer
PROP_ARGUMENTS_COUNT = "ArgsCnt"       # Integer, number of function arguments
PROP_ARGUMENTS_ADDRESS = "ArgsAddr"    # Number, memory address of function arguments

class StackTraceService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, ids, done):
        """
        Retrieve context info for given context IDs.

        The command will fail if parent thread is not suspended.
        Client can use Run Control service to suspend a thread.

        @param ids - array of context IDs.
        @param done - call back interface called when operation is completed.
        """
        raise NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, done):
        """
        Retrieve stack trace context list.
        Parent context usually corresponds to an execution thread.
        Some targets have more then one stack. In such case children of a thread
        are stacks, and stack frames are deeper in the hierarchy - they can be
        retrieved with additional getChildren commands.

        The command will fail if parent thread is not suspended.
        Client can use Run Control service to suspend a thread.

        @param parent_context_id - parent context ID.
        @param done - call back interface called when operation is completed.
        """
        raise NotImplementedError("Abstract method")

class DoneGetContext(object):
    """
    Client call back interface for getContext().
    """
    def doneGetContext(self, token, error, contexts):
        """
        Called when context data retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param contexts - array of context data or null if error.
        """
        pass

class DoneGetChildren(object):
    """
    Client call back interface for getChildren().
    """
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param context_ids - array of available context IDs.
        Stack frames are ordered from stack bottom to top.
        """
        pass

class StackTraceContext(object):
    """
    StackTraceContext represents stack trace objects - stacks and stack frames.
    """
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Stack Trace Context %s]" % self._props

    def getID(self):
        """
        Get Context ID.
        @return context ID.
        """
        return self._props.get(PROP_ID)

    def getParentID(self):
        """
        Get parent context ID.
        @return parent context ID.
        """
        return self._props.get(PROP_PARENT_ID)

    def getName(self):
        """
        Get context name - if context represents a stack.
        @return context name or null.
        """
        return self._props.get(PROP_NAME)

    def getFrameAddress(self):
        """
        Get memory address of this frame.
        @return address or None if not a stack frame.
        """
        return self._props.get(PROP_FRAME_ADDRESS)

    def getReturnAddress(self):
        """
        Get program counter saved in this stack frame -
        it is address of instruction to be executed when the function returns.
        @return return address or null if not a stack frame.
        """
        return self._props.get(PROP_RETURN_ADDRESS)

    def getInstructionAddress(self):
        """
        Get address of the next instruction to be executed in this stack frame.
        For top frame it is same as PC register value.
        For other frames it is same as return address of the next frame.
        @return instruction address or null if not a stack frame.
        """
        return self._props.get(PROP_INSTRUCTION_ADDRESS)

    def getArgumentsCount(self):
        """
        Get number of function arguments for this frame.
        @return function arguments count.
        """
        return self._props.get(PROP_ARGUMENTS_COUNT)

    def getArgumentsAddress(self):
        """
        Get address of function arguments area in memory.
        @return function arguments address or null if not available.
        """
        return self._props.get(PROP_ARGUMENTS_ADDRESS, 0)

    def getProperties(self):
        """
        Get complete map of context properties.
        @return map of context properties.
        """
        return self._props
