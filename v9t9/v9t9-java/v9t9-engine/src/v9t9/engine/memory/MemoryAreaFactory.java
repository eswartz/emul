/*
  MemoryAreaFactory.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemory;
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
    	
		if (memory != null)
			area.setLatency(info.getDomain(memory).getLatency(info.getAddress()));
		
		area.memory = new byte[Math.abs(info.getSize())];
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
        area.memory = new short[Math.abs(info.getSize()) / 2];
        area.read = area.memory;
        if (info.isStored())
        	area.write = area.memory;

		return area;
	}

}
