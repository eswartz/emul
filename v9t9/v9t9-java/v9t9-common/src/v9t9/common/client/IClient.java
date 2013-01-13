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

	void start();

    void close();
    
    void tick();

	boolean isAlive();

	void handleEvents();
	
	IEventNotifier getEventNotifier();

	/**
	 * Run a task in the machine's asyncExec loop, but in the UI thread.
	 * @param runnable
	 */
	void asyncExecInUI(Runnable runnable);
	
	IVideoRenderer getVideoRenderer();

}
