/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.FDCStatus;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.IdMarker;
import v9t9.emulator.hardware.dsrs.realdisk.DiskImageDsr.StatusBit;

public abstract class BaseTrackDiskImage extends BaseDiskImage  {
	
	public BaseTrackDiskImage(String name, File file) {
		super(name, file);
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

}