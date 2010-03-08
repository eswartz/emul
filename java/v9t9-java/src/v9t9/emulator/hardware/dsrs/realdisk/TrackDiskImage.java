/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.FDCStatus;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.IdMarker;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.StatusBit;

public class TrackDiskImage extends BaseDiskImage  {
	

	/*	Header for track (*.trk) files; also used internally for sector
		files, but not present in image: we guess the disk geometry from
		the size and sector 0 information. */

	static final String TRACK_MAGIC			= "trak";
	static final int TRACK_MAGIC_SIZE 	= 4;
	static final int TRACK_VERSION		= 1;

	public TrackDiskImage(String name, File file) {
		super(name, file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDiskType()
	 */
	@Override
	public String getDiskType() {
		return "track-image";
	}
	
	@Override
	public void writeImageHeader() throws IOException {
		if (handle == null || readonly) {
			return;
		}

		/* byteswap header for export */
		handle.seek(0);
		handle.write(hdr.magic);
		handle.write(hdr.version);
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
	
	@Override
	public void readImageHeader() throws IOException {
		if (handle == null)
			return;
		
		readonly = !spec.canWrite();

		handle.seek(0);
		
		/* byteswap imported header */
		handle.read(hdr.magic);
		hdr.version = handle.readByte();
		hdr.tracks = handle.readByte();
		hdr.sides = handle.readByte();
		handle.readByte(); // unused
		hdr.tracksize =  (short) (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));
		hdr.track0offs = (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));

		/* verify */
		if (!Arrays.equals(TRACK_MAGIC.getBytes(), hdr.magic)) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has unknown type (got {1}, expected {2})",
					spec,
					new String(hdr.magic),
					TRACK_MAGIC));
		}	

		if (hdr.version < 1 || hdr.version > TRACK_VERSION) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has too new version ({1} > {2})\n",
						  spec, hdr.version, TRACK_VERSION));
		}
		
		if (hdr.tracksize < 0) {
			throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has invalid track size {1}\n",
					  spec, hdr.tracksize));
		}

		if (hdr.tracksize > DiskImageDsr.DSKbuffersize) {
			throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
						  hdr.tracksize, DiskImageDsr.DSKbuffersize));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDefaultTrackSize()
	 */
	@Override
	public short getDefaultTrackSize() {
		return (short) DiskImageDsr.DSKtracksize_SD;
	}
	
	@Override
	public void writeSectorData(byte[] rwBuffer, int start, int buflen,
			IdMarker marker, FDCStatus status) {
		if (readonly) {
			status.set(StatusBit.WRITE_PROTECT);
			return;
		}

		if (marker == null) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}
		
		int ptr;

		int idoffset = marker.idoffset;
		int dataoffset = marker.dataoffset;
		
		if (idoffset < 0 || dataoffset < 0) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}
		
		// write new ID field
		int offs = idoffset;
		if (trackBuffer[offs] != (byte) 0xfe)
			DiskImageDsr.error("Inconsistent idoffset ({0})", idoffset);

		trackBuffer[offs+0] = (byte) 0xfe;
		trackBuffer[offs+1] = marker.trackid;
		trackBuffer[offs+2] = marker.sideid;
		trackBuffer[offs+3] = marker.sectorid;
		trackBuffer[offs+4] = marker.sizeid;
		trackBuffer[offs+5] = (byte) (marker.crcid >> 8);
		trackBuffer[offs+6] = (byte) (marker.crcid & 255);

		// write data with new CRC
		if (trackBuffer[dataoffset] != (byte) 0xfb)
			DiskImageDsr.error("Inconsistent dataoffset ({0})", dataoffset);
		trackBuffer[dataoffset] = (byte) 0xfb;
		
		offs = dataoffset;
		
		int size = (128 << marker.sizeid);
		ptr = offs + 1;
		
		marker.crcid = (short) 0xffff;
		
		while (size > 0) {
			int tocopy = Math.min(trackBuffer.length - ptr, size);
			System.arraycopy(rwBuffer, 0, trackBuffer, ptr, tocopy);
			
			while (tocopy-- > 0) {
				marker.crcid = DiskImageDsr.calc_crc(marker.crcid, trackBuffer[ptr++]);
				size--;
			}
			if (ptr >= trackBuffer.length)
				ptr = 0;
		}

		trackBuffer[ptr++] = (byte) (marker.crcid >> 8);
		trackBuffer[ptr++] = (byte) (marker.crcid & 0xff);
		
		// dump contents
		DiskImageDsr.dumpBuffer(rwBuffer, start, buflen);
		
	}

	/**
	 * Write data written for the track; may be larger than allowed track size
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 * @param fdc 
	 */
	@Override
	public void writeTrackData(byte[] rwBuffer, int i, int buflen, FDCStatus status) {
		if (readonly) {
			status.set(StatusBit.WRITE_PROTECT);
			return;
		}

		buflen = Math.min(hdr.tracksize, buflen);
		System.arraycopy(rwBuffer, i, trackBuffer, 0, buflen);
		trackFetched = true;

		// dump contents
		DiskImageDsr.dumpBuffer(rwBuffer, i, buflen);
	}
	
	/**
	 * Scan the current track for ID markers
	 * @return
	 */
	@Override
	public List<IdMarker> getTrackMarkers() {
		List<IdMarker> markers = new ArrayList<IdMarker>();
		
		try {
			readCurrentTrackData();
		} catch (IOException e) {
			DiskImageDsr.error(e.getMessage());
			return markers;
		}
		
		/* scan track for markers */
		
		for (int startoffset = 0; startoffset < hdr.tracksize; startoffset++) {
			if (trackBuffer[startoffset] != (byte) 0xfe)
				continue;
			
			CircularByteIter iter = new CircularByteIter(trackBuffer, hdr.tracksize);
		
			iter.setPointers(0, startoffset);
			iter.setCount(30);	/* 30 or 43 for MFM */

			IdMarker marker = new IdMarker();
			marker.idoffset = iter.getPointer() + iter.getStart();
			
			// reset CRC
			short crc;
			crc = (short) 0xffff;
			crc = DiskImageDsr.calc_crc(crc, iter.next());

			// get ID
			marker.trackid = iter.next();
			marker.sideid = iter.next();
			marker.sectorid = iter.next();
			marker.sizeid = iter.next();
			marker.crcid = (short) (iter.next()<<8); marker.crcid |= iter.next() & 0xff;
			
			crc = DiskImageDsr.calc_crc(crc, marker.trackid);
			crc = DiskImageDsr.calc_crc(crc, marker.sideid);
			crc = DiskImageDsr.calc_crc(crc, marker.sectorid);
			crc = DiskImageDsr.calc_crc(crc, marker.sizeid);

			// this algorithm does NOT WORK
			if (false && crc != marker.crcid)
			{
				DiskImageDsr.info("FDCfindIDmarker: failed ID CRC check (>{0} != >{1})",
						HexUtils.toHex4(marker.crcid), HexUtils.toHex4(crc));
				continue;
			}
			
			// look ahead to see if we find a data marker
			boolean foundAnotherId = false;
			while (iter.hasNext() && iter.peek() != (byte) 0xfb) {
				if (iter.peek() == (byte) 0xfe) {
					foundAnotherId = true;
					break;
				}
				iter.next();
			}
			
			// we probably started inside data
			if (foundAnotherId)
				continue;
			
			if (iter.hasNext())
				marker.dataoffset = iter.getPointer() + iter.getStart();
			else
				marker.dataoffset = -1;

			markers.add(marker);
		}
		return markers;
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