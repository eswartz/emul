/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
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

    public PureJavaClient(final Machine machine, VdpHandler vdp, Display display) {
    	this.display = display;
        this.machine = machine;
        
        videoRenderer = new SwtVideoRenderer(display, vdp.getCanvas());
        video = vdp;
        
        ((SwtVideoRenderer) videoRenderer).getShell().addShellListener(new ShellAdapter() {
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
        
        keyboardHandler = new SwtKeyboardHandler(((SwtVideoRenderer) videoRenderer).getShell(), keyboardState);
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
    	video.update();
    	videoRenderer.redraw();
    	keyboardHandler.scan(keyboardState);
    	//video.update();
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

