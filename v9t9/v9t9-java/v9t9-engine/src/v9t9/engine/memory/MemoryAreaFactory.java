/**
 * 
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.modules.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class MemoryAreaFactory {


	public static MemoryArea createMemoryArea(IMemory memory, MemoryEntryInfo info) {
		if (info.isByteSized())
			return createByteMemoryArea(memory, info);
		else
			return createWordMemoryArea(memory, info);
	}
	
	public static ByteMemoryArea createByteMemoryArea(IMemory memory, MemoryEntryInfo info) {
		ByteMemoryArea area;
        
		if (!info.isStored()) {
			area = new ByteMemoryArea();
		} else {
			area = new ByteMemoryArea() {
	    		public void writeByte(IMemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
    		};
		}
    	
		area.setLatency(info.getDomain(memory).getLatency(info.getAddress()));
		area.memory = new byte[info.getSize()];
		area.read = area.memory;
		if (info.isStored())
			area.write = area.memory;

		return area;
	}

	public static WordMemoryArea createWordMemoryArea(IMemory memory, MemoryEntryInfo info) {
		WordMemoryArea area;
        
        if (!info.isStored()) {
			area = new WordMemoryArea();
		} else {
			area = new WordMemoryArea() {
	    		public void writeByte(IMemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
	    		@Override
	    		public void writeWord(IMemoryEntry entry, int addr, short val) {
	    			super.writeWord(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
    		};
		}
        area.setLatency(info.getDomain(memory).getLatency(info.getAddress()));
        area.memory = new short[info.getSize() / 2];
        area.read = area.memory;
        if (info.isStored())
        	area.write = area.memory;

		return area;
	}

}
