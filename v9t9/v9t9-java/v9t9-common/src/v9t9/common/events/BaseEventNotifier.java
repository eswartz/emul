/*
  BaseEventNotifier.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import v9t9.common.events.NotifyEvent.Level;
import ejs.base.utils.ListenerList;



/**
 * Handle user notifications, which are dispensed in order of severity, then time, then content.
 * @author ejs
 *
 */
public class BaseEventNotifier implements IEventNotifier {

	protected List<NotifyEvent> events;
	protected int baseIdx;
	private int errorCount;
	
	private ListenerList<IEventNotifierListener> listeners = new ListenerList<IEventNotifierListener>();
	
	public BaseEventNotifier() {
		events = new ArrayList<NotifyEvent>();
	}
	
	public BaseEventNotifier(Collection<NotifyEvent> events) {
		events = new ArrayList<NotifyEvent>(events);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.IEventNotifier#getNotificationCount()
	 */
	@Override
	public int getNotificationCount() {
		return NotifyEvent.ORDER;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.IEventNotifier#getNotificationCount()
	 */
	@Override
	public int getErrorCount() {
		return errorCount;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.IEventNotifier#notifyEvent(java.lang.Object, v9t9.emulator.clients.builtin.Level, java.lang.String)
	 */
	@Override
	public final void notifyEvent(Object context, Level level, String message) {
		NotifyEvent event = new NotifyEvent(System.currentTimeMillis(), context, level, message);
		notifyEvent(event);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.IEventNotifier#notifyEvent(v9t9.emulator.clients.builtin.IEventNotifier.NotifyEvent)
	 */
	@Override
	public final void notifyEvent(final NotifyEvent event) {
		if (event.level == Level.ERROR)
			errorCount++;
		events.add(event);
		
		listeners.fire(new ListenerList.IFire<IEventNotifierListener>() {

			@Override
			public void fire(IEventNotifierListener listener) {
				listener.eventNotified(event);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifier#addListener(v9t9.common.events.IEventNotifierListener)
	 */
	@Override
	public void addListener(IEventNotifierListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifier#removeListener(v9t9.common.events.IEventNotifierListener)
	 */
	@Override
	public void removeListener(IEventNotifierListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifier#fetchEvent(int)
	 */
	@Override
	public NotifyEvent fetchEvent(int index) {
		return index >= baseIdx && index < baseIdx + this.events.size() 
				? this.events.get(index - baseIdx) : null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifier#getEvents()
	 */
	@Override
	public Collection<NotifyEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
