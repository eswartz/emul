/**
 * Mar 1, 2011
 */
package v9t9.emulator.hardware.dsrs.realdisk;

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
	
	/**
	 * @param status
	 * @return
	 */
	public byte calculate(int command, StringBuilder status) {
		StatusBit[] bits = COMMON_STATUS;
		switch (command) {
		case RealDiskImageDsr.FDC_readIDmarker:
		case RealDiskImageDsr.FDC_readsector:
		case RealDiskImageDsr.FDC_readtrack:
			bits = R_STATUS;
			break;
		case RealDiskImageDsr.FDC_writesector:
		case RealDiskImageDsr.FDC_writetrack:
			bits = W_STATUS;
			break;
		}
		
		byte val = 0;
		for (StatusBit bit : bits) {
			if (is(bit)) {
				if (status.length() > 0)
					status.append(',');
				status.append(bit);
				val |= bit.val;
			}
		}
		return val;
	}
}