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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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
	
	private double zoom = 1.0;
	
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
		
		GridLayoutFactory.fillDefaults().applyTo(shell);
		
		tabFolder = new CTabFolder(shell, SWT.TOP);
		tabFolderData = GridDataFactory.fillDefaults().grab(true, true).create();
		tabFolder.setLayoutData(tabFolderData);
		
		GridLayoutFactory.fillDefaults().applyTo(tabFolder);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem item = tabFolder.getSelection();
				if (item != null) {
					Scrollable scrollable = (Scrollable) item.getControl();
					if (scrollable != null) {
						scrollable.getVerticalBar().setSelection(0);
					}
				}
			}
		});
	
		ToolBar toolbar = new ToolBar(tabFolder, SWT.NONE);
		tabFolder.setTopRight(toolbar);
		final ToolItem zoomIn = new ToolItem(toolbar, SWT.PUSH);
		zoomIn.setText("+");
		zoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom * 2);
			}
		});
		final ToolItem zoomOut = new ToolItem(toolbar, SWT.PUSH);
		zoomOut.setText("-");
		zoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom / 2);
			}
		});
		final ToolItem zoomReset = new ToolItem(toolbar, SWT.PUSH);
		zoomReset.setText("=");
		zoomReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(1.0);
			}
		});
	}


	protected void zoom(double toZoom) {
		this.zoom = toZoom;
		updatePageZooms();
	}

	/**
	 * 
	 */
	private void updatePageZooms() {
		CTabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			CTabItem item = items[i];
			ScrolledComposite scrolled = (ScrolledComposite) item.getControl();
			Composite canvas = (Composite) scrolled.getContent();
			BufferedImage image = bufferedImages.get(i + 1);
			if (image != null) {
				canvas.setSize(new Point((int) (image.getWidth() * zoom), (int) (image.getHeight() * zoom)));
	            canvas.layout();
			}
		}
		tabFolder.redraw();
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
				
//				tabFolderData.widthHint = (int) (image.getWidth() * zoom);
//				tabFolderData.heightHint = (int) (image.getHeight() * zoom);
				
				CTabItem item = new CTabItem(tabFolder, SWT.NONE);
				
				bufferedImages.put(thisPage, image);
				
				ScrolledComposite scrolled = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
				scrolled.setAlwaysShowScrollBars(false);
				item.setControl(scrolled);
				GridLayoutFactory.swtDefaults().applyTo(scrolled);
				
				canvas = new Canvas(scrolled, SWT.BORDER);
				
				scrolled.setContent(canvas);
		
				GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
				
				item.setText("Page " + thisPage);
				
				if (pageNum == 1) {
					zoom = 1.0;
					while (zoom * image.getHeight() > shell.getDisplay().getBounds().height)
						zoom /= 2;
					
				}
				
				updatePageZooms();
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
						
						e.gc.setAntialias(SWT.ON);
						e.gc.setInterpolation(SWT.HIGH);
						if (swtImage != null) {
							//							Rectangle bounds = swtImage.getBounds();
//							e.gc.drawImage(swtImage, 0, 0, bounds.width, bounds.height, 
//									0, 0, (int) (bounds.width * zoom), (int) (bounds.height * zoom));
							Transform xfrm = new Transform(e.gc.getDevice());
							xfrm.scale((float) zoom, (float) zoom);
							e.gc.setTransform(xfrm);
							e.gc.drawImage(swtImage, 0, 0);
							xfrm.dispose();
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
