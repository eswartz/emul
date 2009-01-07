/**
 * 
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.emulator.hardware.V9t9;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
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


	private BufferedImage surface;
	private Canvas canvas;

	private Rectangle updateRect;
	
	public AwtVideoRenderer() {
		updateRect = new Rectangle(0, 0, 0, 0);
		setCanvas(new ImageDataCanvas24Bit(0));
		//desiredWidth = (int)(zoomx * 256);
		//desiredHeight = (int)(zoomy * 192);
		this.canvas = new Canvas() {

			private static final long serialVersionUID = 8795221581767897631L;
			
			@Override
			public void paint(Graphics g) {
				Rectangle clipRect = g.getClipBounds();
				//System.out.println("Clippy rect: " + clipRect);
				doRedraw(g, 
						clipRect.x, clipRect.y, 
						clipRect.width, clipRect.height);
					
			}
			
			@Override
			public void update(Graphics g) {
				// do not clear background
				paint(g);
			}
			
		};
		canvas.setFocusTraversalKeysEnabled(false);
		canvas.setFocusable(true);
		canvas.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(HierarchyEvent e) {
				int width = canvas.getWidth();
				int height = canvas.getHeight();
				updateWidgetOnResize(width, height);
			}
		});
		
		//updateWidgetSizeForMode();
		doResizeToFit();
		

		V9t9.settingMonitorDrawing.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				synchronized (AwtVideoRenderer.this) {
					synchronized (vdpCanvas) {
						vdpCanvas.markDirty();
					}
					redraw();
				}
			}
			
		});
	}
	
	private void doResizeToFit()  {
		canvas.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		canvas.setSize(new Dimension(desiredWidth, desiredHeight));
		resizeTopLevel();
	}
	
	protected void resizeTopLevel() {
		Component comp = canvas;
		while (comp != null && !(comp instanceof Window)) {
		    comp = comp.getParent();
		}
		Window topLevel = (Window) comp;
		if (topLevel != null)
			topLevel.pack();
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
		return new Rectangle((int)((x - vdpCanvas.getXOffset()) * zoomx), (int)(y * zoomy), 
				Math.round(w * zoomx), Math.round(h * zoomy));
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		int x = (int)(physical.x / zoomx);
		int y = (int)(physical.y / zoomy);
		int ex = (int)((physical.x + physical.width + zoomx - .5) / zoomx);
		int ey = (int)((physical.y + physical.height + zoomy - .5) / zoomy);
		return new Rectangle(x + vdpCanvas.getXOffset(), y, 
				ex -x, ey -y ); 
	}

	/**
	 * Add a rectangle to the update region.  Does not immediately redraw.
	 * @param rect
	 */
	public void update(Rectangle rect) {
		if (updateRect.isEmpty())
			updateRect = rect;
		else
			updateRect.add(rect);
		isDirty = true;
		//System.out.println("Updating " + updateRect);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#redraw()
	 */
	public synchronized void redraw() {
		if (!isDirty)
			return;
		
		synchronized (vdpCanvas) {
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
				
				// if resizing, no point redrawing
				if (updateWidgetSizeForMode())
					return;
				
				Rectangle redrawPhys = logicalToPhysical(redrawRect);
				//System.out.println("Adding canvas " + redrawPhys);
				update(redrawPhys);
				
				//System.out.println("Redrawing " + updateRect);
				
				
				//BufferStrategy bufferStrategy = getBufferStrategy();
				
				//BufferStrategy bufferStrategy = canvas.getBufferStrategy();
				//doRedraw(bufferStrategy.getDrawGraphics(), updateRect.x, updateRect.y, 
				//		updateRect.width, updateRect.height);
				
				//bufferStrategy.show();
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
	}

	/**
	 * On a resize event (presumed to be user-driven), ensure that the 
	 * resolution preserves the aspect ratio and has a close-to-integral zoom 
	 * factor in each axis (we tolerate 0.5 for cases of 512-x or 384/424-y).
	 * The size the user sees depends on the current X and Y resolutions; 
	 * factor that in when determining the nearest zoom.
	 */
	protected void updateWidgetOnResize(int width, int height) {
		if (width <= 0 || height <= 0)
			return;
		
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
		if (vdpCanvas.getVisibleWidth() == 512)
			zoomx = zoom / 2.0f;
		else
			zoomx = zoom;
		
		if (zoomx != oldzoomx && zoomy != oldzoomy) {
			resizeWidgets();
		}
		
	}
	
	/**
	 * Resize the widgets if they need to change to fit the
	 * zoom level.
	 * @return true if resize queued
	 */
	protected boolean resizeWidgets() {
		Rectangle targetRect = logicalToPhysical(0, 0, vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		Point size = new Point(targetRect.width, targetRect.height);
		Point curSize = new Point(canvas.getWidth(), canvas.getHeight());
		if (curSize.x == size.x && curSize.y == size.y)
			return false;
		
		//manualResize = true;
		
		// resize to fit the required physical space -- but avoid oscillating if the zoom
		// is simply too large for the screen (where the WM might again resize it smaller)
		if (desiredWidth != size.x || desiredHeight != size.y) {
			desiredWidth = size.x;
			desiredHeight = size.y;
			
			doResizeToFit();
			return true;
		}
		return false;
	}

	/**
	 * If the X or Y resolutions changed, ensure the widget can show it correctly.
	 * Pretending that the window's size is the real physical monitor's size,
	 * adjust the zooms to keep the same physical resolution.  For the 192/212
	 * change in Y resolution, we assume there is "wiggle room" to resize the
	 * window.
	 * @return true if resize queued
	 */
	protected boolean updateWidgetSizeForMode() {
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
		
		return resizeWidgets();
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
		if (surface == null || surface.getWidth() != desiredWidth || surface.getHeight() != desiredHeight) {
			// ignore redraw request before we've decided our size
			if (desiredWidth == 0 || desiredHeight == 0)
				return;
			System.out.println("New BufferedImage");
			surface = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_BGR);
		}

		int destWidth = surface.getWidth();
		int destHeight = surface.getHeight();
		
		//System.out.println("surface size: " + surface.getWidth()+"/"+surface.getHeight()+"; " + x +"/"+y+"/"+width+"/"+height);
		DataBufferInt buffer = (DataBufferInt) surface.getRaster().getDataBuffer();
		int[] data = buffer.getData();
		if (true) {
			Rectangle logMax = physicalToLogical(new Rectangle(0, 0, destWidth, destHeight));
			//width += x; x= 0; 
			//height += y; y = 0;
			
			if (x + width > destWidth)
				width = destWidth - x;
			if (y + height > destHeight)
				height = destHeight - y;
			
			//System.out.println("x,y="+ x +"/"+y+"/"+width+"/"+height);
			if (width < 0 || height < 0)
				return;
			
			Rectangle logRect = physicalToLogical(new Rectangle(x, y, width, height));
			
			if (V9t9.settingMonitorDrawing.getBoolean()) {
				// modify a slightly larger area due to blending 
				if (logRect.x > 0) { logRect.x--; logRect.width++; }
				//if (logRect.y > 0) { logRect.y--; logRect.height++; }
				if (logRect.x + logRect.width + 2 <= logMax.width) logRect.width++;
				if (logRect.y + logRect.height + 2 <= logMax.height) logRect.height++;
			}
			
			Rectangle physRect = logicalToPhysical(logRect);
			x = physRect.x; y = physRect.y; width = physRect.width; height = physRect.height;
			/*
			if (logRect.x + logRect.width > vdpCanvas.getVisibleWidth())
				logRect.width = vdpCanvas.getVisibleWidth() - logRect.x;
			if (logRect.y + logRect.height > vdpCanvas.getHeight())
				logRect.height = vdpCanvas.getVisibleHeight() - logRect.y;
				*/
			int srcoffset = vdpCanvas.getDisplayAdjustOffset() 
			+ (logRect.y * vdpCanvas.getLineStride() + logRect.x * vdpCanvas.getPixelStride());
			//System.out.println("logRect = " + logRect + " x/y="+x+","+y+"; width/height="+width+","+height+"; srcoffset="+srcoffset);
			//System.out.println("srcoffset="+srcoffset+"; mod="+(srcoffset%3));
			
			synchronized (vdpCanvas) {
				V9t9RenderUtils.scaleImageToRGBA(
						data,
						vdpCanvas.getImageData().data, 
						srcoffset,
						logRect.width, logRect.height, vdpCanvas.getLineStride(),
						width, height, destWidth * 4,
						x, y, width, height);
			}
			//System.out.println("scaled");
			if (V9t9.settingMonitorDrawing.getBoolean()) {
				// modify a slightly larger area
				//if (logRect.x > 0) { logRect.x--; logRect.width++; }
				if (logRect.y > 0) { logRect.y--; logRect.height++; }
				if (logRect.y + logRect.height + 2 <= logMax.height) logRect.height++;
				Rectangle nphys = logicalToPhysical(logRect);
				
				V9t9RenderUtils.addNoiseRGBA(data,
						destWidth * 4 * nphys.y + 4 * nphys.x,
						nphys.width, nphys.height, destWidth * 4,
						logRect.width, logRect.height);
			}
			
			//width = destWidth - x;	height = destHeight - y;
			//g.setClip(x, y, width, height);
			//g.drawImage(surface, 0, 0, destWidth, destHeight, canvas);
			g.drawImage(
					surface,
					x, y, x + width, y + height,
					x, y, x + width, y + height,
					canvas);
		} else {
			synchronized (vdpCanvas) {
				V9t9RenderUtils.scaleImageToRGBA(
						data,
						vdpCanvas.getImageData().data, vdpCanvas.getDisplayAdjustOffset(),
						vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight(), vdpCanvas.getLineStride(),
						destWidth, destHeight, destWidth * 4,
						0, 0, destWidth, destHeight);
			}
			if (V9t9.settingMonitorDrawing.getBoolean()) {
				V9t9RenderUtils.addNoiseRGBA(data, 0,
						destWidth, destHeight, destWidth * 4,
						vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight());
			}
			g.drawImage(
					surface,
					x, y, x + width, y + height,
					x, y, x + width, y + height,
					canvas);
		}
		
		//window.getBufferStrategy().show();
		//while (canvas.imageUpdate(surface, ImageObserver.ALLBITS, x, y, width, height)) ;
	}

	public Component getAwtCanvas() {
		return canvas;
	}

	public void setFocus() {
		canvas.requestFocus();
	}

}
