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

from . import ProcessesProxy
from tcf.services import processes_v1
from tcf.channel.Command import Command

class ProcessesV1Proxy(ProcessesProxy.ProcessesProxy, processes_v1.ProcessesV1Service):
    def start(self, directory, file, command_line, environment, params, done):
        done = self._makeCallback(done)
        service = self
        env = ProcessesProxy._toEnvStringArray(environment)
        class StartCommand(Command):
            def __init__(self):
                super(StartCommand, self).__init__(service.channel, service,
                        "start", (directory, file, command_line, env, params))
            def done(self, error, args):
                ctx = None
                if not error:
                    assert len(args) == 2
                    error = self.toError(args[0])
                    if args[1]: ctx = ProcessesProxy.ProcessContext(service, args[1])
                done.doneStart(self.token, error, ctx)
        return StartCommand().token
