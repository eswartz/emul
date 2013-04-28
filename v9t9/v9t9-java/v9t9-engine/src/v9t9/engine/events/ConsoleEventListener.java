/**
 * 
 */
package v9t9.engine.events;

import v9t9.common.events.IEventNotifierListener;
import v9t9.common.events.NotifyEvent;

/**
 * @author ejs
 *
 */
public class ConsoleEventListener implements IEventNotifierListener {

	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifierListener#eventNotified(v9t9.common.events.NotifyEvent)
	 */
	@Override
	public void eventNotified(NotifyEvent event) {
		System.err.println(event);
	}
}
