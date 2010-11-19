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
package org.eclipse.tm.internal.tcf.debug.model;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.services.IMemoryMap.MemoryRegion;

public class TCFMemoryRegion implements MemoryRegion, Comparable<TCFMemoryRegion> {

    private final Map<String,Object> props;

    public final BigInteger addr;
    public final BigInteger size;

    public TCFMemoryRegion(Map<String,Object> props) {
        this.props = props;
        Number addr = (Number)props.get(IMemoryMap.PROP_ADDRESS);
        Number size = (Number)props.get(IMemoryMap.PROP_SIZE);
        this.addr = addr == null || addr instanceof BigInteger ? (BigInteger)addr : new BigInteger(addr.toString());
        this.size = size == null || size instanceof BigInteger ? (BigInteger)size : new BigInteger(size.toString());
    }

    public Number getAddress() {
        return addr;
    }

    public Number getSize() {
        return size;
    }

    public Number getOffset() {
        return (Number)props.get(IMemoryMap.PROP_OFFSET);
    }

    public String getFileName() {
        return (String)props.get(IMemoryMap.PROP_FILE_NAME);
    }

    public String getSectionName() {
        return (String)props.get(IMemoryMap.PROP_SECTION_NAME);
    }

    public int getFlags() {
        Number n = (Number)props.get(IMemoryMap.PROP_FLAGS);
        if (n != null) return n.intValue();
        return 0;
    }

    public Map<String,Object> getProperties() {
        return props;
    }

    public int compareTo(TCFMemoryRegion r) {
        if (addr == null && r.addr == null) return 0;
        if (addr == null) return -1;
        if (r.addr == null) return +1;
        return addr.compareTo(r.addr);
    }
}
