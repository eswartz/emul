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
PathMap service manages file path translation across systems.
"""

from tcf import services

NAME = "PathMap"

# Path mapping rule property names.
# String, rule ID
PROP_ID = "ID"

# String, source, or compile-time file path
PROP_SOURCE = "Source"

# String, destination, or run-time file path
PROP_DESTINATION = "Destination"

# String
PROP_HOST = "Host"

# String, file access protocol, see PROTOCOL_*, default is regular file
PROP_PROTOCOL = "Protocol"

# PROP_PROTOCOL values.
# Regular file access using system calls
PROTOCOL_FILE = "file"

# File should be accessed using File System service on host
PROTOCOL_HOST = "host"

# File should be accessed using File System service on target
PROTOCOL_TARGET = "target"

class PathMapRule(object):
    """
    PathMapRule represents a single file path mapping rule.
    """
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return str(self._props)

    def __json__(self):
        return self._props

    def getProperties(self):
        """
        Get rule properties. See PROP_* definitions for property names.
        Context properties are read only, clients should not try to modify them.
        @return Map of rule properties.
        """
        return self._props

    def getID(self):
        """
        Get rule unique ID.
        Same as getProperties().get(PROP_ID)
        @return rule ID.
        """
        return self._props.get(PROP_ID)

    def getSource(self):
        """
        Get compile-time file path.
        Same as getProperties().get(PROP_SOURCE)
        @return compile-time file path.
        """
        return self._props.get(PROP_SOURCE)

    def getDestination(self):
        """
        Get run-time file path.
        Same as getProperties().get(PROP_DESTINATION)
        @return run-time file path.
        """
        return self._props.get(PROP_DESTINATION)

    def getHost(self):
        """
        Get host name of this rule.
        Same as getProperties().get(PROP_HOST)
        @return host name.
        """
        return self._props.get(PROP_HOST)

    def getProtocol(self):
        """
        Get file access protocol name.
        Same as getProperties().get(PROP_PROTOCOL)
        @return protocol name.
        """
        return self._props.get(PROP_PROTOCOL)


class PathMapService(services.Service):
    def getName(self):
        return NAME

    def get(self, done):
        """
        Retrieve file path mapping rules.

        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

    def set(self, map, done):
        """
        Set file path mapping rules.

        @param map - file path mapping rules.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        return NotImplementedError("Abstract method")

class DoneGet(object):
    """
    Client call back interface for get().
    """
    def doneGet(self, token, error, map):
        """
        Called when file path mapping retrieval is done.
        @param error - error description if operation failed, None if succeeded.
        @param map - file path mapping data.
        """
        pass

class DoneSet(object):
    """
    Client call back interface for set().
    """
    def doneSet(self, token, error):
        """
        Called when file path mapping transmission is done.
        @param error - error description if operation failed, None if succeeded.
        @param map - memory map data.
        """
        pass
