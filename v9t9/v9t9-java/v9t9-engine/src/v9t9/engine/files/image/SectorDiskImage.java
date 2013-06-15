/*
  SectorDiskImage.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IdMarker;
import v9t9.engine.dsr.realdisk.CRC16;
import v9t9.engine.dsr.realdisk.ICRCAlgorithm;



public class SectorDiskImage extends BaseDiskImage  {
	public SectorDiskImage(ISettingsHandler settings, String name, File file) {
		super(name, file, settings);
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
	
	@Override
	public void writeImageHeader() throws IOException {
		if (getHandle() == null || readonly) {
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

		if (getHandle() == null)
			return;
		
		readonly |= !spec.canWrite();

		/* no header: guess */
		sz = getHandle().length();
		if (sz < 256)
			throw new IOException("Disk size for '" + spec + "' is too small to be a disk file: " + sz);

		/* read sector 0 */
		getHandle().seek(0);
		getHandle().read(sector);

		hdr.setTracks(sector[0x11] & 0xff);
		hdr.setSides(sector[0x12] & 0xff);
		hdr.setTrackSize((sector[0x0C] * 256));
		hdr.setSecsPerTrack(sector[0x0C]);
		hdr.setTrack0Offset(0);
		if ((hdr.getTracks() <= 0 || hdr.getSides() <= 0 || hdr.getTrackSize() <= 0)
				 || 'D' != sector[0x0D] || 'S' != sector[0x0E] || 'K' != sector[0x0F])
		{
			// hmm... bogus or unformatted disk -- guess
			hdr.setSides(1);
			hdr.setTrackSize(256*9);
			int tracks = (int) (sz / hdr.getTrackSize());
			hdr.setTracks(tracks);
			hdr.setSecsPerTrack(9);
			if (tracks >= 80) {
				tracks /= 2;
				hdr.setTracks(tracks);
				hdr.setSides(hdr.getSides() + 1);
				if (tracks >= 80) {
					tracks /= 2;
					hdr.setTracks(tracks);
					hdr.setSecsPerTrack(hdr.getSecsPerTrack() << 1);
					hdr.setTrackSize(hdr.getTrackSize() << 1);
					if (tracks > 40) {
						hdr.setTracks(40);
					}
				}
			}
		}

		hdr.setSide2DirectionKnown(false);
		
		if (hdr.getTrackSize() > RealDiskConsts.DSKbuffersize) {
			throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
						  hdr.getTrackSize(), RealDiskConsts.DSKbuffersize));
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
			IdMarker marker) {
		try {
			System.arraycopy(rwBuffer, 0, trackBuffer, marker.dataoffset + 1, buflen);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		trackChanged = true;
		
		// dump contents
		RealDiskUtils.dumpBuffer(dumper, rwBuffer, start, buflen);
	}

	@Override
	public void writeTrackData(byte[] rwBuffer, int start, int buflen) {
		super.writeTrackData(rwBuffer, start, buflen);
		int expSecs = hdr.getTrackSize()/256;
		if (trackMarkers == null || trackMarkers.size() != expSecs) {
			dumper.error("Program is formatting track at {1} on ''{0}'' with non-ordinary sectors; " +
					"this does not work with sector-image disks", 
					spec, Long.toHexString(getTrackDiskOffset()));
		}
		
		// extract only sector data
		for (IdMarker marker : trackMarkers) {
			if (marker.sizeid == 1 && marker.sectorid < 18) {
				try {
					System.arraycopy(rwBuffer, marker.dataoffset+1,
							trackBuffer, marker.sectorid*256, 256);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
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
			trackFormat = fmFormat;
			trackMarkers = Collections.emptyList();
			return;
		}
		
		/* easy */
		
		trackMarkers = new ArrayList<IdMarker>();
		int sizeid = 1; // HACK
		int sectorsize = (128 << sizeid);
		
		ICRCAlgorithm crcAlg = new CRC16(0x1021);
		for (int dataoffset = 0; dataoffset < hdr.getTrackSize(); dataoffset += sectorsize) {
			IdMarker marker = new IdMarker();

			crcAlg.reset();
			marker.idCode = (byte) 0xfe;
			marker.dataCode = (byte) 0xfb;
			marker.idoffset = -1;
			marker.dataoffset = dataoffset - 1;
			marker.trackid = (byte) track;
			marker.sectorid = (byte) (dataoffset / sectorsize);
			marker.sideid = (byte) side;
			marker.sizeid = (byte) sizeid;
			
			crcAlg.feed(marker.idCode);
			crcAlg.feed(marker.trackid);
			crcAlg.feed(marker.sideid);
			crcAlg.feed(marker.sectorid);
			crcAlg.feed(marker.sizeid);
			
			marker.crcid = crcAlg.read();
			
			trackMarkers.add(marker);
		}
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
		RealDiskUtils.dumpBuffer(dumper, rwBuffer, 0, 256);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.BaseDiskImage#discoverSide2TrackOrdering()
	 */
	@Override
	protected void discoverSide2TrackOrdering() {
		hdr.setInvertedSide2(true);
		hdr.setSide2DirectionKnown(true);
	}
}