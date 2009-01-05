/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.gtk.OS;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.hardware.V9t9;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
import v9t9.jni.v9t9render.SWIGTYPE_p_AnalogTV;
import v9t9.jni.v9t9render.V9t9Render;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
@SuppressWarnings("restriction")
public class SwtVideoRendererGTK extends SwtVideoRenderer {
	private static final boolean USE_ANALOGTV = false;
	private static final boolean USE_GDKONLY = false;
	private static final boolean USE_SWTONLY = false;
	private static final boolean USE_NOISY = true;
	
	static {
		System.loadLibrary("v9t9render");
	}

	private SWIGTYPE_p_AnalogTV analog;
	
	public SwtVideoRendererGTK() {
		super();
		
		V9t9.settingMonitorDrawing.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				if (USE_NOISY)
					vdpCanvas.markDirty();
			}
			
		});
	}
	
	@Override
	protected void setupCanvas() {
		//OS.gtk_widget_set_app_paintable(canvas.handle, false);
	}

	@Override
	protected int getStyleBits() {
		return super.getStyleBits(); // | SWT.DOUBLE_BUFFERED;
	}
	@Override
	protected void resizeWidgets() {
		super.resizeWidgets();
		if (USE_ANALOGTV && analog != null) {
			V9t9Render.freeAnalogTv(analog);
			analog = null;
		}
	}

	@Override
	protected void doRepaint(GC gc, Rectangle updateRect) {
		if (!(vdpCanvas instanceof ImageDataCanvas))
			return;
		ImageData imageData = ((ImageDataCanvas) vdpCanvas).getImageData();
		if (imageData != null) {
			
			// the actual canvas size might not match our wishes
			Point canvasSize = new Point((int)(vdpCanvas.getVisibleWidth() * zoomx), 
					(int)(vdpCanvas.getVisibleHeight() * zoomy));

			if (USE_SWTONLY) {
				Rectangle destRect = updateRect;
				
				destRect = destRect.intersection(logicalToPhysical(0, 0, 
						vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight()));
				
				Rectangle imageRect = physicalToLogical(destRect);
				imageRect = vdpCanvas.mapVisible(imageRect);
				
				blitImageData(gc, imageData, destRect, imageRect);
			} else {
				int lineStride = vdpCanvas.getLineStride();
				if (vdpCanvas.isInterlacedEvenOdd())
					lineStride /= 2;
				if (USE_GDKONLY) {
					synchronized (vdpCanvas) {
						V9t9Render.renderGdkPixbufFromImageData(imageData.data,
								vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight(), lineStride,
								canvasSize.x, canvasSize.y,
								updateRect.x, updateRect.y, updateRect.width, updateRect.height,
								OS.GTK_WIDGET_WINDOW(canvas.handle));
					}
				} else if (USE_NOISY) {
					synchronized (vdpCanvas) {
						//System.out.println("repaint: " + updateRect);
						V9t9Render.renderNoisyGdkPixbufFromImageData(imageData.data,
								vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight(), lineStride,
								canvasSize.x, canvasSize.y,
								updateRect.x, updateRect.y, updateRect.width, updateRect.height,
								OS.GTK_WIDGET_WINDOW(canvas.handle));
					}
				} else if (USE_ANALOGTV) {
					if (analog == null) {
						analog = V9t9Render.allocateAnalogTv(canvasSize.x, canvasSize.y);
					}
					synchronized (vdpCanvas) {
						V9t9Render.renderAnalogGdkPixbufFromImageData(
								analog,
								imageData.data,
								vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight(), lineStride,
								canvasSize.x, canvasSize.y,
								OS.GTK_WIDGET_WINDOW(canvas.handle));
					}
				}
			}
		}
	}

}
