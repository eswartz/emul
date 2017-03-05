/*
  MemoryArea.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import ejs.base.settings.ISettingSection;
import net.iHarder.Base64;

import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryEntry;

/**
 * A memory area is the smallest unit of contiguous memory which has the
 * same behavior and callbacks.  Each has (possibly) unique handling for
 * byte and word reads and writes if these differ.    
 * <p>
 * A MemoryArea must retain a unique identity throughout its lifetime -- 
 * it cannot morph (i.e. be bank-switched).  A higher-level entity like
 * MemoryEntry must handle that.
 *
 * @author ejs
 */
public abstract class MemoryArea implements IMemoryArea {
	private static int gIdentity;
    private int identity = gIdentity++;

	public MemoryArea(int latency) {
    	this.latency = (byte) latency;
	}
    
    @Override
    public int hashCode() {
    	return identity;
    }
    
    /** The cycle count for accessing memory from this area. 
     * This is understood to be the "native bus" latency -- either
     * accessing a byte on an 8-bit bus or a word on a 16-bit bus.
     * In the TMS9900, in CPU memory, bytes are accessed as words, 
     * so the latency does not depend on the unit size.  */
    private byte latency;
    
    abstract protected int getSize();
    
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#hasWriteAccess()
	 */
	@Override
	abstract public boolean hasWriteAccess();

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#hasReadAccess()
	 */
	@Override
	abstract public boolean hasReadAccess();

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#flatReadWord(v9t9.common.memory.MemoryEntry, int)
	 */
	@Override
	public short flatReadWord(IMemoryEntry entry, int addr) {
		return readWord(entry, addr);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#flatReadByte(v9t9.common.memory.MemoryEntry, int)
	 */
    @Override
	public byte flatReadByte(IMemoryEntry entry, int addr) {
    	return readByte(entry, addr);
    }

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#flatWriteWord(v9t9.common.memory.MemoryEntry, int, short)
	 */
    @Override
	public void flatWriteWord(IMemoryEntry entry, int addr, short val) {
    	writeWord(entry, addr, val);
    }

    abstract public boolean patchWord(MemoryEntry memoryEntry, int addr, short value);


	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#flatWriteByte(v9t9.common.memory.MemoryEntry, int, byte)
	 */
    @Override
	public void flatWriteByte(IMemoryEntry entry, int addr, byte val) {
    	writeByte(entry, addr, val);
    }

	/**
	 * Read a word at the given 16-bit address.
	 * @param entry
	 * @param addr address
	 */
    protected abstract short readWord(IMemoryEntry entry, int addr);

	/**
	 * Read a byte at the given 16-bit address.
	 * @param entry
	 * @param addr address
	 */
    protected abstract byte readByte(IMemoryEntry entry, int addr);

	/**
	 * Write a word at the given 16-bit address.
	 * @param entry
	 * @param addr address
	 */
    protected abstract void writeWord(IMemoryEntry entry, int addr, short val);

	/**
	 * Write a byte at the given 16-bit address.
	 * @param entry
	 * @param addr address
	 */
    protected abstract void writeByte(IMemoryEntry entry, int addr, byte val);

	@Override
	public void setLatency(int latency) {
		this.latency = (byte) latency;
	}

	protected void setLatency(byte latency) {
		this.latency = latency;
	}

	protected byte getLatency(int addr) {
		return latency;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryArea#getLatency()
	 */
	@Override
	public int getLatency() {
		return latency;
	}

	public void saveContents(ISettingSection section, IMemoryEntry entry) {
		ISettingSection contents = section.addSection("Contents");
		int endAddr = entry.getAddr() + getSize();
		byte[] chunk = new byte[256];
		for(int saveAddr = entry.getAddr(); saveAddr < endAddr; saveAddr += 256) {
			int perLine = saveAddr + 256 < endAddr ? 256 : endAddr - saveAddr;
			boolean allZero = true;
			try {
				for (int idx = 0; idx < perLine; idx++) {
					byte byt = flatReadByte(entry, saveAddr + idx);
					chunk[idx] = byt;
					allZero &= (byt == 0);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// wtf
			}
			if (!allZero) {
				String encoded = Base64.encodeBytes(chunk, Base64.GZIP);
				contents.put(Integer.toHexString(saveAddr).toUpperCase(), encoded);
			}
		}
	}

	public void loadContents(ISettingSection section, IMemoryEntry memoryEntry) {
		ISettingSection contents = section.getSection("Contents");
		if (contents != null) {
			// clear memory first
			clearMemoryOnLoad(memoryEntry);
			
			for (ISettingSection.SettingEntry entry : contents) {
				try {
					int saveAddr = Integer.parseInt(entry.name, 16);
					byte[] chunk = Base64.decode(entry.value.toString(), Base64.GZIP);
					loadChunk(memoryEntry, saveAddr, chunk);
				} catch (NumberFormatException e) {
					// not a chunk
				}
			}		
	
		}
		else {
			// compatibility
			String[] contentsStr = section.getArray("Contents");
			if (contentsStr == null)
				return;
			
			for (String entry : contentsStr) {
				int cidx = entry.indexOf(':');
				try {
					int saveAddr = Integer.parseInt(entry.substring(0, cidx), 16);
					String encoded = entry.substring(cidx + 1);
					byte[] chunk = Base64.decode(encoded, Base64.GZIP);
					loadChunk(memoryEntry, saveAddr, chunk);
				} catch (NumberFormatException e) {
					// not a chunk
				}
			}
		}
	}

	/**
	 * @param memoryEntry
	 */
	protected void clearMemoryOnLoad(IMemoryEntry memoryEntry) {
		for (int idx = 0; idx < getSize(); idx++) {
			memoryEntry.flatWriteByte(memoryEntry.getAddr() + idx, (byte) 0);
		}
	}

	/**
	 * @param memoryEntry
	 * @param saveAddr
	 * @param chunk
	 */
	protected void loadChunk(IMemoryEntry memoryEntry, int saveAddr,
			byte[] chunk) {
		for (int idx = 0; idx < chunk.length; idx++) {
			flatWriteByte(memoryEntry, saveAddr++, chunk[idx]);
		}
	}

	public int compareTo(IMemoryArea o) {
		return hashCode() - o.hashCode();
	}
}
