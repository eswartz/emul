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
MemoryMap service provides information about executable modules (files) mapped (loaded) into target memory.
"""

from tcf import services

NAME = "MemoryMap"


# Memory region property names.
# Number, region address in memory
PROP_ADDRESS = "Addr"

# Number, region size
PROP_SIZE = "Size"

# Number, region offset in the file
PROP_OFFSET = "Offs"

# Boolean, true if the region represents BSS
PROP_BSS = "BSS"

# Number, region memory protection flags, see FLAG_*
PROP_FLAGS = "Flags"

# String, name of the file
PROP_FILE_NAME = "FileName"

# String, name of the object file section
PROP_SECTION_NAME = "SectionName"

# Memory region flags.
# Read access is allowed
FLAG_READ = 1

# Write access is allowed
FLAG_WRITE = 2

# Instruction fetch access is allowed
FLAG_EXECUTE = 4

class MemoryRegion(object):
    """Memory region object."""

    def __init__(self, props):
        self._props = props

    def getProperties(self):
        """
        Get region properties. See PROP_* definitions for property names.
        Properties are read only, clients should not try to modify them.
        @return Map of region properties.
        """
        self._props

    def getAddress(self):
        """
        Get memory region address.
        @return region address.
        """
        return self._props.get(PROP_ADDRESS)

    def getSize(self):
        """
        Get memory region size.
        @return region size.
        """
        return self._props.get(PROP_SIZE)

    def getOffset(self):
        """
        Get memory region file offset.
        @return file offset.
        """
        return self._props.get(PROP_OFFSET)

    def getFlags(self):
        """
        Get memory region flags.
        @return region flags.
        """
        return self._props.get(PROP_FLAGS, 0)

    def getFileName(self):
        """
        Get memory region file name.
        @return file name.
        """
        return self._props.get(PROP_FILE_NAME)

    def getSectionName(self):
        """
        Get memory region section name.
        @return section name.
        """
        return self._props.get(PROP_SECTION_NAME)

    def __json__(self):
        # This makes it serializable using JSON serializer
        return self._props

    def __repr__(self):
        return "MemoryRegion(%s)" % str(self._props)
    __str__ = __repr__

class MemoryMapService(services.Service):
    def getName(self):
        return NAME

    def get(self, id, done):
        """
        Retrieve memory map for given context ID.

        @param id - context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def set(self, id, map, done):
        """
        Set memory map for given context ID.

        @param id - context ID.
        @param map - memory map data.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add memory map event listener.
        @param listener - memory map event listener to add.
        """
        return NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove memory map event listener.
        @param listener - memory map event listener to remove.
        """
        return NotImplementedError("Abstract method")

class DoneGet(object):
    """
    Client call back interface for get().
    """
    def doneGet(self, token, error, map):
        """
        Called when memory map data retrieval is done.
        @param error - error description if operation failed, None if succeeded.
        @param map - memory map data.
        """
        pass

class DoneSet(object):
    """
    Client call back interface for set().
    """
    def doneSet(self, token, error):
        """
        Called when memory map set command is done.
        @param error - error description if operation failed, None if succeeded.
        """
        pass

class MemoryMapListener(object):
    """
    Service events listener interface.
    """
    def changed(self, context_id):
        """
        Called when context memory map changes.
        @param context_id - context ID.
        """
        pass
