/**
 * 
 */
package v9t9.engine.speech;

abstract class BaseLpcDataFetcher implements ILPCDataFetcher {
	protected ILPCByteFetcher byteFetcher;
	protected int bit;
	
	/**
	 * 
	 */
	public BaseLpcDataFetcher() {
	}
	
	/**
	 * @param byteFetcher the byteFetcher to set
	 */
	public void setByteFetcher(ILPCByteFetcher byteFetcher) {
		this.byteFetcher = byteFetcher;
	}
	
	public void reset() {
		bit = 0;
	}
	
	protected int extractBits(int cur, int bits) {
		/* Get the bits we want. */
		cur = (cur << bit + 16) >>> (32 - bits);

		/* Adjust bit ptr */
		bit = (bit + bits) & 7;

		bits += bits;
		return cur;
	}
	

	/**
	 * Fetch so many bits.
	 */
	@Override
	public int fetch(int bits) {
		int cur;

		if (bit + bits >= 8) { /* we will cross into the next byte */
			cur = byteFetcher.read();
			cur <<= 8;
			cur |= byteFetcher.peek() & 0xff; /*
								 * we can't read more than 6 bits, so no
								 * poss of crossing TWO bytes
								 */
		} else
			cur = byteFetcher.peek() << 8;

		return extractBits(cur, bits);
	}

}