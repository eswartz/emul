/**
 * 
 */
package v9t9.common.speech;

import java.io.IOException;

/**
 * Marker interface for LPC parameters for a frame of speech. 
 * @author ejs
 *
 */
public interface ILPCParameters {

	/**
	 * Convert parameters to bytes for serialization
	 * @return
	 * @throws IOException 
	 */
	byte[] toBytes() throws IOException;

	/**
	 * Decode parameters from bytes 
	 * @return
	 * @throws IOException 
	 */
	void fromBytes(byte[] bytes) throws IOException;
	
}
