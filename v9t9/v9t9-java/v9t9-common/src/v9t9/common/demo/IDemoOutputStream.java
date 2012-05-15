/**
 * 
 */
package v9t9.common.demo;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface IDemoOutputStream extends Closeable {
	/**
	 * Write new event.
	 * @return event
	 * @throws IOException 
	 */
	void writeEvent(IDemoEvent event) throws IOException;

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
