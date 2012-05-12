package v9t9.common.machine;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;


/**
 * Track a series of changes to a set of registers.
 * @author ejs
 *
 */
public class SimpleRegisterWriteTracker extends BaseRegisterWriteTracker {
	private Map<Integer, Integer> changes = new TreeMap<Integer, Integer>(); 
	
	public SimpleRegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
		super(access, baseReg, regbits);
	}

	public SimpleRegisterWriteTracker(IRegisterAccess access) {
		super(access);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.BaseRegisterWriteTracker#clearChanges()
	 */
	@Override
	public void clearChanges() {
		synchronized (changes) {
			changes.clear();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.BaseRegisterWriteTracker#record(int, int)
	 */
	@Override
	protected void record(int reg, int value) {
		synchronized (changes) {
			changes.put(reg, value);
		}
	}
	
	/**
	 * Get the changes, sorted by register key.  Only the last
	 * change to each register is mentioned.
	 * 
	 * Caller may modify
	 * but cannot own the list.
	 * @return the changes
	 */
	public Map<Integer, Integer> getChanges() {
		return changes;
	}
}