/*
  RealDiskImageDsr.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.realdisk;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.dsr.realdisk.BaseDiskImageDsr;
import v9t9.engine.files.image.FDC1771;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.hardware.ICruReader;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.ti99.dsr.IDsrHandler9900;
import v9t9.machine.ti99.machine.TI99Machine;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This DSR handler assumes the TI-99/4A model where some control is
 * performed through CRU bits and address ports are in MMIO in the DSR area.
 * @author ejs
 *
 */
public class TIDiskImageDsr extends BaseDiskImageDsr implements IDsrHandler9900 {
	private IMemoryEntry romMemoryEntry;

//	public static final SettingSchema tiDiskControllerEnabled = new IconSettingSchema(
//			ISettingsHandler.MACHINE,
//			"TIDiskControllerEnabled",
//			"TI Disk Controller",
//			"This implements a drive (like DSK1) in a disk image on your host, "+
//			"supporting three drives, single or double sided operation, and single density disks.\n\n"+
//			"Either sector image or track image disks are supported.\n\n"+
//			"A track image can support copy-protected disks, while a sector image cannot.",
//			Boolean.TRUE, RealDiskSettings.diskImageIconPath
//			);
	
	public static SettingSchema settingDsrRomFileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"TIDiskDsrFileName",
			"tidiskdsr.bin");

	public static MemoryEntryInfo dsrRomInfo = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withFilenameProperty(settingDsrRomFileName)
			.withDescription("TI Disk Controller ROM (double-sided, single-density)")
			.withFileMD5("C9A737D6930F5FD1D96829FD89359CF1")
			.withFileMD5Limit(0x1FF0)
			.create("TI Disk DSR ROM");


	static final int 
		R_RDSTAT = 0,
		R_RTADDR = 1,
		R_RSADDR = 2,
		R_RDDATA = 3,
		W_WTCMD = 4,
		W_WTADDR = 5,
		W_WSADDR = 6,
		W_WTDATA = 7
	;
	
	private ICruWriter cruwRealDiskMotor = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Motor %s\n"), data ? "on" : "off");
			fdc.setDiskMotor(data != 0);
			return 0;
		}
	};

	private IMemoryEntry ioMemoryEntry;
	private DiskMMIOMemoryArea ioArea;	
	
	private ICruWriter cruwRealDiskHold = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU hold %s\n"), data ? "on" : "off");
		
			fdc.setHold(data != 0);
			
			return 0;
		}
	};

	private ICruWriter cruwRealDiskHeads = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Heads %s\n"), data ? "on" : "off");
	
			fdc.setHeads(data != 0);
			return 0;
		}
	};
	
	private ICruWriter cruwRealDiskSel = new ICruWriter() {
		public int write(int addr, int data, int num) {
			byte newnum = (byte) (((addr - base - 8) >> 1) + 1);
	
			fdc.selectDisk(newnum, data != 0);
			
			return 0;
		}
	};
	
	private ICruWriter cruwRealDiskSide = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU disk side #%d\n"), data);
			setDiskSide(data);
			return 0;
		}
	};
	
	private ICruReader crurRealDiskPoll = new ICruReader() {
		public int read(int addr, int data, int num) {
			byte newnum = (byte) (((addr - base - 2) >> 1) + 1);
			return fdc.getSelectedDisk() == (int) newnum ? 1 : 0;
		}
	};

	private ICruReader crurRealDiskMotor = new ICruReader() {
		public int read(int addr, int data, int num) {
			return fdc.isMotorRunning() ? 1 : 0;
			
		}
	};
	
	private ICruReader crurRealDiskZero = new ICruReader() {
		public int read(int addr, int data, int num) {
			return 0;
		}
	};
	
	private ICruReader crurRealDiskOne = new ICruReader() {
		public int read(int addr, int data, int num) {
			return 1;
		}
	};

	
	private ICruReader crurRealDiskSide = new ICruReader() {
		public int read(int addr, int data, int num) {
			return getSide();
		}
	};

	private short base;

	protected IProperty tiDiskDsrActiveSetting;

	private boolean inited;

	public TIDiskImageDsr(TI99Machine machine, short base) {
		super(machine);
		this.base = base;

		fdc = new FDC1771(machine, dumper, 3);
		
//		IProperty enabled = settings.get(tiDiskControllerEnabled);
//		enabled.addEnablementDependency(settings.get(RealDiskSettings.diskImagesEnabled));
		
		if (!imageMapper.getDiskSettingsMap().isEmpty()) {
			
			// one setting for entire DSR
			tiDiskDsrActiveSetting = new SettingSchemaProperty(getName(), Boolean.FALSE);
			tiDiskDsrActiveSetting.addEnablementDependency(settingImagesEnabled);
			
			settingImagesEnabled.addListener(new IPropertyListener() {
				@Override
				public void propertyChanged(IProperty property) {
					settings.get(IDeviceIndicatorProvider.settingDevicesChanged).firePropertyChange();
				}
			});

		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.realdisk.BaseDiskImageDsr#init()
	 */
	@Override
	public void init() {
		if (inited)
			return;
		
		inited = true;
		super.init();
		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		cruManager.add(base + 0x2, 1, cruwRealDiskMotor);
		cruManager.add(base + 0x4, 1, cruwRealDiskHold);
		cruManager.add(base + 0x6, 1, cruwRealDiskHeads);
		cruManager.add(base + 0x8, 1, cruwRealDiskSel);
		cruManager.add(base + 0xA, 1, cruwRealDiskSel);
		cruManager.add(base + 0xC, 1, cruwRealDiskSel);
		cruManager.add(base + 0xE, 1, cruwRealDiskSide);
		cruManager.add(base + 0x2, 1, crurRealDiskPoll);
		cruManager.add(base + 0x4, 1, crurRealDiskPoll);
		cruManager.add(base + 0x6, 1, crurRealDiskPoll);
		cruManager.add(base + 0x8, 1, crurRealDiskMotor);
		cruManager.add(base + 0xA, 1, crurRealDiskZero);
		cruManager.add(base + 0xC, 1, crurRealDiskOne);
		cruManager.add(base + 0xE, 1, crurRealDiskSide);

	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.realdisk.BaseDiskImageDsr#dispose()
	 */
	@Override
	public void dispose() {
		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		cruManager.removeWriter(base + 0x2, 1);
		cruManager.removeWriter(base + 0x4, 1);
		cruManager.removeWriter(base + 0x6, 1);
		cruManager.removeWriter(base + 0x8, 1);
		cruManager.removeWriter(base + 0xA, 1);
		cruManager.removeWriter(base + 0xC, 1);
		cruManager.removeWriter(base + 0xE, 1);
		cruManager.removeReader(base + 0x2, 1);
		cruManager.removeReader(base + 0x4, 1);
		cruManager.removeReader(base + 0x6, 1);
		cruManager.removeReader(base + 0x8, 1);
		cruManager.removeReader(base + 0xA, 1);
		cruManager.removeReader(base + 0xC, 1);
		cruManager.removeReader(base + 0xE, 1);
		super.dispose();
	}

	private class DiskMMIOMemoryArea extends ConsoleMmioArea {
		public DiskMMIOMemoryArea() {
			super(4);
		}

//		@Override
//		@Deprecated
//		public byte readByte(IMemoryEntry entry, int addr) {
//			return readByte(addr);
//		}
//		@Override
//		@Deprecated
//		public void writeByte(IMemoryEntry entry, int addr, byte val) {
//			writeByte(addr, val);
//		}
		
		@Override
		public byte readByte(int addr) {
			
			if (addr < 0x5ff0)
				return romMemoryEntry.getArea().flatReadByte(addr);

			byte ret = 0;

			if (!settingImagesEnabled.getBoolean())
				return ret;


			switch ((addr - 0x5ff0) >> 1) {
			case R_RDSTAT:
				byte ret1 = fdc.readStatus();
				ret = ret1;
				break;

			case R_RTADDR:
				byte ret2 = fdc.getTrackReg();
				ret = ret2;
				break;

			case R_RSADDR:
				byte ret3 = fdc.getSectorReg();
				ret = ret3;
				break;

			case R_RDDATA:
				ret = fdc.readByte();
				break;

			case W_WTCMD:
			case W_WTADDR:
			case W_WSADDR:
			case W_WTDATA:
				ret = 0x00;
				//module_logger(&realDiskDSR, _L|L_1, _("FDC read write xxx >%04X = >%02X\n"), addr, (u8) ret);
				break;
			}
			return (byte) ~ret;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeByte(v9t9.engine.memory.MemoryEntry, int, byte)
		 */
		@Override
		public void writeByte(int addr, byte val) {
			if (addr < 0x5ff0) {
				romMemoryEntry.getArea().flatWriteByte(addr, val);
				return;
			}
			
			if (!settingImagesEnabled.getBoolean())
				return;

			val = (byte) ~val;

			switch ((addr - 0x5ff0) >> 1) {
			case R_RDSTAT:
			case R_RTADDR:
			case R_RSADDR:
			case R_RDDATA:
				//module_logger(&realDiskDSR, _L|L_1, _("FDC write read xxx >%04X, >%02X\n"), addr, val);
				break;

			case W_WTCMD:
				fdc.writeCommand(val);
				break;

			case W_WTADDR:
				fdc.setTrackReg(val);
				//DSK.status &= ~fdc_LOSTDATA;
				break;

			case W_WSADDR:
				fdc.setSectorReg(val);
				//DSK.status &= ~fdc_LOSTDATA;
				break;

			case W_WTDATA:
				fdc.writeData(val);
			}
		
			
		}
		
//		@Override
//		@Deprecated
//		public short readWord(IMemoryEntry entry, int addr) {
//			return (short) ((readByte(entry, (addr & ~1)) << 8) 
//			| (readByte(entry, (addr | 1)) & 0xff));
//		}
//		
//		@Override
//		@Deprecated
//		public void writeWord(IMemoryEntry entry, int addr, short val) {
//			writeByte(entry, (addr & ~1), (byte) (val >> 8));
//			writeByte(entry, (addr | 1), (byte) (val & 0xff));
//		}
		

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readWord(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public short readWord(int addr) {
			return (short) ((readByte((addr & ~1)) << 8) 
			| (readByte((addr | 1)) & 0xff));
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeWord(v9t9.engine.memory.MemoryEntry, int, short)
		 */
		@Override
		public void writeWord(int addr, short val) {
			writeByte((addr & ~1), (byte) (val >> 8));
			writeByte((addr | 1), (byte) (val & 0xff));
		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return base;
	}


	public void activate(IMemoryDomain console, IMemoryEntryFactory memoryEntryFactory) throws IOException {
		if (!settingImagesEnabled.getBoolean() || tiDiskDsrActiveSetting.getBoolean())
			return;
		
		init();
		
		tiDiskDsrActiveSetting.setBoolean(true);

		this.romMemoryEntry = memoryEntryFactory.newMemoryEntry(dsrRomInfo);
		
		if (ioMemoryEntry == null) {
			ioArea = new DiskMMIOMemoryArea();
			ioMemoryEntry = new MemoryEntry("TI Disk DSR ROM MMIO", console, 0x5C00, 0x400, ioArea);
		}

		console.mapEntry(romMemoryEntry);
		console.mapEntry(ioMemoryEntry);
		
	}
	
	public void deactivate(IMemoryDomain console) {
		console.unmapEntry(ioMemoryEntry);
		console.unmapEntry(romMemoryEntry);
		
		tiDiskDsrActiveSetting.setBoolean(false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		
		if (!settingImagesEnabled.getBoolean())
			return Collections.emptyList();
		
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				tiDiskDsrActiveSetting, 
				"Disk image activity",
				IDevIcons.DSR_DISK_IMAGE, IDevIcons.DSR_LIGHT);
		return Collections.<IDeviceIndicatorProvider>singletonList(deviceIndicatorProvider);
	}
}
