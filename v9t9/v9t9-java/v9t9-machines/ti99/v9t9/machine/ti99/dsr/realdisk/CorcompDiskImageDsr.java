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
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.ti99.dsr.IDsrHandler9900;
import v9t9.machine.ti99.machine.TI99Machine;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This DSR handler represents the Corcomp DSDD disk controller.
 * 
 * memory:
 * 
 * write >4000/>4001  disk?  (>42E4, b2)
 * write >4002/>4003 
 * write >4004/>4005 
 * write >4006/>4007   
 * write >400A
 * write >400B
 * write >407F  (>41C6 during init)
 * 
 * write >5FF8
 * 
 * CRU:
 * 
 * 0 - dsr select
 * 1 - light?
 * 2
 * 4
 * 5
 * 6
 * 7
 * 8
 * A
 * >B : bank  (5F74/5F7C)  (same in both banks)
 * 
 * 
 * @author ejs
 *
 */
public class CorcompDiskImageDsr extends BaseDiskImageDsr implements IDsrHandler9900 {
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
	
//	public static final SettingSchema corcompDiskControllerEnabled = new IconSettingSchema(
//			ISettingsHandler.MACHINE,
//			"CorcompDiskControllerEnabled",
//			"Corcomp Disk Controller",
//			"This implements a drive (like DSK1) in a disk image on your host, "+
//			"supporting four drives, single or double sided operation, and single or double density disks.\n\n"+
//			"Either sector image or track image disks are supported.\n\n"+
//			"A track image can support copy-protected disks, while a sector image cannot.",
//			Boolean.TRUE, RealDiskSettings.diskImageIconPath
//			);
	
	public static SettingSchema settingDsrRom1FileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CorcompDiskDsrBank1FileName",
			"cc_mgbank1.bin");
	public static SettingSchema settingDsrRom2FileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CorcompDiskDsrBank2FileName",
			"cc_mgbank2.bin");

	public static MemoryEntryInfo dsrRomInfo = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withDescription("Corcomp Disk Controller ROM (double-sided, double-density)")
			.withFilenameProperty(settingDsrRom1FileName)
			.withFileMD5("956B78A3AAC982BE9829523042E7CBAB")
			.withFileMD5Limit(0x2000 - 0x80)
			.withFilename2Property(settingDsrRom2FileName)
			.withFile2MD5("B9C53C584842387B3AEC668B856EC8AD")
			.withFile2MD5Offset(0x80)
			.withFile2MD5Limit(0x2000 - 0x80 - 0x80)
			.withBankClass(CorcompDsrRomBankedMemoryEntry.class)
			.create("Corcomp Disk DSR ROM");

	/** this entry is only for discovery to avoid over-complicating the ROM setup dialog */ 
	public static MemoryEntryInfo dsrRomBank1Info = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withDescription("Corcomp Disk Controller ROM (double-sided, double-density)")
			.withFilenameProperty(settingDsrRom1FileName)
			.withFileMD5("956B78A3AAC982BE9829523042E7CBAB")
			.withFileMD5Limit(0x2000 - 0x80)
			.create("Corcomp Disk DSR ROM (bank 1)");
	
	/** this entry is only for discovery to avoid over-complicating the ROM setup dialog */ 
	public static MemoryEntryInfo dsrRomBank2Info = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withDescription("Corcomp Disk Controller ROM (double-sided, double-density)")
			.withFilenameProperty(settingDsrRom2FileName)
			.withFileMD5("B9C53C584842387B3AEC668B856EC8AD")
			.withFileMD5Offset(0x80)
			.withFileMD5Limit(0x2000 - 0x80 - 0x80)
			.create("Corcomp Disk DSR ROM (bank 2)");

	private ICruWriter cruwRealDiskMotor = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Motor %s\n"), data ? "on" : "off");
			fdc.setDiskMotor(data != 0);
			return 0;
		}
	};

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

	private ICruWriter cruwRealDiskBank = new ICruWriter() {
		public int write(int addr, int data, int num) {
			if (romMemoryEntry != null) {
				if (data == 0) {
					romMemoryEntry.selectBank(0);
				} else {
					romMemoryEntry.selectBank(1);
				}
			}
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

	private IMemoryEntry ioMemoryEntry;
	private DiskMMIOMemoryArea ioArea;	

	private short base;

	protected IProperty corcompDiskDsrActiveSetting;

	
	private BankedMemoryEntry romMemoryEntry;
	
	protected IMemoryDomain console;

	private boolean inited;

	public CorcompDiskImageDsr(TI99Machine machine, short base) {
		super(machine);
		
		this.base = base;
		this.console = machine.getConsole();

		fdc = new FDC1771(machine, dumper, 4);

//		IProperty enabled = settings.get(corcompDiskControllerEnabled);
//		enabled.addEnablementDependency(settings.get(RealDiskSettings.diskImagesEnabled));
		
		if (!imageMapper.getDiskSettingsMap().isEmpty()) {
			
			// one setting for entire DSR
			corcompDiskDsrActiveSetting = new SettingSchemaProperty(getName(), Boolean.FALSE);
			//settingImagesEnabled.addEnablementDependency(corcompDiskDsrActiveSetting);
			corcompDiskDsrActiveSetting.addEnablementDependency(settingImagesEnabled);
			
			settingImagesEnabled.addListener(new IPropertyListener() {
				
				@Override
				public void propertyChanged(IProperty property) {
					settings.get(IDeviceIndicatorProvider.settingDevicesChanged).firePropertyChange();
				}
			});

		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#init()
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
		cruManager.add(base + 0xB*2, 1, cruwRealDiskBank);
		
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
		cruManager.removeWriter(base + 0xB*2, 1);
		
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
			return (byte) ret;
		}
		
//		@Override
//		@Deprecated
//		public void writeByte(IMemoryEntry entry, int addr, byte val) {
//			writeByte(addr, val);
//		}
		@Override
		public void writeByte(int addr, byte val) {
			if (addr < 0x5ff0) {
				romMemoryEntry.getArea().flatWriteByte(addr, val);
				return;
			}
			
			if (!settingImagesEnabled.getBoolean())
				return;

			//val = (byte) ~val;

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
		
//		/* (non-Javadoc)
//		 * @see v9t9.engine.memory.WordMemoryArea#readWord(v9t9.engine.memory.MemoryEntry, int)
//		 */
//		@Override
//		@Deprecated
//		public short readWord(IMemoryEntry entry, int addr) {
//			return (short) ((readByte(entry, (addr & ~1)) << 8) 
//			| (readByte(entry, (addr | 1)) & 0xff));
//		}
//		
//		/* (non-Javadoc)
//		 * @see v9t9.engine.memory.WordMemoryArea#writeWord(v9t9.engine.memory.MemoryEntry, int, short)
//		 */
//		@Override
//		@Deprecated
//		public void writeWord(IMemoryEntry entry, int addr, short val) {
//			writeByte(entry, (addr & ~1), (byte) (val >> 8));
//			writeByte(entry, (addr | 1), (byte) (val & 0xff));
//		}
		

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
		if (!settingImagesEnabled.getBoolean() || corcompDiskDsrActiveSetting.getBoolean())
			return;

		init();
		
		corcompDiskDsrActiveSetting.setBoolean(true);
		
		if (romMemoryEntry == null) {
			this.romMemoryEntry = (BankedMemoryEntry) memoryEntryFactory.newMemoryEntry(dsrRomInfo);
		}
		
		if (ioMemoryEntry == null) {
			ioArea = new DiskMMIOMemoryArea();
			ioMemoryEntry = new MemoryEntry("Corcomp Disk DSR ROM MMIO", console, 0x5C00, 0x400, ioArea);
		}

		
		console.mapEntry(romMemoryEntry);
		console.mapEntry(ioMemoryEntry);
		
		romMemoryEntry.selectBank(0);
		
//		settings.get(IMachine.settingPauseMachine).setBoolean(true);
//		settings.get(ICpu.settingDebugging).setBoolean(true);
	}
	
	public void deactivate(IMemoryDomain console) {
		console.unmapEntry(ioMemoryEntry);
		console.unmapEntry(romMemoryEntry);
		
		corcompDiskDsrActiveSetting.setBoolean(false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		
		if (!settingImagesEnabled.getBoolean())
			return Collections.emptyList();
		
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				corcompDiskDsrActiveSetting, 
				"Disk image activity",
				IDevIcons.DSR_DISK_IMAGE, IDevIcons.DSR_LIGHT);
		return Collections.<IDeviceIndicatorProvider>singletonList(deviceIndicatorProvider);
	}
	
}
