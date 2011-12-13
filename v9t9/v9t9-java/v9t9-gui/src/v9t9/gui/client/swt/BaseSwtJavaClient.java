/**
 * 
 */
package v9t9.gui.client.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IClient;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.TerminatedException;
import v9t9.common.settings.Settings;
import v9t9.gui.client.awt.AwtKeyboardHandler;
import v9t9.gui.sound.JavaSoundHandler;

/**
 * @author ejs
 *
 */
public abstract class BaseSwtJavaClient implements IClient {

	static {
		// set early for GNOME Shell
		Display.setAppName("V9t9");
	}
	
	protected IVdpChip video;
	protected IMachine machine;
	protected IKeyboardHandler keyboardHandler;
	protected ISwtVideoRenderer videoRenderer;
	protected Display display;
	private long avgUpdateTime;
	protected long expectedUpdateTime;
	private int displaySkips;
	protected final int QUANTUM = 1000 / 60;
	protected IEventNotifier eventNotifier;
	protected final ISettingsHandler settingsHandler;

	/**
	 * @param machine 
	 * 
	 */
	public BaseSwtJavaClient(final IMachine machine) {
		this.settingsHandler = Settings.getSettings(machine);
		this.display = Display.getDefault();
    	this.video = machine.getVdp();
    	this.machine = machine;
    	
    	setupRenderer();


        final SwtWindow window = new SwtWindow(display, machine, (ISwtVideoRenderer) videoRenderer, settingsHandler);
        eventNotifier = window.getEventNotifier();
        
        if (keyboardHandler instanceof AwtKeyboardHandler)
        	((AwtKeyboardHandler) keyboardHandler).setEventNotifier(eventNotifier);
        
        //window.setSwtVideoRenderer((ISwtVideoRenderer) videoRenderer);

        expectedUpdateTime = QUANTUM;
        
        //video.setCanvas(videoRenderer.getCanvas());

        Shell shell = window.getShell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isAlive()) {
		        	machine.setNotRunning();
		        }
			}
		});
		
        machine.getSound().setSoundHandler(new JavaSoundHandler(machine));
        
        if (keyboardHandler instanceof ISwtKeyboardHandler)
        	((ISwtKeyboardHandler) keyboardHandler).init(((ISwtVideoRenderer) videoRenderer).getControl());
	}
	

	abstract protected void setupRenderer();

	/**
	 * @param display2
	 * @return
	 */
	protected ISwtVideoRenderer createSwtVideoRenderer(Display display2) {
	   	ISwtVideoRenderer videoRenderer = null;
		if (false && videoRenderer == null && SWT.getPlatform().equals("gtk")) {
	    	// try OpenGL first ?
	    	try {
	    		Class<?> klass = Class.forName(
	    				SwtVideoRenderer.class.getName() + "OGL");
	    		videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load OpenGL/GTK-specific support: " +e.getMessage());
	    	}
	    }
	
	    if (false && videoRenderer == null) {
	    	// try J3D first ?
	    	try {
	    		Class<?> klass = Class.forName(
	    				SwtVideoRenderer.class.getName() + "J3D");
	    		videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load J3D support: " +e.getMessage());
	    	}
	    }
	
	    /*
	    if (videoRenderer == null && SWT.getPlatform().equals("gtk")) {
	    	try {
	        	Class<?> klass = Class.forName(
	        			SwtVideoRenderer.class.getName() + "GTK");
	        	videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load GTK-specific support: " +e.getMessage());
	    	}
	    }*/
	    if (videoRenderer == null)
	    	videoRenderer = new SwtVideoRenderer(settingsHandler, video);
	    return videoRenderer;
	}

	@Override
	public IEventNotifier getEventNotifier() {
		return eventNotifier;
	}

	public void close() {
		try {
			machine.stop();
		} catch (TerminatedException e) {
			// expected
		}
		if (videoRenderer != null)
			videoRenderer.dispose();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void timerInterrupt() {
		//System.out.print('.');
		keyboardHandler.scan(machine.getKeyboardState());
	}

	public void updateVideo() {
		//long start = System.currentTimeMillis();
		if (videoRenderer.isIdle() && videoRenderer.isVisible()) {
			/*
			try {
				if (!video.update())
					return;
			} catch (Throwable t) {
				t.printStackTrace();
			}
			*/
			videoRenderer.getCanvasHandler().update();
			videoRenderer.redraw();
			// compensate for slow frames
	    	long elapsed = videoRenderer.getLastUpdateTime() * 4;
	    	//System.out.println(elapsed + " / " + expectedUpdateTime);
			if (elapsed * 2 >= expectedUpdateTime) {
				//System.out.println("slow :" + elapsed);
				//displaySkips += (elapsed + 1000 / 60 - 1) / (1000 / 60);
				expectedUpdateTime <<= 1;
				if (expectedUpdateTime > 1000)
					expectedUpdateTime = 1000;
				displaySkips = 0;
				//nextVideoUpdate = next + avgUpdateTime;
			} else if (elapsed <= expectedUpdateTime / 2 && elapsed >= QUANTUM) {
				displaySkips++;
				if (displaySkips > 30) {
					//System.out.println("fast :" + (elapsed));
	    			expectedUpdateTime >>= 1;
	    			if (expectedUpdateTime < QUANTUM)
	    				expectedUpdateTime = QUANTUM;
	    			displaySkips = 0;
				}
				//nextVideoUpdate = start + 1000 / 60;
			} else {
				displaySkips = 0;
			}
			//nextVideoUpdate = System.currentTimeMillis() + expectedUpdateTime * 4;
			avgUpdateTime = (avgUpdateTime + elapsed * 9) / 10; 
		} else {
			//displaySkips--;
		}
	}

	public IKeyboardHandler getKeyboardHandler() {
		return keyboardHandler;
	}

	public void handleEvents() {
		try {
			while (display.readAndDispatch()) ;
		} catch (SWTException e) {
			e.printStackTrace();
		} catch (SWTError e) {
			e.printStackTrace();
		}
	}

	public boolean isAlive() {
		return !display.isDisposed();
	}
	
}