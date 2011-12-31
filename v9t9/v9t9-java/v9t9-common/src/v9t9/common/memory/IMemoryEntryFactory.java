/**
 * 
 */
package v9t9.common.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;


/**
 * @author ejs
 *
 */
public interface IMemoryEntryFactory {

	/**
	 * Read memory and create a memory entry with CPU byte ordering.
	 * @param addr
	 * @param size	the expected size of the entry (a maximum if != 0, else for 0, the
	 * actual size is used)
	 * @param name
	 * @param domain
	 * @param filepath
	 * @param fileoffs
	 * @param isStored if true, this is a RAM entry which can be rewritten
	 * @return new entry
	 * @throws IOException if file cannot be read, and is not stored
	 */
	IMemoryEntry newMemoryEntry(MemoryEntryInfo info) throws IOException;

	StoredMemoryEntryInfo resolveMemoryEntry(MemoryEntryInfo info, String name,
			String filename, int fileoffs) throws IOException;
	
	List<MemoryEntryInfo> loadEntriesFrom(String name, Element root);
	void saveEntriesTo(Collection<MemoryEntryInfo> memoryEntries, Element root);
	
	/**
	 * Create a memory entry from storage
	 * @param entryStore
	 * @return
	 */
	IMemoryEntry createEntry(IMemoryDomain domain, ISettingSection entryStore);

}