/*
  BinaryUtils.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

/**
 * @author ejs
 *
 */
public class BinaryUtils {

	static byte   swapped_nybbles[] = 
	{ 
		0x0, 0x8, 0x4, 0xc,
		0x2, 0xa, 0x6, 0xe,
		0x1, 0x9, 0x5, 0xd,
		0x3, 0xb, 0x7, 0xf
	};

	public static byte swapBits(byte in)
	{
		return (byte) ((swapped_nybbles[in & 0xf] << 4) |
			(swapped_nybbles[(in & 0xf0) >> 4]));
	}

	public static int getMask(int size) {
		int mask = size - 1;
		while (mask != (mask |= (mask >>> 1))) /**/;
		return mask;
	}

	/**
	 * @param val2
	 * @return
	 */
	public static int numberOfBits(int val_) {
		int cnt = 0;
		int val = val_;
		while (val != 0) {
			cnt++;
			val &= (val - 1);
		}
		return cnt;
	}
	public static int maxBitNumber(int val_) {
		int cnt = 0;
		int val = val_;
		while (val > 0) {
			cnt++;
			val >>= 1;
		}
		return cnt;
	}
}
