/**
 * 
 */
package v9t9.machine.printer;

import org.eclipse.swt.widgets.Display;

import v9t9.common.dsr.IPIOListener;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.machine.IMachine;

/**
 * This handles 
 * @author ejs
 *
 */
public class PIOPrinterImageHandler implements IPIOListener, IPrinterImageHandler {

	private IPrinterImageEngine engine = new EpsonPrinterImageEngine(360, 360);
	private int printerId;
	
	public PIOPrinterImageHandler(IMachine machine, int printerId) {
		this.printerId = printerId;
		machine.getDemoManager().registerActorProvider(new PrinterImageActorProvider(printerId));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageHandler#getPrinterId()
	 */
	@Override
	public int getPrinterId() {
		return printerId;
	}
	
	
	@Override
	public IPrinterImageEngine getEngine() {
		return engine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPIOListener#charsTransmitted(byte[])
	 */
	@Override
	public void charsTransmitted(final byte[] buffer) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (byte b : buffer) {
					engine.print((char) b);
				}
			}
		});
	}

}
