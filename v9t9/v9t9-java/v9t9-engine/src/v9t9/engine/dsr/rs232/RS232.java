/**
 * 
 */
package v9t9.engine.dsr.rs232;

import java.util.Arrays;

import v9t9.engine.Dumper;
/**
 * This manages high-level emulation of the RS232.
 * @author ejs
 *
 */
public class RS232 {
	private Dumper dumper;
	
	public RS232(Dumper dumper) {
		this.dumper = dumper;
	}

	/**
	 * @return the dumper
	 */
	public Dumper getDumper() {
		return dumper;
	}

	
	// (500000/5/TM_HZ)		// max possible rate to deal with
	static final int BUF_SIZ = 1024;
	static final int BUF_MASK = 1023;

	public boolean isXmitBufferEmpty() {
		return t_st == t_en;
	}
	public boolean isXmitBufferFull() {
		return t_st == ((t_en + 1) & BUF_MASK);
	}
	public int getXmitBufferLeft() {
		return ((BUF_SIZ - t_st + t_en) & BUF_MASK);
	}
	public boolean isRecvBufferEmpty() {
		return r_st == r_en;
	}
	public boolean isRecvBufferFull() {
		return r_st == ((r_en + 1) & BUF_MASK);
	}
	public int getRecvBufferLeft() {
		return ((BUF_SIZ - r_st + r_en) & BUF_MASK);
	}

	byte	xmit[] = new byte[BUF_SIZ], recv[] = new byte[BUF_SIZ];
	int	t_st,t_en, r_st, r_en;			// pointers to ring
	/**
	 * 
	 */
	public void flushBuffers() {
		dumper.info("*** Flushing buffers");
		
		// lose all readable data
		r_st = r_en = 0;

		// send all writeable data
		// (don't loop here!)
		transmitChars();

		if (!isXmitBufferEmpty()) { 
			dumper.info("*** Transmit buffer was not empty");
		}
		t_st = t_en = 0;
		//dump();		
	}

	/**
	 * 
	 */
	private void transmitChars() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void transmitChar() {
		// TODO Auto-generated method stub
		
	}


	/**
	 * @param old
	 * @param bit
	 */
	public void setControlBits(int old, int bit) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void receiveData() {
		// TODO Auto-generated method stub
		
	}


	/**
	 * 
	 */
	public void readStatusBits() {
		// TODO Auto-generated method stub
		
	}

	public void dump()
	{
		StringBuilder sb = new StringBuilder(); 
	
		sb.append(String.format(("Write buffer: %d/%d  Read buffer: %d/%d\n"),
			 t_st, t_en, r_st, r_en));
	
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

		Arrays.fill(xmit, (byte) 0);
		Arrays.fill(recv, (byte) 0);
		t_st = t_en = r_st = r_en = 0;
		
		setCTRLRegister();
		setINVLRegister();
		setRCVRATERegister();
		setXMITRATERegister();
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
	public void setCTRLRegister() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void setINVLRegister() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void setRCVRATERegister() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void setXMITRATERegister() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param x
	 */
	public void setTransmitRate(int bps) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param recvbps
	 */
	public void setReceiveRate(int recvbps) {
		
	}

}
