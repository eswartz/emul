/*
  MemoryEntryInfoBuilder.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	
	public MemoryEntryInfoBuilder withAddress2(int addr) {
		props.put(MemoryEntryInfo.ADDRESS2, addr);
		return this;
	}
	
	public MemoryEntryInfoBuilder withSize2(int size) {
		props.put(MemoryEntryInfo.SIZE2, size);
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
	public MemoryEntryInfoBuilder withFileMD5Offset(int offs) {
		props.put(MemoryEntryInfo.FILE_MD5_OFFSET, offs);
		return this;
	}
	
	public MemoryEntryInfoBuilder withFile2MD5(String file2MD5) {
		if (file2MD5 != null) props.put(MemoryEntryInfo.FILE2_MD5, file2MD5);
		return this;
	}

	public MemoryEntryInfoBuilder withFile2MD5Limit(int md5Limit) {
		props.put(MemoryEntryInfo.FILE2_MD5_LIMIT, md5Limit);
		return this;
	}
	public MemoryEntryInfoBuilder withFile2MD5Offset(int md5Limit) {
		props.put(MemoryEntryInfo.FILE2_MD5_OFFSET, md5Limit);
		return this;
	}
	

	public MemoryEntryInfoBuilder withFilenameProperty(SettingSchema schema) {
		if (schema != null) props.put(MemoryEntryInfo.FILENAME_PROPERTY, schema);
		return this;
	}
	public MemoryEntryInfoBuilder withFilename2Property(SettingSchema schema) {
		if (schema != null) props.put(MemoryEntryInfo.FILENAME2_PROPERTY, schema);
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
	public MemoryEntryInfoBuilder withBankSize(int size) {
		if (size != 0)
			props.put(MemoryEntryInfo.BANK_SIZE, size);
		else
			props.remove(MemoryEntryInfo.BANK_SIZE);
		return this;
	}

	public MemoryEntryInfoBuilder isReversed(boolean b) {
		props.put(MemoryEntryInfo.REVERSED, b);
		return this;
	}

	public MemoryEntryInfoBuilder withLatency(int latency) {
		props.put(MemoryEntryInfo.LATENCY, latency);
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

	public static MemoryEntryInfoBuilder standardModuleRom(
			String filename) {
		return wordMemoryEntry()
				.withAddress(0x6000)
				.withSize(0x2000)
				.withFilename(filename);
	}
	
	public static MemoryEntryInfoBuilder standardModuleGrom(String filename) {
		return byteMemoryEntry()
			.withDomain(IMemoryDomain.NAME_GRAPHICS)
			.withAddress(0x6000)
			.withFilename(filename);

	}

}
