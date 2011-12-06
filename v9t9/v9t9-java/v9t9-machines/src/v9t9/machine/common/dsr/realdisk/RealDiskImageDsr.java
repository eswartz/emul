/**
 * 
 */
package v9t9.machine.common.dsr.realdisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import v9t9.base.settings.SettingProperty;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.dsr.realdisk.BaseDiskImageDsr;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.hardware.ICruReader;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryEntry;
import v9t9.machine.ti99.dsr.DsrHandler9900;
import v9t9.machine.ti99.machine.TI99Machine;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;

/**
 * This DSR handler assumes the TI-99/4A model where some control is
 * performed through CRU bits and address ports are in MMIO in the DSR area.
 * @author ejs
 *
 */
public class RealDiskImageDsr extends BaseDiskImageDsr implements DsrHandler9900 {
	private DiskMemoryEntry romMemoryEntry;
	

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
			setDiskMotor(data != 0);
			return 0;
		}
	};

	private MemoryEntry ioMemoryEntry;
	private DiskMMIOMemoryArea ioArea;	
	
	private ICruWriter cruwRealDiskHold = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU hold %s\n"), data ? "on" : "off");
		
			setDiskHold(data != 0);
			
			return 0;
		}
	};

	private ICruWriter cruwRealDiskHeads = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Heads %s\n"), data ? "on" : "off");
	
			setDiskHeads(data != 0);
			return 0;
		}
	};
	
	private ICruWriter cruwRealDiskSel = new ICruWriter() {
		public int write(int addr, int data, int num) {
			byte newnum = (byte) (((addr - 0x1108) >> 1) + 1);
	
			selectDisk(newnum, data != 0);
			
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
			byte newnum = (byte) (((addr - 0x1102) >> 1) + 1);
			return isPolledDisk(newnum) ? 1 : 0;
		}
	};

	private ICruReader crurRealDiskMotor = new ICruReader() {
		public int read(int addr, int data, int num) {
			return isMotorRunning() ? 1 : 0;
			
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


	private List<IDeviceIndicatorProvider> deviceIndicatorProviders;
	
	
	public RealDiskImageDsr(TI99Machine machine, short base) {
		super(machine);
		this.base = base;

		deviceIndicatorProviders = new ArrayList<IDeviceIndicatorProvider>();
		
		if (!diskSettingsMap.isEmpty()) {
			
			// one setting for entire DSR
			realDiskDsrActiveSetting = new SettingProperty(getName(), Boolean.FALSE);
			realDiskDsrActiveSetting.addEnablementDependency(settingDsrEnabled);
			DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
					realDiskDsrActiveSetting, 
					"Disk image activity",
					IDevIcons.DSR_DISK_IMAGE, IDevIcons.DSR_LIGHT);
			deviceIndicatorProviders.add(deviceIndicatorProvider);
			
			/*
			for (Map.Entry<String, SettingProperty> entry : diskSettingsMap.entrySet()) {
				BaseDiskImage image = getDiskImage(entry.getValue().getName());
				DiskImageDeviceIndicatorProvider provider = new DiskImageDeviceIndicatorProvider(image);
				list.add(provider);
			}
			*/
		}
		
		CruManager cruManager = machine.getCruManager();
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

	private class DiskMMIOMemoryArea extends ConsoleMmioArea {
		public DiskMMIOMemoryArea() {
			super(4);
		}

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readByte(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public byte readByte(IMemoryEntry entry, int addr) {
			
			if (addr < 0x5ff0)
				return romMemoryEntry.getArea().flatReadByte(romMemoryEntry, addr);

			byte ret = 0;

			if (!settingDsrEnabled.getBoolean())
				return ret;


			switch ((addr - 0x5ff0) >> 1) {
			case R_RDSTAT:
				ret = readStatus();
				break;

			case R_RTADDR:
				ret = readTrackAddr();
				break;

			case R_RSADDR:
				ret = readSectorAddr();
				break;

			case R_RDDATA:
				ret = readData();
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
		public void writeByte(IMemoryEntry entry, int addr, byte val) {
			if (addr < 0x5ff0) {
				romMemoryEntry.getArea().flatWriteByte(romMemoryEntry, addr, val);
				return;
			}
			
			if (!settingDsrEnabled.getBoolean())
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
				writeCommand(val);
				break;

			case W_WTADDR:
				writeTrackAddr(val);
				break;

			case W_WSADDR:
				writeSectorAddr(val);
				break;

			case W_WTDATA:
				writeData(val);
			}
		
			
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readWord(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public short readWord(IMemoryEntry entry, int addr) {
			return (short) ((readByte(entry, (addr & ~1)) << 8) 
			| (readByte(entry, (addr | 1)) & 0xff));
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeWord(v9t9.engine.memory.MemoryEntry, int, short)
		 */
		@Override
		public void writeWord(IMemoryEntry entry, int addr, short val) {
			writeByte(entry, (addr & ~1), (byte) (val >> 8));
			writeByte(entry, (addr | 1), (byte) (val & 0xff));
		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return base;
	}


	public void activate(IMemoryDomain console) throws IOException {
		if (!settingDsrEnabled.getBoolean())
			return;
		
		realDiskDsrActiveSetting.setBoolean(true);
		
		if (romMemoryEntry == null)
			this.romMemoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "TI Disk DSR ROM", console,
					"disk.bin", 0, false);
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
		
		realDiskDsrActiveSetting.setBoolean(false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		return deviceIndicatorProviders;
	}
}
