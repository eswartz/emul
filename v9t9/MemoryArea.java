/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9;

/**
 * Warning: OLD DOCS
 * 
 * Below we define a table of mrstructs which map CPU addresses to areas of
 * memory, including means of dealing with special memory types through the use
 * of read/write functions.
 * 
 * It is advantageous to store memory in the areamemory/arearead/areawrite
 * pointers and let the MEMORY_xxx or memory_xxx routines access it directly,
 * for speed purposes, but for some memory-mapped areas it is saner to have a
 * routine manage reads and writes to the memory. You must use a memory handling
 * routine when:
 * 
 * (1) word accesses are not the same as two simultaneous byte accesses (2)
 * reading memory and writing memory are not orthogonal, i.e., writing/reading
 * memory has side effects (memory-mapped)
 * 
 * If the contents of memory are bank-switched or toggled on and off (such as
 * the DSR), it's most speed efficient to change handlers to remap the contents
 * (through changing the arearead and areawrite pointers) rather than copying
 * contents in and out of a static areamemory.
 * 
 * The emulator will make no assumptions about the semantics of memory which has
 * read or write routines attached to an area.
 * 
 * Area handlers are the main gateway to the memory bus. Each AREASIZE section
 * of memory has various properties which define how to read from and write to
 * it. mrstruct->areamemory contains the memory for the area. Pure RAM will have
 * mrstruct->arearead and mrstruct->areawrite be set to this. Memory mapped I/O
 * areas or ROM will leave one or both of the arearead or areawrite pointers
 * NULL and instead define a routine, {read|write}_{byte|word} to handle the
 * access to areamemory.
 * 
 * Note, that while the arearead or areawrite pointers points to an AREASIZE
 * sized block, the read/write byte/word routines are always passed a full
 * 16-bit address. This allows a common function to control access to several
 * contiguous areas.
 *
 * @author ejs
 */
public class MemoryArea {
	/*
	 * All CPU memory arrays in V9t9 are arranged by words. Accessing a word
	 * from memory is very simple. Accessing a byte may mean toggling the lowest
	 * address bit.
	 */

	final static short getWord(byte[] memory, int offset, int addr) {
		/*
		 * processor ignores word access on odd boundaries, and stores in
		 * big-endian format
		 */
		addr &= 0xfffe;
		return (short) ((memory[addr + offset] << 8) | (memory[addr + offset + 1] & 0xff));
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

	/*
	 * An area is the smallest unit of memory which has the same essential
	 * behavior, as far as we know. We choose 1k because the TI-99/4A memory
	 * mapped areas for VDP, GROM, etc are accessed 1k apart from each other.
	 */
	static final int AREASIZE = 1024;

	static final int AREASHIFT = 10;

	interface AreaReadWord {
		short readWord(MemoryArea area, int address);
	}

	interface AreaReadByte {
		byte readByte(MemoryArea area, int address);
	}

	interface AreaWriteWord {
		void writeWord(MemoryArea area, int address, short val);
	}

	interface AreaWriteByte {
		void writeByte(MemoryArea area, int address, byte val);
	}

	public boolean bWordAccess = false;

	/* actual memory for area, except for empty mem */
	public byte[] memory;

	/* our offset into that memory */
	int offset = 0;

	/* if non-NULL, we can statically read */
	public byte[] read;

	/* if non-NULL, we can statically write */
	public byte[] write;

	/*
	 * These routines are used before the memory maps when using the
	 * Domain.read/write functions, but the memory is used first when the
	 * Memory.MEMORY_XXX_XXX static functions are used to read CPU memory.
	 */
	public AreaReadWord areaReadWord;

	public AreaReadByte areaReadByte;

	public AreaWriteWord areaWriteWord;

	public AreaWriteByte areaWriteByte;

    public MemoryEntry entry;
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        MemoryArea area = (MemoryArea)obj;
        return area.bWordAccess == bWordAccess
        &&	area.areaReadByte == areaReadByte
        &&	area.areaWriteByte == areaWriteByte
        &&	area.areaReadWord == areaReadWord
        &&	area.areaWriteWord == areaWriteWord
        &&	area.memory == memory;
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
	
	public static MemoryArea newDefaultArea() {
	    MemoryArea area = new MemoryArea();
        DefaultAreaHandlers handlers = new DefaultAreaHandlers();
        area.areaReadByte = handlers;
        area.areaWriteByte = handlers;
        return area;
	}
	
	protected Object clone() {
		MemoryArea area = new MemoryArea();
		area.areaReadByte = this.areaReadByte;
		area.areaReadWord = this.areaReadWord;
		area.areaWriteByte = this.areaWriteByte;
		area.areaWriteWord = this.areaWriteWord;
		area.memory = this.memory;
		area.offset = this.offset;
		area.read = this.read;
		area.write = this.write;
		area.bWordAccess = this.bWordAccess;
		return area;
	}

	final boolean isMemoryMapped() {
		return areaReadWord != null || areaReadByte != null
				|| areaWriteWord != null || areaWriteByte != null;
	}

	boolean hasRamAccess() {
		return read != null && write != null;
	}

	boolean hasRomAccess() {
		return read != null;
	}

	/*
	 * "Flat" memory access bypasses any semantics and just tries to
	 * read readable memory and write writeable memory.
	 */
	final short flatReadWord(int addr) {
		if (read != null)
			return MemoryArea.getWord(read, offset, (addr & (AREASIZE - 2)));
		else
			return 0;
	}

	final byte flatReadByte(int addr) {
		if (read != null)
			return read[offset + (addr & (AREASIZE - 1))];
		else
			return 0;
	}

	final void flatWriteWord(int addr, short val) {
		if (write != null)
			putWord(write, offset, (addr & (MemoryArea.AREASIZE - 2)), val);
	}

	final void flatWriteByte(int addr, byte val) {
		if (write != null)
			write[offset + (addr & (MemoryArea.AREASIZE - 1))] = val;
	}

	/*
	 * 	All of these safely and laboriously access memory in the way
	 * 	God intended.
	 */
	final short readWord(int addr) {
		if (areaReadWord != null)
			return areaReadWord.readWord(this, addr);
		else if (areaReadByte != null)
			return (short) ((areaReadByte.readByte(this, addr & 0xfffe) << 8) | 
					((areaReadByte.readByte(this, (addr & 0xfffe) + 1)) & 0xff));
		else if (read != null)
			if (bWordAccess)
				return getWord(read, offset, (addr & (AREASIZE - 2)));
			else
				return (short) ((read[offset + ((addr & (AREASIZE - 2)))] << 8) 
						+ (read[offset + (addr & (AREASIZE - 2)) + 1] & 0xff));
		else
			return 0;
	}

	final byte readByte(int addr) {
		if (areaReadByte != null)
			return areaReadByte.readByte(this, addr);
		else if (read != null)
			return read[offset + ((addr & (AREASIZE - 1)))];
		else
			return 0;
	}

	final void writeWord(int addr, short val) {
		if (areaWriteWord != null)
			areaWriteWord.writeWord(this, addr, val);
		else if (areaWriteByte != null) {
			areaWriteByte.writeByte(this, addr & 0xfffe, (byte) (val >> 8));
			areaWriteByte.writeByte(this, (addr & 0xfffe) + 1, (byte) (val & 0xff));
		} else if (write != null)
			if (bWordAccess)
				putWord(write, offset, (addr & (AREASIZE - 2)), val);
			else {
				write[offset + (addr & (AREASIZE - 2))] = (byte) (val >> 8);
				write[offset + (addr & (AREASIZE - 2)) + 1] = (byte) val;
			}
	}

	final void writeByte(int addr, byte val) {
		if (areaWriteByte != null)
			areaWriteByte.writeByte(this, addr, val);
		else if (write != null)
			write[offset + ((addr & (AREASIZE - 1)))] = val;
	}

}