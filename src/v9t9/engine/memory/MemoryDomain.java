/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

/**
 * @author ejs
 */
public class MemoryDomain {
    /*
     * This must remain 64K, even if mega-memory expansion is emulated. All the
     * public routines expect to be passed 16-bit addresses.
     */
    public static final int PHYSMEMORYSIZE = 65536;

    static final int NUMAREAS = PHYSMEMORYSIZE >> MemoryArea.AREASHIFT;

    /** Listener for noticing memory accesses. */
    public interface MemoryAccessListener {
    	/** Indicate that a read/write of a byte/word occurred, taking the given number
    	 * of CPU cycles.
    	 * @param read
    	 * @param word
    	 * @param cycles total cycles
    	 */
    	void access(boolean read, boolean word, int cycles);
    }
    
    public MemoryAccessListener nullMemoryAccessListener = new MemoryAccessListener() {

		public void access(boolean read, boolean word, int cycles) {
		}
    	
    };
    
    private MemoryAccessListener accessListener = nullMemoryAccessListener;
    
    private MemoryArea areahandlers[] = new MemoryArea[NUMAREAS];
    
    private int baseLatency;
    
    private Stack<MemoryEntry> mappedEntries = new Stack<MemoryEntry>();
	private MemoryEntry zeroMemoryEntry;
    
    public MemoryDomain(int latency) {
    	baseLatency = latency;
    	
    	zeroMemoryEntry = new MemoryEntry("Unmapped memory",
    			this,
    			0,
    			PHYSMEMORYSIZE,
    			new ZeroWordMemoryArea(latency));
    	
        //setArea(0, PHYSMEMORYSIZE, area);
    	mapEntry(zeroMemoryEntry);
    }
    
	public MemoryDomain() {
    	this(1);
    }
    
    /** For testing, create a RAM-accessible memory domain which spans
     * the size of data.
     * @param data populating data, length on AREASIZE boundary 
     * @return
     */
    public static MemoryDomain newFromArray(short[] data, boolean bWordAccess) {
        MemoryDomain domain = new MemoryDomain();
        WordMemoryArea area = WordMemoryArea.newDefaultArea();
        area.bWordAccess = bWordAccess;
        area.memory = data;
        area.read = data;
        area.write = data;
        domain.setArea(0, data.length*2, area);
        return domain;
    }    
    
    /** For testing, create a RAM-accessible memory domain which spans
     * the size of data.
     * @param data populating data, length on AREASIZE boundary 
     * @return
     */
    public static MemoryDomain newFromArray(byte[] data) {
        MemoryDomain domain = new MemoryDomain();
        ByteMemoryArea area = ByteMemoryArea.newDefaultArea();
        area.memory = data;
        area.read = data;
        area.write = data;
        domain.setArea(0, data.length, area);
        return domain;
    }    

    public final MemoryArea getArea(int addr) {
        return areahandlers[(addr & PHYSMEMORYSIZE - 1) >> MemoryArea.AREASHIFT];
    }

    void setArea(int addr, int size, MemoryArea handler) {
        MemoryArea tmp = handler.copy();

        if (size == 0)
        	return;
        	
        if (size < MemoryArea.AREASIZE
                || (addr & MemoryArea.AREASIZE - 1) != 0) {
			throw new AssertionError(
                    "attempt made to set a memory handler on an illegal boundary\n"
                            + "(" + Integer.toHexString(addr) + "..."
                            + Integer.toHexString(addr + size - 1)
                            + "), the minimum granularity is "
                            + Integer.toHexString(MemoryArea.AREASIZE)
                            + " bytes");
		}

        /*if (handler.read == null && handler.areaReadByte == null
                && handler.areaReadWord != null)
            throw new AssertionError(
                    "cannot have a handler define read_word without read_byte");
        if (handler.write == null && handler.areaWriteByte == null
                && handler.areaWriteWord != null)
            throw new AssertionError(
                    "cannot have a handler define write_word without write_byte");
*/
        
        if (size > PHYSMEMORYSIZE || addr >= PHYSMEMORYSIZE
                || addr + size > PHYSMEMORYSIZE) {
			throw new AssertionError("illegal address or size (64k limit)");
		}

        /*
        if (handler.getSize() != 0 && handler.offset + size > handler.getSize()) {
			throw new AssertionError(
                    "memory is not big enough for area handlers from "
                            + Integer.toHexString(handler.offset) + " ("
                            + Integer.toHexString(handler.getSize())
                            + ") for " + Integer.toHexString(size) + " bytes");
		}*/

        //System.out.println("setting addr="+addr+",size="+size);
        size = size + MemoryArea.AREASIZE - 1 >> MemoryArea.AREASHIFT;
        addr >>= MemoryArea.AREASHIFT;
        //System.out.println("====== addr="+addr+",size="+size+" of "+areahandlers.length);
        while (size != 0) {
            areahandlers[addr++] = tmp;

            /* advance memory pointer(s) */
            if (size-- != 0) {
                tmp = tmp.copy();
                tmp.offset += MemoryArea.AREASIZE;
            }
        }
        //System.out.println("area "+areahandlers+":");
        //for (size=0; size<areahandlers.length; size++)
        //    System.out.print(areahandlers[size]+",");
        //System.out.println();
    }

    public final short flatReadWord(int addr) {
        MemoryArea area = getArea(addr);
        accessListener.access(true, true, area.getReadWordLatency());
        return area.flatReadWord(addr);
    }

    public final short flatReadByte(int addr) {
        MemoryArea area = getArea(addr);
        accessListener.access(true, false, area.getReadByteLatency());
        return area.flatReadByte(addr);
    }

    public final void flatWriteByte(int addr, byte val) {
        MemoryArea area = getArea(addr);
        accessListener.access(false, false, area.getWriteByteLatency());
        area.flatWriteByte(addr, val);
    }

    public final void flatWriteWord(int addr, short val) {
        MemoryArea area = getArea(addr);
        accessListener.access(false, true, area.writeWordLatency);
        area.flatWriteWord(addr, val);
    }

    public final byte readByte(int addr) {
        MemoryArea area = getArea(addr);
        accessListener.access(true, false, area.getReadByteLatency());
        return area.readByte(addr);
    }

    public final short readWord(int addr) {
        MemoryArea area = getArea(addr);
        accessListener.access(true, true, area.getReadWordLatency());
        return area.readWord(addr);
    }

    public final void writeByte(int addr, byte val) {
        MemoryArea area = getArea(addr);
        accessListener.access(false, false, area.getWriteByteLatency());
        area.writeByte(addr, val);
    }

    public final void writeWord(int addr, short val) {
        MemoryArea area = getArea(addr);
        accessListener.access(false, true, area.writeWordLatency);
        area.writeWord(addr, val);
    }

    public final boolean hasRamAccess(int addr) {
        MemoryArea area = getArea(addr);
        return area != null && area.hasWriteAccess();
    }

    public final boolean hasRomAccess(int addr) {
        MemoryArea area = getArea(addr);
        return area != null && area.hasReadAccess();
    }

    /** Iterate all the areas in the domain. */
    public class AreaIterator implements Iterator<MemoryArea> {

        MemoryArea area;
        
        int areaIdx;

        int lastArea;

        //private boolean bFresh;

        /** Iterate over a specified memory range */
        AreaIterator(int startaddr, int size) {
            if (startaddr < 0) {
				throw new IndexOutOfBoundsException();
			}
            area = null;
            areaIdx = startaddr >> MemoryArea.AREASHIFT;
            lastArea = startaddr + size + MemoryArea.AREASIZE - 1 >> MemoryArea.AREASHIFT;
            if (lastArea < 0 || lastArea > NUMAREAS) {
				throw new IndexOutOfBoundsException();
			}
        }

        /** Iterate all the memory in the domain */
        AreaIterator() {
            this(0, PHYSMEMORYSIZE);
        }

        private void getNext() {
            while (areaIdx < lastArea) {
                area = areahandlers[areaIdx++];
                if (area != null) {
					break;
				}
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (area == null) {
				getNext();
			}
            return area != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        public MemoryArea next() {
            if (area == null) {
				getNext();
			}
            if (area == null) {
				throw new java.util.NoSuchElementException();
			}
            MemoryArea ret = area;
            area = null;
            return ret;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** Clear out the memory areas, making them inaccessible.
     *	 
     */
    /*public void clear() {
        for (int i = 0; i < areahandlers.length; i++) {
            areahandlers[i] = null;
        }
    }*/

    /** Zero out the memory areas, setting them to zeroed-out ROM.
     *	 
     */
    public void zero() {
        for (int i = 0; i < areahandlers.length; i++) {
            areahandlers[i] = new ZeroWordMemoryArea(baseLatency);
        }
    }

	public void setAccessListener(MemoryAccessListener listener) {
		this.accessListener = listener;
	}
	public int getReadWordLatency(int addr) {
		MemoryArea area = getArea(addr);
		return area.readWordLatency;
	}

	/**
	 * Tell if the entry has been mapped at all -- though it may
	 * have been obscured in the meantime.
	 * @param memoryEntry
	 * @return true if the entry has been mapped
	 */
	public boolean isEntryMapped(MemoryEntry memoryEntry) {
		return mappedEntries.contains(memoryEntry);
	}
	/**
	 * Tell if the entry has been mapped and is fully visible
	 * @param memoryEntry
	 * @return true if all MemoryAreas for the entry are visible
	 */
	public boolean isEntryFullyMapped(MemoryEntry memoryEntry) {
		AreaIterator iter = new AreaIterator(memoryEntry.addr, memoryEntry.size);
        while (iter.hasNext()) {
            MemoryArea theArea = (MemoryArea)iter.next();
            if (theArea.entry != memoryEntry) {
                return false;
            }
        }
        return true;
	}

	/**
	 * Map a memory entry, so that its range of addresses
	 * replace any handled by existing entries.
	 * @param memoryEntry
	 */
	public void mapEntry(MemoryEntry memoryEntry) {
		if (!mappedEntries.contains(memoryEntry))
			mappedEntries.add(memoryEntry);
		mapEntryAreas(memoryEntry);
		memoryEntry.onMap();
	}

	private void mapEntryAreas(MemoryEntry memoryEntry) {
		setArea(memoryEntry.addr, memoryEntry.size, memoryEntry.area);
	}

	/**
	 * Unmap a memory entry, exposing any entries previously mapped.
	 * @param memoryEntry
	 */
	public void unmapEntry(MemoryEntry memoryEntry) {
		// TODO: remove from end?
		mappedEntries.remove(memoryEntry);
		
		int maxAddr = -1, maxEndAddr = 0;
		for (ListIterator<MemoryEntry> entryIter = mappedEntries.listIterator(mappedEntries.size());
			entryIter.hasPrevious(); ) {
			MemoryEntry entry = entryIter.previous();
			if (entry.addr + entry.size > memoryEntry.addr 
					&& entry.addr < memoryEntry.addr + memoryEntry.size) {
				int overlappingAddr = Math.max(memoryEntry.addr, entry.addr);
				int overlappingSize = memoryEntry.addr + memoryEntry.size - overlappingAddr;
				setArea(overlappingAddr, overlappingSize, entry.area);
				if (overlappingAddr > maxAddr)
					maxAddr = overlappingAddr;
				if (overlappingAddr + overlappingSize > maxEndAddr)
					maxEndAddr = overlappingAddr + overlappingSize;
				if (maxAddr == memoryEntry.addr && maxEndAddr == memoryEntry.addr + memoryEntry.size)
					break;
			}
		}
		memoryEntry.onUnmap();
	}

	/**
	 * Quickly swap banked entries. 
	 * @param currentBank
	 * @param newBankEntry
	 */
	public void switchBankedEntry(MemoryEntry currentBank,
			MemoryEntry newBankEntry) {
		if (currentBank != null && newBankEntry != null && isEntryMapped(currentBank)) {
			if (currentBank != newBankEntry) {
				mappedEntries.remove(currentBank);
				currentBank.onUnmap();
				mappedEntries.add(newBankEntry);
				newBankEntry.onMap();
				mapEntryAreas(newBankEntry);
			}
		} else {
			if (currentBank != null) 
				unmapEntry(currentBank);
			if (newBankEntry != null)
				mapEntry(newBankEntry);
		}
	}

	public MemoryEntry getEntryAt(int addr) {
		for (ListIterator<MemoryEntry> entryIter = mappedEntries.listIterator(mappedEntries.size());
			entryIter.hasPrevious(); ) {
			MemoryEntry entry = entryIter.previous();
			if (entry.addr <= addr && addr < entry.addr + entry.size)
				return entry;
		}
		return null;
	}

}
