/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
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
			double zoomx = fixedAspectLayout.getZoomX();
			double zoomy = fixedAspectLayout.getZoomY();
			Point canvasSize = new Point((int)(vdpCanvas.getVisibleWidth() * zoomx), 
					(int)(vdpCanvas.getVisibleHeight() * zoomy));

			int lineStride = vdpCanvas.getLineStride();
			if (vdpCanvas.isInterlacedEvenOdd())
				lineStride /= 2;
		
			synchronized (vdpCanvas) {
				//System.out.println("repaint: " + updateRect);
				/*
				V9t9Render.renderNoisyWin32ImageFromImageData(imageData.data, 0,
						vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight(), lineStride,
						canvasSize.x, canvasSize.y, canvasSize.x * 4,
						updateRect.x, updateRect.y, updateRect.width, updateRect.height,
						canvas.handle);
						*/
			}
		}
	}

}
