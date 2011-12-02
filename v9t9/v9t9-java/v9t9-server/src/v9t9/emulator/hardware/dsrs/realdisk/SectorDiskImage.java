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


public class SectorDiskImage extends BaseDiskImage  {
	public SectorDiskImage(String name, File file) {
		super(name, file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDiskType()
	 */
	@Override
	public String getDiskType() {
		return "sector-image";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getDefaultTrackSize()
	 */
	@Override
	public short getDefaultTrackSize() {
		return 256 * 9;
	}
	
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
		
		RealDiskImageDsr.info(
			"Opened sector-image disk ''{0}'' {1},\n#tracks={2}, tracksize={3}, sides={4}",
			spec, name, hdr.tracks, hdr.tracksize, hdr.sides);
	}

	@Override
	public void writeImageHeader() throws IOException {
		if (handle == null || readonly) {
			return;
		}

		// no explicit header
		
		/* maintain invariants */
		growImageForContent(); 
	}
	
	@Override
	public void readImageHeader() throws IOException
	{
		long sz;
		byte sector[] = new byte[256];

		if (handle == null)
			return;
		
		readonly = !spec.canWrite();

		/* no header: guess */
		sz = handle.length();
		if (sz < 256)
			throw new IOException("Disk size for '" + spec + "' is too small to be a disk file");

		/* read sector 0 */
		handle.seek(0);
		handle.read(sector);

		hdr.tracks = sector[17];
		hdr.sides = sector[18];
		hdr.tracksize = (short) (sector[12] * 256L);
		hdr.track0offs = 0;
		if (hdr.tracks <= 0 || hdr.sides <= 0 || hdr.tracksize <= 0)
		{
			hdr.sides = 1;
			hdr.tracksize = 256*9;
			int tracks = (int) (sz / hdr.tracksize);
			hdr.tracks = (byte) tracks;
			if (tracks >= 80) {
				tracks /= 2;
				hdr.tracks = (byte) tracks;
				hdr.sides++;
				if (tracks >= 80) {
					tracks /= 2;
					hdr.tracks = (byte) tracks;
					hdr.tracksize <<= 1;
					if (tracks > 40) {
						hdr.tracks = 40;
					}
				}
			}
		}

		if (hdr.tracksize > RealDiskImageDsr.DSKbuffersize) {
			throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
						  hdr.tracksize, RealDiskImageDsr.DSKbuffersize));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.realdisk.BaseDiskImage#getHeaderSize()
	 */
	@Override
	public int getHeaderSize() {
		return 0;
	}
	
	/**
	 * @param rwBuffer
	 * @param start
	 * @param buflen
	 * @param dataoffset
	 */
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
		
		System.arraycopy(rwBuffer, 0, trackBuffer, marker.dataoffset + 1, buflen);
		
		// dump contents
		RealDiskImageDsr.dumpBuffer(rwBuffer, start, buflen);
		
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
		
		formatSectorTrack(rwBuffer, i, buflen);

		// dump contents
		RealDiskImageDsr.dumpBuffer(rwBuffer, i, buflen);
	}
	

	/**
	 * Interpret track data and extract sector data from it 
	 * @param fdc 
	 */
	private void formatSectorTrack(byte[] buffer, int start, int length) {
		// interpret data
		int is = start;
		while (is < length) {
			while (is < length && is < buffer.length && buffer[is] != (byte) 0xfe) is++;
			if (is + 6 < length) {
				byte track, side, sector, size;
				int offs, sz;
				short crc = (short) 0xffff, crcid;

				// ID marker
				is++;

				crc = RealDiskImageDsr.calc_crc(crc, 0xfe);
				track = buffer[is++]; crc = RealDiskImageDsr.calc_crc(crc, track);
				side = buffer[is++]; crc = RealDiskImageDsr.calc_crc(crc, side);
				sector = buffer[is++]; crc = RealDiskImageDsr.calc_crc(crc, sector);
				size = buffer[is++]; crc = RealDiskImageDsr.calc_crc(crc, size);
				crcid = (short) (buffer[is++]<<8); crcid += buffer[is++]&0xff;

				if (crcid == (short) 0xf7ff) crcid = crc;
				/*
				if (false && crcid != crc) {
					// only for MFM, apparently
					info("retrying for id marker (CRC=%04X != %04X)", crc, crcid);
					goto retry;
				}*/

				RealDiskImageDsr.info("Formatting sector track:{0}, side:{1}, sector:{2}, size:{3}, crc={4}", track, side, sector, size, crc);

				sz = 128 << size;
				offs = sector * sz;
				if (offs >= hdr.tracksize) {
					RealDiskImageDsr.error("Program is formatting track on ''{0}'' with non-ordinary sectors; " +
									"this does not work with sector-image disks", spec);
					offs = 0;
				}

				while (is < length && buffer[is++] != (byte) 0xfb) /**/;

				crc = (short) 0xffff;

				if (is + sz + 2 < length) {
					crc = RealDiskImageDsr.calc_crc(crc, 0xfb);
					int cnt = 0;
					for (cnt=0; cnt < sz; cnt++) {
						crc = RealDiskImageDsr.calc_crc(crc, buffer[cnt + is]);
					}
					crcid = (short) (buffer[cnt++]<<8); crcid += buffer[cnt++]&0xff;
					if (crcid == (short) 0xf7ff) crcid = crc;
					/*
					if (0 && crc != crcid) {
						module_logger(&realDiskDSR, _L|L_3,
									  _("retrying for sector data (CRC=%04X != %04X)\n"),
									  crc, crcid);
						goto retry1;
					}*/

					System.arraycopy(buffer, is, trackBuffer, offs, sz);
					//FDCwritedataat(buffer, offs, is, sz, status);
					
					is += sz + 2; // + crc
				} else {
					RealDiskImageDsr.error("Lost sector data in format of sector-image disk ''{0}''", spec);
					break;
				}
			}
		}			
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
			RealDiskImageDsr.error(e.getMessage());
			return markers;
		}
		
		/* easy */
		
		int sizeid = 1; // HACK
		int sectorsize = (128 << sizeid);
		
		for (int dataoffset = 0; dataoffset < hdr.tracksize; dataoffset += sectorsize) {
			IdMarker marker = new IdMarker();
			marker.crcid = 0;
			marker.idoffset = -1;
			marker.dataoffset = dataoffset - 1;
			marker.trackid = seektrack;
			marker.sectorid = (byte) (dataoffset / sectorsize);
			marker.sideid = sideReg;
			marker.sizeid = (byte) sizeid;
			
			markers.add(marker);
		}
		
		return markers;
	}

	/**
	 * @param currentMarker
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 */
	@Override
	public void readSectorData(IdMarker currentMarker, byte[] rwBuffer,
			int i, int buflen) {
		System.arraycopy(trackBuffer, currentMarker.dataoffset + 1, rwBuffer, 0, buflen);
		RealDiskImageDsr.dumpBuffer(rwBuffer, 0, 256);
		
	}
	
}