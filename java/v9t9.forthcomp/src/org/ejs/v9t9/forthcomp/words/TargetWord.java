/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public abstract class TargetWord implements ITargetWord {

	protected final DictEntry entry;


	/**
	 * 
	 */
	public TargetWord(DictEntry entry) {
		this.entry = entry;
		
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
	public boolean isImmediate() {
		return entry.isImmediate();
	}
}
