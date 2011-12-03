/**
 * 
 */
package v9t9.common.events;

import v9t9.common.events.IEventNotifier.Level;


/**
 * An exception which can be reported as a notify event
 * @author ejs
 *
 */
public class NotifyException extends Exception {

	private static final long serialVersionUID = 6309762529845908071L;
	
	private NotifyEvent event;

	public NotifyException(NotifyEvent event) {
		super(event.message);
		this.event = event;
	}
	public NotifyException(NotifyEvent event, Throwable cause) {
		super(event.message, cause);
		this.event = event;
	}
	
	public NotifyException(Object context, String message) {
		super(message);
		this.event = new NotifyEvent(System.currentTimeMillis(), context, Level.ERROR, message);
	}
	
	public NotifyException(Object context,String message, Throwable cause) {
		super(message, cause);
		this.event = new NotifyEvent(System.currentTimeMillis(), context, Level.ERROR, message);
	}
	
	public NotifyEvent getEvent() {
		return event;
	}
}
