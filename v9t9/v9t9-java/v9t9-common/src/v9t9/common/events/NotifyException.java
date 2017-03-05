/*
  NotifyException.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.events;

import v9t9.common.events.NotifyEvent.Level;


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
		this.event = new NotifyEvent(System.currentTimeMillis(), context, Level.ERROR, 
				cause != null ? message + "\n" + cause.getMessage() : message);
	}
	
	public NotifyEvent getEvent() {
		return event;
	}
}
