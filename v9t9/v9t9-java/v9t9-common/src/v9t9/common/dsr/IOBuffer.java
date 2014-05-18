/**
 * 
 */
package v9t9.common.dsr;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class IOBuffer {
	final int bufSiz;
	final int bufMask;
	public IOBuffer(int size) {
		this.bufSiz = size;
		this.bufMask = size - 1;
		if ((bufSiz & bufMask) != 0)
			throw new IllegalArgumentException();
		buf = new byte[bufSiz];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int idx = st;
		while (idx != en) {
			sb.append((char) buf[idx]);
			if (++idx == bufSiz)
				idx = 0;
		}
		return st + "/" + en + "=" + sb;
	}
	public synchronized boolean isEmpty() {
		return st == en;
	}
	public synchronized boolean isFull() {
		if (st == ((en + 1) & bufMask))
			return true;
		return false;
	}
	public synchronized int getLeft() {
		return ((bufSiz - st + en) & bufMask);
	}

	private byte buf[];
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
	public synchronized int getSt() {
		return st;
	}
	/**
	 * @return the en
	 */
	public synchronized int getEn() {
		return en;
	}
	/**
	 * 
	 */
	public synchronized void clear() {
		Arrays.fill(buf, (byte) 0);
		st = en = 0;
	}
	/**
	 * @param ch
	 */
	public synchronized void add(byte ch) {
		// Put char in buffer.  Kill most recent char if full.
		if (!isFull()) {
			buf[en] = ch;
			en = (en + 1) & bufMask;
		} else {
			buf[(en - 1 + bufSiz) & bufMask] = ch;
		}
		
	}
	/**
	 * @return
	 */
	public synchronized byte take() {
		byte ch;
		if (!isEmpty()) {
			ch = buf[st];
			st = (st + 1) & bufMask;
		} else {
			ch = buf[(st - 1 + bufSiz) & bufMask];
		}
		return ch;
	}

	/**
	 * @return
	 */
	public byte[] takeAll() {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (!isEmpty()) {
			byte b = take();
			bos.write(b);
		}
		return bos.toByteArray();
	}
}