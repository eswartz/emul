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

from tcf.services import disassembly
from tcf.channel.Command import Command

class DisassemblyProxy(disassembly.DisassemblyService):
    def __init__(self, channel):
        self.channel = channel

    def getCapabilities(self, context_id, done):
        done = self._makeCallback(done)
        service = self
        class GetCapabilitiesCommand(Command):
            def __init__(self):
                super(GetCapabilitiesCommand, self).__init__(service.channel, service, "getCapabilities", (context_id,))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = args[1]
                done.doneGetCapabilities(self.token, error, arr)
        return GetCapabilitiesCommand().token

    def disassemble(self, context_id, addr, size, params, done):
        done = self._makeCallback(done)
        service = self
        class DisassembleCommand(Command):
            def __init__(self):
                super(DisassembleCommand, self).__init__(service.channel, service, "disassemble", (context_id, addr, size, params))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = _toDisassemblyArray(args[1])
                done.doneDisassemble(self.token, error, arr)
        return DisassembleCommand().token


def _toDisassemblyArray(o):
    if o is None: return None
    return map(_toDisassemblyLine, o)

def _toDisassemblyLine(m):
    addr = m.get("Address")
    size = m.get("Size")
    instruction = m.get("Instruction")
    return disassembly.DisassemblyLine(addr, size, instruction)
