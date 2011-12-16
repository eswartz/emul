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
from tcf.services import symbols
from tcf.channel.Command import Command


class Context(symbols.Symbol):
    def __init__(self, props):
        super(Context, self).__init__(props)
        self.value = channel.toByteArray(props.get(symbols.PROP_VALUE))

    def getValue(self):
        return self.value


class SymbolsProxy(symbols.SymbolsService):
    def __init__(self, channel):
        self.channel = channel

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
                    if args[1]: ctx = Context(args[1])
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

    def find(self, context_id, ip, name, done):
        done = self._makeCallback(done)
        service = self
        class FindCommand(Command):
            def __init__(self):
                super(FindCommand, self).__init__(service.channel, service, "find", (context_id, ip, name))
            def done(self, error, args):
                id = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    id = args[1]
                done.doneFind(self.token, error, id)
        return FindCommand().token

    def findByAddr(self, context_id, addr, done):
        done = self._makeCallback(done)
        service = self
        class FindByAddrCommand(Command):
            def __init__(self):
                super(FindByAddrCommand, self).__init__(service.channel, service, "findByAddr", (context_id, addr))
            def done(self, error, args):
                id = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    id = args[1]
                done.doneFind(self.token, error, id)
        return FindByAddrCommand().token

    def list(self, context_id, done):
        done = self._makeCallback(done)
        service = self
        class ListCommand(Command):
            def __init__(self):
                super(ListCommand, self).__init__(service.channel, service, "list", (context_id,))
            def done(self, error, args):
                lst = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    lst = args[1]
                done.doneList(self.token, error, lst)
        return ListCommand().token

    def findFrameInfo(self, context_id, address, done):
        done = self._makeCallback(done)
        service = self
        class FindFrameInfoCommand(Command):
            def __init__(self):
                super(FindFrameInfoCommand, self).__init__(service.channel, service, "findFrameInfo", (context_id, address))
            def done(self, error, args):
                address = None
                size = None
                fp_cmds = None
                reg_cmds = None
                if not error:
                    assert len(args) == 5
                    error = self.toError(args[0])
                    address, size, fp_cmds, reg_cmds = args[1:5]
                done.doneFindFrameInfo(self.token, error, address, size, fp_cmds, reg_cmds)
        return FindFrameInfoCommand().token
