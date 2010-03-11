/**
 * 
 */
package v9t9.engine.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class MemoryEntryInfo {

	/** Class<? extends MemoryEntry> */
	public final static String CLASS = "class";
	/** String */
	public final static String NAME = "name";
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
	public MemoryEntry createMemoryEntry(Memory memory) throws NotifyException {
		try {
			MemoryEntry entry = null;
			if (properties.containsKey(FILENAME2)) {
				try {
					entry = DiskMemoryEntry.newBankedWordMemoryFromFile(
							(Class) properties.get(CLASS),
							getInt(ADDRESS),
							getInt(SIZE),
							memory,
							getString(NAME),
							memory.getDomain(getString(DOMAIN)),
							getFilePath(getString(FILENAME), getBool(STORED)),
							getInt(OFFSET),
							getFilePath(getString(FILENAME2), getBool(STORED)),
							getInt(OFFSET2));
				} catch (IOException e) {
					String filename = getString(FILENAME); 
					String filename2 = getString(FILENAME2); 
					if (filename2 == null)
					throw new NotifyException(null, 
							"Failed to load file(s) '" + filename + "' and/or '"+ filename2 + "' for '" + getString(NAME) + "'",
							e);
				}
			} else if ("CPU".equals(properties.get(DOMAIN))) {
				entry = DiskMemoryEntry.newWordMemoryFromFile(
						getInt(ADDRESS),
						getInt(SIZE),
						getString(NAME),
						memory.getDomain(getString(DOMAIN)),
						getFilePath(getString(FILENAME), getBool(STORED)),
						getInt(OFFSET),
						getBool(STORED));
			} else {
				entry = DiskMemoryEntry.newByteMemoryFromFile(
						getInt(ADDRESS),
						getInt(SIZE),
						getString(NAME),
						memory.getDomain(getString(DOMAIN)),
						getFilePath(getString(FILENAME), getBool(STORED)),
						getInt(OFFSET),
						getBool(STORED));
			}
			return entry;
		} catch (IOException e) {
			String filename = getString(FILENAME); 
			throw new NotifyException(null, "Failed to load file '" + filename + "' for '" + getString(NAME) +"'", e);
		}
	}

	/**
	 * @param filename
	 * @return
	 */
	private String getFilePath(String filename, boolean isStored) {
		if (isStored) {
			File existing = DataFiles.resolveFile(filename);
			if (existing != null && existing.exists())
				return existing.getAbsolutePath();
			
			String base = EmulatorSettings.INSTANCE.getBaseConfigurationPath();
			File storedMemory = new File(new File(base), "module_ram");
			storedMemory.mkdirs();
		
			return new File(storedMemory, filename).getAbsolutePath();
		}
		return filename;
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
