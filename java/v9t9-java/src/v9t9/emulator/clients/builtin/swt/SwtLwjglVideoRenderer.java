/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;

/**
 * Render video into an OpenGL canvas in an SWT window
 * @author ejs
 *
 */
public class SwtLwjglVideoRenderer extends SwtVideoRenderer {
	private GLCanvas glCanvas;
	private GLData glData;
	// pfft, lwjgl doesn't handle all our modes
	//private MemoryCanvas memoryCanvas;
	private ImageDataCanvas imageCanvas;
	private int vdpCanvasTexture;
	private ByteBuffer vdpCanvasBuffer;

	protected VdpCanvas createCanvas() {
		imageCanvas = new ImageDataCanvas24Bit();
		vdpCanvasBuffer = ByteBuffer.allocateDirect(imageCanvas.getImageData().bytesPerLine * imageCanvas.getImageData().height);

		return imageCanvas;
	}


	protected Canvas createCanvas(Composite parent, int flags) {
		glData = new GLData();
		glData.doubleBuffer = true;
		glCanvas = new GLCanvas(parent, flags | getStyleBits(), glData);
		
		glCanvas.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				glCanvas.setCurrent();
				try {
					GLContext.useContext(glCanvas);
					updateWidgetSizeForMode();
				} catch (LWJGLException e) {
					e.printStackTrace();
				}

			} 
			
		});
		

		glCanvas.setCurrent();
		try {
			GLContext.useContext(glCanvas);
		} catch (LWJGLException e) { 
			e.printStackTrace(); 
			return null;
		}
		
		
		glShadeModel(GL_FLAT);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		
		setupCanvasTexture();
		
		return glCanvas;
	}


	private void setupCanvasTexture() {
		IntBuffer tmp = BufferUtils.createIntBuffer(1);
		glGenTextures(tmp);
		tmp.rewind();
		vdpCanvasTexture = tmp.get(0);
		
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, vdpCanvasTexture);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#updateWidgetSizeForMode()
	 */
	@Override
	protected void updateWidgetSizeForMode() {
		super.updateWidgetSizeForMode();
		
		Rectangle bounds = glCanvas.getClientArea();
		
		Rectangle destRect = new Rectangle(0, 0, 
				bounds.width, bounds.height);
		
		Rectangle imageRect = physicalToLogical(destRect);
		destRect = logicalToPhysical(imageRect);
		
		//System.out.printf("Viewport: %d x %d --> %d x %d%n",
		//		bounds.width, bounds.height,
		//		destRect.width, destRect.height);
		glViewport(0, 0,
				destRect.width, 
				destRect.height);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluOrtho2D(0.0f, 1.0f, 1.0f, 0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	
	protected void doRepaint(GC gc, Rectangle updateRect) {
		//reblit();
	
		reblit();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#doRedraw(org.eclipse.swt.graphics.Rectangle)
	 */
	@Override
	protected void doTriggerRedraw(Rectangle redrawRect) {

		reblit();
	}


	private void reblit() {
		glCanvas.setCurrent();
		try {
			GLContext.useContext(glCanvas);
		} catch (LWJGLException e) { 
			e.printStackTrace(); 
			return;
		}
		
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		glBindTexture(GL_TEXTURE_2D, vdpCanvasTexture);
		
		vdpCanvasBuffer = imageCanvas.copy(vdpCanvasBuffer);

		//System.out.printf("Texture size: %d x %d%n", 
		//		imageCanvas.getVisibleWidth(),
		//		imageCanvas.getVisibleHeight());
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexImage2D(GL_TEXTURE_2D, 0, 
				GL_RGB,
				imageCanvas.getVisibleWidth(),
				imageCanvas.getVisibleHeight(),
				0, 
				GL_RGB,
				GL_UNSIGNED_BYTE, 
				vdpCanvasBuffer);		
		
		glColor3f(1.0f, 1.0f, 1.0f);
		
		glBegin(GL_QUADS);
		glTexCoord2f(0f, 0f);		glVertex2i(0, 0);
		glTexCoord2f(0f, 1.0f);		glVertex2i(0, 1);
		glTexCoord2f(1.0f, 1.0f);	glVertex2i(1, 1);
		glTexCoord2f(1.0f, 0f);		glVertex2i(1, 0);
		glEnd();
		
		glColor3f(0.2f, 0.4f, 0.6f);
		glBegin(GL_LINES);
		glVertex2i(0, 0);
		glVertex2i(vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		glEnd();
		
		glCanvas.swapBuffers();
	}
	
}
