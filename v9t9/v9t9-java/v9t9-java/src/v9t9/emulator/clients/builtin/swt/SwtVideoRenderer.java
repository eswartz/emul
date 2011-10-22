/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.engine.VdpHandler;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements VideoRenderer, ICanvasListener, ISwtVideoRenderer {
	
	protected Canvas canvas;
	protected VdpCanvas vdpCanvas;
	protected Color bg;

	protected Image image;
	protected Rectangle updateRect;
	protected boolean isDirty;
	protected long lastUpdateTime;
	protected boolean busy;
	private Shell shell;
	
	protected FixedAspectLayout fixedAspectLayout;
	private final VdpHandler vdp;
	
	public SwtVideoRenderer(VdpHandler vdp) {
		this.vdp = vdp;
		fixedAspectLayout = new FixedAspectLayout(256, 192, 3.0, 3.0, 1., 5);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#getVdpHandler()
	 */
	@Override
	public VdpHandler getVdpHandler() {
		return vdp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#dispose()
	 */
	@Override
	public void dispose() {
		
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
		
		setCanvas(createVdpCanvas());
		this.updateRect = new Rectangle(0, 0, 0, 0);
		
		initWidgets();
		
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

	protected VdpCanvas createVdpCanvas() {
		return new ImageDataCanvas24Bit(0);
	}

	protected void initWidgets() {
		
	}
	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x /*- vdpCanvas.getXOffset()*/, logical.y, logical.width, logical.height);
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
		return new Rectangle((int)(physical.x / zoomx) /*+ vdpCanvas.getXOffset()*/, 
				(int)(physical.y / zoomy), 
				(int)((physical.width + zoomx - .1) / zoomx), 
				(int)((physical.height + zoomy - .1) / zoomy));
	}


	public void redraw() {
		if (!isDirty || canvas == null)
			return;
		
		final Rectangle redrawRect = vdpCanvas.getDirtyRect();
		
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				synchronized (vdpCanvas) {
					if (canvas.isDisposed())
						return;
					
					//updateWidgetSizeForMode();
					
					try {
						doTriggerRedraw(redrawRect);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					
					isDirty = false;
				}
			}
			
		});
	}
	
	protected void doTriggerRedraw(final Rectangle redrawRect) {
		Rectangle redrawPhys = logicalToPhysical(redrawRect);
		canvas.redraw(redrawPhys.x, redrawPhys.y, 
				redrawPhys.width, redrawPhys.height, false);
	}

	
	/**
	 * If the X or Y resolutions changed, ensure the widget can show it correctly.
	 * Pretending that the window's size is the real physical monitor's size,
	 * adjust the zooms to keep the same physical resolution.  For the 192/212
	 * change in Y resolution, we assume there is "wiggle room" to resize the
	 * window.
	 */
	protected void updateWidgetSizeForMode() {
		int visibleWidth = getCanvas().getVisibleWidth();
		int visibleHeight = getCanvas().getVisibleHeight();
		fixedAspectLayout.setSize(visibleWidth, visibleHeight);
		if (visibleWidth > 256)
			visibleWidth /= 2;
		if (vdpCanvas.isInterlacedEvenOdd())
			visibleHeight /= 2;
		fixedAspectLayout.setAspect((double) visibleWidth / visibleHeight);
		if (canvas != null)
			canvas.getParent().layout(true);
	}

	protected boolean zoomWithin(int physsize, float zoom, int logSize) {
		return Math.abs(physsize / zoom - logSize) < 64;
	}
	
	public void canvasResized(VdpCanvas canvas) {
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

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public synchronized void setZoom(int zoom) {
		synchronized (vdpCanvas) {
			fixedAspectLayout.setZoomX(zoom);
			fixedAspectLayout.setZoomY(zoom);
			isDirty = true;
			updateWidgetSizeForMode();
			redraw();
		}
	}
	
	public int getZoom() {
		return (int) fixedAspectLayout.getZoomX();
	}

	public void canvasDirtied(VdpCanvas canvas) {
		//redraw();
		//System.out.println("!");
		isDirty = true;
	}

	protected Color allocColor(Color color, byte[] rgb) {
		if (color != null)
			color.dispose();
		if (rgb == null)
			rgb = vdpCanvas.getRGB(0);
		
		return new Color(shell.getDisplay(), rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff);
	}
	
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public boolean isIdle() {
		return !busy;
	}

	public void setCanvas(VdpCanvas vdpCanvas) {
		this.vdpCanvas = vdpCanvas;
		this.vdpCanvas.setListener(this);
		updateWidgetSizeForMode();
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

}
