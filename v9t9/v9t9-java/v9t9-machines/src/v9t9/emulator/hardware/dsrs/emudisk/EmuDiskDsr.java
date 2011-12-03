/**
 * 
 */
package v9t9.emulator.hardware.dsrs.emudisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.IDeviceIndicatorProvider;
import v9t9.emulator.hardware.dsrs.DeviceIndicatorProvider;
import v9t9.emulator.hardware.dsrs.DsrException;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.DsrHandler9900;
import v9t9.emulator.hardware.dsrs.IDevIcons;
import v9t9.emulator.hardware.dsrs.MemoryTransfer;
import v9t9.emulator.hardware.dsrs.emudisk.DiskDirectoryMapper.EmuDiskSetting;
import v9t9.emulator.hardware.dsrs.realdisk.RealDiskDsrSettings;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;

/**
 * This is a device handler which allows accessing files on the local filesystem.
 * Each directory is a disk.  The DSR instructions in ROM are "enhanced instructions"
 * that forward to the DSR manager and trigger this code.
 * @author ejs
 *
 */
public class EmuDiskDsr implements DsrHandler, DsrHandler9900 {
	private DiskMemoryEntry memoryEntry;
	private short vdpNameCompareBuffer;
	private final IFileMapper mapper;

	private Map<String, SettingProperty> diskActivitySettings;
	private List<IDeviceIndicatorProvider> deviceIndicatorProviders;

	private SettingProperty emuDiskDsrActiveSetting;

	public EmuDiskDsr(IFileMapper mapper) {
		//emuDiskDsrEnabled.setBoolean(true);
		WorkspaceSettings.CURRENT.register(EmuDiskDsrSettings.emuDiskDsrEnabled);
		
		this.mapper = mapper;
		
		
    	String diskRootPath = WorkspaceSettings.CURRENT.getConfigDirectory() + "disks";
    	File diskRootDir = new File(diskRootPath);
    	File dskdefault = new File(diskRootDir, "default");
    	dskdefault.mkdirs();
    	
    	deviceIndicatorProviders = new ArrayList<IDeviceIndicatorProvider>();
    	diskActivitySettings = new HashMap<String, SettingProperty>();

    	// one setting for entire DSR
		emuDiskDsrActiveSetting = new SettingProperty(getName(), Boolean.FALSE);
		emuDiskDsrActiveSetting.addEnablementDependency(EmuDiskDsrSettings.emuDiskDsrEnabled);
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				emuDiskDsrActiveSetting, 
				"Disk directory activity",
				IDevIcons.DSR_DISK_DIR, IDevIcons.DSR_LIGHT);
		deviceIndicatorProviders.add(deviceIndicatorProvider);

		
    	for (int dev = 1; dev <= 5; dev++) {
    		String devname = EmuDiskDsrSettings.getEmuDiskSetting(dev);
    		
    		EmuDiskSetting diskSetting = new EmuDiskSetting(devname, dskdefault.getAbsolutePath(),
    				EmuDiskDsrSettings.diskDirectoryIconPath);
    		WorkspaceSettings.CURRENT.register(diskSetting);
			
			DiskDirectoryMapper.INSTANCE.registerDiskSetting(devname, diskSetting);

			// one setting per disk
			SettingProperty diskActiveSetting = new SettingProperty(devname, Boolean.FALSE);
			diskActiveSetting.addEnablementDependency(EmuDiskDsrSettings.emuDiskDsrEnabled);
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
	public void activate(MemoryDomain console) throws IOException {
		if (!EmuDiskDsrSettings.emuDiskDsrEnabled.getBoolean())
			return;
		
		emuDiskDsrActiveSetting.setBoolean(true);

		if (memoryEntry == null)
			this.memoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "File Stream DSR ROM", console,
					"emudisk.bin", 0, false);
		
		console.mapEntry(memoryEntry);
	}
	
	public void deactivate(MemoryDomain console) {
		console.unmapEntry(memoryEntry);
		emuDiskDsrActiveSetting.setBoolean(false);
	}

	public boolean handleDSR(MemoryTransfer xfer, short code) {
		if (!EmuDiskDsrSettings.emuDiskDsrEnabled.getBoolean())
			return false;
		
		try {
			return doHandleDSR(xfer, code);
		} finally {
			
		}
	}

	private boolean doHandleDSR(MemoryTransfer xfer, short code) {
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
			EmuDiskPabHandler handler = new EmuDiskPabHandler(getCruBase(), xfer, mapper, 
					(short) (vdpNameCompareBuffer + 1));
			
			if (handler.devname.equals("DSK1")
					|| handler.devname.equals("DSK2")) {
				if (RealDiskDsrSettings.diskImageDsrEnabled.getBoolean())
					return false;
			}
			
			SettingProperty settingProperty = diskActivitySettings.get(handler.devname);
			if (settingProperty != null)
				settingProperty.setBoolean(true);
			
			EmuDiskPabHandler.info(handler.toString());
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
			if (!RealDiskDsrSettings.diskImageDsrEnabled.getBoolean())
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
			DirectDiskHandler handler = new DirectDiskHandler(getCruBase(), xfer, mapper, code);
	
			if (handler.dev <= 2 && RealDiskDsrSettings.diskImageDsrEnabled.getBoolean()) {
				//Executor.settingDumpFullInstructions.setBoolean(true);
				return false;
			}
			
			if (handler.getDevice() <= EmuDiskConsts.MAXDRIVE) {
				
				SettingProperty activity = diskActivitySettings.get(EmuDiskDsrSettings.getEmuDiskSetting(handler.dev));
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
			EmuDiskPabHandler.info("EmuDiskDSR: ignoring code = " + code);
			return false;
		}
	}

	private void subAllocFiles(MemoryTransfer xfer) {
		// let real disk allocate space
		if (RealDiskDsrSettings.diskImageDsrEnabled.getBoolean())
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
	private void allocFiles(MemoryTransfer xfer, int count) {
		
		boolean empty = count < 0;
		if (empty)
			count = -count;
		
		PabInfoBlock block = EmuDiskPabHandler.getPabInfoBlock(getCruBase());
		if (block.openFiles.size() > count) {
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
			
			block.maxOpenFileCount = count;

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
		settings.add(EmuDiskDsrSettings.emuDiskDsrEnabled);
		map.put(DsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = Arrays.asList(mapper.getSettings());
		map.put(DsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(ISettingSection section) {
		EmuDiskDsrSettings.emuDiskDsrEnabled.saveState(section);
		mapper.saveState(section.addSection("Mappings"));
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		EmuDiskDsrSettings.emuDiskDsrEnabled.loadState(section);
		mapper.loadState(section.getSection("Mappings"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getDeviceIndicatorProvider()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		return deviceIndicatorProviders;
	}
	
	/**
	 * @return the mapper
	 */
	public IFileMapper getMapper() {
		return mapper;
	}
}
