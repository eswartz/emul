/**
 * 
 */
package v9t9.common.events;

import java.io.PrintStream;

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

	public void print(PrintStream out) {
		out.println(level.getLabel() + ": " + message);
	}
	
}