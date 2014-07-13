/*
  TargetWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.ITargetWord;

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
		entry.setTargetWord(this);
		this.hostPc = -1;
		if (entry != null)
			setName(entry.getName());
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
	@Override
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
