/*
 */
package v9t9.common.client;

import v9t9.common.events.IEventNotifier;

/**
 * @author ejs
 */
public interface IKeyboardHandler {
	void init(IVideoRenderer renderer);
	void setEventNotifier(IEventNotifier notifier);
	
//	KeyDelta[] scanKeyDeltas();
	
	void setPasteKeyDelay(int times);

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
