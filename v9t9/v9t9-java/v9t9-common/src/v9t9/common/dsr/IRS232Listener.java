/*
  IRS232Listener.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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