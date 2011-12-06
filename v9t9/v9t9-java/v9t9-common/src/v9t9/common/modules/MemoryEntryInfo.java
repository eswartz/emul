/**
 * 
 */
package v9t9.common.modules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;

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
	 * @param settings TODO
	 * @param filename
	 * @return
	 */
	public String getFilePath(ISettingsHandler settings, String baseStoredDir, String filename, boolean isStored) {
		if (isStored) {
			File existing = DataFiles.resolveFile(settings, filename);
			if (existing != null && existing.exists())
				return existing.getAbsolutePath();
			
			File storedMemory = new File(new File(baseStoredDir), "module_ram");
			storedMemory.mkdirs();
		
			return new File(storedMemory, filename).getAbsolutePath();
		}
		return filename;
	}

	public int getInt(String name) {
		Integer i = (Integer) properties.get(name);
		if (i == null)
			return 0;
		return (int) i;
	}
	
	public String getString(String name) {
		String s = (String) properties.get(name);
		if (s == null)
			return "";
		return s;
	}
	
	public boolean getBool(String name) {
		Boolean s = (Boolean) properties.get(name);
		if (s == null)
			return false;
		return s.booleanValue();
	}
	
}
