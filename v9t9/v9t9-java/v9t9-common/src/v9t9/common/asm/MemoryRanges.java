/*
  MemoryRanges.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
        Integer iAddr = Integer.valueOf(addr);
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
        ranges.put(Integer.valueOf(baseAddr), new MemoryRange(baseAddr, MemoryRange.CODE));
        
        // mark end of range
        //if (endRange == null) {
            ranges.put(Integer.valueOf(baseAddr + size), new MemoryRange(baseAddr + size, MemoryRange.EMPTY));
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
        ranges.put(Integer.valueOf(0x10000), new MemoryRange(0x10000, MemoryRange.EMPTY));
		
	}

    
}
