/*
  TestCRC.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.tests.dsr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import v9t9.engine.dsr.realdisk.CRC16;
import v9t9.engine.dsr.realdisk.ICRCAlgorithm;
import ejs.base.utils.BinaryUtils;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class TestCRC {

	public static class CRC16_b implements ICRCAlgorithm {

		private short crc;
		private int poly;
		private int crcmask;

		/**
		 * @param poly 
		 * 
		 */
		public CRC16_b(int poly) {
			reset();
			setPoly(poly);
		}
		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.tests.dsr.TestCRC.ICRCAlgorithm#reset()
		 */
		@Override
		public void reset() {
			crc = (short) 0xffff;
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.tests.dsr.TestCRC.ICRCAlgorithm#setPoly(int)
		 */
		@Override
		public void setPoly(int poly) {
			this.poly = poly;
			crcmask = 1;
			while (crcmask+crcmask < poly)
				crcmask <<= 1;
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
				if ((crc & crcmask) != 0) 
					crc = (short) ((crc << 1) ^ poly);
				else
					crc = (short) ((crc << 1) | ((b >> 7) & 1));
				b <<= 1;
			}
			return crc;
		}
		/* (non-Javadoc)
		 * @see v9t9.engine.dsr.realdisk.ICRCAlgorithm#read()
		 */
		@Override
		public short read() {
			return crc;
		}
		
	}
	private Map<Integer, Integer> crcMatches = new HashMap<Integer, Integer>();
	
	public static void main(String[] args) {
		TestCRC crc = new TestCRC();
		for (int i = 1; i < 0xffff; i+=2) {
//			crc.run(new CRC16(0x11021));
//			crc.run(new CRC16(0x8048));
//			crc.run(new CRC16(0x8005));
//			crc.run(new CRC16(0xa001));
//			crc.run(new CRC16(0xc002));
//			crc.run(new CRC16(0x8810));
			crc.run(new CRC16(i), new byte[] { 0, 0, 0, 1 }, 0xf1d3);
			crc.run(new CRC16(i), new byte[] { 0, 0, 2, 1 }, 0x97b1);
			crc.run(new CRC16(i), new byte[] { 0, 0, 7, 1 }, 0x6844);
			crc.run(new CRC16(i), new byte[] { 0, 0, 4, 1 }, 0x3d17);
		}
		
		for (Map.Entry<Integer, Integer> ent : crc.crcMatches.entrySet()) {
			if (ent.getValue() > 1)
				System.out.println("CRC " + HexUtils.toHex4(ent.getKey()) + " got " + ent.getValue() + " matches");
		}
	}

	/**
	 * 
	 */
	private void run(ICRCAlgorithm alg, byte[] middle, int exp) {
		byte[] bytes;
		for (int includeLead = 1; includeLead < 2; includeLead++) {
			for (int includeCRC = 0; includeCRC < 1; includeCRC++) {
				
				for (int invert = 0; invert < 1; invert++) {
					if (includeLead == 0) {
						if (includeCRC == 0) {
							bytes = Arrays.copyOf(middle, middle.length);
						} else {
							bytes = Arrays.copyOf(middle, middle.length + 2);
							bytes[middle.length+0] = (byte) (exp >> 8);
							bytes[middle.length+1] = (byte) (exp & 0xff);
						}
					} else {
						if (includeCRC == 0) {
							bytes = Arrays.copyOf(middle, middle.length + 1);
							System.arraycopy(bytes, 0, bytes, 1, middle.length);
							bytes[0] = (byte) 0xfe;
						} else {
							bytes = Arrays.copyOf(middle, middle.length + 3);
							System.arraycopy(bytes, 0, bytes, 1, middle.length);
							bytes[0] = (byte) 0xfe;
							bytes[middle.length+1] = (byte) (exp >> 8);
							bytes[middle.length+2] = (byte) (exp & 0xff);
						}
						
					}
					if (invert != 0) {
						for (int i = 0; i < bytes.length; i++)
							bytes[i] = (byte) ~bytes[i];
					}
					for (int reverse = 0; reverse < 1; reverse++) {
						runTest(includeLead+":"+includeCRC+":"+reverse+":"+invert, 
								alg, reverse!=0, 
								exp, bytes);
						runTest(includeLead+":"+includeCRC+":"+reverse+":"+invert, 
								alg, reverse!=0, 
								0, bytes);
					}
				}
			}
		}
		
	}

	/**
	 * @param string 
	 * @param alg
	 * @param i
	 * @param rev 
	 * @param bs
	 */
	private void runTest(String string, ICRCAlgorithm alg, boolean rev, int exp, byte[] bs) {
		alg.reset();
		int crc = 0;
		for (byte b : bs) {
			if (rev)
				b = BinaryUtils.swapBits(b);
			crc = alg.feed(b);
		}
		if ((short) crc == (short) exp ) {
			Integer count = crcMatches.get(alg.getPoly());
			if (count == null)
				count = 1;
			else
				count++;
			crcMatches.put(alg.getPoly(), count);
			
			System.out.println(string + ": CRC(" + alg.getClass().getSimpleName() + ":" +  
					Integer.toHexString(alg.getPoly()) + ")=" + HexUtils.toHex4(crc));
		}
	}

}
