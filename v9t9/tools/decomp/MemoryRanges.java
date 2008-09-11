/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tools.decomp;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import v9t9.utils.Check;

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

    public void addRange(int baseAddr, int size, boolean isCode) {
        Check.checkArg(size > 0);
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
