/*
  CRC16.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr.realdisk;

public class CRC8 implements ICRCAlgorithm {

	private byte crc;
	private int poly;

	/**
	 * @param poly 
	 * 
	 */
	public CRC8(int poly) {
		reset();
		setPoly(poly);
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.tests.dsr.TestCRC.ICRCAlgorithm#reset()
	 */
	@Override
	public void reset() {
		crc = (byte) 0xff;
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.tests.dsr.TestCRC.ICRCAlgorithm#setPoly(int)
	 */
	@Override
	public void setPoly(int poly) {
		this.poly = poly;
	}
	/**
	 * @return the poly
	 */
	@Override
	public int getPoly() {
		return poly;
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.tests.dsr.TestCRC.ICRCAlgorithm#feed(byte)
	 */
	@Override
	public short feed(byte b) {
		for (int j = 0; j < 8; j++) {
			crc = (byte) ((crc << 1) ^ ((((crc >> 7) ^ (b << j)) & 0x0080) != 0 ? poly : 0));
		}
		return crc;
	}
	/**
	 * @return
	 */
	public short read() {
		return crc;
	}
	
}