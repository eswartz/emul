/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.services;

import com.windriver.tcf.api.protocol.IService;
import com.windriver.tcf.api.protocol.IToken;

/**
 * This is optional service that can be implemented by a peer. 
 * If implemented, the service can be used for testing of the peer and
 * communication channel functionality and reliability.
 */

public interface IDiagnostics extends IService {

    public static final String NAME = "Diagnostics";
    
    public IToken echo(String s, DoneEcho done);
    
    public interface DoneEcho {
        public void doneEcho(IToken token, Throwable error, String s);
    }

    public IToken getTestList(DoneGetTestList done);
    
    public interface DoneGetTestList {
        public void doneGetTestList(IToken token, Throwable error, String[] list);
    }
    
    public IToken runTest(String s, DoneRunTest done);
    
    public interface DoneRunTest {
        public void doneRunTest(IToken token, Throwable error, String context_id);
    }

    public IToken cancelTest(String context_id, DoneCancelTest done);
    
    public interface DoneCancelTest {
        public void doneCancelTest(IToken token, Throwable error);
    }
    
    public IToken getSymbol(String context_id, String symbol_name, DoneGetSymbol done);
    
    public interface DoneGetSymbol {
        public void doneGetSymbol(IToken token, Throwable error, ISymbol symbol);
    }
    
    public interface ISymbol {
        public String getSectionName();
        public Number getValue();
        public boolean isUndef();
        public boolean isCommon();
        public boolean isGlobal();
        public boolean isLocal();
        public boolean isAbs();
    }
}
