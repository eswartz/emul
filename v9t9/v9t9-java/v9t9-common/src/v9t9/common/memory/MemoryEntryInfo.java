/*
  MemoryEntryInfo.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

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
	/** String, hex-encoded */
	public final static String FILE_MD5 = "fileMd5";
	/** integer */
	public final static String FILE_MD5_LIMIT = "fileMd5Limit";
	/** String, hex-encoded */
	public final static String FILE2_MD5 = "file2Md5";
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
	/** Boolean: changes are stored */
	public final static String STORED = "stored";
	
	/** Int */
	public static final String UNIT_SIZE = "unitSize";
	/** String */
	public static final String DESCRIPTION = "description";
	/** SettingsSchema */
	public static final String FILENAME_PROPERTY = "fileProperty";

	/** Boolean: whether bank order is reversed */
	public final static String REVERSED = "reversed";

	private Map<String, Object> properties;
	
	public MemoryEntryInfo() {
		properties = new HashMap<String, Object>();
	}

	
	/**
	 * @param props
	 */
	public MemoryEntryInfo(Map<String, Object> props) {
		this.properties = props;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemoryEntryInfo other = (MemoryEntryInfo) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}


	public Map<String, Object> getProperties() {
		return properties;
	}

	public int getInt(String name) {
		Integer i = (Integer) properties.get(name);
		if (i == null)
			return 0;
		return (int) i;
	}
	
	public String getString(String name) {
		String s = (String) properties.get(name);
		return s;
	}
	
	public boolean getBool(String name) {
		Boolean s = (Boolean) properties.get(name);
		if (s == null)
			return false;
		return s.booleanValue();
	}


	public boolean isStored() {
		return getBool(MemoryEntryInfo.STORED);
	}


	public String getFilename() {
		return getString(MemoryEntryInfo.FILENAME);
	}
	

	public SettingSchema getFilenameProperty() {
		return (SettingSchema) properties.get(MemoryEntryInfo.FILENAME_PROPERTY);
	}
	
	public String getFileMD5() {
		return getString(MemoryEntryInfo.FILE_MD5);
	}


	public IMemoryDomain getDomain(IMemory memory) {
		return memory.getDomain(getString(MemoryEntryInfo.DOMAIN));
	}


	public String getName() {
		return getString(MemoryEntryInfo.NAME);
	}


	public int getSize() {
		return getInt(MemoryEntryInfo.SIZE);
	}


	public int getAddress() {
		return getInt(MemoryEntryInfo.ADDRESS);
	}

	public String getFilename2() {
		return getString(MemoryEntryInfo.FILENAME2);
	}
	public String getFile2MD5() {
		return getString(MemoryEntryInfo.FILE2_MD5);
	}

	public int getOffset() {
		return getInt(MemoryEntryInfo.OFFSET);
	}

	public int getOffset2() {
		return getInt(MemoryEntryInfo.OFFSET2);
	}

	public boolean isBanked() { 
		if (getFilename() != null && getFilename2() != null
		&& !getFilename().equals(getFilename2()))
			return true;
		if (getBankedClass() != null)
			return true;
		return false;
	}


	public boolean isByteSized() {
		return getInt(MemoryEntryInfo.UNIT_SIZE) == 1;
	}


	public Class<?> getBankedClass() {
		return (Class<?>) properties.get(CLASS);
	}

	public boolean isDefaultFilename(ISettingsHandler settings) {
		return getFilenameProperty() != null && 
				settings.get(getFilenameProperty()).getValue().equals(
						getFilenameProperty().getDefaultValue());
	}

	public String getResolvedFilename(ISettingsHandler settings) {
		if (getFilenameProperty() != null)
			return settings.get(getFilenameProperty()).getString();
		else
			return getFilename();
	}


	public String getDescription() {
		return (String) properties.get(DESCRIPTION);
	}
	
	/**
	 * @return the fileMd5Limit
	 */
	public int getFileMd5Limit() {
		return getInt(FILE_MD5_LIMIT);
	}


	/**
	 * @param storedInfo
	 * @return
	 */
	public boolean matches(StoredMemoryEntryInfo storedInfo) {
		if (getFilenameProperty() != null && storedInfo.fileName.equals(getFilenameProperty().getDefaultValue()))
			return true;
		if (getFileMD5() == null || storedInfo.md5.equals(getFileMD5())) 
			return true;
//		if (getFile2MD5() != null && storedInfo.md5.equals(getFile2MD5()))
//			return true;
		return false;
	}


	/**
	 * @return
	 */
	public boolean isReversed() {
		return getBool(REVERSED);
	}

}
