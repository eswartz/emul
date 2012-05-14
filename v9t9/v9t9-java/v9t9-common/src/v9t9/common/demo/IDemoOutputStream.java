/**
 * 
 */
package v9t9.common.demo;

import java.io.Closeable;

import v9t9.common.events.NotifyException;

/**
 * @author ejs
 *
 */
public interface IDemoOutputStream extends Closeable {
	/**
	 * Write new event.
	 * @return event
	 * @throws NotifyException 
	 */
	void writeEvent(IDemoEvent event) throws NotifyException;

	/**
	 * Get the rate at which timer events occur
	 * @return Hz
	 */
	int getTimerRate();

	/**
	 * Get the position of the stream in bytes
	 */
	long getPosition();
	
	/**
	 * Get the elapsed time of the demo (after the previous event) in ms.
	 */
	long getElapsedTime();

}
