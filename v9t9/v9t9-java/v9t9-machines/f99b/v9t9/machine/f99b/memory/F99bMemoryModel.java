/*
  F99bMemoryModel.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.memory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

import ejs.base.properties.IProperty;
import ejs.base.utils.FileUtils;
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


/**
 * F99b console memory model.
 * @author ejs
 */
public class F99bMemoryModel extends BaseTI994AMemoryModel {
	private static final Logger log = Logger.getLogger(F99bMemoryModel.class);
	
	private MemoryEntry consoleEntry;

	private IMemoryEntry gromEntry;

	private IMemoryEntry gramDictEntry;

	private IMemoryEntry gramMemoryEntry;

	private IMemoryEntry cpuRomEntry;

	public F99bMemoryModel(IMachine machine) {
		super(machine);
	}

	@Override
	protected void initSettings(ISettingsHandler settings) {
		IProperty shipPath = settings.get(DataFiles.settingShippingRomsPath);

		URL dataURL;
		dataURL = EmulatorMachinesData.getDataURL("../../../build/forth99");
		DataFiles.addSearchPath(settings, dataURL.toString());
		shipPath.getList().add(dataURL.toString());
		
		dataURL = EmulatorMachinesData.getDataURL("f99b/");
		shipPath.getList().add(dataURL.toString());

	}


	protected void defineConsoleMemory(IBaseMachine machine) {
		consoleEntry = new MemoryEntry("64K RAM", CPU, 
				0x0400, 0xFC00, new EnhancedRamByteArea(0, 0xFC00));
		memory.addAndMap(consoleEntry);
	}
	
	protected void defineMmioMemory(IBaseMachine machine) {
		this.memory.addAndMap(new MemoryEntry("MMIO", CPU, 0x0000, 0x0400,
                new F99bConsoleMmioArea((IMachine) machine)));
	}
	
	private static MemoryEntryInfo f99bRomMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withFilename("f99brom.bin")
		.withOffset(0x400)
		.withAddress(0x400)
		.withSize(-(0x10000 - 0x400))
		.create("Forth99 CPU ROM");

	private static String FORTH_GROM = "f99bgrom.bin";
	
	private static MemoryEntryInfo f99bGromMemoryEntryInfo = MemoryEntryInfoBuilder
		.standardConsoleGrom(FORTH_GROM)
		.withSize(-0x4000)
		.create("Forth99 CPU GROM");


	private static MemoryEntryInfo f99bGramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withDomain(IMemoryDomain.NAME_GRAPHICS)
		.withAddress(0x4000)
		.withSize(0x4000)
		.storable(true)
		.create("Forth99 16K GRAM Dictionary");
	
	private static MemoryEntryInfo f99bDiskGramMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withFilename("f99bgram.bin")
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
			
			// shrink RAM accordingly
			int st = cpuRomEntry.getAddr() + 0x400 * ((cpuRomEntry.getSize() + 0x3ff) / 0x400);
			int sz = 0x10000 - st;
			
			memory.removeAndUnmap(cpuRomEntry);
			memory.removeAndUnmap(consoleEntry);
			
			consoleEntry = new MemoryEntry("64K RAM", CPU, 
					st, sz, new EnhancedRamByteArea(0, sz));
			memory.addAndMap(consoleEntry);
			
			memory.addAndMap(cpuRomEntry);
    	} catch (IOException e) {
    		reportLoadError(eventNotifier, f99bRomMemoryEntryInfo.getFilename(), e);
    	}
    	
    	// GROM consists of ROM up to 16k
		try {
			memory.removeAndUnmap(gromEntry);
			
			gromEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f99bGromMemoryEntryInfo);
			
			memory.addAndMap(gromEntry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, FORTH_GROM, e);
		}
		
		// then 16k of GRAM for new dictionary
		try {
			memory.removeAndUnmap(gramDictEntry);
			gramDictEntry = memory.getMemoryEntryFactory().newMemoryEntry(
					f99bGramMemoryEntryInfo);
			gramDictEntry.getArea().setLatency(0);
			memory.addAndMap(gramDictEntry);
		} catch (IOException e1) {
			// should not happen
			reportLoadError(eventNotifier, f99bGramMemoryEntryInfo.getFilename(), e1);
		}
		
		// then 32k of GRAM storage
		IProperty shipPath = settings.get(DataFiles.settingShippingRomsPath);
		URI shippingDiskImage = URI.create(shipPath.getList().get(0) + f99bDiskGramMemoryEntryInfo.getFilename());
		URI userDiskImage = machine.getRomPathFileLocator().getWriteURI(f99bDiskGramMemoryEntryInfo.getFilename());
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
					f99bDiskGramMemoryEntryInfo);
			memory.addAndMap(gramMemoryEntry);
		} catch (IOException e) {
			reportLoadError(eventNotifier, f99bDiskGramMemoryEntryInfo.getFilename(), e);
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
				f99bGromMemoryEntryInfo,
				f99bDiskGramMemoryEntryInfo,
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
