/**
 * 
 */
package v9t9.common.demos;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ejs
 *
 */
public interface IDemoInputStream extends Closeable {
	/** Get underlying stream */
	InputStream getInputStream();

	/**
	 * Read next event.
	 * @return event, or <code>null</code> once the stream is exhausted
	 * @throws IOException 
	 */
	IDemoEvent readNext() throws IOException;

	/**
	 * Get the rate at which the timer ticks.
	 * @return Hz
	 */
	int getTimerRate();
	
	/**
	 * Get the position of the stream in bytes
	 */
	long getPosition();
	
	/**
	 * Get the elapsed time of the demo (before the next event) in ms.
	 */
	long getElapsedTime();
}
