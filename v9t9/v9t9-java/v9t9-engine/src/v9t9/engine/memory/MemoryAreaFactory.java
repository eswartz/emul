/*
  MemoryAreaFactory.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;

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
        

        int latency = info.getLatency();
        if (latency == -1)
        	latency = info.getDomain(memory).getLatency(info.getAddress());

        int size = Math.abs(info.getSize());
        if (size % IMemoryDomain.AREASIZE != 0)
        	size += IMemoryDomain.AREASIZE - size % IMemoryDomain.AREASIZE;
        
		if (!info.isStored()) {
			area = new ByteMemoryArea(latency, new byte[size], false);
		} else {
			area = new ByteMemoryArea(latency, new byte[size], true) {
	    		public void writeByte(IMemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
    		};
		}
    	
		if (info.isStored())
			area.write = area.memory;

		return area;
	}

	public static WordMemoryArea createWordMemoryArea(IMemory memory, MemoryEntryInfo info) {
		WordMemoryArea area;

        int latency = info.getLatency();
        if (latency == -1)
        	latency = info.getDomain(memory).getLatency(info.getAddress());

        int size = Math.abs(info.getSize());
        if (size % IMemoryDomain.AREASIZE != 0)
        	size += IMemoryDomain.AREASIZE - size % IMemoryDomain.AREASIZE;
        
        if (!info.isStored()) {
			area = new WordMemoryArea(latency, new short[size / 2], false);
		} else {
			area = new WordMemoryArea(latency, new short[size / 2], true) {
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
        
        if (info.isStored())
        	area.write = area.memory;

		return area;
	}

}
