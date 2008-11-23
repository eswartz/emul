/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

/**
 * Word memory is accessed in 9900 byte order but accessed with usual
 * host endianness.
 * @author ejs
 */
public class WordMemoryArea extends MemoryArea {
	/**
	 * All CPU memory arrays in V9t9 are arranged by words. Accessing a word
	 * from memory is very simple.  Accessing a byte means reading the word
     * and extracting part of it (big-endian: low address is high byte).
	 */

	public WordMemoryArea() {
		this(0);
	}
    public WordMemoryArea(int latency) {
    	this.readByteLatency = this.readWordLatency = this.writeByteLatency = this.writeWordLatency = (byte) latency;
	}

	/**
     * Extract a word 
     * @param offset byte offset
     * @param addr byte address offset
     */
	final static short getWord(short[] memory, int offset, int addr) {
		/*
		 * processor ignores word access on odd boundaries, and stores in
		 * big-endian format
		 */
		addr &= 0xfffe;
		return memory[addr + offset >> 1];
	}

    /**
     * Place a word 
     * @param offset byte offset
     * @param addr byte address offset
     */
	final static void putWord(short[] memory, int offset, int addr, short val) {
		/*
		 * processor ignores word access on odd boundaries, and stores in
		 * big-endian format
		 */
		addr &= 0xfffe;
		memory[addr + offset >> 1] = val;
	}

	public boolean bWordAccess = true;

	/* actual memory for area, except for empty mem */
	public short[] memory;

	/* if non-NULL, we can statically read */
	public short[] read;

	/* if non-NULL, we can statically write */
	public short[] write;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof WordMemoryArea)) {
			return false;
		}
        WordMemoryArea area = (WordMemoryArea)obj;
        return area.bWordAccess == bWordAccess
        &&	area.areaReadByte == areaReadByte
        &&	area.areaWriteByte == areaWriteByte
        &&	area.areaReadWord == areaReadWord
        &&	area.areaWriteWord == areaWriteWord
        &&	area.memory == memory
        &&  area.read == read
        &&  area.write == write;
    }

	public static WordMemoryArea newDefaultArea() {
	    WordMemoryArea area = new WordMemoryArea();
        DefaultAreaHandlers handlers = new DefaultAreaHandlers();
        area.areaReadByte = handlers;
        area.areaWriteByte = handlers;
        return area;
	}
	
	@Override
	public MemoryArea copy() {
		WordMemoryArea area = new WordMemoryArea();
		
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
		area.bWordAccess = this.bWordAccess;
		
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
        return memory != null ? memory.length*2 : 0;
    }

    @Override
	void copyFromBytes(byte[] array) {
        int length = Math.min(array.length, memory.length*2);
        for (int i = 0; i < length; i+=2) {
            memory[i/2] = (short) (array[i]<<8 | array[i+1]&0xff);
        }
    }
    
    @Override
	void copyToBytes(byte[] array) {
        int length = Math.min(array.length, memory.length*2);
        for (int i = 0; i < length; i+=2) {
            array[i] = (byte) (memory[i/2] >> 8);
            array[i+1] = (byte) (memory[i/2] & 0xff);
        }
    }
    
	/*
	 * "Flat" memory access bypasses any semantics and just tries to
	 * read readable memory and write writeable memory.
	 */
	@Override
	public final short flatReadWord(int addr) {
		if (read != null) {
			return WordMemoryArea.getWord(read, offset, (addr & AREASIZE - 2));
		} else {
			return 0;
		}
	}

    @Override
	public final byte flatReadByte(int addr) {
		if (read != null) {
            short word = WordMemoryArea.getWord(read, offset, (addr & AREASIZE - 2));
            if ((addr & 1) == 0) {
				return (byte)(word >> 8);
			} else {
				return (byte)word;
			}
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
            
            short word = WordMemoryArea.getWord(write, offset, (addr & AREASIZE - 2));
            if ((addr & 1) == 0) {
				word = (short) (val << 8 | word & 0xff);
			} else {
				word = (short) (word & 0xff00 | val & 0xff);
			}
            
			putWord(write, offset, (addr & AREASIZE - 2), word);
        }
	}

	/*
	 * 	All of these safely and laboriously access memory in the way
	 * 	God intended.
	 */
	@Override
	final short readWord(int addr) {
		if (areaReadWord != null) {
			return areaReadWord.readWord(this, addr);
		} else if (areaReadByte != null) {
			return (short) (areaReadByte.readByte(this, addr & 0xfffe) << 8 | 
					areaReadByte.readByte(this, (addr & 0xfffe) + 1) & 0xff);
		} else if (read != null) {
			return getWord(read, offset, (addr & AREASIZE - 2));
		} else {
			return 0;
		}
	}

	@Override
	final byte readByte(int addr) {
		if (areaReadByte != null) {
			return areaReadByte.readByte(this, addr);
		} else if (read != null) {
			short word = getWord(read, offset, (addr & AREASIZE - 2));
            if ((addr & 1) == 0) {
				return (byte)(word >> 8);
			} else {
				return (byte)word;
			}
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
			putWord(write, offset, (addr & AREASIZE - 2), val);
		}
	}

	@Override
	final void writeByte(int addr, byte val) {
		if (areaWriteByte != null) {
			areaWriteByte.writeByte(this, addr, val);
		} else if (write != null) {
            short word = getWord(write, offset, (addr & AREASIZE - 2));
            if ((addr & 1) == 0) {
                word = (short) (val << 8 | word & 0xff);
            } else {
                word = (short) (word & 0xff00 | val & 0xff);
            }
            putWord(write, offset, (addr & AREASIZE - 2), word);
        }
	}

}