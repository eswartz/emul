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
Memory service provides basic operations to read/write memory on a target.
"""

from tcf import services

NAME = "Memory"

# Context property names.
PROP_ID = "ID"                         # String, ID of the context, same as getContext command argument
PROP_PARENT_ID = "ParentID"            # String, ID of a parent context
PROP_PROCESS_ID = "ProcessID"          # String, process ID, see Processes service
PROP_BIG_ENDIAN = "BigEndian"          # Boolean, True if memory is big-endian
PROP_ADDRESS_SIZE = "AddressSize"      # Number, size of memory address in bytes
PROP_NAME = "Name"                     # String, name of the context, can be used for UI purposes
PROP_START_BOUND = "StartBound"        # Number, lowest address (inclusive) which is valid for the context
PROP_END_BOUND = "EndBound"            # Number, highest address (inclusive) which is valid for the context
PROP_ACCESS_TYPES = "AccessTypes"      # Array of String, the access types allowed for this context

# Values of "AccessTypes".
# Target system can support multiple different memory access types, like instruction and data access.
# Different access types can use different logic for address translation and memory mapping, so they can
# end up accessing different data bits, even if address is the same.
# Each distinct access type should be represented by separate memory context.
# A memory context can represent multiple access types if they are equivalent - all access same memory bits.
# Same data bits can be exposed through multiple memory contexts.
ACCESS_INSTRUCTION = "instruction"     # Context represent instructions fetch access
ACCESS_DATA = "data"                   # Context represents data access
ACCESS_IO = "io"                       # Context represents IO peripherals
ACCESS_USER = "user"                   # Context represents a user (e.g. application running in Linux) view to memory
ACCESS_SUPERVISOR = "supervisor"       # Context represents a supervisor (e.g. Linux kernel) view to memory
ACCESS_HYPERVISOR = "hypervisor"       # Context represents a hypervisor view to memory
ACCESS_VIRTUAL = "virtual"             # Context uses virtual addresses
ACCESS_PHYSICAL = "physical"           # Context uses physical addresses
ACCESS_CACHE = "cache"                 # Context is a cache
ACCESS_TLB = "tlb"                     # Context is a TLB memory


# Memory access mode:
# Carry on when some of the memory cannot be accessed and
# return MemoryError at the end if any of the bytes
# were not processed correctly.
MODE_CONTINUEONERROR = 0x1

# Memory access mode:
# Verify result of memory operations (by reading and comparing).
MODE_VERIFY = 0x2

class MemoryContext(object):
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Memory Context %s]" % self._props

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

    def isBigEndian(self):
        """
        Get memory endianness.
        @return True if memory is big-endian.
        """
        return self._props.get(PROP_BIG_ENDIAN, False)

    def getAddressSize(self):
        """
        Get memory address size.
        @return number of bytes used to store memory address value.
        """
        return self._props.get(PROP_ADDRESS_SIZE, 0)

    def getName(self):
        """
        Get memory context name.
        The name can be used for UI purposes.
        @return context name.
        """
        return self._props.get(PROP_NAME)

    def getStartBound(self):
        """
        Get lowest address (inclusive) which is valid for the context.
        @return lowest address.
        """
        return self._props.get(PROP_START_BOUND)

    def getEndBound(self):
        """
        Get highest address (inclusive) which is valid for the context.
        @return highest address.
        """
        return self._props.get(PROP_END_BOUND)

    def getAccessTypes(self):
        """
        Get the access types allowed for this context.
        @return collection of access type names.
        """
        return self._props.get(PROP_ACCESS_TYPES)

    def set(self, addr, word_size, buf, offs, size, mode, done):
        """
        Set target memory.
        If 'word_size' is 0 it means client does not care about word size.
        """
        raise NotImplementedError("Abstract method")

    def get(self, addr, word_size, buf, offs, size, mode, done):
        """
        Read target memory.
        """
        raise NotImplementedError("Abstract method")

    def fill(self, addr, word_size, value, size, mode, done):
        """
        Fill target memory with given pattern.
        'size' is number of bytes to fill.
        """
        raise NotImplementedError("Abstract method")

class DoneMemory(object):
    """
    Client call back interface for set(), get() and fill() commands.
    """
    def doneMemory(self, token, error):
        pass

class MemoryError(Exception):
    pass

class ErrorOffset(object):
    """
    ErrorOffset may be implemented by MemoryError object,
    which is returned by get, set and fill commands.

    get/set/fill () returns this exception when reading failed
    for some but not all bytes, and MODE_CONTINUEONERROR
    has been set in mode. (For example, when only part of the request
    translates to valid memory addresses.)
    Exception.getMessage can be used for generalized message of the
    possible reasons of partial memory operation.
    """
    # Error may have per byte information
    BYTE_VALID        = 0x00
    BYTE_UNKNOWN      = 0x01 # e.g. out of range
    BYTE_INVALID      = 0x02
    BYTE_CANNOT_READ  = 0x04
    BYTE_CANNOT_WRITE = 0x08

    RANGE_KEY_ADDR  = "addr"
    RANGE_KEY_SIZE  = "size"
    RANGE_KEY_STAT  = "stat"
    RANGE_KEY_MSG   = "msg"

    def getStatus(self, offset):
        raise NotImplementedError("Abstract method")

    def getMessage(self, offset):
        raise NotImplementedError("Abstract method")

class MemoryService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve context info for given context ID.

        @param id - context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, done):
        """
        Retrieve contexts available for memory commands.
        A context corresponds to an execution thread, process, address space, etc.
        A context can belong to a parent context. Contexts hierarchy can be simple
        plain list or it can form a tree. It is up to target agent developers to choose
        layout that is most descriptive for a given target. Context IDs are valid across
        all services. In other words, all services access same hierarchy of contexts,
        with same IDs, however, each service accesses its own subset of context's
        attributes and functionality, which is relevant to that service.

        @param parent_context_id - parent context ID. Can be None -
        to retrieve top level of the hierarchy, or one of context IDs retrieved
        by previous getChildren commands.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add memory service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove memory service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")

class MemoryListener(object):
    """
    Memory event listener is notified when memory context hierarchy
    changes, and when memory is modified by memory service commands.
    """

    def contextAdded(self, contexts):
        """
        Called when a new memory access context(s) is created.
        """
        pass

    def contextChanged(self, contexts):
        """
        Called when a memory access context(s) properties changed.
        """
        pass

    def contextRemoved(self, context_ids):
        """
        Called when memory access context(s) is removed.
        """
        pass

    def memoryChanged(self, context_id, addr, size):
        """
        Called when target memory content was changed and clients
        need to update themselves. Clients, at least, should invalidate
        corresponding cached memory data.
        Not every change is notified - it is not possible,
        only those, which are not caused by normal execution of the debuggee.
        'addr' and 'size' can be None if unknown.
        """
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
