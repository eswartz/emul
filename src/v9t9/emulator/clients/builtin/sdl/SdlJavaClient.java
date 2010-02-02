/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.clients.builtin.sdl;

import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.event.SDLExposeEvent;
import sdljava.event.SDLKeyboardEvent;
import sdljava.event.SDLMouseButtonEvent;
import sdljava.event.SDLMouseMotionEvent;
import sdljava.event.SDLResizeEvent;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.KeyboardHandler;
import v9t9.engine.VdpHandler;

/**
 * This client uses SDL for the video and keyboard.
 * @author ejs
 */
public class SdlJavaClient implements Client {
    VdpHandler video;
    CruHandler cruHandler;
    private Machine machine;
	private SdlKeyboardHandler keyboardHandler;
	private SdlVideoRenderer videoRenderer;
	private SdlWindow window;

    public SdlJavaClient(final Machine machine, VdpHandler vdp) {
    	try {
			window = new SdlWindow(machine);
			videoRenderer = window.getVideoRenderer();
		} catch (SDLException e) {
			e.printStackTrace();
			System.exit(1);
		}
    	
        this.machine = machine;
        video = vdp;
        
        
        video.setCanvas(videoRenderer.getCanvas());

        
        
        /*Shell shell = window.getShell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isRunning()) {
		        	machine.setNotRunning();
		        }
			}
		});*/

        cruHandler = machine.getCru(); 
        machine.getSound().setSoundHandler(new JavaSoundHandler(machine));
        
        //keyboardHandler = new SwtKeyboardHandler(((SwtVideoRenderer) videoRenderer).getWidget(),
        //		machine.getKeyboardState(), machine);
        keyboardHandler = new SdlKeyboardHandler(machine.getKeyboardState(), machine);
    }
    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#close()
     */
    public void close() {
    	try {
    		machine.stop();
    		window.dispose();
    		SDLMain.quit();
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
    	}
    }

    public KeyboardHandler getKeyboardHandler() {
    	return keyboardHandler;
    }
    
    public void handleEvents() {
    	SDLEvent event;
    	/* Poll input queue, run keyboard loop */
		try {
			while ( (event = SDLEvent.pollEvent()) != null ) {
				if ( event.getType() == SDLEvent.SDL_QUIT ) {
					close();
					continue;
				}
				if (event.getType() == SDLEvent.SDL_KEYUP ||
						event.getType() == SDLEvent.SDL_KEYDOWN) {
					keyboardHandler.handleEvent((SDLKeyboardEvent) event);
					continue;
				}
				if (event.getType() == SDLEvent.SDL_VIDEORESIZE) {
					window.handleResize((SDLResizeEvent) event);
					continue;
				}
				if (event.getType() == SDLEvent.SDL_VIDEOEXPOSE) {
					window.handleExpose((SDLExposeEvent) event);
					continue;
				}
				if (event.getType() == SDLEvent.SDL_MOUSEBUTTONDOWN) {
					window.handleMouse((SDLMouseButtonEvent) event);
					continue;
				}
				if (event.getType() == SDLEvent.SDL_MOUSEMOTION) {
					window.handleMouse((SDLMouseMotionEvent) event);
					continue;
				}
			}
		} catch (SDLException e) {
			close();
		}
		
    }
    
    public boolean isAlive() {
    	//return !display.isDisposed();
    	return true;
    }
}

