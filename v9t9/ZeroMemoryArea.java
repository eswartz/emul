/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9;

/**
 * @author ejs
 */
public class ZeroMemoryArea extends MemoryArea {
	/* can neither read nor write directly */
	/* for reads, return zero */
	/* for writes, ignore */
	static byte zeroes[] = new byte[0x10000];

	public ZeroMemoryArea() {
		memory = zeroes;
	}
}

