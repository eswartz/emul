/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.nio.ByteBuffer;

import v9t9.emulator.clients.builtin.AwtWindow;
import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.jni.v9t9render.utils.V9t9RenderUtils;

/**
 * @author Ed
 *
 */
public class AwtVideoRenderer implements VideoRenderer, ICanvasListener {

	static {
		System.loadLibrary("v9t9renderutils");
	}
	private ImageDataCanvas vdpCanvas;

	// global zoom
	protected int zoom = 3;
	// zoom based on the resolution
	protected float zoomx = 3, zoomy = 3;

	private boolean isDirty;

	private boolean isBlank;

	private int desiredWidth;

	private int desiredHeight;

	private final AwtWindow window;

	private BufferedImage surface;
	private Canvas canvas;

	private Rectangle updateRect;
	
	public AwtVideoRenderer(AwtWindow window) {
		this.window = window;
		updateRect = new Rectangle(0, 0, 0, 0);
		setCanvas(new ImageDataCanvas24Bit());
		desiredWidth = (int)(zoomx * 256);
		desiredHeight = (int)(zoomy * 192);
		this.canvas = new Canvas() {

			private static final long serialVersionUID = 8795221581767897631L;
			
			@Override
			public void paint(Graphics g) {
				Rectangle clipRect = g.getClipBounds();
				doRedraw(g, 
						clipRect.x, clipRect.y, 
						clipRect.width, clipRect.height);
					
			}
			
			@Override
			public void update(Graphics g) {
				paint(g);
			}
			
		};
		canvas.setFocusTraversalKeysEnabled(false);
		canvas.setFocusable(true);
		//canvas.setIgnoreRepaint(true);
		canvas.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(HierarchyEvent e) {
				updateWidgetOnResize(canvas.getWidth(), canvas.getHeight());
			}
		});
		
		doResizeToFit();
		
	}
	
	private void doResizeToFit()  {
		canvas.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		canvas.setSize(new Dimension(desiredWidth, desiredHeight));

		//canvas.setSize(new Dimension(desiredWidth, desiredHeight));
		window.setDesiredScreenSize(desiredWidth, desiredHeight);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#getCanvas()
	 */
	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#setCanvas(v9t9.emulator.clients.builtin.video.VdpCanvas)
	 */
	public void setCanvas(VdpCanvas vdpCanvas) {
		this.vdpCanvas = (ImageDataCanvas) vdpCanvas;
		this.vdpCanvas.setListener(this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#getLastUpdateTime()
	 */
	public long getLastUpdateTime() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#isIdle()
	 */
	public boolean isIdle() {
		return true;
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

	/**
	 * Add a rectangle to the update region.  Does not immediately redraw.
	 * @param rect
	 */
	public void update(Rectangle rect) {
		updateRect.add(rect);
		isDirty = true;
		//System.out.println("Updating " + updateRect);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#redraw()
	 */
	public void redraw() {
		if (!isDirty)
			return;
		
		boolean becameBlank = vdpCanvas.isBlank() && !isBlank;
		isBlank = vdpCanvas.isBlank();
		
		org.eclipse.swt.graphics.Rectangle dirtyRect = vdpCanvas.getDirtyRect(); 
		Rectangle redrawRect_ = new Rectangle(dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
		if (becameBlank)
			redrawRect_ = new Rectangle(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight());
		
		if (vdpCanvas.isInterlacedEvenOdd()) {
			redrawRect_.y *= 2;
			redrawRect_.height *= 2;
		}
		
		if (redrawRect_ != null) {
			final Rectangle redrawRect = redrawRect_;
			
			updateWidgetSizeForMode();
			
			Rectangle redrawPhys = logicalToPhysical(redrawRect);
			//System.out.println("Adding canvas " + redrawPhys);
			update(redrawPhys);
			
			//System.out.println("Redrawing " + updateRect);
			
			BufferStrategy bufferStrategy = window.getBufferStrategy();
			doRedraw(bufferStrategy.getDrawGraphics(), updateRect.x, updateRect.y, 
					updateRect.width, updateRect.height);
			
			bufferStrategy.show();
			canvas.repaint(updateRect.x, updateRect.y, 
					updateRect.width, updateRect.height);
			
			updateRect.width = 0;
			updateRect.height = 0;
			updateRect.x = 0;
			updateRect.y = 0;
			isDirty = false;
			vdpCanvas.clearDirty();

		}
	}

	/**
	 * On a resize event (presumed to be user-driven), ensure that the 
	 * resolution preserves the aspect ratio and has a close-to-integral zoom 
	 * factor in each axis (we tolerate 0.5 for cases of 512-x or 384/424-y).
	 * The size the user sees depends on the current X and Y resolutions; 
	 * factor that in when determining the nearest zoom.
	 */
	protected void updateWidgetOnResize(int width, int height) {
		float oldzoomx = zoomx;
		float oldzoomy = zoomy;
		
		Point curSize = new Point(width, height);
		zoom = (int) (curSize.y + 64) / vdpCanvas.getHeight();
		
		if (zoom == 0)
			zoom = 1;
		
		if (vdpCanvas.isInterlacedEvenOdd())
			zoomy = zoom / 2.0f;
		else
			zoomy = zoom;
		if (vdpCanvas.getWidth() == 512)
			zoomx = zoom / 2.0f;
		else
			zoomx = zoom;
		
		if (zoomx != oldzoomx && zoomy != oldzoomy) {
			resizeWidgets();
		}
		
	}
	protected void resizeWidgets() {
		if (surface == null)
			return;
		
		Rectangle targetRect = logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getVisibleHeight());
		Point size = new Point(targetRect.width, targetRect.height);
		Point curSize = new Point(canvas.getWidth(), canvas.getHeight());
		if (curSize.x == size.x && curSize.y == size.y)
			return;
		
		//manualResize = true;
		
		// resize to fit the required physical space -- but avoid oscillating if the zoom
		// is simply too large for the screen (where the WM might again resize it smaller)
		if (desiredWidth != size.x || desiredHeight != size.y) {
			desiredWidth= size.x;
			desiredHeight= size.y;
			
			doResizeToFit();
		}
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
		if (vdpCanvas.getWidth() > 256) {
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
	

	public void canvasResized(VdpCanvas canvas) {
		//needResize = true;
		isDirty = true;
	}
	
	public void canvasDirtied(VdpCanvas canvas) {
		//redraw();
		//System.out.println("!");
		isDirty = true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#setZoom(int)
	 */
	public void setZoom(int zoom) {
		isDirty = true;
		this.zoom = zoom;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#sync()
	 */
	public void sync() {
		
	}
	
	protected synchronized void doRedraw(Graphics g, int x, int y, int width, int height) {
		if (surface == null || surface.getWidth() != desiredHeight || surface.getHeight() != desiredHeight) {
			surface = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_BGR);
		}

		int destWidth = surface.getWidth();
		int destHeight = surface.getHeight();
		
		//System.out.println(
		//		"surface size: " + surface.getWidth()+"/"+surface.getHeight()+"; " + x +"/"+y+"/"+width+"/"+height);
		DataBufferInt buffer = (DataBufferInt) surface.getRaster().getDataBuffer();
		synchronized (vdpCanvas) {
			V9t9RenderUtils.scaleImageToRGBA(
					buffer.getData(),
					vdpCanvas.getImageData().data,
					vdpCanvas.getWidth(), vdpCanvas.getHeight(), vdpCanvas.getLineStride(),
					destWidth, destHeight,
					0, 0, destWidth, destHeight);
		}
		V9t9RenderUtils.addNoiseRGBA(buffer.getData(), 
				destWidth, destHeight, destWidth * 4,
				vdpCanvas.getWidth(), vdpCanvas.getHeight());

		g.drawImage(
					surface,
					x, y, x + width, y + height,
					x, y, x + width, y + height,
					canvas);
		//window.getBufferStrategy().show();
		//while (canvas.imageUpdate(surface, ImageObserver.ALLBITS, x, y, width, height)) ;
	}

	public Component getAwtCanvas() {
		return canvas;
	}

}
