/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.memory;

import java.io.IOException;

import v9t9.utils.Utils;

/**
 * These enums and struct define a higher-level organization of the memory map,
 * used to allow large-scale customization of the emulated computer's architecture.
 * 
 * A MemoryEntry deals with larger ranges of memory than a MemoryArea but
 * smaller ones than a MemoryDomain. It represents an unbroken range of memory
 * with the same characteristics and origin.  Each MemoryEntry may be associated with a
 * file on disk, either as ROM or a nonvolatile RAM image. 
 * 
 * A set of MemoryEntrys in a MemoryMap covers the entire span of addressable
 * memory. Multiple MemoryEntrys may cover parts of each other (and this is a
 * necessity for DSR ROMs, banked memory, etc). The Memory / MemoryDomains
 * structurally allow only one MemoryArea to be active at any given location,
 * though. To determine what MemoryEntrys contribute to the actual Memory, use
 * the backlink from MemoryArea to MemoryEntry.
 * 
 * @author ejs
 */
public class MemoryEntry {
    /** start address */
    public int addr;

    /** size in bytes */
    public int size;

    /** name of entry for debugging */
    public String name;

    /** where the memory lives */
    public MemoryDomain domain;

    /** how the memory acts */
    public MemoryArea area;
    
    /** is the memory accessed as words or as bytes? */
    public boolean bWordAccess = true;

    public MemoryEntry(String name, MemoryDomain domain, int addr,
            int size, MemoryArea area) {
        if (size <= 0 || addr < 0 || addr + size > MemoryDomain.PHYSMEMORYSIZE) {
			throw new AssertionError("illegal address range");
		}
        if ((addr & MemoryArea.AREASIZE-1) != 0) {
			throw new AssertionError("illegal address: must live on " + MemoryArea.AREASIZE + " byte boundary");
		}
        if (domain == null || area == null) {
			throw new NullPointerException();
		}
        if ((size & MemoryArea.AREASIZE-1) != 0) {
        	size += MemoryArea.AREASIZE - (size & MemoryArea.AREASIZE-1);
        }
        
        this.addr = addr;
        this.size = size;
        this.name = name;
        this.domain = domain;
        this.area = area;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        MemoryEntry ent = (MemoryEntry)obj;
        return ent.addr == addr
        &&	ent.size == size
        &&	ent.area == area
        &&	ent.domain == domain
        &&	ent.name == name;
    }
    
    @Override
	public String toString() {
        if (name != null) {
			return name;
		}
        return "[memory area >" + Utils.toHex4(addr) + "..." + Utils.toHex4(addr+size) + "]";
    }
    
    /** Tell if entry is mapped. */
    public boolean isMapped() {
        MemoryDomain.AreaIterator iter = domain.new AreaIterator(addr, size);
        while (iter.hasNext()) {
            MemoryArea theArea = (MemoryArea)iter.next();
            if (theArea.equals(area)) {
                return true;
            }
        }
        return false;
    }
    
    /** Map entry into address space */
    public void map() {
        domain.setArea(addr, size, area);
        load();
    }

    /** Unmap entry from address space */
    public void unmap() {
        try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
        unload();
        domain.setArea(addr, size, new WordMemoryArea());
    }

    /** Save entry, if applicable 
     * @throws IOException */
    public void save() throws IOException {
        /* nothing */
    }

    /** Load entry, if applicable */
    public void load() {
        /* nothing */
    }

    /** Unload entry, if applicable */
    public void unload() {
        /* nothing */
    }
}
