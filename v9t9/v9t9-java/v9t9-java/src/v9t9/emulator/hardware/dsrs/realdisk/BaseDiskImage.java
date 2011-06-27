/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.hardware.dsrs.CatalogEntry;
import v9t9.engine.files.V9t9FDR;


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
	
	protected String name;
	protected File spec;
	protected RandomAccessFile handle;
	
	private SettingProperty inUseSetting;
	
	protected boolean trackFetched;
	protected byte trackBuffer[] = new byte[StandardDiskImageDsr.DSKbuffersize];
	protected DSKheader hdr = new DSKheader();
	protected boolean readonly;
	int trackoffset;
	protected byte seektrack;
	protected byte sideReg;
	protected boolean motorRunning;
	protected long motorTimeout;

	/**
	 * @param name 
	 * 
	 */
	public BaseDiskImage(String name, File spec) {
		this.name = name;
		this.spec = spec;
		inUseSetting = new SettingProperty(name, Boolean.FALSE);
		inUseSetting.addEnablementDependency(BaseDiskImageDsr.diskImageDsrEnabled);
	}

	/**
	 * @return the inUseSetting
	 */
	public SettingProperty getInUseSetting() {
		return inUseSetting;
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
		
		StandardDiskImageDsr.info("Opened {0} disk ''{1}'' {2},\n#tracks={3}, tracksize={4}, sides={5}",
					  getDiskType(),
					  spec,
			 name, hdr.tracks, hdr.tracksize, hdr.sides);
	}
	

	public void createDiskImage() throws IOException
	{
		StandardDiskImageDsr.info("Creating new {2} disk image at {0} ({1})", name, spec, getDiskType());

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
			
			StandardDiskImageDsr.info("Reading {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
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
	
		StandardDiskImageDsr.info("Writing {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
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
		spec = StandardDiskImageDsr.getDefaultDiskImage(name);
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
			StandardDiskImageDsr.dumpBuffer(rwBuffer, 0, 256);
		}
	}

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
	public List<CatalogEntry> readCatalog() throws IOException {
		List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
		byte[] sec1 = new byte[256];
		byte[] fdrSec = new byte[256];
		
		readSector(1, sec1, 0, 256);
		for (int ent = 0; ent < 256; ent+=2) {
			int sec = ((sec1[ent] << 8) | (sec1[ent+1] & 0xff)) & 0xffff;
			if (sec == 0)
				break;
			try {
				readSector(sec, fdrSec, 0, 256);
				V9t9FDR fdr = V9t9FDR.createFDR(fdrSec, 0);
				int sz = fdr.getSectorsUsed() + 1;
				
				entries.add(new CatalogEntry(fdr.getFileName(), sz, fdr.getFlags(), fdr.getRecordLength()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return entries;
	}
}