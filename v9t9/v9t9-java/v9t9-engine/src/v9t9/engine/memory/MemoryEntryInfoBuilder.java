/**
 * 
 */
package v9t9.engine.memory;

import java.util.Map;

import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class MemoryEntryInfoBuilder {

	private String domain = IMemoryDomain.NAME_CPU;
	private int unitSize;
	private int addr;
	private int size;
	private String filename;
	private String filename2;
	private int offset;
	private int offset2;
	private boolean isStored;
	private Class<? extends BankedMemoryEntry> klass;
	private String fileMD5;
	private String file2MD5;


	public static MemoryEntryInfoBuilder byteMemoryEntry() {
		return new MemoryEntryInfoBuilder(1);
	}
	public static MemoryEntryInfoBuilder wordMemoryEntry() {
		return new MemoryEntryInfoBuilder(2);
	}


	public MemoryEntryInfoBuilder(int unitSize) {
		this.unitSize = unitSize;
	}


	public MemoryEntryInfoBuilder withFilename(String string) {
		this.filename = string;
		return this;
	}

	public MemoryEntryInfoBuilder withFilename2(String string) {
		this.filename2 = string;
		return this;
	}
	
	
	public MemoryEntryInfoBuilder withFileMD5(String string) {
		this.fileMD5 = string;
		return this;
	}
	
	public MemoryEntryInfoBuilder withFile2MD5(String string) {
		this.file2MD5 = string;
		return this;
	}
	

	public MemoryEntryInfoBuilder withSize(int size) {
		this.size = size;
		return this;
	}


	public MemoryEntryInfoBuilder withOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public MemoryEntryInfoBuilder withOffset2(int offset2) {
		this.offset2 = offset2;
		return this;
	}
	public MemoryEntryInfoBuilder withAddress(int address) {
		this.addr = address;
		return this;
	}
	
	public MemoryEntryInfoBuilder withDomain(String domain) {
		this.domain = domain;
		return this;
	}
	
	public MemoryEntryInfoBuilder storable(boolean b) {
		this.isStored = b;
		return this;
	}

	public MemoryEntryInfo create(String name) {
		MemoryEntryInfo info = new MemoryEntryInfo();
		Map<String, Object> props = info.getProperties();
		props.put(MemoryEntryInfo.NAME, name);
		props.put(MemoryEntryInfo.DOMAIN, domain);
		props.put(MemoryEntryInfo.UNIT_SIZE, unitSize);
		if (filename != null) props.put(MemoryEntryInfo.FILENAME, filename);
		if (filename2 != null) props.put(MemoryEntryInfo.FILENAME2, filename2);
		if (fileMD5 != null) props.put(MemoryEntryInfo.FILE_MD5, fileMD5);
		if (file2MD5 != null) props.put(MemoryEntryInfo.FILE2_MD5, file2MD5);
		props.put(MemoryEntryInfo.ADDRESS, addr);
		props.put(MemoryEntryInfo.SIZE, size);
		props.put(MemoryEntryInfo.OFFSET, offset);
		props.put(MemoryEntryInfo.OFFSET2, offset2);
		props.put(MemoryEntryInfo.STORED, isStored);
		props.put(MemoryEntryInfo.CLASS, klass);
		
		return info;
	}

	public MemoryEntryInfoBuilder withBankClass(Class<? extends BankedMemoryEntry> klass) {
		this.klass = klass;
		return this;
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
