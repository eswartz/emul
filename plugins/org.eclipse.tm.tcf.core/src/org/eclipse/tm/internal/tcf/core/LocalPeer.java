/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;

public class LocalPeer extends AbstractPeer {
    
    private static Map<String,String> createAttributes() {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(ATTR_ID, "TCFLocal");
        attrs.put(ATTR_NAME, "Local Peer");
        attrs.put(ATTR_OS_NAME, System.getProperty("os.name"));
        attrs.put(ATTR_TRANSPORT_NAME, "Loop");
        return attrs;
    }

    public LocalPeer() {
        super(createAttributes());
    }
}