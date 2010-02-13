/**
 * 
 */
package v9t9.emulator.clients.builtin;

/**
 * Handler for events happening inside the emulator, usually presented to the user
 * as a tooltip or balloon.
 * @author ejs
 *
 */
public interface IEventNotifier {
	/**
	 * Notify of an event
	 * @param context for example, an Event
	 * @param message message to show
	 */
	void notifyEvent(Object context, String message);
}
