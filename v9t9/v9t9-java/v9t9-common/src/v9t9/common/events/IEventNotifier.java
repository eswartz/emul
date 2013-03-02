/*
  IEventNotifier.java

  (c) 2010-2011 Edward Swartz

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

/**
 * Handler for events happening inside the emulator, usually presented to the user
 * as a tooltip or balloon.
 * @author ejs
 *
 */
public interface IEventNotifier {
	enum Level {
		INFO(0, "Info"),
		WARNING(1, "Warning"),
		ERROR(2, "Error");

		private int code;
		private String label;
		private Level(int code, String label) {
			this.code = code;
			this.label = label;
		}
		public int getCode() {
			return code;
		}
		public String getLabel() {
			return label;
		}
	}
	
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
}
