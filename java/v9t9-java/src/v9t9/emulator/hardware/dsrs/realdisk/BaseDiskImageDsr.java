/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.Emulator;
import v9t9.emulator.clients.builtin.IconSetting;
import v9t9.emulator.common.Machine;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.DsrSettings;
import v9t9.emulator.hardware.dsrs.MemoryTransfer;
import v9t9.emulator.hardware.dsrs.emudisk.EmuDiskDsr;
import v9t9.emulator.runtime.cpu.Executor;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public abstract class BaseDiskImageDsr implements FDC1771Constants, DsrSettings {
	private static final String diskImageIconPath = Emulator.getDataFile("icons/disk_image.png").getAbsolutePath();


	public static final SettingProperty diskImageDebug = new SettingProperty("DiskImageDebug",
			"Debug Disk Image Support",
			"When set, log disk operation information to the console.",
			Boolean.FALSE);
	public static final SettingProperty diskImageRealTime = new SettingProperty("DiskImageRealTime",
			"Real-Time Disk Images",
			"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
			Boolean.TRUE);
	
	
	public static final SettingProperty diskImageDsrEnabled = new IconSetting("DiskImageDSREnabled",
			"Disk Image Support",
			"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
			"Either sector image or track image disks are supported.\n\n"+
			"A track image can support copy-protected disks, while a sector image cannot.",
			Boolean.FALSE, diskImageIconPath);
	
	/** setting name (DSKImage1) to setting */
	private Map<String, SettingProperty> diskSettingsMap = new LinkedHashMap<String, SettingProperty>();
	
	static void dumpBuffer(byte[] buffer, int offs, int len)
	{
		StringBuilder builder = new StringBuilder();
		int rowLength = 32;
		int x;
		if (len > 0)
			builder.append("Buffer contents:\n");
		for (x = offs; len-- > 0; x+=rowLength, len-=rowLength) {
			int         y;

			builder.append(HexUtils.toHex4(x));
			builder.append(' ');
			for (y = 0; y < rowLength; y++)
				builder.append(HexUtils.toHex2(buffer[x + y]) + " ");
			builder.append(' ');
			for (y = 0; y < rowLength; y++) {
				byte b = buffer[x+y];
				if (b >= 32 && b < 127)
					builder.append((char) b);
				else
					builder.append('.');
			}
			builder.append('\n');
		}
		info(builder.toString());

	}

	FDC1771 fdc = new FDC1771();
	
	/** currently selected disk */
	byte selectedDisk = 0;

	/** note: the side is global to all disks, though we propagate it to all DiskInfos */
	protected byte side;
	

	private static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}
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
		String name = getDiskImageSetting(num);
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


	static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}

	static class DiskImageSetting extends IconSetting {
		public DiskImageSetting(String name, Object storage, String iconPath) {
			super(name, 
					"DSK" + name.charAt(name.length() - 1) + " Image",
					"Specify the full path of the image for this disk.\n\n"+
					"The extension selects the image type when creating a new image.\n\nUse *.dsk for sector-image disks and *.trk for track image disks.",
					storage, iconPath);
			
			addEnablementDependency(EmuDiskDsr.emuDiskDsrEnabled);
			addEnablementDependency(BaseDiskImageDsr.diskImageDsrEnabled);
			addEnablementDependency(BaseDiskImageDsr.diskImageRealTime);
			addEnablementDependency(BaseDiskImageDsr.diskImageDebug);
		}

		/* (non-Javadoc)
		 * @see org.ejs.coffee.core.utils.SettingProperty#isAvailable()
		 */
		@Override
		public boolean isEnabled() {
			if (!BaseDiskImageDsr.diskImageDsrEnabled.getBoolean())
				return false;
			if (!EmuDiskDsr.emuDiskDsrEnabled.getBoolean())
				return true;
			
			// only DSK1 and DSK2 are real disks if emu disk also enabled
			return getName().compareTo(getDiskImageSetting(3)) < 0;
		}
	}

	public void registerDiskImagePath(String device, File dskfile) {
		DiskImageSetting diskSetting = new DiskImageSetting(device, dskfile.getAbsolutePath(),
				diskImageIconPath);
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

	private static File defaultDiskRootDir;

	
	public BaseDiskImageDsr(Machine machine) {
		//diskImageDsrEnabled.setBoolean(true);
		WorkspaceSettings.CURRENT.register(diskImageDsrEnabled);
		WorkspaceSettings.CURRENT.register(diskImageRealTime);
		WorkspaceSettings.CURRENT.register(diskImageDebug);
		
    	String diskImageRootPath = WorkspaceSettings.CURRENT.getConfigDirectory() + "disks";
    	defaultDiskRootDir = new File(diskImageRootPath);
    	defaultDiskRootDir.mkdirs();
    	
    	for (int drive = 1; drive <= 3; drive++) {
    		String name = getDiskImageSetting(drive);
			registerDiskImagePath(name, getDefaultDiskImage(name)); 
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
								info("{0}: motor on", name);
								
								fdc.status.reset(StatusBit.BUSY);
								info.motorTimeout = System.currentTimeMillis() + 4320;
							}
						} else {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = false;
								//fdc.status.set(StatusBit.BUSY);
								info("{0}: motor off", name);
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
			error(e.getMessage());
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
						error(e.getMessage());
					}
					
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
						error(e.getMessage());
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
						error(e.getMessage());
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
					if (diskImageRealTime.getBoolean()) {
						info("DSK{0}: motor starting", selectedDisk);
						info.motorTimeout = System.currentTimeMillis() + 1500;
						//fdc.status.set(StatusBit.BUSY);
					} else {
						info("DSK{0}: motor on", selectedDisk);
						info.motorTimeout = System.currentTimeMillis();
						info.motorRunning = true;
						
					}
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
			error(e.getMessage());
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
		if ((image != null && !image.motorRunning && (!diskImageRealTime.getBoolean() || image.motorTimeout > 0))
				|| (diskImageRealTime.getBoolean() && fdc.commandBusyExpiration > System.currentTimeMillis()))
			ret |= 0x1;
		
		if (ret != lastStatus) {
			info("FDC read status >" + HexUtils.toHex2(ret) + " : " + status);
		}
		lastStatus = ret;

		return ret;
	}


	public void writeTrackAddr(byte val) {
		fdc.trackReg = val;
		//DSK.status &= ~fdc_LOSTDATA;
		info("FDC write track addr " + val + " >" + HexUtils.toHex2(val));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
		
	}



	/**
	 * @param val
	 */
	public void writeSectorAddr(byte val) {
		fdc.sectorReg = val;
		//DSK.status &= ~fdc_LOSTDATA;
		info("FDC write sector addr " + val + " >" + HexUtils.toHex2(val));
		//module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
		
	}




	/**
	 * @return
	 */
	public byte readTrackAddr() {
		byte ret = fdc.trackReg;
		info("FDC read track " + ret + " >" + HexUtils.toHex2(ret));
		return ret;
	}




	/**
	 * @return
	 */
	public byte readSectorAddr() {
		byte ret = fdc.sectorReg;
		info("FDC read sector " + ret + " >" + HexUtils.toHex2(ret));
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
	
	/* calculate CRC for data address marks or sector data */
	/* borrowed from xmess-0.56.2.  seems like this only works for MFM */
	static short calc_crc(int crc, int value) {
		int l, h;

		l = value ^ ((crc >> 8) & 0xff);
		crc = (crc & 0xff) | (l << 8);
		l >>= 4;
		l ^= (crc >> 8) & 0xff;
		crc <<= 8;
		crc = (crc & 0xff00) | l;
		l = (l << 4) | (l >> 4);
		h = l;
		l = (l << 2) | (l >> 6);
		l &= 0x1f;
		crc = crc ^ (l << 8);
		l = h & 0xf0;
		crc = crc ^ (l << 8);
		l = (h << 1) | (h >> 7);
		l &= 0xe0;
		crc = crc ^ l;
		return (short) crc;
	}
	
	public void writeData(byte val) {
		if (!fdc.hold)
			info("FDC write data ("+fdc.bufpos+") >"+HexUtils.toHex2(val)); 
		//			   (u8) val);
		if (!fdc.hold) {
			fdc.lastbyte = val;
		} else {
			fdc.status.set(StatusBit.DRQ_PIN);;
			
			if (fdc.command == FDC_writesector) {
				// normal write
				fdc.writeByte(val);
				

			} else if (fdc.command == FDC_writetrack) {
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
				info("Unexpected data write >" + HexUtils.toHex2(val) + " for command >" + HexUtils.toHex2(fdc.command));
			}
		}
	}
	
	public void writeCommand(byte val)  {
		try {
			if (fdc.status.is(StatusBit.BUSY) && (val & 0xf0) != 0xf0) {
				info("FDC writing command >" + HexUtils.toHex2(val) + " while busy!");
				//return;
			}
			
			fdc.FDCflush();
			fdc.buflen = fdc.bufpos = 0;
			
			info("FDC command >" + HexUtils.toHex2(val));
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
			case FDC_restore:
				fdc.FDCrestore();
				break;
			case FDC_seek:
				fdc.FDCseek();
				break;
			case FDC_step:
				fdc.FDCstep();
				break;
			case FDC_stepin:
				fdc.stepout = false;
				fdc.FDCstep();
				break;
			case FDC_stepout:
				fdc.stepout = true;
				fdc.FDCstep();
				break;
			case FDC_readsector:
				fdc.FDCreadsector();
				break;
			case FDC_writesector:
				fdc.FDCwritesector();
				break;
			case FDC_readIDmarker:
				fdc.FDCreadIDmarker();
				break;
			case FDC_interrupt:
				fdc.FDCinterrupt();
				break;
			case FDC_writetrack:
				fdc.FDCwritetrack();
				break;
			case FDC_readtrack:
				fdc.FDCreadtrack();
				break;
			default:
				//module_logger(&realDiskDSR, _L|L_1, _("unknown FDC command >%02X\n"), val);
				info("Unknown FDC command >" + HexUtils.toHex2(val));
			}
		} catch (IOException e) {
			error(e.getMessage());
		}  catch(Throwable t) {
			error(t.getMessage());
		}
	}
	
	/**
	 * @param xfer  
	 */
	public boolean handleDSR(MemoryTransfer xfer, short code) {
		info("RealDiskDSR: ignoring code = " + code);
		return false;
	}

	/**
	 * @param string
	 */
	static void info(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		if (diskImageDebug.getBoolean())
			System.out.println(string);
		
	}
	static void info(String fmt, Object... args) {
		info(MessageFormat.format(fmt, args));
		
	}
	static void error(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		System.err.println(string);
		
	}
	static void error(String fmt, Object... args) {
		error(MessageFormat.format(fmt, args));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	public Map<String, Collection<SettingProperty>> getEditableSettingGroups() {
		Map<String, Collection<SettingProperty>> map = new LinkedHashMap<String, Collection<SettingProperty>>();
		
		Collection<SettingProperty> settings = new ArrayList<SettingProperty>();
		settings.add(diskImageDsrEnabled);
		map.put(DsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = new ArrayList<SettingProperty>(diskSettingsMap.values());
		settings.add(diskImageRealTime);
		settings.add(diskImageDebug);
		map.put(DsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(ISettingSection section) {
		diskImageDsrEnabled.saveState(section);
		fdc.saveState(section.addSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().saveState(section.addSection(entry.getKey()));
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		diskImageDsrEnabled.loadState(section);
		fdc.loadState(section.getSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}
}
