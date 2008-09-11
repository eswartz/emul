/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

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
public abstract class MemoryArea {
	/*
	 * An area is the smallest unit of memory which has the same essential
	 * behavior, as far as we know. We choose 1k because the TI-99/4A memory
	 * mapped areas for VDP, GROM, etc are accessed 1k apart from each other.
	 */
	static public final int AREASIZE = 1024;

	static public final int AREASHIFT = 10;

	public interface AreaReadWord {
		short readWord(MemoryArea area, int address);
	}

	public interface AreaReadByte {
		byte readByte(MemoryArea area, int address);
	}

	public interface AreaWriteWord {
		void writeWord(MemoryArea area, int address, short val);
	}

	public interface AreaWriteByte {
		void writeByte(MemoryArea area, int address, byte val);
	}

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
    
    /** Offset into the given memory entry */
    public int offset;
    
    abstract int getSize();
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof MemoryArea)) {
			return false;
		}
        MemoryArea area = (MemoryArea)obj;
        return area.areaReadByte == areaReadByte
        &&	area.areaWriteByte == areaWriteByte
        &&	area.areaReadWord == areaReadWord
        &&	area.areaWriteWord == areaWriteWord;
    }

    abstract MemoryArea copy();
    
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
	
	final boolean isMemoryMapped() {
		return areaReadWord != null || areaReadByte != null
				|| areaWriteWord != null || areaWriteByte != null;
	}

	abstract public boolean hasWriteAccess();

	abstract public boolean hasReadAccess();

	/*
	 * "Flat" memory access bypasses any semantics and just tries to
	 * read readable memory and write writeable memory.
	 */
	public abstract short flatReadWord(int addr);

    public abstract byte flatReadByte(int addr);

    public abstract void flatWriteWord(int addr, short val);

    public abstract void flatWriteByte(int addr, byte val);

	/*
	 * 	All of these safely and laboriously access memory in the way
	 * 	God intended.
	 */
    abstract short readWord(int addr);

    abstract byte readByte(int addr);

    abstract void writeWord(int addr, short val);

    abstract void writeByte(int addr, byte val);

    abstract void copyToBytes(byte[] array);
    abstract void copyFromBytes(byte[] array);
}