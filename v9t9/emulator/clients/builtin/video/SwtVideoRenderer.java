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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements VideoRenderer, ICanvasListener {
	
	protected Shell shell;
	protected Canvas canvas;
	protected final ImageDataCanvas vdpCanvas;
	protected Color bg;

	// global zoom
	protected int zoom = 3;
	// zoom based on the resolution
	protected float zoomx = 3, zoomy = 3;
	protected boolean sizedToZoom = false;
	protected Image image;
	protected Rectangle updateRect;
	protected boolean isBlank;
	protected boolean wasBlank;
	protected boolean isDirty;
	protected long lastUpdateTime;
	private boolean busy;
	
	public SwtVideoRenderer(Display display, VdpCanvas canvas) {
		shell = new Shell(display);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		shell.setBounds(800,800,0,0);
		
		this.canvas = new Canvas(shell, SWT.NO_BACKGROUND);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.canvas.setLayoutData(gridData);
		this.canvas.setLayout(new FillLayout());

		this.vdpCanvas = (ImageDataCanvas) canvas;
		this.vdpCanvas.setListener(this);		
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
		
		
		shell.open();
	}

	protected void initWidgets() {
		
	}
	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x, logical.y, logical.width, logical.height);
	}
	
	protected Rectangle logicalToPhysical(int x, int y, int w, int h) {
		return new Rectangle((int)(x * zoomx), (int)(y * zoomy), (int)(w * zoomx), (int)(h * zoomy));
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		return new Rectangle((int)(physical.x / zoomx), (int)(physical.y / zoomy), 
				(int)(physical.width / zoomx), (int)(physical.height / zoomy));
	}

	public void redraw() {
		if (!isDirty)
			return;
		
		boolean becameBlank = vdpCanvas.isBlank() && !isBlank;
		isBlank = vdpCanvas.isBlank();
		
		Rectangle redrawRect_ = vdpCanvas.getDirtyRect();
		if (becameBlank)
			redrawRect_ = new Rectangle(0, 0, vdpCanvas.width, vdpCanvas.height);
			
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
	 * On a resize event, ensure that the resolution preserves the aspect ratio
	 * and has an integral zoom factor in each axis.
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
			zoomy = zoom;
			if (vdpCanvas.getWidth() == 512)
				zoomx = zoomy / 2.0f;
			else
				zoomx = zoomy;
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
		
		Rectangle targetRect = logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight());
		Point size = new Point(targetRect.width, targetRect.height);
		canvas.setSize(size);
		
		Rectangle trim = shell.computeTrim(0, 0, size.x, size.y);
		shell.setSize(trim.width, trim.height);
		
		//sizedToZoom = false;
	}

	/**
	 * If the X or Y resolutions changed, ensure the widget can show it correctly
	 * <p>
	 * Currently, just update for the Y resolution change.
	 */
	protected void updateWidgetSizeForMode() {
		// update size if needed
		updateZoomFactor();
		
		resizeWidgets();
	}

	protected boolean zoomWithin(int physsize, float zoom, int logSize) {
		return Math.abs(physsize / zoom - logSize) < 64;
	}
	protected void resizeToProportions(Point curSize, Rectangle targetRect) {
		if (sizedToZoom) {
			// avoid strangely growing or shrinking the window when y-res goes 192 <--> 212
			if (zoomWithin(curSize.y, zoomy, vdpCanvas.getHeight())) {
				zoomx = Math.round((zoom * 256) / vdpCanvas.getWidth());
				zoomy = Math.round((curSize.y + 64) / vdpCanvas.getHeight());
			} else {
				zoomx = Math.round((curSize.x + 255) / vdpCanvas.getWidth());
				zoomy = Math.round((curSize.y + vdpCanvas.getHeight()-1) / vdpCanvas.getHeight());
			}
			zoom = (int) zoomy;
		} else {
			sizedToZoom = true;
		}
		
		resizeWidgets();
	}

	protected void updateZoomFactor() {
		if (vdpCanvas.getWidth() > 256) {
			zoomx = (float) (zoom / 2.);
		} else {
			zoomx = zoom;
		}
		zoomy = zoom;
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
		lastUpdateTime = Long.MAX_VALUE;
		busy = true;
		long started = System.currentTimeMillis();
		ImageData imageData = vdpCanvas.getImageData();
		if (imageData != null) {
			
			Rectangle destRect = updateRect;
			
			destRect = destRect.intersection(logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight()));
			
			Rectangle imageRect = physicalToLogical(destRect);
			imageRect = vdpCanvas.mapVisible(imageRect);
			
			blitImageData(gc, imageData, destRect, imageRect);
			wasBlank = false;
			lastUpdateTime = System.currentTimeMillis() - started;
		}
		vdpCanvas.clearDirty();
		busy = false;
		
	}

	protected void blitImageData(GC gc, ImageData imageData, Rectangle destRect,
			Rectangle imageRect) {
		if (!isBlank) {
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
			
			//System.out.println(updateRect);
			synchronized (vdpCanvas) {
				image = new Image(shell.getDisplay(), imageData);
			}
			
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
}
