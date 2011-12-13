/**
 * 
 */
package v9t9.gui.client.swt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.timer.FastTimer;
import v9t9.canvas.video.ImageDataCanvas;
import v9t9.canvas.video.ImageDataCanvas24Bit;
import v9t9.canvas.video.VdpCanvasFactory;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.video.ICanvas;
import v9t9.common.video.ICanvasListener;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.gui.common.BaseEmulatorWindow;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements IVideoRenderer, ICanvasListener, ISwtVideoRenderer {
	
	protected Canvas canvas;
	protected ImageDataCanvas vdpCanvas;
	protected Color bg;

	protected Image image;
	protected Rectangle updateRect;
	protected boolean isDirty;
	protected long lastUpdateTime;
	protected boolean busy;
	private Shell shell;
	
	protected FixedAspectLayout fixedAspectLayout;
	private final IVdpChip vdp;
	protected final ISettingsHandler settings;
	protected IVdpCanvasRenderer vdpCanvasRenderer;
	private float zoomy;
	private float zoomx;
	private IProperty fullScreen;
	private FastTimer fastTimer;
	
	public SwtVideoRenderer(IMachine machine) {
		this.settings = machine.getSettings();
		this.vdp = machine.getVdp();
		fixedAspectLayout = new FixedAspectLayout(256, 192, 3.0, 3.0, 1., 5);
		zoomx = zoomy = 0.0f;
		fastTimer = new FastTimer("Video Renderer");
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#getVdpHandler()
	 */
	@Override
	public IVdpChip getVdpHandler() {
		return vdp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#dispose()
	 */
	@Override
	public void dispose() {
		if (vdpCanvasRenderer != null)
			vdpCanvasRenderer.dispose();
	}
	/**
	 * Create the control, and set it up with GridData.
	 * @param parent
	 * @return
	 */
	final public Control createControl(Composite parent, int flags) {
		this.shell = parent.getShell();
		this.canvas = createCanvasControl(parent, flags);
		canvas.setLayout(fixedAspectLayout);	
		
		createVdpCanvasHandler();
		this.updateRect = new Rectangle(0, 0, 0, 0);
		
		vdpCanvas.setListener(this);
		
		initWidgets();
		updateWidgetSizeForMode();
		
		// the canvas collects update regions in a big rect
		this.canvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (updateRect.isEmpty()) {
					updateRect.x = e.x;
					updateRect.y = e.y;
					updateRect.width = e.width;
					updateRect.height = e.height;
				}
				else {
					updateRect.add(new Rectangle(e.x, e.y, e.width, e.height));
				}
				//System.out.println(updateRect);
				if (e.count == 0) {
					//System.out.println(updateRect);
					repaint(e.gc, updateRect);
					updateRect.width = updateRect.height = 0;
				}
			}
			
		});
		
		canvas.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
				canvas.setFocus();
			}
		});
		
		canvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point size = ((Control)e.widget).getSize();
				System.out.println("Control resized to: " + size.x + "/" + size.y);

				updateWidgetOnResize();
			}
		});
		
		fullScreen = settings.get(BaseEmulatorWindow.settingFullScreen);
		fullScreen.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				fixedAspectLayout.setFullScreen(property.getBoolean());
			}
		});
		
		setupCanvas();
		return canvas;
	}

	protected Canvas createCanvasControl(Composite parent, int flags) {
		return new Canvas(parent, flags | getStyleBits());
	}

	public Control getControl() {
		return canvas;
	}
	protected void setupCanvas() {
		
	}

	protected int getStyleBits() {
		return SWT.NO_BACKGROUND;
	}

	protected void createVdpCanvasHandler() {
		vdpCanvas = new ImageDataCanvas24Bit();
		vdpCanvasRenderer = VdpCanvasFactory.createCanvasHandler(settings, this);
	}

	protected void initWidgets() {
		
	}
	
	/*
	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x // - vdpCanvas.getXOffset()
					, logical.y, logical.width, logical.height);
	}
	
	protected Rectangle logicalToPhysical(int x, int y, int w, int h) {
		double zoomx = fixedAspectLayout.getZoomX();
		double zoomy = fixedAspectLayout.getZoomY();
		return new Rectangle((int)(x * zoomx), (int)(y * zoomy), 
				(int) Math.round(w * zoomx), (int) Math.round(h * zoomy));
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		double zoomx = fixedAspectLayout.getZoomX();
		double zoomy = fixedAspectLayout.getZoomY();
		//System.out.printf("zoom: %g x %g%n", zoomx, zoomy);
		return new Rectangle((int)(physical.x / zoomx) //+ vdpCanvas.getXOffset() 
				, (int)(physical.y / zoomy), 
				(int)((physical.width + zoomx - .1) / zoomx), 
				(int)((physical.height + zoomy - .1) / zoomy));
	}
*/


	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x, logical.y, logical.width, logical.height);
	}
	
	protected Rectangle logicalToPhysical(int x, int y, int w, int h) {
		return new Rectangle((int)((x - vdpCanvas.getXOffset()) * zoomx), (int)(y * zoomy), 
				Math.round(w * zoomx), Math.round(h * zoomy));
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		int x = (int)(physical.x / zoomx);
		int y = (int)(physical.y / zoomy);
		int ex = (int)((physical.x + physical.width + zoomx - .5) / zoomx);
		int ey = (int)((physical.y + physical.height + zoomy - .5) / zoomy);
		return new Rectangle(x + vdpCanvas.getXOffset(), y, ex - x, ey - y ); 
	}

	public void redraw() {
		if (!isDirty || canvas == null)
			return;
		
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				synchronized (vdpCanvas) {
					if (canvas.isDisposed())
						return;
					
					//updateWidgetSizeForMode();
					
					try {
						doTriggerRedraw();
					} catch (Throwable t) {
						t.printStackTrace();
					}
					
					isDirty = false;
				}
			}
			
		});
	}
	
	protected void doTriggerRedraw() {
		if (vdpCanvas.getDirtyRect() != null) {
			Rectangle redrawPhys = logicalToPhysical(vdpCanvas.getDirtyRect());
			canvas.redraw(redrawPhys.x, redrawPhys.y, 
					redrawPhys.width, redrawPhys.height, false);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#reblit()
	 */
	@Override
	public void reblit() {
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				getControl().redraw();
			}
		});
	}
	
	/**
	 * If the X or Y resolutions changed, ensure the widget can show it correctly.
	 * Pretending that the window's size is the real physical monitor's size,
	 * adjust the zooms to keep the same physical resolution.  For the 192/212
	 * change in Y resolution, we assume there is "wiggle room" to resize the
	 * window.
	 */
	protected void updateWidgetSizeForMode() {
		/*
		int visibleWidth = getCanvas().getVisibleWidth();
		int visibleHeight = getCanvas().getVisibleHeight();

		fixedAspectLayout.setSize(visibleWidth, visibleHeight);
		if (visibleWidth > 256)
			visibleWidth /= 2;
		if (getCanvas().isInterlacedEvenOdd())
			visibleHeight /= 2;
		fixedAspectLayout.setAspect((double) visibleWidth / visibleHeight);
		
		
		//System.out.println("zoomx = " + zoomx + "; zoomy = " + zoomy);
		
		updateWidgetOnResize();
		*/
		int visibleWidth = getCanvas().getVisibleWidth();
		int visibleHeight = getCanvas().getVisibleHeight();
		fixedAspectLayout.setSize(visibleWidth, visibleHeight);
		if (visibleWidth > 256)
			visibleWidth /= 2;
		if (vdpCanvas.isInterlacedEvenOdd())
			visibleHeight /= 2;
		fixedAspectLayout.setAspect((double) visibleWidth / visibleHeight);

		updateWidgetOnResize();
	}

	/**
	 * 
	 */
	protected void updateWidgetOnResize() {
		if (canvas != null) {
			canvas.getParent().layout(true, true);
			zoomx = (float) fixedAspectLayout.getZoomX();
			zoomy = (float) fixedAspectLayout.getZoomY();
		}
		
	}

	protected boolean zoomWithin(int physsize, float zoom, int logSize) {
		return Math.abs(physsize / zoom - logSize) < 64;
	}
	
	public void canvasResized(ICanvas canvas) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateWidgetSizeForMode();
			}
		});
	}
	public void sync() {
		if (canvas == null)
			return;
			
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				if (canvas.isDisposed())
					return;
				canvas.update();
			}
		});
	}
	
	public Shell getShell() {
		return shell;
	}


	protected void repaint(GC gc, Rectangle updateRect) {
		if (canvas.isDisposed())
			return;
		
		if (!canvas.isVisible() || !shell.isVisible())
			return;

		lastUpdateTime = Long.MAX_VALUE;
		busy = true;
		long started = System.currentTimeMillis();
		doRepaint(gc, updateRect);

		lastUpdateTime = System.currentTimeMillis() - started;
		vdpCanvas.clearDirty();
		busy = false;
		
	}

	protected void doRepaint(GC gc, Rectangle updateRect) {
		if (vdpCanvas instanceof ImageDataCanvas) {
			ImageData imageData = ((ImageDataCanvas) vdpCanvas).getImageData();
			if (imageData != null) {
				
				Rectangle destRect = updateRect;
				
				destRect = destRect.intersection(logicalToPhysical(0, 0, 
						vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight()));
				
				Rectangle imageRect = physicalToLogical(destRect);
				destRect = logicalToPhysical(imageRect);
				
				blitImageData(gc, imageData, destRect, imageRect);
			}
		}
	}

	protected void blitImageData(GC gc, ImageData imageData, Rectangle destRect,
			Rectangle imageRect) {
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}
		
		//System.out.println(updateRect);
		synchronized (vdpCanvas) {
			image = new Image(shell.getDisplay(), imageData);
		}
		
		//System.out.println("imageRect="+imageRect+";destRect="+destRect);
		gc.drawImage(image, 
				imageRect.x, imageRect.y, 
				imageRect.width, imageRect.height, 
				destRect.x, destRect.y, 
				destRect.width, destRect.height);
	}

	public IVdpCanvas getCanvas() {
		return vdpCanvas;
	}
	

	public void canvasDirtied(ICanvas canvas) {
		//redraw();
		//System.out.println("!");
		isDirty = true;
	}

	protected Color allocColor(Color color, byte[] rgb) {
		if (color != null)
			color.dispose();
		if (rgb == null)
			rgb = vdpCanvas.getColorMgr().getRGB(0);
		
		return new Color(shell.getDisplay(), rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff);
	}
	
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public boolean isIdle() {
		return !busy;
	}

	public void setFocus() {
		if (!canvas.isDisposed()) {
			canvas.setFocus();
		}
	}

	public void saveScreenShot(File file) throws IOException {
		ImageLoader imageLoader = new ImageLoader();
		ImageData data = getScreenshotImageData();
		if (data == null)
			throw new IOException("Sorry, this renderer has no image to save");
		imageLoader.data = new ImageData[] { data };
		imageLoader.save(new FileOutputStream(file), SWT.IMAGE_PNG);
	}

	/**
	 * @return
	 */
	protected ImageData getScreenshotImageData() {
		if (image == null && vdpCanvas instanceof ImageDataCanvas) {
			ImageData imageData = ((ImageDataCanvas) vdpCanvas).getImageData();
			if (imageData != null) {
				image = new Image(shell.getDisplay(), imageData);
			}
		}
		if (image == null)
			return null;
		return image.getImageData();
	}

	public void addMouseEventListener(MouseListener listener) {
		canvas.addMouseListener(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#addMouseMotionListener(org.eclipse.swt.events.MouseMoveListener)
	 */
	public void addMouseMotionListener(MouseMoveListener listener) {
		canvas.addMouseMoveListener(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#isVisible()
	 */
	@Override
	public boolean isVisible() {
		if (Display.getDefault().isDisposed())
			return false;
		final boolean[] visible = { false };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				visible[0] = !shell.isDisposed() && shell.isVisible() && !shell.getMinimized();
			}
		});
		return visible[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.ISwtVideoRenderer#getCanvasHandler()
	 */
	@Override
	public IVdpCanvasRenderer getCanvasHandler() {
		return vdpCanvasRenderer;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IVideoRenderer#getTimer()
	 */
	@Override
	public FastTimer getFastTimer() {
		return fastTimer;
	}
}
