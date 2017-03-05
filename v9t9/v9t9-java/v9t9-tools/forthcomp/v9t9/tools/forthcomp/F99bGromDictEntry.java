/*
  GromDictEntry.java

  (c) 2011-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 * 
 */
public class F99bGromDictEntry extends DictEntry {

	private int dictAddr;

	public F99bGromDictEntry(int headerSize, int addr, String name, int gp) {
		super(headerSize, addr, name);
		this.dictAddr = gp;
	}

	@Override
	public int getContentAddr() {
		return addr;
	}

	public int getDictAddr() {
		return dictAddr;
	}

	@Override
	public void writeEntry(ITargetContext targetContext) {
		byte[] ent = doWriteEntry(targetContext);

		// ignore link
		MemoryDomain domain = ((IGromTargetContext) targetContext).getGrom();
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
