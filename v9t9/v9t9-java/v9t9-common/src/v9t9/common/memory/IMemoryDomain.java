/*
  IMemoryDomain.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;

import java.io.IOException;

import v9t9.common.events.IEventNotifier;
import ejs.base.settings.ISettingSection;


/**
 * @author ejs
 *
 */
public interface IMemoryDomain {
    /**
	 * 
	 */
	String UNMAPPED_MEMORY_ID = "Unmapped memory";

	/*
	 * This must remain 64K, even if mega-memory expansion is emulated. All the
	 * public routines expect to be passed 16-bit addresses.
	 */
	public static final int PHYSMEMORYSIZE = 65536;
	/**
	 * An area is the smallest unit of memory which has the same essential
	 * behavior, as far as we know. We choose 1k because the TI-99/4A memory
	 * mapped areas for VDP, GROM, etc are accessed 1k apart from each other.
	 */
	static public final int AREASIZE = 1024;
	static public final int AREASHIFT = 10;
	public static final String NAME_GRAPHICS = "GRAPHICS";
	public static final String NAME_SPEECH = "SPEECH";
	public static final String NAME_VIDEO = "VIDEO";
	public static final String NAME_CPU = "CPU";

	IMemoryEntry getEntryAt(int addr);

	short flatReadWord(int addr);

	byte flatReadByte(int addr);

	void flatWriteByte(int addr, byte val);

	void flatWriteWord(int addr, short val);

	byte readByte(int addr);

	short readWord(int addr);

	void writeByte(int addr, byte val);

	void writeWord(int addr, short val);

	boolean hasRamAccess(int addr);

	boolean hasRomAccess(int addr);

	boolean isVolatile(int addr);

	boolean isStatic(int addr);

	/** Zero out the memory areas, setting them to zeroed-out ROM.
	 *	 
	 */
	void zero();

	void setAccessListener(IMemoryAccessListener listener);

	void addWriteListener(IMemoryWriteListener listener);

	void removeWriteListener(IMemoryWriteListener listener);

	int getLatency(int addr);

	/**
	 * Tell if the entry has been mapped at all -- though it may
	 * have been obscured in the meantime.
	 * @param memoryEntry
	 * @return true if the entry has been mapped
	 */
	boolean isEntryMapped(IMemoryEntry memoryEntry);

	/**
	 * Tell if the entry has been mapped and is fully visible
	 * @param memoryEntry
	 * @return true if all MemoryAreas for the entry are visible
	 */
	boolean isEntryFullyMapped(IMemoryEntry memoryEntry);

	/**
	 * Tell if the entry has been mapped but is fully obscured
	 * @param memoryEntry
	 * @return true if all MemoryAreas for the entry are covered
	 */
	boolean isEntryFullyUnmapped(IMemoryEntry memoryEntry);

	/**
	 * Map a memory entry, so that its range of addresses
	 * replace any handled by existing entries.
	 * @param memoryEntry
	 */
	void mapEntry(IMemoryEntry memoryEntry);

	/**
	 * Unmap a memory entry, exposing any entries previously mapped.
	 * @param memoryEntry
	 */
	void unmapEntry(IMemoryEntry memoryEntry);

	/**
	 * Quickly swap banked entries. 
	 * @param currentBank
	 * @param newBankEntry
	 */
	void switchBankedEntry(IMemoryEntry currentBank, IMemoryEntry newBankEntry);

	void saveState(ISettingSection section);

	void loadState(ISettingSection section);

	void unmapAll();

	IMemoryEntry[] getMemoryEntries();

	/**
	 * Get all the memory entries, with individual banks expanded
	 * @return
	 */
	IMemoryEntry[] getFlattenedMemoryEntries();

	void touchMemory(int addr);

	String getIdentifier();
	String getName();

	void loadMemory(IEventNotifier notifier, ISettingSection dSection);

	void save();

	/**
	 * @return
	 */
	IMemory getMemory();
	
	int getSize();
	void setSize(int size);

	boolean isWordAccess();

	void reset();
}