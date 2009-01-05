/**
 * 
 */
package v9t9.emulator.clients.builtin.sdl;

import java.nio.ByteBuffer;

import org.eclipse.swt.graphics.Point;

import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.jni.v9t9render.utils.V9t9RenderUtils;

/**
 * @author Ed
 *
 */
public class SdlVideoRenderer implements VideoRenderer, ICanvasListener {

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

	protected int desiredWidth;

	protected int desiredHeight;

	protected SDLSurface surface;

	private SDLRect updateRect;

	private boolean resizePending;

	private SdlWindow sdlWindow;
	
	public SdlVideoRenderer() throws SDLException {
		if (SDLMain.wasInit(SDLMain.SDL_INIT_VIDEO) !=  SDLMain.SDL_INIT_VIDEO)
			SDLMain.init(SDLMain.SDL_INIT_VIDEO);
		updateRect = new SDLRect(0, 0, 0, 0);
		setCanvas(new ImageDataCanvas24Bit(16));
		desiredWidth = (int)(zoomx * 256);
		desiredHeight = (int)(zoomy * 192);
		getRenderingSurface();
		
	}
	
	protected synchronized void getRenderingSurface() throws SDLException {
		long flags = SDLVideo.SDL_SWSURFACE /*| SDLVideo.SDL_RESIZABLE*/;
		
		/*
		try {
			surface = SDLVideo.createRGBSurface(
					flags,
					desiredWidth, desiredHeight, 
					24, 0xFF, 0xFF00, 0xFF0000, 0);
		} catch (SDLException e) {
			System.err.println("Could not acquire hardware surface");
		}
		if (surface == null)*/
		if (surface != null && surface.getSwigSurface() != null)
			surface.freeSurface();
		
		{
			surface = SDLVideo.createRGBSurface(
					flags,
					desiredWidth, 
					desiredHeight,
					24, 0xFF, 0xFF00, 0xFF0000, 0);
		}
		resizeTopLevel();
		
	}
	protected void resizeTopLevel() {
		resizePending = true;	
	}
	
	public boolean isResizePending() {
		return resizePending;
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

	protected SDLRect logicalToPhysical(SDLRect logical) {
		return logicalToPhysical(logical.x, logical.y, logical.width, logical.height);
	}
	
	protected SDLRect logicalToPhysical(int x, int y, int w, int h) {
		return new SDLRect((int)(x * zoomx), (int)(y * zoomy), Math.round(w * zoomx), Math.round(h * zoomy));
	}
	
	protected SDLRect physicalToLogical(SDLRect physical) {
		return new SDLRect((int)(physical.x / zoomx), (int)(physical.y / zoomy), 
				(int)((physical.width + zoomx - 1) / zoomx), 
				(int)((physical.height + zoomy - 1) / zoomy));
	}

	/**
	 * Add a rectangle to the update region.  Does not immediately redraw.
	 * @param rect
	 */
	public void update(SDLRect rect) {
		if (updateRect.width == 0 || updateRect.height == 0) {
			updateRect.x = rect.x;
			updateRect.y = rect.y;
			updateRect.width = rect.width;
			updateRect.height = rect.height;
		}
		else {
			SdlUtils.addRect(updateRect, rect);
		}
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
		
		SDLRect redrawRect_ = SdlUtils.convertRect(vdpCanvas.getDirtyRect());
		if (becameBlank)
			redrawRect_ = new SDLRect(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight());
		
		if (vdpCanvas.isInterlacedEvenOdd()) {
			redrawRect_.y *= 2;
			redrawRect_.height *= 2;
		}
		
		if (redrawRect_ != null) {
			final SDLRect redrawRect = redrawRect_;
			
			updateWidgetSizeForMode();
			
			SDLRect redrawPhys = logicalToPhysical(redrawRect);
			//System.out.println("Adding canvas " + redrawPhys);
			update(redrawPhys);
			
			//System.out.println("Redrawing " + updateRect);
			sdlRedraw(updateRect.x, updateRect.y, 
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
	public void updateWidgetOnResize(int width, int height) {
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
			try {
				getRenderingSurface();
			} catch (SDLException e) {
				e.printStackTrace();
			}
		}
		
	}
	protected void resizeWidgets() {
		if (surface == null)
			return;
		
		SDLRect targetRect = logicalToPhysical(0, 0, vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		Point size = new Point(targetRect.width, targetRect.height);
		Point curSize = new Point(surface.getWidth(), surface.getHeight());
		if (curSize.x == size.x && curSize.y == size.y)
			return;
		
		//manualResize = true;
		
		// resize to fit the required physical space -- but avoid oscillating if the zoom
		// is simply too large for the screen (where the WM might again resize it smaller)
		if (desiredWidth != size.x || desiredHeight != size.y) {
			desiredWidth= size.x;
			desiredHeight= size.y;
			
			try {
				getRenderingSurface();
			} catch (SDLException e) {
				desiredWidth = curSize.x;
				desiredHeight = curSize.y;
				try {
					getRenderingSurface();
				} catch (SDLException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
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
	
	protected void sdlRedraw(int x, int y, int width, int height) {
		if (surface == null || surface.getSwigSurface() == null)
			return;
		int destWidth = surface.getWidth();
		int destHeight = surface.getHeight();
		byte[] scaledData = new byte[destWidth * destHeight * 3];
		V9t9RenderUtils.scaleImage(
				scaledData,
				vdpCanvas.getImageData().data, vdpCanvas.getDisplayAdjustOffset(),
				vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight(),
				vdpCanvas.getLineStride(),
				destWidth, destHeight, destWidth * 3,
				x, y, width, height);
				
		V9t9RenderUtils.addNoise(scaledData, 0, destWidth, destHeight,
				destWidth * 3, vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight());
		
		//System.out.println("buffer size: " + buffer.limit()+"; scaled data size: " + scaledData.data.length);
		ByteBuffer buffer = surface.getPixelData();
		buffer.clear();
		buffer.put(scaledData);
		try {
			surface.updateRect(x, y, width, height);
		} catch (SDLException e) {
			e.printStackTrace();
		}
		
		// TODO: this expose needs to happen somewhere but shouldn't depend on sdlWindow like this
		if (sdlWindow != null) {
			try {
				sdlWindow.handleExpose(x, y, width, height);
			} catch (SDLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setSdlWindow(SdlWindow window) {
		this.sdlWindow = window;
	}

	public SDLSurface getSurface() {
		return surface;
	}

	public void setFocus() {
		
	}

	public int getDesiredWidth() {
		return desiredWidth;
	}
	public int getDesiredHeight() {
		return desiredHeight;
	}

	public void setDesiredScreenSize(int i, int j) {
		desiredWidth = i;
		desiredHeight = j;
		resizePending = true;
	}

	public void setResizePending(boolean b) {
		resizePending = b;
	}

}
