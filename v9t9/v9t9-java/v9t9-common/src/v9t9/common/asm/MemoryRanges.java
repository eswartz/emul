/*
  MemoryRanges.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.common.asm;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import ejs.base.utils.Check;


/**
 * Management of ranges of memory partitioned into types
 * @author ejs
 *
 */
public class MemoryRanges {
    private TreeMap<Integer, MemoryRange> ranges;

    public MemoryRanges() {
        ranges = new TreeMap<Integer, MemoryRange>();
        clear();
        
    }
    
    /**
     * Get a non-empty range containibarefDefTablesseAddrng the addr.
     * @param addr
     * @return
     */
    public MemoryRange getRangeContaining(int addr) {
        Integer iAddr = new Integer(addr);
        MemoryRange range = ranges.get(iAddr);
        if (range != null) {
			return range;
		}
        SortedMap<Integer, MemoryRange> subMap = ranges.headMap(iAddr);
        if (subMap.isEmpty()) {
			return null;
		}
        return subMap.get(subMap.lastKey());
    }

    /**
	 * @param isCode  
	 */
    public void addRange(int baseAddr, int size, boolean isCode) {
        Check.checkArg((size > 0));
        /*
        MemoryRange startRange = getRangeContaining(baseAddr);
        MemoryRange endRange = getRangeContaining(baseAddr + size);
        if (startRange != null) {
        	if (startRange != endRange) {
        		Check.checkState(false);
        		ranges.remove(startRange);
        	}
        }*/
        ranges.put(new Integer(baseAddr), new MemoryRange(baseAddr, MemoryRange.CODE));
        
        // mark end of range
        //if (endRange == null) {
            ranges.put(new Integer(baseAddr + size), new MemoryRange(baseAddr + size, MemoryRange.EMPTY));
        //}
    }

    public boolean isEmpty() {
        return ranges.size() <= 1;
    }

    public Iterator<MemoryRange> rangeIterator() {
        return ranges.values().iterator();
    }

	public void clear() {
		ranges.clear();
        ranges.put(new Integer(0x10000), new MemoryRange(0x10000, MemoryRange.EMPTY));
		
	}

    
}
