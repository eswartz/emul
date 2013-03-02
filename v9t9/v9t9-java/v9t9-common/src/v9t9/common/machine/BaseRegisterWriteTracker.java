/*
  BaseRegisterWriteTracker.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
					if (!regbits.get(reg - baseReg))
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