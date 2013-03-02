/*
  BitOutputStream.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * Output bits to a byte stream, where bits are arranged in packages
 * from the high part of a byte to the low part, and which may span
 * bytes.  An arrangement of bits within a package is in normal bit order.
 * <p/>
 * For example, a stream containing 13, as 5 bits, and 1, as 3 bits, 
 * would be packaged in one byte, 0x69, as:
 * <p/>
 * <pre> 
 * < 0 1 1 0 1 > < 0 0 1 >
 * @author ejs
 *
 */
public class BitOutputStream implements Closeable {

	private final ByteArrayOutputStream bos;
	// bit pointer, descending from 8 to 1
	private int bit;
	private int curByte;
	
	private final static int MAX_LENGTH  = 16;
	
	public BitOutputStream(ByteArrayOutputStream bos) {
		this.bos = bos;
		bit = 8;
		curByte = 0;
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (bit != 8) {
			bos.write(curByte);
		}
		bos.close();
	}

	public void reset() {
		bos.reset();
		bit = 8;
		curByte = 0;
	}
	

	protected int encodeBits(int cur, int value, int bits) {
		/* Reduce value to # bits */
		value = (value << 32 - bits) >>> (32 - bits);
		
		/* Inject bits */
		cur |= value << (bit - bits);

		/* Adjust bit ptr */
		bit = (bit - bits) & 7;

		return cur;
	}
	
	public void writeBits(int value, int count) throws IOException {
		if (count == 0)
			return;
		if (count < 0)
			throw new IllegalArgumentException();
		if (count >= MAX_LENGTH)
			throw new UnsupportedOperationException();
		
		
		if (bit - count < 0) { /* we will cross into the next byte */
			
			// encode higher-order bits
			int toUse = bit;
			curByte = encodeBits(curByte, value >>> count - bit, toUse);
			bos.write(curByte);
			
			bit = 8;
			curByte = encodeBits(0, value, count - toUse);
		} else {
			curByte = encodeBits(curByte, value, count);
		}
		
		if (bit == 0) {
			bos.write(curByte);
			bit = 8;
			curByte = 0;
		}
		
	}
	
}
