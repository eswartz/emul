/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *  
 */
package v9t9.engine.memory;

import java.util.Iterator;
import java.util.List;

/*
 * @author ejs
 */
public class Memory {

    public MemoryMap map;

    public static ZeroWordMemoryArea zeroWordMemoryArea = new ZeroWordMemoryArea();

    private List<MemoryListener> listeners;
    
    public void addListener(MemoryListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MemoryListener listener) {
		listeners.remove(listener);
	}

	public void notifyListeners(MemoryEntry entry) {
		if (listeners != null) {
			for (Iterator<MemoryListener> iter = listeners.iterator(); iter
					.hasNext();) {
				MemoryListener element = iter.next();
				element.notifyMemoryMapChanged(entry);
			}
		}
	}
    
    public void addAndMap(MemoryEntry entry) {
        map.add(entry);
        entry.map();
        notifyListeners(entry);
    }
    
    public void addDomain(MemoryDomain domain) {
        map.add(domain);
    }
    
    public Memory() {
        listeners = new java.util.ArrayList<MemoryListener>();
        
        map = new MemoryMap();
    }
  }

