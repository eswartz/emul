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
from tcf import channel
from tcf.services import breakpoints
from tcf.channel.Command import Command

class BPCommand(Command):
    def __init__(self, service, cmd, cb, *args):
        super(BPCommand, self).__init__(service.channel, service, cmd, args)
        self.__cb = cb
    def done(self, error, args):
        if not error:
            assert len(args) == 1
            error = self.toError(args[0])
        self.__cb.doneCommand(self.token, error)


class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "status":
                assert len(args) == 2
                self.listener.breakpointStatusChanged(args[0], args[1])
            elif name == "contextAdded":
                assert len(args) == 1
                self.listener.contextAdded(args[0])
            elif name == "contextChanged":
                assert len(args) == 1
                self.listener.contextChanged(args[0])
            elif name == "contextRemoved":
                assert len(args) == 1
                self.listener.contextRemoved(args[0])
            else:
                raise IOError("Breakpoints service: unknown event: " + name);
        except exceptions.Exception as x:
            self.service.channel.terminate(x)

class BreakpointsProxy(breakpoints.BreakpointsService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def set(self, properties, done):
        return BPCommand(self, "set", done, properties).token

    def add(self, properties, done):
        return BPCommand(self, "add", done, properties).token

    def change(self, properties, done):
        return BPCommand(self, "change", done, properties).token

    def disable(self, ids, done):
        return BPCommand(self, "disable", done, ids).token

    def enable(self, ids, done):
        return BPCommand(self, "enable", done, ids).token

    def remove(self, ids, done):
        return BPCommand(self, "remove", done, ids).token

    def getIDs(self, done):
        service = self
        class GetIDsCommand(Command):
            def __init__(self):
                super(GetIDsCommand, self).__init__(service.channel, service, "getIDs", None)
            def done(self, error, args):
                ids = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    ids = args[1]
                done.doneGetIDs(self.token, error, ids)
        return GetIDsCommand().token

    def getProperties(self, id, done):
        service = self
        class GetPropertiesCommand(Command):
            def __init__(self):
                super(GetPropertiesCommand, self).__init__(service.channel, service, "getProperties", (id,))
            def done(self, error, args):
                map = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    map = args[1]
                done.doneGetProperties(self.token, error, map)
        return GetPropertiesCommand().token

    def getStatus(self, id, done):
        service = self
        class GetStatusCommand(Command):
            def __init__(self):
                super(GetStatusCommand, self).__init__(service.channel, service, "getStatus", (id,))
            def done(self, error, args):
                map = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    map = args[1]
                done.doneGetStatus(self.token, error, map)
        return GetStatusCommand().token

    def getCapabilities(self, id, done):
        service = self
        class GetCapabilitiesCommand(Command):
            def __init__(self):
                super(GetCapabilitiesCommand, self).__init__(service.channel, service, "getCapabilities", (id,))
            def done(self, error, args):
                map = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    map = args[1]
                done.doneGetCapabilities(self.token, error, map)
        return GetCapabilitiesCommand().token

    def addListener(self, listener):
        l = ChannelEventListener(self, listener)
        self.channel.addEventListener(self, l)
        self.listeners[listener] = l

    def removeListener(self, listener):
        l = self.listeners.get(listener)
        if l:
            del self.listeners[listener]
            self.channel.removeEventListener(self, l)
