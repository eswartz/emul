/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface IDemoEventFormatter {
	String getBufferIdentifer();
	String getEventIdentifier();
	
	IDemoEvent readEvent(IDemoInputEventBuffer buffer) throws IOException;
	void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event) throws IOException;

}
