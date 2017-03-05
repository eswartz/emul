/*
  PCodeDsr.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.pcode;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.IconSettingSchema;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.common.settings.Settings;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.dsr.IDsrHandler9900;
import v9t9.machine.ti99.machine.TI99Machine;
import v9t9.machine.ti99.memory.mmio.ConsoleGramWriteArea;
import v9t9.machine.ti99.memory.mmio.ConsoleGromReadArea;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;

/**
 * Allow loading the P-Code ROMs and GROMs.
 * 
 * This case is confusing because the P-Code DSR ROMs are shipped 
 * two different ways:
 * 
 * 1) pcode_c.bin, pcode_d.bin, pcode_g.bin
 * 	c is 8k, has AA header, second 4k is 06A0 04178
 *  d is 8k, also has header, but last 4k has rewind tape
 *  
 *  so, pcode_c = COMMON + B0
 *      pcode_d = COMMON + B1
 *      
 * 2) pcode_r0.bin, pcode_r1.bin, pcode_g0.bin/pCodeGroms.bin
 * 	r0 is 4k, has AA header
 * 	r1 is pure code, first 4k is 06A0 4178, second bank has rewind tape (C180 0246)
 * 
 *  so, pcode_r0.bin has COMMON
 *      pcode_r1.bin has B0 + B1
 *      
 * ==> this variant seems most common.  Dunno where c/d came from.
 * 
 * COMMON is always at 0x4000.  The two BANKS swap in at 0x5000.
 * A CRU bit toggles the bank.  
 *      
 * @author ejs
 *
 */
public class PCodeDsr implements IDsrHandler9900 {
	private static URL pcodeIconPath = EmulatorMachinesData.getDataURL("icons/pcode_system.png");

	public static final String PCODE = "PCODE";

	static public final IconSettingSchema settingPcodeCardEnabled = new IconSettingSchema(
			ISettingsHandler.MACHINE,
			"PCodeCardEnabled", "P-Code Support", 
			"Enables the UCSD Pascal P-Code card.",
			Boolean.FALSE,
			pcodeIconPath);
	
	public static MemoryEntryInfo pcodeDsrRomMemoryEntryInfo = MemoryEntryInfoBuilder
		.wordMemoryEntry()
		.withFilename("pxcode_r0.bin")
		.withAddress(0x4000)
		.withSize(0x1000)
		.withFileMD5("4CC461030701A1F2D2E209644F8DEB9C")
		.create("P-Code DSR ROM (common)");

	/** This is where bank #1 is the first 4k of 8k banks file */
	public static MemoryEntryInfo pcodeDsrRomBank1aMemoryEntryInfo = MemoryEntryInfoBuilder
		.wordMemoryEntry()
		.withFilename("pxcode_r1.bin")
		.withAddress(0x5000)
		.withSize(0x1000)
		
		// there are two MMIO areas here and at the end; don't be too picky
		.withFileMD5("ABE55D238E3925D20B17CC859AD43D36")
		.withFileMD5Algorithm(MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":0000+0BFC") 
		
		.create("P-Code DSR ROM (bank 1)");

	/** This is where bank #1 is the second 4k of common + bank file */
	public static MemoryEntryInfo pcodeDsrRomBank1bMemoryEntryInfo = MemoryEntryInfoBuilder
		.wordMemoryEntry()
		.withFilename("pxcode_d.bin")
		.withAddress(0x5000)
		.withSize(0x1000)
		.withOffset(0x1000)
		
		// there are two MMIO areas here and at the end; don't be too picky
		.withFileMD5("ABE55D238E3925D20B17CC859AD43D36")
		.withFileMD5Algorithm(MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":0000+0BFC") 
		
		.create("P-Code DSR ROM (bank 1, alternate)");

	/** Bank #2 is always the second 4k of some file */
	public static MemoryEntryInfo pcodeDsrRomBank2MemoryEntryInfo = MemoryEntryInfoBuilder
		.wordMemoryEntry()
		
		.withFilename("pxcode_r1.bin")
		.withAddress(0x5000)
		.withSize(0x1000)
		.withOffset(0x1000)
		
		// there are two MMIO areas here and at the end; don't be too picky
		.withFileMD5("7ED0D59B05752007CB0777531F19300C")
		.withFileMD5Algorithm(MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":1000+0BFC")
		
		.create("P-Code DSR ROM (bank 2)");

//	/** this entry is only for discovery to avoid over-complicating the ROM setup dialog */ 
//	public static MemoryEntryInfo pcodeDsrRomBankAMemoryEntryInfo = MemoryEntryInfoBuilder
//		.wordMemoryEntry()
//		.withFilename("pxcode_r0.bin")
//		.withAddress(0x4000)
//		.withSize(0x1000)
//		.withFileMD5("4CC461030701A1F2D2E209644F8DEB9C")
//		.create("P-Code DSR ROM");
//
//	/** this entry is only for discovery to avoid over-complicating the ROM setup dialog */ 
//	public static MemoryEntryInfo pcodeDsrRomBankBMemoryEntryInfo = MemoryEntryInfoBuilder
//		.wordMemoryEntry()
//		.withFilename("pxcode_r1.bin")
//		.withAddress(0x4000)
//		.withSize(0x2000)
//		.withFileMD5("F53F51C88608ED7A000BE394F6722BF0")
//		.withFileMD5Algorithm(MD5FilterAlgorithms.ALGORITHM_SEGMENT + ":0000+0BFC:1000+0BFC")
//		.create("P-Code DSR ROM (banks)");

	public static MemoryEntryInfo pcodeGromMemoryEntryInfo = MemoryEntryInfoBuilder
		.byteMemoryEntry()
		.withDomain(PCODE)
		.withAddress(0x0)
		.withSize(0x10000)
		.withFilename("pxcode_g.bin")
		.withFileMD5("58A043220F96D25706A9ABDC220CDCEE")
		.withFileMD5Algorithm(MD5FilterAlgorithms.ALGORITHM_GROM)
		.create("P-Code GROM");

	private IMemoryEntry dsrCommonMemoryEntry;
	private PCodeDsrRomBankedMemoryEntry dsrBankedMemoryEntry;
	private TI99Machine machine;
	private IMemoryDomain pcodeDomain;
	private GplMmio pcodeGromMmio;
	private IMemoryEntry readMmioEntry;
	private IMemoryEntry writeMmioEntry;
	private IMemoryEntry gromMemoryEntry;
	private IProperty pcodeActive;

	private IProperty pcodeCardEnabled;

	/**
	 * @param machine
	 */
	public PCodeDsr(TI99Machine machine_) {
		this.machine = machine_;
		pcodeActive = new SettingSchemaProperty("pcodeActive", Boolean.FALSE);
		pcodeCardEnabled = Settings.get(machine, settingPcodeCardEnabled);
		pcodeActive.addEnablementDependency(pcodeCardEnabled);
		
		pcodeCardEnabled.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				Settings.get(machine, IDeviceIndicatorProvider.settingDevicesChanged).firePropertyChange();
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return 0x1f00;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	@Override
	public void dispose() {

	}


	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#activate(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void activate(IMemoryDomain console, IMemoryEntryFactory memoryEntryFactory) throws IOException {
		// DSR ROM
		if (!pcodeCardEnabled.getBoolean())
			return;
		
		pcodeActive.setBoolean(true);
		
		IMemory memory = console.getMemory();

		ensureSetup(memoryEntryFactory);
		
		// pCode GROMs are accessed specially
		memory.addAndMap(dsrCommonMemoryEntry);
		memory.addAndMap(dsrBankedMemoryEntry);
		memory.addAndMap(readMmioEntry);
		memory.addAndMap(writeMmioEntry);
		
		memory.addAndMap(gromMemoryEntry);
	}

	private void ensureSetup(IMemoryEntryFactory memoryEntryFactory) throws IOException {
		IMemory memory = machine.getMemory();
		IMemoryDomain console = machine.getConsole();

		if (console.getEntryAt(0x5000) instanceof PCodeDsrRomBankedMemoryEntry) {
			dsrBankedMemoryEntry = (PCodeDsrRomBankedMemoryEntry) console.getEntryAt(0x5000);
		}
		
		if (dsrCommonMemoryEntry == null) {
			this.dsrCommonMemoryEntry = memoryEntryFactory.newMemoryEntry(pcodeDsrRomMemoryEntryInfo);
		}
		if (dsrBankedMemoryEntry == null) {
			IMemoryEntry bank1 = null;
			try {
				bank1 = memoryEntryFactory.newMemoryEntry(pcodeDsrRomBank1aMemoryEntryInfo);
			} catch (IOException e) {
				bank1 = memoryEntryFactory.newMemoryEntry(pcodeDsrRomBank1bMemoryEntryInfo);
			}
			IMemoryEntry bank2 = memoryEntryFactory.newMemoryEntry(pcodeDsrRomBank2MemoryEntryInfo);
			this.dsrBankedMemoryEntry = new PCodeDsrRomBankedMemoryEntry(
					machine.getSettings(), memory, "P-Code DSR ROM (Banks)",
					new IMemoryEntry[] { bank1, bank2 });
		}
		
		pcodeDomain = memory.getDomain(PCODE);
		if (pcodeDomain == null) {
			// P-Code GROMs are completely private to the card
			pcodeDomain = new MemoryDomain(PCODE);
			
			memory.addDomain(PCODE, pcodeDomain);
		}
		if (gromMemoryEntry == null) {
			gromMemoryEntry = memoryEntryFactory.newMemoryEntry(
					pcodeGromMemoryEntryInfo);
		}
		
		if (pcodeGromMmio == null) {
			pcodeGromMmio = new GplMmio(machine, pcodeDomain);
			readMmioEntry = null;
			writeMmioEntry = null;
		}
		
		dsrBankedMemoryEntry.setup(machine, pcodeGromMmio);
		
		if (readMmioEntry == null) {
			readMmioEntry = new MemoryEntry("PCode Read MMIO", pcodeDomain, 0x5800, 0x0400,
					new ConsoleGromReadArea(pcodeGromMmio));
	        writeMmioEntry = new MemoryEntry("PCode Write MMIO", pcodeDomain, 0x5C00, 0x0400,
	                new ConsoleGramWriteArea(pcodeGromMmio));
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#deactivate(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void deactivate(IMemoryDomain console) {
		IMemory memory = console.getMemory();
		
		if (dsrBankedMemoryEntry != null) {
			memory.removeAndUnmap(gromMemoryEntry);
			memory.removeAndUnmap(readMmioEntry);
			memory.removeAndUnmap(writeMmioEntry);
			memory.removeAndUnmap(dsrBankedMemoryEntry);
			memory.removeAndUnmap(dsrCommonMemoryEntry);
		}
		
		pcodeActive.setBoolean(false);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	@Override
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		return Collections.<String, Collection<IProperty>>singletonMap(GROUP_DSR_SELECTION,
				Collections.<IProperty>singletonList(pcodeCardEnabled));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getName()
	 */
	@Override
	public String getName() {
		return "UCSD P-System";
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#handleDSR(v9t9.emulator.hardware.dsrs.MemoryTransfer, short)
	 */
	@Override
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IPersistable#loadState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		
		ISettingSection sub = section.getSection("P-Code");
		if (sub == null)
			return;
		
		pcodeCardEnabled.loadState(sub);
		
		try {
			ensureSetup(machine.getMemory().getMemoryEntryFactory());
		} catch (IOException e) {
			machine.notifyEvent(Level.ERROR, e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IPersistable#saveState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		ISettingSection sub = section.addSection("P-Code");
		pcodeCardEnabled.saveState(sub);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		if (!Settings.get(machine, settingPcodeCardEnabled).getBoolean())
			return Collections.emptyList();
		
		IDeviceIndicatorProvider provider= new DeviceIndicatorProvider(
				pcodeActive, "USCD P-System Activity", 
				IDevIcons.DSR_USCD, IDevIcons.DSR_LIGHT,
				null);
		return Collections.singletonList(provider);
	}
}
