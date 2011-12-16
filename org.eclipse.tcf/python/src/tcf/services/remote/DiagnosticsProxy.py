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

import time
from tcf import errors
from tcf.services import diagnostics
from tcf.channel.Command import Command

class DiagnosticsProxy(diagnostics.DiagnosticsService):
    def __init__(self, channel):
        self.channel = channel

    def echo(self, s, done):
        done = self._makeCallback(done)
        service = self
        class EchoCommand(Command):
            def __init__(self):
                super(EchoCommand, self).__init__(service.channel, service, "echo", (s,))
            def done(self, error, args):
                str = None
                if not error:
                    assert len(args) == 1
                    str = args[0]
                done.doneEcho(self.token, error, str)
        return EchoCommand().token

    def echoFP(self, n, done):
        done = self._makeCallback(done)
        service = self
        class EchoFPCommand(Command):
            def __init__(self):
                super(EchoFPCommand, self).__init__(service.channel, service, "echoFP", (n,))
            def done(self, error, args):
                n = None
                if not error:
                    assert len(args) == 1
                    n = args[0]
                done.doneEchoFP(self.token, error, n)
        return EchoFPCommand().token

    def echoERR(self, err, done):
        map = None
        if isinstance(err, errors.ErrorReport):
            map = err.getAttributes()
        else:
            map = {
                errors.ERROR_TIME : int(time.time() * 1000),
                errors.ERROR_CODE : errors.TCF_ERROR_OTHER,
                errors.ERROR_FORMAT : err.message
            }
        done = self._makeCallback(done)
        service = self
        class EchoERRCommand(Command):
            def __init__(self):
                super(EchoERRCommand, self).__init__(service.channel, service, "echoERR", (map,))
            def done(self, error, args):
                err = None
                str = None
                if not error:
                    assert len(args) == 2
                    err = self.toError(args[0])
                    str = args[1]
                done.doneEchoERR(self.token, error, err, str)
        return EchoERRCommand().token

    def getTestList(self, done):
        done = self._makeCallback(done)
        service = self
        class GetTestListCommand(Command):
            def __init__(self):
                super(GetTestListCommand, self).__init__(service.channel, service, "getTestList", None)
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = args[1]
                done.doneGetTestList(self.token, error, arr)
        return GetTestListCommand().token

    def runTest(self, s, done):
        done = self._makeCallback(done)
        service = self
        class RunTestCommand(Command):
            def __init__(self):
                super(RunTestCommand, self).__init__(service.channel, service, "runTest", (s,))
            def done(self, error, args):
                str = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    str = args[1]
                done.doneRunTest(self.token, error, str)
        return RunTestCommand().token

    def cancelTest(self, s, done):
        done = self._makeCallback(done)
        service = self
        class CancelTestCommand(Command):
            def __init__(self):
                super(CancelTestCommand, self).__init__(service.channel, service, "cancelTest", (s,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneCancelTest(self.token, error)
        return CancelTestCommand().token

    def getSymbol(self, context_id, symbol_name, done):
        done = self._makeCallback(done)
        service = self
        class GetSymbolCommand(Command):
            def __init__(self):
                super(GetSymbolCommand, self).__init__(service.channel, service, "getSymbol", (context_id, symbol_name))
            def done(self, error, args):
                sym = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    sym = _toSymbol(args[1])
                done.doneGetSymbol(self.token, error, sym)
        return GetSymbolCommand().token

    def createTestStreams(self, inp_buf_size, out_buf_size, done):
        done = self._makeCallback(done)
        service = self
        class CreateTestStreamsCommand(Command):
            def __init__(self):
                super(CreateTestStreamsCommand, self).__init__(service.channel, service, "createTestStreams", (inp_buf_size, out_buf_size))
            def done(self, error, args):
                inp_id = None
                out_id = None
                if not error:
                    assert len(args) == 3
                    error = self.toError(args[0])
                    inp_id = args[1]
                    out_id = args[2]
                done.doneCreateTestStreams(self.token, error, inp_id, out_id)
        return CreateTestStreamsCommand().token

    def disposeTestStream(self, id, done):
        done = self._makeCallback(done)
        service = self
        class DisposeTestStreamCommand(Command):
            def __init__(self):
                super(DisposeTestStreamCommand, self).__init__(service.channel, service, "disposeTestStream", (id,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneDisposeTestStream(self.token, error)
        return DisposeTestStreamCommand().token

    def not_implemented_command(self, done):
        done = self._makeCallback(done)
        service = self
        class NotImplementedCommand(Command):
            def __init__(self):
                super(NotImplementedCommand, self).__init__(service.channel, service, "not implemented command", None)
            def done(self, error, args):
                done.doneNotImplementedCommand(self.token, error)
        return NotImplementedCommand().token

def _toSymbol(o):
    if o is None: return None
    return diagnostics.Symbol(o)
