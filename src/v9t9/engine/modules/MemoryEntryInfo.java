/**
 * 
 */
package v9t9.engine.modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StdMultiBankedMemoryEntry;

/**
 * @author ejs
 *
 */
public class MemoryEntryInfo {

	/** Class<? extends MemoryEntry> */
	public final static String CLASS = "class";
	/** String */
	public final static String FILENAME = "fileName";
	/** String */
	public final static String FILENAME2 = "fileName2";
	/** String */
	public final static String DOMAIN = "domain";
	/** Integer */
	public final static String ADDRESS = "address";
	/** Integer */
	public final static String SIZE = "size";
	/** Integer */
	public final static String OFFSET = "offset";
	/** Integer */
	public final static String OFFSET2 = "offset2";
	/** Boolean */
	public final static String BANKED = "banked";
	/** Boolean */
	public final static String STANDARD_BANKING = "standardBanking";
	/** Integer */
	public final static String LATENCY = "latency";
	/** Boolean */
	public final static String STORED = "stored";

	private Map<String, Object> properties;
	
	public MemoryEntryInfo() {
		properties = new HashMap<String, Object>();
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @param memory
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public MemoryEntry createMemoryEntry(Memory memory) throws IOException {
		MemoryEntry entry = null;
		if (properties.containsKey(FILENAME2)) {
			entry = DiskMemoryEntry.newBankedWordMemoryFromFile(
					(Class) properties.get(CLASS),
					getInt(ADDRESS),
					getInt(SIZE),
					memory,
					getString(FILENAME),
					memory.getDomain(getString(DOMAIN)),
					getString(FILENAME),
					getInt(OFFSET),
					getString(FILENAME2),
					getInt(OFFSET2));
		} else if ("CPU".equals(properties.get(DOMAIN))) {
			entry = DiskMemoryEntry.newWordMemoryFromFile(
					getInt(ADDRESS),
					getInt(SIZE),
					getString(FILENAME),
					memory.getDomain(getString(DOMAIN)),
					getString(FILENAME),
					getInt(OFFSET),
					getBool(STORED));
		} else {
			entry = DiskMemoryEntry.newByteMemoryFromFile(
					getInt(ADDRESS),
					getInt(SIZE),
					getString(FILENAME),
					memory.getDomain(getString(DOMAIN)),
					getString(FILENAME),
					getInt(OFFSET),
					getBool(STORED));
		}
		return entry;
	}

	private int getInt(String name) {
		Integer i = (Integer) properties.get(name);
		if (i == null)
			return 0;
		return (int) i;
	}
	
	private String getString(String name) {
		String s = (String) properties.get(name);
		if (s == null)
			return "";
		return s;
	}
	
	private boolean getBool(String name) {
		Boolean s = (Boolean) properties.get(name);
		if (s == null)
			return false;
		return s.booleanValue();
	}
	
}
