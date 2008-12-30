/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements VideoRenderer, ICanvasListener {
	
	protected Canvas canvas;
	protected VdpCanvas vdpCanvas;
	protected Color bg;

	protected Image image;
	protected Rectangle updateRect;
	protected boolean isBlank;
	protected boolean wasBlank;
	protected boolean isDirty;
	protected long lastUpdateTime;
	protected boolean busy;
	private Shell shell;
	
	// global zoom
	protected int zoom = 3;
	// zoom based on the resolution
	protected float zoomx = 3, zoomy = 3;
	protected boolean sizedToZoom = false;
	private GridData canvasLayoutData;
	
	public SwtVideoRenderer() {
	}

	/**
	 * Create the control, and set it up with GridData.
	 * @param parent
	 * @return
	 */
	public Control createControl(Composite parent) {
		this.shell = parent.getShell();
		this.canvas = new Canvas(parent, getStyleBits());
		this.canvas.setLayout(new FillLayout());
		canvasLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasLayoutData.minimumHeight = 256;
		canvasLayoutData.minimumWidth = 192;
		canvasLayoutData.widthHint = 256 * 3;
		canvasLayoutData.heightHint = 192 * 3;
		canvas.setLayoutData(canvasLayoutData);
		
		setCanvas(createCanvas());
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
		
		this.canvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateWidgetOnResize();
			}
		});
		
		setupCanvas();
		return canvas;
	}

	protected void setupCanvas() {
		
	}

	protected int getStyleBits() {
		return SWT.NO_BACKGROUND;
	}

	protected VdpCanvas createCanvas() {
		return new ImageDataCanvas24Bit();
	}

	protected void initWidgets() {
		
	}
	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x, logical.y, logical.width, logical.height);
	}
	
	protected Rectangle logicalToPhysical(int x, int y, int w, int h) {
		return new Rectangle((int)(x * zoomx), (int)(y * zoomy), Math.round(w * zoomx), Math.round(h * zoomy));
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		return new Rectangle((int)(physical.x / zoomx), (int)(physical.y / zoomy), 
				(int)((physical.width + zoomx - 1) / zoomx), 
				(int)((physical.height + zoomy - 1) / zoomy));
	}

	public void redraw() {
		if (!isDirty)
			return;
		
		boolean becameBlank = vdpCanvas.isBlank() && !isBlank;
		isBlank = vdpCanvas.isBlank();
		
		Rectangle redrawRect_ = vdpCanvas.getDirtyRect();
		if (becameBlank)
			redrawRect_ = new Rectangle(0, 0, vdpCanvas.width, vdpCanvas.height);
		
		if (vdpCanvas.isInterlacedEvenOdd()) {
			redrawRect_.y *= 2;
			redrawRect_.height *= 2;
		}
		
		if (redrawRect_ != null) {
			final Rectangle redrawRect = redrawRect_;
			
			Display.getDefault().asyncExec(new Runnable() {
	
				public void run() {
					synchronized (vdpCanvas) {
						if (canvas.isDisposed())
							return;
						
						updateWidgetSizeForMode();
						
						Rectangle redrawPhys = logicalToPhysical(redrawRect);
						canvas.redraw(redrawPhys.x, redrawPhys.y, 
								redrawPhys.width, redrawPhys.height, false);
						
						isDirty = false;
					}
				}
				
			});
			
		}
	}
	
	/**
	 * On a resize event (presumed to be user-driven), ensure that the 
	 * resolution preserves the aspect ratio and has a close-to-integral zoom 
	 * factor in each axis (we tolerate 0.5 for cases of 512-x or 384/424-y).
	 * The size the user sees depends on the current X and Y resolutions; 
	 * factor that in when determining the nearest zoom.
	 */
	protected void updateWidgetOnResize() {
		float oldzoomx = zoomx;
		float oldzoomy = zoomy;
		
		if (sizedToZoom) {
			Point curSize = canvas.getSize();
			zoom = (int) (curSize.y + 64) / vdpCanvas.getHeight();
			
			if (zoom == 0)
				zoom = 1;
			
			if (vdpCanvas.isInterlacedEvenOdd())
				zoomy = zoom / 2.0f;
			else
				zoomy = zoom;
			if (vdpCanvas.getVisibleWidth() == 512)
				zoomx = zoom / 2.0f;
			else
				zoomx = zoom;
		} else {
			sizedToZoom = true;
		}
		
		if (zoomx != oldzoomx && zoomy != oldzoomy) {
			getShell().getDisplay().asyncExec(new Runnable() {
	
				public void run() {
					resizeWidgets();
				}
			
			});
		}
		
	}
	protected void resizeWidgets() {
		if (canvas.isDisposed())
			return;
		
		Rectangle targetRect = logicalToPhysical(0, 0, vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		Point size = new Point(targetRect.width, targetRect.height);
		Point curSize = canvas.getSize();
		if (curSize.x == size.x && curSize.y == size.y)
			return;
		
		//manualResize = true;
		
		// resize to fit the required physical space -- but avoid oscillating if the zoom
		// is simply too large for the screen (where the WM might again resize it smaller)
		if (canvasLayoutData.widthHint != size.x || canvasLayoutData.heightHint != size.y) {
		//Rectangle screenSize = shell.getDisplay().getClientArea();
		//Rectangle trim = canvas.getParent().computeTrim(0, 0, size.x, size.y);
		//if (trim.width - trim.x <= screenSize.width  && trim.height - trim.y <= screenSize.height) { 
			//autoResize = true;
			canvasLayoutData.widthHint = size.x;
			canvasLayoutData.heightHint = size.y;
			
			canvas.setSize(size);
			canvas.getShell().pack();
			//canvas.getParent().setSize(trim.width, trim.height);
		}			
		//sizedToZoom = false;
	}

	/**
	 * If the X or Y resolutions changed, ensure the widget can show it correctly.
	 * Pretending that the window's size is the real physical monitor's size,
	 * adjust the zooms to keep the same physical resolution.  For the 192/212
	 * change in Y resolution, we assume there is "wiggle room" to resize the
	 * window.
	 */
	protected void updateWidgetSizeForMode() {
		// update size if needed
		if (vdpCanvas.getVisibleWidth() > 256) {
			zoomx = zoom / 2.f;
		} else {
			zoomx = zoom;
		}
		if (vdpCanvas.isInterlacedEvenOdd()) {
			zoomy = zoom / 2.f;
		} else {
			zoomy = zoom;
		}
		
		resizeWidgets();
	}

	protected boolean zoomWithin(int physsize, float zoom, int logSize) {
		return Math.abs(physsize / zoom - logSize) < 64;
	}
	protected void resizeToProportions(Point curSize, Rectangle targetRect) {
		if (sizedToZoom) {
			// avoid strangely growing or shrinking the window when y-res goes 192 <--> 212
			if (zoomWithin(curSize.y, zoomy, vdpCanvas.getVisibleHeight())) {
				zoomx = Math.round((zoom * 256) / vdpCanvas.getVisibleWidth());
				zoomy = Math.round((curSize.y + 64) / vdpCanvas.getVisibleHeight());
			} else {
				zoomx = Math.round((curSize.x + 255) / vdpCanvas.getVisibleWidth());
				zoomy = Math.round((curSize.y + vdpCanvas.getVisibleHeight()-1) / vdpCanvas.getVisibleHeight());
			}
			zoom = (int) zoomy;
		} else {
			sizedToZoom = true;
		}
		
		resizeWidgets();
	}

	public void canvasResized(VdpCanvas canvas) {
		//needResize = true;
	}
	public void sync() {
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

	/*
	public void updateList(RedrawBlock[] blocks, int count) {
		//final Region region = new Region(shell.getDisplay());
		Rectangle redrawRect_ = null;
		int nblocks = 0;
		for (int idx = 0; idx < count; idx++) {
			final RedrawBlock block = blocks[idx];
			//region.add(new Rectangle(block.c, block.r, block.w, block.h));
			Rectangle blockRect = new Rectangle(block.c, block.r, block.w, block.h);
			if (redrawRect_ == null)
				redrawRect_ = blockRect;
			else
				redrawRect_ = redrawRect_.union(blockRect);
			nblocks++;
		}
		
		// queue redraw
		if (redrawRect_ != null) {
			//final Rectangle redrawRect = region.getBounds();
			final Rectangle redrawRect = redrawRect_;
			//region.dispose();
			
			//if (nblocks > 0) 
			//	System.out.println("Redrew " + nblocks + " blocks to " + redrawRect);
			
			Display.getDefault().asyncExec(new Runnable() {
	
				public void run() {
					if (canvas.isDisposed())
						return;
					canvas.redraw(redrawRect.x * zoom, redrawRect.y * zoom, 
							redrawRect.width * zoom, redrawRect.height * zoom, true);
				}
				
			});
		}
	}*/

	protected void repaint(GC gc, Rectangle updateRect) {
		if (canvas.isDisposed())
			return;
		
		if (!canvas.isVisible() || !shell.isVisible())
			return;

		lastUpdateTime = Long.MAX_VALUE;
		busy = true;
		long started = System.currentTimeMillis();
		doRepaint(gc, updateRect);
		wasBlank = false;
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
				imageRect = vdpCanvas.mapVisible(imageRect);
				destRect = logicalToPhysical(imageRect);
				
				blitImageData(gc, imageData, destRect, imageRect);
			}
		}
	}

	protected void blitImageData(GC gc, ImageData imageData, Rectangle destRect,
			Rectangle imageRect) {
		if (true || !isBlank) {
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
		} else {
			if (wasBlank)
				return;
			bg = allocColor(bg, vdpCanvas.getClearRGB());
			gc.setBackground(bg);
			gc.fillRectangle(updateRect);
			wasBlank = true;
		}
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
		isDirty = true;
		redraw();
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

	public Control getWidget() {
		return canvas;
	}

	public void setCanvas(VdpCanvas vdpCanvas) {
		this.vdpCanvas = vdpCanvas;
		this.vdpCanvas.setListener(this);
		updateWidgetSizeForMode();
	}
	


}
