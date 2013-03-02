/*
  GromDictEntry.java

  (c) 2011 Edward Swartz

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
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.forthcomp.words.TargetContext;

/**
 * @author ejs
 *
 */
public class GromDictEntry extends DictEntry {

	private int dictAddr;

	/**
	 * @param headerSize
	 * @param addr
	 * @param name
	 */
	public GromDictEntry(int headerSize, int addr, String name, int gp) {
		super(headerSize, addr, name);
		this.dictAddr = gp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.DictEntry#getContentAddr()
	 */
	@Override
	public int getContentAddr() {
		return addr;
	}
	/**
	 * @return the dictAddr
	 */
	public int getDictAddr() {
		return dictAddr;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.DictEntry#writeEntry(v9t9.forthcomp.words.TargetContext)
	 */
	@Override
	public void writeEntry(TargetContext targetContext) {
		byte[] ent = doWriteEntry(targetContext);
		
		// ignore link
		MemoryDomain domain = ((F99bTargetContext) targetContext).getGrom();
		int gp = dictAddr;
		for (int i = 0; i < ent.length - targetContext.getCellSize(); i++) {
			domain.writeByte(gp++, ent[i + targetContext.getCellSize()]);
		}
		
		// place xt
		targetContext.writeCell(ent, 0, getContentAddr());
		for (int i = 0; i < targetContext.getCellSize(); i++) {
			domain.writeByte(gp++, ent[i]);
		}

	}
}
