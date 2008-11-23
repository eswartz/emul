/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

/**
 * A memory area is the smallest unit of contiguous memory which has the
 * same behavior and callbacks.  Each has (possibly) unique handling for
 * byte and word reads and writes if these differ.    
 *
 * @author ejs
 */
public abstract class MemoryArea {
	/**
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
