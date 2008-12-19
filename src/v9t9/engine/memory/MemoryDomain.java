/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

import java.util.Stack;

/**
 * @author ejs
 */
public class MemoryDomain implements MemoryAccess {
    /*
     * This must remain 64K, even if mega-memory expansion is emulated. All the
     * public routines expect to be passed 16-bit addresses.
     */
    public static final int PHYSMEMORYSIZE = 65536;
	/**
	 * An area is the smallest unit of memory which has the same essential
	 * behavior, as far as we know. We choose 1k because the TI-99/4A memory
	 * mapped areas for VDP, GROM, etc are accessed 1k apart from each other.
	 */
	static public final int AREASIZE = 1024;

	static public final int AREASHIFT = 10;

    static final int NUMAREAS = PHYSMEMORYSIZE >> AREASHIFT;

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
    
    private MemoryEntry entries[] = new MemoryEntry[NUMAREAS];
    
    private Stack<MemoryEntry> mappedEntries = new Stack<MemoryEntry>();
	private MemoryEntry zeroMemoryEntry;
    
    public MemoryDomain(int latency) {
    	zeroMemoryEntry = new MemoryEntry("Unmapped memory",
    			this,
    			0,
    			PHYSMEMORYSIZE,
    			new ZeroWordMemoryArea(latency));
    	
        //setArea(0, PHYSMEMORYSIZE, area);
    	mapEntry(zeroMemoryEntry);
    }
    
	public MemoryDomain() {
    	this(0);
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
        MemoryEntry entry = new MemoryEntry("Test Entry",
        		domain, 0, data.length * 2,
        		area);
        domain.mapEntry(entry);
        return domain;
    }    
    
    public final MemoryEntry getEntryAt(int addr) {
        return entries[(addr & PHYSMEMORYSIZE - 1) >> AREASHIFT];
    }

    void mapEntryAreas(int addr, int size, MemoryEntry entry) {
        if (size == 0)
        	return;
        	
        if (size < AREASIZE
                || (addr & AREASIZE - 1) != 0) {
			throw new AssertionError(
                    "attempt made to set a memory handler on an illegal boundary\n"
                            + "(" + Integer.toHexString(addr) + "..."
                            + Integer.toHexString(addr + size - 1)
                            + "), the minimum granularity is "
                            + Integer.toHexString(AREASIZE)
                            + " bytes");
		}

        if (size > PHYSMEMORYSIZE || addr >= PHYSMEMORYSIZE
                || addr + size > PHYSMEMORYSIZE) {
			throw new AssertionError("illegal address or size (64k limit)");
		}

        size = size + AREASIZE - 1 >> AREASHIFT;
        addr >>= AREASHIFT;
        while (size-- != 0) {
            entries[addr++] = entry;
        }
    }

    public final short flatReadWord(int addr) {
        MemoryEntry entry = getEntryAt(addr);
        accessListener.access(true, true, entry.getLatency());
        return entry.flatReadWord(addr);
    }

    public final byte flatReadByte(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(true, false, entry.getLatency());
        return entry.flatReadByte(addr);
    }

    public final void flatWriteByte(int addr, byte val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(false, false, entry.getLatency());
        entry.flatWriteByte(addr, val);
    }

    public final void flatWriteWord(int addr, short val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(false, true, entry.getLatency());
        entry.flatWriteWord(addr, val);
    }

    public final byte readByte(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(true, false, entry.getLatency());
        return entry.readByte(addr);
    }

    public final short readWord(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(true, true, entry.getLatency());
        return entry.readWord(addr);
    }

    public final void writeByte(int addr, byte val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(false, false, entry.getLatency());
        entry.writeByte(addr, val);
    }

    public final void writeWord(int addr, short val) {
        MemoryEntry entry = getEntryAt(addr);
        accessListener.access(false, true, entry.getLatency());
        entry.writeWord(addr, val);
    }

    public final boolean hasRamAccess(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        return entry != null && entry.hasWriteAccess();
    }

    public final boolean hasRomAccess(int addr) {
        MemoryEntry entry = getEntryAt(addr);
        return entry != null && entry.hasReadAccess();
    }

    /** Zero out the memory areas, setting them to zeroed-out ROM.
     *	 
     */
    public void zero() {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = zeroMemoryEntry;
        }
    }

	public void setAccessListener(MemoryAccessListener listener) {
		this.accessListener = listener;
	}
	public int getLatency(int addr) {
		MemoryEntry entry = getEntryAt(addr);
		return entry.getLatency();
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
		for (int addr = memoryEntry.addr; addr < memoryEntry.addr + memoryEntry.size; addr += AREASIZE) {
			MemoryEntry theEntry = getEntryAt(addr);
			if (theEntry != memoryEntry)
				return false;
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
		mapEntryAreas(memoryEntry.addr, memoryEntry.size, memoryEntry);
	}

	/**
	 * Unmap a memory entry, exposing any entries previously mapped.
	 * @param memoryEntry
	 */
	public void unmapEntry(MemoryEntry memoryEntry) {
		// TODO: remove from end?
		mappedEntries.remove(memoryEntry);
		
		for (MemoryEntry entry : mappedEntries) {
			mapEntryAreas(entry.addr, entry.size, entry);
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


}
