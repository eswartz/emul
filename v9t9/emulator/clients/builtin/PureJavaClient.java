/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.SoundHandler;
import v9t9.engine.VdpHandler;
import v9t9.keyboard.KeyboardState;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class PureJavaClient implements Client {
    VdpHandler video;
    CruHandler cru;
    private Machine machine;
	private SoundHandler sound;
	private KeyboardState keyboardState;
	private KeyboardHandler keyboardHandler;
	private VideoRenderer videoRenderer;
	private Display display;
	private long avgUpdateTime;
	private long expectedUpdateTime;
	private int displaySkips;

	private final int QUANTUM = 1000 / 60;
	
    public PureJavaClient(final Machine machine, VdpHandler vdp, Display display) {
    	this.display = display;
        this.machine = machine;
        
        expectedUpdateTime = QUANTUM;
        
        if (false && videoRenderer == null && SWT.getPlatform().equals("gtk")) {
        	// try OpenGL first ?
        	try {
        		Class<?> klass = getClass().getClassLoader().loadClass(
        				SwtVideoRenderer.class.getName() + "OGL");
        		videoRenderer = (VideoRenderer) klass.getConstructor(Display.class).newInstance(
        				display);
        	} catch (Exception e) {
        		System.err.println("Cannot load OpenGL/GTK-specific support: " +e.getMessage());
        	}
        }
        
        if (videoRenderer == null && SWT.getPlatform().equals("gtk")) {
        	try {
	        	Class<?> klass = getClass().getClassLoader().loadClass(
	        			SwtVideoRenderer.class.getName() + "GTK");
	        	videoRenderer = (VideoRenderer) klass.getConstructor(Display.class).newInstance(
	        			display);
        	} catch (Exception e) {
        		System.err.println("Cannot load GTK-specific support: " +e.getMessage());
        	}
        }
        if (videoRenderer == null)
        	videoRenderer = new SwtVideoRenderer(display);
        video = vdp;
        video.setCanvas(videoRenderer.getCanvas());
        
        Shell shell = ((SwtVideoRenderer) videoRenderer).getShell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isRunning()) {
		        	machine.setNotRunning();
		        }
			}
		});

        keyboardState = new KeyboardState();
        cru = new InternalCru(machine, keyboardState);
        
        sound = new SoundHandler() {

			public void writeSound(byte val) {
				
			}
        	
        };
        
        keyboardHandler = new SwtKeyboardHandler(((SwtVideoRenderer) videoRenderer).getWidget(), keyboardState);
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
    	keyboardHandler.scan(keyboardState);
    }
    
    public void updateVideo() {
    	//long start = System.currentTimeMillis();
    	if (videoRenderer.isIdle() ) {
    		try {
    			video.update();
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

    /* (non-Javadoc)
     * @see v9t9.Client#getSound()
     */
    public SoundHandler getSoundHandler() {
        return sound;
    }

    /*
     *  (non-Javadoc)
     * @see v9t9.Client#setSoundHandler(v9t9.sound.SoundHandler)
     */
    public void setSoundHandler(SoundHandler handler) {
        this.sound = handler;
    }
   
    public CruHandler getCruHandler() {
        return cru;
    }
    
    public void setCruHandler(CruHandler handler) {
        this.cru = handler;
    }
    
    public void handleEvents() {
    	while (display.readAndDispatch()) ;
    }
    
    public boolean isAlive() {
    	return !display.isDisposed();
    }
}

