/*
  TIRS232Dsr.java

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.common.settings.Settings;
import v9t9.engine.Dumper;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.dsr.rs232.RS232;
import v9t9.engine.hardware.CruManager;
import v9t9.engine.hardware.ICruReader;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.ti99.dsr.IDsrHandler9900;
import v9t9.machine.ti99.machine.TI99Machine;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
/**
 * @author ejs
 *
 */
public class TIRS232Dsr implements IDsrHandler9900, IDeviceSettings {
	protected IMemoryEntry romMemoryEntry;


	public static SettingSchema settingRS232DsrRomFileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"RS232DsrFileName",
			"rs232pio.bin");

	
	public static MemoryEntryInfo rs232DsrRomInfo = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withFilenameProperty(settingRS232DsrRomFileName)
			.withDescription("RS232 Controller ROM")
			.withFileMD5("4E4E08FF10D23B799AAA990344553E2E")
//			.withFileMD5("A5467BE2E6BD04E1B9CF1DE8FA507541")
			.create("TI RS232 DSR ROM");
	
	

	protected short base;

	protected Dumper dumper;

	protected boolean inited;

	private IProperty rs232ActiveSetting;

	protected IMachine machine;

	private Map<String, RS232Regs> rs232DeviceMap = new HashMap<String, RS232Regs>();
	private Map<Integer, RS232Regs> rs232Devices = new HashMap<Integer, RS232Regs>();


	private ISettingsHandler settings;

	public TIRS232Dsr(IMachine machine, short base) {
		this.machine = machine;
		this.base = base;
		
		rs232ActiveSetting = new SettingSchemaProperty(getName(), Boolean.FALSE);
		
		this.settings = Settings.getSettings(machine);
		this.dumper = new Dumper(settings,
				RS232Settings.settingRS232Debug, ICpu.settingDumpFullInstructions);
	
		registerRS232Device(1, "RS232/1");
		registerRS232Device(2, "RS232/2");
	}
	
	public void activate(IMemoryDomain console, IMemoryEntryFactory memoryEntryFactory) throws IOException {
		init();
		
		rs232ActiveSetting.setBoolean(true);

		initDevice(memoryEntryFactory);
	}

	/**
	 * @param memoryEntryFactory
	 * @throws IOException
	 */
	protected void initDevice(IMemoryEntryFactory memoryEntryFactory)
			throws IOException {
		this.romMemoryEntry = memoryEntryFactory.newMemoryEntry(rs232DsrRomInfo);
		machine.getConsole().mapEntry(romMemoryEntry);
		//machine.getCpu().settingDumpFullInstructions().setBoolean(true);
	}
	
	public void deactivate(IMemoryDomain console) {
		console.unmapEntry(romMemoryEntry);
		
		termDevice();
	}

	/**
	 * 
	 */
	protected void termDevice() {
		//machine.getCpu().settingDumpFullInstructions().setBoolean(false);
		rs232ActiveSetting.setBoolean(false);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return base;
	}
	
	/** 
	 *	Writes of low register bits
	 */
	private ICruWriter cruwRealRS232_0_10 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			int bit = 1 << ((addr & 0x1f) >> 1);
			
			if (bit >= 0x100) {
				dumper.info(String.format("RealRS232_0_10_w: %d / %d", (addr & 0x3f) / 2, data));
			}
			regs.setReadPort(regs.getReadPort() & ~RS232Regs.RS_FLAG);
			regs.updateWriteBits(data, bit);
			regs.triggerChange(bit);
			return 0;
		}
	};
	
	/**
	 *	Load control register bits
	 */
	private ICruWriter cruwRealRS232_11_14 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			int bit = 1 << (((addr & 0x1f) >> 1) - 11);
			
			dumper.info(String.format("RealRS232_11_14_w: %d / %d", (addr & 0x3f) / 2, data));
			
			regs.setReadPort(regs.getReadPort() | RS232Regs.RS_FLAG);
			
			regs.updateRegisterSelect(data, bit);
			
			regs.triggerChange(0);
			
			return 0;
		}
	};
	
	/**
	 *	Remaining write ports
	 *
	 */
	private ICruWriter cruwRealRS232_16_21 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			int bit = 1 << ((addr & 0x3f) >> 1);
			
			dumper.info(String.format("RealRS232_16_21_w: %d / %d", (addr & 0x3f) / 2, data));
			
			regs.updateWritePort(data, bit);
			
			regs.dump();
			
			//rs.setControlBits(regs.getDataSize(), regs.getParity(), regs.getStop());
			
			return 0;
		}
	};
	
	private ICruWriter cruwRealRS232_Reset = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			
			dumper.info(String.format("RealRS232_Reset_w: %d", data));

			if (data != 0) {
				
				regs.clear();
			
			}
			
			return 0;
		}
	};
	
	/**
	 *	Read character
	 *
	 */
	private ICruReader crurRealRS232_0_7 = new ICruReader() {
		public int read(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			RS232 rs = regs.getRS232();
			int bit = 1 << ((addr & 0xf) >> 1);

			if (bit == 1) {
				byte ch = rs.receiveData();
				regs.updateFlagsAndInts();
				regs.setReadPort((regs.getReadPort() & ~0xff) | (ch & 0xff));
				regs.dump();
			}

			int ret = (regs.getReadPort() & bit) != 0 ? 1 : 0;

			if (bit >= 0x100) {
				dumper.info(String.format("RealRS232_0_7_r: %04X / %d", addr, ret));
			}

			return ret;
		}
	};
	
	/**
	 *	Read status bit
	 *
	 */
	private ICruReader crurRealRS232_9_31 = new ICruReader() {
		public int read(int addr, int data, int num) {
			RS232Regs regs = getRS232DeviceForAddr(addr);
			RS232 rs = regs.getRS232();
			int bit = 1 << ((addr & 0x3f) >> 1);

			int bits = rs.readStatusBits();
			
			// force DSR while buffer is nonempty
			if (!rs.getRecvBuffer().isEmpty()) {
				bits |= RS232Regs.RS_DSR;
			}
			if (!rs.getXmitBuffer().isFull()) {
				bits |= RS232Regs.RS_DSR;
			} else {
				bits |= RS232Regs.RS_CTS;
			}
			if (!rs.getXmitBuffer().isFull()) {
				bits |= RS232Regs.RS_XBRE | RS232Regs.RS_XSRE;
			}
			regs.setReadPort(bits);
			
			regs.dump();
			
			int ret = (bits & bit) != 0 ? 1 : 0;
			

			//  turn off bit once read
		//  if (bit == RS_INT || bit == RS_RBINT || bit == RS_XBINT || 
//		      bit == RS_TIMINT || bit == RS_DSCINT) 
//		      rs->rdport &= ~bit;

			dumper.info(String.format("RealRS232_9_31_r: %d / %d", (addr & 0x3f) / 2, ret));
			return ret;
		}
	};



	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.rs232.BaseRS232Dsr#init()
	 */
	@Override
	public void init() {
		if (inited)
			return;
		
		inited = true;
		
		registerDevicesAndCRUs();

	}

	/**
	 * 
	 */
	protected void registerDevicesAndCRUs() {
		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		
		for (int dev = 1; dev <= 2; dev++) {
			short base = (short) (this.base + dev * 0x40);
			
			cruManager.add(base + 2*0, 11, cruwRealRS232_0_10);
			cruManager.add(base + 2*11, 4, cruwRealRS232_11_14);
			cruManager.add(base + 2*16, 6, cruwRealRS232_16_21);
			cruManager.add(base + 2*31, 1, cruwRealRS232_Reset);
			
			cruManager.add(base + 2*0, 8, crurRealRS232_0_7);
			cruManager.add(base + 2*9, 23, crurRealRS232_9_31);
		}
	}

	protected RS232Regs getRS232DeviceForAddr(int addr) {
		return getRS232Device((addr - base) / 0x40);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.realRS232.BaseRS232ImageDsr#dispose()
	 */
	@Override
	public void dispose() {
		CruManager cruManager = ((TI99Machine) machine).getCruManager();
		for (int dev = 1; dev <= 2; dev++) {
			short base = (short) (this.base + dev * 0x40);
			
			cruManager.removeWriter(base + 2*0, 11);
			cruManager.removeWriter(base + 2*11, 4);
			cruManager.removeWriter(base + 2*16, 6);
			cruManager.removeWriter(base + 2*31, 1);
			
			cruManager.removeReader(base + 2*0, 8);
			cruManager.removeReader(base + 2*9, 23);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.dsr.IDsrHandler9900#handleDSR(v9t9.common.dsr.IMemoryTransfer, short)
	 */
	@Override
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		dumper.info(("RealRS232DSR: ignoring code = " + code));
		return false;
	}

	protected void registerRS232Device(int index, String name) {
		RS232Regs rs = new RS232Regs(machine, new RS232(machine.getFastMachineTimer(), dumper), dumper);
		rs232DeviceMap.put(name, rs);
		rs232Devices.put(index, rs);
	}

	public RS232Regs getRS232Device(String name) {
		return rs232DeviceMap.get(name);
	}
	public RS232Regs getRS232Device(int index) {
		return rs232Devices.get(index);
	}

	@Override
	public String getName() {
		return "RS232 DSR";
	}

	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				rs232ActiveSetting, 
				"RS232 activity",
				IDevIcons.DSR_RS232, IDevIcons.DSR_LIGHT,
				null);
		return Collections.<IDeviceIndicatorProvider>singletonList(deviceIndicatorProvider);
	}

	@Override
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		Map<String, Collection<IProperty>> map = new LinkedHashMap<String, Collection<IProperty>>();
		
		Collection<IProperty> settings;
		
		settings = new ArrayList<IProperty>(2);
		settings.add(this.settings.get(RS232Settings.rs232Controller));
		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
		
		return map;
	}

	@Override
	public void loadState(ISettingSection section) {
		for (Map.Entry<String, RS232Regs> ent : rs232DeviceMap.entrySet()) {
			ISettingSection rsSec = section.getSection(ent.getKey());
			ent.getValue().loadState(rsSec);
		}
	}

	@Override
	public void saveState(ISettingSection section) {
		for (Map.Entry<String, RS232Regs> ent : rs232DeviceMap.entrySet()) {
			ISettingSection rsSec = section.addSection(ent.getKey());
			ent.getValue().saveState(rsSec);
		}
		
	}

}
