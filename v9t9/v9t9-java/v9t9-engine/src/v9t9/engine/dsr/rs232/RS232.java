/**
 * 
 */
package v9t9.engine.dsr.rs232;

import v9t9.common.dsr.IRS232Handler;
import v9t9.common.dsr.IRS232Handler.Buffer;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.engine.Dumper;
/**
 * This manages high-level emulation of the RS232.
 * @author ejs
 *
 */
public class RS232 {
	private Dumper dumper;
	private IRS232Handler handler;
	
	public RS232(Dumper dumper) {
		this.dumper = dumper;
	}
	
	/**
	 * @param handler the handler to set
	 */
	public void setHandler(IRS232Handler handler) {
		this.handler = handler;
	}
	/**
	 * @return the handler
	 */
	public IRS232Handler getHandler() {
		return handler;
	}

	/**
	 * @return the dumper
	 */
	public Dumper getDumper() {
		return dumper;
	}

	private Buffer xmitBuffer = new Buffer(), recvBuffer = new Buffer();
	
	private DataSize dataSize = DataSize.FIVE;
	private Stop stop = Stop.STOP_1_5;
	private Parity parity = Parity.NONE;
	private int recvbps;
	private int xmitbps;
	private int intervalHz;
	
	/**
	 * 
	 */
	public void flushBuffers() {
		dumper.info("RS232: *** Flushing buffers");
		
		// lose all readable data
		recvBuffer.clear();

		// send all writeable data
		// (don't loop here!)
		if (handler != null)
			handler.transmitChars(xmitBuffer);

		if (!xmitBuffer.isEmpty()) { 
			dumper.info("RS232: *** Transmit buffer was not empty");
		}
		xmitBuffer.clear();
		//dump();		
	}

	
	/**
	 * @param txchar 
	 * 
	 */
	public void transmitChar(byte ch) {
		dumper.info(String.format("RS232: Buffering char %02X (%c)", ch, (char) ch));

		xmitBuffer.add(ch);
	}

	/**
	 * @return the parity
	 */
	public Parity getParity() {
		return parity;
	}
	/**
	 * @return the size
	 */
	public DataSize getDataSize() {
		return dataSize;
	}
	/**
	 * @return the stop
	 */
	public Stop getStopBits() {
		return stop;
	}

	/**
	 * @param old
	 * @param bit
	 */
	public void setControlBits(DataSize size, Parity parity, Stop stop) {
		this.dataSize = size;
		this.parity = parity;
		this.stop = stop;
		
		if (handler != null) {
			handler.updateControl(this.dataSize, this.parity, this.stop);
		}
	}

	/**
	 * @return 
	 * 
	 */
	public byte receiveData() {
		// Get char from buffer, or last one if empty.
		byte ch = recvBuffer.take();
		dumper.info(String.format("RS232: Received char %02X (%c)"), ch, (char) ch);
		return ch;
	}


	/**
	 * @return 
	 * 
	 */
	public int readStatusBits() {
		return 0;
	}

	public void dump()
	{
		StringBuilder sb = new StringBuilder(); 
	
		sb.append(String.format("RS232: Write buffer: %s  Read buffer: %s",
			 xmitBuffer, recvBuffer));
	
		dumper.info(sb.toString());
	}

	/**
	 * 
	 */
	public void clear() {
//		#if BUFFERED_RS232
//		if (rs232_interrupt_tag)
//			TM_ResetEvent(rs232_interrupt_tag);
//#endif

		//regs.clear();

		xmitBuffer.clear();
		recvBuffer.clear();

		setControlBits(DataSize.FIVE, Parity.NONE, Stop.STOP_1_5);
		
		setIntervalRate(0);
		setReceiveRate(0);
		setTransmitRate(0);
//		Reset_RS232_SysDeps(rs);
		dump();
		
//		#if BUFFERED_RS232
//				if (!rs232_interrupt_tag)
//					rs232_interrupt_tag = TM_UniqueTag();
//
//				TM_SetEvent(rs232_interrupt_tag, TM_HZ, 0, TM_FUNC | TM_REPEAT,
//							RS232_Monitor);
//		#endif
	}


	/**
	 * 
	 */
	public void setIntervalRate(int intervalHz) {
		this.intervalHz = intervalHz;
		
	}

	/**
	 * @param x
	 */
	public void setTransmitRate(int bps) {
		this.xmitbps = bps;
		if (handler != null)
			handler.setTransmitRate(bps);
	}
	/**
	 * @return the xmitbps
	 */
	public int getTransmitRate() {
		return xmitbps;
	}

	/**
	 * @param recvbps
	 */
	public void setReceiveRate(int recvbps) {
		this.recvbps = recvbps;
		if (handler != null)
			handler.setReceiveRate(recvbps);
	}
	/**
	 * @return the recvbps
	 */
	public int getReceiveRate() {
		return recvbps;
	}

	/**
	 * @return
	 */
	public Buffer getRecvBuffer() {
		return recvBuffer;
	}
	/**
	 * @return the xmitBuffer
	 */
	public Buffer getXmitBuffer() {
		return xmitBuffer;
	}

	/**
	 * @return
	 */
	public int getIntervalRate() {
		return intervalHz;
	}

}
