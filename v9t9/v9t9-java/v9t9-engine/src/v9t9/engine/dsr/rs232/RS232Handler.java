/**
 * 
 */
package v9t9.engine.dsr.rs232;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IRS232Handler;
import v9t9.common.dsr.IRS232Listener;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * This handles 
 * @author ejs
 *
 */
public class RS232Handler implements IRS232Handler {

	private DataSize dataSize;
	private Parity parity;
	private Stop stop;
	
	private ListenerList<IRS232Listener> listeners = new ListenerList<IRS232Listener>();
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#addListener(v9t9.common.dsr.IRS232Handler.IRS232Listener)
	 */
	@Override
	public void addListener(IRS232Listener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#removeListener(v9t9.common.dsr.IRS232Handler.IRS232Listener)
	 */
	@Override
	public void removeListener(IRS232Listener listener) {
		listeners.remove(listener);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#updateControl(v9t9.common.dsr.IRS232Handler.DataSize, v9t9.common.dsr.IRS232Handler.Parity, v9t9.common.dsr.IRS232Handler.Stop)
	 */
	@Override
	public void updateControl(DataSize dataSize, Parity parity, Stop stop) {
		// ignore unless changing
		if (this.dataSize != dataSize || this.parity != parity || this.stop != stop) {
			this.dataSize = dataSize;
			this.parity = parity;
			this.stop = stop;

			listeners.fire(new IFire<IRS232Listener>() {

				@Override
				public void fire(IRS232Listener listener) {
					listener.updatedControl(
							RS232Handler.this.dataSize, 
							RS232Handler.this.parity, 
							RS232Handler.this.stop);
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#setTransmitRate(int)
	 */
	@Override
	public void setTransmitRate(final int bps) {
		listeners.fire(new IFire<IRS232Listener>() {

			@Override
			public void fire(IRS232Listener listener) {
				listener.transmitRateSet(bps);
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#setReceiveRate(int)
	 */
	@Override
	public void setReceiveRate(final int bps) {
		listeners.fire(new IFire<IRS232Listener>() {

			@Override
			public void fire(IRS232Listener listener) {
				listener.receiveRateSet(bps);
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IRS232Handler#transmitChars(v9t9.common.dsr.IRS232Handler.IOBuffer)
	 */
	@Override
	public void transmitChars(final IOBuffer buf) {
		final byte[] buffer = buf.takeAll();
		
		listeners.fire(new IFire<IRS232Listener>() {

			@Override
			public void fire(IRS232Listener listener) {
				listener.charsTransmitted(buffer);
			}
		});
		
	}

}
