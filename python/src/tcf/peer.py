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
Both hosts and targets are represented by objects
implementing IPeer interface. A peer can act as host or
target depending on services it implements.
List of currently known peers can be retrieved by
calling Locator.getPeers()

A TCF agent houses one or more service managers. A service manager has a one or more
services to expose to the world. The service manager creates one or more peers
to represent itself, one for every access path the agent is
reachable by. For example, in agents accessible via TCP/IP, the
service manger would create a peer for every subnet it wants to participate in.
All peers of particular service manager represent identical sets of services.
"""

import os, exceptions, time, json
from tcf import protocol, transport, services
from tcf.services import locator

# Peer unique ID 
ATTR_ID = "ID"

# Unique ID of service manager that is represented by this peer 
ATTR_SERVICE_MANAGER_ID = "ServiceManagerID"

# Agent unique ID 
ATTR_AGENT_ID = "AgentID"

# Peer name 
ATTR_NAME = "Name"

# Name of the peer operating system 
ATTR_OS_NAME = "OSName"

# Transport name, for example TCP, SSL 
ATTR_TRANSPORT_NAME = "TransportName"

# If present, indicates that the peer can forward traffic to other peers 
ATTR_PROXY = "Proxy"

# Host DNS name or IP address 
ATTR_IP_HOST = "Host"

# Optional list of host aliases 
ATTR_IP_ALIASES = "Aliases"

# Optional list of host addresses 
ATTR_IP_ADDRESSES = "Addresses"

# IP port number, must be decimal number 
ATTR_IP_PORT = "Port"


class Peer(object):
    def __init__(self, attrs):
        self.attrs = attrs
    def getAttributes(self):
        """@return map of peer attributes"""
        return self.attrs

    def getID(self):
        """@return peer unique ID, same as getAttributes().get(ATTR_ID)"""
        return self.attrs.get(ATTR_ID)

    def getServiceManagerID(self):
        """@return service manager unique ID, same as getAttributes().get(ATTR_SERVICE_MANAGER_ID)"""
        assert protocol.isDispatchThread()
        return self.attrs.get(ATTR_SERVICE_MANAGER_ID)

    def getAgentID(self):
        """@return agent unique ID, same as getAttributes().get(ATTR_AGENT_ID)"""
        assert protocol.isDispatchThread()
        return self.attrs.get(ATTR_AGENT_ID)

    def getName(self):
        """@return peer name, same as getAttributes().get(ATTR_NAME)"""
        return self.attrs.get(ATTR_NAME)

    def getOSName(self):
        """@return agent OS name, same as getAttributes().get(ATTR_OS_NAME)"""
        return self.attrs.get(ATTR_OS_NAME)

    def getTransportName(self):
        """@return transport name, same as getAttributes().get(ATTR_TRANSPORT_NAME)"""
        return self.attrs.get(ATTR_TRANSPORT_NAME)

    def openChannel(self):
        """Open channel to communicate with this peer.
        Note: the channel is not fully open yet when this method returns.
        Its state is channel.STATE_OPENING.
        Protocol.ChannelOpenListener and IChannel.IChannelListener listeners will be called when
        the channel will change state to open or closed.
        Clients are supposed to register IChannel.IChannelListener right after calling openChannel(), or,
        at least, in same dispatch cycle. For example:
                 channel = peer.openChannel()
                 channel.addChannelListener(...)
        """
        raise exceptions.RuntimeError("Abstract method")


class TransientPeer(Peer):
    """
    Transient implementation of IPeer interface.
    Objects of this class are not tracked by Locator service.
    See AbstractPeer for IPeer objects that should go into the Locator table.
    """
    
    rw_attrs = {}

    def __init__(self, attrs):
        self.rw_attrs.update(attrs)
        # TODO readonly map
        ro_attrs = {}
        ro_attrs.update(self.rw_attrs)
        super(TransientPeer, self).__init__(ro_attrs)

    def openChannel(self):
        return transport.openChannel(self)

class LocalPeer(TransientPeer):
    """
    LocalPeer object represents local end-point of TCF communication channel.
    There should be exactly one such object in a TCF agent.
    The object can be used to open a loop-back communication channel that allows
    the agent to access its own services same way as remote services.
    Note that "local" here is relative to the agent, and not same as in "local host".
    """
    def __init__(self):
        super(LocalPeer, self).__init__(self.createAttributes())
    
    def createAttributes(self):
        attrs = {
            ATTR_ID : "TCFLocal",
            ATTR_SERVICE_MANAGER_ID : services.getServiceManagerID(),
            ATTR_AGENT_ID : protocol.getAgentID(),
            ATTR_NAME : "Local Peer",
            ATTR_OS_NAME : os.name,
            ATTR_TRANSPORT_NAME : "Loop"
        }
        return attrs;

class AbstractPeer(TransientPeer):
    """
    Abstract implementation of IPeer interface.
    Objects of this class are stored in Locator service peer table.
    The class implements sending notification events to Locator listeners.
    See TransientPeer for IPeer objects that are not stored in the Locator table.
    """

    last_heart_beat_time = 0

    def __init__(self, attrs):
        super(AbstractPeer, self).__init__(attrs)
        assert protocol.isDispatchThread()
        id = self.getID()
        assert id
        peers = locator.getLocator().getPeers()
        if isinstance(peers.get(id), RemotePeer):
            peers.get(id).dispose()
        assert not peers.has_key(id)
        peers[id] = self
        self.sendPeerAddedEvent()

    def dispose(self):
        assert protocol.isDispatchThread()
        id = self.getID()
        assert id
        peers = locator.getLocator().getPeers()
        assert peers.get(id) == self
        del peers[id]
        self.sendPeerRemovedEvent()

    def onChannelTerminated(self):
        # A channel to this peer was terminated:
        # not delaying next heart beat helps client to recover much faster.
        self.last_heart_beat_time = 0

    def updateAttributes(self, attrs):
        equ = True
        assert attrs.get(ATTR_ID) == self.rw_attrs.get(ATTR_ID)
        for key in self.rw_attrs.keys():
            if self.rw_attrs.get(key) != attrs.get(key):
                equ = False
                break
        for key in attrs.keys():
            if attrs.get(key) != self.rw_attrs.get(key):
                equ = False
                break
        timeVal = int(time.time())
        if not equ:
            self.rw_attrs.clear()
            self.rw_attrs.update(attrs)
            for l in locator.getListeners():
                try:
                    l.peerChanged(self)
                except exceptions.Exception as x:
                    protocol.log("Unhandled exception in Locator listener", x)
            try:
                args = [self.rw_attrs]
                protocol.sendEvent(locator.NAME, "peerChanged", json.dumps(args))
            except exceptions.IOError as x:
                protocol.log("Locator: failed to send 'peerChanged' event", x)
            self.last_heart_beat_time = timeVal
        elif self.last_heart_beat_time + locator.DATA_RETENTION_PERIOD / 4 < timeVal:
            for l in locator.getListeners():
                try:
                    l.peerHeartBeat(attrs.get(ATTR_ID))
                except exceptions.Exception as x:
                    protocol.log("Unhandled exception in Locator listener", x)
            try:
                args = [self.rw_attrs.get(ATTR_ID)]
                protocol.sendEvent(locator.NAME, "peerHeartBeat", json.dumps(args))
            except exceptions.IOError as x:
                protocol.log("Locator: failed to send 'peerHeartBeat' event", x)
            self.last_heart_beat_time = timeVal

    def sendPeerAddedEvent(self):
        for l in locator.getListeners():
            try:
                l.peerAdded(self)
            except exceptions.Exception as x:
                protocol.log("Unhandled exception in Locator listener", x)
        try:
            args = [self.rw_attrs]
            protocol.sendEvent(locator.NAME, "peerAdded", json.dumps(args))
        except exceptions.IOError as x:
            protocol.log("Locator: failed to send 'peerAdded' event", x)
        self.last_heart_beat_time = int(time.time())

    def sendPeerRemovedEvent(self):
        for l in locator.getListeners():
            try:
                l.peerRemoved(self.rw_attrs.get(ATTR_ID))
            except exceptions.Exception as x:
                protocol.log("Unhandled exception in Locator listener", x)
        try:
            args = [self.rw_attrs.get(ATTR_ID)]
            protocol.sendEvent(locator.NAME, "peerRemoved", json.dumps(args))
        except exceptions.IOError as x:
            protocol.log("Locator: failed to send 'peerRemoved' event", x)


class RemotePeer(AbstractPeer):
    """
    RemotePeer objects represent TCF agents that Locator service discovered on local network.
    This includes both local host agents and remote host agents.
    Note that "remote peer" means any peer accessible over network,
    it does not imply the agent is running on a "remote host".
    If an agent binds multiple network interfaces or multiple ports, it can be represented by
    multiple RemotePeer objects - one per each network address/port combination.
    RemotePeer objects life cycle is managed by Locator service.
    """

    last_update_time = 0

    def __init__(self, attrs):
        super(RemotePeer, self).__init__(attrs)
        self.last_update_time = int(time.time())

    def updateAttributes(self, attrs):
        super(RemotePeer, self).updateAttributes(attrs)
        self.last_update_time = int(time.time())

    def getLastUpdateTime(self):
        return self.last_update_time
