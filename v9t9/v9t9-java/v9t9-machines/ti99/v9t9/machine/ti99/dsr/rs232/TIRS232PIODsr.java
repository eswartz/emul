/*
  TIRS232PIODsr.java

  (c) 2014-2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.rs232;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.dsr.rs232.PIO;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.hardware.ICruReader;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.dsr.rs232.RS232Settings;
import v9t9.machine.ti99.machine.TI99Machine;
import v9t9.machine.ti99.memory.mmio.ConsoleMmioArea;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
/**
 * @author ejs
 *
 */
public class TIRS232PIODsr extends TIRS232Dsr {

	public static SettingSchema settingPIODsrRomFileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"RS232DsrFileName",
			"rs232pio.bin");

	public static MemoryEntryInfo pioDsrRomInfo = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withFilenameProperty(settingPIODsrRomFileName)
			.withDescription("RS232/PIO Controller ROM")
//			.withFileMD5("4E4E08FF10D23B799AAA990344553E2E")
			.withFileMD5("A5467BE2E6BD04E1B9CF1DE8FA507541")
			.create("TI RS232/PIO DSR ROM");
	
	private class PIOMMIOMemoryArea extends ConsoleMmioArea {
		public PIOMMIOMemoryArea() {
			super(4);
		}

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readByte(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public byte readByte(IMemoryEntry entry, int addr) {
			if (activePIO != null) {
				return activePIO.data;
			}
			return (byte) 0;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeByte(v9t9.engine.memory.MemoryEntry, int, byte)
		 */
		@Override
		public void writeByte(IMemoryEntry entry, int addr, byte val) {
			if (activePIO != null) {
				activePIO.data = val;
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
	
	
	private Map<String, PIORegs> pioDeviceMap = new HashMap<String, PIORegs>();
	private Map<Integer, PIORegs> pioDevices = new HashMap<Integer, PIORegs>();

	private IProperty pioActiveSetting;
	private PIOMMIOMemoryArea ioArea;

	private MemoryEntry ioMemoryEntry;

	private PIORegs activePIO;	


	public TIRS232PIODsr(IMachine machine, short base) {
		super(machine, base);
	
		pioActiveSetting = new SettingSchemaProperty(getName(), Boolean.FALSE);

		registerPIODevice(1, "PIO/1");
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.rs232.TIRS232Dsr#initROM(v9t9.common.memory.IMemoryEntryFactory)
	 */
	@Override
	protected void initDevice(IMemoryEntryFactory memoryEntryFactory)
			throws IOException {
		this.romMemoryEntry = memoryEntryFactory.newMemoryEntry(pioDsrRomInfo);
		machine.getConsole().mapEntry(romMemoryEntry);
		
		if (ioMemoryEntry == null) {
			ioArea = new PIOMMIOMemoryArea();
			ioMemoryEntry = new MemoryEntry("TI PIO ROM MMIO", machine.getConsole(), 
					0x5000, 0x1000, ioArea);
		}


		machine.getConsole().mapEntry(ioMemoryEntry);
//		machine.getCpu().settingDumpFullInstructions().setBoolean(true);
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.rs232.TIRS232Dsr#termDevice()
	 */
	@Override
	protected void termDevice() {
//		machine.getCpu().settingDumpFullInstructions().setBoolean(false);

		machine.getConsole().unmapEntry(ioMemoryEntry);
	}

	protected PIORegs getPIODeviceForAddr(int addr) {
		PIORegs regs = getPIODevice(1); //(addr - base) / 0x40 + 1);
		activePIO = regs;
		return regs;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#handleDSR(v9t9.common.dsr.IMemoryTransfer, short)
	 */
	@Override
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		dumper.info(("RealPIODSR: ignoring code = " + code));
		return false;
	}

	protected void registerPIODevice(int index, String name) {
		PIORegs rs = new PIORegs(machine, new PIO(dumper), dumper);
		pioDeviceMap.put(name, rs);
		pioDevices.put(index, rs);
	}
	
	public PIORegs getPIODevice(String name) {
		return pioDeviceMap.get(name);
	}
	public PIORegs getPIODevice(int index) {
		return pioDevices.get(index);
	}

	@Override
	public String getName() {
		return "RS232/PIO DSR";
	}

	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				pioActiveSetting, 
				"RS232/PIO activity",
				IDevIcons.DSR_RS232, IDevIcons.DSR_LIGHT,
				"RS232/PIO Settings",
				IDsrHandler.GROUP_PIO_CONFIGURATION, IDsrHandler.GROUP_RS232_CONFIGURATION);
		return Collections.<IDeviceIndicatorProvider>singletonList(deviceIndicatorProvider);
	}


	@Override
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		Map<String, Collection<IProperty>> map = super.getEditableSettingGroups();

		List<IProperty> settings = new ArrayList<IProperty>(1);
		settings.add(this.settings.get(RS232Settings.settingPIOPrint));
		map.put(IDsrHandler.GROUP_PIO_CONFIGURATION, settings);
		
		return map;
	}
	
	@Override
	public void loadState(ISettingSection section) {
		super.loadState(section);
		if (section == null)
			return;
		
		ISettingSection pioSec = section.getSection("PIO");
		activePIO.loadState(pioSec);
	}

	@Override
	public void saveState(ISettingSection section) {
		super.saveState(section);
		
		ISettingSection pioSec = section.addSection("PIO");
		activePIO.saveState(pioSec);
	}

	private ICruWriter cruwPIO_1 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			regs.reading = data != 0;
			if (!regs.reading) {
				// clear handshake
				regs.handshakeout = true;
			}
			return 0;
		}
	};
	
	private ICruWriter cruwPIO_2 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			regs.handshakeout = data != 0;
			if (!regs.handshakeout) {
				regs.getPIO().transmitChar(regs.data);
			} else {
				regs.handshakein = true;
				
			}
			return 0;
		}
	};
	

	private ICruReader crurPIO_2 = new ICruReader() {
		@Override
		public int read(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			
			if (regs.handshakeout) {
				// initial state after SBZ >1
				regs.handshakein = false;
				
			} else {
				// after SBZ >2
				regs.handshakein = !regs.getPIO().getXmitBuffer().isFull();
			}

			return regs.handshakein ? 1 : 0;
		}
	};
	
	private ICruWriter cruwPIO_3 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			regs.spareout = data != 0;
			return 0;
		}
	};
	private ICruReader crurPIO_3 = new ICruReader() {
		@Override
		public int read(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			return regs.spareout ? 1 : 0;
		}
	};
	

	private ICruWriter cruwPIO_4 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			regs.reflect = data != 0;
			return 0;
		}
	};
	private ICruReader crurPIO_4 = new ICruReader() {
		@Override
		public int read(int addr, int data, int num) {
			PIORegs regs = getPIODeviceForAddr(addr);
			return regs.reflect ? 1 : 0;
		}
	};
	
	/** CTS */
	private ICruWriter cruwPIO_5_6 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			//PIORegs regs = getPIODeviceForAddr(addr);
			return 0;
		}
	};
	/** CTS */
	private ICruReader crurPIO_5_6 = new ICruReader() {
		@Override
		public int read(int addr, int data, int num) {
			//PIORegs regs = getPIODeviceForAddr(addr);
			return 0;
		}
	};
	
	private ICruWriter cruwPIO_7 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			pioActiveSetting.setBoolean(data != 0);
			return 0;
		}
	};
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.rs232.TIRS232Dsr#registerDevicesAndCRUs()
	 */
	@Override
	protected void registerDevicesAndCRUs() {
		super.registerDevicesAndCRUs();

		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		
		cruManager.add(base + 2*1, 1, cruwPIO_1);
		cruManager.add(base + 2*2, 1, cruwPIO_2);
		cruManager.add(base + 2*3, 1, cruwPIO_3);
		cruManager.add(base + 2*4, 1, cruwPIO_4);
		cruManager.add(base + 2*5, 2, cruwPIO_5_6);
		cruManager.add(base + 2*7, 1, cruwPIO_7);
		
		cruManager.add(base + 2*2, 1, crurPIO_2);
		cruManager.add(base + 2*3, 1, crurPIO_3);
		cruManager.add(base + 2*4, 1, crurPIO_4);
		cruManager.add(base + 2*5, 2, crurPIO_5_6);

	}

	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.realRS232.BaseRS232ImageDsr#dispose()
	 */
	@Override
	public void dispose() {
		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		
		cruManager.removeWriter(base + 2*1, 7);
		cruManager.removeReader(base + 2*1, 6);
	}
	
	
}
