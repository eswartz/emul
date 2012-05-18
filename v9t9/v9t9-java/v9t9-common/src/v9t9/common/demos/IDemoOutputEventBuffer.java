/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface IDemoOutputEventBuffer extends IDemoOutputBuffer {

	/**
	 * Encode the event type registered to this buffer
	 * @param event
	 * @throws IOException 
	 */
	void encodeEvent(IDemoEvent event) throws IOException;
}
