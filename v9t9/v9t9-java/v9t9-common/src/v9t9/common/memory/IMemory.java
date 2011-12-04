/**
 * 
 */
package v9t9.common.memory;

import v9t9.base.properties.IPersistable;

/**
 * @author ejs
 *
 */
public interface IMemory extends IPersistable {

	void addListener(IMemoryListener listener);

	void removeListener(IMemoryListener listener);

	void notifyListenersOfPhysicalChange(IMemoryEntry entry);

	void notifyListenersOfLogicalChange(IMemoryEntry entry);

	void addDomain(String key, IMemoryDomain domain);

	IMemoryDomain getDomain(String key);

	void addAndMap(IMemoryEntry entry);

	void removeAndUnmap(IMemoryEntry entry);

	IMemoryModel getModel();

	IMemoryDomain[] getDomains();

	/**
	 * 
	 */
	void save();

	/**
	 * 
	 */
	void clear();

}