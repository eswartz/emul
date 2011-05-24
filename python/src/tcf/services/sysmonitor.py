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
If implemented, the service can be used for monitoring system activity and utilization.
It provides list of running processes, different process attributes like command line, environment, etc.,
and some resource utilization data. The service can be used by a client to provide functionality
similar to Unix 'top' utility or Windows 'Task Manager'.
"""

from tcf import services

NAME = "SysMonitor"

# Context property names.
# The TCF context ID"""
PROP_ID = "ID"

# The TCF parent context ID
PROP_PARENTID = "ParentID"

# Current working directory of the process
PROP_CWD = "CWD"

# The process's root directory (as set by chroot)
PROP_ROOT = "Root"

# User ID of the process owner
PROP_UID = "UID"

# Group ID of the process owner
PROP_UGID = "UGID"

# User name of the process owner
PROP_USERNAME = "UserName"

# Group name of the process owner
PROP_GROUPNAME = "GroupName"

# System process ID
PROP_PID = "PID"

# Executable file of the process
PROP_FILE = "File"

# One character from the string "RSDZTW"  where  R  is  running,  S  is
# sleeping  in  an  interruptible wait, D is waiting in uninterruptible
# disk sleep, Z is zombie, T is traced or stopped (on a signal), and  W
# is paging.
PROP_STATE = "State"

# System ID of the parent process
PROP_PPID = "PPID"

# The process group ID of the process
PROP_PGRP = "PGRP"

# The session ID of the process
PROP_SESSION = "Session"

# The tty the process uses
PROP_TTY = "TTY"

# The process group ID of the process which currently owns the tty that
# the process is connected to.
PROP_TGID = "TGID"

# ID of a process that has attached this process for tracing or debugging
PROP_TRACERPID = "TracerPID"

# The kernel flags word of the process. Details depend on the kernel
PROP_FLAGS = "Flags"

# The  number  of  minor  faults  the  process  has made which have not
# required loading a memory page from disk
PROP_MINFLT = "MinFlt"

# The number of minor faults that  the  process's  waited-for  children have made
PROP_CMINFLT = "CMinFlt"

# The  number  of major faults the process has made which have required
# loading a memory page from disk
PROP_MAJFLT = "MajFlt"

# The number of major faults that  the  process's  waited-for  children
# have made
PROP_CMAJFLT = "CMajFlt"

# The number of milliseconds that this process has been scheduled in user mode
PROP_UTIME = "UTime"

# The number of milliseconds that this process has been scheduled in kernel mode
PROP_STIME = "STime"

# The  number  of  jiffies that this process's waited-for children have
# been scheduled in user mode
PROP_CUTIME = "CUTime"

# The  number  of  jiffies that this process's waited-for children have
# been scheduled in user mode
PROP_CSTIME = "CSTime"

# The standard nice value
PROP_PRIORITY = "Priority"

# The nice value
PROP_NICE = "Nice"

# The time in milliseconds before the next SIGALRM is sent  to  the  process
# due to an interval timer
PROP_ITREALVALUE = "ITRealValue"

# The time in milliseconds the process started after system boot
PROP_STARTTIME = "StartTime"

# Virtual memory size in bytes
PROP_VSIZE = "VSize"

# Memory pages size in bytes
PROP_PSIZE = "PSize"

# Resident  Set  Size:  number of pages the process has in real memory,
# minus used for administrative purposes. This is  just  the  pages  which
# count  towards  text,  data,  or  stack space.  This does not include
# pages which have not been demand-loaded in, or which are swapped out
PROP_RSS = "RSS"

# Current  limit in bytes on the rss of the process
PROP_RLIMIT = "RLimit"

# The address above which program text can run
PROP_CODESTART = "CodeStart"

# The address below which program text can run
PROP_CODEEND = "CodeEnd"

# The address of the start of the stack
PROP_STACKSTART = "StackStart"

# The bitmap of pending signals
PROP_SIGNALS = "Signals"

# The bitmap of blocked signals
PROP_SIGBLOCK = "SigBlock"

# The bitmap of ignored signals
PROP_SIGIGNORE = "SigIgnore"

# The bitmap of caught signals
PROP_SIGCATCH = "SigCatch"

# This  is  the  "channel"  in which the process is waiting.  It is the
# address of a system call, and can be looked up in a name list  if  you
# need  a  textual  name
PROP_WCHAN = "WChan"

# Number of pages swapped
PROP_NSWAP = "NSwap"

# Cumulative NSwap for child processes
PROP_CNSWAP = "CNSwap"

# Signal to be sent to parent when this process exits
PROP_EXITSIGNAL = "ExitSignal"

# CPU number last executed on
PROP_PROCESSOR = "Processor"

# Real-time scheduling priority
PROP_RTPRIORITY = "RTPriority"

# Scheduling policy
PROP_POLICY = "Policy"


class SysMonitorContext(object):
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
        return "[Sys Monitor Context %s]" % self._props

    def getID(self):
        """
        Get context ID.
        Same as getProperties().get("ID")
        """
        return self._props.get(PROP_ID)

    def getParentID(self):
        """
        Get parent context ID.
        Same as getProperties().get("ParentID")
        """
        return self._props.get(PROP_PARENTID)

    def getPGRP(self):
        """
        Get process group ID.
        Same as getProperties().get("PGRP")
        """
        return self._props.get(PROP_PGRP, -1)

    def getPID(self):
        """
        Get process ID.
        Same as getProperties().get("PID")
        """
        return self._props.get(PROP_PID, -1)

    def getPPID(self):
        """
        Get process parent ID.
        Same as getProperties().get("PPID")
        """
        return self._props.get(PROP_PPID, -1)

    def getTGID(self):
        """
        Get process TTY group ID.
        Same as getProperties().get("TGID")
        """
        return self._props.get(PROP_TGID, -1)

    def getTracerPID(self):
        """
        Get tracer process ID.
        Same as getProperties().get("TracerPID")
        """
        return self._props.get(PROP_TRACERPID, -1)

    def getUID(self):
        """
        Get process owner user ID.
        Same as getProperties().get("UID")
        """
        return self._props.get(PROP_UID, -1)

    def getUserName(self):
        """
        Get process owner user name.
        Same as getProperties().get("UserName")
        """
        return self._props.get(PROP_USERNAME)

    def getUGID(self):
        """
        Get process owner user group ID.
        Same as getProperties().get("UGID")
        """
        return self._props.get(PROP_UGID, -1)

    def getGroupName(self):
        """
        Get process owner user group name.
        Same as getProperties().get("GroupName")
        """
        return self._props.get(PROP_GROUPNAME)

    def getState(self):
        """
        Get process state.
        Same as getProperties().get("State")
        """
        return self._props.get(PROP_STATE)

    def getVSize(self):
        """
        Get process virtual memory size in bytes.
        Same as getProperties().get("VSize")
        """
        return self._props.get(PROP_VSIZE, -1)

    def getPSize(self):
        """
        Get process virtual memory page size in bytes.
        Same as getProperties().get("PSize")
        """
        return self._props.get(PROP_PSIZE, -1)

    def getRSS(self):
        """
        Get number of memory pages in process resident set.
        Same as getProperties().get("RSS")
        """
        return self._props.get(PROP_RSS, -1)

    def getFile(self):
        """
        Get context executable file.
        Same as getProperties().get("File")
        """
        return self._props.get(PROP_FILE)

    def getRoot(self):
        """
        Get context current file system root.
        Same as getProperties().get("Root")
        """
        return self._props.get(PROP_ROOT)

    def getCurrentWorkingDirectory(self):
        """
        Get context current working directory.
        Same as getProperties().get("CWD")
        """
        return self._props.get(PROP_CWD)

    def getProperties(self):
        """
        Get all available context properties.
        @return Map 'property name' -> 'property value'
        """
        return self._props


class SysMonitorService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve context info for given context ID.
        
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

    def getCommandLine(self, id, done):
        """
        Get context command line.
        """
        raise NotImplementedError("Abstract method")

    def getEnvironment(self, id, done):
        """
        Get context environment variables.
        """
        raise NotImplementedError("Abstract method")


class DoneGetContext(object):
    """
    Client callback interface for getContext().
    """
    def doneGetContext(self, token, error, context):
        """
        Called when context data retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param context - context data.
        """
        pass

class DoneGetChildren(object):
    """
    Client callback interface for getChildren().
    """
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param error - error description if operation failed, null if succeeded.
        @param context_ids - array of available context IDs.
        """
        pass

class DoneGetCommandLine(object):
    def doneGetCommandLine(self, token, error, cmd_line):
        pass

class DoneGetEnvironment(object):
    def doneGetEnvironment(self, token, error, environment):
        pass
