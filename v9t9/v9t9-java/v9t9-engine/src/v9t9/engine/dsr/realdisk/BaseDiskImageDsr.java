/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;


import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.ISettingSection;
import v9t9.base.utils.HexUtils;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDsrSettings;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.machine.IMachine;
import v9t9.engine.dsr.IDevIcons;
import v9t9.engine.settings.WorkspaceSettings;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public abstract class BaseDiskImageDsr implements IDsrSettings {
	/** setting name (DSKImage1) to setting */
	protected Map<String, SettingProperty> diskSettingsMap = new LinkedHashMap<String, SettingProperty>();
	
	FDC1771 fdc = new FDC1771();
	
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
			SettingProperty setting = diskSettingsMap.get(name);
			if (setting == null)
				return null;
			info = DiskImageFactory.createDiskImage(name, new File(setting.getString()));
			disks.put(name, info);
		}
		return disks.get(name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	public void dispose() {
	}


	public void registerDiskImagePath(String device, File dskfile) {
		DiskImageSetting diskSetting = new DiskImageSetting(device, dskfile.getAbsolutePath(),
				RealDiskDsrSettings.diskImageIconPath);
		WorkspaceSettings.CURRENT.register(diskSetting);
	
		diskSettingsMap.put(device, diskSetting); 
		diskSetting.addListener(new IPropertyListener() {
			
			public void propertyChanged(IProperty setting) {
	
				BaseDiskImage image = disks.get(setting.getName());
				if (image != null) {
					if (image == fdc.getImage())
						fdc.setImage(null);
					
				}
				image = DiskImageFactory.createDiskImage(setting.getName(), new File(setting.getString()));
				disks.put(setting.getName(), image);
						
				//setting.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
	}

	
	private TimerTask motorTickTask;

	private byte lastStatus;


	protected SettingProperty realDiskDsrActiveSetting;

	public BaseDiskImageDsr(IMachine machine) {
		//diskImageDsrEnabled.setBoolean(true);
		WorkspaceSettings.CURRENT.register(RealDiskDsrSettings.diskImageDsrEnabled);
		WorkspaceSettings.CURRENT.register(RealDiskDsrSettings.diskImageRealTime);
		WorkspaceSettings.CURRENT.register(RealDiskDsrSettings.diskImageDebug);
		
    	String diskImageRootPath = WorkspaceSettings.CURRENT.getConfigDirectory() + "disks";
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
					if (info.motorTimeout != 0) {
						if (!info.motorRunning) {
							if (now >= info.motorTimeout) {
								//info.motorTimeout = 0;
								info.motorRunning = true;
								BaseDiskImage.info("{0}: motor on", name);
								
								fdc.status.reset(StatusBit.BUSY);
								info.motorTimeout = System.currentTimeMillis() + 4320;
							}
						} else {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = false;
								//fdc.status.set(StatusBit.BUSY);
								BaseDiskImage.info("{0}: motor off", name);
								info.getInUseSetting().setBoolean(false);
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
			BaseDiskImage.error(e.getMessage());
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
						BaseDiskImage.error(e.getMessage());
					}
					
					oldInfo.getInUseSetting().setBoolean(false);
					
					// just in case the image went missing
					if (info != null)
						info.validateDiskImage();
				}
			}
			if (info != null) {
				if (info.handle == null) {
					try {
						info.openDiskImage();
					} catch (IOException e) {
						BaseDiskImage.error(e.getMessage());
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
						BaseDiskImage.error(e.getMessage());
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
				if (!info.motorRunning) {
					if (RealDiskDsrSettings.diskImageRealTime.getBoolean()) {
						BaseDiskImage.info("DSK{0}: motor starting", selectedDisk);
						info.motorTimeout = System.currentTimeMillis() + 1500;
						//fdc.status.set(StatusBit.BUSY);
					} else {
						BaseDiskImage.info("DSK{0}: motor on", selectedDisk);
						info.motorTimeout = System.currentTimeMillis();
						info.motorRunning = true;
						
					}
					info.getInUseSetting().setBoolean(true);
				} else {
					info.motorTimeout = System.currentTimeMillis() + 4230;
				}
			}
		}
		
	}

	public boolean isMotorRunning() {
		BaseDiskImage info = getSelectedDiskImage();
		if (info != null)
			return info.motorRunning;
		else
			return false;
	}

	public boolean isPolledDisk(int newnum) {

		return selectedDisk == newnum;
	}

	public void setDiskHeads(boolean b) {
		fdc.heads = b;
	}

	public void setDiskHold(boolean b) {
		try {
			fdc.FDChold(b);
		} catch (IOException e) {
			BaseDiskImage.error(e.getMessage());
		}
		fdc.hold = b;
		
	}
	public int getSide() {
		return side;
	}

	public boolean isDiskHeads() {
		return fdc.heads;
	}
	public boolean isDiskHold() {
		return fdc.hold;
	}
	
	public byte readStatus() {
		StringBuilder status = new StringBuilder();
		byte ret = fdc.status.calculate(fdc.command, status);
		
		BaseDiskImage image = getSelectedDiskImage();
		if ((image != null && !image.motorRunning && (!RealDiskDsrSettings.diskImageRealTime.getBoolean() || image.motorTimeout > 0))
				|| (RealDiskDsrSettings.diskImageRealTime.getBoolean() && fdc.commandBusyExpiration > System.currentTimeMillis()))
			ret |= 0x1;
		
		if (ret != lastStatus) {
			BaseDiskImage.info("FDC read status >" + HexUtils.toHex2(ret) + " : " + status);
		}
		lastStatus = ret;

		return ret;
	}


	public void writeTrackAddr(byte val) {
		fdc.trackReg = val;
		//DSK.status &= ~fdc_LOSTDATA;
		BaseDiskImage.info("FDC write track addr " + val + " >" + HexUtils.toHex2(val));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
		
	}



	/**
	 * @param val
	 */
	public void writeSectorAddr(byte val) {
		fdc.sectorReg = val;
		//DSK.status &= ~fdc_LOSTDATA;
		BaseDiskImage.info("FDC write sector addr " + val + " >" + HexUtils.toHex2(val));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
		
	}




	/**
	 * @return
	 */
	public byte readTrackAddr() {
		byte ret = fdc.trackReg;
		BaseDiskImage.info("FDC read track " + ret + " >" + HexUtils.toHex2(ret));
		return ret;
	}




	/**
	 * @return
	 */
	public byte readSectorAddr() {
		byte ret = fdc.sectorReg;
		BaseDiskImage.info("FDC read sector " + ret + " >" + HexUtils.toHex2(ret));
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
		if (!fdc.hold)
			BaseDiskImage.info("FDC write data ("+fdc.bufpos+") >"+HexUtils.toHex2(val)); 
		//			   (u8) val);
		if (!fdc.hold) {
			fdc.lastbyte = val;
		} else {
			fdc.status.set(StatusBit.DRQ_PIN);;
			
			if (fdc.command == RealDiskConsts.FDC_writesector) {
				// normal write
				fdc.writeByte(val);
				

			} else if (fdc.command == RealDiskConsts.FDC_writetrack) {
				if (true /* is FM */) {
					// for FM write, >F5 through >FE are special
					if (val == (byte) 0xf5 || val == (byte) 0xf6) {
						fdc.status.reset(StatusBit.REC_NOT_FOUND);;
					} else if (val == (byte) 0xf7) {
						// write CRC
						fdc.writeByte((byte) (fdc.crc >> 8));
						fdc.writeByte((byte) (fdc.crc & 0xff));
					} else if (val >= (byte) 0xf8 && val <= (byte) 0xfb) {
						fdc.crc = -1;
						fdc.writeByte(val);
					} else {
						fdc.writeByte(val);
					}
				} else {
					fdc.writeByte(val);
				}
			} else {
				BaseDiskImage.info("Unexpected data write >" + HexUtils.toHex2(val) + " for command >" + HexUtils.toHex2(fdc.command));
			}
		}
	}
	
	public void writeCommand(byte val)  {
		try {
			if (fdc.status.is(StatusBit.BUSY) && (val & 0xf0) != 0xf0) {
				BaseDiskImage.info("FDC writing command >" + HexUtils.toHex2(val) + " while busy!");
				//return;
			}
			
			fdc.FDCflush();
			fdc.buflen = fdc.bufpos = 0;
			
			BaseDiskImage.info("FDC command >" + HexUtils.toHex2(val));
			//module_logger(&realDiskDSR, _L|L_1, _("FDC command >%02X\n"), val);
			
			fdc.command = val & 0xF0;
			
			// standardize commands
			if (fdc.command == 0x30 || fdc.command == 0x50 || fdc.command == 0x70
					|| fdc.command == (byte)0x90 || fdc.command == (byte)0xA0)
				fdc.command &= ~0x10;
			
			fdc.flags = (byte) (val & 0x1F);
		
			// stock execution time
			fdc.commandBusyExpiration = System.currentTimeMillis() + 1;
			fdc.status.reset(StatusBit.BUSY);
			
			switch (fdc.command) {
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
				fdc.stepout = false;
				fdc.FDCstep();
				break;
			case RealDiskConsts.FDC_stepout:
				fdc.stepout = true;
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
				BaseDiskImage.info("Unknown FDC command >" + HexUtils.toHex2(val));
			}
		} catch (IOException e) {
			BaseDiskImage.error(e.getMessage());
		}  catch(Throwable t) {
			BaseDiskImage.error(t.getMessage());
		}
	}
	
	/**
	 * @param xfer  
	 */
	public boolean handleDSR(IMemoryTransfer xfer, short code) {
		BaseDiskImage.info("RealDiskDSR: ignoring code = " + code);
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	public Map<String, Collection<SettingProperty>> getEditableSettingGroups() {
		Map<String, Collection<SettingProperty>> map = new LinkedHashMap<String, Collection<SettingProperty>>();
		
		Collection<SettingProperty> settings = new ArrayList<SettingProperty>();
		settings.add(RealDiskDsrSettings.diskImageDsrEnabled);
		map.put(IDsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = new ArrayList<SettingProperty>(diskSettingsMap.values());
		settings.add(RealDiskDsrSettings.diskImageRealTime);
		settings.add(RealDiskDsrSettings.diskImageDebug);
		map.put(IDsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(ISettingSection section) {
		RealDiskDsrSettings.diskImageDsrEnabled.saveState(section);
		fdc.saveState(section.addSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().saveState(section.addSection(entry.getKey()));
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		RealDiskDsrSettings.diskImageDsrEnabled.loadState(section);
		fdc.loadState(section.getSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}
	

	static class DiskImageDeviceIndicatorProvider implements IDeviceIndicatorProvider {

		private final BaseDiskImage image;

		public DiskImageDeviceIndicatorProvider(BaseDiskImage image) {
			this.image = image;
		}

		@Override
		public String getToolTip() {
			return image.name + " activity";
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
		public SettingProperty getActiveProperty() {
			return image.getInUseSetting();
		}
	}

}
