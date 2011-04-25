/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.model;

import java.math.BigInteger;

import org.eclipse.tm.tcf.services.ILineNumbers;

/**
 * Objects of this class represent a mapping between an address and source code area.
 */
public class TCFSourceRef {
    public String context_id;
    public int address_size;
    public BigInteger address;
    public ILineNumbers.CodeArea area;
    public Throwable error;

    public String toString() {
        StringBuffer bf = new StringBuffer();
        bf.append('[');
        bf.append(context_id);
        bf.append(',');
        bf.append(address_size);
        bf.append(',');
        bf.append(address);
        bf.append(',');
        bf.append(area);
        bf.append(',');
        bf.append(error);
        bf.append(']');
        return bf.toString();
    }
}
