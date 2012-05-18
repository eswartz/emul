/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;
import java.util.Queue;

/**
 * @author ejs
 *
 */
public interface IDemoInputEventBuffer extends IDemoInputBuffer {

	/**
	 * Get the unique code for the buffer
	 * @return
	 */
	int getCode();
	
	/**
	 * Get the unique identifier for the buffer
	 * @return
	 */
	String getIdentifier();
	
	/**
	 * Decode events from the buffer
	 * @param queuedEvents
	 */
	void decodeEvents(Queue<IDemoEvent> queuedEvents) throws IOException;

	/**
	 * @param string
	 * @return
	 */
	IOException newBufferException(String string);

}
