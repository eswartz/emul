/*
  V9t9TrackDiskImage.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import v9t9.common.client.ISettingsHandler;

public class V9t9TrackDiskImage extends BaseTrackDiskImage  {
	

	/*	Header for track (*.trk) files; also used internally for sector
		files, but not present in image: we guess the disk geometry from
		the size and sector 0 information. */

	static final String TRACK_MAGIC			= "trak";
	static final int TRACK_MAGIC_SIZE 	= 4;
	static final int TRACK_VERSION		= 1;

	public static final short TRACK_HEADER_SIZE = 12;

	public V9t9TrackDiskImage(ISettingsHandler settings, String name, File file) {
		super(name, file, settings);
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
		if (getHandle() == null || readonly) {
			return;
		}

		/* byteswap header for export */
		getHandle().seek(0);
		getHandle().write(TRACK_MAGIC.getBytes());
		getHandle().write(TRACK_VERSION);
		getHandle().write(hdr.tracks);
		getHandle().write(hdr.sides);
		getHandle().write(hdr.secsPerTrack);
		getHandle().write(hdr.tracksize >> 8);
		getHandle().write(hdr.tracksize & 0xff);
		getHandle().write(hdr.track0offs >> 8);
		getHandle().write(hdr.track0offs & 0xff);

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
		if (getHandle() == null)
			return;
		
		readonly |= !spec.canWrite();

		getHandle().seek(0);
		
		/* byteswap imported header */
		byte[] magic = new byte[TRACK_MAGIC.length()];
		getHandle().read(magic);
		byte version = getHandle().readByte();
		hdr.tracks = getHandle().readByte() & 0xff;
		hdr.sides = getHandle().readByte() & 0xff;
		byte spt = getHandle().readByte(); 
		hdr.tracksize =  (((getHandle().read() & 0xff) << 8) | (getHandle().read() & 0xff));
		if (spt == 0) {
			spt = (byte) (hdr.tracksize / 256);
			if (spt < 18)
				spt = 9;
			else
				spt = 18;
		}
		hdr.secsPerTrack = spt & 0xff;
		hdr.track0offs = (((getHandle().read() & 0xff) << 8) | (getHandle().read() & 0xff));

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

		if (hdr.tracksize > RealDiskConsts.DSKbuffersize) {
			throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
						  hdr.tracksize, RealDiskConsts.DSKbuffersize));
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