/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine;

import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.common.IEventNotifier;


/** The client the emulated machine interacts with.  This could
 * be the emulator itself, hosting a window, keyboard, etc., 
 * or it could be a demo running, or it could be a remote host.
 * 
 * @author ejs
 */
public interface Client {
	
	String getIdentifier();
	
    void close();
    
    /**
     * Handle one timer tick (nominally 1/60 second).  
     *
     */
    void timerInterrupt();

	boolean isAlive();

	void handleEvents();

	void updateVideo();

	VideoRenderer getVideoRenderer();
	
	KeyboardHandler getKeyboardHandler();
	
	IEventNotifier getEventNotifier();
}
