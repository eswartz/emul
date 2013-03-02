/*
  IKeyboardHandler.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
