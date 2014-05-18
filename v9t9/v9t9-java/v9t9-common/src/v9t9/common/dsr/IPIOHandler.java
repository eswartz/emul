/**
 * 
 */
package v9t9.common.dsr;




/**
 * This interface provides the backend for an emulated parallel port.
 * @author ejs
 *
 */
public interface IPIOHandler {

	void addListener(IPIOListener listener);
	void removeListener(IPIOListener listener);

	/**
	 * Transmit characters from the transmit buffer 
	 */
	void transmitChars(IOBuffer buf);

}

