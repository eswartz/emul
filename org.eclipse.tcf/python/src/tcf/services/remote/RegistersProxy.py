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
from tcf.services import registers
from tcf.channel import toByteArray
from tcf.channel.Command import Command

class Context(registers.RegistersContext):
    def __init__(self, service, props):
        super(Context, self).__init__(props)
        self.service = service

    def getNamedValues(self):
        return _toValuesArray(self._props.get(registers.PROP_VALUES))

    def get(self, done):
        service = self.service
        done = service._makeCallback(done)
        id = self.getID()
        class GetCommand(Command):
            def __init__(self):
                super(GetCommand, self).__init__(service.channel, service, "get", (id,))
            def done(self, error, args):
                val = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    val = toByteArray(args[1])
                done.doneGet(self.token, error, val)
        return GetCommand().token

    def set(self, value, done):
        service = self.service
        done = service._makeCallback(done)
        id = self.getID()
        binary = bytearray(value)
        class SetCommand(Command):
            def __init__(self):
                super(SetCommand, self).__init__(service.channel, service, "set", (id, binary))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneSet(self.token, error)
        return SetCommand().token

    def search(self, filter, done):
        service = self.service
        done = service._makeCallback(done)
        id = self.getID()
        class SearchCommand(Command):
            def __init__(self):
                super(SearchCommand, self).__init__(service.channel, service, "search", (id, filter))
            def done(self, error, args):
                paths = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    paths = args[1]
                done.doneSearch(self.token, error, paths)
        return SearchCommand().token

class RegistersProxy(registers.RegistersService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def getChildren(self, parent_context_id, done):
        done = self._makeCallback(done)
        service = self
        class GetChildrenCommand(Command):
            def __init__(self):
                super(GetChildrenCommand, self).__init__(service.channel, service, "getChildren", (parent_context_id,))
            def done(self, error, args):
                contexts = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    contexts = args[1]
                done.doneGetChildren(self.token, error, contexts)
        return GetChildrenCommand().token

    def getContext(self, id, done):
        done = self._makeCallback(done)
        service = self
        class GetContextCommand(Command):
            def __init__(self):
                super(GetContextCommand, self).__init__(service.channel, service, "getContext", (id,))
            def done(self, error, args):
                ctx = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: ctx = Context(service, args[1])
                done.doneGetContext(self.token, error, ctx)
        return GetContextCommand().token

    def getm(self, locs, done):
        done = self._makeCallback(done)
        service = self
        class GetMCommand(Command):
            def __init__(self):
                super(GetMCommand, self).__init__(service.channel, service, "getm", (locs,))
            def done(self, error, args):
                val = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    val = toByteArray(args[1])
                done.doneGet(self.token, error, val)
        return GetMCommand().token

    def setm(self, locs, value, done):
        done = self._makeCallback(done)
        service = self
        binary = bytearray(value)
        class SetMCommand(Command):
            def __init__(self):
                super(SetMCommand, self).__init__(service.channel, service, "setm", (locs, binary))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneSet(self.token, error)
        return SetMCommand().token

    def addListener(self, listener):
        l = ChannelEventListener(self, listener)
        self.channel.addEventListener(self, l)
        self.listeners[listener] = l

    def removeListener(self, listener):
        l = self.listeners.get(listener)
        if l:
            del self.listeners[listener]
            self.channel.removeEventListener(self, l)

class NamedValueInfo(registers.NamedValue):
    def __init__(self, m):
        desc = m.get("Description")
        name = m.get("Name")
        value = toByteArray(m.get("Value"))
        super(NamedValueInfo, self).__init__(value, name, desc)

def _toValuesArray(o):
    if o is None: return None
    arr = []
    for m in o:
        arr.append(NamedValueInfo(m))
    return arr

class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "contextChanged":
                self.listener.contextChanged()
            elif name == "registerChanged":
                assert len(args) == 1
                self.listener.registerChanged(args[0])
            else:
                raise IOError("Registers service: unknown event: " + name);
        except Exception as x:
            self.service.channel.terminate(x)
