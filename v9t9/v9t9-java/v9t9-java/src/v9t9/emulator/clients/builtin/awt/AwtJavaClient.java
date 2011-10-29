/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.VdpHandler;

/**
 * This client uses SDL for the video and keyboard.
 * @author ejs
 */
public class AwtJavaClient implements Client {
	public static String ID = "AWT";
	
    VdpHandler video;
    private Machine machine;
	private AwtKeyboardHandler keyboardHandler;
	private AwtVideoRenderer videoRenderer;
	private AwtWindow window;

    public AwtJavaClient(final Machine machine) {
    	this.machine = machine;
        video = machine.getVdp();
        
        init();
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.Client#getIdentifier()
     */
    @Override
    public String getIdentifier() {
    	return ID;
    }
    protected void init() {
    	window = new AwtWindow( machine);
		
		videoRenderer = window.getVideoRenderer();
    	
		window.getFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
        
        
        video.setCanvas(videoRenderer.getCanvas());

        machine.getSound().setSoundHandler(new JavaSoundHandler(machine));
        
        keyboardHandler = new AwtKeyboardHandler(
        		videoRenderer.getAwtCanvas(),
        		machine.getKeyboardState(), machine);
        keyboardHandler.setEventNotifier(window.getEventNotifier());
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.Client#getEventNotifier()
     */
    @Override
    public IEventNotifier getEventNotifier() {
    	return window.getEventNotifier();
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
    		window.dispose();
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
    	keyboardHandler.scan(machine.getKeyboardState());
    }
    
    public void updateVideo() {
    	if (videoRenderer.isIdle()) { 
			try {
				if (!video.update())
					return;
			} catch (Throwable t) {
				t.printStackTrace();
			}
    		videoRenderer.redraw();
    	}
    }

    public KeyboardHandler getKeyboardHandler() {
    	return keyboardHandler;
    }

    public void handleEvents() {
    	
    }
    public boolean isAlive() {
    	return true;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.Client#getVideoRenderer()
     */
    @Override
    public VideoRenderer getVideoRenderer() {
    	return videoRenderer;
    }
}

