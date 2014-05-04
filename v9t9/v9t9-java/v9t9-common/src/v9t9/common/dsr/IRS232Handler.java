/**
 * 
 */
package v9t9.common.dsr;



/**
 * This interface provides the backend for an emulated RS232 port.
 * @author ejs
 *
 */
public interface IRS232Handler {
	
	public enum Stop {
		STOP_1_5,
		STOP_2,
		STOP_1
	}
	
	public enum Parity {
		NONE,
		ODD,
		EVEN
	}
	
	public enum DataSize {
		FIVE,
		SIX,
		SEVEN,
		EIGHT
	}
	
	/**
	 * Update control parameters
	 * @param dataSize
	 * @param parity
	 * @param stop
	 */
	void updateControl(DataSize dataSize, Parity parity, Stop stop);

	/**
	 * Update transmit rate
	 */
	void setTransmitRate(int bps);
	/**
	 * Update receive rate
	 */
	void setReceiveRate(int bps);
	
	/**
	 * Transmit characters from the transmit buffer 
	 */
	void transmitChars(IOBuffer buf);

}

