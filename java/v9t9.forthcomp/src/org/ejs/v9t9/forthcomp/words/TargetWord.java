/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public abstract class TargetWord implements ITargetWord {

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
	 * @see org.ejs.v9t9.forthcomp.IWord#getName()
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
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getEntry()
	 */
	public DictEntry getEntry() {
		return entry;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public final boolean isImmediate() {
		return entry.isImmediate();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		if (getHostDp() >= 0 && !getEntry().isTargetOnly()) {
			hostContext.pushCall(getHostDp());
			hostContext.interpret(hostContext, targetContext);
		} else {
			throw hostContext.abort("cannot execute target word: " + entry.getName());
		}		
	}
}
