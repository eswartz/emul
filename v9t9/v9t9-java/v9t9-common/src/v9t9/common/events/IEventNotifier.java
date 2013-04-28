/*
  IEventNotifier.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.events;

import java.util.Collection;

import v9t9.common.events.NotifyEvent.Level;

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
	
	void addListener(IEventNotifierListener listener);
	void removeListener(IEventNotifierListener listener);
	
	NotifyEvent fetchEvent(int index);

	/**
	 * @return
	 */
	Collection<NotifyEvent> getEvents();
}
