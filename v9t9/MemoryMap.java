/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * @author ejs
 */
public class MemoryMap {
    /* map MemoryDomain to Map<address,MemoryEntry> */
    private Map domainMapMap = new HashMap();

    /**
     * @param entry
     * @return
     */
    private Map getMapForDomain(MemoryDomain domain) {
        Map map = (Map)domainMapMap.get(domain);
        if (map == null) 
            throw new NullPointerException();	// TODO
        return map;
    }

    /** Register a domain
     * @param domain
     */
    public void add(MemoryDomain domain) {
        if (domainMapMap.get(domain) == null) {
            domainMapMap.put(domain, new HashMap());
        }
    }

    /**
     * @param entry
     * @return
     */
    private Map getMapForEntry(MemoryEntry entry) {
        return getMapForDomain(entry.domain);
    }
    
    public void add(MemoryEntry entry) {
        Map map = getMapForEntry(entry);
        map.put(new Integer(entry.addr), entry);
    }

    public void remove(MemoryEntry entry) {
        entry.unmap();
        Map map = getMapForEntry(entry);
        map.remove(new Integer(entry.addr));
    }
    
    public MemoryEntry lookupStart(MemoryDomain domain, int address) {
        MemoryArea area = domain.getArea(address);
        if (area.entry != null)
            return area.entry;
        Map map = getMapForDomain(domain);
        MemoryEntry ret = (MemoryEntry)map.get(new Integer(address));
        area.entry = ret;
        return ret;
    }
 
    public MemoryEntry lookupEntry(MemoryDomain domain, int address) {
        MemoryArea area = domain.getArea(address);
        if (area.entry != null)
            return area.entry;

        Map map = getMapForDomain(domain);
        for (Iterator iter = map.values().iterator(); iter.hasNext();) {
            MemoryEntry element = (MemoryEntry) iter.next();
            if (element.addr <= address && element.addr + element.size > address) {
                area.entry = element;
                return element;
            }
        }
        return null;
    }
 
    /** iterate in order through all domains and all memory entries. */
    public class MemoryEntryIterator implements Iterator {
        java.util.TreeSet maps;
        Iterator mapsiter;
        java.util.TreeSet entries;
        Iterator entriesiter;
        MemoryEntry entry;
        
        private void getNext() {
            do {
                if (entriesiter == null) {
                    if (mapsiter == null) {
                        maps = new java.util.TreeSet((java.util.Set)domainMapMap.keySet());
                        mapsiter = maps.iterator();
                    }
                    if (mapsiter != null) {
                        if (!mapsiter.hasNext()) {
                            mapsiter = null;
                            break;
                        }
                        entries = new java.util.TreeSet(((java.util.Map)mapsiter.next()).keySet());
                        entriesiter = entries.iterator();
                    }
                }
                if (entriesiter != null) {
                    if (!entriesiter.hasNext())
                        entriesiter = null;
                }
            } while (true);
        }
        
        public boolean hasNext() {
            if (entry == null)
                getNext();
            return (entry != null);
        }

        public Object next() {
            if (entry == null)
                getNext();
            if (entry == null)
                throw new java.util.NoSuchElementException();
            MemoryEntry ret = entry;
            entry = null;
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public MemoryEntryIterator iterator() {
        return new MemoryEntryIterator();
    }
    
    public void dump(java.io.PrintStream stream) {
        stream.println("memory dump");
        for (MemoryEntryIterator iter = iterator(); iter.hasNext(); ) {
            MemoryEntry entry = (MemoryEntry)iter.next();
            stream.println("--> "+Integer.toHexString(entry.addr)+","+Integer.toHexString(entry.size));
        }
    }
    
    public void save() {
        for (MemoryEntryIterator iter = iterator(); iter.hasNext(); ) {
            MemoryEntry entry = (MemoryEntry)iter.next();
            entry.save();
        }
    }

    public void load() {
        for (MemoryEntryIterator iter = iterator(); iter.hasNext(); ) {
            MemoryEntry entry = (MemoryEntry)iter.next();
            entry.load();
        }
    }

}
