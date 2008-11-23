/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

/**
 * Byte memory decomposes word accesses into two byte accesses in big-endian order.
 *
 * @author ejs
 */
public class ByteMemoryArea extends MemoryArea {
	public ByteMemoryArea() {
		this(0);
	}
    public ByteMemoryArea(int latency) {
    	this.readByteLatency = this.readWordLatency = this.writeByteLatency = this.writeWordLatency = (byte) latency;
	}

	final static short getWord(byte[] memory, int offset, int addr) {
        /*
         * processor ignores word access on odd boundaries, and stores in
         * big-endian format
         */
        addr &= 0xfffe;
        return (short) (memory[addr + offset] << 8 | memory[addr + offset + 1] & 0xff);
    }

    final static void putWord(byte[] memory, int offset, int addr, short val) {
        /*
         * processor ignores word access on odd boundaries, and stores in
         * big-endian format
         */
        addr &= 0xfffe;
        memory[addr + offset] = (byte) (val >> 8);
        memory[addr + offset + 1] = (byte) (val & 0xff);
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
        return 
        area.areaReadByte == areaReadByte
        &&  area.areaWriteByte == areaWriteByte
        &&  area.areaReadWord == areaReadWord
        &&  area.areaWriteWord == areaWriteWord
        &&  area.memory == memory;
    }

    public static class DefaultAreaHandlers implements AreaReadByte, AreaWriteByte {
        /* (non-Javadoc)
         * @see v9t9.MemoryArea.AreaReadWord#read(v9t9.MemoryArea, int)
         */
        public byte readByte(MemoryArea area, int address) {
            return area.flatReadByte(address);
        }
        
        /* (non-Javadoc)
         * @see v9t9.MemoryArea.AreaWriteByte#write(v9t9.MemoryArea, int, byte)
         */
        public void writeByte(MemoryArea area, int address, byte val) {
            area.flatWriteByte(address, val);
        }
    }
    
    public static ByteMemoryArea newDefaultArea() {
        ByteMemoryArea area = new ByteMemoryArea();
        DefaultAreaHandlers handlers = new DefaultAreaHandlers();
        area.areaReadByte = handlers;
        area.areaWriteByte = handlers;
        return area;
    }
    
    @Override
	public MemoryArea copy() {
    	
        ByteMemoryArea area = new ByteMemoryArea();
        area.readByteLatency = this.readByteLatency;
        area.readWordLatency = this.readWordLatency;
        area.writeByteLatency = this.writeByteLatency;
        area.writeWordLatency = this.writeWordLatency;

        area.areaReadByte = this.areaReadByte;
        area.areaReadWord = this.areaReadWord;
        area.areaWriteByte = this.areaWriteByte;
        area.areaWriteWord = this.areaWriteWord;
        area.memory = this.memory;
        area.offset = this.offset;
        area.read = this.read;
        area.write = this.write;
        
        area.entry = this.entry;
        return area;
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
	int getSize() {
        return memory != null ? memory.length : 0;
    }
    
    @Override
	void copyFromBytes(byte[] array) {
        System.arraycopy(array, 0, memory, 0, Math.min(array.length, memory.length));
    }

    @Override
	void copyToBytes(byte[] array) {
        System.arraycopy(memory, 0, array, 0, Math.min(array.length, memory.length));
    }

    /*
     * "Flat" memory access bypasses any semantics and just tries to
     * read readable memory and write writeable memory.
     */
    @Override
	public final short flatReadWord(int addr) {
        if (read != null) {
			return ByteMemoryArea.getWord(read, offset, (addr & AREASIZE - 2));
		} else {
			return 0;
		}
    }

    @Override
	public final byte flatReadByte(int addr) {
        if (read != null) {
			return read[offset + (addr & AREASIZE - 1)];
		} else {
			return 0;
		}
    }

    @Override
	public final void flatWriteWord(int addr, short val) {
        if (write != null) {
			putWord(write, offset, (addr & MemoryArea.AREASIZE - 2), val);
		}
    }

    @Override
	public final void flatWriteByte(int addr, byte val) {
        if (write != null) {
			write[offset + (addr & MemoryArea.AREASIZE - 1)] = val;
		}
    }

    public ByteMemoryAccess getReadMemoryAccess(int addr) {
    	return new ByteMemoryAccess(read, offset + (addr & AREASIZE - 1));
    }
    
    /*
     *  All of these safely and laboriously access memory in the way
     *  God intended.
     */
    @Override
	final short readWord(int addr) {
        if (areaReadWord != null) {
			return areaReadWord.readWord(this, addr);
		} else if (areaReadByte != null) {
			return (short) (areaReadByte.readByte(this, addr & 0xfffe) << 8 | 
                    areaReadByte.readByte(this, (addr & 0xfffe) + 1) & 0xff);
		} else if (read != null) {
			return (short) ((read[offset + (addr & AREASIZE - 2)] << 8) 
                        + (read[offset + (addr & AREASIZE - 2) + 1] & 0xff));
		} else {
			return 0;
		}
    }

    @Override
	final byte readByte(int addr) {
        if (areaReadByte != null) {
			return areaReadByte.readByte(this, addr);
		} else if (read != null) {
			return read[offset + (addr & AREASIZE - 1)];
		} else {
			return 0;
		}
    }

    @Override
	final void writeWord(int addr, short val) {
        if (areaWriteWord != null) {
			areaWriteWord.writeWord(this, addr, val);
		} else if (areaWriteByte != null) {
            areaWriteByte.writeByte(this, addr & 0xfffe, (byte) (val >> 8));
            areaWriteByte.writeByte(this, (addr & 0xfffe) + 1, (byte) (val & 0xff));
        } else if (write != null) {
            write[offset + (addr & AREASIZE - 2)] = (byte) (val >> 8);
            write[offset + (addr & AREASIZE - 2) + 1] = (byte) val;
        }
    }

    @Override
	final void writeByte(int addr, byte val) {
        if (areaWriteByte != null) {
			areaWriteByte.writeByte(this, addr, val);
		} else if (write != null) {
			write[offset + (addr & AREASIZE - 1)] = val;
		}
    }

}