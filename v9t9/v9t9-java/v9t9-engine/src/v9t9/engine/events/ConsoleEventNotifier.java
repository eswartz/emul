/**
 * 
 */
package v9t9.engine.events;

import v9t9.common.events.BaseEventNotifier;
import v9t9.common.events.NotifyEvent;

/**
 * @author ejs
 *
 */
public class ConsoleEventNotifier extends BaseEventNotifier {
	/* (non-Javadoc)
	 * @see v9t9.common.events.BaseEventNotifier#consumeEvent(v9t9.common.events.NotifyEvent)
	 */
	@Override
	protected void consumeEvent(NotifyEvent event) {
		System.err.println(event);
	}
}
