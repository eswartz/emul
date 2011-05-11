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
Locator service uses transport layer to search for peers and to collect data about
peer's attributes and capabilities (services). Discovery mechanism depends on transport protocol
and is part of that protocol handler. Targets, known to other hosts, can be found through
remote instances of Locator service. Automatically discovered targets require no further
configuration. Additional targets can be configured manually.

Clients should use protocol.getLocator() to obtain local instance of locator,
then locator.getPeers() can be used to get list of available peers (hosts and targets).
"""

import exceptions
from tcf import services

# Peer data retention period in milliseconds.
DATA_RETENTION_PERIOD = 60 * 1000;

# Auto-configuration protocol version.
CONF_VERSION = '2'

# Auto-configuration command and response codes.
CONF_REQ_INFO = 1
CONF_PEER_INFO = 2
CONF_REQ_SLAVES = 3
CONF_SLAVES_INFO = 4

NAME = "Locator"

class LocatorService(services.Service):
    def getName(self):
        return NAME
    def getPeers(self):
        """
        Get map (ID -> IPeer) of available peers (hosts and targets).
        The method return cached (currently known to the framework) list of peers.
        The list is updated according to event received from transport layer
        """
        raise exceptions.NotImplementedError("Abstract method")
    def redirect(self, peer, done):
        """
        Redirect this service channel to given peer using this service as a proxy.
        @param peer - Peer ID or attributes map.
        """
        raise exceptions.NotImplementedError("Abstract method")
    def sync(self, done):
        """
        Call back after TCF messages sent to this target up to this moment are delivered.
        This method is intended for synchronization of messages
        across multiple channels.
        
        Note: Cross channel synchronization can reduce performance and throughput.
        Most clients don't need channel synchronization and should not call this method.
        
        @param done will be executed by dispatch thread after communication
        messages are delivered to corresponding targets.
        
        This is internal API, TCF clients should use module 'tcf.protocol'.
        """
        raise exceptions.NotImplementedError("Abstract method")
    def addListener(self, listener):
        "Add a listener for Locator service events."
        raise exceptions.NotImplementedError("Abstract method")
    def removeListener(self, listener):
        "Remove a listener for Locator service events."
        raise exceptions.NotImplementedError("Abstract method")

class DoneRedirect(object):
    def doneRedirect(self, token, error):
        pass

class DoneSync(object):
    def doneSync(self, token):
        pass

class LocatorListener(object):
    def peerAdded(self, peer):
        pass
    def peerChanged(self, peer):
        pass
    def peerRemoved(self, id):
        pass
    def peerHeartBeat(self, id):
        pass
