/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.IconSetting;
import v9t9.emulator.hardware.CruManager;
import v9t9.emulator.hardware.CruReader;
import v9t9.emulator.hardware.CruWriter;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.MemoryTransfer;
import v9t9.emulator.hardware.dsrs.emudisk.EmuDiskDsr;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public class DiskImageDsr implements DsrHandler {
	/**
	 * 
	 */
	private static final String diskImageIconPath = V9t9.getDataFile("icons/disk_image.png").getAbsolutePath();

	public static final Setting diskImageDsrEnabled = new IconSetting("DiskImageDSREnabled",
			"Disk Image Support",
			"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
			"Either sector image or track image disks are supported.\n\n"+
			"A track image can support copy-protected disks, while a sector image cannot.",
			Boolean.FALSE, diskImageIconPath);
	
	/** setting name (DSKImage1) to setting */
	private Map<String, Setting> diskSettingsMap = new LinkedHashMap<String, Setting>();
	private DiskMemoryEntry romMemoryEntry;
	
	static final int DSKtracksize_SD = (3210);
	//private static final int DSKtracksize_DD = (6420);

	static final int DSKbuffersize = (16384);		/* maximum track size */

	static class DSKheader
	{
		byte			tracks;		/* tracks per side */
		byte			sides;		/* 1 or 2 */
		byte			unused;
		short			tracksize;	/* bytes per track */
		int			track0offs;	/* offset for track 0 data */
		public long getImageSize() {
			return (tracksize & 0xffff) * (tracks & 0xff) * sides;
		}
		public int getTrackSize(int num) {
			return num * (tracksize & 0xffff);
		}
	};

	private static final int 
		R_RDSTAT = 0,
		R_RTADDR = 1,
		R_RSADDR = 2,
		R_RDDATA = 3,
		W_WTCMD = 4,
		W_WTADDR = 5,
		W_WSADDR = 6,
		W_WTDATA = 7
	;

	static final int 
		FDC_restore			= 0x00,
		FDC_seek			= 0x10,
			fl_head_load	= 0x08,
			fl_verify_track	= 0x04,	/* match track register with sector ID */
			fl_step_rate	= 0x03,
		
		FDC_step			= 0x20,
		FDC_stepin			= 0x40,
		FDC_stepout			= 0x60,
			fl_update_track	= 0x10, /* for all step commands */	

			// +fl_head_load, fl_verify_track, fl_step_rate

		FDC_readsector		= 0x80,
		FDC_writesector		= 0xA0,
			fl_multiple		= 0x10,
			fl_length_coding= 0x08,	// sector length coding in FDC1771 
			fl_side_number	= 0x08,	/* which side to match */	// FDC179x
			fl_side_compare	= 0x02,
			fl_deleted_dam	= 0x01,

		FDC_readIDmarker= 0xC0,
		FDC_readtrack	= 0xE0,
		FDC_writetrack	= 0xF0,

			fl_15ms_delay	= 0x04,	/* common to readsector...writetrack */

		FDC_interrupt	= 0xD0
	;

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

	static class IdMarker {
		public int idoffset;
		public int dataoffset;
		
		public byte trackid;
		public byte sectorid;
		public byte sideid;
		public byte sizeid;
		public short crcid;
	}

	public interface IDiskImage {
		void readImageHeader() throws IOException;
		void createDiskImage(boolean fulltrk) throws IOException;
		void openDiskImage() throws IOException;
		void closeDiskImage() throws IOException;
		void writeImageHeader() throws IOException;
		void readCurrentTrackData() throws IOException;
		long getTrackDiskOffset();
		boolean seekToCurrentTrack();
		void flushTrack() throws IOException;
		void formatTrack(byte[] buffer, int start, int length);
		int getTrackBufferSize();
		void growImageForContent() throws IOException;
	}
	
	static class FDC1771 {

		
		boolean hold; /* holding for data? */
		byte lastbyte; /* last byte written to WDDATA when hold off */
		byte rwBuffer[] = new byte[DSKbuffersize]; /* read/write contents */
		int buflen; /* max length of data expected for a write/read */
		int bufpos; /* offset into buffer */

		int command; /* command being executed (0xE0 mask) */
		byte flags; /* flags sent with command being executed (0x1F mask) */

		/* command-specified */
		byte trackReg; /* desired track */
		byte sideReg; /* current side */
		byte sectorReg; /* desired sector */
		short crc; /* current CRC */

		boolean stepout; /* false: in, true: out */
		
		byte seektrack; /* physically seeked track */
		byte seekside; /* the side we seeked to in the image */
		
		protected FDCStatus status = new FDCStatus();
		
		private BaseDiskImage image;
		private List<IdMarker> trackMarkers;
		private Iterator<IdMarker> trackMarkerIter;
		private IdMarker currentMarker;
		
		/**
		 * 
		 */
		public FDC1771() {
		}
		
		/**
		 * @param image the image to set
		 */
		public void setImage(BaseDiskImage image) {
			if (this.image != image) {
				this.image = image;

				// refetch markers (and track) next time
				trackMarkerIter = null;
				trackMarkers = null;
				currentMarker = null;
				
				seektrack = -1;
				seekside = -1;
			}
		}

		public void FDChold(boolean onoff) throws IOException {
			if (onoff) {
				// info("FDChold on");
				/* about to read or write */
				status.set(StatusBit.DRQ_PIN);
			} else {
				// info("FDChold off");
				if (hold && (command == FDC_writesector || command == FDC_writetrack)) {
					FDCflush();
				}
			}

		}
		

		/**
		 * 
		 */
		public void FDCflush() throws IOException {
			if (!hold) return;

			if (image == null) {
				status.set(StatusBit.NOT_READY);
				return;
			}
			
			if (buflen != 0) {
				//status &= ~fdc_LOSTDATA;

				status.reset(StatusBit.WRITE_PROTECT);
				status.reset(StatusBit.LOST_DATA);
				status.reset(StatusBit.CRC_ERROR);

				if (command == FDC_writesector)
					image.writeSectorData(rwBuffer, 0, buflen, currentMarker, status);
				else if (command == FDC_writetrack)
					image.writeTrackData(rwBuffer, 0, buflen, status);
				
				image.commitTrack(status);
			
				//readCurrentTrackData();
			}

		}

		private void ensureTrackMarkers() {
			if (trackMarkers == null) {
				if (image == null) {
					trackMarkers = new ArrayList<IdMarker>();
				} else {
					trackMarkers = image.getTrackMarkers();
				}
				trackMarkerIter = trackMarkers.iterator();
			}
		}


		/*	Find a sector ID on the track */
		private boolean
		FDCfindIDmarker()
		{
			ensureTrackMarkers();
			int iters = trackMarkers.size();
			while (iters-- > 0) {
				if (!trackMarkerIter.hasNext()) {
					trackMarkerIter = trackMarkers.iterator();
				}
				if (trackMarkerIter.hasNext()) {
					currentMarker = trackMarkerIter.next();
					if (command != FDC_readIDmarker || currentMarker.sideid == sideReg)
						return true;
				}
			}
			
			currentMarker = null;
			return false;
			
		}

		/*	Match the current ID with the desired track/sector id */
		private boolean
		FDCmatchIDmarker() {
			info("FDC match ID marker: looking for T{0}, S{1}", trackReg, sectorReg);
			
			status.reset(StatusBit.REC_NOT_FOUND);
			status.reset(StatusBit.CRC_ERROR);
		
			// FDC179x mode
			//byte desiredSide = (byte) ((flags & fl_side_number) != 0 ? 1 : 0);
		
			ensureTrackMarkers();
			
			int tries = trackMarkers.size();
			boolean found = false;
			while (!found && tries-- > 0) {
				if (!FDCfindIDmarker())
					break;
				
				if (currentMarker.trackid == trackReg
					//&& sideid == desiredSide  // for FDC179x
					&& currentMarker.sectorid == sectorReg
					//&& crcid == crc
					)
				{
					found = true;
				}
			}
	
			if (!found) {
				error("FDCmatchIDmarker failed");
				status.set(StatusBit.REC_NOT_FOUND);
				return false;
			}
			
			info("FDCmatchIDmarker succeeded: track {0}, sector {1}, side {2}, size {3} (sector #{4})",
					currentMarker.trackid, currentMarker.sectorid, 
					currentMarker.sideid, currentMarker.sizeid, 
					currentMarker.trackid * 9 + currentMarker.sectorid);
			return true;
		}

		private void updateSeek(byte track, byte side) throws IOException {
			if (seektrack != track || seekside != side) {
				// don't change anything until the track changes, 
				// so we can fetch the hidden sectors with the same id as normal ones
				seektrack = track;
				seekside = side;
				
				currentMarker = null;
				trackMarkers = null;
				trackMarkerIter = null;
				
				if (image != null)
					image.seekToCurrentTrack(seektrack, seekside);
			}
		}

		public void FDCrestore() throws IOException {
			info("FDC restore");
			
			trackReg = 0;
			updateSeek(trackReg, sideReg);
			
			status.set(StatusBit.TRACK_0);
			status.reset(StatusBit.REC_NOT_FOUND);
			status.reset(StatusBit.CRC_ERROR);
			status.reset(StatusBit.SEEK_ERROR);
			
			if ((flags & fl_verify_track) != 0) {
				verifyTrack(trackReg);
			}			
		}

		public void FDCseek() throws IOException {
			info("FDC seek, T{0} s{1}", lastbyte, sideReg);
			
			trackReg = lastbyte;
			updateSeek(trackReg, sideReg);

			status.reset(StatusBit.SEEK_ERROR);
			status.reset(StatusBit.TRACK_0);
			if (trackReg == 0)
				status.set(StatusBit.TRACK_0);

			if ((flags & fl_verify_track) != 0) {
				verifyTrack(trackReg);
			}
			
		}

		/**
		 * 
		 */
		private void verifyTrack(byte track) {
			ensureTrackMarkers();
			
			status.reset(StatusBit.SEEK_ERROR);
			boolean found = false;
			for (int tries = 0; tries < trackMarkers.size(); tries++) {
				if (!FDCfindIDmarker())
					break;
				if (currentMarker.trackid == track) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				status.set(StatusBit.SEEK_ERROR);
			   	error("FDC seek, could not find marker for track {0}", track);
			}

		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCstep() throws IOException {

			byte newtrack = (byte) (seektrack + (stepout ? -1 : 1));
			if ((flags & fl_update_track) != 0)
				trackReg = newtrack;
			
			info("FDC step {2}, T{0} s{1}", newtrack, sideReg,
					stepout ? "out" : "in");
			
			status.reset(StatusBit.TRACK_0);
			if (newtrack == 0)
				status.set(StatusBit.TRACK_0);
			
			updateSeek(newtrack, sideReg);
			
			if ((flags & fl_verify_track) != 0) {
				verifyTrack(newtrack);
			}
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCreadsector() throws IOException {
			info("FDC read sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);
			
			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.DRQ_PIN);
			
			if (image == null) {
				status.reset(StatusBit.REC_NOT_FOUND);
				return;
			}

			if (!FDCmatchIDmarker())
				return;
			
			buflen = 128 << currentMarker.sizeid;
			bufpos = 0;
			
			image.readSectorData(currentMarker, rwBuffer, 0, buflen);
			
			
			status.set(StatusBit.DRQ_PIN);
		}


		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCwritesector() throws IOException {
			info("FDC write sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.DRQ_PIN);
			
			if (image == null) {
				status.reset(StatusBit.REC_NOT_FOUND);
				return;
			}

			if (!FDCmatchIDmarker()) 
				return;
			
			// not sure this is true
			// http://nouspikel.group.shef.ac.uk//ti99/disks.htm#Sector%20size%20code
			if (true || (flags & fl_length_coding) == 0)
				buflen = 128 << currentMarker.sizeid;
			else
				buflen = currentMarker.sizeid != 0 ? (currentMarker.sizeid & 0xff) * 16 : 4096;
				
			bufpos = 0;
			
			status.set(StatusBit.DRQ_PIN);
			
			if (image.readonly)
				status.set(StatusBit.WRITE_PROTECT);
		}

		/**
		 * 
		 */
		public void FDCreadIDmarker() {
			/* since we can't tell what's a valid ID field,
			   always go to the beginning of the track to search. */

			status.reset(StatusBit.LOST_DATA);
			
			if (!FDCfindIDmarker()) {
				status.set(StatusBit.REC_NOT_FOUND);
				return;
			}

			int ptr = 0;

			rwBuffer[ptr++] = currentMarker.trackid;
			rwBuffer[ptr++] = currentMarker.sideid;
			rwBuffer[ptr++] = currentMarker.sectorid;
			rwBuffer[ptr++] = currentMarker.sizeid;
			rwBuffer[ptr++] = (byte) (currentMarker.crcid >> 8);
			rwBuffer[ptr++] = (byte) (currentMarker.crcid & 0xff);

			buflen = 6;
			bufpos = 0;

			// the detected track is copied into the sector register (!)
			sectorReg = currentMarker.trackid;
			
			info("FDC read ID marker: track={0} sector={1} side={2} size={3}",
					currentMarker.trackid, currentMarker.sectorid, currentMarker.sideid, currentMarker.sizeid);

			
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCinterrupt() throws IOException {
			info("FDC interrupt");
			
			status.clear();
			
			FDCflush();

			buflen = bufpos = 0;			
		}

		public void FDCwritetrack() {
			info("FDC write track, #{0}", seektrack);

			status.reset(StatusBit.LOST_DATA);
			
			buflen = DSKbuffersize;  
				
			bufpos = 0;
			
			if (image.readonly)
				status.set(StatusBit.WRITE_PROTECT);
		}

		public void FDCreadtrack() throws IOException {
			info("FDC read track, #{0}", seektrack);

			status.reset(StatusBit.LOST_DATA);
			
			bufpos = 0;
			if (image != null) {
				buflen = image.getTrackSize();
				image.readTrackData(rwBuffer, 0, buflen);
			} else {
				buflen = 0;
			}
			
		}

		public void saveState(IDialogSettings section) {
		}

		public void loadState(IDialogSettings section) {
			if (image != null) {
				try {
					image.closeDiskImage();
				} catch (IOException e) {
					error(e.getMessage());
				}
			}
			image = null;
		}

		/**
		 * @return
		 */
		public byte readByte() {
			byte ret = 0;

			if (hold && buflen != 0) {
				ret = rwBuffer[bufpos++];
				crc = calc_crc(crc, ret & 0xff);
				if (bufpos >= buflen) {
					status.reset(StatusBit.DRQ_PIN);
				}
			} else {
				ret = lastbyte;
			}
			return ret;
		}

		/**
		 * @param val
		 */
		public void writeByte(byte val) {
			if (buflen != 0) {
				/* fill circular buffer */
				if (bufpos < buflen) {
					rwBuffer[bufpos++] = val;
					crc = calc_crc(crc, val);
				} else {
					status.reset(StatusBit.DRQ_PIN);
					error("Tossing extra byte >{0}", Integer.toHexString(val & 0xff));
				}
			}	
		}

		/**
		 * @param side
		 * @throws IOException 
		 */
		public void setSide(byte side) throws IOException {
			info("Select side {0}", side);
			updateSeek(seektrack, side);
			sideReg = side;
		}

		/**
		 * @return
		 */
		public BaseDiskImage getImage() {
			return image;
		}

	}
	
	private FDC1771 fdc = new FDC1771();
	
	/** currently selected disk */
	private byte selectedDisk = 0;

	/** note: the side is global to all disks, though we propagate it to all DiskInfos */
	protected byte side;
	

	private static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}
	private Map<String, BaseDiskImage> disks = new LinkedHashMap<String, BaseDiskImage>();
	
	private BaseDiskImage getSelectedDisk() {
		if (selectedDisk == 0)
			return null;
		return getDiskInfo(selectedDisk);
	}
	
	
	private BaseDiskImage getDiskInfo(int num) {
		String name = getDiskImageSetting(num);
		BaseDiskImage info = disks.get(name);
		if (info == null) {
			Setting setting = diskSettingsMap.get(name);
			if (setting == null)
				return null;
			info = createDiskImage(name, new File(setting.getString()));
			disks.put(name, info);
		}
		return disks.get(name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	public void dispose() {
		motorTimer.cancel();		
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
			addEnablementDependency(DiskImageDsr.diskImageDsrEnabled);
		}

		/* (non-Javadoc)
		 * @see org.ejs.coffee.core.utils.Setting#isAvailable()
		 */
		@Override
		public boolean isEnabled() {
			if (!DiskImageDsr.diskImageDsrEnabled.getBoolean())
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
		EmulatorSettings.INSTANCE.register(diskSetting);
	
		diskSettingsMap.put(device, diskSetting); 
		diskSetting.addListener(new ISettingListener() {
			
			public void changed(Setting setting, Object oldValue) {
	
				BaseDiskImage image = disks.get(setting.getName());
				if (image != null) {
					if (image == fdc.getImage())
						fdc.setImage(null);
					
				}
				image = createDiskImage(setting.getName(), new File(setting.getString()));
				disks.put(setting.getName(), image);
						
				//setting.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
	}
	
	/**
	 * @param name
	 * @param file
	 * @return
	 */
	protected BaseDiskImage createDiskImage(String name, File file) {
		if (file.exists()) {
			if (TrackDiskImage.isTrackImage(file))
				return new TrackDiskImage(name, file);
		}
		if (file.getName().toLowerCase().endsWith(".trk"))
			return new TrackDiskImage(name, file);
		else
			return new SectorDiskImage(name, file);
	}
	
	private CruWriter cruwRealDiskMotor = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Motor %s\n"), data ? "on" : "off");
			BaseDiskImage info = getSelectedDisk();
			if (info != null) {
				// strobe the motor (this doesn't turn it off, which happens via timeout)
				if (data != 0) {
					if (!info.motorRunning) {
						info("DSK{0}: motor on", selectedDisk);
						info.motorTimeout = System.currentTimeMillis() /*+ 1000*/;
						info.motorRunning = true;
					} else {
						info.motorTimeout = System.currentTimeMillis() + 4230;
					}
				}
			}
			return 0;
		}
	};

	private MemoryEntry ioMemoryEntry;
	private DiskMMIOMemoryArea ioArea;	
	
	private CruWriter cruwRealDiskHold = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU hold %s\n"), data ? "on" : "off");
		
			try {
				fdc.FDChold(data != 0);
			} catch (IOException e) {
				error(e.getMessage());
			}
			fdc.hold = data != 0;
			
			return 0;
		}
	};

	private CruWriter cruwRealDiskHeads = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Heads %s\n"), data ? "on" : "off");
	
			// TODO
			return 0;
		}
	};
	
	private CruWriter cruwRealDiskSel = new CruWriter() {
		public int write(int addr, int data, int num) {
			byte newnum = (byte) (((addr - 0x1108) >> 1) + 1);
	
			//module_logger(&realDiskDSR, _L|L_1, _("CRU disk select, #%d\n"), newnum);
			
			if (data != 0) {
				BaseDiskImage oldInfo = getSelectedDisk();
				
				selectedDisk = newnum;
				
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
				BaseDiskImage oldInfo = getSelectedDisk();
				
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
			
			return 0;
		}
	};
	
	private CruWriter cruwRealDiskSide = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU disk side #%d\n"), data);
			
			// the side is global to all disks
			side = (byte) (data & 1);
			
			try {
				fdc.setSide(side);
			} catch (IOException e) {
				error(e.getMessage());
			}
			
			for (BaseDiskImage info : disks.values()) {
				info.setSide(side);
			}
			
			return 0;
		}
	};
	
	private CruReader crurRealDiskPoll = new CruReader() {
		public int read(int addr, int data, int num) {
			byte newnum = (byte) (((addr - 0x1102) >> 1) + 1);
			return selectedDisk == newnum ? 1 : 0;
		}
	};

	private CruReader crurRealDiskMotor = new CruReader() {
		public int read(int addr, int data, int num) {
			BaseDiskImage info = getSelectedDisk();
			if (info != null)
				return info.motorRunning ? 1 : 0;
			else
				return 0;
		}
	};
	
	private CruReader crurRealDiskZero = new CruReader() {
		public int read(int addr, int data, int num) {
			return 0;
		}
	};
	
	private CruReader crurRealDiskOne = new CruReader() {
		public int read(int addr, int data, int num) {
			return 1;
		}
	};

	
	private CruReader crurRealDiskSide = new CruReader() {
		public int read(int addr, int data, int num) {
			return side;
		}
	};

	private TimerTask motorTickTask;

	private Timer motorTimer;
	
	private static File defaultDiskRootDir;

	
	
	public DiskImageDsr(Machine machine) {
		diskImageDsrEnabled.setBoolean(true);
		
    	String diskImageRootPath = EmulatorSettings.INSTANCE.getBaseConfigurationPath() + "disks";
    	defaultDiskRootDir = new File(diskImageRootPath);
    	defaultDiskRootDir.mkdirs();
    	
    	for (int drive = 1; drive <= 3; drive++) {
    		String name = getDiskImageSetting(drive);
			registerDiskImagePath(name, getDefaultDiskImage(name)); 
    	}
    	
    	CruManager cruManager = machine.getCruManager();
    	
		cruManager.add(0x1102, 1, cruwRealDiskMotor);
		cruManager.add(0x1104, 1, cruwRealDiskHold);
		cruManager.add(0x1106, 1, cruwRealDiskHeads);
		cruManager.add(0x1108, 1, cruwRealDiskSel);
		cruManager.add(0x110A, 1, cruwRealDiskSel);
		cruManager.add(0x110C, 1, cruwRealDiskSel);
		cruManager.add(0x110E, 1, cruwRealDiskSide);
		cruManager.add(0x1102, 1, crurRealDiskPoll);
		cruManager.add(0x1104, 1, crurRealDiskPoll);
		cruManager.add(0x1106, 1, crurRealDiskPoll);
		cruManager.add(0x1108, 1, crurRealDiskMotor);
		cruManager.add(0x110A, 1, crurRealDiskZero);
		cruManager.add(0x110C, 1, crurRealDiskOne);
		cruManager.add(0x110E, 1, crurRealDiskSide);
		
		// add motor timer
		motorTickTask = new TimerTask() {
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				Set<Entry<String, BaseDiskImage>> entrySet = new HashSet<Entry<String,BaseDiskImage>>(disks.entrySet());
				for (Map.Entry<String, BaseDiskImage> entry : entrySet) {
					String name = "DSK" + entry.getKey().charAt(entry.getKey().length() - 1);
					BaseDiskImage info = entry.getValue();
					if (info.motorTimeout != 0) {
						if (!info.motorRunning) {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = true;
								info("{0}: motor on", name);
							}
						} else {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = false;
								info("{0}: motor off", name);
							}
						}
					}
				}
			}
		};
		
		motorTimer = new Timer();
		
		motorTimer.scheduleAtFixedRate(motorTickTask, 0, 100);
	}


	public String getName() {
		return "Disk Image DSR";
				
	}
	public short getCruBase() {
		return 0x1100;
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
	
	private class DiskMMIOMemoryArea extends ConsoleMmioArea {
		public DiskMMIOMemoryArea() {
			super(4);
		}

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readByte(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public byte readByte(MemoryEntry entry, int addr) {
			
			if (addr < 0x5ff0)
				return romMemoryEntry.getArea().flatReadByte(romMemoryEntry, addr);

			byte ret = 0;

			if (!diskImageDsrEnabled.getBoolean())
				return ret;


			switch ((addr - 0x5ff0) >> 1) {
			case R_RDSTAT:
				StringBuilder status = new StringBuilder();
				ret = fdc.status.calculate(fdc.command, status);
				info("FDC read status >" + HexUtils.toHex2(ret) + " : " + status);
				break;

			case R_RTADDR:
				ret = fdc.trackReg;
				info("FDC read track " + ret + " >" + HexUtils.toHex2(ret));
				break;

			case R_RSADDR:
				ret = fdc.sectorReg;
				info("FDC read sector " + ret + " >" + HexUtils.toHex2(ret));
				break;

			case R_RDDATA:
				/* read from circular buffer */

				ret = fdc.readByte();
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
		public void writeByte(MemoryEntry entry, int addr, byte val) {
			if (addr < 0x5ff0) {
				romMemoryEntry.getArea().flatWriteByte(romMemoryEntry, addr, val);
				return;
			}
			
			if (!diskImageDsrEnabled.getBoolean())
				return;

			val = (byte) ~val;

			try {
				switch ((addr - 0x5ff0) >> 1) {
				case R_RDSTAT:
				case R_RTADDR:
				case R_RSADDR:
				case R_RDDATA:
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write read xxx >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WTCMD:
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
					break;
	
				case W_WTADDR:
					fdc.trackReg = val;
					//DSK.status &= ~fdc_LOSTDATA;
					info("FDC write track addr " + val + " >" + HexUtils.toHex2(val));
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WSADDR:
					fdc.sectorReg = val;
					//DSK.status &= ~fdc_LOSTDATA;
					info("FDC write sector addr " + val + " >" + HexUtils.toHex2(val));
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WTDATA:
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
			} catch (IOException e) {
				error(e.getMessage());
			}  catch(Throwable t) {
				error(t.getMessage());
			}
			
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readWord(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public short readWord(MemoryEntry entry, int addr) {
			return (short) ((readByte(entry, (addr & ~1)) << 8) 
			| (readByte(entry, (addr | 1)) & 0xff));
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeWord(v9t9.engine.memory.MemoryEntry, int, short)
		 */
		@Override
		public void writeWord(MemoryEntry entry, int addr, short val) {
			writeByte(entry, (addr & ~1), (byte) (val >> 8));
			writeByte(entry, (addr | 1), (byte) (val & 0xff));
		}
		
	}
	public static enum StatusBit {
		// all steppings + force_interrupt
		NOT_READY(0x80),
		WRITE_PROTECT(0x40),
		HEAD_LOADED(0x20),
		SEEK_ERROR(0x10),
		CRC_ERROR(0x08),
		TRACK_0(0x04),
		INDEX_PULSE(0x02),
		BUSY(0x01),
		
		// read/write
		REC_NOT_FOUND(0x10),
		LOST_DATA(0x04),
		DRQ_PIN(0x02),
		
		MARK_TYPE_40(0x40)
		;
		
		private int val;
	
		StatusBit(int val) {
			this.val = val;
		}
	}

	public static class FDCStatus {
		private Map<StatusBit, Boolean> values = new HashMap<StatusBit, Boolean>();
		//public byte markType;
		
		public static final StatusBit[] COMMON_STATUS = {
			StatusBit.NOT_READY, StatusBit.WRITE_PROTECT, StatusBit.HEAD_LOADED, StatusBit.SEEK_ERROR, 
			StatusBit.CRC_ERROR, StatusBit.TRACK_0, StatusBit.INDEX_PULSE, StatusBit.BUSY
		};
		
		public static final StatusBit[] R_STATUS = {
			StatusBit.NOT_READY, StatusBit.REC_NOT_FOUND,
			StatusBit.CRC_ERROR, StatusBit.LOST_DATA, StatusBit.DRQ_PIN, StatusBit.BUSY
		};
		public static final StatusBit[] W_STATUS = {
			StatusBit.NOT_READY, StatusBit.WRITE_PROTECT, StatusBit.REC_NOT_FOUND,
			StatusBit.CRC_ERROR, StatusBit.LOST_DATA, StatusBit.DRQ_PIN, StatusBit.BUSY
		};
		public boolean is(StatusBit bit) {
			return values.containsKey(bit) && values.get(bit);
		}
		public void set(StatusBit bit) {
			values.put(bit, Boolean.TRUE);
		}
		public void reset(StatusBit bit) {
			values.put(bit, Boolean.FALSE);
		}
		public void clear() {
			values.clear();
		}
		
		/**
		 * @param status
		 * @return
		 */
		public byte calculate(int command, StringBuilder status) {
			StatusBit[] bits = COMMON_STATUS;
			switch (command) {
			case FDC_readIDmarker:
			case FDC_readsector:
			case FDC_readtrack:
				bits = R_STATUS;
				break;
			case FDC_writesector:
			case FDC_writetrack:
				bits = W_STATUS;
				break;
			}
			
			byte val = 0;
			for (StatusBit bit : bits) {
				if (is(bit)) {
					if (status.length() > 0)
						status.append(',');
					status.append(bit);
					val |= bit.val;
				}
			}
			return val;
		}
	}
	public void activate(MemoryDomain console) throws IOException {
		if (!diskImageDsrEnabled.getBoolean())
			return;
		
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
	
	public void deactivate(MemoryDomain console) {
		console.unmapEntry(ioMemoryEntry);
		console.unmapEntry(romMemoryEntry);
	}

	public boolean handleDSR(MemoryTransfer xfer, short code) {
		info("RealDiskDSR: ignoring code = " + code);
		return false;
	}

	/**
	 * @param string
	 */
	private static void info(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
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
	public Map<String, Collection<Setting>> getEditableSettingGroups() {
		Map<String, Collection<Setting>> map = new LinkedHashMap<String, Collection<Setting>>();
		
		Collection<Setting> settings = new ArrayList<Setting>();
		settings.add(diskImageDsrEnabled);
		map.put(DsrHandler.GROUP_DSR_SELECTION, settings);
		
		settings = diskSettingsMap.values();
		map.put(DsrHandler.GROUP_DISK_CONFIGURATION, settings);
		
		return map;
	}
	public void saveState(IDialogSettings section) {
		diskImageDsrEnabled.saveState(section);
		fdc.saveState(section.addNewSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().saveState(section.addNewSection(entry.getKey()));
	}
	
	public void loadState(IDialogSettings section) {
		if (section == null) return;
		diskImageDsrEnabled.loadState(section);
		fdc.loadState(section.getSection("FDC1771"));
		for (Map.Entry<String, BaseDiskImage> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}
}
