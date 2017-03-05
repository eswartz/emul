/*
  NotifyEvent.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.events;

public class NotifyEvent implements Comparable<NotifyEvent> {
	public static enum Level {
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