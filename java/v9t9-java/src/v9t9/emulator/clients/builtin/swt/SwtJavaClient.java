/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.awt.AwtKeyboardHandler;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.VdpHandler;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtJavaClient implements Client {
    VdpHandler video;
    private Machine machine;
	private KeyboardHandler keyboardHandler;
	private ISwtVideoRenderer videoRenderer;
	private Display display;
	private long avgUpdateTime;
	private long expectedUpdateTime;
	private int displaySkips;

	private final int QUANTUM = 1000 / 60;
	private IEventNotifier eventNotifier;
	private MouseJoystickHandler mouseJoystickHandler;
	
    public SwtJavaClient(final Machine machine, VdpHandler vdp, boolean awtRenderer) {
    	this.display = new Display();
    	this.video = vdp;
    	
    	if (awtRenderer) {
    		videoRenderer = new SwtAwtVideoRenderer();
    	
    		keyboardHandler = new AwtKeyboardHandler(
        		((SwtAwtVideoRenderer)videoRenderer).getAwtCanvas(),
        		machine.getKeyboardState(), machine);
    	} else {
    		videoRenderer = createSwtVideoRenderer(display);
    		keyboardHandler = new SwtKeyboardHandler(
    				machine.getKeyboardState(), machine);
    	}

        SwtWindow window = new SwtWindow(display, machine);
        eventNotifier = window.getEventNotifier();
        
        if (keyboardHandler instanceof AwtKeyboardHandler)
        	((AwtKeyboardHandler) keyboardHandler).setEventNotifier(eventNotifier);
        
        window.setSwtVideoRenderer((ISwtVideoRenderer) videoRenderer);

        mouseJoystickHandler = new MouseJoystickHandler(videoRenderer, machine.getKeyboardState());
        window.setMouseJoystickHandler(mouseJoystickHandler);
        
        this.machine = machine;
        
        expectedUpdateTime = QUANTUM;
        
        video.setCanvas(videoRenderer.getCanvas());

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
        
        if (keyboardHandler instanceof SwtKeyboardHandler)
        	((SwtKeyboardHandler) keyboardHandler).init(((SwtVideoRenderer) videoRenderer).getWidget());
    }
    /**
	 * @param display2
	 * @return
	 */
	private ISwtVideoRenderer createSwtVideoRenderer(Display display2) {
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
        	videoRenderer = new SwtVideoRenderer();
        return videoRenderer;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.Client#getEventNotifier()
	 */
	@Override
	public IEventNotifier getEventNotifier() {
		return eventNotifier;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    public void close() {
    	try {
    		machine.stop();
    	} catch (TerminatedException e) {
    		// expected
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#getVideo()
     */
    public v9t9.engine.VdpHandler getVideoHandler() {
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(v9t9.engine.VdpHandler video) {
        this.video = video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable {
    	close();
    	super.finalize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#timerTick()
     */
    public void timerInterrupt() {
    	//System.out.print('.');
    	keyboardHandler.scan(machine.getKeyboardState());
    }
    
    public void updateVideo() {
    	//long start = System.currentTimeMillis();
    	if (videoRenderer.isIdle()) { 
			try {
				if (!video.update())
					return;
			} catch (Throwable t) {
				t.printStackTrace();
			}
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

    public KeyboardHandler getKeyboardHandler() {
    	return keyboardHandler;
    }
    
    public void handleEvents() {
    	while (display.readAndDispatch()) ;
    }
    
    public boolean isAlive() {
    	return !display.isDisposed();
    }
    
}

