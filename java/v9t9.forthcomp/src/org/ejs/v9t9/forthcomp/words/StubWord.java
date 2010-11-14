/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * Stub word, keeping track of low-level opcode uses for histograms.
 * Should never appear in real code. 
 * @author ejs
 *
 */
public class StubWord extends BaseWord implements ITargetWord {

	private DictEntry entry;

	/**
	 * 
	 */
	public StubWord(String name) {
		entry = new DictEntry(0, 0, name);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#getName()
	 */
	public String getName() {
		return entry.getName();
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#getEntry()
	 */
	public DictEntry getEntry() {
		return entry;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.ITargetWord#setHostDp(int)
	 */
	public void setHostDp(int localDP) {
		// TODO Auto-generated method stub
		
	}

}
