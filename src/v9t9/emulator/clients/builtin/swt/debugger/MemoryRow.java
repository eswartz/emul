/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;


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
		range.writeByte(getAddress() + column, byt);
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