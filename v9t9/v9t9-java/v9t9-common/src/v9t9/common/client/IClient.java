/*
  IClient.java

  (c) 2005-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
