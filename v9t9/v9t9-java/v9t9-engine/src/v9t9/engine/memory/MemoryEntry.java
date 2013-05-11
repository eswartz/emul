/*
  MemoryEntry.java

  (c) 2005-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

import v9t9.common.files.IPathFileLocator;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;

//import v9t9.engine.modules.IModule;

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
public class MemoryEntry implements IPersistable, IMemoryEntry {
    /** start address */
    private int addr;

    /** size in bytes */
    private int size;

    /** name of entry for debugging */
    private String name;

    /** where the memory lives */
    private IMemoryDomain domain;

    /** how the memory acts */
    protected MemoryArea area;
    
    /** is the memory accessed as words or as bytes? */
    private boolean bWordAccess = true;

	private TreeMap<Short, String> symbols;

	protected int addrOffset = 0;
	protected IMemory memory;

	private boolean isVolatile;

	protected IPathFileLocator locator;
	
    public MemoryEntry(String name, IMemoryDomain domain, int addr,
            int size, MemoryArea area) {
        if (size < 0 || addr < 0 /*|| addr + size > MemoryDomain.PHYSMEMORYSIZE*/) {
			throw new AssertionError("illegal address range");
		}
        if ((addr & IMemoryDomain.AREASIZE-1) != 0) {
			throw new AssertionError("illegal address: must live on " + IMemoryDomain.AREASIZE + " byte boundary");
		}
        if (domain == null) {
			throw new NullPointerException();
		}
        if ((size & IMemoryDomain.AREASIZE-1) != 0) {
        	size += IMemoryDomain.AREASIZE - (size & IMemoryDomain.AREASIZE-1);
        }
        
        this.addr = addr;
        this.size = size;
        this.name = name;
        this.domain = domain;
        this.area = area;
        this.isVolatile = area != null && area.hasWriteAccess();
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
    
    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#toString()
	 */
	@Override
	public String toString() {
        if (name != null) {
			return name;
		}
        return "[memory entry >" + HexUtils.toHex4(addr) + "..." + HexUtils.toHex4((addr+size)) + "]";
    }

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#isVolatile()
	 */
    @Override
	public boolean isVolatile() {
    	return isVolatile;
    }
    
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#setVolatile(boolean)
	 */
	@Override
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return !hasWriteAccess();
	}
    
    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#setArea(v9t9.common.memory.MemoryArea)
	 */
    @Override
	public void setArea(IMemoryArea area) {
    	if (this.area == null && area != null)
    		isVolatile = area.hasWriteAccess();
    	this.area = (MemoryArea) area;
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

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#save()
	 */
    @Override
	public void save() throws IOException {
        /* nothing */
    }

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#load()
	 */
    @Override
	public void load() {
        /* nothing */
    }

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#unload()
	 */
    @Override
	public void unload() {
        /* nothing */
    }

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#loadSymbols(java.io.InputStream)
	 */
    @Override
	public void loadSymbolsAndClose(InputStream is) throws IOException {
    	BufferedReader reader = null;
    	try {
			reader = new BufferedReader(new InputStreamReader(is));
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
    		if (reader != null) {
    			reader.close();
    		} else {
    			is.close();
    		}
    	}
    }
    

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#writeSymbols(java.io.PrintStream)
	 */
	@Override
	public void writeSymbols(PrintStream os) {
		for (Map.Entry<Short, String> entry : symbols.entrySet()) {
			os.println(HexUtils.toHex4(entry.getKey()) + " " + entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#defineSymbol(int, java.lang.String)
	 */
	@Override
	public void defineSymbol(int addr, String name) {
		if (addr < this.addr || addr >= this.addr + this.size) {
			return;
		}
		if (symbols == null) {
			symbols = new TreeMap<Short, String>();
		}
		symbols.put((short) addr, name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#lookupSymbol(short)
	 */
	@Override
	public String lookupSymbol(short addr) {
		if (symbols == null) return null;
		return symbols.get(addr);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#clearSymbols()
	 */
	@Override
	public void clearSymbols() {
		symbols = null;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#copySymbols(v9t9.common.memory.MemoryDomain)
	 */
	@Override
	public void copySymbols(IMemoryDomain domain) {
		if (symbols != null) {
			for (Map.Entry<Short, String> entry : symbols.entrySet()) {
				domain.getEntryAt(entry.getKey()).defineSymbol(entry.getKey(), entry.getValue());
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#lookupSymbolNear(short, int)
	 */
	@Override
	public Pair<String,Short> lookupSymbolNear(short addr, int range) {
		if (symbols == null) return null;
		SortedMap<Short, String> headMap = symbols.headMap(addr, true);
		if (headMap.isEmpty())
			return null;
		short naddr = headMap.lastKey();
		if (addr - naddr >= range)
			return null;
		return new Pair<String, Short>(symbols.get(naddr), naddr);
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getDomain()
	 */
	@Override
	final public IMemoryDomain getDomain() {
		return domain;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getArea()
	 */
	@Override
	public final IMemoryArea getArea() {
		return area;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#mapAddress(int)
	 */
	@Override
	public final int mapAddress(int addr) {
		return (addr & 0xffff) + addrOffset;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#flatReadByte(int)
	 */
	@Override
	public byte flatReadByte(int addr) {
		return area.flatReadByte(this, mapAddress(addr));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#flatReadWord(int)
	 */
	@Override
	public short flatReadWord(int addr) {
		return area.flatReadWord(this, mapAddress(addr));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#flatWriteByte(int, byte)
	 */
	@Override
	public void flatWriteByte(int addr, byte val) {
		area.flatWriteByte(this, mapAddress(addr), val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#flatWriteWord(int, short)
	 */
	@Override
	public void flatWriteWord(int addr, short val) {
		area.flatWriteWord(this, mapAddress(addr), val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#hasReadAccess()
	 */
	@Override
	public boolean hasReadAccess() {
		return area.hasReadAccess();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#hasWriteAccess()
	 */
	@Override
	public boolean hasWriteAccess() {
		return area.hasWriteAccess();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#readByte(int)
	 */
	@Override
	public byte readByte(int addr) {
		return area.readByte(this, mapAddress(addr));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#readWord(int)
	 */
	@Override
	public short readWord(int addr) {
		return area.readWord(this, mapAddress(addr));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#writeByte(int, byte)
	 */
	@Override
	public void writeByte(int addr, byte val) {
		area.writeByte(this, mapAddress(addr), val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#writeWord(int, short)
	 */
	@Override
	public void writeWord(int addr, short val) {
		area.writeWord(this, mapAddress(addr), val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getLatency()
	 */
	@Override
	public final byte getLatency(int addr) {
		return area.getLatency(addr);
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getName()
	 */
	@Override
	public String getName() {
		return name;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#saveState(v9t9.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		section.put("Class", getClass().getCanonicalName());
		section.put("Name", getName());
		section.put("Address", addr);
		section.put("AddressOffset", addrOffset);
		section.put("Size", size);
		section.put("WordAccess", bWordAccess);
		saveMemoryContents(section);
	}


	protected void saveMemoryContents(ISettingSection section) {
		if (area.hasWriteAccess()) {
			area.saveContents(section, this);
		}
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#loadState(v9t9.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		loadFields(section);
		loadMemoryContents(section);
	}


	protected void loadFields(ISettingSection section) {
		name = section.get("Name");
		addr = section.getInt("Address");
		addrOffset = section.getInt("AddressOffset");
		size = section.getInt("Size");
		bWordAccess = section.getBoolean("WordAccess");
	}


	protected void loadMemoryContents(ISettingSection section) {
		if (area.hasReadAccess()) {
			area.loadContents(section, this);
		}
	}


	public int compareTo(IMemoryEntry o) {
		int diff = domain.hashCode() - o.getDomain().hashCode();
		if (diff != 0) return diff;
		diff = addr - o.getAddr(); 
		return diff;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getUniqueName()
	 */
	@Override
	public String getUniqueName() {
		return name;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#contains(int)
	 */
	@Override
	public boolean contains(int addr) {
		return addr >= this.addr + this.addrOffset && addr < this.addr + this.addrOffset + this.size;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getMemory()
	 */
	@Override
	final public IMemory getMemory() {
		return memory;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getAddr()
	 */
	@Override
	final public int getAddr() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getSize()
	 */
	@Override
	final public int getSize() {
		return size;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#isWordAccess()
	 */
	@Override
	final public boolean isWordAccess() {
		return bWordAccess;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntry#getAddrOffset()
	 */
	@Override
	final public int getAddrOffset() {
		return addrOffset;
	}


	/**
	 * @param domain2
	 */
	public void setDomain(IMemoryDomain domain) {
		this.domain = domain;
	}


	/**
	 * @param isWordAccess
	 */
	public void setWordAccess(boolean isWordAccess) {
		this.bWordAccess = isWordAccess;
	}


	/**
	 * @param memory2
	 */
	public void setMemory(IMemory memory) {
		this.memory = memory;
	}


	/**
	 * @param locator
	 */
	public void setLocator(IPathFileLocator locator) {
		this.locator = locator;
	}

}
