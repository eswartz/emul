/*
  ConsoleEventListener.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
