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
	
	public BaseRegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
		this.access = access;
		this.baseReg = baseReg;
		this.regbits = regbits;
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