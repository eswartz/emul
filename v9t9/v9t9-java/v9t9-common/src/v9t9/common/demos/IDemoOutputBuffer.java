/**
 * 
 */
package v9t9.common.demos;

import java.io.IOException;

/**
 * This interface represents a buffered amount of demo data that
 * is encoded per timer tick.
 * @author ejs
 *
 */
public interface IDemoOutputBuffer {
	boolean isEmpty();
	void flush() throws IOException;
	
	void push(byte val) throws IOException;
	
	void pushData(byte[] chunk, int offs, int len) throws IOException;
	void pushData(byte[] data) throws IOException;
}
