/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.IPropertyStorage;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.modules.IModule;

/**
 * These enums and struct define a higher-level organization of the memory map,
 * used to allow large-scale customization of the emulated computer's architecture.
 * 
 * A MemoryEntry deals with larger ranges of memory than a MemoryArea but
 * smaller ones than a MemoryDomain. It represents an unbroken range of memory
 * with the same characteristics and origin.  Each MemoryEntry may be associated with a
 * file on disk, either as ROM or a nonvolatile RAM image. 
 * 
 * A set of MemoryEntrys in a MemoryDomain covers the entire span of addressable
 * memory. Multiple MemoryEntrys may cover parts of each other (and this is a
 * necessity for DSR ROMs, banked memory, etc). The Memory / MemoryDomains
 * structurally allow only one MemoryArea to be active at any given location,
 * though. 
 * 
 * @author ejs
 */
public class MemoryEntry implements MemoryAccess, Comparable<MemoryEntry>, IPersistable {
    /** start address */
    public int addr;

    /** size in bytes */
    public int size;

    /** name of entry for debugging */
    public String name;

    /** where the memory lives */
    public MemoryDomain domain;

    /** how the memory acts */
    protected MemoryArea area;
    
    /** is the memory accessed as words or as bytes? */
    public boolean bWordAccess = true;

	private TreeMap<Short, String> symbols;

	public int addrOffset = 0;

	/** Tell if this came from a module */
	public IModule moduleLoaded;

	public Memory memory;
	
    public MemoryEntry(String name, MemoryDomain domain, int addr,
            int size, MemoryArea area) {
        if (size < 0 || addr < 0 /*|| addr + size > MemoryDomain.PHYSMEMORYSIZE*/) {
			throw new AssertionError("illegal address range");
		}
        if ((addr & MemoryDomain.AREASIZE-1) != 0) {
			throw new AssertionError("illegal address: must live on " + MemoryDomain.AREASIZE + " byte boundary");
		}
        if (domain == null) {
			throw new NullPointerException();
		}
        if ((size & MemoryDomain.AREASIZE-1) != 0) {
        	size += MemoryDomain.AREASIZE - (size & MemoryDomain.AREASIZE-1);
        }
        
        this.addr = addr;
        this.size = size;
        this.name = name;
        this.domain = domain;
        this.area = area;
    }

    
    /**
	 * Only to be used when reconstructing 
	 */
	public MemoryEntry() {
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + addr;
		result = prime * result + ((area == null) ? 0 : area.hashCode());
		result = prime * result + (bWordAccess ? 1231 : 1237);
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + size;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MemoryEntry other = (MemoryEntry) obj;
		if (addr != other.addr) {
			return false;
		}
		if (area == null) {
			if (other.area != null) {
				return false;
			}
		} else if (!area.equals(other.area)) {
			return false;
		}
		if (bWordAccess != other.bWordAccess) {
			return false;
		}
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}
    
    @Override
	public String toString() {
        if (name != null) {
			return name;
		}
        return "[memory entry >" + HexUtils.toHex4(addr) + "..." + HexUtils.toHex4((addr+size)) + "]";
    }

    public void setArea(MemoryArea area) {
    	this.area = area;
    }
    
    /** Map entry into address space */
    public void onMap() {
    	//domain.mapEntry(this);
        load();
    }

    /** Unmap entry from address space */
    public void onUnmap() {
        try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
        unload();
        //domain.unmapEntry(this);
        //domain.setArea(addr, size, new WordMemoryArea());
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

    /** Load symbols from file in the form:
     * 
     * &lt;addr&gt; &lt;name&gt;
     * @throws IOException 
     */
    public void loadSymbols(InputStream is) throws IOException {
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    		String line;
    		while ((line = reader.readLine()) != null) {
    			int idx = line.indexOf(' ');
    			if (idx > 0) {
    				try {
	    				int addr = Integer.parseInt(line.substring(0, idx), 16);
	    				String name = line.substring(idx+1);
	    				defineSymbol(addr, name);
    				} catch (NumberFormatException e) {
    					
    				}
    			} 
    		}
    		
    	} finally {
    		is.close();
    	}
    }

	public void defineSymbol(int addr, String name) {
		if (addr < this.addr || addr >= this.addr + this.size) {
			return;
		}
		if (symbols == null) {
			symbols = new TreeMap<Short, String>();
		}
		symbols.put((short) addr, name);
	}
	
	public String lookupSymbol(short addr) {
		if (symbols == null) return null;
		return symbols.get(addr);
	}

	public MemoryDomain getDomain() {
		return domain;
	}
	
	/**
	 * Get the active area.
	 * @return
	 */
	public final MemoryArea getArea() {
		return area;
	}
	
	/**
	 * Get the mapping for the address
	 * @param addr
	 * @return
	 */
	protected final int mapAddress(int addr) {
		return (addr & 0xffff) + addrOffset;
	}
	public byte flatReadByte(int addr) {
		return area.flatReadByte(this, mapAddress(addr));
	}
	
	public short flatReadWord(int addr) {
		return area.flatReadWord(this, mapAddress(addr));
	}
	
	public void flatWriteByte(int addr, byte val) {
		area.flatWriteByte(this, mapAddress(addr), val);
	}
	
	public void flatWriteWord(int addr, short val) {
		area.flatWriteWord(this, mapAddress(addr), val);
	}
	
	public boolean hasReadAccess() {
		return area.hasReadAccess();
	}
	
	public boolean hasWriteAccess() {
		return area.hasWriteAccess();
	}
	
	public byte readByte(int addr) {
		return area.readByte(this, mapAddress(addr));
	}
	
	public short readWord(int addr) {
		return area.readWord(this, mapAddress(addr));
	}
	
	public void writeByte(int addr, byte val) {
		area.writeByte(this, mapAddress(addr), val);
	}
	
	public void writeWord(int addr, short val) {
		area.writeWord(this, mapAddress(addr), val);
	}
	
	public final byte getLatency() {
		return area.getLatency();
	}


	public String getName() {
		return name;
	}


	public void saveState(IPropertyStorage section) {
		section.put("Class", getClass().getCanonicalName());
		section.put("Name", getName());
		section.put("Address", addr);
		section.put("Size", size);
		saveMemoryContents(section);
	}


	protected void saveMemoryContents(IPropertyStorage section) {
		if (area.hasWriteAccess()) {
			area.saveContents(section, this);
		}
	}


	public void loadState(IPropertyStorage section) {
		loadFields(section);
		loadMemoryContents(section);
	}


	protected void loadFields(IPropertyStorage section) {
		name = section.get("Name");
		addr = section.getInt("Address");
		size = section.getInt("Size");
	}


	protected void loadMemoryContents(IPropertyStorage section) {
		if (area.hasReadAccess()) {
			area.loadContents(section, this);
		}
	}


	public int compareTo(MemoryEntry o) {
		int diff = domain.hashCode() - o.domain.hashCode();
		if (diff != 0) return diff;
		diff = addr - o.addr; 
		return diff;
	}


	public String getUniqueName() {
		return name;
	}


	public boolean contains(int addr) {
		return addr >= this.addr + this.addrOffset && addr < this.addr + this.addrOffset + this.size;
	}


	/**
	 * @param entryStore
	 * @return
	 */
	public static MemoryEntry createEntry(MemoryDomain domain, IPropertyStorage entryStore) {
		MemoryEntry entry = null;
		String klazzName = entryStore.get("Class");
		if (klazzName != null) {
			try {
				Class<?> klass = Class.forName(klazzName);
				
				entry = (MemoryEntry) klass.newInstance();
				entry.domain = domain;
				entry.bWordAccess = domain.getName().equals("Console");	// TODO
				int latency = domain.getLatency(entryStore.getInt("Address"));
				if (entry.bWordAccess)
					entry.area = new WordMemoryArea(latency);
				else
					entry.area = new ByteMemoryArea(latency);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			entry.memory = domain.memory;
			entry.loadState(entryStore);
		}
		return entry;
	}
}
