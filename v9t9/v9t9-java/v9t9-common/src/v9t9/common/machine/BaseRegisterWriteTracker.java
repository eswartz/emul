/*
  BaseRegisterWriteTracker.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import java.util.BitSet;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;


/**
 * Track a series of changes to a set of registers.
 * @author ejs
 *
 */
public abstract class BaseRegisterWriteTracker {
	private final IRegisterAccess access;
	private IRegisterWriteListener registerWriteListener;
	private final BitSet regbits;
	private final int baseReg;
	

	/**
	 * @param access
	 * @return
	 */
	protected static BitSet getRegSet(IRegisterAccess access) {
		BitSet bs = new BitSet();
		bs.set(0, access.getRegisterCount());
		return bs;
	}
	
	public BaseRegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
		this.access = access;
		this.baseReg = baseReg;
		this.regbits = regbits;
	}

	public BaseRegisterWriteTracker(IRegisterAccess access) {
		this.access = access;
		this.baseReg = access.getFirstRegister();
		this.regbits = getRegSet(access);
	}
	
	public void addRegisterListener() {
		if (registerWriteListener == null) {
			registerWriteListener = new IRegisterWriteListener() {
	
				/* (non-Javadoc)
				 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
				 */
				@Override
				public void registerChanged(int reg, int value) {
					if (reg < baseReg || !regbits.get(reg - baseReg))
						return;
					
					synchronized (BaseRegisterWriteTracker.this) {
						record(reg, value);
					}
				}
				
			};
			
			clearChanges();
			access.addWriteListener(registerWriteListener);
		}
	}
	
	/**
	 * Forget all changes.  
	 * @param reg
	 * @param value
	 */
	abstract public void clearChanges();

	/**
	 * Record a change.  The receiver is synchronized.
	 * @param reg
	 * @param value
	 */
	abstract protected void record(int reg, int value);
	
	public void removeRegisterListener() {
		if (registerWriteListener != null) {
			access.removeWriteListener(registerWriteListener);
			registerWriteListener = null;
		}
	}
}