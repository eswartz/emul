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

from tcf import errors, channel
from tcf.services import memory
from tcf.channel.Command import Command

class Range(object):
    offs = 0
    size = 0
    stat = 0
    msg = None
    def __cmp__(self, o):
        if self.offs < o.offs: return -1
        if self.offs > o.offs: return +1
        return 0

class MemoryErrorReport(errors.ErrorReport, memory.MemoryError, memory.ErrorOffset):
    def __init__(self, msg, attrs, addr, ranges):
        super(MemoryErrorReport, self).__init__(msg, attrs)
        if ranges is None:
            self.ranges = None
        else:
            self.ranges = []
            for m in ranges:
                r = Range()
                x = m.get(memory.ErrorOffset.RANGE_KEY_ADDR)
                if isinstance(x, str):
                    y = int(x)
                else:
                    y = x
                r.offs = y - addr
                r.size = m.get(memory.ErrorOffset.RANGE_KEY_SIZE)
                r.stat = m.get(memory.ErrorOffset.RANGE_KEY_STAT)
                r.msg = errors.toErrorString(m.get(memory.ErrorOffset.RANGE_KEY_MSG))
                assert r.offs >= 0
                assert r.size >= 0
                self.ranges.append(r)
            self.ranges.sort()

    def getMessage(self, offset):
        if self.ranges is None: return None
        l = 0
        h = len(self.ranges) - 1
        while l <= h:
            n = (l + h) / 2
            r = self.ranges[n]
            if r.offs > offset:
                h = n - 1
            elif offset >= r.offs + r.size:
                l = n + 1
            else:
                return r.msg
        return None

    def getStatus(self, offset):
        if self.ranges is None: return memory.ErrorOffset.BYTE_UNKNOWN
        l = 0
        h = len(self.ranges) - 1
        while l <= h:
            n = (l + h) / 2
            r = self.ranges[n]
            if r.offs > offset:
                h = n - 1
            elif offset >= r.offs + r.size:
                l = n + 1
            else:
                return r.stat
        return memory.ErrorOffset.BYTE_UNKNOWN


class MemContext(memory.MemoryContext):
    def __init__(self, service, props):
        super(MemContext, self).__init__(props)
        self.service = service

    def fill(self, addr, word_size, value, size, mode, done):
        service = self.service
        id = self.getID()
        done = service._makeCallback(done)
        class FillCommand(MemoryCommand):
            def __init__(self):
                super(FillCommand, self).__init__(service,
                        "fill", (id, addr, word_size, size, mode, value))
            def done(self, error, args):
                e = None
                if error:
                    e = memory.MemoryError(error.message)
                else:
                    assert len(args) == 2
                    e = self.toMemoryError(args[0], args[1])
                done.doneMemory(self.token, e)
        return FillCommand().token

    def get(self, addr, word_size, buf, offs, size, mode, done):
        service = self.service
        id = self.getID()
        done = service._makeCallback(done)
        class GetCommand(MemoryCommand):
            def __init__(self):
                super(GetCommand, self).__init__(service,
                        "get", (id, addr, word_size, size, mode))
            def done(self, error, args):
                e = None
                if error:
                    e = memory.MemoryError(error.message)
                else:
                    assert len(args) == 3
                    bytes = channel.toByteArray(args[0])
                    assert len(bytes) <= size
                    buf[offs:offs+len(bytes)] = bytes
                    e = self.toMemoryError(args[1], args[2])
                done.doneMemory(self.token, e)
        return GetCommand().token

    def set(self, addr, word_size, buf, offs, size, mode, done):
        service = self.service
        id = self.getID()
        done = service._makeCallback(done)
        class SetCommand(MemoryCommand):
            def __init__(self):
                super(SetCommand, self).__init__(service,
                        "set", (id, addr, word_size, size, mode, bytearray(buf[offs:offs:size])))
            def done(self, error, args):
                e = None
                if error:
                    e = memory.MemoryError(error.message)
                else:
                    assert len(args) == 2
                    e = self.toMemoryError(args[1], args[2])
                done.doneMemory(self.token, e)
        return SetCommand().token

class MemoryProxy(memory.MemoryService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def getContext(self, context_id, done):
        done = self._makeCallback(done)
        service = self
        class GetContextCommand(Command):
            def __init__(self):
                super(GetContextCommand, self).__init__(service.channel, service, "getContext", (context_id,))
            def done(self, error, args):
                ctx = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: ctx = MemContext(service, args[1])
                done.doneGetContext(self.token, error, ctx)
        return GetContextCommand().token

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

    def addListener(self, listener):
        l = ChannelEventListener(self, listener)
        self.channel.addEventListener(self, l)
        self.listeners[listener] = l

    def removeListener(self, listener):
        l = self.listeners.get(listener)
        if l:
            del self.listeners[listener]
            self.channel.removeEventListener(self, l)

class MemoryCommand(Command):
    def __init__(self, service, cmd, args):
        super(MemoryCommand, self).__init__(service.channel, service, cmd, args)
    def toMemoryError(self, addr, data, ranges):
        if data is None: return None
        code = data.get(errors.ERROR_CODE)
        cmd = self.getCommandString()
        if len(cmd) > 72: cmd = cmd[0:72] + "..."
        e = MemoryErrorReport(
                "TCF command exception:\nCommand: %s\nException: %s\nError code: " % (
                    cmd, self.toErrorString(data), code),
                map, addr, ranges)
        caused_by = data.get(errors.ERROR_CAUSED_BY)
        if caused_by is not None: e.caused_by = self.toError(caused_by, False)
        return e


class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "contextAdded":
                assert len(args) == 1
                self.listener.contextAdded(_toContextArray(args[0]))
            elif name == "contextChanged":
                assert len(args) == 1
                self.listener.contextChanged(_toContextArray(args[0]))
            elif name == "contextRemoved":
                assert len(args) == 1
                self.listener.contextRemoved(args[0])
            elif name == "memoryChanged":
                assert len(args) == 2
                self.listener.memoryChanged(args[0], _toAddrArray(args[1]), _toSizeArray(args[1]))
            else:
                raise IOError("Memory service: unknown event: " + name);
        except Exception as x:
            self.service.channel.terminate(x)


def _toContextArray(o):
    if o is None: return None
    ctx = []
    for m in o: ctx.append(MemContext(m))
    return ctx

def _toSizeArray(o):
    if o is None: return None
    a = []
    for m in o:
        sz = m.get("size", 0)
        a.append(sz)
    return a

def _toAddrArray(o):
    if o is None: return None
    a = []
    for m in o:
        a.append(m.get("addr"))
    return a
