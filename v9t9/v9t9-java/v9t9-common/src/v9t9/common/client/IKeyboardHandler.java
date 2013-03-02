/*
  IKeyboardHandler.java

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

/**
 * @author ejs
 */
public interface IKeyboardHandler {
	SettingSchema settingPasteKeyDelay = new SettingSchema(
			ISettingsHandler.MACHINE,
			"PasteKeyDelay", 20);
	
	void init(IVideoRenderer renderer);
	void setEventNotifier(IEventNotifier notifier);
	
//	KeyDelta[] scanKeyDeltas();

	void cancelPaste();

	/**
	 * Paste text into the clipboard
	 * @param contents
	 */
	void pasteText(String contents);

	boolean isPasting();

	void resetProbe();

	void setProbe();
	
	boolean anyKeyAvailable();

	boolean postCharacter(boolean pressed, byte shift, char ch);

}
