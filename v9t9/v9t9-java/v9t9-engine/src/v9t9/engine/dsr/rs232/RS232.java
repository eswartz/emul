/*
  RS232.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr.rs232;

import java.util.Timer;
import java.util.TimerTask;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IRS232Handler;
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
	private Timer timer;
	
	public RS232(Dumper dumper) {
		this.handler = new RS232Handler();
		this.dumper = dumper;
		timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				rs232Monitor();
			}
			
		}, 0, 1000 / 50);
	}
	
	/*	Timer event which periodically checks the RS232 devices for
	activity, reading into buffers and writing from buffers.
	It is not efficient anywhere to call the OS for each status bit
	read, which is why we eat up the buffers the OS is storing for us. 	*/
	protected void rs232Monitor() {
		if (handler != null) {
			// transmit buffered chars.
			handler.transmitChars(xmitBuffer);
	
//			// receive characters from the OS.
//			// these are available for reading immediately.
//			Receive_Chars(rs);
//	
//			// update modem lines
//			Update_Modem_Lines(rs);
//	
//			// update status flags and interrupt if needed
//			Update_Flags_And_Ints(rs);
		} else {
			xmitBuffer.clear();
		}
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

	private IOBuffer xmitBuffer = new IOBuffer(1024), recvBuffer = new IOBuffer(1024);
	
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
		dumper.info(String.format("RS232: Received char %02x (%c)"), ch, (char) ch);
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
	public IOBuffer getRecvBuffer() {
		return recvBuffer;
	}
	/**
	 * @return the xmitBuffer
	 */
	public IOBuffer getXmitBuffer() {
		return xmitBuffer;
	}

	/**
	 * @return
	 */
	public int getIntervalRate() {
		return intervalHz;
	}

}
