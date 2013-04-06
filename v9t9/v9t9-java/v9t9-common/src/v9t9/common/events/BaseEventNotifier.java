/*
  BaseEventNotifier.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.events;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;



/**
 * Handle user notifications, which are dispensed in order of severity, then time, then content.
 * @author ejs
 *
 */
public abstract class BaseEventNotifier implements IEventNotifier {

	protected PriorityBlockingQueue<NotifyEvent> pendingEvents;
	private Thread displayer;
	private int errorCount;
	
	public BaseEventNotifier() {
		pendingEvents = new PriorityBlockingQueue<NotifyEvent>();
	}
	
	public BaseEventNotifier(Collection<NotifyEvent> events) {
		pendingEvents = new PriorityBlockingQueue<NotifyEvent>(events);
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
	
	protected interface IEventConsumer {
		void consumeEvent(NotifyEvent event);
	}
	
	/**
	 * Start a thread which consumes events as they become available.
	 * Calls the #consumeEvent() method.
	 */
	protected final void startConsumerThread() {
		displayer = new Thread("Event notifier") {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				while (true) {
					while (!canConsume()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							return;
						}
					}
					NotifyEvent event = null;

					try {
						event = pendingEvents.take();
					} catch (InterruptedException e) {
						return;
					}
					
					consumeEvent(event);
				}
			}
		};
		displayer.setDaemon(true);
		displayer.start();
	}
	
	/**
	 * Tell if you are ready to consume another event.  This does not mean
	 * one is ready, only that other events have been properly handled.
	 * @return
	 */
	protected boolean canConsume() { 
		return true;
	}
	/** 
	 * Consume the next event.
	 * @param event
	 */
	abstract protected void consumeEvent(NotifyEvent event);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if (displayer != null) {
			displayer.interrupt();
		}
		super.finalize();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.IEventNotifier#notifyEvent(java.lang.Object, v9t9.emulator.clients.builtin.IEventNotifier.Level, java.lang.String)
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
	public final void notifyEvent(NotifyEvent event) {
		if (event.level == Level.ERROR)
			errorCount++;
		pendingEvents.put(event);
	}
	
	/**
	 * @return
	 */
	public final NotifyEvent getNextEvent() {
		return pendingEvents.poll();
	}
	/**
	 * @return
	 */
	public final NotifyEvent peekNextEvent() {
		return pendingEvents.peek();
	}

}
