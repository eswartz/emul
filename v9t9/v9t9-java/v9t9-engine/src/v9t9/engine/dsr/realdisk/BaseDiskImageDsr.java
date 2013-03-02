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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.common.settings.Settings;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.files.image.BaseDiskImage;
import v9t9.engine.files.image.DiskImageFactory;
import v9t9.engine.files.image.DiskImageSetting;
import v9t9.engine.files.image.Dumper;
import v9t9.engine.files.image.FDC1771;
import v9t9.engine.files.image.RealDiskConsts;
import v9t9.engine.files.image.RealDiskDsrSettings;
import v9t9.engine.files.image.StatusBit;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public abstract class BaseDiskImageDsr implements IDeviceSettings {
	/** setting name (DiskImage1) to setting */
	protected Map<String, IProperty> diskSettingsMap = new LinkedHashMap<String, IProperty>();
	
	FDC1771 fdc;
	
	/** currently selected disk */
	byte selectedDisk = 0;

	/** note: the side is global to all disks, though we propagate it to all DiskInfos */
	protected byte side;
	

	private Map<String, BaseDiskImage> disks = new LinkedHashMap<String, BaseDiskImage>();
	
	protected int getSelectedDisk() {
		return selectedDisk;
	}
	BaseDiskImage getSelectedDiskImage() {
		if (selectedDisk == 0)
			return null;
		return getDiskInfo(selectedDisk);
	}
	
	
	private BaseDiskImage getDiskInfo(int num) {
		String name = RealDiskDsrSettings.getDiskImageSetting(num);
		return getDiskImage(name);
	}
	protected BaseDiskImage getDiskImage(String name) {
		BaseDiskImage info = disks.get(name);
		if (info == null) {
			IProperty setting = diskSettingsMap.get(name);
			if (setting == null)
				return null;
			info = DiskImageFactory.createDiskImage(settings, name, new File(setting.getString()));
			disks.put(name, info);
		}
		return disks.get(name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	public void dispose() {
	}


	private void registerDiskImagePath(String device, File dskfile) {
		DiskImageSetting diskSetting = settings.get(ISettingsHandler.MACHINE,
				new DiskImageSetting(settings, 
						device, dskfile.getAbsolutePath(),
						RealDiskDsrSettings.diskImageIconPath));
	
		diskSettingsMap.put(device, diskSetting); 
		diskSetting.addListener(new IPropertyListener() {
			
			public void propertyChanged(IProperty setting) {
	
				BaseDiskImage image = disks.get(setting.getName());
				if (image != null) {
					if (image == fdc.getImage())
						fdc.setImage(null);
					
				}
				image = DiskImageFactory.createDiskImage(settings, 
						setting.getName(), new File(setting.getString()));
				disks.put(setting.getName(), image);
						
				//setting.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
	}

	
	private TimerTask motorTickTask;

	private byte lastStatus = -1;


	private IProperty settingRealTime;
	protected IProperty settingDsrEnabled;
	private IProperty settingDebug;

	protected Dumper dumper;

	protected ISettingsHandler settings;


	public BaseDiskImageDsr(IMachine machine) {
		settings = Settings.getSettings(machine);
		dumper = new Dumper(settings,
				RealDiskDsrSettings.diskImageDebug, ICpu.settingDumpFullInstructions);

		// register
		settingDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		settingRealTime = settings.get(RealDiskDsrSettings.diskImageRealTime);
		settingDebug = settings.get(RealDiskDsrSettings.diskImageDebug);
		
		fdc = new FDC1771(dumper);
		
    	String diskImageRootPath = settings.getMachineSettings().getConfigDirectory() + "disks";
    	RealDiskDsrSettings.defaultDiskRootDir = new File(diskImageRootPath);
    	RealDiskDsrSettings.defaultDiskRootDir.mkdirs();
    	
    	for (int drive = 1; drive <= 3; drive++) {
    		String name = RealDiskDsrSettings.getDiskImageSetting(drive);
			registerDiskImagePath(name, RealDiskDsrSettings.getDefaultDiskImage(name)); 
    	}
    	
		// add motor timer
		motorTickTask = new TimerTask() {
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				
				if (now > fdc.commandBusyExpiration)
					fdc.commandBusyExpiration = 0;
				
				Set<Entry<String, BaseDiskImage>> entrySet = new HashSet<Entry<String,BaseDiskImage>>(disks.entrySet());
				for (Map.Entry<String, BaseDiskImage> entry : entrySet) {
					String name = "DSK" + entry.getKey().charAt(entry.getKey().length() - 1);
					BaseDiskImage info = entry.getValue();
					if (info.getMotorTimeout() != 0) {
						if (!info.isMotorRunning()) {
							if (now >= info.getMotorTimeout()) {
								info.setMotorRunning(true);
								dumper.info("{0}: motor on", name);
								
								fdc.getStatus().reset(StatusBit.BUSY);
								info.setMotorTimeout(System.currentTimeMillis() + 4320);
							}
						} else {
							if (now >= info.getMotorTimeout()) {
								info.setMotorTimeout(0);
								info.setMotorRunning(false);
								dumper.info("{0}: motor off", name);
								updateDiskActivity(entry.getKey(), false);
								//info.getInUseSetting().setBoolean(false);
							}
						}
					}
				}
			}
		};
		
		machine.getMachineTimer().scheduleAtFixedRate(motorTickTask, 0, 100);
	}
	
	public void setDiskSide(int side_) {
		// the side is global to all disks
		side = (byte) side_;
		
		try {
			fdc.setSide(side);
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}
		
		for (BaseDiskImage info : disks.values()) {
			info.setSide(side);
		}
		
	}


	public void selectDisk(int newnum, boolean on) {
		//module_logger(&realDiskDSR, _L|L_1, _("CRU disk select, #%d\n"), newnum);
		
		if (on) {
			BaseDiskImage oldInfo = getSelectedDiskImage();
			
			selectedDisk = (byte) newnum;
			
			BaseDiskImage info = getDiskInfo(newnum);
			
			if (oldInfo != info) {
				if (oldInfo != null) {
					fdc.setImage(null);
					try {
						oldInfo.closeDiskImage();
					} catch (IOException e) {
						dumper.error(e.getMessage());
					}
					
					updateDiskActivity(info.getName(), false);
					//oldInfo.getInUseSetting().setBoolean(false);
					
					// just in case the image went missing
					if (info != null)
						info.validateDiskImage();
				}
			}
			if (info != null) {
				if (info.getHandle() == null) {
					try {
						info.openDiskImage();
					} catch (IOException e) {
						dumper.error(e.getMessage());
					}
				}
				fdc.setImage(info);
			}

		} else {
			BaseDiskImage oldInfo = getSelectedDiskImage();
			
			if (selectedDisk == newnum) {
				selectedDisk = 0;
			
				if (oldInfo != null) {
					try {
						oldInfo.closeDiskImage();
					} catch (IOException e) {
						dumper.error(e.getMessage());
					}
					fdc.setImage(null);
				}
			}
		}
				
	}
	
	public int getDiskNumber() {
		return selectedDisk;
	}
	public void setDiskMotor(boolean on) {
		BaseDiskImage info = getSelectedDiskImage();
		if (info != null) {
			// strobe the motor (this doesn't turn it off, which happens via timeout)
			if (on) {
				if (!info.isMotorRunning()) {
					if (settingRealTime.getBoolean()) {
						Object[] args = { selectedDisk };
						dumper.info("DSK{0}: motor starting", args);
						info.setMotorTimeout(System.currentTimeMillis() + 1500);
						//fdc.status.set(StatusBit.BUSY);
					} else {
						Object[] args = { selectedDisk };
						dumper.info("DSK{0}: motor on", args);
						info.setMotorTimeout(System.currentTimeMillis());
						info.setMotorRunning(true);
						
					}
					updateDiskActivity(info.getName(), true);
				} else {
					info.setMotorTimeout(System.currentTimeMillis() + 4230);
				}
			}
		}
		
	}

	public boolean isMotorRunning() {
		BaseDiskImage info = getSelectedDiskImage();
		if (info != null)
			return info.isMotorRunning();
		else
			return false;
	}

	public boolean isPolledDisk(int newnum) {

		return selectedDisk == newnum;
	}

	public void setDiskHeads(boolean b) {
		fdc.setHeads(b);
	}

	public void setDiskHold(boolean b) {
		try {
			fdc.FDChold(b);
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}
		fdc.setHold(b);
		
	}
	public int getSide() {
		return side;
	}

	public boolean isDiskHeads() {
		return fdc.isHeads();
	}
	public boolean isDiskHold() {
		return fdc.isHold();
	}
	
	public byte readStatus() {
		byte ret = fdc.getStatus().calculate(fdc.getCommand());
		
		BaseDiskImage image = getSelectedDiskImage();
		boolean realTime = settingRealTime.getBoolean();
		if ((image != null && !image.isMotorRunning() && 
				(!realTime || image.getMotorTimeout() > 0))
				|| (realTime && fdc.commandBusyExpiration > System.currentTimeMillis()))
			ret |= StatusBit.BUSY.getVal();
		
		if (ret != lastStatus) {
			StringBuilder status = new StringBuilder();
			fdc.getStatus().toString(fdc.getCommand());
			dumper.info(("FDC read status >" + HexUtils.toHex2(ret) + " : " + status));
		}
		lastStatus = ret;

		return ret;
	}


	public void writeTrackAddr(byte val) {
		fdc.setTrackReg(val);
		//DSK.status &= ~fdc_LOSTDATA;
		dumper.info(("FDC write track addr " + val + " >" + HexUtils.toHex2(val)));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
		
	}



	/**
	 * @param val
	 */
	public void writeSectorAddr(byte val) {
		fdc.setSectorReg(val);
		//DSK.status &= ~fdc_LOSTDATA;
		dumper.info(("FDC write sector addr " + val + " >" + HexUtils.toHex2(val)));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
		
	}




	/**
	 * @return
	 */
	public byte readTrackAddr() {
		byte ret = fdc.getTrackReg();
		dumper.info(("FDC read track " + ret + " >" + HexUtils.toHex2(ret)));
		return ret;
	}




	/**
	 * @return
	 */
	public byte readSectorAddr() {
		byte ret = fdc.getSectorReg();
		dumper.info(("FDC read sector " + ret + " >" + HexUtils.toHex2(ret)));
		return ret;
	}


	/**
	 * @return
	 */
	public byte readData() {
		/* read from circular buffer */

		return fdc.readByte();
	}


	
	public String getName() {
		return "Disk Image DSR";
				
	}
	
	public void writeData(byte val) {
		if (!fdc.isHold())
			dumper.info(("FDC write data ("+fdc.getBufpos()+") >"+HexUtils.toHex2(val))); 
		//			   (u8) val);
		if (!fdc.isHold()) {
			fdc.setLastbyte(val);
		} else {
			fdc.getStatus().set(StatusBit.DRQ_PIN);;
			
			if (fdc.getCommand() == RealDiskConsts.FDC_writesector) {
				// normal write
				fdc.writeByte(val);
				

			} else if (fdc.getCommand() == RealDiskConsts.FDC_writetrack) {
				if (true /* is FM */) {
					// for FM write, >F5 through >FE are special
					if (val == (byte) 0xf5 || val == (byte) 0xf6) {
						fdc.getStatus().reset(StatusBit.REC_NOT_FOUND);;
					} else if (val == (byte) 0xf7) {
						// write CRC
						fdc.writeByte((byte) (fdc.getCrc() >> 8));
						fdc.writeByte((byte) (fdc.getCrc() & 0xff));
					} else if (val >= (byte) 0xf8 && val <= (byte) 0xfb) {
						fdc.setCrc((short) -1);
						fdc.writeByte(val);
					} else {
						fdc.writeByte(val);
					}
				} else {
					fdc.writeByte(val);
				}
			} else {
				dumper.info(("Unexpected data write >" + HexUtils.toHex2(val) + " for command >" + HexUtils.toHex2(fdc.getCommand())));
			}
		}
	}
	
	public void writeCommand(byte val)  {
		try {
			if (fdc.getStatus().is(StatusBit.BUSY) && (val & 0xf0) != 0xf0) {
				dumper.info(("FDC writing command >" + HexUtils.toHex2(val) + " while busy!"));
				//return;
			}
			
			fdc.FDCflush();
			fdc.setBuflen(fdc.setBufpos(0));
			
			dumper.info(("FDC command >" + HexUtils.toHex2(val)));
			//module_logger(&realDiskDSR, _L|L_1, _("FDC command >%02X\n"), val);
			
			fdc.setCommand(val & 0xF0);
			
			// standardize commands
			if (fdc.getCommand() == 0x30 || fdc.getCommand() == 0x50 || fdc.getCommand() == 0x70
					|| fdc.getCommand() == (byte)0x90 || fdc.getCommand() == (byte)0xA0)
				fdc.setCommand(fdc.getCommand() & (~0x10));
			
			fdc.setFlags((byte) (val & 0x1F));
		
			// stock execution time
			fdc.commandBusyExpiration = System.currentTimeMillis() + 1;
			fdc.getStatus().reset(StatusBit.BUSY);
			
			switch (fdc.getCommand()) {
			case RealDiskConsts.FDC_restore:
				fdc.FDCrestore();
				break;
			case RealDiskConsts.FDC_seek:
				fdc.FDCseek();
				break;
			case RealDiskConsts.FDC_step:
				fdc.FDCstep();
				break;
			case RealDiskConsts.FDC_stepin:
				fdc.setStepout(false);
				fdc.FDCstep();
				break;
			case RealDiskConsts.FDC_stepout:
				fdc.setStepout(true);
				fdc.FDCstep();
				break;
			case RealDiskConsts.FDC_readsector:
				fdc.FDCreadsector();
				break;
			case RealDiskConsts.FDC_writesector:
				fdc.FDCwritesector();
				break;
			case RealDiskConsts.FDC_readIDmarker:
				fdc.FDCreadIDmarker();
				break;
			case RealDiskConsts.FDC_interrupt:
				fdc.FDCinterrupt();
				break;
			case RealDiskConsts.FDC_writetrack:
				fdc.FDCwritetrack();
				break;
			case RealDiskConsts.FDC_readtrack:
				fdc.FDCreadtrack();
				break;
			default:
				//module_logger(&realDiskDSR, _L|L_1, _("unknown FDC command >%02X\n"), val);
				dumper.info(("Unknown FDC command >" + HexUtils.toHex2(val)));
			}
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}  catch(Throwable t) {
			dumper.error(t.getMessage());
		}
	}
	
	/**
	 * @param xfer  
	 */
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		dumper.info(("RealDiskDSR: ignoring code = " + code));
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		Map<String, Collection<IProperty>> map = new LinkedHashMap<String, Collection<IProperty>>();
		
		Collection<IProperty> settings = new ArrayList<IProperty>();
		settings.add(settingDsrEnabled);
		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = new ArrayList<IProperty>(diskSettingsMap.values());
		settings.add(settingRealTime);
		settings.add(settingDebug);
		map.put(IDsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(ISettingSection section) {
		settingDsrEnabled.saveState(section);
		fdc.saveState(section.addSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().saveState(section.addSection(entry.getKey()));
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		settingDsrEnabled.loadState(section);
		fdc.loadState(section.getSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}


	private Map<String, IProperty> diskMotorProperties = new HashMap<String, IProperty>();
	
	protected IProperty getDiskMotorProperty(String name) {
		IProperty prop = diskMotorProperties.get(name);
		if (prop == null) {
			settingDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
			prop = new SettingSchemaProperty(name + "Active", Boolean.FALSE);
			prop.addEnablementDependency(settingDsrEnabled);
			diskMotorProperties.put(name, prop);
		}
		return prop;
			
	}
	
	
	/**
	 * @param on 
	 * @param key
	 */
	protected void updateDiskActivity(String name, boolean on) {
		getDiskMotorProperty(name).setBoolean(on);
	}

	class DiskMotorIndicatorProvider implements IDeviceIndicatorProvider {

		private String name;

		public DiskMotorIndicatorProvider(String name) {
			this.name = name;
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
			return getDiskMotorProperty(name);
		}
	}
}
