/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import v9t9.jni.v9t9render.SWIGTYPE_p_OpenGL;
import v9t9.jni.v9t9render.V9t9Render;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRendererOGL extends SwtVideoRenderer {
	
	static {
		System.loadLibrary("v9t9render");
	}

	private SWIGTYPE_p_OpenGL ogl;
	
	public SwtVideoRendererOGL(Display display, VdpCanvas canvas) {
		super(display, canvas);
	}

	protected void initWidgets() {
		super.initWidgets();
		ogl = V9t9Render.allocateOpenGL(256, 192, 
					canvas.handle);
		
		
	}
	@Override
	protected void resizeWidgets() {
		super.resizeWidgets();
		if (ogl != null)
			V9t9Render.freeOpenGL(ogl);
		canvas.setVisible(false);
		ogl = V9t9Render.allocateOpenGL(256, 192, 
				canvas.handle);
		canvas.setVisible(true);
	}

	protected void repaint(GC gc, Rectangle updateRect) {
		if (canvas.isDisposed())
			return;
		
		if (!canvas.isVisible() || !shell.isVisible())
			return;
		
		if (ogl == null)
			return;
		
		long started = System.currentTimeMillis();
		ImageData imageData = vdpCanvas.getImageData();
		if (imageData != null) {
			
			Point canvasSize = canvas.getSize();
			
			synchronized (vdpCanvas) {
				V9t9Render.renderOpenGLFromImageData(
						ogl,
						imageData.data,
						imageData.width, imageData.height, imageData.bytesPerLine,
						canvasSize.x, canvasSize.y);
			}
			wasBlank = false;
			lastUpdateTime = System.currentTimeMillis() - started;
		}
		vdpCanvas.clearDirty();
		
	}

}
