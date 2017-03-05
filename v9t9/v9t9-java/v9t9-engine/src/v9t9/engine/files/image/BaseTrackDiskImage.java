/*
  BaseTrackDiskImage.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IdMarker;
import v9t9.engine.dsr.realdisk.CRC16;


public abstract class BaseTrackDiskImage extends BaseDiskImage  {
	
	private CRC16 crcAlg = new CRC16(0x1021);

	public BaseTrackDiskImage(String name, File file, ISettingsHandler settings) {
		super(name, file, settings);
	}
	
	@Override
	public void writeSectorData(byte[] rwBuffer, int start, int buflen,
			IdMarker marker) {
		int ptr;

		int idoffset = marker.idoffset;
		int dataoffset = marker.dataoffset;
		
		// write new ID field
		int offs = idoffset;
		if (trackBuffer[offs] != marker.idCode)
			dumper.error("Inconsistent idoffset ({0})", idoffset);

		trackBuffer[offs+0] = marker.idCode;
		trackBuffer[offs+1] = marker.trackid;
		trackBuffer[offs+2] = marker.sideid;
		trackBuffer[offs+3] = marker.sectorid;
		trackBuffer[offs+4] = marker.sizeid;
		trackBuffer[offs+5] = (byte) (marker.crcid >> 8);
		trackBuffer[offs+6] = (byte) (marker.crcid & 255);

		// write data with new CRC
		if (trackBuffer[dataoffset] != marker.dataCode)
			dumper.error("Inconsistent dataoffset ({0})", dataoffset);
		trackBuffer[dataoffset] = marker.dataCode;
		
		offs = dataoffset;
		
		int size = (128 << marker.sizeid);
		ptr = offs + 1;
		
		crcAlg.reset();
		crcAlg.feed(marker.dataCode);
		
		while (size > 0) {
			int tocopy = Math.min(trackBuffer.length - ptr, size);
			System.arraycopy(rwBuffer, 0, trackBuffer, ptr, tocopy);
			
			while (tocopy-- > 0) {
				crcAlg.feed(trackBuffer[ptr++]);
				size--;
			}
			if (ptr >= trackBuffer.length)
				ptr = 0;
		}

		trackBuffer[ptr++] = (byte) (marker.crcid >> 8);
		trackBuffer[ptr++] = (byte) (marker.crcid & 0xff);
		
		trackChanged = true;
		
		// dump contents
		RealDiskUtils.dumpBuffer(dumper, rwBuffer, start, buflen);
		
	}

	/**
	 * Scan the current track for ID markers
	 * @return
	 */
	@Override
	public void fetchFormatAndTrackMarkers() {
		
		try {
			readCurrentTrackData();
		} catch (IOException e) {
			dumper.error(e.getMessage());
			trackMarkers = Collections.emptyList();
			trackFormat = fmFormat;
			return;
		}
		
		List<IdMarker> fmMarkers = fmFormat.fetchIdMarkers(trackBuffer, hdr.getTrackSize(), false);
		List<IdMarker> mfmMarkers = mfmFormat.fetchIdMarkers(trackBuffer, hdr.getTrackSize(), false);
		
		if (fmMarkers.size() == 0 && mfmMarkers.size() == 0) {
			// unformatted
			if (trackFormat == null) {
				trackFormat = fmFormat;
			} else {
				// keep last format
			}
			trackMarkers = Collections.emptyList();
		}
		else {
			int totFmSize = 0;
			int totMfmSize = 0;
			for (IdMarker marker : fmMarkers) {
				totFmSize += marker.getSectorSize();
			}
			for (IdMarker marker : mfmMarkers) {
				totMfmSize += marker.getSectorSize();
			}
			if (totFmSize < hdr.getTrackSize() / 2 && mfmMarkers.size() == 0) {
				dumper.info("found only " + fmMarkers.size() + " FM sectors on track " + track +" in disk image " 
						+ spec);
			}
			else if (totMfmSize < hdr.getTrackSize() / 2 && fmMarkers.size() == 0) {
				dumper.info("found only " + mfmMarkers.size() + " MFM sectors on track " + track  +" in disk image " 
						+ spec);
			}
			if (fmMarkers.size() > mfmMarkers.size()) {
				crcAlg = new CRC16(0x1021);
				trackMarkers = fmMarkers;
				trackFormat = fmFormat;
			} else {
				crcAlg = new CRC16(0x1021);
				trackMarkers = mfmMarkers;
				trackFormat = mfmFormat;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.BaseDiskImage#discoverSide2TrackOrdering()
	 */
	@Override
	protected void discoverSide2TrackOrdering() {
		// detect the side 2 ordering
		int origTrack = track;
		try {
			setTrack(0);
			
			fetchFormatAndTrackMarkers();
			boolean any = false;
			for (IdMarker marker : trackMarkers) {
				if (marker.sideid != 1)
					continue;
				
				if (marker.trackid == hdr.getTracks() - 1) {
					any = true;
					hdr.setInvertedSide2(true);
					break;
				}
				else if (marker.trackid == 0) {
					any = true;
					hdr.setInvertedSide2(false);
					break;
				}
			}
			if (any) {
				hdr.setSide2DirectionKnown(true);
			}
		} catch (IOException e) {
			// ok, leave it alone for now
		} finally {
			try {
				setTrack(origTrack);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}