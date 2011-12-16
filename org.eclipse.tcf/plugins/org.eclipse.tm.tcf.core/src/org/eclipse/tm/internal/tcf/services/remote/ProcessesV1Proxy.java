/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.remote;

import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcessesV1;

public class ProcessesV1Proxy extends ProcessesProxy implements IProcessesV1 {

    public ProcessesV1Proxy(IChannel channel) {
        super(channel);
    }

    public String getName() {
        return IProcessesV1.NAME;
    }

    public IToken start(String directory, String file,
            String[] command_line, Map<String,String> environment,
            Map<String,Object> params, final DoneStart done) {
        return new Command(channel, this,
                "start", new Object[]{ directory, file, command_line,
                toEnvStringArray(environment), params }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                ProcessContext ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new ProcessContextInfo((Map<String,Object>)args[1]);
                }
                done.doneStart(token, error, ctx);
            }
        }.token;
    }
}
