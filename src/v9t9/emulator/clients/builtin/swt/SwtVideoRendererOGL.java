/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
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
	
	public SwtVideoRendererOGL() {
		super();
		
	}
	
	protected VdpCanvas createCanvas() {
		ogl = V9t9Render.allocateOpenGL(32 * 27);
		//return new GLCanvas(ogl);
		return new ImageDataCanvas24Bit();
	}

	protected void initWidgets() {
		super.initWidgets();
		V9t9Render.realizeOpenGL(ogl, canvas.handle);
		
	}
	@Override
	protected void resizeWidgets() {
		if (!canvas.isVisible())
			return;
		super.resizeWidgets();
		//V9t9Render.freeOpenGL(ogl);
		//ogl = V9t9Render.allocateOpenGL(32 *27);
		canvas.setVisible(false);
		V9t9Render.realizeOpenGL(ogl, canvas.handle);
		canvas.setVisible(true);
	}

	@Override
	protected void doRepaint(GC gc, Rectangle updateRect) {
		if (ogl == null)
			return;
		
		/*
		if (vdpCanvas instanceof GLCanvas) {
			GLCanvas glCanvas = (GLCanvas) vdpCanvas;
			if (glCanvas.ogl != null) {
				Point canvasSize = canvas.getSize();
				synchronized (vdpCanvas) {
					
					V9t9Render. renderOpenGLFromBlocks(glCanvas.ogl,
							vdpCanvas.getWidth(), vdpCanvas.getHeight(),
							canvasSize.x, canvasSize.y,
							updateRect.x, updateRect.y, updateRect.width, updateRect.height);
					
				}
			}
		} else*/ {
			ImageData imageData = ((ImageDataCanvas) vdpCanvas).getImageData();
			if (imageData != null) {
				
				Point canvasSize = canvas.getSize();
				
				synchronized (vdpCanvas) {
					V9t9Render.renderOpenGLFromImageData(
							ogl,
							imageData.data,
							vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight(), vdpCanvas.getLineStride(),
							canvasSize.x, canvasSize.y,
							updateRect.x, updateRect.y, updateRect.width, updateRect.height);
				}
			}
		}
	}

}
