/*
  StatusBit.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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