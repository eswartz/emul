/**
 * 
 */
package v9t9.machine.printer;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IPrinterHtmlEngine;
import v9t9.common.dsr.IRS232Handler;

/**
 * @author ejs
 *
 */
public class RS232HtmlHandler implements IRS232Handler {
	
	private DataSize dataSize;
	private Parity parity;
	private Stop stop;

	private IPrinterHtmlEngine engine = new EpsonPrinterHtmlEngine();
	
	public IPrinterHtmlEngine getEngine() {
		return engine;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#updateControl(v9t9.common.dsr.IRS232Handler.DataSize, v9t9.common.dsr.IRS232Handler.Parity, v9t9.common.dsr.IRS232Handler.Stop)
	 */
	@Override
	public void updateControl(DataSize dataSize, Parity parity, Stop stop) {
		// ignore unless changing
		if (this.dataSize != dataSize || this.parity != parity || this.stop != stop) {
			this.dataSize = dataSize;
			this.parity = parity;
			this.stop = stop;

			engine.newPage();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#setTransmitRate(int)
	 */
	@Override
	public void setTransmitRate(int bps) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#setReceiveRate(int)
	 */
	@Override
	public void setReceiveRate(int bps) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#transmitChars(v9t9.common.dsr.IRS232Handler.IOBuffer)
	 */
	@Override
	public void transmitChars(IOBuffer buf) {
		while (!buf.isEmpty()) {
			char ch = (char) buf.take();
			engine.sendChar(ch);
		}
	}

}
