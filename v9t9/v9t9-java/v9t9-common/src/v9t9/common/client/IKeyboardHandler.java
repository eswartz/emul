/*
 */
package v9t9.common.client;

import v9t9.common.events.IEventNotifier;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 */
public interface IKeyboardHandler {
	SettingSchema settingPasteKeyDelay = new SettingSchema(
			ISettingsHandler.WORKSPACE,
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
