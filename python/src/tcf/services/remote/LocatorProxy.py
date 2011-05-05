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

import exceptions
from tcf import protocol, peer, channel
from tcf.services import locator
from tcf.channel.Command import Command

class Peer(peer.TransientPeer):
    def __init__(self, parent, attrs):
        super(Peer, self).__init__(attrs)
        self.parent = parent
    def openChannel(self):
        assert protocol.isDispatchThread()
        c = self.parent.openChannel()
        c.redirect(self.getID())
        return c

class ChannelEventListener(channel.EventListener):
    def __init__(self, proxy):
        self.proxy = proxy
        self.channel = proxy.channel
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "peerAdded":
                assert len(args) == 1
                peer = Peer(self.channel.getRemotePeer(), args[0])
                if self.proxy.peers.get(peer.getID()):
                    protocol.log("Invalid peerAdded event", exceptions.Exception())
                    return
                self.proxy.peers[peer.getID()] = peer
                for l in self.proxy.listeners:
                    try:
                        l.peerAdded(peer)
                    except exceptions.Exception as x:
                        protocol.log("Unhandled exception in Locator listener", x)
            elif name == "peerChanged":
                assert len(args) == 1
                m = args[0]
                if not m: raise exceptions.Exception("Locator service: invalid peerChanged event - no peer ID")
                peer = self.proxy.peers.get(m.get(peer.ATTR_ID))
                if not peer: return
                self.proxy.peers[peer.getID()] = peer
                for l in self.proxy.listeners:
                    try:
                        l.peerChanged(peer)
                    except exceptions.Exception as x:
                        protocol.log("Unhandled exception in Locator listener", x)
            elif name == "peerRemoved":
                assert len(args) == 1
                id = args[0]
                peer = self.proxy.peers.get(id)
                if not peer: return
                del self.proxy.peers[id]
                for l in self.proxy.listeners:
                    try:
                        l.peerRemoved(id)
                    except exceptions.Exception as x:
                        protocol.log("Unhandled exception in Locator listener", x)
            elif name == "peerHeartBeat":
                assert len(args) == 1
                id = args[0]
                peer = self.proxy.peers.get(id)
                if not peer: return
                for l in self.proxy.listeners:
                    try:
                        l.peerHeartBeat(id)
                    except exceptions.Exception as x:
                        protocol.log("Unhandled exception in Locator listener", x)
            else:
                raise exceptions.IOError("Locator service: unknown event: " + name)
        except exceptions.Exception as x:
            self.channel.terminate(x)

class LocatorProxy(locator.LocatorService):
    def __init__(self, channel):
        self.channel = channel;
        self.peers = {}
        self.listeners = []
        self.get_peers_done = False
        self.event_listener = ChannelEventListener(self)
        channel.addEventListener(self, self.event_listener)

    def getPeers(self):
        return self.peers

    def redirect(self, peer, done):
        done = self._makeCallback(done)
        service = self
        class RedirectCommand(Command):
            def __init__(self):
                super(RedirectCommand, self).__init__(service.channel, service, "redirect", [peer])
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneRedirect(self.token, error)
        return RedirectCommand().token

    def sync(self, done):
        done = self._makeCallback(done)
        service = self
        class SyncCommand(Command):
            def __init__(self):
                super(SyncCommand, self).__init__(service.channel, service, "sync", None)
            def done(self, error, args):
                if error: service.channel.terminate(error)
                done.doneSync(self.token)
        return SyncCommand().token

    def addListener(self, listener):
        self.listeners.add(listener)
        if not self.get_peers_done:
            service = self
            class GetPeersCommand(Command):
                def __init__(self):
                    super(GetPeersCommand, self).__init__(service.channel, service, "getPeers", None)
                def done(self, error, args):
                    if not error:
                        assert len(args) == 2
                        error = self.toError(args[0])
                    if error:
                        protocol.log("Locator error", error)
                        return
                    c = args[1]
                    if c:
                        for m in c:
                            id = m.get(peer.ATTR_ID)
                            if service.peers.get(id): continue;
                            peer = Peer(service.channel.getRemotePeer(), m)
                            service.peers[id] = peer
                            for l in service.listeners:
                                try:
                                    l.peerAdded(peer)
                                except exceptions.Exception as x:
                                    protocol.log("Unhandled exception in Locator listener", x)
            self.get_peers_done = True

    def removeListener(self, listener):
        self.listeners.remove(listener)
