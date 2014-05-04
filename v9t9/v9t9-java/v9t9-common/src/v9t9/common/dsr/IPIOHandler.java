/**
 * 
 */
package v9t9.common.dsr;



/**
 * This interface provides the backend for an emulated RS232 port.
 * @author ejs
 *
 */
public interface IPIOHandler {
	
	
	/**
	 * Transmit characters from the transmit buffer 
	 */
	void transmitChars(IOBuffer buf);

}

