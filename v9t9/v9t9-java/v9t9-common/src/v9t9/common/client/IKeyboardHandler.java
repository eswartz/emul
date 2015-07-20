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
import v9t9.common.keyboard.IPasteListener;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 */
public interface IKeyboardHandler {

	public static final char WAIT_FOR_FLUSH = '\uFFFC';
	public static final char WAIT_VIDEO = '\uFFFD';
	public static final char FCTN = '\uFFFB';
	public static final char HOLD_DOWN = '\uFFFA';
	public static final char RELEASE = '\uFFF9';

	
	SettingSchema settingPasteKeyDelay = new SettingSchema(
			ISettingsHandler.MACHINE,
			"PasteKeyDelay", 20);
	
	void init(IVideoRenderer renderer);
	void setEventNotifier(IEventNotifier notifier);
	
//	KeyDelta[] scanKeyDeltas();

	void addPasteListener(IPasteListener listener);
	void removePasteListener(IPasteListener listener);
	
	void cancelPaste();
	void finishPaste();

	/**
	 * Paste text into the clipboard
	 * @param contents
	 */
	void pasteText(String contents);

	boolean isPasting();

	void resetProbe();

	void setProbe();
	
	boolean anyKeyAvailable();

	boolean postCharacter(long time, boolean pressed, byte shift, char ch);
	/**
	 * @return
	 */
	IMachine getMachine();
	/**
	 * 
	 */
	void flushCurrentGroup();
	/**
	 * @return
	 */
	boolean isAnyKeyPending();
	/**
	 * 
	 */
	void applyKeyGroup();

}
