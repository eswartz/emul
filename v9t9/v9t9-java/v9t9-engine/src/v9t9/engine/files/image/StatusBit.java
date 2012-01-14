/**
 * Mar 1, 2011
 */
package v9t9.engine.files.image;

public enum StatusBit {
	// all steppings + force_interrupt
	NOT_READY(0x80),
	WRITE_PROTECT(0x40),
	HEAD_LOADED(0x20),
	SEEK_ERROR(0x10),
	CRC_ERROR(0x08),
	TRACK_0(0x04),
	INDEX_PULSE(0x02),
	BUSY(0x01),
	
	// read/write
	REC_NOT_FOUND(0x10),
	LOST_DATA(0x04),
	DRQ_PIN(0x02),
	
	MARK_TYPE_40(0x40)
	;
	
	int val;

	StatusBit(int val) {
		this.val = val;
	}
	
	/**
	 * @return the val
	 */
	public int getVal() {
		return val;
	}
}