package v9t9.common.machine;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;


/**
 * Track a series of changes to a set of registers.
 * @author ejs
 *
 */
public class RegisterWriteTracker {
	private final IRegisterAccess access;
	private List<Long> changes = new ArrayList<Long>(1024); 
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
							long ent = ((long) reg) << 32;
							ent |= (value & 0xffffffffL);
							changes.add(ent);
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
	 * Get the changes, as they occurred, in order.  Caller may modify
	 * but cannot own the list.
	 * @return the changes
	 */
	public List<Long> getChanges() {
		return changes;
	}
	
	/**
	 * Get the changes, sorted by register key.  Only the last
	 * change to each register is mentioned.
	 *  Caller may modify.
	 * @return the changes
	 */
	public synchronized Map<Integer, Integer> getChangeMapAndReset() {
		synchronized (changes) {
			Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
			for (Long ent : changes) {
				map.put((int) (ent >> 32), (int)(ent & 0xffffffff));
			}
			changes.clear();
			return map;
		}
	}
	
	public void removeRegisterListener() {
		if (registerWriteListener != null) {
			access.removeWriteListener(registerWriteListener);
			registerWriteListener = null;
		}
	}
}