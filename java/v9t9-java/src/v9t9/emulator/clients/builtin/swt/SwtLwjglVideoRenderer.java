/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.ejs.coffee.core.utils.CompatUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
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
	// NVIDIA hw claimed to use 2..15
	private static final int HEIGHT_ATTRIB_INDEX = 16;
	private static final int WIDTH_ATTRIB_INDEX = 17;


	static {
		//System.out.println(System.getProperty("java.library.path"));
	}
	private GLCanvas glCanvas;
	private GLData glData;
	// pfft, lwjgl doesn't handle all our modes
	//private MemoryCanvas memoryCanvas;
	private ImageDataCanvas imageCanvas;
	private int vdpCanvasTexture;
	private ByteBuffer vdpCanvasBuffer;
	private int fragShader;
	private int vertexShader;
	private int programObject;
	
	private Rectangle glViewportRect;
	private Rectangle imageRect;

	protected VdpCanvas createCanvas() {
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

		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyListener#propertyChanged(org.ejs.coffee.core.properties.IProperty)
	 */
	@Override
	public void propertyChanged(IProperty property) {
		if (property == BaseEmulatorWindow.settingMonitorDrawing) {
			if (!glCanvas.isDisposed()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {

						glCanvas.setCurrent();
						try {
							GLContext.useContext(glCanvas);
							compileLinkShaders();
							
							reblit();
						} catch (LWJGLException e) { 
							e.printStackTrace(); 
							return;
						}
						
					}
				});
			}
		}
	}

	protected Canvas createCanvas(Composite parent, int flags) {
		glData = new GLData();
		glData.doubleBuffer = true;
		glCanvas = new GLCanvas(parent, flags | getStyleBits(), glData);
		
		BaseEmulatorWindow.settingMonitorDrawing.addListener(this);

		
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
		
		defineShaders();
		
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
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		
	}
	
	private void defineShaders() {
		try {
			fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			programObject = GL20.glCreateProgram();
			compileLinkShaders();
		} catch (OpenGLException e) {
			System.out.println("OpenGL 2.0 shaders not supported");
		}
		
	}

	static class GLShaderException extends Exception {

		private static final long serialVersionUID = 3737775043188087342L;
		private final String filename;

		public GLShaderException(String filename, String message, 
				Throwable cause) {
			super(message, cause);
			this.filename = filename;
		}
		/* (non-Javadoc)
		 * @see java.lang.Throwable#toString()
		 */
		@Override
		public String toString() {
			return "Shader exception: " + filename + ": " + getMessage() +
			 (getCause() != null ? "\n("+getCause().toString()+")" : "");
		}
		/**
		 * @return the filename
		 */
		public String getFilename() {
			return filename;
		}
		
	}
	/**
	 * 
	 */
	private void compileLinkShaders() {
		if (programObject == 0)
			return;
		
		try {
			String base = "shaders/" + (BaseEmulatorWindow.settingMonitorDrawing.getBoolean() ? "crt" : "std");
			compileShader(fragShader, base + ".frag");
			compileShader(vertexShader, base + ".vert");
			
			linkShaders(programObject, fragShader, vertexShader);

		} catch (GLShaderException e) {
			e.printStackTrace();
		}
	}

	private void compileShader(int shaderObj, String filename) throws GLShaderException {
		File file = DataFiles.resolveFile(filename);
		String text;
		try {
			text = DataFiles.readFileText(file);
		} catch (IOException e) {
			throw new GLShaderException(filename, "Cannot read file " + file, e);
		}
		GL20.glShaderSource(shaderObj, text);
		GL20.glCompileShader(shaderObj);
		
		int error = GL20.glGetShader(shaderObj, GL20.GL_COMPILE_STATUS);
		if (error != GL11.GL_TRUE) {
			throw new GLShaderException(filename, GL20.glGetShaderInfoLog(shaderObj, 65536),
					null);
		}
	}


	private void linkShaders(int programObj, int... shaders) throws GLShaderException {
		for (int shader : shaders) {
			GL20.glAttachShader(programObj, shader);
		}
		
		GL20.glLinkProgram(programObj);
		
		int error = GL20.glGetShader(programObj, GL20.GL_LINK_STATUS);
		if (error != GL11.GL_TRUE) {
			throw new GLShaderException("<<program>>", 
					GL20.glGetShaderInfoLog(programObj, 65536),
					null);
		}
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
		
		imageRect = physicalToLogical(destRect);
		glViewportRect = logicalToPhysical(imageRect);
		
		//System.out.printf("Viewport: %d x %d --> %d x %d%n",
		//		bounds.width, bounds.height,
		//		destRect.width, destRect.height);
		glViewport(0, 0,
				glViewportRect.width, 
				glViewportRect.height);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluOrtho2D(0.0f, 1.0f, 1.0f, 0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
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
		
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		glBindTexture(GL_TEXTURE_2D, vdpCanvasTexture);
		
		// copy current bitmap to texture (EXPENSIVE ON SLOW CARDS!)
		vdpCanvasBuffer = imageCanvas.copy(vdpCanvasBuffer);
		

		if (programObject != 0) {

			// bind program so we can look up uniforms
			GL20.glUseProgram(programObject);
			
			GL20.glUniform2i(GL20.glGetUniformLocation(programObject, "visible"), imageRect.width, imageRect.height);
			GL20.glUniform2i(GL20.glGetUniformLocation(programObject, "viewport"), glViewportRect.width, glViewportRect.height);
		}

		//System.out.printf("Texture size: %d x %d%n", 
		//		imageCanvas.getVisibleWidth(),
		//		imageCanvas.getVisibleHeight());
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glEnable(GL_TEXTURE_2D);

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
		
		/*
		glColor3f(0.2f, 0.4f, 0.6f);
		glBegin(GL_LINES);
		glVertex2i(0, 0);
		glVertex2i(vdpCanvas.getVisibleWidth(), vdpCanvas.getVisibleHeight());
		glEnd();
		*/
		
		glDisable(GL_TEXTURE_2D);
		

		if (programObject != 0) {
			GL20.glUseProgram(0); 
		
		}

		
		glCanvas.swapBuffers();
	}
	
}
