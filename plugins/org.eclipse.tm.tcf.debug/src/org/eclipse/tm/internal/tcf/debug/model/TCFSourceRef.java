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
import org.eclipse.tm.tcf.services.IMemory;

/**
 * Objects of this class represent a mapping between an address and source code area.
 */
public class TCFSourceRef {
    public IMemory.MemoryContext context;
    public BigInteger address;
    public ILineNumbers.CodeArea area;
    public Throwable error;
}
