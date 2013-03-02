/*
  NotifyException.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
		this.event = new NotifyEvent(System.currentTimeMillis(), context, Level.ERROR, 
				cause != null ? message + "\n" + cause.getMessage() : message);
	}
	
	public NotifyEvent getEvent() {
		return event;
	}
}
