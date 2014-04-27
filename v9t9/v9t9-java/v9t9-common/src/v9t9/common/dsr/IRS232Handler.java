/**
 * 
 */
package v9t9.common.dsr;

import java.util.Arrays;


/**
 * This interface provides the backend for an emulated RS232 port.
 * @author ejs
 *
 */
public interface IRS232Handler {
	
	public enum Stop {
		STOP_1_5,
		STOP_2,
		STOP_1
	}
	
	public enum Parity {
		NONE,
		ODD,
		EVEN
	}
	
	public enum DataSize {
		FIVE,
		SIX,
		SEVEN,
		EIGHT
	}
	
	public class Buffer {
		// (500000/5/TM_HZ)		// max possible rate to deal with
		static final int BUF_SIZ = 1024;
		static final int BUF_MASK = 1023;

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int idx = st;
			while (idx != en) {
				sb.append((char) buf[idx]);
				if (++idx == BUF_SIZ)
					idx = 0;
			}
			return st + "/" + en + "=" + sb;
		}
		public boolean isEmpty() {
			return st == en;
		}
		public boolean isFull() {
			return st == ((en + 1) & BUF_MASK);
		}
		public int getLeft() {
			return ((BUF_SIZ - st + en) & BUF_MASK);
		}

		private byte	buf[] = new byte[BUF_SIZ];
		private int	st,en;			// pointers to ring
		
		/**
		 * @return the buf
		 */
		public byte[] getBuf() {
			return buf;
		}
		/**
		 * @return the st
		 */
		public int getSt() {
			return st;
		}
		/**
		 * @return the en
		 */
		public int getEn() {
			return en;
		}
		/**
		 * 
		 */
		public void clear() {
			Arrays.fill(buf, (byte) 0);
			st = en = 0;
		}
		/**
		 * @param ch
		 */
		public void add(byte ch) {
			// Put char in buffer.  Kill most recent char if full.
			if (!isFull()) {
				buf[en] = ch;
				en = (en + 1) & BUF_MASK;
			} else {
				buf[(en - 1 + BUF_SIZ) & BUF_MASK] = ch;
			}
			
		}
		/**
		 * @return
		 */
		public byte take() {
			byte ch;
			if (!isEmpty()) {
				ch = buf[st];
				st = (st + 1) & BUF_MASK;
			} else {
				ch = buf[(st - 1 + BUF_SIZ) & BUF_MASK];
			}
			return ch;
		}
	}

	/**
	 * Update control parameters
	 * @param dataSize
	 * @param parity
	 * @param stop
	 */
	void updateControl(DataSize dataSize, Parity parity, Stop stop);

	/**
	 * Update transmit rate
	 */
	void setTransmitRate(int bps);
	/**
	 * Update receive rate
	 */
	void setReceiveRate(int bps);
	
	/**
	 * Transmit characters from the transmit buffer 
	 */
	void transmitChars(Buffer buf);

}

