/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

public class V9t9TrackDiskImage extends BaseTrackDiskImage  {
	

	/*	Header for track (*.trk) files; also used internally for sector
		files, but not present in image: we guess the disk geometry from
		the size and sector 0 information. */

	static final String TRACK_MAGIC			= "trak";
	static final int TRACK_MAGIC_SIZE 	= 4;
	static final int TRACK_VERSION		= 1;

	public static final short TRACK_HEADER_SIZE = 12;

	public V9t9TrackDiskImage(String name, File file) {
		super(name, file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDiskType()
	 */
	@Override
	public String getDiskType() {
		return "track-image-v9t9";
	}
	
	@Override
	public void writeImageHeader() throws IOException {
		if (handle == null || readonly) {
			return;
		}

		/* byteswap header for export */
		handle.seek(0);
		handle.write(TRACK_MAGIC.getBytes());
		handle.write(TRACK_VERSION);
		handle.write(hdr.tracks);
		handle.write(hdr.sides);
		handle.write(0); // unused
		handle.write(hdr.tracksize >> 8);
		handle.write(hdr.tracksize & 0xff);
		handle.write(hdr.track0offs >> 8);
		handle.write(hdr.track0offs & 0xff);

		/* maintain invariants */
		growImageForContent(); 
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getHeaderSize()
	 */
	@Override
	public int getHeaderSize() {
		return TRACK_HEADER_SIZE;
	}
	
	@Override
	public void readImageHeader() throws IOException {
		if (handle == null)
			return;
		
		readonly = !spec.canWrite();

		handle.seek(0);
		
		/* byteswap imported header */
		byte[] magic = new byte[TRACK_MAGIC.length()];
		handle.read(magic);
		byte version = handle.readByte();
		hdr.tracks = handle.readByte();
		hdr.sides = handle.readByte();
		handle.readByte(); // unused
		hdr.tracksize =  (short) (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));
		hdr.track0offs = (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));

		/* verify */
		if (!Arrays.equals(TRACK_MAGIC.getBytes(), magic)) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has unknown type (got {1}, expected {2})",
					spec,
					new String(magic),
					TRACK_MAGIC));
		}	

		if (version < 1 || version > TRACK_VERSION) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has too new version ({1} > {2})\n",
						  spec, version, TRACK_VERSION));
		}
		
		if (hdr.tracksize < 0) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has invalid track size {1}\n",
					  spec, hdr.tracksize));
		}

		if (hdr.tracksize > RealDiskImageDsr.DSKbuffersize) {
			throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
						  hdr.tracksize, RealDiskImageDsr.DSKbuffersize));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDefaultTrackSize()
	 */
	@Override
	public short getDefaultTrackSize() {
		return (short) 3210;
	}
	
	public static boolean isTrackImage(File file) {
		byte[] header = new byte[TRACK_MAGIC.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			fis.read(header);
			
			return Arrays.equals(TRACK_MAGIC.getBytes(), header);
		} catch (IOException e) {
			// ignore
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

}