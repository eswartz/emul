/**
 * 
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.dsr.IPrinterHtmlListener;

/**
 * @author ejs
 *
 */
public class PrinterHtmlShell implements IPrinterHtmlListener {

	private Shell shell; 
	private CTabFolder tabFolder;
	
	// all for the current page; old pages are abandoned
	private Browser browser;

	/**
	 * 
	 */
	public PrinterHtmlShell() {
		shell = new Shell(SWT.TOOL | SWT.RESIZE);
		
		GridLayoutFactory.fillDefaults().applyTo(shell);
		
		tabFolder = new CTabFolder(shell, SWT.TOP);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(shell);
		
		shell.setText("Printer Output");
		shell.setSize(800, 1100);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#updated(java.lang.String)
	 */
	@Override
	public void updated(final String html) {
		if (browser == null)
			return;
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (browser == null || browser.isDisposed())
					return;
				browser.setText(html, true);
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#newPage()
	 */
	@Override
	public void newPage() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				
				if (browser == null)
					shell.open();
				
				CTabItem item = new CTabItem(tabFolder, SWT.NONE);
				
				browser = new Browser(tabFolder, SWT.BORDER);
				item.setControl(browser);
				
				tabFolder.setSelection(item);
			}
		});
	}

}
