/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.services;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;

public final class TCFAddress implements IAddress {
    
    private final BigInteger addr;
    
    public TCFAddress(Number addr) {
        if (addr instanceof BigInteger) this.addr = (BigInteger)addr;
        else this.addr = new BigInteger(addr.toString(), 10);
    }

    public IAddress add(BigInteger i) {
        return new TCFAddress(addr.add(i));
    }

    public IAddress add(long l) {
        if (l == 0) return this;
        return new TCFAddress(addr.add(BigInteger.valueOf(l)));
    }

    public BigInteger distanceTo(IAddress a) {
        return a.getValue().subtract(addr);
    }

    public int getCharsNum() {
        // TODO don't know what getCharsNum() is supposed to return
        return 0;
    }

    public BigInteger getMaxOffset() {
        // TODO don't know what getMaxOffset() is supposed to return
        return null;
    }

    public int getSize() {
        // TODO don't know what getSize() is supposed to return
        return 0;
    }

    public BigInteger getValue() {
        return addr;
    }

    public boolean isMax() {
        return false;
    }

    public boolean isZero() {
        return addr.equals(BigInteger.ZERO);
    }

    public String toBinaryAddressString() {
        return toHexAddressString();
    }

    public String toHexAddressString() {
        return "0x" + toString(16);
    }

    public String toString(int radix) {
        return addr.toString(radix);
    }

    public int compareTo(Object o) {
        return addr.compareTo(((TCFAddress)o).addr);
    }
    
    public String toString() {
        return "[" + toHexAddressString() + "]";
    }
}
