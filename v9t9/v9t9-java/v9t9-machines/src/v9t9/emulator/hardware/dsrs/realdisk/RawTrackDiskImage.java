/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;

/**
 * Raw track image which has no header
 * @author ejs
 *
 */
public class RawTrackDiskImage extends BaseTrackDiskImage  {
	
	public RawTrackDiskImage(String name, File file) {
		super(name, file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDiskType()
	 */
	@Override
	public String getDiskType() {
		return "track-image-raw";
	}
	
	@Override
	public void writeImageHeader() throws IOException {
		if (handle == null || readonly) {
			return;
		}

		/* maintain invariants */
		growImageForContent(); 
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getHeaderSize()
	 */
	@Override
	public int getHeaderSize() {
		return 0;
	}
	
	@Override
	public void readImageHeader() throws IOException {
		if (handle == null)
			return;
		
		readonly = !spec.canWrite();

		// try to get sector 0
		byte[] sector = readSector0(handle);

		if (sector == null)
			throw new IOException(MessageFormat.format("RawTrackDiskImage:  disk image ''{0}'' does not appear to be a raw track image",
					spec));
		
		hdr.sides = sector[0x12];
		hdr.tracks = sector[0x11];
		hdr.tracksize = (short) (handle.length() / hdr.tracks / hdr.sides);
		if (hdr.sides == 1 && hdr.tracksize > 5000) {
			hdr.tracksize /= 2;
			hdr.sides++;
		}
		hdr.track0offs = 0;

		if (hdr.tracksize <= 0) {
			throw new IOException(MessageFormat.format("RawTrackDiskImage:  disk image ''{0}'' has invalid track size {1}\n",
					  spec, hdr.tracksize));
		}

		if (hdr.tracksize > RealDiskImageDsr.DSKbuffersize) {
			throw new IOException(MessageFormat.format("RawTrackDiskImage: disk image ''{0}'' has too large track size ({1} > {2})",
					spec, hdr.tracksize, RealDiskImageDsr.DSKbuffersize));
		}
	}
	
	/**
	 * @param handle
	 * @return
	 * @throws IOException 
	 */
	private static byte[] readSector0(RandomAccessFile handle) throws IOException {
		int ch;
		int count = 0;
		handle.seek(0);
		while ((ch = handle.read()) >= 0 && count < 18) {
			if (ch == 0xfe 		// ID
			&& handle.read() == 0 	// track
			&& handle.read() == 0	// ...
			&& handle.read() == 0)	// sector 
			{
				while ((ch = handle.read()) != 0xfb && ch >= 0) /* */ ;
				if (ch < 0)
					continue;
				
				byte[] sector = new byte[256];
				handle.read(sector);
				
				if (new String(sector).contains("DSK"))
					return sector;
				count++;
			}
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDefaultTrackSize()
	 */
	@Override
	public short getDefaultTrackSize() {
		return (short) 3253;
	}
	
	public static boolean isTrackImage(File file) {
		RandomAccessFile fh = null;
		try {
			fh = new RandomAccessFile(file, "r");
			byte[] sector = readSector0(fh);
			
			return sector != null;
		} catch (IOException e) {
			// ignore
		} finally {
			if (fh != null) {
				try {
					fh.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

}