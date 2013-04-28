/*
  GuiEventNotifier.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifierListener;
import v9t9.common.events.NotifyEvent;

/**
 * @author ejs
 *
 */
public abstract class BaseGuiEventNotifier implements IEventNotifierListener {
	private Thread displayer;
	protected int position;
	protected IEventNotifier notifier;
	
	public BaseGuiEventNotifier(IEventNotifier notifier) 
	{
		this.notifier = notifier;
		
		startConsumerThread();
	}
	
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

					event = notifier.fetchEvent(position++);
					if (event != null) {
						consumeEvent(event);
					}
				}
			}
		};
		displayer.setDaemon(true);
		displayer.start();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.events.IEventNotifierListener#eventNotified(v9t9.common.events.NotifyEvent)
	 */
	@Override
	public void eventNotified(NotifyEvent event) {
		// ignore
	}
	
	abstract protected boolean canConsume();

	abstract protected void consumeEvent(NotifyEvent event);
}