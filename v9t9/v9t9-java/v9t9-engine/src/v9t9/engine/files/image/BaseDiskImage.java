/*
  BaseDiskImage.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.DiskImageFDR;
import v9t9.common.files.EmulatedDiskImageFile;
import v9t9.common.files.IDiskHeader;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IdMarker;
import v9t9.common.files.VDR;
import v9t9.engine.dsr.realdisk.ICRCAlgorithm;
import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;


/**
 * @author ejs
 *
 */
public abstract class BaseDiskImage implements IPersistable, IDiskImage {

	static class DSKheader implements IDiskHeader
	{
		String spec;

		/** tracks per side */
		int tracks;
		/** 1 or 2 */
		int sides;
		byte unused;
		/** bytes per track */
		int tracksize;
		/** offset for track 0 data */
		int track0offs;
	
		/* (non-Javadoc)
		 * @see v9t9.common.files.IDiskHeader#getPath()
		 */
		@Override
		public String getPath() {
			return spec;
		}
		public long getImageSize() {
			return (tracksize & 0xffff) * (tracks & 0xff) * sides;
		}
		public int getTrackOffset(int num) {
			return num * (tracksize & 0xffff);
		}
		/* (non-Javadoc)
		 * @see v9t9.engine.files.image.IDiskHeader#getTracks()
		 */
		@Override
		public int getTracks() {
			return tracks;
		}
		/* (non-Javadoc)
		 * @see v9t9.engine.files.image.IDiskHeader#getSides()
		 */
		@Override
		public int getSides() {
			return sides;
		}
		/* (non-Javadoc)
		 * @see v9t9.engine.files.image.IDiskHeader#getTrackSize()
		 */
		@Override
		public int getTrackSize() {
			return tracksize;
		}
		/* (non-Javadoc)
		 * @see v9t9.engine.files.image.IDiskHeader#getTrack0Offset()
		 */
		@Override
		public int getTrack0Offset() {
			return track0offs;
		}
		@Override
		public String toString() {
			return "DSKheader [spec="+spec+", tracks=" + tracks + ", sides=" + sides
					+ ", tracksize=" + tracksize + ", track0offs=" + track0offs
					+ "]";
		}
		
		
	};
	
	private String name;
	protected File spec;
	private RandomAccessFile handle;
	
	protected boolean trackFetched;
	protected byte trackBuffer[] = new byte[RealDiskConsts.DSKbuffersize];
	protected DSKheader hdr = new DSKheader();
	protected boolean readonly;
	/** track offset, minus side */
	long trackoffset;
	/** track offset, plus side */
	long tracksideoffset;
	protected int seektrack;
	protected byte sideReg;
	private boolean motorRunning;
	private long motorTimeout;
	
	protected Dumper dumper;
	protected IDiskFormat fmFormat;
	protected IDiskFormat mfmFormat;
	
	
	protected IDiskFormat trackFormat = new FMFormat(dumper);
	protected List<IdMarker> trackMarkers;
	
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
		fmFormat = new FMFormat(dumper);
		mfmFormat = new MFMFormat(dumper);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + spec;
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
		trackFetched = false;
		trackMarkers = null;
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
		openDiskImage(false);
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void openDiskImage(boolean readOnly) throws IOException {
		if (getHandle() != null)
			closeDiskImage();
	
		if (spec.exists()) {
			hdr.spec = spec.getPath();
			if (readOnly) {
				setHandle(new RandomAccessFile(spec, "r"));
				readonly = true;
			} else {
				try {
					setHandle(new RandomAccessFile(spec, "rw"));
					readonly = false;
				} catch (IOException e) {
					setHandle(new RandomAccessFile(spec, "r"));
					readonly = true;
				}
			}
		} else {
			readonly = readOnly;
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
		hdr.spec = spec.getPath();
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
			trackFormat = null;
			trackMarkers = null;
			
			ensureFormatAndTrackMarkers();
		}
		
		return trackBuffer;
	}


	/**
	 * 
	 */
	private void ensureFormatAndTrackMarkers() {
		if (trackFormat == null || trackMarkers == null || trackMarkers.isEmpty()) {
			fetchFormatAndTrackMarkers();
		}
		
	}

	/**
	 * 
	 */
	@Override
	public boolean seekToCurrentTrack(int seektrack, int sideReg)
			throws IOException {
		int         offs;
	
		this.seektrack = seektrack;
		this.sideReg = (byte) sideReg;
		
		trackoffset = 0;
	
		if (handle == null) 
			return false;
	
		if (!readonly && (seektrack >= hdr.tracks || sideReg >= hdr.sides)) {
			// grow the disk
			if (seektrack >= hdr.tracks)
				hdr.tracks = seektrack;
			if (sideReg >= hdr.sides)
				hdr.sides = sideReg;
			
			writeImageHeader();
		}
	
		offs = hdr.track0offs + hdr.getTrackOffset(seektrack % hdr.tracks);
	
		// side is handled dynamically
		
		trackoffset = offs;
		
		// refresh
		trackFetched = false;
		trackMarkers = null;
		//readCurrentTrackData();
		
		return true;
	}


	/**
	 * @return
	 */
	public long getTrackDiskOffset() {
		tracksideoffset = trackoffset;
		if (sideReg != 0) {
			tracksideoffset += hdr.getTrackOffset(hdr.tracks);
		}
		return tracksideoffset;
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
	 */
	public void commitTrack() throws IOException {
		int size = getTrackSize();
		long diskoffs = getTrackDiskOffset();
	
		dumper.info("Writing {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
				size, seektrack, trackoffset, Long.toHexString(diskoffs));
	
		handle.seek(diskoffs);
		handle.write(trackBuffer, 0, size);
		
		handle.getFD().sync();
		
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
		if (!spec.exists() && handle != null) {
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
				ICRCAlgorithm crcAlg = trackFormat.getCRCAlgorithm();
				crcAlg.reset();
				crcAlg.feed(currentMarker.dataCode);
				for (int offs = 0; offs < buflen; offs++) {
					byte b = trackBuffer[currentMarker.dataoffset + 1 + offs];
					crcAlg.feed(b);
					rwBuffer[start + offs] = b;
				}
				//System.arraycopy(trackBuffer, currentMarker.dataoffset + 1, rwBuffer, start, buflen);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Possible bogus sector size: " + buflen);
			}
			RealDiskUtils.dumpBuffer(dumper, rwBuffer, 0, 256);
		}
	}

	/** Fetch ID markers into markers and establish format */ 
	protected abstract void fetchFormatAndTrackMarkers();

	public void readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException {
		ensureFormatAndTrackMarkers();
		
		int secsPerTrack = 9;
		int track = (sector / secsPerTrack);
		byte side = (byte) (track > 40 ? 1 : 0);
		track %= 40;
		byte tracksec = (byte) (sector % secsPerTrack);
		seekToCurrentTrack(track, side);
		readCurrentTrackData();
		for (IdMarker marker : trackMarkers) {
			if (marker.sectorid == tracksec && marker.trackid == track) {
				readSectorData(marker, rwBuffer, start, buflen);
				return;
			}
		}
		throw new MissingSectorException(sector, tracksideoffset,
				spec+": sector " + tracksec + " not found on track " + track + ", side " + side, null);
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
			readSector(sec, fdrSec, 0, 256);
			DiskImageFDR fdr = DiskImageFDR.createFDR(fdrSec, 0);
//				int sz = fdr.getSectorsUsed() + 1;
			
			entries.add(new CatalogEntry(fdr.getFileName(), 
					new EmulatedDiskImageFile(this, fdr, fdr.getFileName())));
//						sz, 
//						fdr.getFlags(), fdr.getRecordLength(),
//						(fdr.getFlags() & FDR.ff_protected) != 0));
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#isFormatted()
	 */
	@Override
	public boolean isFormatted() {
		boolean wasOpen = isDiskImageOpen();
		if (!wasOpen) {
			try {
				openDiskImage(true);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (!wasOpen) {
					try {
						closeDiskImage();
					} catch (IOException e) {
					}
				}
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#getHeader()
	 */
	@Override
	public IDiskHeader getHeader() {
		return hdr;
	}

	/**
	 * @return
	 */
	public List<IdMarker> getTrackMarkers() {
		ensureFormatAndTrackMarkers();
		return trackMarkers;
	}

	/**
	 * @return
	 */
	public boolean isReadOnly() {
		return readonly;
	}
	
	/**
	 * Write data written for the track; may be larger than allowed track size
	 * @param rwBuffer
	 * @param buflen
	 * @param start
	 * @param fdc 
	 */
	@Override
	public void writeTrackData(byte[] rwBuffer, int start, int buflen) {
		buflen = Math.min(RealDiskConsts.DSKbuffersize, buflen);
		if (buflen == 0)
			buflen = Math.min(rwBuffer.length, RealDiskConsts.DSKbuffersize);
		
		if (trackFormat == null) {
			if (mfmFormat.doesFormatMatch(rwBuffer, buflen))
				trackFormat = mfmFormat;
			else
				trackFormat = fmFormat;
		}
		
		// fetch and update markers
		trackMarkers = trackFormat.fetchIdMarkers(rwBuffer, buflen, true);
		
		System.arraycopy(rwBuffer, start, trackBuffer, 0, buflen);
		trackFetched = true;

		// dump contents
		RealDiskUtils.dumpBuffer(dumper, rwBuffer, start, buflen);
	}
	

}