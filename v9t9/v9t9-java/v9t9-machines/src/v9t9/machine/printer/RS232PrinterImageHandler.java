/**
 * 
 */
package v9t9.machine.printer;

import org.eclipse.swt.widgets.Display;

import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.common.dsr.IRS232Listener;
import v9t9.common.machine.IMachine;

/**
 * This handles 
 * @author ejs
 *
 */
public class RS232PrinterImageHandler implements IRS232Listener, IPrinterImageHandler {

	private IPrinterImageEngine engine;
	
	/**
	 * @param machine
	 * @param i
	 */
	public RS232PrinterImageHandler(IMachine machine, IPrinterImageEngine engine) {
		this.engine = engine;
		machine.getDemoManager().registerActorProvider(new PrinterImageActorProvider(engine.getPrinterId()));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageHandler#getPrinterId()
	 */
	@Override
	public String getPrinterId() {
		return engine.getPrinterId();
	}
	
	@Override
	public IPrinterImageEngine getEngine() {
		return engine;
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
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#updatedControl(v9t9.common.dsr.IRS232Handler.DataSize, v9t9.common.dsr.IRS232Handler.Parity, v9t9.common.dsr.IRS232Handler.Stop)
	 */
	@Override
	public void updatedControl(DataSize size, Parity parity, Stop stop) {
		engine.flushPage();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler.IRS232Listener#charsTransmitted(byte[])
	 */
	@Override
	public void charsTransmitted(final byte[] buffer) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				for (byte b : buffer) {
					char ch = (char) b;
					engine.print(ch);
				}
			}
		});
	}

}
