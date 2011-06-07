/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.engine.files.DataFiles;

/**
 * Render video into an OpenGL canvas in an SWT window
 * @author ejs
 *
 */
public class SwtLwjglVideoRenderer extends SwtVideoRenderer implements IPropertyListener {
	private static boolean VERBOSE = false;
	
	private enum Effect {
		STANDARD("shaders/std", null, GL_LINEAR, GL_NEAREST),
		CRT("shaders/crt", null, GL_LINEAR, GL_NEAREST),
		CRT1("shaders/crt1", "shaders/monitor.png", GL_LINEAR, GL_LINEAR),
		CRT2("shaders/crt2", "shaders/monitorRGB.png", GL_LINEAR, GL_LINEAR);
		
		private final String shaderBase;
		private final String texture;
		private final int minFilter;
		private final int magFilter;

		Effect(String shaderBase, String texture, int minFilter, int maxFilter) {
			this.shaderBase = shaderBase;
			this.texture = texture;
			this.minFilter = minFilter;
			this.magFilter = maxFilter;
		}
		
		public String getShaderBase() {
			return shaderBase;
		}
		public String getTexture() {
			return texture;
		}
		public int getMinFilter() {
			return minFilter;
		}
		public int getMagFilter() {
			return magFilter;
		}
	}
	
	static {
		if (VERBOSE) System.out.println(System.getProperty("java.library.path"));
	}
	private GLCanvas glCanvas;
	private GLData glData;
	// pfft, lwjgl doesn't handle all our modes
	//private MemoryCanvas memoryCanvas;
	private ImageDataCanvas imageCanvas;
	private int vdpCanvasTexture;
	private ByteBuffer vdpCanvasBuffer;
	
	private boolean supportsShaders = true;
	
	private int fragShader;
	private int vertexShader;
	private int programObject;
	
	private Rectangle glViewportRect;
	private Rectangle imageRect;
	private Listener resizeListener;
	private TextureLoader textureLoader = new TextureLoader();
	private int curvedList;

	protected VdpCanvas createVdpCanvas() {
		imageCanvas = new ImageDataCanvas24Bit();
		vdpCanvasBuffer = ByteBuffer.allocateDirect(imageCanvas.getImageData().bytesPerLine * imageCanvas.getImageData().height);

		return imageCanvas;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#dispose()
	 */
	@Override
	public void dispose() {
		BaseEmulatorWindow.settingMonitorDrawing.removeListener(this);
		glCanvas.getParent().removeListener(SWT.Resize, resizeListener);
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyListener#propertyChanged(org.ejs.coffee.core.properties.IProperty)
	 */
	@Override
	public void propertyChanged(IProperty property) {
		if (property == BaseEmulatorWindow.settingMonitorDrawing) {
			updateShaders();
		}
	}

	protected void updateShaders() {
		if (!glCanvas.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					glCanvas.setCurrent();
					try {
						GLContext.useContext(glCanvas);
						compileLinkShaders();
						glCanvas.redraw();
						updateWidgetSizeForMode();
						reblit();
					} catch (LWJGLException e) { 
						e.printStackTrace(); 
						return;
					}
					
				}
			});
		}
	}
	
	protected Canvas createCanvasControl(Composite parent, int flags) {
		glData = new GLData();
		glData.doubleBuffer = true;
		glData.depthSize = 0;
		glCanvas = new GLCanvas(parent, flags | getStyleBits(), glData);
		
		BaseEmulatorWindow.settingMonitorDrawing.addListener(this);

		
		resizeListener = new Listener() {

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
			
		};

		parent.addListener(SWT.Resize, resizeListener);

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
		if (vdpCanvasTexture != 0)
			glDeleteTextures(vdpCanvasTexture);
		vdpCanvasTexture = glGenTextures();
		
		// do not read data until blit time
	}
	
	static class GLShaderException extends Exception {

		private static final long serialVersionUID = 3737775043188087342L;
		private final String filename;

		public GLShaderException(String filename, String message, 
				Throwable cause) {
			super(message, cause);
			this.filename = filename;
		}
		@Override
		public String toString() {
			return "Shader exception: " + filename + ": " + getMessage() +
			 (getCause() != null ? "\n("+getCause().toString()+")" : "");
		}
		public String getFilename() {
			return filename;
		}
		
	}
	
	private Effect getEffect() {
		return BaseEmulatorWindow.settingMonitorDrawing.getBoolean() 
			? Effect.CRT2 : Effect.STANDARD;
	}
	
	private void compileLinkShaders() {
		if (!supportsShaders)
			return;
		
		try {
			if (programObject != 0)
				ARBShaderObjects.glDeleteObjectARB(programObject);
			programObject = ARBShaderObjects.glCreateProgramObjectARB();
			
			String base = getEffect().getShaderBase();
			vertexShader = compileShader(vertexShader, ARBVertexShader.GL_VERTEX_SHADER_ARB, base + ".vert");
			fragShader = compileShader(fragShader, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB, base + ".frag");
			
			linkShaders(programObject, vertexShader, fragShader);

		} catch (GLShaderException e) {
			ARBShaderObjects.glDeleteObjectARB(programObject);
			ARBShaderObjects.glDeleteObjectARB(fragShader);
			ARBShaderObjects.glDeleteObjectARB(vertexShader);
			programObject = fragShader = vertexShader = 0;
			e.printStackTrace();
		}
	}

	private int compileShader(int shaderObj, int type, String filename) throws GLShaderException {
		URL url = getClass().getClassLoader().getResource(filename);
		if (url == null)
			throw new GLShaderException(filename, "Not found", null);
		
		if (shaderObj != 0)
			ARBShaderObjects.glDeleteObjectARB(shaderObj);
		shaderObj = ARBShaderObjects.glCreateShaderObjectARB(type);

		if (VERBOSE) System.out.println("Compiling " + url + " to " +shaderObj);
		String text;
		try {
			text = DataFiles.readInputStreamText(url.openStream());
		} catch (IOException e) {
			throw new GLShaderException(filename, "Cannot read file " + url, e);
		}
		ARBShaderObjects.glShaderSourceARB(shaderObj, text);
		ARBShaderObjects.glCompileShaderARB(shaderObj);
		
		int error = ARBShaderObjects.glGetObjectParameteriARB(shaderObj, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB);
		if (error == GL_FALSE) {
			int length = ARBShaderObjects.glGetObjectParameteriARB(shaderObj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);
			String log = ARBShaderObjects.glGetInfoLogARB(shaderObj, length);
			throw new GLShaderException(filename, 
					log,
					null);
		}
		return shaderObj;
	}


	private void linkShaders(int programObj, int... shaders) throws GLShaderException {
		for (int shader : shaders) {
			if (VERBOSE) System.out.println("Linking " + shader + " to " + programObj);
			ARBShaderObjects.glAttachObjectARB(programObj, shader);
		}
		
		ARBShaderObjects.glLinkProgramARB(programObj);
		
		int error = ARBShaderObjects.glGetObjectParameteriARB(programObj, GL20.GL_LINK_STATUS);
		if (error == GL_FALSE) {
			int length = ARBShaderObjects.glGetObjectParameteriARB(programObj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);
			String log = ARBShaderObjects.glGetInfoLogARB(programObj, length);
			throw new GLShaderException("<<program>>", 
					log,
					null);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#canvasResized(v9t9.emulator.clients.builtin.video.VdpCanvas)
	 */
	@Override
	public void canvasResized(VdpCanvas canvas) {
		super.canvasResized(canvas);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				updateWidgetSizeForMode();
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#updateWidgetSizeForMode()
	 */
	@Override
	protected void updateWidgetSizeForMode() {
		super.updateWidgetSizeForMode();
		
		Rectangle bounds = glCanvas.getClientArea();
		if (VERBOSE) System.out.printf("updateWidgetSizeForMode at %s%n", 
				bounds);
		
		Rectangle destRect = new Rectangle(0, 0, 
				bounds.width, bounds.height);
		
		imageRect = physicalToLogical(destRect);
		glViewportRect = logicalToPhysical(imageRect);
		//glViewportRect = logicalToPhysical(new Rectangle(0, 0, vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight()));
		
		if (VERBOSE) System.out.printf("Viewport: %d x %d --> %d x %d%n",
				bounds.width, bounds.height,
				glViewportRect.width, glViewportRect.height);
		glViewport(0, 0,
				glViewportRect.width, 
				glViewportRect.height);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluOrtho2D(0.0f, 1.0f, 1.0f, 0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		if (programObject != 0) {
			// bind program so we can look up uniforms
			ARBShaderObjects.glUseProgramObjectARB(programObject);
			
			if (VERBOSE) System.out.printf("Sending sizes: %s and %s%n", imageRect, glViewportRect);
			ARBShaderObjects.glUniform2iARB(
					ARBShaderObjects.glGetUniformLocationARB(programObject, "visible"), 
					imageRect.width, imageRect.height);
			ARBShaderObjects.glUniform2iARB(
					ARBShaderObjects.glGetUniformLocationARB(programObject, "viewport"), 
					glViewportRect.width, glViewportRect.height);
			
			ARBShaderObjects.glUniform1iARB(
					ARBShaderObjects.glGetUniformLocationARB(programObject, "canvasTexture"), 
					0);
			ARBShaderObjects.glUniform1iARB(
					ARBShaderObjects.glGetUniformLocationARB(programObject, "pixelTexture"), 
					1);

			glActiveTexture(GL_TEXTURE1);
			
			glMatrixMode(GL_TEXTURE);
			
			glLoadIdentity();
			glScalef(vdpCanvas.getVisibleWidth() > 256 ? vdpCanvas.getVisibleWidth() / 2 : vdpCanvas.getVisibleWidth(), 
					vdpCanvas.isInterlacedEvenOdd() ? vdpCanvas.getVisibleHeight() / 2 : vdpCanvas.getVisibleHeight(), 1.0f);

			glMatrixMode(GL_MODELVIEW);
			
			glActiveTexture(GL_TEXTURE0);

		}
		
		if (VERBOSE) System.out.printf("Texture size: %d x %d%n", 
				imageCanvas.getVisibleWidth(),
				imageCanvas.getVisibleHeight());

	}

	
	protected void doRepaint(GC gc, Rectangle updateRect) {
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

		if (programObject != 0) {
			ARBShaderObjects.glUseProgramObjectARB(programObject);
		}
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		glEnable(GL_TEXTURE_2D);
		
		/*
		 * Main texture: the VDP canvas
		 */
		glActiveTexture(GL_TEXTURE0);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, getEffect().getMagFilter());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, getEffect().getMinFilter());

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		
		glBindTexture(GL_TEXTURE_2D, vdpCanvasTexture);
		
		// copy current bitmap to texture (EXPENSIVE ON SLOW CARDS!)
		vdpCanvasBuffer = imageCanvas.copy(vdpCanvasBuffer);
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexImage2D(GL_TEXTURE_2D, 0, 
				GL_RGB,
				imageCanvas.getVisibleWidth(),
				imageCanvas.getVisibleHeight(),
				0, 
				GL_RGB,
				GL_UNSIGNED_BYTE, 
				vdpCanvasBuffer);		

		/*
		 * Second texture: the monitor overlay
		 */
		glActiveTexture(GL_TEXTURE1);
		Effect effect = getEffect();
		if (effect.getTexture() != null) {
			try {
				textureLoader.getTexture(effect.getTexture()).bind();
			} catch (IOException e) {
				e.printStackTrace();
			}

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			
		} else {
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		
		glActiveTexture(GL_TEXTURE0);

		if (effect == Effect.STANDARD) {
			drawFlatScreen();
		} else {
			if (curvedList == 0) {
				curvedList = glGenLists(1);
				glNewList(curvedList, GL_COMPILE);
				drawCurvedScreen();
				glEndList();
			}
			glCallList(curvedList);
		}
		
		/*
		glColor3f(0.2f, 0.4f, 0.6f);
		glBegin(GL_LINES);
		glVertex2i(0, 0);
		glVertex2i(vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		glEnd();
		*/
		
		glDisable(GL_TEXTURE_2D);

		if (programObject != 0) {
			ARBShaderObjects.glUseProgramObjectARB(0); 
		
		}
		
		glCanvas.swapBuffers();
		
		// HACK for Intel Mobile Express Graphics --
		// if shaders are compiled/linked BEFORE an initial render,
		// the whole ig4icd[32|64].dll DLL crashes and burns
		if (programObject == 0 && supportsShaders) {
			compileLinkShaders();
		}
	}

	/**
	 * 
	 */
	private void drawCurvedScreen() {
		glBegin(GL_QUADS);

		// vertically taper the screen
		drawVerticalCurves(0.5f, 0.5f, 0.5f, 0.5f);
		drawVerticalCurves(0.5f, -0.5f, 0.5f, -0.5f);

		glEnd();
	}

	private final static float CVMAJOR = 0.3f;
	private final static float TVMAJOR = 0.275f;
	private final static float CHMAJOR = 0.2f;
	private final static float THMAJOR = 0.19f;
	
	private final static float EPSILON = 0.02f;
	
	private void drawVerticalCurves(float cy, float cdy, float ty, float tdy) {
		if (Math.abs(cdy) < EPSILON) {
			drawHorizontalCurves(0.5f, -0.5f, cy, cdy,
					0.5f, -0.5f, ty, tdy);
			drawHorizontalCurves(0.5f, 0.5f, cy, cdy,
					0.5f, 0.5f, ty, tdy);
			return;
		}
		// horizontally taper a curve
		drawHorizontalCurves(0.5f, -0.5f, cy, cdy * CVMAJOR,
				0.5f, -0.5f, ty, tdy * TVMAJOR);
		drawHorizontalCurves(0.5f, 0.5f, cy, cdy * CVMAJOR,
				0.5f, 0.5f, ty, tdy * TVMAJOR);
		
		// inner part
		drawVerticalCurves(cy + cdy * CVMAJOR, cdy * (1 - CVMAJOR), 
				ty + tdy * TVMAJOR, tdy * (1 - TVMAJOR));
	}

	private void drawHorizontalCurves(float cx, float cdx, float cy, float cdy,
			float tx, float tdx, float ty, float tdy) {
		if (Math.abs(cdx) < EPSILON) {
			drawQuad(cx, cy, cx + cdx, cy + cdy, tx, ty, tx + tdx, ty + tdy);
			return;
		}
		// inner half
		drawHorizontalCurves(cx, cdx * CHMAJOR, cy, cdy, 
				tx, tdx * THMAJOR, ty, tdy);
		// outer half
		drawHorizontalCurves(cx + cdx * CHMAJOR, cdx * (1 - CHMAJOR), cy, cdy, 
				tx + tdx * THMAJOR, tdx * (1 - THMAJOR), ty, tdy);
	}

	/**
	 * 
	 */
	private void drawFlatScreen() {
		glBegin(GL_QUADS);
		
		drawQuad(0, 0, 1, 1, 0, 0, 1, 1);
		
		glEnd();
		
	}
	
	private void drawQuad(float cx, float cy, float cx2, float cy2,
			float tx, float ty, float tx2, float ty2) {
		glMultiTexCoord2f(GL_TEXTURE1, cx, cy);
		glTexCoord2f(tx, ty);		
		glVertex2f(cx, cy);
		glMultiTexCoord2f(GL_TEXTURE1, cx, cy2);
		glTexCoord2f(tx, ty2);		
		glVertex2f(cx, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, cx2, cy2);
		glTexCoord2f(tx2, ty2);		
		glVertex2f(cx2, cy2);
		glMultiTexCoord2f(GL_TEXTURE1, cx2, cy);
		glTexCoord2f(tx2, ty);		
		glVertex2f(cx2, cy);
	}
	
}
