/**
 * 
 */
package v9t9.common.memory;

/**
 * @author ejs
 *
 */
public interface IMemory {

	void addListener(MemoryListener listener);

	void removeListener(MemoryListener listener);

	void notifyListenersOfPhysicalChange(IMemoryEntry entry);

	void notifyListenersOfLogicalChange(IMemoryEntry entry);

	void addDomain(String key, MemoryDomain domain);

	MemoryDomain getDomain(String key);

	void addAndMap(IMemoryEntry entry);

	void removeAndUnmap(MemoryEntry entry);

	MemoryModel getModel();

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