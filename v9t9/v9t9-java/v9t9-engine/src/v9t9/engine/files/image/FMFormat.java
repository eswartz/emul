/**
 * 
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
public class FMFormat implements IDiskFormat {

	private static final byte ID_MARK = (byte) 0xfe;
	private static final byte DATA_MARK = (byte) 0xfb;
	
	private CRC16 crcAlg;
	private Dumper dumper;

	/**
	 * 
	 */
	public FMFormat(Dumper dumper) {
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
		int ffCount = 0;
		for (int startoffset = 0; startoffset < trackSize; startoffset++) {
			byte b = trackBuffer[startoffset];
			if (b == (byte) 0xff) {
				ffCount++;
				continue;
			}
			if (b != ID_MARK)
				continue;
			if (ffCount < 12 && startoffset >= 32) {
				ffCount = 0;
				continue;
			}
			
			CircularByteIter iter = new CircularByteIter(trackBuffer, trackSize);
		
			iter.setPointers(0, startoffset);
			iter.setCount(30);	

			IdMarker marker = new IdMarker();
			marker.idoffset = iter.getPointer() + iter.getStart();
			
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

			boolean matched = marker.crcid == (short) 0xf7f7;	// marker for 'please calculate CRC for me'
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
				if (iter.peek() == ID_MARK) {
					foundAnotherId = true;
					break;
				}
				iter.next();
			}
			
			// we probably started inside data
			if (foundAnotherId)
				continue;
			
			if (iter.hasNext() && marker.sideid >= 0) {
				marker.dataoffset = iter.getPointer() + iter.getStart();
				markers.add(marker);
			}

		}
		return markers;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.files.image.IDiskFormat#doesFormatMatch()
	 */
	@Override
	public boolean doesFormatMatch(byte[] trackBuffer, int trackSize) {
		IdMarker marker = new IdMarker();
		int ffCount = 0;
		int secCount = 0;
		for (int startoffset = 0; startoffset < trackSize; startoffset++) {
			byte b = trackBuffer[startoffset];
			if (b == (byte) 0xff) {
				ffCount++;
				continue;
			}
			if (b != ID_MARK)
				continue;
			if (ffCount < 12 && startoffset >= 16) {
				ffCount = 0;
				continue;
			}
			
			CircularByteIter iter = new CircularByteIter(trackBuffer, trackSize);
		
			iter.setPointers(0, startoffset);
			iter.setCount(30);	

			marker.idoffset = iter.getPointer() + iter.getStart();
			
			crcAlg.reset();

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

			boolean matched = marker.crcid == (short) 0xf7f7;	// marker for 'please calculate CRC for me'
			if (!matched) {
				short crc = crcAlg.read();
				if (crc == marker.crcid)
					matched = true;
			}
			
			if (!matched) {
				continue;
			}
			
			secCount++;

			while (iter.hasNext() && iter.peek() != DATA_MARK) {
				iter.next();
			}
			
			if (iter.hasNext())
				marker.dataoffset = iter.getPointer() + iter.getStart();
			else
				marker.dataoffset = -1;
		}
		return secCount > 0;
	}
}
