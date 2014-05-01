/**
 * 
 */
package v9t9.gui.test;

import org.eclipse.swt.widgets.Display;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.dsr.IRS232Handler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.gui.client.swt.shells.PrinterImageShell;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.MachineModelFactory;

/**
 * @author ejs
 *
 */
public class ManualTestPrinter {

	public static void main(String[] args) {
		Display display = new Display();
		
		PrinterImageShell printerShell = null;
		IPrinterImageEngine engine = null;
		new EmulatorLocalServer();	// init machine models
		
		// TODO
		ISettingsHandler settings = new BasicSettingsHandler();  
		IMachine machine = MachineModelFactory.INSTANCE.createModel("StandardTI994A").createMachine(settings);
        for (IRS232Handler handler : machine.getRS232Handlers()) {
        	if (handler instanceof IPrinterImageHandler) {
        		engine = ((IPrinterImageHandler) handler).getEngine();
        		printerShell = new PrinterImageShell();
        		engine.addListener(printerShell);
        	}
        }
		
        if (printerShell == null || engine == null)
        	System.exit(1);
        
        engine.newPage();
       
        printStuff(engine);
        
		while (!printerShell.getShell().isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		
	}

	/**
	 * @param engine
	 */
	private static void printStuff(IPrinterImageEngine engine) {
		engine.print("Hello there.  This is the last ditch effort.");
		
		int off = 0;
		for (int row = 0; row < 80; row++) {
			off++;
			for (int col = 0; col < 80; col++) {
				engine.print((char) ((off + col) % 96 + 32));
			}
			engine.print('\r');
			engine.print('\n');
		}
	}
}
