/*
  StubWord.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.ITargetWord;

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
	 * @see v9t9.tools.forthcomp.ITargetWord#getHostDp()
	 */
	@Override
	public int getHostDp() {
		return 0;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getName()
	 */
	public String getName() {
		return entry.getName();
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#getEntry()
	 */
	public DictEntry getEntry() {
		return entry;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.ITargetWord#setHostDp(int)
	 */
	public void setHostDp(int localDP) {
		// TODO Auto-generated method stub
		
	}

}
