/*
  IdMarker.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import ejs.base.utils.HexUtils;

public class IdMarker {
	/** Offset of ID in track */
	public int idoffset;
	/** Offset of data in track (or -1) */
	public int dataoffset;
	
	public byte trackid;
	public byte sectorid;
	public byte sideid;
	public byte sizeid;
	public short crcid;
	public byte idCode;
	public byte dataCode;
	
	@Override
	public String toString() {
		return "IdMarker [trackid=" + trackid + ", sectorid=" + sectorid
				+ ", sideid=" + sideid + ", crc=" + HexUtils.toHex4(crcid) + "]";
	}

	/**
	 * @return
	 */
	public boolean isInvalid() {
		return sideid < 0 || sideid > 1;
	}

	/**
	 * @return
	 */
	public int getSectorSize() {
		return 128 << sizeid;
	}
	
	
}