/**
 * 
 */
package v9t9.gui.client.swt.shells;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageListener;
import v9t9.gui.EmulatorGuiData;

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
	protected long nextUpdateTime;
	private GridData tabFolderData;
	private IPrinterImageEngine engine;

	/**
	 * @param engine 
	 * 
	 */
	public PrinterImageShell(IPrinterImageEngine engine) {
		this.engine = engine;
		newShell();
		engine.addListener(this);
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
		zoomIn.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_plus.gif"));
		zoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom * 2);
			}
		});
		final ToolItem zoomOut = new ToolItem(toolbar, SWT.PUSH);
		zoomOut.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_minus.gif"));
		zoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom / 2);
			}
		});
		final ToolItem zoomReset = new ToolItem(toolbar, SWT.PUSH);
		zoomReset.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_equal.gif"));
		zoomReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(1.0);
			}
		});
		final ToolItem newPage = new ToolItem(toolbar, SWT.PUSH);
		newPage.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/formfeed.gif"));
		newPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				engine.newPage();
			}
		});
		tabFolder.setTabHeight(28);
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
			Image image = pageImages.get(item.getData());
			if (image != null) {
				Rectangle bounds = image.getBounds();
				canvas.setSize(new Point((int) (bounds.width * zoom), (int) (bounds.height * zoom)));
	            canvas.layout();
			}
		}
		tabFolder.redraw();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#updated(java.lang.String)
	 */
	@Override
	public void updated(Object imageObj) {
		if (canvas == null) {
			newPage(imageObj);
		}
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (canvas == null || canvas.isDisposed())
					return;
				long now = System.currentTimeMillis();
				if (now >= nextUpdateTime) {
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
	public void newPage(final Object imageObj) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				
				Image image = (Image) imageObj;
				
				final int thisPage = ++pageNum;
				
				if (canvas == null || canvas.isDisposed()) {
					if (shell.isDisposed()) {
						newShell();
					}
					shell.open();
				}
				
//				tabFolderData.widthHint = (int) (image.getWidth() * zoom);
//				tabFolderData.heightHint = (int) (image.getHeight() * zoom);
				
				CTabItem item = new CTabItem(tabFolder, SWT.NONE | SWT.CLOSE);
				item.setData(thisPage);
				
				item.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						pageImages.remove(thisPage);
					}
				});
				
				pageImages.put(thisPage, image);
				
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
					Rectangle bounds = image.getBounds();
					while (zoom * bounds.height > shell.getDisplay().getBounds().height)
						zoom /= 2;
					
				}
				
				updatePageZooms();
				if (pageNum == 1)
					shell.pack();
				
				
				canvas.addPaintListener(new PaintListener() {
					
					@Override
					public void paintControl(PaintEvent e) {
						Image swtImage = pageImages.get(thisPage);
						if (swtImage == null) {
							return;
						}
						
						Rectangle bounds = canvas.getBounds();
						
						e.gc.setAntialias(SWT.ON);
						e.gc.setInterpolation(SWT.HIGH);
						Transform xfrm = new Transform(e.gc.getDevice());
						xfrm.scale((float) zoom, (float) zoom);
						e.gc.setTransform(xfrm);
						if (swtImage != null) {
							e.gc.drawImage(swtImage, 0, 0);
						} else {
							e.gc.fillRectangle(e.x, e.y, e.width, e.height);
						}
						e.gc.setTransform(null);
						xfrm.dispose();

						if (thisPage == pageImages.size() ) {
							double rowPerc = engine.getPageRowPercentage();
							double colPerc = engine.getPageColumnPercentage();
							int pixX = (int) (colPerc * bounds.width);
							int pixY = (int) (rowPerc * bounds.height);
							
							e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
							e.gc.drawLine(0, pixY, bounds.width, pixY);
							e.gc.drawLine(pixX, pixY, pixX, pixY + 16);
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
