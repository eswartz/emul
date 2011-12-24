/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.gui.client.awt;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.TimerTask;

import ejs.base.timer.FastTimer;

import v9t9.audio.sound.SoundGeneratorFactory;
import v9t9.audio.speech.SpeechGeneratorFactory;
import v9t9.common.client.IClient;
import v9t9.common.events.IEventNotifier;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.TerminatedException;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.speech.ISpeechGenerator;
import v9t9.gui.sound.JavaSoundHandler;

/**
 * This client uses AWT for the video and keyboard.
 * @author ejs
 */
public class AwtJavaClient implements IClient {
	public static String ID = "AWT";
	
    protected final int soundUpdateTick = 100;
    protected final int clientTick = 100;
    protected final int videoUpdateTick = 30;
    
    IVdpChip video;
    private IMachine machine;
	private AwtKeyboardHandler keyboardHandler;
	private AwtVideoRenderer videoRenderer;
	private AwtWindow window;

	protected FastTimer timer;

	private ISoundGenerator soundGenerator;

	private ISpeechGenerator speechGenerator;
	private JavaSoundHandler soundHandler;

    public AwtJavaClient(IMachine machine) {
		this.machine = machine;
		timer = new FastTimer();
        video = machine.getVdp();
        
        init();
    }
    
    @Override
    public String getIdentifier() {
    	return ID;
    }
    
    protected void init() {
    	window = new AwtWindow( machine, timer);
		
		videoRenderer = window.getVideoRenderer();
    	
		window.getFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
        
        //video.setCanvas(videoRenderer.getCanvas());
		soundGenerator = SoundGeneratorFactory.createSoundGenerator(machine);
		speechGenerator = SpeechGeneratorFactory.createSpeechGenerator(machine);
        soundHandler = new JavaSoundHandler(machine, soundGenerator, speechGenerator);
        
        keyboardHandler = new AwtKeyboardHandler(
        		videoRenderer.getAwtCanvas(),
        		machine.getKeyboardState(), machine);
        keyboardHandler.setEventNotifier(window.getEventNotifier());
        

        // the client's interrupt task, which lets it monitor
        // other less expensive devices like the keyboard, sound,
        // etc.
        TimerTask clientTask = new TimerTask() {
        	
        	@Override
        	public void run() {
        		keyboardHandler.scan(machine.getKeyboardState());
        	}
        };
        timer.scheduleTask(clientTask, clientTick);
        
        // the potentially expensive task of blitting the screen to the
        // physical screen -- not scheduled at a fixed rate to avoid
        // overloading the CPU with pending redraw requests
        TimerTask videoUpdateTask = new TimerTask() {

            @Override
			public void run() {
            	if (videoRenderer.isIdle()) { 
            		videoRenderer.redraw();
            	}
            }
        };
        timer.scheduleTask(videoUpdateTask, videoUpdateTick);
        
        TimerTask soundUpdateTask = new TimerTask() {

            @Override
			public void run() {
            	soundHandler.flushAudio();
            }
        };
        timer.scheduleTask(soundUpdateTask, soundUpdateTick);
    }
    
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
    public v9t9.common.hardware.IVdpChip getVideoHandler() {
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(v9t9.common.hardware.IVdpChip video) {
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
    
    public void handleEvents() {
    	
    }
    public boolean isAlive() {
    	return true;
    }
    
}

