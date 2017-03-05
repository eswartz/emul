/*
  MemoryEntryInfo.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.MD5FilterAlgorithms;
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
	/** String */
	public final static String FILE_MD5_ALGORITHM = "fileMd5Algorithm";
	/** integer */
	public final static String FILE_MD5_LIMIT = "fileMd5Limit";
	/** integer */
	public final static String FILE_MD5_OFFSET = "fileMd5Offset";
	/** String, hex-encoded */
	public final static String FILE2_MD5 = "file2Md5";
	/** String */
	public final static String FILE2_MD5_ALGORITHM = "file2Md5Algorithm";
	/** integer */
	public final static String FILE2_MD5_LIMIT = "file2Md5Limit";
	/** integer */
	public final static String FILE2_MD5_OFFSET = "file2Md5Offset";
	/** String */
	public final static String DOMAIN = "domain";
	/** Integer */
	public final static String ADDRESS = "address";
	/** Integer */
	public final static String ADDRESS2 = "address2";
	/** Integer */
	public final static String SIZE = "size";
	/** Integer */
	public final static String SIZE2 = "size2";
	/** Integer */
	public final static String OFFSET = "offset";
	/** Integer */
	public final static String OFFSET2 = "offset2";
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
	/** SettingsSchema */
	public static final String FILENAME2_PROPERTY = "file2Property";

	/** int: bank size */
	public final static String BANK_SIZE = "bankSize";

	/** Boolean: whether bank order is reversed */
	public final static String REVERSED = "reversed";

	/** Boolean: whether the memory had a recognized header (0xAA) */
	public final static String HAS_HEADER = "hasHeader";

	private Map<String, Object> properties;
	
	public MemoryEntryInfo() {
		properties = new HashMap<String, Object>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return properties.toString();
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

	public int getLatency() {
		Integer i = (Integer) properties.get(MemoryEntryInfo.LATENCY);
		if (i == null)
			return -1;
		return (int) i;
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
	
	
	public SettingSchema getFilename2Property() {
		return (SettingSchema) properties.get(MemoryEntryInfo.FILENAME2_PROPERTY);
	}
	
	public String getFileMD5() {
		return getString(MemoryEntryInfo.FILE_MD5);
	}

	public String getDefaultMD5Algorithm() {
		if (IMemoryDomain.NAME_GRAPHICS.equals(getDomainName())) {
			return MD5FilterAlgorithms.ALGORITHM_GROM;
		} else {
			return MD5FilterAlgorithms.ALGORITHM_FULL;
		}
	}
	
	public String getFileMD5Algorithm() {
		return getString(MemoryEntryInfo.FILE_MD5_ALGORITHM);
	}
	
	public int getDefaultFileSize() {
		if (IMemoryDomain.NAME_CPU.equals(getDomainName()))
			return 0x2000;
		return 0;
	}
	public String getEffectiveFileMD5Algorithm() {
		String alg = getFileMD5Algorithm();
		if (alg == null || alg.isEmpty()) {
			if (getOffset() != 0 || (getSize() > 0 && getSize() != getDefaultFileSize()))
				alg = MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":" + 
					new IMD5SumFilter.FilterSegment(getOffset(), getSize()).toString();
			else
				alg = getDefaultMD5Algorithm();
		}
		return alg;
	}

	public String getDomainName() {
		return getString(MemoryEntryInfo.DOMAIN);
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
	public int getSize2() {
		return getInt(MemoryEntryInfo.SIZE2);
	}


	public int getAddress() {
		return getInt(MemoryEntryInfo.ADDRESS);
	}
	public int getAddress2() {
		return getInt(MemoryEntryInfo.ADDRESS2);
	}

	public String getFilename2() {
		return getString(MemoryEntryInfo.FILENAME2);
	}
	public String getFile2MD5() {
		return getString(MemoryEntryInfo.FILE2_MD5);
	}
	public String getFile2MD5Algorithm() {
		return getString(MemoryEntryInfo.FILE2_MD5_ALGORITHM);
	}
	public String getEffectiveFile2MD5Algorithm() {
		String alg = getFile2MD5Algorithm();
		if (alg == null || alg.isEmpty()) {
			if (getOffset2() != 0 || (getSize2() > 0 && getSize2() != getDefaultFileSize()))
				alg = MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":" + 
					new IMD5SumFilter.FilterSegment(getOffset2(), getSize2()).toString();
			else
				alg = getDefaultMD5Algorithm();
		}
		return alg;
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
		SettingSchema filenameProperty = getFilenameProperty();
		if (filenameProperty == null)
			return true;
		Object filenameProp = settings.get(filenameProperty).getValue();
		Object filenameDefault = filenameProperty.getDefaultValue();
		return filenameProperty != null && 
				filenameProp.equals(filenameDefault);
	}

	public String getResolvedFilename(ISettingsHandler settings) {
		if (getFilenameProperty() != null && settings != null)
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
	 * @return the file2Md5Limit
	 */
	public int getFile2Md5Limit() {
		return getInt(FILE2_MD5_LIMIT);
	}

	/**
	 * @return the file2Md5Offset
	 */
	public int getFile2Md5Offset() {
		return getInt(FILE2_MD5_OFFSET);
	}
	/**
	 * @return the fileMd5Offset
	 */
	public int getFileMd5Offset() {
		return getInt(FILE_MD5_OFFSET);
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
		return false;
	}


	/**
	 * @return
	 */
	public boolean isReversed() {
		return getBool(REVERSED);
	}

	public int getBankSize() {
		return getInt(BANK_SIZE);
	}

	public MemoryEntryInfo asBank(int number, int offset, int size) {
		MemoryEntryInfo copy = new MemoryEntryInfo(new HashMap<String, Object>(properties));
		copy.getProperties().put(NAME, getName() + " (bank " + number + ")"); 
		copy.getProperties().put(OFFSET, offset);
		if (size > 0)
			copy.getProperties().put(SIZE, size); 
		return copy;
	}

	public MemoryEntryInfo asFirstBank() {
		int bankSize = getBankSize();
		if (bankSize == 0)
			bankSize = 0x2000;
		MemoryEntryInfo copy = asBank(0, 0, bankSize);
		Map<String, Object> props = copy.getProperties();
		props.remove(FILENAME2);
		props.remove(OFFSET2);
		props.remove(ADDRESS2);
		props.remove(SIZE2);
		props.remove(FILE2_MD5);
		props.remove(FILE2_MD5_ALGORITHM);
		props.remove(FILE2_MD5_LIMIT);
		props.remove(FILE2_MD5_OFFSET);
		props.remove(FILENAME2_PROPERTY);
		return copy;
	}
	
	public MemoryEntryInfo asSecondBank() {
		int bankSize = getBankSize();
		if (bankSize == 0)
			bankSize = 0x2000;
		MemoryEntryInfo copy = asBank(1, 0, bankSize);
		Map<String, Object> props = copy.getProperties();
		move(props, FILENAME, getFilename2());
		move(props, ADDRESS, getAddress2() != 0 ? getAddress2() : getAddress());
		move(props, SIZE, getSize2() != 0 ? getSize2() : getSize());
		move(props, OFFSET, getOffset2());
		move(props, FILE_MD5, getFile2MD5());
		move(props, FILE_MD5_ALGORITHM, getFile2MD5Algorithm());
		move(props, FILE_MD5_OFFSET, getFile2Md5Offset());
		move(props, FILE_MD5_LIMIT, getFile2Md5Limit());
		move(props, FILENAME_PROPERTY, getFilename2Property());
		return copy;
	}

	private void move(Map<String, Object> props, String key, Object val) {
		if (val != null) {
			props.put(key, val);
			props.remove(key + "2");
			if (key.length() > 4)
				props.remove(key.substring(0, 4) + "2" + key.substring(4));
		}
	}

	/**
	 * Update properties from an older version (e.g. the unnamed version "1" 
	 * which used file MD5s, offsets, and limits, to the 
	 * current version "2" which uses a single MD5 algorithm).
	 */
	public void updateProperties() {
		boolean isGrom = IMemoryDomain.NAME_GRAPHICS.equals(getDomainName());
		
		int offset = getInt(FILE_MD5_OFFSET);
		properties.remove(FILE_MD5_OFFSET);
		int limit = getInt(FILE_MD5_LIMIT);
		properties.remove(FILE_MD5_LIMIT);
		if (properties.containsKey(FILE_MD5)) {
			String algorithm = isGrom ? MD5FilterAlgorithms.ALGORITHM_FULL : null;
			if (offset < limit) {
				algorithm = new MD5FilterAlgorithms.FileSegmentFilter(offset, limit).getId();
			}
			if (algorithm != null) {
				properties.put(FILE_MD5_ALGORITHM, algorithm);
			}
		}

		offset = getInt(FILE2_MD5_OFFSET);
		properties.remove(FILE2_MD5_OFFSET);
		limit = getInt(FILE2_MD5_LIMIT);
		properties.remove(FILE2_MD5_LIMIT);
		if (properties.containsKey(FILE2_MD5)) {
			String algorithm = isGrom ? MD5FilterAlgorithms.ALGORITHM_FULL : null;
			if (offset < limit) {
				algorithm = new MD5FilterAlgorithms.FileSegmentFilter(offset, limit).getId();
			}
			if (algorithm != null) {
				properties.put(FILE2_MD5_ALGORITHM, algorithm);
			}
		}
	}

}
