/**
 * 
 */
package v9t9.gui.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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

	private Display display;
	private PrinterImageShell printerShell;
	private IPrinterImageEngine engine;

	/**
	 * @param i
	 */
	public ManualTestPrinter() {
		display = new Display();
		
		printerShell = null;
		engine = null;
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
	}

	public static void main(String[] args) {
		ManualTestPrinter test = new ManualTestPrinter();
		test.run();
		
	}

	/**
	 * 
	 */
	private void run() {
		 
        engine.setDpi(300, 300);
        
        makeControlPanel();
        
		while (!printerShell.getShell().isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		
	}

	class ControlItem {
		private String label;
		private Runnable runnable;
		/**
		 * @param string
		 * @param runnable
		 */
		public ControlItem(String string, Runnable runnable) {
			label = string;
			this.runnable = runnable;
		}
		@Override
		public String toString() {
			return label;
		}
		public Runnable getRunnable() {
			return runnable;
		}
		
	}
	/**
	 * 
	 */
	private void makeControlPanel() {
		Shell controlShell = new Shell();
		GridLayoutFactory.fillDefaults().applyTo(controlShell);
		
		final ListViewer list = new ListViewer(controlShell, SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(list.getList());
		list.setContentProvider(new ArrayContentProvider());
		list.setLabelProvider(new LabelProvider());
		List<ControlItem> items = new ArrayList<ManualTestPrinter.ControlItem>();
		initControlItems(items);
		list.setInput(items);
		
		Button runButton = new Button(controlShell, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(runButton);
		runButton.setText("Run");
		
		runButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) list.getSelection();
				if (sel.isEmpty()) return;
				
				ControlItem item = (ControlItem) sel.getFirstElement();
				item.getRunnable().run();
			}
		});

		Button formFeedButton = new Button(controlShell, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(formFeedButton);
		formFeedButton.setText("New Page");
		
		formFeedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				engine.newPage();
			}
		});

		controlShell.setSize(300, 500);
		controlShell.open();
	}

	/**
	 * @param items
	 */
	private void initControlItems(List<ControlItem> items) {
		items.add(new ControlItem("Print Stuff", new Runnable() {
			public void run() {
				printStuff();
			}
		}));
	}

	/**
	 * @param engine
	 */
	private void printStuff() {
		
		for (int col = 0; col < 80; col++) {
			engine.print((char) ('0' + col % 10));
		}
		engine.print('\r');
		engine.print('\n');
		
		engine.print("Hello there.  This is the last ditch effort.");
		
		int off = 0;
		for (int row = 0; row < 80; row++) {
			off++;
			for (int col = 0; col < 80; col++) {
				engine.print((char) ((off + col) % 96 + 32));
			}
		}
	}
}
