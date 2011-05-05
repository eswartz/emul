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

from tcf.services import linenumbers
from tcf.channel.Command import Command

class LineNumbersProxy(linenumbers.LineNumbersService):

    def __init__(self, channel):
        self.channel = channel

    def mapToSource(self,  context_id, start_address, end_address, done):
        done = self._makeCallback(done)
        service = self
        class MapCommand(Command):
            def __init__(self):
                super(MapCommand, self).__init__(service.channel, service, 
                        "mapToSource", (context_id, start_address, end_address))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = _toCodeAreaArray(args[1])
                done.doneMapToSource(self.token, error, arr)
        return MapCommand().token

    def mapToMemory(self, context_id, file, line, column, done):
        done = self._makeCallback(done)
        service = self
        class MapCommand(Command):
            def __init__(self):
                super(MapCommand, self).__init__(service.channel, service, 
                        "mapToMemory", (context_id, file, line, column))
            def done(self, error, args):
                arr = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    arr = _toCodeAreaArray(args[1])
                done.doneMapToMemory(self.token, error, arr)
        return MapCommand().token

def _toCodeAreaArray(o):
    if not o: return None
    arr = []
    directory = None
    file = None
    for area in o:
        directory = area.get("Dir", directory)
        file = area.get("File", file)
        arr.append(linenumbers.CodeArea(directory, file,
                area.get("SLine", 0), area.get("SCol", 0),
                area.get("ELine", 0), area.get("ECol", 0),
                area.get("SAddr"), area.get("EAddr"),
                area.get("ISA", 0),
                area.get("IsStmt"), area.get("BasicBlock"),
                area.get("PrologueEnd"), area.get("EpilogueBegin")))
    return arr
