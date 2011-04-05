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
from tcf.services import runcontrol
from tcf.channel.Command import Command

class RunContext(runcontrol.RunControlContext):
    def __init__(self, service, props):
        super(RunContext, self).__init__(props)
        self.service = service

    def getState(self, done):
        service = self.service
        id = self.getID()
        class GetStateCommand(Command):
            def __init__(self):
                super(GetStateCommand, self).__init__(service.channel, service, "getState", (id,))
            def done(self, error, args):
                susp = False
                pc = None
                reason = None
                map = None
                if not error:
                    assert len(args) == 5
                    error = self.toError(args[0])
                    susp = args[1]
                    if args[2]: pc = str(args[2])
                    reason = args[3]
                    map = args[4]
                done.doneGetState(self.token, error, susp, pc, reason, map)
        return GetStateCommand().token

#    def resume(self, mode, count, done):
#        return self._command("resume", [self.getID(), mode, count], done)

    def resume(self, mode, count, params, done):
        if not params:
            return self._command("resume", (self.getID(), mode, count), done)
        else:
            return self._command("resume", (self.getID(), mode, count, params), done)

    def suspend(self, done):
        return self._command("suspend", (self.getID(),), done)

    def terminate(self, done):
        return self._command("terminate", (self.getID(),), done)

    def _command(self, cmd, args, done):
        service = self.service
        class RCCommand(Command):
            def __init__(self, cmd, args):
                super(RCCommand, self).__init__(service.channel, service, cmd, args)
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneCommand(self.token, error)
        return RCCommand(cmd, args).token


class ChannelEventListener(channel.EventListener):
    def __init__(self, service, listener):
        self.service = service
        self.listener = listener
    def event(self, name, data):
        try:
            args = channel.fromJSONSequence(data)
            if name == "contextSuspended":
                assert len(args) == 4
                self.listener.contextSuspended(args[0], args[1], args[2], args[3])
            elif name == "contextResumed":
                assert len(args) == 1
                self.listener.contextResumed(args[0])
            elif name == "contextAdded":
                assert len(args) == 1
                self.listener.contextAdded(args[0])
            elif name == "contextChanged":
                assert len(args) == 1
                self.listener.contextChanged(args[0])
            elif name == "contextRemoved":
                assert len(args) == 1
                self.listener.contextRemoved(args[0])
            elif name == "contextException":
                assert len(args) == 2
                self.listener.contextException(args[0], args[1])
            elif name == "containerSuspended":
                assert len(args) == 5
                self.listener.containerSuspended(args[0], args[1], args[2], args[3], args[4])
            elif name == "containerResumed":
                assert len(args) == 1
                self.listener.containerResumed(args[0])
            else:
                raise IOError("RunControl service: unknown event: " + name);
        except exceptions.Exception as x:
            self.service.channel.terminate(x)

class RunControlProxy(runcontrol.RunControlService):
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
                    if args[1]: ctx = RunContext(service, args[1])
                done.doneGetContext(self.token, error, ctx)
        return GetContextCommand().token

    def getChildren(self, parent_context_id, done):
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
