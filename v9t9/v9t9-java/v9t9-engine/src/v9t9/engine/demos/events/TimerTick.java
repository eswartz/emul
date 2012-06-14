/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

/**
 * A timer tick represents the next tick in the demo stream.
 * All events occurring before the tick have been flushed,
 * and the tick's elapsed time represents the next instant
 * in time.
 * @author ejs
 *
 */
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