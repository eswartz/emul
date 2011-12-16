# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. self program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies self distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

from tcf.services import sysmonitor
from tcf.channel.Command import Command

class SysMonitorProxy(sysmonitor.SysMonitorService):
    def __init__(self, channel):
        self.channel = channel

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
                    if args[1]: ctx = sysmonitor.SysMonitorContext(args[1])
                done.doneGetContext(self.token, error, ctx)
        return GetContextCommand().token

    def getCommandLine(self, id, done):
        done = self._makeCallback(done)
        service = self
        class GetCommandLineCommand(Command):
            def __init__(self):
                super(GetCommandLineCommand, self).__init__(service.channel, service, "getCommandLine", (id,))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = args[1]
                done.doneGetCommandLine(self.token, error, arr)
        return GetCommandLineCommand().token

    def getEnvironment(self, id, done):
        done = self._makeCallback(done)
        service = self
        class GetEnvironmentCommand(Command):
            def __init__(self):
                super(GetEnvironmentCommand, self).__init__(service.channel, service, "getEnvironment", (id,))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = args[1]
                done.doneGetCommandLine(self.token, error, arr)
        return GetEnvironmentCommand().token
