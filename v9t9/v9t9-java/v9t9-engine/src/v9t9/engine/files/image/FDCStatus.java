/**
 * Mar 1, 2011
 */
package v9t9.engine.files.image;

import java.util.HashMap;
import java.util.Map;

public class FDCStatus {
	private Map<StatusBit, Boolean> values = new HashMap<StatusBit, Boolean>();
	//public byte markType;
	
	public static final StatusBit[] COMMON_STATUS = {
		StatusBit.NOT_READY, StatusBit.WRITE_PROTECT, StatusBit.HEAD_LOADED, StatusBit.SEEK_ERROR, 
		StatusBit.CRC_ERROR, StatusBit.TRACK_0, StatusBit.INDEX_PULSE, StatusBit.BUSY
	};
	
	public static final StatusBit[] R_STATUS = {
		StatusBit.NOT_READY, StatusBit.REC_NOT_FOUND,
		StatusBit.CRC_ERROR, StatusBit.LOST_DATA, StatusBit.DRQ_PIN, StatusBit.BUSY
	};
	public static final StatusBit[] W_STATUS = {
		StatusBit.NOT_READY, StatusBit.WRITE_PROTECT, StatusBit.REC_NOT_FOUND,
		StatusBit.CRC_ERROR, StatusBit.LOST_DATA, StatusBit.DRQ_PIN, StatusBit.BUSY
	};
	public boolean is(StatusBit bit) {
		return values.containsKey(bit) && values.get(bit);
	}
	public void set(StatusBit bit) {
		values.put(bit, Boolean.TRUE);
	}
	public void reset(StatusBit bit) {
		values.put(bit, Boolean.FALSE);
	}
	public void clear() {
		values.clear();
	}
	
	public String toString(int command) {
		StringBuilder status = new StringBuilder();
		StatusBit[] bits = COMMON_STATUS;
		switch (command) {
		case RealDiskConsts.FDC_readIDmarker:
		case RealDiskConsts.FDC_readsector:
		case RealDiskConsts.FDC_readtrack:
			bits = R_STATUS;
			break;
		case RealDiskConsts.FDC_writesector:
		case RealDiskConsts.FDC_writetrack:
			bits = W_STATUS;
			break;
		}
		
		for (StatusBit bit : bits) {
			if (is(bit)) {
				if (status.length() > 0)
					status.append(',');
				status.append(bit);
			}
		}
		return status.toString();
	}
	/**
	 * @param status
	 * @return
	 */
	public byte calculate(int command) {
		StatusBit[] bits = COMMON_STATUS;
		switch (command) {
		case RealDiskConsts.FDC_readIDmarker:
		case RealDiskConsts.FDC_readsector:
		case RealDiskConsts.FDC_readtrack:
			bits = R_STATUS;
			break;
		case RealDiskConsts.FDC_writesector:
		case RealDiskConsts.FDC_writetrack:
			bits = W_STATUS;
			break;
		}
		
		byte val = 0;
		for (StatusBit bit : bits) {
			if (is(bit)) {
				val |= bit.val;
			}
		}
		return val;
	}
}