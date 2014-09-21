/*
  MemoryRow.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import v9t9.common.memory.IMemoryEntry;


class MemoryRow {
	private int baseaddr;
	private final MemoryRange range;

	public MemoryRow(int baseaddr, MemoryRange range) {
		this.range = range;
		this.baseaddr = baseaddr;
	}

	public final int getAddress() {
		return baseaddr + range.getAddress();
	}

	public final int getByte(int column) {
		return range.readByte(getAddress() + column);
	}

	public final void putByte(int column, byte byt) {
		// write to entry directly to avoid banking issues
		int addr = getAddress() + column;
		IMemoryEntry entry = range.getEntry();
		entry.writeByte(addr, byt);
		entry.getDomain().fireWriteEvent(entry, addr, byt);
	}

	public final char getChar(int column) {
		int b = getByte(column) & 0xff;
		return b > 32 && b < 127 ? (char)b : '.';
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baseaddr;
		result = prime * result + (range == null ? 0 : range.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MemoryRow other = (MemoryRow) obj;
		if (baseaddr != other.baseaddr) {
			return false;
		}
		if (range == null) {
			if (other.range != null) {
				return false;
			}
		} else if (!range.equals(other.range)) {
			return false;
		}
		return true;
	}

	public MemoryRange getRange() {
		return range;
	}
	
}