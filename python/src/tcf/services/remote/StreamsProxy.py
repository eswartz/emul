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

from tcf.services import streams
from tcf import channel
from tcf.channel.Command import Command

class StreamsProxy(streams.StreamsService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def connect(self, stream_id, done):
        done = self._makeCallback(done)
        service = self
        class ConnectCommand(Command):
            def __init__(self):
                super(ConnectCommand, self).__init__(service.channel, service, "connect", (stream_id,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneConnect(self.token, error)
        return ConnectCommand().token

    def disconnect(self, stream_id, done):
        done = self._makeCallback(done)
        service = self
        class DisconnectCommand(Command):
            def __init__(self):
                super(DisconnectCommand, self).__init__(service.channel, service, "disconnect", (stream_id,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneDisconnect(self.token, error)
        return DisconnectCommand().token

    def eos(self, stream_id, done):
        done = self._makeCallback(done)
        service = self
        class EOSCommand(Command):
            def __init__(self):
                super(EOSCommand, self).__init__(service.channel, service, "eos", (stream_id,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneEOS(self.token, error)
        return EOSCommand().token

    def read(self, stream_id, size, done):
        done = self._makeCallback(done)
        service = self
        class ReadCommand(Command):
            def __init__(self):
                super(ReadCommand, self).__init__(service.channel, service, "read", (stream_id, size))
            def done(self, error, args):
                lost_size = 0
                data = None
                eos = False
                if not error:
                    assert len(args) == 4
                    data = channel.toByteArray(args[0])
                    error = self.toError(args[1])
                    lost_size = args[2]
                    eos = args[3]
                done.doneRead(self.token, error, lost_size, data, eos)
        return ReadCommand().token

    def subscribe(self, stream_type, listener, done):
        done = self._makeCallback(done)
        service = self
        class SubscribeCommand(Command):
            def __init__(self):
                super(SubscribeCommand, self).__init__(service.channel, service, "subscribe", (stream_type,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                if not error:
                    l = ChannelEventListener(service, listener)
                    service.listeners[listener] = l
                    service.channel.addEventListener(service, l)
                done.doneSubscribe(self.token, error)
        return SubscribeCommand().token

    def unsubscribe(self, stream_type, listener, done):
        done = self._makeCallback(done)
        service = self
        class UnsubscribeCommand(Command):
            def __init__(self):
                super(UnsubscribeCommand, self).__init__(service.channel, service, "unsubscribe", (stream_type,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                if not error:
                    l = service.listeners.pop(listener, None)
                    if l: service.channel.removeEventListener(service, l)
                done.doneUnsubscribe(self.token, error)
        return UnsubscribeCommand().token

    def write(self, stream_id, buf, offset, size, done):
        done = self._makeCallback(done)
        service = self
        binary = buf[offset:offset+size]
        class WriteCommand(Command):
            def __init__(self):
                super(WriteCommand, self).__init__(service.channel, service, "write", (stream_id, binary))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneWrite(self.token, error)
        return WriteCommand().token


class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "created":
                if len(args) == 3:
                    self.listener.created(args[0], args[1], args[2])
                else:
                    assert len(args) == 2
                    self.listener.created(args[0], args[1], None)
            elif name == "disposed":
                assert len(args) == 2
                self.listener.disposed(args[0], args[1])
            else:
                raise IOError("Streams service: unknown event: " + name);
        except Exception as x:
            self.service.channel.terminate(x)
