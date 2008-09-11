/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.memory;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ejs
 */
public class MemoryMap {
    /* map MemoryDomain to Map<address,MemoryEntry> */
    private Map<MemoryDomain, HashMap<Integer, MemoryEntry>> domainMapMap 
    	= new HashMap<MemoryDomain, HashMap<Integer, MemoryEntry>>();

    /**
     * @param entry
     * @return
     */
    private Map<Integer, MemoryEntry> getMapForDomain(MemoryDomain domain) {
        Map<Integer, MemoryEntry> map = domainMapMap.get(domain);
        if (map == null) {
			throw new NullPointerException();	// TODO
		}
        return map;
    }

    /** Register a domain
     * @param domain
     */
    public void add(MemoryDomain domain) {
        if (domainMapMap.get(domain) == null) {
            domainMapMap.put(domain, new HashMap<Integer, MemoryEntry>());
        }
    }

    /**
     * @param entry
     * @return
     */
    private Map<Integer, MemoryEntry> getMapForEntry(MemoryEntry entry) {
        return getMapForDomain(entry.domain);
    }
    
    public void add(MemoryEntry entry) {
        Map<Integer, MemoryEntry> map = getMapForEntry(entry);
        map.put(new Integer(entry.addr), entry);
    }

    public void remove(MemoryEntry entry) {
        entry.unmap();
        Map<Integer, MemoryEntry> map = getMapForEntry(entry);
        map.remove(new Integer(entry.addr));
    }
    
    public MemoryEntry lookupStart(MemoryDomain domain, int address) {
        MemoryArea area = domain.getArea(address);
        if (area.entry != null) {
			return area.entry;
		}
        Map<Integer, MemoryEntry> map = getMapForDomain(domain);
        MemoryEntry ret = map.get(new Integer(address));
        area.entry = ret;
        return ret;
    }
 
    public MemoryEntry lookupEntry(MemoryDomain domain, int address) {
        MemoryArea area = domain.getArea(address);
        if (area.entry != null) {
			return area.entry;
		}

        Map<Integer, MemoryEntry> map = getMapForDomain(domain);
        for (Iterator<MemoryEntry> iter = map.values().iterator(); iter.hasNext();) {
            MemoryEntry element = iter.next();
            if (element.addr <= address && element.addr + element.size > address) {
                area.entry = element;
                return element;
            }
        }
        return null;
    }
 
}
