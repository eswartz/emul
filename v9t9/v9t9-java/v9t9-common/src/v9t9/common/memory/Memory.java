/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *  
 */
package v9t9.common.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import v9t9.base.properties.IPersistable;
import v9t9.base.settings.ISettingSection;

/*
 * @author ejs
 */
public class Memory implements IPersistable, IMemory {

    private List<MemoryListener> listeners;

	private final MemoryModel model;

	private Map<String, MemoryDomain> domains = new HashMap<String, MemoryDomain>();

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#addListener(v9t9.common.memory.MemoryListener)
	 */
    @Override
	public void addListener(MemoryListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#removeListener(v9t9.common.memory.MemoryListener)
	 */
	@Override
	public void removeListener(MemoryListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#notifyListenersOfPhysicalChange(v9t9.common.memory.IMemoryEntry)
	 */
	@Override
	public void notifyListenersOfPhysicalChange(IMemoryEntry entry) {
		if (listeners != null) {
			for (Iterator<MemoryListener> iter = listeners.iterator(); iter
					.hasNext();) {
				MemoryListener element = iter.next();
				element.physicalMemoryMapChanged(entry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#notifyListenersOfLogicalChange(v9t9.common.memory.IMemoryEntry)
	 */
	@Override
	public void notifyListenersOfLogicalChange(IMemoryEntry entry) {
		if (listeners != null) {
			for (Iterator<MemoryListener> iter = listeners.iterator(); iter
					.hasNext();) {
				MemoryListener element = iter.next();
				element.logicalMemoryMapChanged(entry);
			}
		}
	}
    
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#addDomain(java.lang.String, v9t9.common.memory.MemoryDomain)
	 */
	@Override
	public void addDomain(String key, MemoryDomain domain) {
		this.domains.put(key, domain);
		domain.memory = this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#getDomain(java.lang.String)
	 */
	@Override
	public MemoryDomain getDomain(String key) {
		return domains.get(key);
	}
	
    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#addAndMap(v9t9.common.memory.IMemoryEntry)
	 */
    @Override
	public void addAndMap(IMemoryEntry entry) {
        entry.getDomain().mapEntry(entry);
        notifyListenersOfPhysicalChange(entry);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#removeAndUnmap(v9t9.common.memory.MemoryEntry)
	 */
    @Override
	public void removeAndUnmap(MemoryEntry entry) {
    	entry.getDomain().unmapEntry(entry);
    	notifyListenersOfPhysicalChange(entry);
    }
    
    public Memory(MemoryModel model) {
        this.model = model;
		listeners = new java.util.ArrayList<MemoryListener>();
    }

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#getModel()
	 */
	@Override
	public MemoryModel getModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#saveState(v9t9.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		for (Map.Entry<String, MemoryDomain> entry : domains.entrySet()) {
			entry.getValue().saveState(section.addSection(entry.getKey()));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#loadState(v9t9.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		
		Set<String> notSaved = new HashSet<String>(domains.keySet()); 
		
		for (String domainName : section.getSectionNames()) {
			MemoryDomain domain = getDomain(domainName);
			if (domain != null) {
				notSaved.remove(domainName);
			} else {
				domain = new MemoryDomain(domainName);
				addDomain(domainName, domain);
			}
			ISettingSection dSection = section.getSection(domainName);
			domain.loadState(dSection);
		}
		
		for (IMemoryDomain domain : domains.values()) {
			if (notSaved.contains(domain.getName())) {
				domain.unmapAll();
			}
		}
		domains.keySet().removeAll(notSaved);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#getDomains()
	 */
	@Override
	public IMemoryDomain[] getDomains() {
		return (IMemoryDomain[]) domains.values().toArray(new MemoryDomain[domains.values().size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#save()
	 */
	@Override
	public void save() {
		for (IMemoryDomain domain : domains.values()) {
			domain.save();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#clear()
	 */
	@Override
	public void clear() {
		for (IMemoryDomain domain : getDomains()) {
			domain.unmapAll();
		}
	}

}

