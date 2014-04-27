/**
 * 
 */
package v9t9.engine.dsr.rs232;

import java.util.Arrays;

/** The RS232 device is characterized by a dense packing of registers
	into a small number of bits.  Depending on the settings of CRU
	bits 11-14, a different register set is selected.  We use an array
	to store these registers.  Also, certain bits are called "flag"
	bits since writing their value instantiates the changes made to
	the register.  This saves us a bit of time. */
public class RS232Regs {

	public enum RegisterSelect {
		CONTROL,
		INTERVAL,
		RECVRATE,
		XMITRATE,
		EMIT
	}
	
	RegisterSelect reg;	//  current register
	
	//	WRITE:
	short	regsel;			//	0..0xf of 
	short	wrbits;			//	bits written to register

	//	wrbits copied here
	short	txchar;			//	[loadctrl=0] transmitted char
	short	xmitrate;		//	[loadctrl>=1] transmit rate register: timing
	short	rcvrate;		//	[loadctrl>=2] receive rate register: timing
	
	

	int	recvbps, xmitbps;	//	cached calculation for baud rate

	short	invl;			//	[loadctrl>=4] interval register: ???
	short	ctrl;			//	[loadctrl>=8] control register: clock, parity, stop

	int		wrport;		//	bitmap for remaining write registers

	//	READ:
	int	rdport;			//	bitmap for read registers

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

	int getWordSize() {
		return ((ctrl & (RS232Constants.CTRL_RCL0 + RS232Constants.CTRL_RCL1)) + 5);
	}
	/**
	 * 
	 */
	public void clear() {
		regsel = wrbits = txchar = xmitrate = rcvrate = invl = ctrl = 0;
		recvbps = xmitbps = 0;
		wrport = rdport = 0;
		Arrays.fill(xmit, (byte) 0);
		Arrays.fill(recv, (byte) 0);
		t_st = t_en = r_st = r_en = 0;
		
	}
}
