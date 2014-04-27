/**
 * 
 */
package v9t9.machine.ti99.dsr.rs232;

import java.io.IOException;
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
import static v9t9.machine.ti99.dsr.rs232.RS232Constants.*;
/**
 * @author ejs
 *
 */
public class TIRS232Dsr implements IDsrHandler9900, IDeviceSettings {
	private IMemoryEntry romMemoryEntry;


	public static SettingSchema settingDsrRomFileName = new SettingSchema(
			ISettingsHandler.MACHINE,
			"RS232DsrFileName",
			"rs232.bin");

	
	public static MemoryEntryInfo dsrRomInfo = MemoryEntryInfoBuilder
			.standardDsrRom(null)
			.withFilenameProperty(settingDsrRomFileName)
			.withDescription("RS232 Controller ROM")
			.withFileMD5("4E4E08FF10D23B799AAA990344553E2E")
			//.withFileMD5Limit(0x1FF0)
			.create("TI RS232 DSR ROM");
	
	

	private short base;

	private Dumper dumper;

	private boolean inited;

	protected IProperty rs232ActiveSetting;

	protected IMachine machine;

	private Map<String, RS232Regs> deviceMap = new HashMap<String, RS232Regs>();
	private Map<Integer, RS232Regs> devices = new HashMap<Integer, RS232Regs>();
	
	public TIRS232Dsr(IMachine machine, short base) {
		this.machine = machine;
		this.base = base;
		
		rs232ActiveSetting = new SettingSchemaProperty(getName(), Boolean.FALSE);
		
		this.dumper = new Dumper(Settings.getSettings(machine),
				RS232Settings.settingRS232Debug, ICpu.settingDumpFullInstructions);
		
	
	}
	
	public void activate(IMemoryDomain console, IMemoryEntryFactory memoryEntryFactory) throws IOException {
		init();
		
		rs232ActiveSetting.setBoolean(true);

		this.romMemoryEntry = memoryEntryFactory.newMemoryEntry(dsrRomInfo);
		
		console.mapEntry(romMemoryEntry);
		
		machine.getCpu().settingDumpFullInstructions().setBoolean(true);
	}
	
	public void deactivate(IMemoryDomain console) {
		console.unmapEntry(romMemoryEntry);
		
		rs232ActiveSetting.setBoolean(false);
		
		machine.getCpu().settingDumpFullInstructions().setBoolean(false);
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
			RS232Regs regs = getDeviceForAddr(addr);
			int bit = 1 << ((addr & 0x1f) >> 1);
			
			if (bit >= 0x100) {
				dumper.info(String.format("RealRS232_0_10_w: %d / %d", (addr & 0x3f) / 2, data));
			}
			regs.setReadPort(regs.getReadPort() & ~RS_FLAG);
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
			RS232Regs regs = getDeviceForAddr(addr);
			int bit = 1 << (((addr & 0x1f) >> 1) - 11);
			
			dumper.info(String.format("RealRS232_11_14_w: %d / %d", (addr & 0x3f) / 2, data));
			
			regs.setReadPort(regs.getReadPort() | RS_FLAG);
			
			regs.updateRegisterSelect(data, bit);
			
			//rs.triggerChange(bit, 0);
			
			return 0;
		}
	};
	
	/**
	 *	Remaining write ports
	 *
	 */
	private ICruWriter cruwRealRS232_16_21 = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getDeviceForAddr(addr);
			RS232 rs = regs.getRS232();
			int bit = 1 << ((addr & 0x3f) >> 1);
			
			dumper.info(String.format("RealRS232_16_21_w: %d / %d", (addr & 0x3f) / 2, data));
			
			int old = regs.getWritePort();
			regs.updateWritePort(data, bit);
			
			regs.dump();
			rs.setControlBits(old, bit);
			
			return 0;
		}
	};
	
	private ICruWriter cruwRealRS232_Reset = new ICruWriter() {
		public int write(int addr, int data, int num) {
			RS232Regs regs = getDeviceForAddr(addr);
			
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
			RS232Regs regs = getDeviceForAddr(addr);
			RS232 rs = regs.getRS232();
			int bit = 1 << ((addr & 0xf) >> 1);

			if (bit == 1) {
				byte ch = rs.receiveData();
				regs.setReadPort(ch);
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
			RS232Regs regs = getDeviceForAddr(addr);
			RS232 rs = regs.getRS232();
			int bit = 1 << ((addr & 0x3f) >> 1);

			int bits = rs.readStatusBits();
			
			// force DSR while buffer is nonempty
			if (!rs.isRecvBufferEmpty()) {
				bits |= RS_DSR;
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
		
		registerDevice(1, "RS232/1");
		registerDevice(2, "RS232/2");
		
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


	protected RS232Regs getDeviceForAddr(int addr) {
		return getDevice((addr - base) / 0x40);
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

	protected void registerDevice(int index, String name) {
		RS232Regs rs = new RS232Regs(new RS232(dumper), dumper);
		deviceMap.put(name, rs);
		devices.put(index, rs);
	}

	protected RS232Regs getDevice(String name) {
		return deviceMap.get(name);
	}
	protected RS232Regs getDevice(int index) {
		return devices.get(index);
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
				IDevIcons.DSR_RS232, IDevIcons.DSR_LIGHT);
		return Collections.<IDeviceIndicatorProvider>singletonList(deviceIndicatorProvider);
	}

	@Override
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
			// TODO Auto-generated method stub
			Map<String, Collection<IProperty>> map = new LinkedHashMap<String, Collection<IProperty>>();
	//		Collection<IProperty> settings;
	//		
	//		settings = new ArrayList<IProperty>(2);
	//		settings.add(this.settings.get(RealDiskSettings.diskImagesEnabled));
	//		settings.add(this.settings.get(RealDiskSettings.diskController));
	//		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
	//		
	//		settings = new ArrayList<IProperty>(imageMapper.getDiskSettingsMap().values());
	//		settings.add(settingRealTime);
	//		settings.add(settingDebug);
	//		map.put(IDsrHandler.GROUP_DISK_CONFIGURATION, settings);
			
			return map;
		}

	@Override
	public void loadState(ISettingSection section) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveState(ISettingSection section) {
		// TODO Auto-generated method stub
		
	}

}
