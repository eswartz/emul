/*
 */
package v9t9.common.client;

import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;

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
	
	boolean anyKeyPressed();

	void setKey(int realKey, boolean onoff, boolean synthetic, byte shift, int key, long when);
	boolean postCharacter(IBaseMachine machine, int realKey, boolean pressed, boolean synthetic, byte shift, char ch, long when);

}
