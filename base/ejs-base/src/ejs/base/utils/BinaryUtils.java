/**
 * 
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

}
