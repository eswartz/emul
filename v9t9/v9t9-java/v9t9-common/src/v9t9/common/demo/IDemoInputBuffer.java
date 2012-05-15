/**
 * 
 */
package v9t9.common.demo;

import java.io.IOException;

/**
 * This interface represents a buffered amount of demo data that
 * is decoded per timer tick.
 * @author ejs
 *
 */
public interface IDemoInputBuffer {
	/**
	 * Refill the buffer.
	 * @throws IOException
	 */
	void refill() throws IOException;

	/**
	 * Get position in enclosing stream, for the purpose of reporting errors
	 * @return
	 */
	long getEffectivePos();
	
	/**
	 * Tell if content is available in the buffer
	 * @return
	 */
	boolean isAvailable();

	/**
	 * Read a byte
	 * @return
	 * @throws IOException 
	 */
	int read() throws IOException;
	
	byte[] readData(int chunkLength) throws IOException;
	
	byte[] readRest() throws IOException;
}
