/*
  F99bMemoryModel.java

  (c) 2010-2015 Edward Swartz

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
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.memory.BaseTI994AMemoryModel;
import v9t9.machine.ti99.memory.EnhancedRamByteArea;
import v9t9.machine.ti99.memory.mmio.Forth9900ConsoleMmioArea;
import ejs.base.properties.IProperty;
import ejs.base.utils.FileUtils;


/**
 * F99b console memory model.
 * @author ejs
 */
public class Forth9900StandaloneMemoryModel extends BaseTI994AMemoryModel {
	private static final Logger log = Logger.getLogger(Forth9900StandaloneMemoryModel.class);
	
	private MemoryEntry consoleEntry;

	private IMemoryEntry gromEntry;

	private IMemoryEntry gramDictEntry;

	private IMemoryEntry gramMemoryEntry;

	private IMemoryEntry cpuRomEntry;

	private Forth9900ConsoleMmioArea mmioArea;

	private IMemoryEntry mmioEntry;

	public Forth9900StandaloneMemoryModel(IMachine machine) {
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
		mmioArea = new Forth9900ConsoleMmioArea((IMachine) machine);
		mmioEntry = new MemoryEntry("MMIO", CPU, 0x0000, 0x0400, mmioArea);
	}
	
	private static MemoryEntryInfo f99bRomMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withFilename("f9900rom.bin")
		.withSize(-0x10000)
		.create("Forth9900 CPU ROM");

	private static String FORTH_GROM = "f9900grom_s.bin";
	
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
			cpuRomEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f99bRomMemoryEntryInfo);
			cpuRomEntry.load();
			cpuRomEntry.copySymbols(CPU);
			
			memory.removeAndUnmap(cpuRomEntry);
			memory.removeAndUnmap(consoleEntry);
			
			// Make the RAM area for the Forth RAM dictionary/etc., 
			// which lives on the area boundary past the Forth ROM 
			int st = cpuRomEntry.getAddr() + 0x400 * ((cpuRomEntry.getSize() + 0x3ff) / 0x400);
			int sz = 0x10000 - st;

			consoleEntry = new MemoryEntry("64K RAM", CPU, 
					st, sz, new EnhancedRamByteArea(0, sz));
			memory.addAndMap(consoleEntry);
			
			mmioArea.setUnderlyingRomEntry(cpuRomEntry);
			
			memory.addAndMap(cpuRomEntry);

			memory.addAndMap(mmioEntry);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, f99bRomMemoryEntryInfo.getFilename(), e);
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
				eventNotifier.notifyEvent(this, Level.ERROR, 
						"Failed to copy initial disk image from " + shippingDiskImage + " to " + userDiskImage); 
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
				f99bRomMemoryEntryInfo,
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
		gplMmio = new GplMmio(((IMachine) machine).getGpl()); 
		super.initMemory(machine);
	}
}
