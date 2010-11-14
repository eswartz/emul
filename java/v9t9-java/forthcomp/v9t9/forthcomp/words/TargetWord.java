/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public abstract class TargetWord extends BaseWord implements ITargetWord {

	protected final DictEntry entry;
	private int hostPc;

	/**
	 * 
	 */
	public TargetWord(DictEntry entry) {
		this.entry = entry;
		this.hostPc = -1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getName()
	 */
	public String getName() {
		return entry.getName();
	}
	
	/**
	 * @return the hostPc
	 */
	public int getHostDp() {
		return hostPc;
	}
	/**
	 * @param hostPc the hostPc to set
	 */
	public void setHostDp(int hostPc) {
		this.hostPc = hostPc;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entry.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getEntry()
	 */
	public DictEntry getEntry() {
		return entry;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public final boolean isImmediate() {
		return entry.isImmediate();
	}
	
}
