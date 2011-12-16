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
TCF Disassembly service interface.
"""

from tcf import services

NAME = "Disassembly"

# The name of the instruction set architecture, String
CAPABILITY_ISA = "ISA"

# If true, simplified mnemonics are supported or requested, Boolean
CAPABILITY_SIMPLIFIED = "Simplified"

# If true, pseudo-instructions are supported or requested, Boolean
CAPABILITY_PSEUDO = "Pseudo"


# Instruction field properties
# The type of the instruction field. See FTYPE_*, String.
FIELD_TYPE = "Type"

# Value of the field for "String" and "Register" types, String.
FIELD_TEXT = "Text"

# Value of the field for "Address," "Displacement," or "Immediate" types, Number.
FIELD_VALUE = "Value"

# Context ID of the address space used with "Address" types, String.
FIELD_ADDRESS_SPACE = "AddressSpace"

# Instruction field types
FTYPE_STRING = "String"
FTYPE_Register = "Register"
FTYPE_ADDRESS = "Address"
FTYPE_DISPLACEMENT = "Displacement"
FTYPE_IMMEDIATE = "Immediate"


class DisassemblyService(services.Service):
    def getName(self):
        return NAME

    def getCapabilities(self, context_id, done):
        """
        Retrieve disassembly service capabilities a given context-id.
        @param context_id - a context ID, usually one returned by Run Control or Memory services.
        @param done - command result call back object.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def disassemble(self, context_id, addr, size, params, done):
        """
        Disassemble instruction code from a specified range of memory addresses, in a specified context.
        @param context_id - a context ID, usually one returned by Run Control or Memory services.
        @param addr - address of first instruction to disassemble.
        @param size - size in bytes of the address range.
        @param params - properties to control the disassembly output, an element of capabilities array, see getCapabilities.
        @param done - command result call back object.
        @return - pending command handle.
        """


class DoneGetCapabilities(object):
    """
    Call back interface for 'getCapabilities' command.
    """
    def doneGetCapabilities(self, token, error, capabilities):
        """
        Called when capabilities retrieval is done.
        @param token - command handle.
        @param error - error object or None.
        @param capabilities - array of capabilities, see CAPABILITY_* for contents of each array element.
        """
        pass

class DoneDisassemble(object):
    """
    Call back interface for 'disassemble' command.
    """
    def doneDisassemble(self, token, error, disassembly):
        """
        Called when disassembling is done.
        @param token - command handle.
        @param error - error object or None.
        @param disassembly - array of disassembly lines.
        """
        pass

class DisassemblyLine(object):
    """
    Represents a single disassembly line.
    """
    def __init__(self, addr, size, instruction):
        self.addr = addr
        self.size = size or 0
        self.instruction = instruction

    def getAddress(self):
        """
        @return instruction address.
        """
        return self.addr

    def getSize(self):
        """
        @return instruction size in bytes.
        """
        return self.size

    def getInstruction(self):
        """
        @return array of instruction fields, each field is a collection of field properties, see FIELD_*.
        """
        return self.instruction

    def __str__(self):
        instr = "".join(map(str, self.instruction))
        return "[%s %s %s]" % (self.addr, self.size, instr)
