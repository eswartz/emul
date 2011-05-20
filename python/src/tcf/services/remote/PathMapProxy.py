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

from tcf.services import pathmap
from tcf.channel.Command import Command

class PathMapProxy(pathmap.PathMapService):
    def __init__(self, channel):
        self.channel = channel

    def get(self, done):
        done = self._makeCallback(done)
        service = self
        class GetCommand(Command):
            def __init__(self):
                super(GetCommand, self).__init__(service.channel, service, "get", None)
            def done(self, error, args):
                map = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: map = _toPathMap(args[1])
                done.doneGet(self.token, error, map)
        return GetCommand().token

    def set(self, map, done):
        if isinstance(map, pathmap.PathMapRule) or isinstance(map, dict):
            map = (map,)
        done = self._makeCallback(done)
        service = self
        class SetCommand(Command):
            def __init__(self):
                super(SetCommand, self).__init__(service.channel, service, "set", (map,))
            def done(self, error, args):
                if not error:
                    assert len(args) == 1
                    error = self.toError(args[0])
                done.doneSet(self.token, error)
        return SetCommand().token


def _toPathMap(o):
    if o is None: return None
    return map(_toPathMapRule, o)

def _toPathMapRule(o):
    if o is None: return None
    return pathmap.PathMapRule(o)
