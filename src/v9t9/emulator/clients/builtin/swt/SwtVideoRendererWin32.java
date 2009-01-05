/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.jni.v9t9render.V9t9Render;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
@SuppressWarnings("restriction")
public class SwtVideoRendererWin32 extends SwtVideoRenderer {
	
	static {
		System.loadLibrary("v9t9render");
	}
	
	public SwtVideoRendererWin32() {
		super();
	}
	
	protected void setupCanvas() {
	}

	@Override
	protected int getStyleBits() {
		return super.getStyleBits(); // | SWT.DOUBLE_BUFFERED;
	}
	@Override
	protected void resizeWidgets() {
		super.resizeWidgets();
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

			int lineStride = vdpCanvas.getLineStride();
			if (vdpCanvas.isInterlacedEvenOdd())
				lineStride /= 2;
		
			synchronized (vdpCanvas) {
				//System.out.println("repaint: " + updateRect);
				V9t9Render.renderNoisyWin32ImageFromImageData(imageData.data,
						vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight(), lineStride,
						canvasSize.x, canvasSize.y,
						updateRect.x, updateRect.y, updateRect.width, updateRect.height,
						canvas.handle);
			}
		}
	}

}
