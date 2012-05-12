package v9t9.common.machine;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Track a series of changes to a set of registers.
 * @author ejs
 *
 */
public class FullRegisterWriteTracker extends BaseRegisterWriteTracker {

	private List<Long> changes = new ArrayList<Long>(1024); 
	
	public FullRegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
		super(access, baseReg, regbits);
	}
	
	public FullRegisterWriteTracker(IRegisterAccess access) {
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
			long ent = ((long) reg) << 32;
			ent |= (value & 0xffffffffL);
			changes.add(ent);
		}
	}

	/**
	 * Get the changes, as they occurred, in order.  Caller may modify
	 * but cannot own the list.
	 * @return the changes, as <integer32: reg> | <integer32: value>
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
	public Map<Integer, Integer> getChangeMap() {
		synchronized (changes) {
			Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
			for (Long ent : changes) {
				map.put((int) (ent >> 32), (int)(ent & 0xffffffff));
			}
			return map;
		}
	}
}