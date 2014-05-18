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
	private final static String ESC = "" + (char) 27;


	private Display display;
	private PrinterImageShell printerShell;
	private IPrinterImageEngine engine;


	private Shell controlShell;

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
        for (IPrinterImageHandler handler : machine.getPrinterImageHandlers()) {
        	engine = ((IPrinterImageHandler) handler).getEngine();
        	printerShell = new PrinterImageShell(engine);
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
		 
        engine.setDpi(360, 360);
        engine.newPage();
        
        makeControlPanel();
        
		while (!controlShell.isDisposed()) {
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
		controlShell = new Shell();
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
		items.add(new ControlItem("Character Widths", new Runnable() {
			public void run() {
				printCharWidths(ESC + "F", "WHEE");
			}
		}));
		items.add(new ControlItem("Character Widths Vertical Emphasized", new Runnable() {
			public void run() {
				printCharWidths(ESC + "E", "EMPHASIZED");
			}
		}));
		items.add(new ControlItem("Character Widths Horizontal Emphasized", new Runnable() {
			public void run() {
				printCharWidths(ESC + "G", "EMPHASIZED");
			}
		}));
		items.add(new ControlItem("Print Stuff", new Runnable() {
			public void run() {
				printStuff();
			}
		}));
		
		items.add(new ControlItem("Print Single-Density Graphics", new Runnable() {
			public void run() {
				printDensityWave("K");
			}

		}));
		
		items.add(new ControlItem("Print Double-Density Graphics", new Runnable() {
			public void run() {
				printDensityWave("L");
			}
			
		}));
	}

	protected void printCharWidths(String ctrl, String string) {
		engine.print(ctrl + "With Normal text. " + string + "\r\n");
		engine.print(ctrl + (char) 15 + "With Condensed text. " + string + (char) 18 + "\r\n");
		engine.print(ctrl + (char) 14 + "With Expanded S/% text. " + string + (char) 20 + "\r\n");
		engine.print(ctrl + (char) 14 + (char) 15+ "With Condensed-Enlarged text. " + string + (char) 18 + (char) 20 + "\r\n");
		
		engine.print(ctrl + 
				//         1         2         3         4         5         6         7         8
				"01234567890123456789012345678901234567890123456789012345678901234567890123456789\r\n");
		engine.print(ctrl + (char) 15 + 
				//         1         2         3         4         5         6         7         8         9        10        11        12        13
				"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901" + (char) 18 + "\r\n");
		engine.print(ctrl + (char) 14 + 
				//         1         2         3         4
				"0123456789012345678901234567890123456789" + (char) 20 + "\r\n");
		engine.print(ctrl + (char) 14 + (char) 15+ 
				//         1         2         3         4         5         6     
				"012345678901234567890123456789012345678901234567890123456789012345" + (char) 18 + (char) 20 + "\r\n");
		
		engine.print(ESC + "F");
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
			engine.print('\r');
			engine.print('\n');
		}
	}

	private void printDensityWave(String format) {
		engine.print("\r" + ESC + "0");
		
		engine.print(ESC + format + length16(360*2));
		
		for (int i = 0; i < 360 * 2; i++) {
			engine.print((char) 255);
		}
		engine.print("\r\n");
		engine.print(ESC + format + length16(360*2)); 
		for (int i = 0; i < 360 * 2; i++) {
			engine.print((char) 255);
		}
		engine.print("\r\n");
		
		for (int l = 1; l <= 2; l++) {
			for (int j = 0; j < 2; j++) {
				String str = subWave(l, j);
				engine.print(ESC + format + length16(str.length()) + str);
			}
			engine.print("\r\n");
		}
		engine.print("\r\n");
	}

	/**
	 * @param length
	 * @return
	 */
	private String length16(int length) {
		return "" + (char) (length & 0xff) + (char) ((length >> 8) & 0xff);
	}

	private String subWave(int l, int j) {
		StringBuilder sb = new StringBuilder();
		for (int x = 1; x <= 6; x++) {
			int y = j == 0 ? x : 7 - x;
			for (int z = 0; z <= y; z++) {
				int n = j == 0 ? z : y - z;
				if (l == 2)
					n = 7 - n;
				sb.append((char) (1<<n));
			}					
		}
		return sb.toString();
	}
}
