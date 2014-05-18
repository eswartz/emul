/**
 * 
 */
package v9t9.common.dsr;

import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;

public interface IRS232Listener {
	void updatedControl(DataSize size, Parity parity, Stop stop);
	void transmitRateSet(int xmitrate);
	void receiveRateSet(int recvrate);
	void charsTransmitted(byte[] buffer);
}