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
from tcf.services import processes
from tcf.channel.Command import Command

class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "exited":
                assert len(args) == 2
                self.listener.exited(args[0], args[1])
            else:
                raise IOError("Processes service: unknown event: " + name);
        except exceptions.Exception as x:
            self.service.channel.terminate(x)

class ProcessesProxy(processes.ProcessesService):
    def __init__(self, channel):
        self.channel = channel
        self.listeners = {}

    def addListener(self, listener):
        l = ChannelEventListener(self, listener)
        self.channel.addEventListener(self, l)
        self.listeners[listener] = l

    def removeListener(self, listener):
        l = self.listeners.get(listener)
        if l:
            del self.listeners[listener]
            self.channel.removeEventListener(self, l)

    def getChildren(self, parent_context_id, attached_only, done):
        service = self
        class GetChildrenCommand(Command):
            def __init__(self):
                super(GetChildrenCommand, self).__init__(service.channel, service, 
                        "getChildren", (parent_context_id, attached_only))
            def done(self, error, args):
                contexts = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    contexts = args[1]
                done.doneGetChildren(self.token, error, contexts)
        return GetChildrenCommand().token

    def getContext(self, context_id, done):
        service = self
        class GetContextCommand(Command):
            def __init__(self):
                super(GetContextCommand, self).__init__(service.channel, service, "getContext", (context_id,))
            def done(self, error, args):
                ctx = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: ctx = ProcessContext(service, args[1])
                done.doneGetContext(self.token, error, ctx)
        return GetContextCommand().token

    def getEnvironment(self, done):
        service = self
        class GetEnvCommand(Command):
            def __init__(self):
                super(GetEnvCommand, self).__init__(service.channel, service, "getEnvironment", None)
            def done(self, error, args):
                env = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    env = _toEnvMap(args[1])
                done.doneGetEnvironment(self.token, error, env)
        return GetEnvCommand().token

    def start(self, directory, file, command_line, environment, attach, done):
        service = self
        env = _toEnvStringArray(environment)
        class StartCommand(Command):
            def __init__(self):
                super(StartCommand, self).__init__(service.channel, service,
                        "start", (directory, file, command_line, env, attach))
            def done(self, error, args):
                ctx = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: ctx = ProcessContext(service, args[1])
                done.doneStart(self.token, error, ctx)
        return StartCommand().token

    def getSignalList(self, context_id, done):
        service = self
        class GetSignalsCommand(Command):
            def __init__(self):
                super(GetSignalsCommand, self).__init__(service.channel, service,
                        "getSignalList", (context_id,))
            def done(self, error, args):
                list = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    list = args[1]
                done.doneGetSignalList(self.token, error, list)
        return GetSignalsCommand().token

    def getSignalMask(self, context_id, done):
        service = self
        class GetSignalMaskCommand(Command):
            def __init__(self):
                super(GetSignalMaskCommand, self).__init__(service.channel, service,
                        "getSignalMask", (context_id,))
            def done(self, error, args):
                dont_stop = 0
                dont_pass = 0
                pending = 0
                if not error:
                    assert len(args) == 4
                    error = self.toError(args[0])
                    dont_stop, dont_pass, pending = args[1:3]
                done.doneGetSignalMask(self.token, error, dont_stop, dont_pass, pending)
        return GetSignalMaskCommand().token

    def setSignalMask(self, context_id, dont_stop, dont_pass, done):
        service = self
        class SetSignalMaskCommand(Command):
            def __init__(self):
                super(SetSignalMaskCommand, self).__init__(service.channel, service,
                        "setSignalMask", (context_id, dont_stop, dont_pass))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneCommand(self.token, error)
        return SetSignalMaskCommand().token

    def signal(self, context_id, signal, done):
        service = self
        class SignalCommand(Command):
            def __init__(self):
                super(SignalCommand, self).__init__(service.channel, service,
                        "signal", (context_id, signal))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneCommand(self.token, error)
        return SignalCommand().token

def _toEnvStringArray(map):
    arr = []
    if not map: return arr
    for name, value in map.items():
        arr.append("%s=%s" % (name, value))
    return arr

def _toEnvMap(arr):
    map = {}
    if not arr: return map
    for str in arr:
        i = str.find('=')
        if i >= 0: map[str[:i]] = str[i + 1:]
        else: map[str] = ""
    return map

class ProcessContext(processes.ProcessContext):
    def __init__(self, service, props):
        super(ProcessContext, self).__init__(props)
        self.service = service

    def attach(self, done):
        return self._command("attach", done)

    def detach(self, done):
        return self._command("detach", done)

    def terminate(self, done):
        return self._command("terminate", done)

    def _command(self, command, done):
        service = self.service
        id = self.getID()
        class _Command(Command):
            def __init__(self):
                super(_Command, self).__init__(service.channel, service,
                        command, (id,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneCommand(self.token, error)
        return _Command().token
