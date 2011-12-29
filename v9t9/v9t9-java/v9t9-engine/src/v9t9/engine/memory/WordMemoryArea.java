/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemoryEntry;

/**
 * Word memory is accessed in 9900 byte order but accessed with usual
 * host endianness.
 * <p>
 * This area provides a basis for "traditional" memory which has a flat
 * array of bytes with read and/or write support.
 * @author ejs
 */
public class WordMemoryArea extends MemoryArea {
	public WordMemoryArea() {
		this(0);
	}
    public WordMemoryArea(int latency) {
    	super(latency);
	}
    
	public WordMemoryArea(int latency, short[] memory) {
		super(latency);
		this.memory = memory;
		this.read = memory;
		this.write = memory;
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
        &&	area.memory == memory
        &&  area.read == read
        &&  area.write == write;
    }

	public static WordMemoryArea newDefaultArea() {
	    WordMemoryArea area = new WordMemoryArea();
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
	protected int getSize() {
        return memory != null ? memory.length*2 : 0;
    }

    @Override
	public
	void copyFromBytes(byte[] array) {
    	if (memory == null) {
    		memory = new short[array.length / 2];
    		read = memory;
    	} 

        int length = Math.min(array.length, memory.length*2);
        for (int i = 0; i < length; i+=2) {
            memory[i/2] = (short) (array[i]<<8 | array[i+1]&0xff);
        }
    }
    
    @Override
	public
	void copyToBytes(byte[] array) {
        int length = Math.min(array.length, memory.length*2);
        for (int i = 0; i < length; i+=2) {
            array[i] = (byte) (memory[i/2] >> 8);
            array[i+1] = (byte) (memory[i/2] & 0xff);
        }
    }
    
	@Override
	public short readWord(IMemoryEntry entry, int addr) {
		if (read != null) {
			int addr1 = addr - entry.getAddr();
			/*
			 * processor ignores word access on odd boundaries, and stores in
			 * big-endian format
			 */
			addr1 &= 0xfffe;
			return read[addr1 >> 1];
		} else {
			return 0;
		}
	}

    @Override
	public byte readByte(IMemoryEntry entry, int addr) {
		if (read != null) {
            int addr1 = addr - entry.getAddr();
			/*
			 * processor ignores word access on odd boundaries, and stores in
			 * big-endian format
			 */
			addr1 &= 0xfffe;
			short word = read[addr1 >> 1];
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
	public void writeWord(IMemoryEntry entry, int addr, short val) {
		if (write != null) {
			int addr1 = addr - entry.getAddr();
			/*
			 * processor ignores word access on odd boundaries, and stores in
			 * big-endian format
			 */
			addr1 &= 0xfffe;
			write[addr1 >> 1] = val;
		}
	}

    @Override
	public void writeByte(IMemoryEntry entry, int addr, byte val) {
		if (write != null) {
            
            int addr1 = addr - entry.getAddr();
			/*
			 * processor ignores word access on odd boundaries, and stores in
			 * big-endian format
			 */
			addr1 &= 0xfffe;
			short word = write[addr1 >> 1];
            if ((addr & 1) == 0) {
				word = (short) (val << 8 | word & 0xff);
			} else {
				word = (short) (word & 0xff00 | val & 0xff);
			}
			write[addr1 >> 1] = word;
        }
	}
}