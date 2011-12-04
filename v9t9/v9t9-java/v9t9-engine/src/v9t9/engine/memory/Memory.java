/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *  
 */
package v9t9.engine.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import v9t9.base.settings.ISettingSection;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryListener;
import v9t9.common.memory.IMemoryModel;

/*
 * @author ejs
 */
public class Memory implements IMemory {

    private List<IMemoryListener> listeners;

	private final IMemoryModel model;

	private Map<String, IMemoryDomain> domains = new HashMap<String, IMemoryDomain>();

    /* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#addListener(v9t9.common.memory.MemoryListener)
	 */
    @Override
	public void addListener(IMemoryListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#removeListener(v9t9.common.memory.MemoryListener)
	 */
	@Override
	public void removeListener(IMemoryListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#notifyListenersOfPhysicalChange(v9t9.common.memory.IMemoryEntry)
	 */
	@Override
	public void notifyListenersOfPhysicalChange(IMemoryEntry entry) {
		if (listeners != null) {
			for (Iterator<IMemoryListener> iter = listeners.iterator(); iter
					.hasNext();) {
				IMemoryListener element = iter.next();
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
			for (Iterator<IMemoryListener> iter = listeners.iterator(); iter
					.hasNext();) {
				IMemoryListener element = iter.next();
				element.logicalMemoryMapChanged(entry);
			}
		}
	}
    
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#addDomain(java.lang.String, v9t9.common.memory.MemoryDomain)
	 */
	@Override
	public void addDomain(String key, IMemoryDomain domain) {
		this.domains.put(key, domain);
		((MemoryDomain) domain).memory = this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#getDomain(java.lang.String)
	 */
	@Override
	public IMemoryDomain getDomain(String key) {
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
	public void removeAndUnmap(IMemoryEntry entry) {
    	entry.getDomain().unmapEntry(entry);
    	notifyListenersOfPhysicalChange(entry);
    }
    
    public Memory(IMemoryModel model) {
        this.model = model;
		listeners = new java.util.ArrayList<IMemoryListener>();
    }

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#getModel()
	 */
	@Override
	public IMemoryModel getModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemory#saveState(v9t9.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		for (Map.Entry<String, IMemoryDomain> entry : domains.entrySet()) {
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
			IMemoryDomain domain = getDomain(domainName);
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
		return (IMemoryDomain[]) domains.values().toArray(new IMemoryDomain[domains.values().size()]);
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

