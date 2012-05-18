/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

public class TimerTick implements IDemoEvent {

	public static final String ID = "TimerTick";
	private final long elapsedTime;

	/**
	 * @param elapsedTime
	 */
	public TimerTick(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "TimerTick";
	}
	
	/**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
}