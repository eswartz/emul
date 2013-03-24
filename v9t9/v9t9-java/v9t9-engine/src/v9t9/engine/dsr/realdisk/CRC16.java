/**
 * 
 */
package v9t9.engine.dsr.realdisk;

public class CRC16 implements ICRCAlgorithm {

		private short crc;
		private int poly;

		/**
		 * @param poly 
		 * 
		 */
		public CRC16(int poly) {
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
//				if (((crc ^ b) & 0x80) != 0) 
//					crc = (short) ((crc << 1) ^ poly);
//				else
//					crc <<= 1;
//				b <<= 1;
				crc = (short) ((crc << 1) ^ ((((crc >> 8) ^ (b << j)) & 0x0080) != 0 ? poly : 0));
			}
			
//			byte b1 = BinaryUtils.swapBits((byte) (crc & 0xff));
//			byte b2 = BinaryUtils.swapBits((byte) ((crc >> 8) & 0xff));
//			
//			return (short) ((b2 << 8) | (b1 & 0xff));
			return crc;
		}
		/**
		 * @return
		 */
		public short read() {
			return crc;
		}
		
	}