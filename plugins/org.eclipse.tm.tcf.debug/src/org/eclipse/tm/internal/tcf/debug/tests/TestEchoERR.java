/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IDiagnostics;

class TestEchoERR implements ITCFTest, IDiagnostics.DoneEchoERR {

    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;

    private final Number[] numbers = {
            1,
            4,
            new BigDecimal("0.5")
    };

    private final String[] strings = {
            "",
            "abc",
            "a\u1134c",
            "a\u0003c",
    };

    private final String[] formats = {
            "",
            "{0}",
            "{0,number}",
            "{0,number,integer}",
            "{0,number,percent}",
            "{1}",
            "{0} abcde {1}",
            "{1} '' {0}",
            "{1} 'abcde{}' {1}",
    };

    private final LinkedList<ErrorReport> list = new LinkedList<ErrorReport>();

    TestEchoERR(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
    }

    public void start() {
        for (Number n : numbers) {
            for (String s : strings) {
                for (String f : formats) {
                    ArrayList<Object> params = new ArrayList<Object>();
                    params.add(n);
                    params.add(s);
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(IErrorReport.ERROR_TIME, new Long(System.currentTimeMillis()));
                    map.put(IErrorReport.ERROR_CODE, new Integer(IErrorReport.TCF_ERROR_OTHER));
                    map.put(IErrorReport.ERROR_FORMAT, f);
                    map.put(IErrorReport.ERROR_PARAMS, params);
                    map.put(s, s); // non-standard attribute
                    ErrorReport e = new ErrorReport("TCF error", map);
                    list.add(e);
                    diag.echoERR(e, this);
                }
            }
        }
    }

    public void doneEchoERR(IToken token, Throwable error, Throwable error_obj, String error_msg) {
        ErrorReport e = list.removeFirst();
        if (!test_suite.isActive(this)) return;
        Map<String,Object> map0 = e.getAttributes();
        Map<String,Object> map1 = null;

        if (error_obj instanceof IErrorReport) {
            map1 = ((IErrorReport)error_obj).getAttributes();
        }

        String msg = Command.toErrorString(map0);

        if (error instanceof IErrorReport && ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_INV_COMMAND) {
            // Older agent: the command is not available. Just exit the test.
            test_suite.done(this, null);
        }
        else if (error != null) {
            test_suite.done(this, error);
        }
        else if (!map0.equals(map1)) {
            test_suite.done(this, new Exception("Invalid error report attributes"));
        }
        else if (!msg.equals(error_msg)) {
            test_suite.done(this, new Exception("Invalid error report text"));
        }
        else if (list.size() == 0) {
            test_suite.done(this, null);
        }
    }
}
