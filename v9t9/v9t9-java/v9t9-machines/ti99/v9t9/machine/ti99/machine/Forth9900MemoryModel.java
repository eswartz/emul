/*
  F99bMemoryModel.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.MultiBankedMemoryEntry;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.memory.BaseTI994AMemoryModel;
import v9t9.machine.ti99.memory.EnhancedRamByteArea;
import v9t9.machine.ti99.memory.mmio.Forth9900ConsoleMmioArea;
import ejs.base.properties.IProperty;
import ejs.base.utils.FileUtils;
import ejs.base.utils.Pair;


/**
 * F99b console memory model.
 * @author ejs
 */
public class Forth9900MemoryModel extends BaseTI994AMemoryModel {
	public class Forth9900ConsoleMemoryEntry extends MemoryEntry {
		private Forth9900ConsoleMemoryEntry(String name, IMemoryDomain domain,
				int addr, int size, MemoryArea area) {
			super(name, domain, addr, size, area);
		}

		@Override
		public String lookupSymbol(short addr) {
			String symb = super.lookupSymbol(addr);
			if (symb == null)
				symb = bankedRomMemoryEntry.lookupSymbol(addr);
			return symb;
		}

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.MemoryEntry#findSymbol(java.lang.String)
		 */
		@Override
		public Integer findSymbol(String name) {
			Integer addr = super.findSymbol(name);
			if (addr == null)
				addr = bankedRomMemoryEntry.findSymbol(name);
			return addr;
		}
		@Override
		public Pair<String, Short> lookupSymbolNear(short addr,
				int range) {
			Pair<String, Short> info = super.lookupSymbolNear(addr, range);
			if (info == null)
				info = bankedRomMemoryEntry.lookupSymbolNear(addr, range);
			return info;
		}
	}

	private static final Logger log = Logger.getLogger(Forth9900MemoryModel.class);
	
	private MemoryEntry consoleEntry;

	private IMemoryEntry gromEntry;

	private IMemoryEntry gramDictEntry;

	private IMemoryEntry gramMemoryEntry;

	private IMemoryEntry cpuForthRomEntry;
	private IMemoryEntry cpuRomBankEntry;

	private MemoryEntry mmioEntry;

	private MultiBankedMemoryEntry bankedRomMemoryEntry;

	private Forth9900ConsoleMmioArea mmioArea;

	public Forth9900MemoryModel(IMachine machine) {
		super(machine);
	}

	@Override
	protected void initSettings(ISettingsHandler settings) {
		URL dataURL;
		dataURL = EmulatorMachinesData.getDataURL("../../../build/forth99");
		DataFiles.addSearchPath(settings, dataURL.getPath());
		
		IProperty shipPath = settings.get(DataFiles.settingShippingRomsPath);
		dataURL = EmulatorMachinesData.getDataURL("f9900/");
		shipPath.getList().add(dataURL.toString());
	}


	protected void defineConsoleMemory(IBaseMachine machine) {
		consoleEntry = new MemoryEntry("64K RAM", CPU, 
				0x0400, 0xFC00, new EnhancedRamByteArea(0, 0xFC00));
		memory.addAndMap(consoleEntry);
	}
	
	protected void defineMmioMemory(IBaseMachine machine) {
		//this.memory.addAndMap(mmioEntry);
		mmioArea = new Forth9900ConsoleMmioArea((IMachine) machine);
		mmioEntry = new Forth9900ConsoleMemoryEntry("MMIO", CPU, 0x0000, 0x0400, mmioArea);
	}
	
	private static MemoryEntryInfo f9900ForthRomMemoryEntryInfo = MemoryEntryInfoBuilder
		.wordMemoryEntry()
		.withFilename("f9900rombank0.bin")
		.withSize(-0x10000)
		.create("Forth9900 Forth ROM");

	private static MemoryEntryInfo f9900BankedRomMemoryEntryInfo = MemoryEntryInfoBuilder
			.wordMemoryEntry()
			.withFilename("f9900rombank1.bin")
			.withSize(-0x10000)
			.create("Forth9900 CPU ROM Bank");

	private static String FORTH_GROM = "f9900grom.bin";
	
	private static MemoryEntryInfo f9900GromMemoryEntryInfo = MemoryEntryInfoBuilder
		.standardConsoleGrom(FORTH_GROM)
		.withSize(-0x4000)
		.create("Forth9900 CPU GROM");


	private static MemoryEntryInfo f9900GramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withDomain(IMemoryDomain.NAME_GRAPHICS)
		.withAddress(0x4000)
		.withSize(0x4000)
		.create("Forth9900 16K GRAM Dictionary");
	
	private static MemoryEntryInfo f9900DiskGramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withFilename("f9900gram.bin")
		.withDomain(IMemoryDomain.NAME_GRAPHICS)
		.withAddress(0x8000)
		.withSize(0x8000)
		.storable(true)
		.create("Forth99 GRAM");

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.StandardConsoleMemoryModel#loadMemory()
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
    	try {
    		// get the FORTH ROM (bank #0)
			cpuForthRomEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f9900ForthRomMemoryEntryInfo);
			cpuForthRomEntry.load();
			CPU.mapEntry(cpuForthRomEntry);
			cpuForthRomEntry.copySymbols(CPU);
			
    		// get the CPU ROM (bank #1)
			cpuRomBankEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f9900BankedRomMemoryEntryInfo);
			cpuRomBankEntry.load();
			CPU.mapEntry(cpuRomBankEntry);
			cpuRomBankEntry.copySymbols(CPU);
			
			// Get the meat of the ROM from the assembly bank
			Integer endShared = cpuRomBankEntry.findSymbol("ForthROM");
			if (endShared == null)
				endShared = cpuRomBankEntry.findSymbol("_RESET");		// both symbols at same addr, one wins
			if (endShared != null) {
				boolean changed = false;
				short prevValue = 0;
				boolean invertSoc = false;
				for (int addr = 0; addr < endShared; addr+=2) {
					short value = cpuRomBankEntry.flatReadWord(addr);
					if (prevValue == 0x720) { // SETO @>
						//String sym= cpuRomBankEntry.lookupSymbol((short) (addr - 2));
						if (value == 4) {		// we're in bank 0
							invertSoc = true;
						}
					} else if (invertSoc && value == (short) 0xe3e0) {	// SOC @>...,R15
						value = 0x43e0;	// SZC @>...,R15
						invertSoc = false;
					} else {
						invertSoc = false;
					}
					changed |= cpuForthRomEntry.patchWord(addr, value);
					prevValue = value;
				}
				if (changed) {
					DiskMemoryEntry ent = (DiskMemoryEntry) cpuForthRomEntry;
					ent.overwrite();
				}
			}
			
			memory.removeAndUnmap(cpuRomBankEntry);
			memory.removeAndUnmap(cpuForthRomEntry);
			memory.removeAndUnmap(consoleEntry);
			
			// Make the RAM area for the Forth RAM dictionary/etc., 
			// which lives on the area boundary past the Forth ROM 
			int st = cpuForthRomEntry.getAddr() + 0x400 * ((cpuForthRomEntry.getSize() + 0x3ff) / 0x400);
			int sz = 0x10000 - st;

			consoleEntry = new MemoryEntry("64K RAM", CPU, 
					st, sz, new EnhancedRamByteArea(0, sz));
			memory.addAndMap(consoleEntry);

			// Make the bank-switching ROM
			bankedRomMemoryEntry = new MultiBankedMemoryEntry(settings, memory, "Forth/ROM Bank",  
					new IMemoryEntry[] { cpuForthRomEntry, cpuRomBankEntry });
					
			mmioArea.setUnderlyingRomEntry(bankedRomMemoryEntry);
			
			memory.addAndMap(bankedRomMemoryEntry);
			
			memory.addAndMap(mmioEntry);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, f9900ForthRomMemoryEntryInfo.getFilename(), e);
    	}

    	loadGromAndGram(eventNotifier);
	}
	
	/**
	 * @param eventNotifier 
	 * 
	 */
	private void loadGromAndGram(IEventNotifier eventNotifier) {
    	// GROM consists of ROM up to 16k
		try {
			memory.removeAndUnmap(gromEntry);
			
			gromEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f9900GromMemoryEntryInfo);
			
			memory.addAndMap(gromEntry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, FORTH_GROM, e);
		}
		
		// then 16k of GRAM for new dictionary
		try {
			memory.removeAndUnmap(gramDictEntry);
			gramDictEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f9900GramMemoryEntryInfo);
			gramDictEntry.getArea().setLatency(0);
			memory.addAndMap(gramDictEntry);
		} catch (IOException e1) {
			// should not happen
			reportLoadError(eventNotifier, f9900GramMemoryEntryInfo.getFilename(), e1);
		}
		
		// then 32k of GRAM storage
		IProperty shipPath = settings.get(DataFiles.settingShippingRomsPath);
		URI shippingDiskImage = URI.create(shipPath.getList().get(0) + f9900DiskGramMemoryEntryInfo.getFilename());
		URI userDiskImage = machine.getRomPathFileLocator().getWriteURI(f9900DiskGramMemoryEntryInfo.getFilename());
		File userDiskImageFile = new File(userDiskImage);
		if (shippingDiskImage != null && userDiskImage == null || !userDiskImageFile.exists()) {
			userDiskImageFile.getParentFile().mkdirs();
			InputStream is = null;
			OutputStream os = null;
			try {
				is = machine.getRomPathFileLocator().createInputStream(shippingDiskImage);
				os = machine.getRomPathFileLocator().createOutputStream(userDiskImage);
				byte[] content = FileUtils.readInputStreamContentsAndClose(is);
				FileUtils.writeOutputStreamContentsAndClose(os, content, content.length);
			} catch (IOException e) {
				log.error("Failed to copy initial disk image from " + shippingDiskImage + " to " + userDiskImage, e);
//				eventNotifier.notifyEvent(this, Level.ERROR, 
//						"Failed to copy initial disk image from " + shippingDiskImage + " to " + userDiskImage); 
			} finally {
				try { if (is != null) is.close(); } catch (IOException e) { }
				try { if (os != null) os.close(); } catch (IOException e) { }
			}
		}
		
		try {
			memory.removeAndUnmap(gramMemoryEntry);
			gramMemoryEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f9900DiskGramMemoryEntryInfo);
			memory.addAndMap(gramMemoryEntry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, f9900DiskGramMemoryEntryInfo.getFilename(), e);
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.memory.TI994AStandardConsoleMemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getRequiredRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getRequiredRomMemoryEntries() {
		return new MemoryEntryInfo[] { 
				f9900ForthRomMemoryEntryInfo,
				f9900GromMemoryEntryInfo,
				f9900DiskGramMemoryEntryInfo,
				};
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getOptionalRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getOptionalRomMemoryEntries() {
		return new MemoryEntryInfo[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.memory.BaseTI994AMemoryModel#initMemory(v9t9.common.machine.IBaseMachine)
	 */
	@Override
	public void initMemory(IBaseMachine machine) {
		gplMmio = new GplMmio(machine, GRAPHICS); 
		super.initMemory(machine);
	}
}
