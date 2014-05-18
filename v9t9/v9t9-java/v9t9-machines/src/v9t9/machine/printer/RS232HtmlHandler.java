/**
 * 
 */
package v9t9.machine.printer;

import v9t9.common.dsr.IPrinterHtmlEngine;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.common.dsr.IRS232Listener;

/**
 * @author ejs
 *
 */
public class RS232HtmlHandler implements IRS232Listener {

	private IPrinterHtmlEngine engine = new EpsonPrinterHtmlEngine();
	
	public IPrinterHtmlEngine getEngine() {
		return engine;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#updatedControl(v9t9.common.dsr.IRS232Handler.DataSize, v9t9.common.dsr.IRS232Handler.Parity, v9t9.common.dsr.IRS232Handler.Stop)
	 */
	@Override
	public void updatedControl(DataSize size, Parity parity, Stop stop) {
		engine.newPage();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#receiveRateSet(int)
	 */
	@Override
	public void receiveRateSet(int recvrate) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#transmitRateSet(int)
	 */
	@Override
	public void transmitRateSet(int xmitrate) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#charsTransmitted(byte[])
	 */
	@Override
	public void charsTransmitted(byte[] buffer) {
		for (byte b : buffer) {
			char ch = (char) b;
			engine.sendChar(ch);
		}
	}

}
