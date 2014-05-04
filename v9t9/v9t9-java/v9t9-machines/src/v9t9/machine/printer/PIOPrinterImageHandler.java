/**
 * 
 */
package v9t9.machine.printer;

import org.eclipse.swt.widgets.Display;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IPIOHandler;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;

/**
 * This handles 
 * @author ejs
 *
 */
public class PIOPrinterImageHandler implements IPIOHandler, IPrinterImageHandler {

	private IPrinterImageEngine engine = new EpsonPrinterImageEngine(360, 360);
	
	@Override
	public IPrinterImageEngine getEngine() {
		return engine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#transmitChars(v9t9.common.dsr.IRS232Handler.Buffer)
	 */
	@Override
	public void transmitChars(final IOBuffer buf) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				while (!buf.isEmpty()) {
					char ch = (char) buf.take();
					engine.print(ch);
				}
			}
		});
	}

}
