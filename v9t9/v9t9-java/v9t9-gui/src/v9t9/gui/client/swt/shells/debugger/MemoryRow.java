/*
  MemoryRow.java

  (c) 2009-2012 Edward Swartz

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
package v9t9.gui.client.swt.shells.debugger;


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
		range.getEntry().getDomain().writeByte(getAddress() + column, byt);
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