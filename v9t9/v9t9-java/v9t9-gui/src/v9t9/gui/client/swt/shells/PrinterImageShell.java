/*
  PrinterImageShell.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.ejs.gui.common.SwtUtils;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageListener;
import v9t9.common.dsr.PrinterPage;
import v9t9.common.dsr.PrinterPage.Dot;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.settings.SettingSchema;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.common.BaseEmulatorWindow;
import v9t9.video.common.ImageUtils;

/**
 * @author ejs
 *
 */
public class PrinterImageShell implements IPrinterImageListener {
	private static Logger log = Logger.getLogger(PrinterImageShell.class);


	public static SettingSchema settingHorizDpi = new SettingSchema(
			ISettingsHandler.USER,
			"PrinterHorizontalDPI", 360); 

	public static SettingSchema settingVertDpi = new SettingSchema(
			ISettingsHandler.USER,
			"PrinterVerticalDPI", 360); 

	private Shell shell; 
	private CTabFolder tabFolder;
	
	// all for the current page; old pages are abandoned
	private Canvas canvas;
	/** scaled image of current page */
	private Image canvasImage; 
	
	private double zoom = 1.0;
	
	protected int pageNum;
	protected PrinterPage currentPage;
	
	protected int visiblePageNum;
	
	private Map<Integer, PrinterPage> indexToPages = new HashMap<Integer, PrinterPage>();
	
	
	protected long nextUpdateTime;
	protected long lastUpdateTime;
	private GridData tabFolderData;
	private IPrinterImageEngine engine;

	protected boolean contentsChanged;
	private IProperty horizDpi, vertDpi;

	private int lastCurrentIndex;

	private Image dotImage;
	private float dotImageSize;
	private float dotImageInk;


	private boolean saveImages;


	protected String saveImageExt;


	protected int saveImageType;


	protected String saveImageBase;


	private IEventNotifier notifier;

	public PrinterImageShell(IEventNotifier notifier, ISettingsHandler settings, IPrinterImageEngine engine) {
		this.notifier = notifier;
		log.info("creating shell for engine " + engine.getPrinterId());
		this.engine = engine;
		newShell();
		engine.addListener(this);
		
		horizDpi = settings.get(settingHorizDpi);
		vertDpi = settings.get(settingVertDpi);
		

		horizDpi.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				resizePage();
			}
		});
		vertDpi.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				resizePage();
			}
		});
	}
	
	protected void resizePage() { 
		if (canvasImage != null) {
			canvasImage.dispose();
			canvasImage = null;
		}
	}
	/**
	 * 
	 */
	private void newShell() {
		shell = new Shell(SWT.TOOL | SWT.TITLE | SWT.RESIZE | SWT.CLOSE);

		shell.setText("Printer Output");
		shell.setSize(400, 400);
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				currentPage = null;
				lastCurrentIndex = 0;
				indexToPages.clear();
				if (canvasImage != null)
					canvasImage.dispose();
				canvasImage = null;
				if (dotImage != null)
					dotImage.dispose();
				dotImage = null;
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
					if (canvasImage != null) {
						canvasImage.dispose();
						canvasImage = null;
					}
					contentsChanged = true;
				}
			}
		});
	
		ToolBar toolbar = new ToolBar(tabFolder, SWT.NONE);
		tabFolder.setTopRight(toolbar);
		
		tabFolder.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				populateMenu(e.x, e.y);
			}
		});

		final ToolItem zoomIn = new ToolItem(toolbar, SWT.PUSH);
		zoomIn.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_plus.gif"));
		zoomIn.setToolTipText("Zoom In");
		zoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom * 2);
			}
		});
		final ToolItem zoomOut = new ToolItem(toolbar, SWT.PUSH);
		zoomOut.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_minus.gif"));
		zoomOut.setToolTipText("Zoom Out");
		zoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom(zoom / 2);
			}
		});
		final ToolItem zoomReset = new ToolItem(toolbar, SWT.PUSH);
		zoomReset.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/zoom_equal.gif"));
		zoomReset.setToolTipText("Zoom to Fit");
		zoomReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoom = 1.0;
				int pixelHeight = 11 * vertDpi.getInt();
				while (zoom * pixelHeight > shell.getDisplay().getBounds().height)
					zoom /= 2;

				updatePageZooms();
			}
		});
		final ToolItem newPage = new ToolItem(toolbar, SWT.DROP_DOWN);
		newPage.setImage(EmulatorGuiData.loadImage(tabFolder.getDisplay(), "icons/formfeed.gif"));
		newPage.setToolTipText("Start new page");
		newPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail != SWT.ARROW)
					engine.newPage();
				else
					populateMenu(e.x, e.y);
			}
		});
		
		tabFolder.setTabHeight(28);
	}


	/**
	 * @param e
	 */
	protected void populateMenu(int x, int y) {
		Menu menu = new Menu(tabFolder);
		

		MenuItem inkItem = new MenuItem(menu, SWT.CASCADE);
		inkItem.setText("Ink Level");
		
		Menu inkMenu = new Menu(inkItem);
		
		addInkItem(inkMenu, "Low", 0.25);
		addInkItem(inkMenu, "Medium", 0.5);
		addInkItem(inkMenu, "High", 0.75);
		addInkItem(inkMenu, "Black", 1.0);
		
		inkItem.setMenu(inkMenu);


		MenuItem saveItem = new MenuItem(menu, SWT.CHECK);
		saveItem.setText("Save Pages...");
		if (saveImages) {
			saveItem.setSelection(true);
		}
		saveItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (saveImages) {
					shell.setText("Printer Output");
					saveImages = false;
					return;
				}
				
				String filePath = promptSaveFilename();
				if (filePath == null)
					return;

				int idx = filePath.lastIndexOf(File.separator)+1;
				int dotIdx = filePath.lastIndexOf('.');
				if (dotIdx > idx)
					idx = dotIdx;
				
				if (idx > 0) {
					saveImageExt = filePath.substring(idx+1);
					if (saveImageExt.equalsIgnoreCase("png"))
						saveImageType = SWT.IMAGE_PNG;
					else if (saveImageExt.equalsIgnoreCase("jpg") || saveImageExt.equalsIgnoreCase("jpeg"))
						saveImageType = SWT.IMAGE_JPEG;
					else if (saveImageExt.equalsIgnoreCase("tif") || saveImageExt.equalsIgnoreCase("tiff"))
						saveImageType = SWT.IMAGE_TIFF;
					else if (saveImageExt.equalsIgnoreCase("bmp"))
						saveImageType = SWT.IMAGE_BMP;
					else if (saveImageExt.equalsIgnoreCase("gif"))
						saveImageType = SWT.IMAGE_GIF;
					else
						saveImageType = SWT.IMAGE_PNG;
					
					saveImageBase = filePath.substring(0, idx);
				} else {
					saveImageExt = "png";
					saveImageType = SWT.IMAGE_PNG;
					saveImageBase = filePath;
				}
				
				saveImages = true;
				shell.setText("Printer Output (saving to " + saveImageBase + "." + saveImageExt + ")");
				
				BusyIndicator.showWhile(canvas.getDisplay(), new Runnable() {
					public void run() {
						
						Image prevCanvasImage = canvasImage;
						canvasImage = null;
						
						for (Map.Entry<Integer, PrinterPage> ent : indexToPages.entrySet()) {
							if (ent.getValue() != currentPage) {
								String filename = getPageFilename(ent.getKey(), ent.getValue());
								if (!new File(filename).exists()) {
									reprintPage(ent.getKey());
									savePageImage(filename, ent.getValue());
								}
							}
						}
						
						canvasImage = prevCanvasImage;
					}
				});
				
			}
		});
		

		
		MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
		deleteItem.setText("Delete older pages");
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getItemCount() > 0) {
					CTabItem last = tabFolder.getItem(tabFolder.getItemCount() - 1);
					for (CTabItem item : tabFolder.getItems()) {
						if (item != last)
							item.dispose();
					}
				}
				for (Iterator<Map.Entry<Integer, PrinterPage>> iterator = indexToPages.entrySet().iterator(); iterator
						.hasNext();) {
					Map.Entry<Integer, PrinterPage> ent = iterator.next();
					if (ent.getKey() != pageNum) {
						iterator.remove();
					}
				}
				
			}
		});

		SwtUtils.runMenu(null, x, y, menu);
	}

	/**
	 * @return
	 */
	protected String promptSaveFilename() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		if (saveImageBase != null)
			dialog.setFileName(saveImageBase + "." + saveImageExt);
		
		dialog.setFilterExtensions(new String[] { ".png", ".jpg", ".jpeg", ".gif", ".tiff", ".tif", ".bmp" });
		dialog.setFilterNames(new String[] { "PNG (*.png)", "JPEG (*.jpg)", "JPEG (*.jpeg)", "GIF (*.gif)", 
				"TIFF (*.tiff)", "TIFF (*.tif)", "BMP (*.bmp)" });
		dialog.setText("Enter path for saving image series.  Use %n for page number, %d for date/time, and %z for zoom level.");
		
		return dialog.open();
	}

	/**
	 * @param inkMenu
	 * @param label
	 * @param level
	 */
	private void addInkItem(Menu inkMenu, String label, final double level) {
		final IProperty inkLevel = engine.getInkLevel();
		MenuItem inkItem = new MenuItem(inkMenu, SWT.RADIO);
		inkItem.setText(label);
		inkItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inkLevel.setDouble(level);
			}
		});
		if (Math.abs(inkLevel.getDouble() - level) < 0.001) {
			inkItem.setSelection(true);
		}
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
			PrinterPage page = indexToPages.get(item.getData());
			if (page != null) {
				int width = (int) (page.getPageWidthInches() * horizDpi.getInt());
				int height = (int) (page.getPageHeightInches() * vertDpi.getInt());
				
				Point sz = new Point((int) (width * zoom), (int) (height * zoom));
				canvas.setSize(sz);
	            canvas.layout();
			}
		}
		if (canvasImage != null) {
			canvasImage.dispose();
			canvasImage = null;
		}
        //canvasImage = new Image(canvas.getDisplay(), sz.x, sz.y);
        contentsChanged = true;
		tabFolder.layout();
		tabFolder.redraw();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IPrinterImageListener#bytesProcessed(byte[])
	 */
	@Override
	public void bytesProcessed(byte[] bytes) {
		// ignore
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#updated(java.lang.String)
	 */
	@Override
	public void updated(final PrinterPage page) {
		contentsChanged = true;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (canvas == null || canvas.isDisposed() || tabFolder.getItemCount() == 0) {
					newPage(page);
				}
				long now = System.currentTimeMillis();
				lastUpdateTime = now;
				if (now >= nextUpdateTime) {
					canvas.redraw();
					nextUpdateTime = now + 500;
					
					queueRedrawSoon();
				}
			}
		});
	}

	/**
	 * 
	 */
	protected void queueRedrawSoon() {
		Display.getDefault().timerExec(100, new Runnable() {
			public void run() {
				if (canvas == null || canvas.isDisposed())
					return;
				
				// still printing?
				long now = System.currentTimeMillis();
				if (lastUpdateTime + 5000 > now) {
					nextUpdateTime = now + 500;
					if (lastUpdateTime + 1000 < now)
						engine.flushBuffer();
					canvas.redraw();
					queueRedrawSoon();
				}
			}
		});		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232HtmlListener#newPage()
	 */
	@Override
	public void newPage(final PrinterPage page) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (saveImages && currentPage != null) {
					String filename = getPageFilename(pageNum, currentPage);
					savePageImage(filename, currentPage);
				}
				createNewPage(page);
			}
		});
	}

	/**
	 * @return
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * @param page
	 */
	protected void createNewPage(PrinterPage page) {
		final int thisPage = ++pageNum;
		
		log.info("new page: " + thisPage + " for canvas: " + canvas);
		
		currentPage = page;
		lastCurrentIndex = 0;
		
		if (canvas == null || canvas.isDisposed()) {
			if (shell.isDisposed()) {
				newShell();
			}
			shell.open();
		}
		
		CTabItem item = new CTabItem(tabFolder, SWT.NONE | SWT.CLOSE);
		item.setData(thisPage);
		
		item.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				PrinterPage removed = indexToPages.remove(thisPage);
				if (removed == currentPage)
					currentPage = null;
			}
		});
		
		indexToPages.put(thisPage, page);
		
		ScrolledComposite scrolled = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolled.setAlwaysShowScrollBars(false);
		item.setControl(scrolled);
		GridLayoutFactory.swtDefaults().applyTo(scrolled);
		
		canvas = new Canvas(scrolled, SWT.BORDER | SWT.DOUBLE_BUFFERED);
		
		scrolled.setContent(canvas);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
		
		item.setText("Page " + thisPage);
		
		if (pageNum == 1) {
			zoom = 1.0;
			int pixelHeight = 11 * vertDpi.getInt();
			while (zoom * pixelHeight > shell.getDisplay().getBounds().height)
				zoom /= 2;
			
		}
		
		updatePageZooms();
		if (pageNum == 1)
			shell.pack();

		canvas.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				if (contentsChanged && thisPage == pageNum) {
					if (canvasImage == null) {
						BusyIndicator.showWhile(canvas.getDisplay(), new Runnable() {
							public void run() {
								updatePage();
								contentsChanged = false;
							}
						});
					} else {
						updatePage();
						contentsChanged = false;
					}
				}
				else if (canvasImage == null) {
					BusyIndicator.showWhile(canvas.getDisplay(), new Runnable() {
						public void run() {
							reprintPage(thisPage);
						}
					});
				}

				if (canvasImage != null && !canvasImage.isDisposed()) {
					Rectangle bounds = canvasImage.getBounds();
					
//					int cx1 = Math.max(0, e.x);
//					int cy1 = Math.max(0, e.y);
//					int cx2 = Math.min(bounds.width, e.x + e.width);
//					int cy2 = Math.min(bounds.height, e.y + e.height);
//					
//					e.gc.drawImage(canvasImage, cx1, cy1, cx2-cx1, cy2-cy1, cx1, cy1, cx2-cx1, cy2-cy1);
					
					// FIXME
					e.gc.drawImage(canvasImage, 0, 0);
					
					if (thisPage == pageNum) {
						// draw "print head" in appropriate position
						
						double rowPerc = engine.getPageRowPercentage();
						double colPerc = engine.getPageColumnPercentage();
						int pixX = (int) (colPerc * bounds.width);
						int pixY = (int) (rowPerc * bounds.height);
						
						e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
						e.gc.setLineWidth(4);
						e.gc.drawLine(0, pixY, bounds.width, pixY);
						e.gc.drawLine(pixX, pixY, pixX, pixY + 16);
					}

				}
			}
		});
		
		tabFolder.setSelection(item);
	}
	
	protected String getPageFilename(int number, PrinterPage page) {
		int zn = 1;
		double z = zoom;
		if (z > 1) {
			while (z > 1) {
				zn++;
				z /= 2;
			}
		} else if (z < 1) {
			while (z < 1) {
				zn--;
				z *= 2;
			}
		}

		String filename = saveImageBase + "." + saveImageExt;
		if (filename.contains("%n")) {
			filename = filename.replace("%n", String.format("%04d", number));
		}
		if (filename.contains("%z")) {
			filename = filename.replace("%z", String.format("%d", zn));
		}
		if (filename.contains("%d")) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
			filename = filename.replace("%d", format.format(page.getDate()));
		}
		
		if (!filename.contains("%"))
			filename = BaseEmulatorWindow.getUniqueFile(filename).getPath();

		return filename;
	}

	/**
	 * @param key
	 * @param value
	 */
	protected void savePageImage(String filename, PrinterPage page) {
		if (canvasImage == null || canvasImage.isDisposed())
			return;
		
		ImageLoader loader = new ImageLoader();
		
		try {
			loader.data = new ImageData[] { canvasImage.getImageData() };
			loader.save(filename, saveImageType);
			notifier.notifyEvent(this, Level.INFO, "Saved page to '" + filename + "'");
		} catch (Throwable e) {
			log.error("Failed to save printer image to '" + filename + "'", e);
			notifier.notifyEvent(this, Level.ERROR, "Failed to save printer image to '" + filename + "':\n\n" + e.getMessage());
		} 
	}


	/**
	 * @param thisPage
	 * @param e
	 */
	protected void reprintPage(final int thisPage) {
		//System.out.println(System.currentTimeMillis() + ": start");
		PrinterPage page = indexToPages.get(thisPage);
		if (page != null) {
			int pixelWidth = (int) (page.getPageWidthInches() * horizDpi.getInt() * zoom);
			int pixelHeight = (int)(page.getPageHeightInches() * vertDpi.getInt() * zoom);
			
			if (canvasImage != null && (canvasImage.getBounds().width < pixelWidth
					|| canvasImage.getBounds().height < pixelHeight)) {
				canvasImage.dispose();
				canvasImage = null;
			}
			if (canvasImage == null) {
				canvasImage = new Image(canvas.getDisplay(), pixelWidth, pixelHeight);
			}
		
			GC gc = new GC(canvasImage);
			gc.fillRectangle(canvasImage.getBounds());
			
			double xs = (double) pixelWidth / page.getHorizontalDots();
			double ys = (double) pixelHeight / page.getVerticalDots();
			
			double w1 = horizDpi.getInt() * zoom / 80.;
			double h1 = vertDpi.getInt() * zoom / 80.;
			
			for (float row : page.getRows()) {
				double y = row * ys;
				for (Map.Entry<Float, Float> dot : page.getDotsOnRow(row)) {
					double x = dot.getKey() * xs;
					dot(gc, x, y, dot.getValue(), w1, h1);
				}
			}
			gc.dispose();
		}
		//System.out.println(System.currentTimeMillis() + ": end");
	}

	/**
	 * @param thisPage
	 * @param e
	 */
	protected void updatePage() {
		//System.out.println(System.currentTimeMillis() + ": update start");
		PrinterPage page = currentPage;
		if (page != null) {
			int pixelWidth = (int) (page.getPageWidthInches() * horizDpi.getInt() * zoom);
			int pixelHeight = (int)(page.getPageHeightInches() * vertDpi.getInt() * zoom);
			
			if (canvasImage == null) {
				canvasImage = new Image(canvas.getDisplay(), pixelWidth, pixelHeight);
				lastCurrentIndex = 0;
			}
		
			GC gc = new GC(canvasImage);
			
			double xs = (double) pixelWidth / page.getHorizontalDots();
			double ys = (double) pixelHeight / page.getVerticalDots();
			
			double w1 = horizDpi.getInt() * zoom / 80.;
			double h1 = vertDpi.getInt() * zoom / 80.;
			
			for (Dot dot : page.getDotsFrom(lastCurrentIndex)) {
				double y = dot.y * ys;
				double x = dot.x * xs;
				dot(gc, x, y, dot.ink, w1, h1);
			}
			lastCurrentIndex = page.getDotCount();
			gc.dispose();
		}
		//System.out.println(System.currentTimeMillis() + ": update end");
	}


	private void dot(GC gc, final double x, final double y, final double inkLevel, double w1, double h1) {
		double s = Math.max(w1, h1);
		if (s <= 1) {
			if (x - (int) x < 0.5 && y - (int) y < 0.5)
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			else
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
			
			gc.drawPoint((int) x, (int) y);
			return;
		}
		
		if (dotImage == null || dotImage.isDisposed() || dotImageInk != inkLevel || dotImageSize != w1) {
			if (dotImage != null && !dotImage.isDisposed())
				dotImage.dispose();
			
			ImageData dotImageData = ImageUtils.createStandard32BitImageData(
					(int) Math.round(w1*2), (int) Math.round(h1*2));
			
			//dotImageData.transparentPixel = dotImageData.palette.getPixel(new RGB(255, 255, 255));
			dotImageData.setAlpha(0, 0, 0);
			Arrays.fill(dotImageData.alphaData, (byte) 0);
			
			dotImage = new Image(gc.getDevice(), dotImageData);
			dotImageInk = (float) inkLevel;
			dotImageSize = (float) s;
			
			GC dotGC = new GC(dotImage);
			
			dotGC.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			dotGC.setAlpha((int) (255 * inkLevel));
			dotGC.fillOval((int) Math.round(s/2 - w1/2.0), (int) Math.round(s/2 - h1/2.0), (int) Math.round(w1), (int) Math.round(h1));
		}

		gc.drawImage(dotImage, (int) Math.round(x - w1/2), (int) Math.round(y - h1/2));
	}
	
//	private void dot_(GC gc, final double x, final double y, final double inkLevel, double w1, double h1) {
//		if (w1 < 1 || h1 < 1) {
//			if (x - (int) x < 0.5 && y - (int) y < 0.5)
//				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
//			else
//				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
//			
//			gc.drawPoint((int) x, (int) y);
//		}
//		else {
//			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
//			//w1 = horizDpi / 60.; h1 = vertDpi / 60.;
//			gc.setAlpha((int) (255 * inkLevel));
////				gc.fillOval((int) Math.round(x - w1/2.0), (int) Math.round(y - h1/2.0), (int) w1, (int) h1);
//			gc.fillOval((int) Math.round(x - w1/2.0), (int) Math.round(y - h1/2.0), (int) Math.round(w1), (int) Math.round(h1));
//		}
//		
//	}

}
