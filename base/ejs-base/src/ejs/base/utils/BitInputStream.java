/*
  BitInputStream.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;

/**
 * Input bits from a byte stream, where bits are arranged in packages
 * from the high part of a byte to the low part, and which may span
 * bytes.  An arrangement of bits within a package is in normal bit order.
 * <p/>
 * For example, a stream containing 13, as 5 bits, and 1, as 3 bits, 
 * would be packaged in one byte, 0x69, as:
 * <p/>
 * <pre> 
 * < 0 1 1 0 1 > < 0 0 1 >
 *  </pre>
 * @author ejs
 *
 */
public class BitInputStream implements Closeable {

	private final ByteArrayInputStream bis;
	private int bit;
	private int curByte;
	
	private final static int MAX_LENGTH  = 16;
	
	public BitInputStream(ByteArrayInputStream bis, int bitPosition) {
		this.bis = bis;
		this.bit = bitPosition;
		this.curByte = bit > 0 ? bis.read() : -1;
	}

	public BitInputStream(ByteArrayInputStream bis) {
		this.bis = bis;
		this.bit = 0;
		this.curByte = -1;
	}
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		bis.close();
	}

	public void reset() {
		bis.reset();
		bit = 0;
		curByte = -1;
	}
	

	protected int extractBits(int cur, int bits) {
		/* Get the bits we want, shifting higher-order bits off the end
		 * then shifting back to avoid an AND */
		cur = (cur << bit + 32 - MAX_LENGTH) >>> (32 - bits);

		/* Adjust bit ptr */
		bit = (bit + bits) & 7;

		return cur;
	}
	
	public int readBits(int count) throws IOException {
		if (count == 0)
			return 0;
		if (count < 0)
			throw new IllegalArgumentException();
		if (count >= MAX_LENGTH)
			throw new UnsupportedOperationException();
		
		ensureCurByte();
		if (curByte < 0)
			throw new EOFException();
		
		int cur;
		if (bit + count >= 8) { /* we will cross into the next byte */
			cur = curByte;
			cur <<= 8;
			
			curByte = bis.read();
			if (curByte == -1)
				throw new EOFException();
			
			cur |= curByte & 0xff; 
		} else {
			cur = curByte << 8;
		}
		
		return extractBits(cur, count);
	}

	private void ensureCurByte() throws IOException {
		if (bit == 0 && curByte < 0) {
			curByte = bis.read();
		}		
	}
	
	public int getBitPosition() {
		return bit;
	}
}
