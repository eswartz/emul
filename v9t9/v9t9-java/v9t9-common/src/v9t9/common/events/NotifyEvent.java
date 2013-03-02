/*
  NotifyEvent.java

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

public class NotifyEvent implements Comparable<NotifyEvent> {
	public static int ORDER;
	
	public int order;
	public Level level;
	public long timestamp;
	public Object context;
	public String message;
	public boolean isPriority;
	
	public NotifyEvent(long timestamp, Object context,
			Level level, String message) {
		this.order = ORDER++;
		this.timestamp = timestamp;
		this.context = context;
		this.level = level;
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NotifyEvent o) {
		// sort by severity first
		int diff = level.getCode() - o.level.getCode();
		if (diff != 0)
			return -diff;
		
		// then by order
		diff = (int) (order - o.order);
		return diff;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return level.getLabel() + ": " + message;
	}
	
}