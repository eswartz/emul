/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.memory;

/**
 * @author ejs
 */
public class ZeroWordMemoryArea extends WordMemoryArea {
	/* can neither read nor write directly */
	/* for reads, return zero */
	/* for writes, ignore */
    public static short zeroes[] = new short[0x10000/2];
    public ZeroWordMemoryArea() {
		this(0);
	}
	public ZeroWordMemoryArea(int latency) {
		super(latency);
		memory = zeroes;
	}
}

