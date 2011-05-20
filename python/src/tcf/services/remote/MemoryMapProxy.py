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

from tcf import channel
from tcf.services import memorymap
from tcf.channel.Command import Command

class MemoryMapProxy(memorymap.MemoryMapService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def get(self, id, done):
        done = self._makeCallback(done)
        service = self
        class GetCommand(Command):
            def __init__(self):
                super(GetCommand, self).__init__(service.channel, service, "get", (id,))
            def done(self, error, args):
                map = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: map = _toMemoryMap(args[1])
                done.doneGet(self.token, error, map)
        return GetCommand().token

    def set(self, id, map, done):
        if isinstance(map, memorymap.MemoryRegion) or isinstance(map, dict):
            map = (map,)
        done = self._makeCallback(done)
        service = self
        class SetCommand(Command):
            def __init__(self):
                super(SetCommand, self).__init__(service.channel, service, "set", (id, map))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneSet(self.token, error)
        return SetCommand().token

    def addListener(self, listener):
        l = ChannelEventListener(self, listener)
        self.channel.addEventListener(self, l)
        self.listeners[listener] = l

    def removeListener(self, listener):
        l = self.listeners.get(listener)
        if l:
            del self.listeners[listener]
            self.channel.removeEventListener(self, l)

class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "changed":
                assert len(args) == 1
                self.listener.changed(args[0])
            else:
                raise IOError("MemoryMap service: unknown event: " + name);
        except Exception as x:
            self.service.channel.terminate(x)


def _toMemoryMap(o):
    if o is None: return None
    return map(_toMemoryRegion, o)

def _toMemoryRegion(o):
    if o is None: return None
    return memorymap.MemoryRegion(o)
