/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.dsr.IPrinterImageListener;
import v9t9.gui.client.swt.imageimport.ImageUtils;

/**
 * @author ejs
 *
 */
public class PrinterImageShell implements IPrinterImageListener {

	private Shell shell; 
	private CTabFolder tabFolder;
	
	// all for the current page; old pages are abandoned
	private Canvas canvas;
	
	protected int pageNum;
	
	private Map<Integer, Image> pageImages = new HashMap<Integer, Image>();
	private Map<Integer, BufferedImage> bufferedImages = new HashMap<Integer, BufferedImage>();
	protected long nextUpdateTime;
	private GridData tabFolderData;

	/**
	 * 
	 */
	public PrinterImageShell() {
		newShell();
	}
	
	/**
	 * 
	 */
	private void newShell() {
		shell = new Shell(SWT.TOOL | SWT.RESIZE);
		
		GridLayoutFactory.fillDefaults().applyTo(shell);
		
		tabFolder = new CTabFolder(shell, SWT.TOP);
		tabFolderData = GridDataFactory.fillDefaults().grab(true, true).create();
		tabFolder.setLayoutData(tabFolderData);
		
		GridLayoutFactory.fillDefaults().applyTo(tabFolder);
		
		shell.setText("Printer Output");
		shell.setSize(400, 400);
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				for (Image image : pageImages.values()) {
					image.dispose();
				}
				pageImages.clear();
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#updated(java.lang.String)
	 */
	@Override
	public void updated() {
		if (canvas == null)
			return;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (canvas == null || canvas.isDisposed())
					return;
				long now = System.currentTimeMillis();
				if (now >= nextUpdateTime) {
					Image swtImage = pageImages.remove(pageNum);
					if (swtImage != null) {
						swtImage.dispose();
					}
					canvas.redraw();
					nextUpdateTime = now + 500;
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#newPage()
	 */
	@Override
	public void newPage(final BufferedImage image) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				
				final int thisPage = ++pageNum;
				
				if (canvas == null || canvas.isDisposed()) {
					if (shell.isDisposed())
						newShell();
					shell.open();
				}
				
				tabFolderData.widthHint = image.getWidth();
				tabFolderData.heightHint = image.getHeight();
				
				CTabItem item = new CTabItem(tabFolder, SWT.NONE);
				
				bufferedImages.put(thisPage, image);
				
				canvas = new Canvas(tabFolder, SWT.BORDER);
				item.setControl(canvas);
		
				GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

				item.setText("Page " + thisPage);
				
				shell.pack();
				
				canvas.addPaintListener(new PaintListener() {
					
					@Override
					public void paintControl(PaintEvent e) {
						// TODO: move this to a thread!
						Image swtImage = pageImages.get(thisPage);
						if (swtImage == null) {
							swtImage = ImageUtils.convertAwtImage(shell.getDisplay(), bufferedImages.get(thisPage));
							pageImages.put(thisPage, swtImage);
						}
						
						if (swtImage != null) {
							e.gc.drawImage(swtImage, 0, 0);
						} else {
							e.gc.fillRectangle(e.x, e.y, e.width, e.height);
						}
					}
				});
				
				tabFolder.setSelection(item);
			}
		});
	}

	/**
	 * @return
	 */
	public Shell getShell() {
		return shell;
	}

}
