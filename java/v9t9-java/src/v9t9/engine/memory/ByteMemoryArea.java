/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

/**
 * Byte memory decomposes word accesses into two byte accesses in big-endian order.
 * <p>
 * This area provides a basis for "traditional" memory which has a flat
 * array of bytes with read and/or write support.
 *
 * @author ejs
 */
public class ByteMemoryArea extends MemoryArea {
	public ByteMemoryArea() {
		super(0);
	}
	public ByteMemoryArea(int latency) {
		super(latency);
	}
    public ByteMemoryArea(int latency, byte[] memory) {
		super(latency);
		this.memory = memory;
		this.read = memory;
		this.write = memory;
	}
    
    
	/* actual memory for area, except for empty mem */
    public byte[] memory;

    /* if non-NULL, we can statically read */
    public byte[] read;

    /* if non-NULL, we can statically write */
    public byte[] write;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ByteMemoryArea)) {
			return false;
		}
        ByteMemoryArea area = (ByteMemoryArea)obj;
        return area.memory == memory && area.read == read && area.write == write;
    }

    @Override
	public boolean hasWriteAccess() {
        return read != null && write != null;
    }

    @Override
	public boolean hasReadAccess() {
        return read != null;
    }

    @Override
	protected int getSize() {
        return memory != null ? memory.length : 0;
    }
    
    @Override
	void copyFromBytes(byte[] array) {
    	if (memory == null) {
    		memory = new byte[array.length];
    		read = memory;
    	} 
        System.arraycopy(array, 0, memory, 0, Math.min(array.length, memory.length));
    }

    @Override
	void copyToBytes(byte[] array) {
        System.arraycopy(memory, 0, array, 0, Math.min(array.length, memory.length));
    }

    @Override
	public short readWord(MemoryEntry entry, int addr) {
    	return (short) ((readByte(entry, addr) << 8) | (readByte(entry, addr + 1) & 0xff));
    }

    @Override
	public byte readByte(MemoryEntry entry, int addr) {
        if (read != null) {
			return read[addr - entry.addr];
		} else {
			return 0;
		}
    }

    @Override
	public void writeWord(MemoryEntry entry, int addr, short val) {
    	writeByte(entry, addr, (byte) (val >> 8));
    	writeByte(entry, addr + 1, (byte) (val & 0xff));
    }

    @Override
	public void writeByte(MemoryEntry entry, int addr, byte val) {
        if (write != null) {
			write[addr - entry.addr] = val;
		}
    }

    public ByteMemoryAccess getReadMemoryAccess(MemoryEntry entry, int addr) {
    	return new ByteMemoryAccess(read, addr - entry.addr);
    }
}