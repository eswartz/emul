/*
  BaseDiskImageDsr.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr.realdisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.files.image.Dumper;
import v9t9.engine.files.image.FDC1771;
import v9t9.engine.files.image.RealDiskSettings;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public abstract class BaseDiskImageDsr implements IDeviceSettings, IDsrHandler {

	
	protected FDC1771 fdc;
	
	/** note: the side is global to all disks, though we propagate it to all DiskInfos */
	protected byte side;
	
	
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


	private IProperty settingRealTime;
	protected IProperty settingImagesEnabled;
	protected IProperty settingDiskController;
	private IProperty settingDebug;

	protected Dumper dumper;

	protected ISettingsHandler settings;

	protected IDiskImageMapper imageMapper;

	protected IMachine machine;


	public BaseDiskImageDsr(IMachine machine) {
		this.machine = machine;
		imageMapper = machine.getEmulatedFileHandler().getDiskImageMapper();
		settings = Settings.getSettings(machine);
		dumper = new Dumper(settings,
				RealDiskSettings.diskImageDebug, ICpu.settingDumpFullInstructions);

		// register
		settingImagesEnabled = settings.get(RealDiskSettings.diskImagesEnabled);
		settingDiskController = settings.get(RealDiskSettings.diskController);
		settingRealTime = settings.get(RealDiskSettings.diskImageRealTime);
		settingDebug = settings.get(RealDiskSettings.diskImageDebug);
		
		settingDiskController.addEnablementDependency(settings.get(RealDiskSettings.diskImagesEnabled));
		
    	String diskImageRootPath = settings.getMachineSettings().getConfigDirectory() + "disks";
    	RealDiskSettings.defaultDiskRootDir = new File(diskImageRootPath);
    	RealDiskSettings.defaultDiskRootDir.mkdirs();
	}
	
	public void setDiskSide(int side_) {
		// the side is global to all disks
		side = (byte) side_;
		
		try {
			fdc.setSide(side);
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}
		
//		for (IDiskImage info : imageMapper.getDiskImageMap().values()) {
//			if (info instanceof BaseDiskImage)
//				((BaseDiskImage) info).setSide(side);
//		}
		
	}

	public int getSide() {
		return side;
	}

	public String getName() {
		return "Disk Image DSR";
				
	}
	
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		dumper.info(("RealDiskDSR: ignoring code = " + code));
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		Map<String, Collection<IProperty>> map = new LinkedHashMap<String, Collection<IProperty>>();
		Collection<IProperty> settings;
		
		settings = new ArrayList<IProperty>(2);
		settings.add(this.settings.get(RealDiskSettings.diskImagesEnabled));
		settings.add(this.settings.get(RealDiskSettings.diskController));
		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = new ArrayList<IProperty>(imageMapper.getDiskSettingsMap().values());
		settings.add(settingRealTime);
		settings.add(settingDebug);
		map.put(IDsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	
	public void saveState(ISettingSection section) {
		settingImagesEnabled.saveState(section);
		fdc.saveState(section.addSection("FDC1771"));
		imageMapper.saveState(section);
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		settingImagesEnabled.loadState(section);
		fdc.loadState(section.getSection("FDC1771"));
		imageMapper.loadState(section);
	}

	class DiskMotorIndicatorProvider implements IDeviceIndicatorProvider {

		private String name;
		private IProperty activeProperty;

		public DiskMotorIndicatorProvider(String name, IProperty activeProperty) {
			this.name = name;
			this.activeProperty = activeProperty;
		}

		@Override
		public String getToolTip() {
			return name + " activity";
		}
		
		@Override
		public int getBaseIconIndex() {
			return IDevIcons.LIGHT_OFF;
		}
		
		@Override
		public int getActiveIconIndex() {
			return IDevIcons.LIGHT_ON;
		}
		
		@Override
		public IProperty getActiveProperty() {
			return activeProperty;
		}
	}
}
