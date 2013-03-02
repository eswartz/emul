/*
  MemoryEntryInfoBuilder.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.memory;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public class MemoryEntryInfoBuilder {
	private Map<String, Object> props;


	public static MemoryEntryInfoBuilder byteMemoryEntry() {
		return new MemoryEntryInfoBuilder(1);
	}
	public static MemoryEntryInfoBuilder wordMemoryEntry() {
		return new MemoryEntryInfoBuilder(2);
	}


	public MemoryEntryInfoBuilder(int unitSize) {
		this.props = new HashMap<String, Object>();
		props.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
		props.put(MemoryEntryInfo.UNIT_SIZE, unitSize);
	}


	public MemoryEntryInfoBuilder withFilename(String filename) {
		if (filename != null) props.put(MemoryEntryInfo.FILENAME, filename);
		return this;
	}

	public MemoryEntryInfoBuilder withFilename2(String filename2) {
		if (filename2 != null) props.put(MemoryEntryInfo.FILENAME2, filename2);
		return this;
	}
	
	
	public MemoryEntryInfoBuilder withFileMD5(String fileMD5) {
		if (fileMD5 != null) props.put(MemoryEntryInfo.FILE_MD5, fileMD5);
		return this;
	}
	
	public MemoryEntryInfoBuilder withFileMD5Limit(int md5Limit) {
		props.put(MemoryEntryInfo.FILE_MD5_LIMIT, md5Limit);
		return this;
	}
	
	public MemoryEntryInfoBuilder withFile2MD5(String file2MD5) {
		if (file2MD5 != null) props.put(MemoryEntryInfo.FILE2_MD5, file2MD5);
		return this;
	}
	
	public MemoryEntryInfoBuilder withFilenameProperty(SettingSchema schema) {
		if (schema != null) props.put(MemoryEntryInfo.FILENAME_PROPERTY, schema);
		return this;
	}


	public MemoryEntryInfoBuilder withSize(int size) {
		props.put(MemoryEntryInfo.SIZE, size);
		return this;
	}


	public MemoryEntryInfoBuilder withOffset(int offset) {
		props.put(MemoryEntryInfo.OFFSET, offset);
		return this;
	}

	public MemoryEntryInfoBuilder withOffset2(int offset2) {
		props.put(MemoryEntryInfo.OFFSET2, offset2);
		return this;
	}
	public MemoryEntryInfoBuilder withAddress(int address) {
		props.put(MemoryEntryInfo.ADDRESS, address);
		return this;
	}
	
	public MemoryEntryInfoBuilder withDomain(String domain) {
		props.put(MemoryEntryInfo.DOMAIN, domain);
		return this;
	}
	
	public MemoryEntryInfoBuilder storable(boolean isStored) {
		props.put(MemoryEntryInfo.STORED, isStored);
		return this;
	}

	public MemoryEntryInfoBuilder withDescription(String string) {
		if (string != null) props.put(MemoryEntryInfo.DESCRIPTION, string);
		return this;
	}
	
	public MemoryEntryInfoBuilder withBankClass(Class<? extends BankedMemoryEntry> klass) {
		if (klass != null) props.put(MemoryEntryInfo.CLASS, klass);
		return this;
	}


	public MemoryEntryInfo create(String name) {
		props.put(MemoryEntryInfo.NAME, name);
		MemoryEntryInfo info = new MemoryEntryInfo(props);
		return info;
	}

	public static MemoryEntryInfoBuilder standardConsoleRom(
			String filename) {
		return wordMemoryEntry()
			.withAddress(0)
			.withSize(0x2000)
			.withFilename(filename);
	}
	
	public static MemoryEntryInfoBuilder standardDsrRom(
			String filename) {
		return wordMemoryEntry()
			.withAddress(0x4000)
			.withSize(0x2000)
			.withFilename(filename);
	}
	
	public static MemoryEntryInfoBuilder standardConsoleGrom(
			String filename) {
		return byteMemoryEntry()
			.withDomain(IMemoryDomain.NAME_GRAPHICS)
			.withAddress(0)
			.withSize(0x6000)
			.withFilename(filename);
	}

	public static MemoryEntryInfoBuilder standardModuleGrom(String filename) {
		return byteMemoryEntry()
			.withDomain(IMemoryDomain.NAME_GRAPHICS)
			.withAddress(0x6000)
			.withFilename(filename);

	}

}
