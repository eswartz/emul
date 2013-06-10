/*
  MFMFormat.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.files.IdMarker;
import v9t9.engine.dsr.realdisk.CRC16;
import v9t9.engine.dsr.realdisk.ICRCAlgorithm;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class MFMFormat implements IDiskFormat {

	private static final byte ID_MARK = (byte) 0xfe;
	private static final byte DATA_MARK = (byte) 0xfb;
	
	private ICRCAlgorithm crcAlg;
	private Dumper dumper;

	/**
	 * 
	 */
	public MFMFormat(Dumper dumper) {
		this.dumper = dumper;
		crcAlg = new CRC16(0x1021);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.IDiskFormat#getCRCAlgorithm()
	 */
	@Override
	public ICRCAlgorithm getCRCAlgorithm() {
		return crcAlg;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.IDiskFormat#fetchIdMarkers(byte[], v9t9.common.files.IDiskHeader)
	 */
	@Override
	public List<IdMarker> fetchIdMarkers(byte[] trackBuffer, int trackSize, boolean formatting) {
		List<IdMarker> markers = new ArrayList<IdMarker>();
		int n4eCount = 0;
//		int na1Count = 0;
		for (int startoffset = 0; startoffset < trackSize; startoffset++) {
			byte b = trackBuffer[startoffset];
			if (b == (byte) 0x4e) {
				n4eCount++;
				continue;
			}
//			if (b == (byte) 0xa1) {
//				na1Count++;
//				continue;
//			}
			if (b != ID_MARK)
				continue;
			if (n4eCount < 24 && startoffset >= 32) {
				n4eCount = 0;
				continue;
			}

			CircularByteIter iter = new CircularByteIter(trackBuffer, trackSize);
			
			iter.setPointers(0, startoffset);
			iter.setCount(64);
			
			IdMarker marker = new IdMarker();
			marker.idoffset = iter.getPointer() + iter.getStart();
			
			// reset CRC
			crcAlg.reset();
			
			// get ID
			marker.idCode = iter.next();
			marker.trackid = iter.next();
			marker.sideid = iter.next();
			marker.sectorid = iter.next();
			marker.sizeid = iter.next();
			marker.crcid = (short) (iter.next()<<8); marker.crcid |= iter.next() & 0xff;
			marker.dataCode = DATA_MARK;
			
			crcAlg.feed(marker.idCode);
			crcAlg.feed(marker.trackid);
			crcAlg.feed(marker.sideid);
			crcAlg.feed(marker.sectorid);
			crcAlg.feed(marker.sizeid);

			boolean matched = marker.crcid == (short) 0xf7f7; 	// marker for 'please calculate CRC for me'
						//|| (marker.crcid & 0xff) == 0;	// same for CorComp
			if (!matched) {
				short crc = crcAlg.read();
				if (formatting) {
					crcAlg.feed((byte) (crc >> 8));
					crcAlg.feed((byte) (crc & 0xff));
					if (crcAlg.read() != 0) {
						dumper.info("FDCfindIDmarker: failed ID CRC check on format (>{0} != >{1})",
								HexUtils.toHex4(crcAlg.read()), HexUtils.toHex4(0));
						continue;
					}
					marker.crcid = crc;
					trackBuffer[marker.idoffset + 1 + 4] = (byte) (crc >> 8);
					trackBuffer[marker.idoffset + 1 + 5] = (byte) (crc & 0xff);
				}
				
				if (crc != marker.crcid)
				{
					dumper.info("FDCfindIDmarker: failed ID CRC check (>{0} != >{1})",
							HexUtils.toHex4(marker.crcid), HexUtils.toHex4(crc));
					continue;
				}
			}
			
			// look ahead to see if we find a data marker
			boolean foundAnotherId = false;
			while (iter.hasNext() && iter.peek() != DATA_MARK) {
				b = iter.peek();
				if (b == (byte) 0x4e) {
					n4eCount++;
					iter.next();
					continue;
				}
//				if (b == (byte) 0xa1) {
//					na1Count++;
//					iter.next();
//					continue;
//				}
				if (b == ID_MARK) {
					if (n4eCount >= 32 /*&& na1Count >= 3*/) {
						foundAnotherId = true;
						break;
					}
				}
				iter.next();
			}
			
			// we probably started inside data
			if (foundAnotherId)
				continue;
			
			if (iter.hasNext()) {
				marker.dataoffset = iter.getPointer() + iter.getStart();
			} else {
				marker.dataoffset = -1;
			}
			
			markers.add(marker);
		}
		return markers;

	}

	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.IDiskFormat#doesFormatMatch(byte[], int)
	 */
	@Override
	public boolean doesFormatMatch(byte[] trackBuffer, int trackSize) {
		IdMarker marker = new IdMarker();
		int n4eCount = 0;
//		int na1Count = 0;
		int secCount = 0;
		for (int startoffset = 0; startoffset < trackSize; startoffset++) {
			byte b = trackBuffer[startoffset];
			if (b == (byte) 0x4e) {
				n4eCount++;
				continue;
			}
//			if (b == (byte) 0xa1) {
//				na1Count++;
//				continue;
//			}
			if (b != ID_MARK)
				continue;
			if (n4eCount < 32 /* || na1Count < 3*/) {
				n4eCount = 0;
//				na1Count = 0;
				continue;
			}
			
			CircularByteIter iter = new CircularByteIter(trackBuffer, trackSize);
			
			iter.setPointers(0, startoffset);
			iter.setCount(64);
			
			marker.idoffset = iter.getPointer() + iter.getStart();
			
			// reset CRC
			crcAlg.reset();
			
			// get ID
			marker.idCode = iter.next();
			marker.trackid = iter.next();
			marker.sideid = iter.next();
			marker.sectorid = iter.next();
			marker.sizeid = iter.next();
			marker.crcid = (short) (iter.next()<<8); marker.crcid |= iter.next() & 0xff;
			marker.dataCode = DATA_MARK;
			
			boolean matched = false;
			if (marker.crcid == (short) 0xf7f7	// "please calculate CRC for me"
						|| (marker.crcid & 0xff) == 0) { // same for CorComp
				matched = true;
			} else {
				crcAlg.feed(marker.idCode);
				crcAlg.feed(marker.trackid);
				crcAlg.feed(marker.sideid);
				crcAlg.feed(marker.sectorid);
				crcAlg.feed(marker.sizeid);
	
				short crc = crcAlg.read();
				matched = (crc == marker.crcid);
			}
			
			if (!matched)
				continue;
			
			secCount++;
			
			// look ahead to see if we find a data marker
			while (iter.hasNext() && iter.peek() != DATA_MARK) {
				b = iter.peek();
				if (b == (byte) 0x4e) {
					n4eCount++;
					iter.next();
					continue;
				}
//				if (b == (byte) 0xa1) {
//					na1Count++;
//					iter.next();
//					continue;
//				}
				iter.next();
			}
		}
		return secCount > 0;

	}
}
