/**
 * 
 */
package v9t9.emulator.common;

/**
 * Handler for events happening inside the emulator, usually presented to the user
 * as a tooltip or balloon.
 * @author ejs
 *
 */
public interface IEventNotifier {
	enum Level {
		INFO(0, "Info"),
		WARNING(1, "Warning"),
		ERROR(2, "Error");

		private int code;
		private String label;
		private Level(int code, String label) {
			this.code = code;
			this.label = label;
		}
		public int getCode() {
			return code;
		}
		public String getLabel() {
			return label;
		}
	}
	
	/**
	 * Notify of an event
	 * @param context for example, an Event
	 * @param level message level
	 * @param message message to show
	 */
	void notifyEvent(Object context, Level level, String message);
	
	/**
	 * Notify with a pre-created {@link NotifyEvent} instance
	 * @param event
	 */
	void notifyEvent(NotifyEvent event);

	/**
	 * Get monotonically increasing number representing number of notifications
	 * @return notification count
	 */
	int getNotificationCount();
	

	/**
	 * Get monotonically increasing number representing number of errors
	 * @return notification count
	 */
	int getErrorCount();
}
