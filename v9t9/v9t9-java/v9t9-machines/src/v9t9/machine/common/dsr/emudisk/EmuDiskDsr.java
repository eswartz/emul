/**
 * 
 */
package v9t9.machine.common.dsr.emudisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import v9t9.base.settings.ISettingSection;
import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDiskDsr;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.files.Catalog;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.dsr.DeviceIndicatorProvider;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.dsr.emudisk.DirectDiskHandler;
import v9t9.engine.dsr.emudisk.EmuDiskConsts;
import v9t9.engine.dsr.emudisk.EmuDiskDsrSettings;
import v9t9.engine.dsr.emudisk.EmuDiskPabHandler;
import v9t9.engine.dsr.emudisk.FileDirectory;
import v9t9.engine.dsr.emudisk.IFileMapper;
import v9t9.engine.dsr.emudisk.PabInfoBlock;
import v9t9.engine.dsr.realdisk.Dumper;
import v9t9.engine.dsr.realdisk.RealDiskDsrSettings;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.settings.WorkspaceSettings;
import v9t9.machine.common.dsr.emudisk.DiskDirectoryMapper.EmuDiskSetting;
import v9t9.machine.ti99.dsr.DsrHandler9900;

/**
 * This is a device handler which allows accessing files on the local filesystem.
 * Each directory is a disk.  The DSR instructions in ROM are "enhanced instructions"
 * that forward to the DSR manager and trigger this code.
 * @author ejs
 *
 */
public class EmuDiskDsr implements IDsrHandler, DsrHandler9900, IDiskDsr {
	private DiskMemoryEntry memoryEntry;
	private short vdpNameCompareBuffer;
	private final IFileMapper mapper;

	private Map<String, SettingProperty> diskActivitySettings;
	private List<IDeviceIndicatorProvider> deviceIndicatorProviders;

	private SettingProperty emuDiskDsrActiveSetting;
	private SettingProperty settingDsrEnabled;
	private SettingProperty settingRealDsrEnabled;
	private Dumper dumper;

	public EmuDiskDsr(ISettingsHandler settings, IFileMapper mapper) {
		//emuDiskDsrEnabled.setBoolean(true);
		settingDsrEnabled = settings.get(EmuDiskDsrSettings.emuDiskDsrEnabled);
		settingRealDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		
		this.dumper = new Dumper(settings);
		
		this.mapper = mapper;
		
		
    	String diskRootPath = WorkspaceSettings.CURRENT.getConfigDirectory() + "disks";
    	File diskRootDir = new File(diskRootPath);
    	File dskdefault = new File(diskRootDir, "default");
    	dskdefault.mkdirs();
    	
    	deviceIndicatorProviders = new ArrayList<IDeviceIndicatorProvider>();
    	diskActivitySettings = new HashMap<String, SettingProperty>();

    	// one setting for entire DSR
		emuDiskDsrActiveSetting = new SettingProperty(getName(), Boolean.FALSE);
		emuDiskDsrActiveSetting.addEnablementDependency(settingDsrEnabled);
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				emuDiskDsrActiveSetting, 
				"Disk directory activity",
				IDevIcons.DSR_DISK_DIR, IDevIcons.DSR_LIGHT);
		deviceIndicatorProviders.add(deviceIndicatorProvider);

		
    	for (int dev = 1; dev <= 5; dev++) {
    		String devname = EmuDiskDsrSettings.getEmuDiskSetting(dev);
    		
    		EmuDiskSetting diskSetting = settings.get(ISettingsHandler.WORKSPACE,
    				new EmuDiskSetting(devname, dskdefault.getAbsolutePath(),
    						EmuDiskDsrSettings.diskDirectoryIconPath));
			
			DiskDirectoryMapper.INSTANCE.registerDiskSetting(devname, diskSetting);

			// one setting per disk
			SettingProperty diskActiveSetting = new SettingProperty(devname, Boolean.FALSE);
			diskActiveSetting.addEnablementDependency(settingDsrEnabled);
			diskActivitySettings.put(devname, diskActiveSetting);
			/*
			deviceIndicatorProvider = new DeviceIndicatorProvider(
					diskActiveSetting, 
					devname + " activity",
					20, 21);
			deviceIndicatorProviders.add(deviceIndicatorProvider);
			*/
    	}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	public void dispose() {
		
	}
	
	public String getName() {
		return "Emulated Disk DSR";
				
	}
	public short getCruBase() {
		return 0x1000;
	}
	public void activate(IMemoryDomain console) throws IOException {
		if (!settingDsrEnabled.getBoolean())
			return;
		
		emuDiskDsrActiveSetting.setBoolean(true);

		if (memoryEntry == null)
			this.memoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "File Stream DSR ROM", console,
					"emudisk.bin", 0, false);
		
		console.mapEntry(memoryEntry);
	}
	
	public void deactivate(IMemoryDomain console) {
		console.unmapEntry(memoryEntry);
		emuDiskDsrActiveSetting.setBoolean(false);
	}

	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		if (!settingDsrEnabled.getBoolean())
			return false;
		
		try {
			return doHandleDSR(xfer, code);
		} finally {
			
		}
	}

	private boolean doHandleDSR(IMemoryTransfer xfer, short code) {
		switch (code) {
		// PAB file operation on DSKx 
		case EmuDiskConsts.D_DSK:
			// find disk
		case EmuDiskConsts.D_DSK1:
		case EmuDiskConsts.D_DSK2:
		case EmuDiskConsts.D_DSK3:
		case EmuDiskConsts.D_DSK4:
		case EmuDiskConsts.D_DSK5:
		{
			EmuDiskPabHandler handler = new EmuDiskPabHandler(
					dumper,
					getCruBase(), xfer, mapper, 
					(short) (vdpNameCompareBuffer + 1));
			
			if (handler.devname.equals("DSK1")
					|| handler.devname.equals("DSK2")) {
				if (settingRealDsrEnabled.getBoolean())
					return false;
			}
			
			SettingProperty settingProperty = diskActivitySettings.get(handler.devname);
			if (settingProperty != null)
				settingProperty.setBoolean(true);
			
			dumper.info(handler.toString());
			try {
				handler.run();
			} catch (DsrException e) {
				handler.error(e);
			}
			handler.store();
	
			if (settingProperty != null)
				settingProperty.setBoolean(false);
			
			//  return, indicating that the DSR handled the operation 
			return true;
		}
			/* init disk dsr */
		case EmuDiskConsts.D_INIT:
		{
			for (SettingProperty property : diskActivitySettings.values()) {
				property.setBoolean(true);
			}
			
			EmuDiskPabHandler.getPabInfoBlock(getCruBase()).reset();
			DirectDiskHandler.getDiskInfoBlock(getCruBase()).reset();
			
			// steal some RAM for the name compare buffer,
			// so dependent programs can function
			if (!settingRealDsrEnabled.getBoolean())
				vdpNameCompareBuffer = (short) (xfer.readParamWord(0x70) - 11);
			else
				vdpNameCompareBuffer = (short) 0x3ff5;
			
			allocFiles(xfer, -3);
			
			// ???
			//xfer.writeParamWord(0x6c, (short) 0x404);
			
			for (SettingProperty property : diskActivitySettings.values()) {
				property.setBoolean(false);
			}
			
			return false;  // does not bump return
		}
	
			/* # of files */
		case EmuDiskConsts.D_FILES:
		{
			subAllocFiles(xfer);
			return true;
		}
	
			/* call files(x) from BASIC */
		case EmuDiskConsts.D_CALL_FILES:
			// 0x50 is 0xffff ?
			// 0x52 points to BASIC tokenization of argument
			int argptr = xfer.readParamWord(0x2c) + 7;
			short arginfo = xfer.readVdpShort(argptr);
			// codes for "number literal", with length 1
			boolean error = true;
			if (arginfo == (short)0xC801) {
				argptr += 2;
				int cnt = xfer.readVdpByte(argptr++);
				cnt -= '0';
				xfer.writeParamByte(0x4c, (byte) cnt);
				subAllocFiles(xfer);
				error = xfer.readParamByte(0x50) != 0;
			}
			if (!error) {
				// advance param ptr
				xfer.writeParamWord(0x2c, (short)(argptr + 2));
				// clear error
				xfer.writeParamByte(0x42, (byte) 0);
			}
			
			return true;
	
		//case D_FMTDISK:
		case EmuDiskConsts.D_PROT:
		case EmuDiskConsts.D_RENAME:
		case EmuDiskConsts.D_DINPUT:
		case EmuDiskConsts.D_DOUTPUT:
		case EmuDiskConsts.D_SECRW:
		{
			DirectDiskHandler handler = new DirectDiskHandler(
					dumper, getCruBase(), xfer, mapper, code);
	
			if (handler.getDevice() <= 2 && settingRealDsrEnabled.getBoolean()) {
				//Executor.settingDumpFullInstructions.setBoolean(true);
				return false;
			}
			
			if (handler.getDevice() <= EmuDiskConsts.MAXDRIVE) {
				
				SettingProperty activity = diskActivitySettings.get(EmuDiskDsrSettings.getEmuDiskSetting(handler.getDevice()));
				if (activity != null)
					activity.setBoolean(true);
				
				try {
					handler.run();
				} catch (DsrException e) {
					handler.error(e);
				} finally {
					if (activity != null)
						activity.setBoolean(false);
				}
				return true;
			} else {
				// unhandled device
				return false;
			}
		}
	
		default:
			dumper.info("EmuDiskDSR: ignoring code = " + code);
			return false;
		}
	}

	private void subAllocFiles(IMemoryTransfer xfer) {
		// let real disk allocate space
		if (settingRealDsrEnabled.getBoolean())
			return;
		
		int cnt = xfer.readParamByte(0x4c);
		if (Math.abs(cnt) > 16) { 
			xfer.writeParamWord(0x50, (short) -1);
		} else {
			allocFiles(xfer, cnt);
		}
	}

	/**
	 * Implement backend for CALL FILES or FILES subprograms.
	 * Since we actually need no memory, allow count<0 to mean
	 * -count files open but no VDP space consumed.
	 * @param xfer
	 * @param count
	 */
	private void allocFiles(IMemoryTransfer xfer, int count) {
		
		boolean empty = count < 0;
		if (empty)
			count = -count;
		
		PabInfoBlock block = EmuDiskPabHandler.getPabInfoBlock(getCruBase());
		if (block.getOpenFiles().size() > count) {
			xfer.writeParamWord(0x50, (short) -1);
			return;
		}

		int addr;
		if (!empty) {
			/*
			 * filename compare == 11 (vdpnamebuffer)
			 * 
			 * VIB from sector 0 = 256
			 * not used = 6 bytes
			 * disk drive info = 4 bytes
			 * VDP stack = 252 bytes
			 */
			short vdptop = (short) (vdpNameCompareBuffer - 256 - 6 - 4 - 252);

			/*
			 * Buffer area header = 5 bytes
			 * {
			 * 		File control block = 6 bytes
			 * 		FDR for file = 256
			 * 		Data buffer = 256
			 * }
			 */
			addr = vdptop - count * (256 + 256 + 6) - 5;
		}
		else
			addr = vdpNameCompareBuffer;
		
		if (addr > 0) {
			xfer.writeParamWord(0x70, (short) addr);
			xfer.writeParamWord(0x50, (short) 0);
			
			block.setMaxOpenFileCount(count);

		} else {
			xfer.writeParamWord(0x50, (short) -1);
		}
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	public Map<String, Collection<SettingProperty>> getEditableSettingGroups() {
		Map<String, Collection<SettingProperty>> map = new LinkedHashMap<String, Collection<SettingProperty>>();
		
		Collection<SettingProperty> settings = new ArrayList<SettingProperty>();
		settings.add(settingDsrEnabled);
		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = Arrays.asList(mapper.getSettings());
		map.put(IDsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(ISettingSection section) {
		settingDsrEnabled.saveState(section);
		mapper.saveState(section.addSection("Mappings"));
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		settingDsrEnabled.loadState(section);
		mapper.loadState(section.getSection("Mappings"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProvider()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		return deviceIndicatorProviders;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDiskDsr#isImageBased()
	 */
	@Override
	public boolean isImageBased() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.dsr.IDiskDsr#getCatalog(v9t9.base.properties.SettingProperty)
	 */
	@Override
	public Catalog getCatalog(SettingProperty diskSetting) {
		FileDirectory fileDir = new FileDirectory(
				new File(diskSetting.getString()), 
						mapper);
		Catalog catalog = fileDir.readCatalog();

		return catalog;
	}
	
	
}
