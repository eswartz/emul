/**
 * 
 */
package v9t9.engine.speech;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.BinaryUtils;
import ejs.base.utils.HexUtils;

/**
 * Read LPC data from a FIFO
 * @author ejs
 *
 */
public class FifoLpcDataFetcher extends BaseFifoLpcDataFetcher  {
	
	private byte fifo[]	= new byte[16]; /* fifo buffer */
	private int out, in; /* ptrs. out==in --> empty */
	private int len; /* # bytes in buffer */
	private IProperty logProperty;
	public FifoLpcDataFetcher() {
		setByteFetcher(this);
		
	}
	
	
	public void setLogProperty(IProperty property) {
		this.logProperty = property;
	}
	
	/**
	 * 
	 */
	public synchronized void purge() {
		bit = 0;
		out = in = len = 0;
		
		if (listener != null)
			listener.lengthChanged(len);
	}

	public synchronized void write(byte val) {
		fifo[in] = BinaryUtils.swapBits(val);
		in = (in + 1) & 15;
		
		if (logProperty != null) {
			Logging.writeLogLine(3, logProperty,
				"FIFO write: " + HexUtils.toHex2(val) + "; len = " + len);
			if (len == 16) {
				Logging.writeLogLine(1, logProperty,
						"FIFO OVERFLOW!");
			}
		}
		
		if (len < 16) {
			len++;
		} 
		
		if (listener != null)
			listener.lengthChanged(len);

	}
	
	@Override
	public synchronized byte read() {
		byte ret = fifo[out];
		
		Logging.writeLogLine(3, logProperty, "FIFO read: " + HexUtils.toHex2(ret)
				+ " (was: " + HexUtils.toHex2(BinaryUtils.swapBits(ret)) + ")"
				+ "; len = " + len);


		if (len == 0) {
			if (listener != null) {
				listener.fetchedEmpty();
			}
		}
		if (len > 0) {
			out = (out + 1) & 15;
			len--;
			
			if (listener != null) {
				listener.lengthChanged(len);
			}
		}
		
		return ret;
	}

	@Override
	public synchronized byte peek() {
		return fifo[out];
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.speech.IFifoLpcDataFetcher#isFull()
	 */
	@Override
	public synchronized boolean isFull() {
		return len == fifo.length;
	}
}