/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.engine.memory;

import java.util.ArrayList;
import java.util.List;

import net.iHarder.Base64;

import org.eclipse.jface.dialogs.IDialogSettings;

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
public abstract class MemoryArea implements Comparable<MemoryArea> {
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
    
    abstract int getSize();
    
	abstract public boolean hasWriteAccess();

	abstract public boolean hasReadAccess();

	/**
	 * Read a word at the given 16-bit address, without side effects.
	 * @param entry TODO
	 * @param addr address
	 */
	public short flatReadWord(MemoryEntry entry, int addr) {
		return readWord(entry, addr);
	}

	/**
	 * Read a byte at the given 16-bit address, without side effects.
	 * @param entry TODO
	 * @param addr address
	 */
    public byte flatReadByte(MemoryEntry entry, int addr) {
    	return readByte(entry, addr);
    }

	/**
	 * Write a word at the given 16-bit address, without side effects.
	 * @param entry TODO
	 * @param addr address
	 */
    public void flatWriteWord(MemoryEntry entry, int addr, short val) {
    	writeWord(entry, addr, val);
    }

	/**
	 * Write a byte at the given 16-bit address, without side effects.
	 * @param entry TODO
	 * @param addr address
	 */
    public void flatWriteByte(MemoryEntry entry, int addr, byte val) {
    	writeByte(entry, addr, val);
    }

	/**
	 * Read a word at the given 16-bit address.
	 * @param entry TODO
	 * @param addr address
	 */
    abstract short readWord(MemoryEntry entry, int addr);

	/**
	 * Read a byte at the given 16-bit address.
	 * @param entry TODO
	 * @param addr address
	 */
    abstract byte readByte(MemoryEntry entry, int addr);

	/**
	 * Write a word at the given 16-bit address.
	 * @param entry TODO
	 * @param addr address
	 */
    abstract void writeWord(MemoryEntry entry, int addr, short val);

	/**
	 * Write a byte at the given 16-bit address.
	 * @param entry TODO
	 * @param addr address
	 */
    abstract void writeByte(MemoryEntry entry, int addr, byte val);

    /**
     * Save the content of the memory the given array (sized based on the entry)
     * @param array
     */
    abstract void copyToBytes(byte[] array);
    /**
     * Read the content of the memory the given array (sized based on the entry)
     * @param array
     */
    abstract void copyFromBytes(byte[] array);

	public void setLatency(int latency) {
		this.latency = (byte) latency;
	}

	protected void setLatency(byte latency) {
		this.latency = latency;
	}

	protected byte getLatency() {
		return latency;
	}

	public void saveContents(IDialogSettings section, MemoryEntry entry) {
		List<String> contents = new ArrayList<String>();
		//int endAddr = entry.addr + getSize();
		//endAddr = Math.min(endAddr, entry.addr + entry.size);
		int endAddr = entry.addr + getSize();
		for(int saveAddr = entry.addr; saveAddr < endAddr; saveAddr += 256) {
			int perLine = saveAddr + 256 < endAddr ? 256 : endAddr - saveAddr;
			byte[] chunk = new byte[perLine];
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
				contents.add(Integer.toHexString(saveAddr) + ":" + encoded);
			}
		}
		section.put("Contents", (String[]) contents.toArray(new String[contents.size()]));		
	}

	public void loadContents(IDialogSettings section, MemoryEntry memoryEntry) {
		String[] contents = section.getArray("Contents");
		if (contents == null)
			return;
		
		for (String entry : contents) {
			int cidx = entry.indexOf(':');
			try {
				int saveAddr = Integer.parseInt(entry.substring(0, cidx), 16);
				String encoded = entry.substring(cidx + 1);
				byte[] chunk = Base64.decode(encoded, Base64.GZIP);
				for (int idx = 0; idx < chunk.length; idx++) {
					flatWriteByte(memoryEntry, saveAddr++, chunk[idx]);
				}
			} catch (NumberFormatException e) {
				// not a chunk
			}
		}		
	}

	public int compareTo(MemoryArea o) {
		return hashCode() - o.hashCode();
	}
}
