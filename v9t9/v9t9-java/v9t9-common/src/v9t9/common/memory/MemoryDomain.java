/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.common.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import v9t9.base.properties.IPersistable;
import v9t9.base.settings.ISettingSection;

/**
 * @author ejs
 */
public class MemoryDomain implements IMemoryAccess, IPersistable {
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
    
	public static final String NAME_GRAPHICS = "GRAPHICS";
	public static final String NAME_SPEECH = "SPEECH";
	public static final String NAME_VIDEO = "VIDEO";
	public static final String NAME_CPU = "CPU";

    /** Listener for noticing memory accesses. */
    public interface MemoryAccessListener {
    	/** Indicate that a memory entry was accessed.
    	 * @param entry
    	 */
    	void access(MemoryEntry entry);
    }
    
    /** Listener for noticing memory writes. */
    public interface MemoryWriteListener {
    	void changed(MemoryEntry entry, int addr, boolean isByte);
    }
    
    public MemoryAccessListener nullMemoryAccessListener = new MemoryAccessListener() {

		public void access(MemoryEntry entry) {
		}
    	
    };
    
    public MemoryWriteListener nullMemoryWriteListener = new MemoryWriteListener() {

		public void changed(MemoryEntry entry, int addr, boolean isByte) {
		}
    	
    };
    
    
    private MemoryAccessListener accessListener = nullMemoryAccessListener;
    private MemoryWriteListener[] writeListeners = null;
    
    private MemoryEntry entries[] = new MemoryEntry[NUMAREAS];
    
    private Stack<MemoryEntry> mappedEntries = new Stack<MemoryEntry>();
	private MemoryEntry zeroMemoryEntry;
	private final String name;
	public Memory memory;
    
	
    public MemoryDomain(String name, int latency) {
    	this.name = name;
		zeroMemoryEntry = new MemoryEntry("Unmapped memory",
    			this,
    			0,
    			PHYSMEMORYSIZE,
    			new ZeroWordMemoryArea(latency));
    	
        //setArea(0, PHYSMEMORYSIZE, area);
    	mapEntry(zeroMemoryEntry);
    }
    
	public MemoryDomain(String name) {
    	this(name, 0);
    }
    
    /** For testing, create a RAM-accessible memory domain which spans
     * the size of data.
     * @param data populating data, length on AREASIZE boundary 
     * @return
     */
    public static MemoryDomain newFromArray(short[] data, boolean bWordAccess) {
        MemoryDomain domain = new MemoryDomain(MemoryDomain.NAME_CPU);
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

    public static MemoryDomain newFromArray(byte[] data) {
        MemoryDomain domain = new MemoryDomain(MemoryDomain.NAME_CPU);
        ByteMemoryArea area = new ByteMemoryArea();
        area.memory = data;
        area.read = data;
        area.write = data;
        MemoryEntry entry = new MemoryEntry("Test Entry",
        		domain, 0, data.length,
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
        accessListener.access(entry);
        return entry.flatReadWord(addr);
    }

    public final byte flatReadByte(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        return entry.flatReadByte(addr);
    }

    public final void flatWriteByte(int addr, byte val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        entry.flatWriteByte(addr, val);
    }

    public final void flatWriteWord(int addr, short val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        entry.flatWriteWord(addr, val);
    }

    public final byte readByte(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        return entry.readByte(addr);
    }

    public final short readWord(int addr) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        return entry.readWord(addr);
    }

    public final void writeByte(int addr, byte val) {
    	MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        entry.writeByte(addr, val);
        if (writeListeners != null)
        	fireWriteEvent(entry, addr & 0xffff, true);
    }

    private void fireWriteEvent(MemoryEntry entry, int addr, boolean isByte) {
    	for (MemoryWriteListener listener : writeListeners) {
    		listener.changed(entry, addr, isByte);
    	}
	}

	public final void writeWord(int addr, short val) {
        MemoryEntry entry = getEntryAt(addr);
        accessListener.access(entry);
        entry.writeWord(addr, val);
        if (writeListeners != null)
        	fireWriteEvent(entry, addr & 0xfffe, false);
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
		if (listener == null)
			listener = nullMemoryAccessListener;
		this.accessListener = listener;
	}
	
	public synchronized void addWriteListener(MemoryWriteListener listener) {
		List<MemoryWriteListener> newListeners = new ArrayList<MemoryWriteListener>();
		if (writeListeners != null)
			newListeners.addAll(Arrays.asList(writeListeners));
		if (!newListeners.contains(listener))
			newListeners.add(listener);
		writeListeners = newListeners.toArray(new MemoryWriteListener[newListeners.size()]);
	}
	public synchronized void removeWriteListener(MemoryWriteListener listener) {
		if (writeListeners == null)
			return;
		List<MemoryWriteListener> newListeners = new ArrayList<MemoryWriteListener>();
		newListeners.addAll(Arrays.asList(writeListeners));
		newListeners.remove(listener);
		if (newListeners.size() == 0)
			writeListeners = null;
		else
			writeListeners = newListeners.toArray(new MemoryWriteListener[newListeners.size()]);
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
	 * Tell if the entry has been mapped but is fully obscured
	 * @param memoryEntry
	 * @return true if all MemoryAreas for the entry are covered
	 */
	public boolean isEntryFullyUnmapped(MemoryEntry memoryEntry) {
		for (int addr = memoryEntry.addr; addr < memoryEntry.addr + memoryEntry.size; addr += AREASIZE) {
			MemoryEntry theEntry = getEntryAt(addr);
			if (theEntry == memoryEntry)
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
		memoryEntry.memory = memory;
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
		if (memoryEntry == null)
			return;
		
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

	public void saveState(ISettingSection section) {
		int idx = 0;
		for (MemoryEntry entry : mappedEntries) {
			if (entry != zeroMemoryEntry && !isEntryFullyUnmapped(entry)) {
				entry.saveState(section.addSection(""+ idx));
				idx++;
			}
		}
	}

	public void loadState(ISettingSection section) {
		//unmapAll();
		if (section == null) {
			return;
		}

		for (ISettingSection entryStore : section.getSections()) {
			String name = entryStore.get("Name");
			MemoryEntry entry = findMappedEntry(name);
			if (entry != null) {
				entry.loadState(entryStore);
			} else {
				entry = MemoryEntry.createEntry(this, entryStore);
				if (entry != null)
					mapEntry(entry);
				else
					System.err.println("Cannot find memory entry: " + name);
			}
		}
		
	}


	public MemoryEntry findFullyMappedEntry(String name) {
		for (MemoryEntry entry : mappedEntries) {
			if (entry.getName().equals(name) && isEntryFullyMapped(entry)) {
				return entry;
			}
		}
		return null;
	}

	public MemoryEntry findMappedEntry(String name) {
		for (MemoryEntry entry : mappedEntries) {
			if (entry.getName().equals(name) && !isEntryFullyUnmapped(entry)) {
				return entry;
			}
		}
		return null;
	}

	public void unmapAll() {
		mappedEntries.clear();
		mapEntry(zeroMemoryEntry);
	}
	
	public MemoryEntry[] getMemoryEntries() {
		return entries;
	}

	/**
	 * Get all the memory entries, with individual banks expanded
	 * @return
	 */
	public MemoryEntry[] getFlattenedMemoryEntries() {
		List<MemoryEntry> entryList = new ArrayList<MemoryEntry>();
		for (MemoryEntry entry : mappedEntries) {
			if (entry == zeroMemoryEntry  || !isEntryMapped(entry))
				continue;
			if (entry instanceof MultiBankedMemoryEntry) {
				MultiBankedMemoryEntry banked = (MultiBankedMemoryEntry) entry;
				entryList.addAll(Arrays.asList(banked.getBanks()));
			} else if (entry instanceof BankedMemoryEntry) {
				BankedMemoryEntry banked = (BankedMemoryEntry) entry;
				for (int i = 0; i < banked.getBankCount(); i++) {
					entryList.add(new BankedMemoryProxyEntry(banked, i));
				}
			} else {
				entryList.add(entry);
			}
		}
		return (MemoryEntry[]) entryList.toArray(new MemoryEntry[entryList.size()]);
	}

	public void writeMemory(int addr) {
		if (writeListeners != null)
			fireWriteEvent(getEntryAt(addr), addr, true);
	}

	public String getName() {
		return name;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void save() {
		for (MemoryEntry entry : mappedEntries) {
			try {
				entry.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
