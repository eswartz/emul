/*
  SwtLwjglVideoRenderer.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_REPLACE;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glCopyTexImage2D;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexEnvf;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.glMultiTexCoord2f;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.glu.GLU;

import v9t9.common.client.IMonitorEffectSupport;
import v9t9.common.machine.IMachine;
import v9t9.common.video.ICanvas;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.MonitorEffectSupport;
import v9t9.gui.client.swt.gl.IGLMonitorEffect;
import v9t9.gui.client.swt.gl.MonitorEffect;
import v9t9.gui.client.swt.gl.MonitorParams;
import v9t9.gui.client.swt.gl.SimpleCurvedCrtMonitorRender;
import v9t9.gui.client.swt.gl.StandardMonitorRender;
import v9t9.gui.client.swt.gl.TextureLoader;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import v9t9.gui.common.BaseEmulatorWindow;
import v9t9.video.IGLDataCanvas;
import v9t9.video.common.CanvasFormat;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.utils.FileUtils;

/**
 * Render video into an OpenGL canvas in an SWT window
 * @author ejs
 *
 */
public class SwtLwjglVideoRenderer extends SwtVideoRenderer implements IPropertyListener {
	public static float glVersion;

	private static final String EFFECT_STANDARD_CRT1 = "standardCrt1";
	private static final String EFFECT_STANDARD_CRT2 = "standardCrt2";
	private static final String EFFECT_STANDARD_CRT3 = "standardCrt3";
	private static final String EFFECT_CURVED_CRT1 = "curvedCrt1";
	private static final String EFFECT_CURVED_CRT2 = "curvedCrt2";
//	private static final String EFFECT_SUBPIXEL_CRT1 = "subPixelCrt1";
	private static final String EFFECT_SUBPIXEL_CRT2 = "subPixelCrt2";
	private static final String EFFECT_WAVY_CRT1 = "wavyCrt1";
	private static final String EFFECT_WAVY_CRT2 = "wavyCrt2";
	private static final String EFFECT_DEFAULT = EFFECT_STANDARD_CRT1;

	private static boolean VERBOSE = false;
	
	static final MonitorParams paramsSTANDARD = new MonitorParams(
		"shaders/std", null, GL_LINEAR, GL_NEAREST);
	static final MonitorParams paramsCRT1 = new MonitorParams(
		"shaders/crt", null, GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsCRT2 = new MonitorParams(
		"shaders/crt2", "shaders/monitorRGB.png", GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsCRT3 = new MonitorParams(
		"shaders/crt1", "shaders/monitor.png", GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsSubPixelCRT1 = new MonitorParams(
		"shaders/crtSubPixel1", null, GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsSubPixelCRT2 = new MonitorParams(
			"shaders/crtSubPixel2", null, GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsWavyCRT1 = new MonitorParams(
			"shaders/crtWavy1", null, GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsWavyCRT2 = new MonitorParams(
			"shaders/crtWavy2", null, GL_LINEAR, GL_LINEAR, true);
	static final MonitorParams paramsCurvedCRT1 = new MonitorParams(
			"shaders/crtCurved", null, GL_LINEAR, GL_LINEAR);
	static final MonitorParams paramsCurvedCRT2 = new MonitorParams(
			"shaders/crt2", "shaders/monitorRGB.png", GL_LINEAR, GL_LINEAR);
		
	static final MonitorEffect STANDARD = new MonitorEffect(
			"No effect",
			paramsSTANDARD,
			StandardMonitorRender.INSTANCE);
	
	static {
		if (VERBOSE) System.out.println(System.getProperty("java.library.path"));
	}

	static MonitorEffectSupport monitorEffectSupport = new MonitorEffectSupport(); 
	static {
		monitorEffectSupport.registerEffect(EFFECT_STANDARD_CRT1, new MonitorEffect(
				"Standard CRT #1",
				paramsCRT1,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_STANDARD_CRT2, new MonitorEffect(
				"Standard CRT #2",
				paramsCRT2,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_STANDARD_CRT3, new MonitorEffect(
				"Standard CRT #3",
				paramsCRT3,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_CURVED_CRT1, new MonitorEffect(
				"Curved CRT #1 (frag)",
				paramsCurvedCRT1,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_CURVED_CRT2, new MonitorEffect(
				"Curved CRT #2 (vert)",
				paramsCurvedCRT2,
				SimpleCurvedCrtMonitorRender.INSTANCE));
//		monitorEffectSupport.registerEffect(EFFECT_SUBPIXEL_CRT1, new MonitorEffect(
//				"Subpixel CRT #1",
//				paramsSubPixelCRT1,
//				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_SUBPIXEL_CRT2, new MonitorEffect(
				"Subpixel CRT #2",
				paramsSubPixelCRT2,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_WAVY_CRT1, new MonitorEffect(
				"Wavy CRT (still)",
				paramsWavyCRT1,
				StandardMonitorRender.INSTANCE));
		monitorEffectSupport.registerEffect(EFFECT_WAVY_CRT2, new MonitorEffect(
				"Wavy CRT (moving)",
				paramsWavyCRT2,
				StandardMonitorRender.INSTANCE));
	}
	
	private GLCanvas glCanvas;
	private GLData glData;

	private IGLDataCanvas glDataCanvas;
	private int vdpCanvasTexture;
	
	private boolean supportsShaders = false;
	
	private int fragShader;
	private int vertexShader;
	private int programObject;
	
	private Rectangle glViewportRect;
	private Rectangle imageRect;
	private Listener resizeListener;
	private TextureLoader textureLoader = new TextureLoader();
	private Map<IGLMonitorEffect, Integer> displayListMap = new HashMap<IGLMonitorEffect, Integer>();

	private IProperty monitorDrawing;
	private IProperty monitorEffect;


	private long lastReport;
	private long lastFrameTime;
	@SuppressWarnings("unused")
	private int frames;
	@SuppressWarnings("unused")
	private long frameTimes;
	private boolean supportsMultiTexture;

	public SwtLwjglVideoRenderer(IMachine machine) {
		super(machine);
		monitorDrawing = settings.get(BaseEmulatorWindow.settingMonitorDrawing);
		monitorEffect = settings.get(BaseEmulatorWindow.settingMonitorEffect);
	}

	protected void createVdpCanvasHandler() {
		super.createVdpCanvasHandler();
		if (false == vdpCanvas instanceof IGLDataCanvas) {
			canvasFormat.setValue(getDefaultCanvasFormat());
		} else {
			glDataCanvas = (IGLDataCanvas) vdpCanvas;
			updateShaders();
		}
	}
	

	/**
	 * @return
	 */
	protected CanvasFormat getDefaultCanvasFormat() {
		CanvasFormat format;
		if (getVdpHandler().getRegisterCount() > 16)
			format = CanvasFormat.RGB16_5_6_5;	// V9938
		else
			format = CanvasFormat.RGB8_3_3_2;	// TMS9918A
		
		if (format.getMinGLVersion() > glVersion)
			format = CanvasFormat.RGB24;	
		return format;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#dispose()
	 */
	@Override
	public void dispose() {
		monitorDrawing.removeListener(this);
		monitorEffect.removeListener(this);
		if (!glCanvas.isDisposed())
			glCanvas.getParent().removeListener(SWT.Resize, resizeListener);
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IPropertyListener#propertyChanged(v9t9.base.core.properties.IProperty)
	 */
	@Override
	public void propertyChanged(IProperty property) {
		if (property == monitorDrawing || property == monitorEffect
				|| property == canvasFormat) {
			updateShaders();
		}
	}

	protected void updateShaders() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				synchronized (SwtLwjglVideoRenderer.this) {
					if (!glCanvas.isDisposed()) {
						glCanvas.setCurrent();
						try {
							GLContext.useContext(glCanvas);
							compileLinkShaders();
							glCanvas.redraw();
							updateWidgetSizeForMode();
							reblitGL(false);
						} catch (LWJGLException e) { 
							e.printStackTrace(); 
							return;
						}
					}
				}
			}
		});
	}
	
	protected Canvas createCanvasControl(Composite parent, int flags) {
		glData = new GLData();
		glData.doubleBuffer = true;
		glData.depthSize = 0;
		glCanvas = new GLCanvas(parent, flags | getStyleBits(), glData);
		
		monitorDrawing.addListener(this);
		monitorEffect.addListener(this);

		
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

		String glVersionStr = glGetString(GL_VERSION);
		int idx = glVersionStr.indexOf('.', glVersionStr.indexOf('.') + 1);
		glVersion = Float.parseFloat(glVersionStr.substring(0, idx));
		
		supportsMultiTexture = glVersion >= 1.3;
		supportsShaders = glVersion >= 1.4;
		
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
	
	private IGLMonitorEffect getEffect() {
		if (!monitorDrawing.getBoolean())
			return STANDARD;
		
		String effectId = monitorEffect.getString();
		
		IGLMonitorEffect effect = (IGLMonitorEffect) monitorEffectSupport.getEffect(effectId);
		if (effect == null) {
			effect = (IGLMonitorEffect) monitorEffectSupport.getEffect(EFFECT_DEFAULT);
			if (effect == null) {
				return STANDARD;
			}
		}
		
		return effect;
	}
	
	private synchronized void compileLinkShaders() {
		if (!supportsShaders)
			return;
		
		try {
			if (programObject != 0)
				ARBShaderObjects.glDeleteObjectARB(programObject);
			programObject = ARBShaderObjects.glCreateProgramObjectARB();
			
			IGLMonitorEffect effect = getEffect();
			String base = effect.getParams().getShaderBase();
			vertexShader = compileShader(vertexShader, ARBVertexShader.GL_VERTEX_SHADER_ARB, base + ".vert");
			fragShader = compileShader(fragShader, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB, base + ".frag");
			
			linkShaders(programObject, vertexShader, fragShader);

			updateProgramVariables();
			
		} catch (GLShaderException e) {
			ARBShaderObjects.glDeleteObjectARB(programObject);
			ARBShaderObjects.glDeleteObjectARB(fragShader);
			ARBShaderObjects.glDeleteObjectARB(vertexShader);
			programObject = fragShader = vertexShader = 0;
			e.printStackTrace();
		}
	}

	private int compileShader(int shaderObj, int type, String filename) throws GLShaderException {
		URL url = EmulatorGuiData.getDataURL(filename);
		if (url == null)
			throw new GLShaderException(filename, "Not found", null);
		
		if (shaderObj != 0)
			ARBShaderObjects.glDeleteObjectARB(shaderObj);
		shaderObj = ARBShaderObjects.glCreateShaderObjectARB(type);

		if (VERBOSE) System.out.println("Compiling " + url + " to " +shaderObj);
		String text;
		InputStream is = null;
		try {
			is = url.openStream();
			text = FileUtils.readInputStreamTextAndClose(is);
		} catch (IOException e) {
			throw new GLShaderException(filename, "Cannot read file " + url, e);
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {
			}
		}
		ARBShaderObjects.glShaderSourceARB(shaderObj, text);
		ARBShaderObjects.glCompileShaderARB(shaderObj);
		
		int error = ARBShaderObjects.glGetObjectParameteriARB(shaderObj, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB);
		int length = ARBShaderObjects.glGetObjectParameteriARB(shaderObj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);
		String log = ARBShaderObjects.glGetInfoLogARB(shaderObj, length);
		if (error == GL_FALSE) {
			throw new GLShaderException(filename, 
					log,
					null);
		} else {
			if (!log.isEmpty()) {
				System.err.println(filename+":\n"+log);
			}
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
	public void canvasResized(ICanvas canvas) {
		super.canvasResized(canvas);
		Display.getDefault().asyncExec(new Runnable() {
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
		
		updateProgramVariables();
		
		if (supportsMultiTexture) {
			glActiveTexture(GL_TEXTURE1);
		
			glMatrixMode(GL_TEXTURE);
			
			glLoadIdentity();
			glScalef(vdpCanvas.getVisibleWidth() > 256 ? vdpCanvas.getVisibleWidth() / 2 : vdpCanvas.getVisibleWidth(), 
					vdpCanvas.isInterlacedEvenOdd() ? vdpCanvas.getVisibleHeight() / 2 : vdpCanvas.getVisibleHeight(), 1.0f);
	
			glMatrixMode(GL_MODELVIEW);
		
			glActiveTexture(GL_TEXTURE0);
		}
		
		for (Integer displayList : displayListMap.values()) {
			glDeleteLists(displayList, 1);
		}
		displayListMap.clear();
		
		if (VERBOSE) System.out.printf("Texture size: %d x %d%n", 
				vdpCanvas.getVisibleWidth(),
				vdpCanvas.getVisibleHeight());

	}

	
	/**
	 * 
	 */
	private void updateProgramVariables() {
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
		}
		
	}

	protected void doRepaint(GC gc, Rectangle updateRect) {
		reblitGL(true);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#doRedraw(org.eclipse.swt.graphics.Rectangle)
	 */
	@Override
	protected void doTriggerRedraw() {
		reblitGL(true);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.SwtVideoRenderer#doCleanRedraw()
	 */
	@Override
	protected void doCleanRedraw() {
		IGLMonitorEffect effect = getEffect();
		MonitorParams params = effect.getParams();
		
		if (params.isRefreshRealtime()) {
			reblitGL(false);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtVideoRenderer#reblit()
	 */
	@Override
	public void reblit() {
		try {
			reblitGL(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		glFlush();
	}
	
	private synchronized void reblitGL(boolean updated) {
		long firstTime = System.currentTimeMillis();
		
		IGLMonitorEffect effect = getEffect();
		MonitorParams params = effect.getParams();
		
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
		
		Util.checkGLError();
		/*
		 * Main texture: the VDP canvas
		 */
		if (supportsMultiTexture)
			glActiveTexture(GL_TEXTURE0);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, params.getMagFilter());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, params.getMinFilter());

		Util.checkGLError();
		int border = 0;
		
		if (supportsMultiTexture) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			
		}
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		
		glBindTexture(GL_TEXTURE_2D, vdpCanvasTexture);

		Util.checkGLError();
		
		Buffer vdpCanvasBuffer = glDataCanvas.getBuffer();
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		int imageCanvasFormat = glDataCanvas.getImageFormat();
		int imageCanvasType = glDataCanvas.getImageType();

		int texFmt = glDataCanvas.getInternalFormat();
		
		int sx = 1;
		while (sx < vdpCanvas.getVisibleWidth())
			sx <<= 1;
		int sy;
		if (glVersion >= 1.3f) {
			sy = vdpCanvas.getVisibleHeight() + border * 2;
		} else {
			sy = 1;
			while (sy < vdpCanvas.getVisibleHeight())
				sy <<= 1;
		}
		
			
		Util.checkGLError();
		if (updated) {
			if (vdpCanvasBuffer instanceof ByteBuffer) {
				glTexImage2D(GL_TEXTURE_2D, 0, 
						texFmt,
						sx, sy,
						border, 
						imageCanvasFormat,
						imageCanvasType, 
						(ByteBuffer) vdpCanvasBuffer);
			} else if (vdpCanvasBuffer instanceof ShortBuffer) {
				glTexImage2D(GL_TEXTURE_2D, 0, 
						texFmt,
						sx, sy,
						border, 
						imageCanvasFormat,
						imageCanvasType, 
						(ShortBuffer) vdpCanvasBuffer);
			} else if (vdpCanvasBuffer instanceof IntBuffer) {
				glTexImage2D(GL_TEXTURE_2D, 0, 
						texFmt,
						sx, sy,
						border, 
						imageCanvasFormat,
						imageCanvasType, 
						(IntBuffer) vdpCanvasBuffer);
			}	
		}
		
		Util.checkGLError();
		if (supportsMultiTexture) {
			/*
			 * Second texture: the monitor overlay
			 */
			glActiveTexture(GL_TEXTURE1);
			if (params.getTexture() != null) {
				try {
					textureLoader.getTexture(params.getTexture()).bind();
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
		}
		
		Integer displayList = displayListMap.get(effect);
		if (displayList == null) {
			effect.getRender().init();
			
			displayList = glGenLists(1);
			glNewList(displayList, GL_COMPILE);
			
			GL11.glPushMatrix();
			if (glVersion < 1.3f) {
				glScalef(1, (float) sy / vdpCanvas.getVisibleHeight(), 1);
			}
			effect.getRender().render();
			GL11.glPopMatrix();
			
			
			glEndList();
			displayListMap.put(effect, displayList);
		}
		glCallList(displayList);
		
		glDisable(GL_TEXTURE_2D);

		if (supportsShaders) {
			ARBShaderObjects.glUniform1iARB(
					ARBShaderObjects.glGetUniformLocationARB(programObject, "time"), 
					(int) System.currentTimeMillis());
			
			if (programObject != 0) {
				ARBShaderObjects.glUseProgramObjectARB(0); 
			}
		}

//		for (ISwtSprite sprite : sprites) {
//			
//		}
		
		glCanvas.swapBuffers();

		// HACK for Intel Mobile Express Graphics --
		// if shaders are compiled/linked BEFORE an initial render,
		// the whole ig4icd[32|64].dll DLL crashes and burns
		if (supportsShaders && programObject == 0) {
			compileLinkShaders();
		}
		
		frames++;
		long lastTime = System.currentTimeMillis();
		
		lastFrameTime = lastTime;
		
		frameTimes += (lastTime - firstTime);
		
		if (lastReport + 1000 <= lastFrameTime) {
//			System.out.println("Max FPS: " + 1000 * frames / frameTimes);
			lastReport = lastTime;
		}
		
		if (params.isRefreshRealtime()) {
			//getControl().redraw();
		}
	}
	

	protected void drawQuad(float cx, float cy, float cx2, float cy2,
			float tx, float ty, float tx2, float ty2) {
		if (supportsMultiTexture)
			glMultiTexCoord2f(GL_TEXTURE1, tx, ty);
		glTexCoord2f(tx, ty);		
		glVertex2f(cx, cy);
		if (supportsMultiTexture)
			glMultiTexCoord2f(GL_TEXTURE1, tx, ty2);
		glTexCoord2f(tx, ty2);		
		glVertex2f(cx, cy2);
		if (supportsMultiTexture)
			glMultiTexCoord2f(GL_TEXTURE1, tx2, ty2);
		glTexCoord2f(tx2, ty2);		
		glVertex2f(cx2, cy2);
		if (supportsMultiTexture)
			glMultiTexCoord2f(GL_TEXTURE1, tx2, ty);
		glTexCoord2f(tx2, ty);		
		glVertex2f(cx2, cy);
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.SwtVideoRenderer#getMonitorEffectSupport()
	 */
	@Override
	public IMonitorEffectSupport getMonitorEffectSupport() {
		return monitorEffectSupport;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.SwtVideoRenderer#getActualScreenshotImageData()
	 */
	@Override
	public ImageData getActualScreenshotImageData() {
		int shotTexture = glGenTextures();
		
		try {
			Rectangle bounds = glCanvas.getClientArea();
			int w = bounds.width, h = bounds.height;

			glEnable(GL_TEXTURE_2D);
			if (supportsMultiTexture) {
				glActiveTexture(GL_TEXTURE0);
			}

			glGetError();
			
			glBindTexture(GL_TEXTURE_2D, shotTexture);
			glViewport(0, 0, w, h);
			glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, w, h, 0);
			
			if (glGetError() != 0) {
				w = 1; while (w <= bounds.width) w += w;
				h = 1; while (h <= bounds.height) h += h;
				glViewport(0, 0, w, h);
				glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, bounds.width, bounds.height, 0);
				if (glGetError() != 0) {
					return null;
				}
			}
			
			int span = bounds.width * 3;
			int length = h * span;
			ByteBuffer pixels = ByteBuffer.allocateDirect(length);
			glGetTexImage(GL_TEXTURE_2D, 0, GL_RGB, GL_UNSIGNED_BYTE, pixels);
			
			ImageData data = ImageUtils.createStandard24BitImageData(
					bounds.width, bounds.height);
			pixels.rewind();
			for (int r = bounds.height; r-- > 0; ) {
				pixels.get(data.data, r * span, span);
			}
			
			return data;
			
		} finally {
			glDeleteTextures(shotTexture);
		}
		
	}
}
