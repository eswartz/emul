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

from tcf.services import stacktrace
from tcf.channel.Command import Command

class StackTraceProxy(stacktrace.StackTraceService):
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

    def getContext(self, ids, done):
        done = self._makeCallback(done)
        service = self
        class GetContextCommand(Command):
            def __init__(self):
                super(GetContextCommand, self).__init__(service.channel, service, "getContext", (ids,))
            def done(self, error, args):
                ctxs = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[1])
                    ctxs = service.toContextArray(args[0])
                done.doneGetContext(self.token, error, ctxs)
        return GetContextCommand().token

    def toContextArray(self, ctxProps):
        if ctxProps is None: return None
        ctxs = []
        for props in ctxProps:
            ctxs.append(stacktrace.StackTraceContext(props))
        return ctxs
