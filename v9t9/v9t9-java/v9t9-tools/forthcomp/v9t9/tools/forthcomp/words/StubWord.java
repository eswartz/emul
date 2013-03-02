/*
  StubWord.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
