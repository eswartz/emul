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

from tcf import services

NAME = "RunControl"

# Context property names.
# Run control context ID */
PROP_ID = "ID"

# Context parent (owner) ID, for a thread it is same as process ID */
PROP_PARENT_ID = "ParentID"

# Context process (memory space) ID */
PROP_PROCESS_ID = "ProcessID"

# ID of a context that created this context */
PROP_CREATOR_ID = "CreatorID"

# Human readable context name */
PROP_NAME = "Name"

# true if the context is a container. Container can propagate run control commands to his children */
PROP_IS_CONTAINER = "IsContainer"

# true if context has execution state - can be suspended/resumed */
PROP_HAS_STATE = "HasState"

# Bit-set of RM_ values that are supported by the context */
PROP_CAN_RESUME = "CanResume"

# Bit-set of RM_ values that can be used with count > 1 */
PROP_CAN_COUNT = "CanCount"

# true if suspend command is supported by the context */
PROP_CAN_SUSPEND = "CanSuspend"

# true if terminate command is supported by the context */
PROP_CAN_TERMINATE = "CanTerminate"

# Context ID of a run control group that contains the context.
# Members of same group are always suspended and resumed together:
# resuming/suspending a context resumes/suspends all members of the group */
PROP_RC_GROUP = "RCGroup"

# Context resume modes.
RM_RESUME = 0

# Step over a single instruction.
# If the instruction is a function call then don't stop until the function returns.

RM_STEP_OVER = 1

# Step a single instruction.
# If the instruction is a function call then stop at first instruction of the function.
RM_STEP_INTO = 2

# Step over a single source code line.
# If the line contains a function call then don't stop until the function returns.
RM_STEP_OVER_LINE = 3

# Step a single source code line.
# If the line contains a function call then stop at first line of the function.
RM_STEP_INTO_LINE = 4

# Run until control returns from current function.
RM_STEP_OUT = 5

# Start running backwards.
# Execution will continue until suspended by command or breakpoint.
RM_REVERSE_RESUME = 6

# Reverse of RM_STEP_OVER - run backwards over a single instruction.
# If the instruction is a function call then don't stop until get out of the function.
RM_REVERSE_STEP_OVER = 7

# Reverse of RM_STEP_INTO.
# This effectively "un-executes" the previous instruction
RM_REVERSE_STEP_INTO = 8

# Reverse of RM_STEP_OVER_LINE.
# Resume backward execution of given context until control reaches an instruction that belongs
# to a different source line.
# If the line contains a function call then don't stop until get out of the function.
# Error is returned if line number information not available.
RM_REVERSE_STEP_OVER_LINE = 9

# Reverse of RM_STEP_INTO_LINE,
# Resume backward execution of given context until control reaches an instruction that belongs
# to a different line of source code.
# If a function is called, stop at the beginning of the last line of the function code.
# Error is returned if line number information not available.
RM_REVERSE_STEP_INTO_LINE = 10

# Reverse of RM_STEP_OUT.
# Resume backward execution of the given context until control reaches the point where the current function was called.
RM_REVERSE_STEP_OUT = 11

# Step over instructions until PC is outside the specified range.
# Any function call within the range is considered to be in range.
RM_STEP_OVER_RANGE = 12

# Step instruction until PC is outside the specified range for any reason.
RM_STEP_INTO_RANGE = 13

# Reverse of RM_STEP_OVER_RANGE
RM_REVERSE_STEP_OVER_RANGE = 14

# Reverse of RM_STEP_INTO_RANGE
RM_REVERSE_STEP_INTO_RANGE = 15

# Run until the context becomes active - scheduled to run on a target CPU
RM_UNTIL_ACTIVE = 16

# Run reverse until the context becomes active
RM_REVERSE_UNTIL_ACTIVE = 17

# State change reason of a context.
# Reason can be any text, but if it is one of predefined strings,
# a generic client might be able to handle it better.
REASON_USER_REQUEST = "Suspended"
REASON_STEP = "Step"
REASON_BREAKPOINT = "Breakpoint"
REASON_EXCEPTION = "Exception"
REASON_CONTAINER = "Container"
REASON_WATCHPOINT = "Watchpoint"
REASON_SIGNAL = "Signal"
REASON_SHAREDLIB = "Shared Library"
REASON_ERROR = "Error"

# Optional parameters of context state.
STATE_SIGNAL = "Signal"
STATE_SIGNAL_NAME = "SignalName"
STATE_SIGNAL_DESCRIPTION = "SignalDescription"
STATE_BREAKPOINT_IDS = "BPs"
STATE_PC_ERROR = "PCError"

# Optional parameters of resume command.
# Integer - starting address of step range, inclusive */
RP_RANGE_START = "RangeStart"

# Integer - ending address of step range, exclusive */
RP_RANGE_END = "RangeEnd"

class RunControlService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve context properties for given context ID.

        @param id - context ID.
        @param done - callback interface called when operation is completed.
        """
        raise NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, done):
        """
        Retrieve children of given context.

        @param parent_context_id - parent context ID. Can be null -
        to retrieve top level of the hierarchy, or one of context IDs retrieved
        by previous getContext or getChildren commands.
        @param done - callback interface called when operation is completed.
        """
        raise NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add run control event listener.
        @param listener - run control event listener to add.
        """
        raise NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove run control event listener.
        @param listener - run control event listener to remove.
        """
        raise NotImplementedError("Abstract method")


class RunControlError(Exception):
    pass

class DoneGetState(object):
    def doneGetState(self, token, error, suspended, pc, reason, params):
        """
        Called when getState command execution is complete.
        @param token - pending command handle.
        @param error - command execution error or null.
        @param suspended - true if the context is suspended
        @param pc - program counter of the context (if suspended).
        @param reason - suspend reason (if suspended), see REASON_*.
        @param params - additional target specific data about context state, see STATE_*.
        """
        pass

class DoneCommand(object):
    def doneCommand(self, token, error):
        """
        Called when run control command execution is complete.
        @param token - pending command handle.
        @param error - command execution error or null.
        """
        pass

class DoneGetContext(object):
    "Client callback interface for getContext()."
    def doneGetContext(self, token, error, context):
        """
        Called when context data retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param context - context data.
        """
        pass

class DoneGetChildren(object):
    "Client callback interface for getChildren()."
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param context_ids - array of available context IDs.
        """
        pass

class RunControlContext(object):
    """
    A context corresponds to an execution thread, process, address space, etc.
    A context can belong to a parent context. Contexts hierarchy can be simple
    plain list or it can form a tree. It is up to target agent developers to choose
    layout that is most descriptive for a given target. Context IDs are valid across
    all services. In other words, all services access same hierarchy of contexts,
    with same IDs, however, each service accesses its own subset of context's
    attributes and functionality, which is relevant to that service.
    """
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Run Control Context %s]" % self._props

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

    def getProcessID(self):
        """
        Retrieve context process ID.
        Same as getProperties().get('ProcessID')
        """
        return self._props.get(PROP_PROCESS_ID)

    def getCreatorID(self):
        """
        Retrieve context creator ID.
        Same as getProperties().get('CreatorID')
        """
        return self._props.get(PROP_CREATOR_ID)

    def getName(self):
        """
        Retrieve human readable context name.
        Same as getProperties().get('Name')
        """
        return self._props.get(PROP_NAME)

    def isContainer(self):
        """
        Utility method to read context property PROP_IS_CONTAINER.
        Executing resume or suspend command on a container causes all its children to resume or suspend.
        @return value of PROP_IS_CONTAINER.
        """
        return self._props.get(PROP_IS_CONTAINER)

    def hasState(self):
        """
        Utility method to read context property PROP_HAS_STATE.
        Only context that has a state can be resumed or suspended.
        @return value of PROP_HAS_STATE.
        """
        return self._props.get(PROP_HAS_STATE)

    def canSuspend(self):
        """
        Utility method to read context property PROP_CAN_SUSPEND.
        Value 'true' means suspend command is supported by the context,
        however the method does not check that the command can be executed successfully in
        the current state of the context. For example, the command still can fail if context is
        already suspended.
        @return value of PROP_CAN_SUSPEND.
        """
        return self._props.get(PROP_CAN_SUSPEND)

    def canResume(self, mode):
        """
        Utility method to read a 'mode' bit in context property PROP_CAN_RESUME.
        Value 'true' means resume command is supported by the context,
        however the method does not check that the command can be executed successfully in
        the current state of the context. For example, the command still can fail if context is
        already resumed.
        @param mode - resume mode, see RM_*.
        @return value of requested bit of PROP_CAN_RESUME.
        """
        b = self._props.get(PROP_CAN_RESUME) or 0
        return (b & (1 << mode)) != 0

    def canCount(self, mode):
        """
        Utility method to read a 'mode' bit in context property PROP_CAN_COUNT.
        Value 'true' means resume command with count other then 1 is supported by the context,
        however the method does not check that the command can be executed successfully in
        the current state of the context. For example, the command still can fail if context is
        already resumed.
        @param mode - resume mode, see RM_*.
        @return value of requested bit of PROP_CAN_COUNT.
        """
        b = self._props.get(PROP_CAN_COUNT) or 0
        return (b & (1 << mode)) != 0

    def canTerminate(self):
        """
        Utility method to read context property PROP_CAN_TERMINATE.
        Value 'true' means terminate command is supported by the context,
        however the method does not check that the command can be executed successfully in
        the current state of the context. For example, the command still can fail if context is
        already exited.
        @return value of PROP_CAN_SUSPEND.
        """
        return self._props.get(PROP_CAN_TERMINATE)

    def getRCGroup(self):
        """
        Utility method to read context property PROP_RC_GROUP -
        context ID of a run control group that contains the context.
        Members of same group are always suspended and resumed together:
        resuming/suspending a context resumes/suspends all members of the group.
        @return value of PROP_RC_GROUP.
        """
        return self._props.get(PROP_RC_GROUP)

    def getState(self, done):
        """
        Send a command to retrieve current state of a context.
        @param done - command result call back object.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

    def suspend(self, done):
        """
        Send a command to suspend a context.
        Also suspends children if context is a container.
        @param done - command result call back object.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

#    def resume(self, mode, count, done):
#        """
#        Send a command to resume a context.
#        Also resumes children if context is a container.
#        @param mode - defines how to resume the context, see RM_*.
#        @param count - if mode implies stepping, defines how many steps to perform.
#        @param done - command result call back object.
#        @return pending command handle, can be used to cancel the command.
#        """
#        raise NotImplementedError("Abstract method")

    def resume(self, mode, count, params, done):
        """
        Send a command to resume a context.
        Also resumes children if context is a container.
        @param mode - defines how to resume the context, see RM_*.
        @param count - if mode implies stepping, defines how many steps to perform.
        @param params - resume parameters, for example, step range definition, see RP_*.
        @param done - command result call back object.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

    def terminate(self, done):
        """
        Send a command to terminate a context.
        @param done - command result call back object.
        @return pending command handle, can be used to cancel the command.
        """
        raise NotImplementedError("Abstract method")

class RunControlListener(object):
    "Service events listener interface."
    def contextAdded(self, contexts):
        """
        Called when new contexts are created.
        @param contexts - array of new context properties.
        """
        pass
    def contextChanged(self, contexts):
        """
        Called when a context properties changed.
        @param contexts - array of new context properties.
        """
        pass
    def contextRemoved(self, context_ids):
        """
        Called when contexts are removed.
        @param context_ids - array of removed context IDs.
        """
        pass
    def contextSuspended(self, context, pc, reason, params):
        """
        Called when a thread is suspended.
        @param context - ID of a context that was suspended.
        @param pc - program counter of the context, can be null.
        @param reason - human readable description of suspend reason.
        @param params - additional, target specific data about suspended context.
        """
        pass
    def contextResumed(self, context):
        """
        Called when a thread is resumed.
        @param context - ID of a context that was resumed.
        """
        pass
    def containerSuspended(self, context, pc, reason, params, suspended_ids):
        """
        Called when target simultaneously suspends multiple threads in a container
        (process, core, etc.).

        @param context - ID of a context responsible for the event. It can be container ID or
        any one of container children, for example, it can be thread that hit "suspend all" breakpoint.
        Client expected to move focus (selection) to this context.
        @param pc - program counter of the context.
        @param reason - suspend reason, see REASON_*.
        @param params - additional target specific data about context state, see STATE_*.
        @param suspended_ids - full list of all contexts that were suspended.
        """
        pass
    def containerResumed(self, context_ids):
        """
        Called when target simultaneously resumes multiple threads in a container (process,
        core, etc.).

        @param context_ids - full list of all contexts that were resumed.
        """
        pass
    def contextException(self, context, msg):
        """
        Called when an exception is detected in a target thread.
        @param context - ID of a context that caused an exception.
        @param msg - human readable description of the exception.
        """
        pass
