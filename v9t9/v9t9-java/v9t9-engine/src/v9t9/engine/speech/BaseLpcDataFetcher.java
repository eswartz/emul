/**
 * 
 */
package v9t9.engine.speech;

import v9t9.common.speech.ISpeechPhraseListener;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

abstract class BaseLpcDataFetcher implements ILPCDataFetcher {
	protected ILPCByteFetcher byteFetcher;
	protected int bit;
	private ListenerList<ISpeechPhraseListener> listeners;
	
	/**
	 * 
	 */
	public BaseLpcDataFetcher() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.speech.ILPCDataFetcher#setPhraseListeners(ejs.base.utils.ListenerList)
	 */
	@Override
	public void setPhraseListeners(ListenerList<ISpeechPhraseListener> listeners) {
		this.listeners = listeners;
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
			
			byte next = byteFetcher.peek();
			fireByteAdded(next);
			
			cur |= next & 0xff; /*
								 * we can't read more than 6 bits, so no
								 * poss of crossing TWO bytes
								 */
		} else {
			final byte next = byteFetcher.peek();
			if (bit == 0) {
				fireByteAdded(next);
			}
			cur = next << 8;
		}

		return extractBits(cur, bits);
	}

	/**
	 * @param next
	 */
	private void fireByteAdded(final byte next) {
		if (listeners != null) {
			listeners.fire(new IFire<ISpeechPhraseListener>() {

				@Override
				public void fire(ISpeechPhraseListener listener) {
					listener.phraseByteAdded(next);
				}
			});
		}
		
	}

}