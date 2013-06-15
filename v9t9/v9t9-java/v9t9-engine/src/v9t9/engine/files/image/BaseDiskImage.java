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
import v9t9.common.files.FDR;
import v9t9.common.files.IDiskHeader;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.IdMarker;
import v9t9.common.files.IndexSector;
import v9t9.common.files.VIB;
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
		private String spec;

		/** tracks per side */
		private int tracks;
		/** 1 or 2 */
		private int sides;
		/** # tracks per side */
		private int secsPerTrack;
		/** bytes per track */
		private int tracksize;
		/** offset for track 0 data */
		private int track0offs;
		
		/** if true, tracks go in opposite order on side 2 */
		private boolean invertedSide2;

		private boolean side2DirectionKnown;
	
		/* (non-Javadoc)
		 * @see v9t9.common.files.IDiskHeader#getPath()
		 */
		@Override
		public String getPath() {
			return spec;
		}
		@Override
		public void setPath(String spec) {
			this.spec = spec;
		}

		@Override
		public String toString() {
			return "DSKheader [spec="+spec+", tracks=" + tracks + ", sides=" + sides
					+ ", secsPerTrack=" + secsPerTrack
					+ ", tracksize=" + tracksize + ", track0offs=" + track0offs
					+ "]";
		}
		@Override
		public void setSides(int sides) {
			this.sides = sides;
		}
		@Override
		public void setTracks(int tracks) {
			this.tracks = tracks;
		}
		@Override
		public void setSecsPerTrack(int secsPerTrack) {
			this.secsPerTrack = secsPerTrack;
		}
		@Override
		public void setTrackSize(int tracksize) {
			this.tracksize = tracksize;
		}
		@Override
		public void setTrack0Offset(int track0offs) {
			this.track0offs = track0offs;
		}
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
		/* (non-Javadoc)
		 * @see v9t9.common.files.IDiskHeader#getSecsPerTrack()
		 */
		@Override
		public int getSecsPerTrack() {
			return secsPerTrack;
		}
		public long getImageSize() {
			return (tracksize & 0xffff) * (tracks & 0xff) * sides;
		}
		protected int getTrackOffset(int track) {
			return track0offs + (tracksize & 0xffff) * track;
		}
		public int getTrackOffset(int track, int side) {
			int effTrack = side == 0 ? track : 
				(invertedSide2 ? (tracks * 2 - track - 1) : (tracks + track));
			return getTrackOffset(effTrack);
		}

		public void setInvertedSide2(boolean invertedSide2) {
			this.invertedSide2 = invertedSide2;
		}
		public boolean isInvertedSide2() {
			return invertedSide2;
		}
		/**
		 * @param b
		 */
		public void setSide2DirectionKnown(boolean b) {
			side2DirectionKnown = b;
		}
		/**
		 * @return the side2DirectionKnown
		 */
		public boolean isSide2DirectionKnown() {
			return side2DirectionKnown;
		}
		
	};
	
	private String name;
	protected File spec;
	private RandomAccessFile handle;
	
	protected boolean trackFetched;
	protected boolean trackChanged;
	
	protected byte trackBuffer[] = new byte[RealDiskConsts.DSKbuffersize];
	protected DSKheader hdr = new DSKheader();
	protected boolean readonly;
	protected int track;
	protected int side;
	
	protected Dumper dumper;
	protected IDiskFormat fmFormat;
	protected IDiskFormat mfmFormat;
	
	
	protected IDiskFormat trackFormat = new FMFormat(dumper);
	protected List<IdMarker> trackMarkers;
	private Catalog catalog;
	
	/**
	 * @param name 
	 * @param settings 
	 * 
	 */
	public BaseDiskImage( String name, File spec, ISettingsHandler settings) {
		this.name = name;
		this.spec = spec;

		dumper = new Dumper(settings,
				RealDiskSettings.diskImageDebug, ICpu.settingDumpFullInstructions);
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
		commitTrack();

		reset();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	protected void reset() throws IOException {
		if (handle != null) {
			handle.close();
			handle = null;
		}			
		trackFetched = false;
		trackChanged = false;
		trackMarkers = null;
		catalog = null;
		
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
			hdr.setPath(spec.getPath());
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
			if (!readonly) {
				createDiskImage();
				closeDiskImage();
				return;
			} else {
				throw new FileNotFoundException("no disk image file exists: " + spec.toString());
			}
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
		trackChanged = false;
		
		dumper.info("Opened {0} disk ''{1}'' {2},\n#tracks={3}, tracksize={4}, sides={5}",
					  getDiskType(),
					  spec,
			 name, hdr.getTracks(), hdr.getTrackSize(), hdr.getSides());
	}
	

	public void createDiskImage() throws IOException
	{
		dumper.info("Creating new {2} disk image at {0} ({1})", name, spec, getDiskType());

		/* defaults */
		hdr.setPath(spec.getPath());
		hdr.setTracks(40);
		hdr.setSides(1);
		hdr.setSecsPerTrack(9);
		hdr.setTrackSize(getDefaultTrackSize());
		hdr.setTrack0Offset(getHeaderSize());

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
					hdr.getTrackSize(), track, diskoffs, Long.toHexString(diskoffs));
	
			// TODO: need to encapsulate as track data when reading from sector disk
			handle.seek(diskoffs);
			try {
				handle.read(trackBuffer, 0, hdr.getTrackSize());
			} catch (IndexOutOfBoundsException e) {
				throw (IOException) new IOException().initCause(e);
			}
			trackFetched = true;
			trackChanged = false;
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

	protected void updateDiskSize() throws IOException {
		if (handle == null) 
			return;
	
		if (!readonly && (track >= hdr.getTracks() || side >= hdr.getSides())) {
			// grow the disk
			if (track >= hdr.getTracks())
				hdr.setTracks(track);
			if (side >= hdr.getSides())
				hdr.setSides(side);
			
			writeImageHeader();
		}
	
		// refresh
		trackFetched = false;
		trackChanged = false;
		trackMarkers = null;
	}


	/**
	 * @return
	 */
	public long getTrackDiskOffset() {
		return hdr.getTrackOffset(track, side);
	}

	/**
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 */
	public void readTrackData(byte[] rwBuffer, int i, int buflen) throws IOException {
		readCurrentTrackData();
		
		buflen = Math.min(hdr.getTrackSize(), buflen);
		
		System.arraycopy(trackBuffer, 0, rwBuffer, i, buflen);			
	}

	/**
	 */
	public void commitTrack() throws IOException {
		if (!trackFetched || !trackChanged)
			return;
		
		if (handle == null)
			throw new IOException("lost disk before committing track");
		
		int size = getTrackSize();
		long diskoffs = getTrackDiskOffset();
	
		dumper.info("Writing {0} bytes of data on track {1}, offset = >{2}", 
				size, track, Long.toHexString(diskoffs));
	
		handle.seek(diskoffs);
		handle.write(trackBuffer, 0, size);
		
		boolean newHeader = false;
		if (side > 0 && hdr.getSides() == 1) {
			hdr.setSides(2);
			newHeader = true;
		}
		if (track > hdr.getTracks()) {
			hdr.setTracks(track);
			newHeader = true;
		}
		if (trackMarkers.size() > hdr.getSecsPerTrack()) {
			int cnt = trackMarkers.size();
			if (cnt <= 9)
				hdr.setSecsPerTrack(9);
			else
				hdr.setSecsPerTrack(18);
			hdr.setTrackSize(hdr.getSecsPerTrack() * (256 + 100));	// TODO: scan for actual markers
			newHeader = true;
		}
		if (newHeader) {
			writeImageHeader();
		}
		handle.getFD().sync();
		
		trackFetched = true;
		trackChanged = false;
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
		spec = RealDiskSettings.getDefaultDiskImage(name);
		if (section == null)
			return;
		String path = section.get("FilePath");
		if (path != null)
			spec = new File(path);
	
	}

	/**
	 * 
	 */
	public void closeIfMissing() {
		if (!spec.exists()) {
			try {
				reset();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void setSide(int side) throws IOException {
		if (side != this.side) {
			commitTrack();
			
			this.side = side;
			
			if (side == 1 && !hdr.isSide2DirectionKnown()) {
				discoverSide2TrackOrdering();
			}
			
			updateDiskSize();
		}
		
	}
	/**
	 *	Set the {@link IDiskHeader#setInvertedSide2(boolean)} flag 
	 */
	abstract protected void discoverSide2TrackOrdering();

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#getSide()
	 */
	@Override
	public int getSide() {
		return side;
	}
	
	// for utilities, not realtime usage
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

	public IdMarker readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException {
		if (hdr.getSecsPerTrack() == 0)
			throw new IOException("unformatted disk");

		int secsPerTrack = hdr.getSecsPerTrack();
		boolean gotSector0 = false;
		
		if (sector != 0) {
			// try again if the sector 0 information differs
			byte[] sec0 = new byte[256];
			readSector(0, sec0, 0, 256);
			VIB vib = VIB.createVIB(sec0, 0);
			if (vib.isFormatted() && vib.getSecsPerTrack() > 0) {
				secsPerTrack = vib.getSecsPerTrack();
				gotSector0 = true;
			}
		}
		
		int track;
		byte side;
		byte tracksec;
		
		track = (sector / secsPerTrack);
		side = (byte) (track >= 40 ? 1 : 0);

		track %= 40;
		if (side > 0 && hdr.isInvertedSide2())
			track = 39 - track;

		setTrack(track);
		setSide(side);
		
		readCurrentTrackData();
		
		tracksec = (byte) (sector % secsPerTrack);
		for (IdMarker marker : getTrackMarkers()) {
			if (marker.sectorid == tracksec && marker.trackid == track) {
				readSectorData(marker, rwBuffer, start, buflen);
				return marker;
			}
		}
		
		if (!gotSector0 && secsPerTrack == 18 
				&& getTrackMarkers().size() > 0 && getTrackMarkers().size() < 18) {
			// recalc -- maybe we were wrong
			secsPerTrack = 9;
			
			track = (sector / secsPerTrack);
			side = (byte) (track >= 40 ? 1 : 0);

			track %= 40;
			if (side > 0)
				track = 39 - track;

			setTrack(track);
			setSide(side);
			
			readCurrentTrackData();
			
			tracksec = (byte) (sector % secsPerTrack);
			for (IdMarker marker : getTrackMarkers()) {
				if (marker.sectorid == tracksec && marker.trackid == track) {
					readSectorData(marker, rwBuffer, start, buflen);
					return marker;
				}
			}

		}
		
		throw new MissingSectorException(sector, getTrackDiskOffset(),
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
		VIB vib = VIB.createVIB(asec, 0);
		String volume = vib.getVolumeName();
		int total = vib.getTotalSecs();
		int used = vib.getSecsUsed();
		
		readSector(1, asec, 0, 256);
		for (int ent = 0; ent < 256; ent+=2) {
			int sec = ((asec[ent] << 8) | (asec[ent+1] & 0xff)) & 0xffff;
			if (sec == 0)
				break;
			readSector(sec, fdrSec, 0, 256);
			DiskImageFDR fdr = DiskImageFDR.createFDR(fdrSec, 0);
//				int sz = fdr.getSectorsUsed() + 1;
			
			entries.add(new CatalogEntry(sec, fdr.getFileName(), 
					new EmulatedDiskImageFile(this, sec, fdr, fdr.getFileName())));
//						sz, 
//						fdr.getFlags(), fdr.getRecordLength(),
//						(fdr.getFlags() & FDR.ff_protected) != 0));
		}
		return new Catalog(this, devname, volume, total, used, entries);
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
	 * @see v9t9.common.files.IEmulatedDisk#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean wasOpen = isDiskImageOpen();
		if (!wasOpen) {
			try {
				openDiskImage(true);
				
				if (hdr.getSides() <= 2 && hdr.getTrack0Offset() <= 256 && (hdr.getTracks() == 40 || hdr.getTracks() == 80)
						&& hdr.getTrackSize() < RealDiskConsts.DSKbuffersize) {
					return true;
				}
				return false;
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
	 * @see v9t9.common.files.IDiskImage#isFormatted()
	 */
	@Override
	public boolean isFormatted() {
		boolean wasOpen = isDiskImageOpen();
		if (!wasOpen) {
			try {
				openDiskImage(true);
				
				byte[] sec0 = new byte[256];
				readSector(0, sec0, 0, 256);
				VIB vib = VIB.createVIB(sec0, 0);
				
				return vib.isFormatted();
			} catch (FileNotFoundException e) {
				return false;
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
		
		if (mfmFormat.doesFormatMatch(rwBuffer, buflen))
			trackFormat = mfmFormat;
		else
			trackFormat = fmFormat;
		
		// fetch and update markers
		trackMarkers = trackFormat.fetchIdMarkers(rwBuffer, buflen, true);
		
		System.arraycopy(rwBuffer, start, trackBuffer, 0, buflen);
		trackFetched = true;
		trackChanged = true;

		// dump contents
		RealDiskUtils.dumpBuffer(dumper, rwBuffer, start, buflen);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#readCatalog()
	 */
	@Override
	public Catalog readCatalog() throws IOException {
		if (catalog == null) {
			boolean wasOpen = isDiskImageOpen();
			if (!wasOpen)
				openDiskImage(true);
			
			Catalog catalog = readCatalog("DSK" + name.charAt(name.length() - 1));
			
			if (!wasOpen)
				closeDiskImage();
			
			this.catalog = catalog;
			
		}
		
		return catalog;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#getFile(java.lang.String)
	 */
	@Override
	public IEmulatedFile getFile(String name) throws IOException {
		Catalog catalog = readCatalog();
		CatalogEntry ent = catalog.findEntry(name);
		if (ent == null)
			throw new FileNotFoundException(name);
		return ent.getFile();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#getPath()
	 */
	@Override
	public String getPath() {
		return spec.getPath();
	}

	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#createFile(java.lang.String, v9t9.common.files.FDR)
	 */
	@Override
	public IEmulatedFile createFile(final String fileName, final FDR fdr)
			throws IOException {
		final Catalog catalog = readCatalog();
		for (CatalogEntry entry : catalog.getEntries()) {
			if (entry.getFile().getFileName().equalsIgnoreCase(fileName)) {
				// existing file
				deleteFile(entry.getFile());
				break;
			}
		}
		
		final int indexSector = allocateSector(2);
		if (indexSector < 0)
			throw new IOException("no sectors free");
		
		updateSector(1, new SectorUpdater() {
			
			@Override
			public boolean updateSector(byte[] sec1) throws IOException {
				IndexSector index = IndexSector.create(sec1);

				// free sectors for FDR
				index.add(indexSector, fileName, catalog);
			
				System.arraycopy(index.toBytes(), 0, sec1, 0, sec1.length);
				return true;
			}
		});
		
		
		final DiskImageFDR diskfdr = new DiskImageFDR();
		diskfdr.copyFrom(fdr);

		diskfdr.setSectorsUsed(0);
		
		// get sectors for storage
		diskfdr.allocateSectors(this, fdr.getSectorsUsed());
		
		// dump FDR to sector
		updateSector(indexSector, new SectorUpdater() {
			
			@Override
			public boolean updateSector(byte[] fdrSec) throws IOException {
				byte[] out = diskfdr.toBytes();
				System.arraycopy(out, 0, fdrSec, 0, out.length);
				return true;
			}
		});
		
		EmulatedDiskImageFile file = new EmulatedDiskImageFile(this, indexSector, 
				diskfdr, diskfdr.getFileName());
		
		commitTrack();
		
		return file;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#updateSector(int, v9t9.common.files.IDiskImage.SectorUpdater)
	 */
	@Override
	public void updateSector(int num, SectorUpdater sectorUpdater) throws IOException {
		byte[] sec = new byte[256];
		IdMarker marker = readSector(num, sec, 0, 256);
		
		if (sectorUpdater.updateSector(sec)) {
			writeSectorData(sec, num, 256, marker);
			
			commitTrack();
		}
	}
	
	/**
	 * @return
	 */
	public int allocateSector(final int start) throws IOException {
		final int[] num = { -1 };
		updateSector(0, new SectorUpdater() {
			
			@Override
			public boolean updateSector(byte[] content) throws IOException {
				VIB vib = VIB.createVIB(content, 0);
				num[0] = vib.allocateSector(start);
				
				System.arraycopy(vib.toBytes(), 0, content, 0, 256);

				return true;
			}
		});
		return num[0];
	}

	/**
	 * @param file
	 */
	public void deleteFile(IEmulatedFile file) throws IOException {
		if (false == file instanceof EmulatedDiskImageFile)
			throw new IOException("unexpected file type: " + file.getClass());
		
		final EmulatedDiskImageFile dfile = (EmulatedDiskImageFile) file;
		final int[] contents = dfile.getFDR().getContentSectors();
		
		updateSector(0, new SectorUpdater() {
			
			@Override
			public boolean updateSector(byte[] sec0) throws IOException {
				VIB vib = VIB.createVIB(sec0, 0);

				// free sectors for content
				for (int csec : contents) {
					if (csec > 0)
						vib.deallocateSector(csec);
				}
				
				// free index sector
				if (dfile.getIndexSector() > 0) {
					vib.deallocateSector(dfile.getIndexSector());
				}
			
				System.arraycopy(vib.toBytes(), 0, sec0, 0, sec0.length);
				
				return true;
			}
		});
		
		updateSector(1, new SectorUpdater() {
			
			@Override
			public boolean updateSector(byte[] sec1) throws IOException {
				IndexSector index = IndexSector.create(sec1);

				// free sectors for FDR
				index.remove(dfile.getIndexSector());
			
				System.arraycopy(index.toBytes(), 0, sec1, 0, sec1.length);
				
				return true;
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#getTrack()
	 */
	@Override
	public int getTrack() {
		return track;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImage#setTrack(int)
	 */
	@Override
	public void setTrack(int track) throws IOException {
		if (this.track != track) {
			commitTrack();
			this.track = track;
			updateDiskSize();
		}
	}
}