/*
  BaseDiskImage.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.DiskImageFDR;
import v9t9.common.files.EmulatedDiskImageFile;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.VDR;
import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;


/**
 * @author ejs
 *
 */
public abstract class BaseDiskImage implements IPersistable, IDiskImage {

	static class DSKheader
	{
		/** tracks per side */
		byte			tracks;		
		/** 1 or 2 */
		byte			sides;		
		byte			unused;
		/** bytes per track */
		short			tracksize;	
		/** offset for track 0 data */
		int				track0offs;	
		public long getImageSize() {
			return (tracksize & 0xffff) * (tracks & 0xff) * sides;
		}
		public int getTrackOffset(int num) {
			return num * (tracksize & 0xffff);
		}
	};
	
	private String name;
	protected File spec;
	private RandomAccessFile handle;
	
	protected boolean trackFetched;
	protected byte trackBuffer[] = new byte[RealDiskConsts.DSKbuffersize];
	protected DSKheader hdr = new DSKheader();
	protected boolean readonly;
	int trackoffset;
	protected byte seektrack;
	protected byte sideReg;
	private boolean motorRunning;
	private long motorTimeout;
	
	protected Dumper dumper;

	/**
	 * @param name 
	 * @param settings 
	 * 
	 */
	public BaseDiskImage( String name, File spec, ISettingsHandler settings) {
		this.name = name;
		this.spec = spec;
		
		dumper = new Dumper(settings,
				RealDiskDsrSettings.diskImageDebug, ICpu.settingDumpFullInstructions);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#isDiskImageOpen()
	 */
	@Override
	public boolean isDiskImageOpen() {
		return handle != null;
	}

	/**
	 * @param file
	 */
	public void setFile(File file) {
		try {
			//flush();
			closeDiskImage();
		} catch (IOException e) {
		}
		this.spec = file;
	}

	
	/**
	 * 
	 */
	public void closeDiskImage() throws IOException {
		if (handle != null) {
			//FDCflush();
			//buflen = bufpos = 0;
			handle.close();
			handle = null;
		}			
	}

	public void growImageForContent() throws IOException {
		long      sz, len;
	
		if (spec == null || handle == null) 
			return;
	
		len = spec.length();
	
		sz = hdr.getImageSize();
	
		/* never shrink */
		if (sz > len) {
			handle.setLength(sz);
		}
	}

	protected abstract short getDefaultTrackSize();
	/**
	 * 
	 */
	public void openDiskImage() throws IOException {
		if (handle != null)
			closeDiskImage();
	
		if (spec.exists()) {
			try {
				handle = new RandomAccessFile(spec, "rw");
				readonly = false;
			} catch (IOException e) {
				handle = new RandomAccessFile(spec, "r");
				readonly = true;
			}
		} else {
			readonly = false;
			createDiskImage();
			closeDiskImage();
			return;
		}
	
		/* get disk info */
		try {
			readImageHeader();
		} catch (IOException e) {
			try {
				createDiskImage();
			} catch (IOException e2) {
				closeDiskImage();
				throw e2;
			}
			readImageHeader();
		}
		
		trackFetched = false;
		
		dumper.info("Opened {0} disk ''{1}'' {2},\n#tracks={3}, tracksize={4}, sides={5}",
					  getDiskType(),
					  spec,
			 name, hdr.tracks, hdr.tracksize, hdr.sides);
	}
	

	public void createDiskImage() throws IOException
	{
		dumper.info("Creating new {2} disk image at {0} ({1})", name, spec, getDiskType());

		/* defaults */
		hdr.tracks = 40;
		hdr.sides = 1;
		hdr.tracksize = getDefaultTrackSize();
		hdr.track0offs = getHeaderSize();

		/* create file */
		handle = null;
		handle = new RandomAccessFile(spec, "rw");
		
		writeImageHeader();
	}
	
	/**
	 * 
	 */
	public byte[] readCurrentTrackData() throws IOException {
		if (handle == null)
			return trackBuffer;
	
		if (!trackFetched) {
			long diskoffs = getTrackDiskOffset();
			
			dumper.info("Reading {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
					hdr.tracksize, seektrack, trackoffset, Long.toHexString(diskoffs));
	
			handle.seek(diskoffs);
			try {
				handle.read(trackBuffer, 0, hdr.tracksize);
			} catch (IndexOutOfBoundsException e) {
				throw (IOException) new IOException().initCause(e);
			}
			trackFetched = true;
		}
		
		return trackBuffer;
	}

	/**
	 * 
	 */
	public boolean seekToCurrentTrack(byte seektrack, byte sideReg)
			throws IOException {
		int         offs;
	
		this.seektrack = seektrack;
		this.sideReg = sideReg;
		
		trackoffset = 0;
	
		if (handle == null) 
			return false;
	
		if (seektrack >= hdr.tracks || sideReg >= hdr.sides) {
			// grow the disk
			if (seektrack >= hdr.tracks)
				hdr.tracks = seektrack;
			if (sideReg >= hdr.sides)
				hdr.sides = sideReg;
			
			writeImageHeader();
		}
	
		offs = hdr.track0offs + hdr.getTrackOffset(seektrack);
	
		// side is handled dynamically
		
		trackoffset = offs;
		
		// refresh
		trackFetched = false;
		//readCurrentTrackData();
		
		return true;
	}


	/**
	 * @return
	 */
	public long getTrackDiskOffset() {
		long offset = trackoffset;
		if (sideReg != 0) {
			// goes in reverse order on side 2
			offset = hdr.track0offs + hdr.getTrackOffset(hdr.tracks) * 2 - hdr.getTrackOffset(seektrack + 1);
		}
		return offset;
	}

	/**
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 */
	public void readTrackData(byte[] rwBuffer, int i, int buflen) throws IOException {
		readCurrentTrackData();
		
		buflen = Math.min(hdr.tracksize, buflen);
		
		System.arraycopy(trackBuffer, 0, rwBuffer, i, buflen);			
	}

	/**
	 * @param status
	 */
	public void commitTrack(FDCStatus status) throws IOException {
		if (readonly) {
			status.set(StatusBit.WRITE_PROTECT);
	
			throw new IOException(MessageFormat.format("Disk image ''{0}'' is write-protected",
						  spec));
		}
		
		int size = getTrackSize();
		long diskoffs = getTrackDiskOffset();
	
		dumper.info("Writing {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
				size, seektrack, trackoffset, Long.toHexString(diskoffs));
	
	
		try {
			handle.seek(diskoffs);
		} catch (IOException e) {
			status.set(StatusBit.NOT_READY);
			throw e;
		}
		try {
			handle.write(trackBuffer, 0, size);
		} catch (IOException e) {
			status.set(StatusBit.NOT_READY);
			status.set(StatusBit.CRC_ERROR);
			throw e;
		}
		
		trackFetched = true;
	}

	/**
	 * @return
	 */
	public int getTrackSize() {
		return hdr.tracksize;
	}

	/**
	 * @param addNewSection
	 */
	public void saveState(ISettingSection section) {
		section.put("FilePath", spec.getAbsolutePath());			
	}

	public void loadState(ISettingSection section) {
		spec = RealDiskDsrSettings.getDefaultDiskImage(name);
		if (section == null)
			return;
		String path = section.get("FilePath");
		if (path != null)
			spec = new File(path);
	
	}

	/**
	 * 
	 */
	public void validateDiskImage() {
		if (!spec.exists()) {
			try {
				handle.close();
			} catch (IOException e) {
			}
			handle = null;
		}
	}

	public void setSide(byte side) {
		sideReg = side;
		
	}
	
	public void readSectorData(IdMarker currentMarker, byte[] rwBuffer,
			int start, int buflen) {
		if (currentMarker != null) {
			try {
				System.arraycopy(trackBuffer, currentMarker.dataoffset + 1, rwBuffer, start, buflen);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Possible bogus sector size: " + buflen);
			}
			RealDiskUtils.dumpBuffer(dumper, rwBuffer, 0, 256);
		}
	}

	protected abstract List<IdMarker> getTrackMarkers();

	protected abstract void writeTrackData(byte[] rwBuffer, int i,
			int buflen, FDCStatus status);

	protected abstract void writeSectorData(byte[] rwBuffer, int start,
			int buflen, IdMarker marker, FDCStatus status);

	public void readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException {
		int secsPerTrack = 9;
		byte track = (byte) (sector / secsPerTrack);
		byte side = (byte) (track > 40 ? 1 : 0);
		byte tracksec = (byte) (sector % secsPerTrack);
		seekToCurrentTrack(track, side);
		List<IdMarker> markers = getTrackMarkers();
		for (IdMarker marker : markers) {
			if (marker.sectorid == tracksec && marker.trackid == track) {
				readSectorData(marker, rwBuffer, start, buflen);
				return;
			}
		}
		throw new IOException("sector " + sector + " not found on track " + track + ", side " + side);
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	public Catalog readCatalog(String devname) throws IOException {
		List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
		byte[] asec = new byte[256];
		byte[] fdrSec = new byte[256];
		
		readSector(0, asec, 0, 256);
		VDR vdr = VDR.createVDR(asec, 0);
		String volume = vdr.getVolumeName();
		int total = vdr.getTotalSecs();
		int used = vdr.getSecsUsed();
		
		readSector(1, asec, 0, 256);
		for (int ent = 0; ent < 256; ent+=2) {
			int sec = ((asec[ent] << 8) | (asec[ent+1] & 0xff)) & 0xffff;
			if (sec == 0)
				break;
			try {
				readSector(sec, fdrSec, 0, 256);
				DiskImageFDR fdr = DiskImageFDR.createFDR(fdrSec, 0);
//				int sz = fdr.getSectorsUsed() + 1;
				
				entries.add(new CatalogEntry(fdr.getFileName(), 
						new EmulatedDiskImageFile(this, fdr, fdr.getFileName())));
//						sz, 
//						fdr.getFlags(), fdr.getRecordLength(),
//						(fdr.getFlags() & FDR.ff_protected) != 0));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new Catalog(devname, volume, total, used, entries);
	}

	/**
	 * @param motorTimeout the motorTimeout to set
	 */
	public void setMotorTimeout(long motorTimeout) {
		this.motorTimeout = motorTimeout;
	}

	/**
	 * @return the motorTimeout
	 */
	public long getMotorTimeout() {
		return motorTimeout;
	}

	/**
	 * @param motorRunning the motorRunning to set
	 */
	public void setMotorRunning(boolean motorRunning) {
		this.motorRunning = motorRunning;
	}

	/**
	 * @return the motorRunning
	 */
	public boolean isMotorRunning() {
		return motorRunning;
	}

	/**
	 * @param handle the handle to set
	 */
	public void setHandle(RandomAccessFile handle) {
		this.handle = handle;
	}

	/**
	 * @return the handle
	 */
	public RandomAccessFile getHandle() {
		return handle;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}