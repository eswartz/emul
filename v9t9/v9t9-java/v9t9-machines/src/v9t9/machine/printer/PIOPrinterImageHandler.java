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
 * This handles printer data coming from PIO
 * @author ejs
 *
 */
public class PIOPrinterImageHandler implements IPIOListener, IPrinterImageHandler {

	private IPrinterImageEngine engine;
	
	public PIOPrinterImageHandler(IMachine machine, IPrinterImageEngine engine) {
		this.engine = engine;
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
	 * @see v9t9.common.dsr.IPIOListener#charsTransmitted(byte[])
	 */
	@Override
	public void charsTransmitted(final byte[] buffer) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (byte b : buffer) {
					engine.print(b);
				}
			}
		});
	}

}
