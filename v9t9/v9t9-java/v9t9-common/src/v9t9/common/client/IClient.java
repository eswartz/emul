/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.common.client;

import v9t9.common.events.IEventNotifier;
import v9t9.common.settings.SettingSchema;


/** The client the emulated machine interacts with.  This could
 * be the emulator itself, hosting a window, keyboard, etc., 
 * or it could be a demo running, or it could be a remote host.
 * 
 * @author ejs
 */
public interface IClient {
	
	SettingSchema settingNewConfiguration = new SettingSchema(
			ISettingsHandler.TRANSIENT, "NewConfiguration", Boolean.FALSE);

	String getIdentifier();
	
    void close();
    
    //ISettingsHandler getSettingsHandler();
    
    /**
     * Handle one timer tick (nominally 1/60 second).  
     *
     */
    //void timerInterrupt();

	boolean isAlive();

	void handleEvents();

	//void updateVideo();

	//IVideoRenderer getVideoRenderer();
	
	//IKeyboardHandler getKeyboardHandler();
	
	IEventNotifier getEventNotifier();

}
