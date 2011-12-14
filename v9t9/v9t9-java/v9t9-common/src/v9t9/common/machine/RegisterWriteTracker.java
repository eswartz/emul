package v9t9.common.machine;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;


/**
 * Track a series of changes to a set of registers
 * @author ejs
 *
 */
public class RegisterWriteTracker {
	private final IRegisterAccess access;
	private Map<Integer, Integer> changes = new TreeMap<Integer, Integer>(); 
	private IRegisterWriteListener registerWriteListener;
	private final BitSet regbits;
	private final int baseReg;
	
	public RegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
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
					
					synchronized (RegisterWriteTracker.this) {
						synchronized (changes) {
							changes.put(reg, value);
						}
					}
				}
				
			};
			
			synchronized (changes) {
				changes.clear();
			}
			access.addWriteListener(registerWriteListener);
		}
	}
	
	/**
	 * Get the changes, sorted by integer key;
	 * caller may modify
	 * @return the changes
	 */
	public Map<Integer, Integer> getChanges() {
		return changes;
	}
	
	public void removeRegisterListener() {
		if (registerWriteListener != null) {
			access.removeWriteListener(registerWriteListener);
			registerWriteListener = null;
		}
	}
}