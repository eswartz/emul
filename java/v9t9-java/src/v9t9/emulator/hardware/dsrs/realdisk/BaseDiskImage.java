/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.DSKheader;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.FDCStatus;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.IdMarker;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.StatusBit;


/**
 * @author ejs
 *
 */
public abstract class BaseDiskImage {

	public abstract String getDiskType();
	
	public abstract List<IdMarker> getTrackMarkers();

	public abstract void writeTrackData(byte[] rwBuffer, int i,
			int buflen, FDCStatus status);

	public abstract void writeSectorData(byte[] rwBuffer, int start,
			int buflen, IdMarker marker, FDCStatus status);

	protected abstract void readImageHeader() throws IOException;

	protected abstract void writeImageHeader() throws IOException;

	protected abstract int getHeaderSize();
	
	protected String name;
	protected File spec;
	protected RandomAccessFile handle;
	
	protected boolean trackFetched;
	protected byte trackBuffer[] = new byte[DiskImageDsr.DSKbuffersize];
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

	protected void growImageForContent() throws IOException {
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
		
		DiskImageDsr.info("Opened {0} disk ''{1}'' {2},\n#tracks={3}, tracksize={4}, sides={5}",
					  getDiskType(),
					  spec,
			 name, hdr.tracks, hdr.tracksize, hdr.sides);
	}
	

	public void createDiskImage() throws IOException
	{
		DiskImageDsr.info("Creating new {2} disk image at {0} ({1})", name, spec, getDiskType());

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
			
			DiskImageDsr.info("Reading {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
					hdr.tracksize, seektrack, trackoffset, Long.toHexString(diskoffs));
	
			handle.seek(getTrackDiskOffset());
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
	 * @return
	 */
	private long getTrackDiskOffset() {
		long offset = trackoffset;
		if (sideReg != 0) {
			// goes in reverse order on side 2
			offset = hdr.track0offs + hdr.getTrackSize(hdr.tracks) * 2 - hdr.getTrackSize(hdr.tracks - seektrack);
		}
		return offset;
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
	
		offs = hdr.track0offs + hdr.getTrackSize(seektrack);
	
		// side is handled dynamically
		
		trackoffset = offs;
		
		// refresh
		trackFetched = false;
		//readCurrentTrackData();
		
		return true;
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
	
		DiskImageDsr.info("Writing {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
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
	public void saveState(IDialogSettings section) {
		section.put("FilePath", spec.getAbsolutePath());			
	}

	public void loadState(IDialogSettings section) {
		spec = DiskImageDsr.getDefaultDiskImage(name);
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
			System.arraycopy(trackBuffer, currentMarker.dataoffset + 1, rwBuffer, 0, buflen);
			DiskImageDsr.dumpBuffer(rwBuffer, 0, 256);
		}
	}

}