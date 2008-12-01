/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.jni.v9t9render.SWIGTYPE_p_AnalogTV;
import v9t9.jni.v9t9render.V9t9Render;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRendererGTK extends SwtVideoRenderer {
	private static final boolean USE_ANALOGTV = false;
	private static final boolean USE_GDKONLY = false;
	private static final boolean USE_SWTONLY = false;
	private static final boolean USE_NOISY = true;
	
	static {
		System.loadLibrary("v9t9render");
	}

	private SWIGTYPE_p_AnalogTV analog;
	
	public SwtVideoRendererGTK(Display display, VdpCanvas canvas) {
		super(display, canvas);
	}

	@Override
	protected void resizeToProportions(Point curSize, Rectangle targetRect) {
		super.resizeToProportions(curSize, targetRect);
			
		if (USE_ANALOGTV && analog != null) {
			V9t9Render.freeAnalogTv(analog);
			analog = null;
		}
		
	}

	@SuppressWarnings("restriction")
	protected void repaint(GC gc, Rectangle updateRect) {
		long started = System.currentTimeMillis();
		ImageData imageData = vdpCanvas.getImageData();
		if (imageData != null) {
			
			if (USE_SWTONLY) {
				Rectangle destRect = updateRect;
				
				destRect = destRect.intersection(logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight()));
				
				Rectangle imageRect = physicalToLogical(destRect);
				imageRect = vdpCanvas.mapVisible(imageRect);
				
				blitImageData(gc, imageData, destRect, imageRect);
			} else if (USE_GDKONLY) {
				synchronized (vdpCanvas) {
					Point canvasSize = canvas.getSize();
					V9t9Render.renderGdkPixbufFromImageData(imageData.data,
							imageData.width, imageData.height, imageData.bytesPerLine,
							canvasSize.x, canvasSize.y,
							updateRect.x, updateRect.y, updateRect.width, updateRect.height,
							OS.GTK_WIDGET_WINDOW(canvas.handle));
				}
			} else if (USE_NOISY) {
				synchronized (vdpCanvas) {
					Point canvasSize = canvas.getSize();
					V9t9Render.renderNoisyGdkPixbufFromImageData(imageData.data,
							imageData.width, imageData.height, imageData.bytesPerLine,
							canvasSize.x, canvasSize.y,
							updateRect.x, updateRect.y, updateRect.width, updateRect.height,
							OS.GTK_WIDGET_WINDOW(canvas.handle));
				}
			} else if (USE_ANALOGTV) {
				Point canvasSize = canvas.getSize();
				if (analog == null) {
					analog = V9t9Render.allocateAnalogTv(canvasSize.x, canvasSize.y);
				}
				synchronized (vdpCanvas) {
					V9t9Render.renderAnalogGdkPixbufFromImageData(
							analog,
							imageData.data,
							imageData.width, imageData.height, imageData.bytesPerLine,
							canvasSize.x, canvasSize.y,
							OS.GTK_WIDGET_WINDOW(canvas.handle));
				}
			}
			wasBlank = false;
			lastUpdateTime = System.currentTimeMillis() - started;
		}
		vdpCanvas.clearDirty();
		
	}

}
