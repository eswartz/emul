/**
 * 
 */
package v9t9.machine.ti99.dsr.rs232;

import static v9t9.machine.ti99.dsr.rs232.RS232Constants.*;
import v9t9.engine.Dumper;
import v9t9.engine.dsr.rs232.RS232;

/** The RS232 device is characterized by a dense packing of registers
	into a small number of bits.  Depending on the settings of CRU
	bits 11-14, a different register set is selected.  We use an array
	to store these registers.  Also, certain bits are called "flag"
	bits since writing their value instantiates the changes made to
	the register.  This saves us a bit of time. */
public class RS232Regs {

	private RS232 rs232;
	
	//	WRITE:
	private byte regsel;			//	0..0xf of 
	private short	wrbits;			//	bits written to register

	//	wrbits copied here
	private short	txchar;			//	[loadctrl=0] transmitted char
	private short	xmitrate;		//	[loadctrl>=1] transmit rate register: timing
	private short	rcvrate;		//	[loadctrl>=2] receive rate register: timing
	
	

	private int	recvbps, xmitbps;	//	cached calculation for baud rate

	private short	invl;			//	[loadctrl>=4] interval register: ???
	private short	ctrl;			//	[loadctrl>=8] control register: clock, parity, stop

	private int		wrport;		//	bitmap for remaining write registers

	//	READ:
	private int	rdport;			//	bitmap for read registers

	private Dumper dumper;
	
	/**
	 * 
	 */
	public RS232Regs(RS232 rs232, Dumper dumper) {
		this.rs232 = rs232;
		this.dumper = dumper;
	}
	

	public int getWordSize() {
		return ((ctrl & (RS232Constants.CTRL_RCL0 + RS232Constants.CTRL_RCL1)) + 5);
	}
	/**
	 * 
	 */
	public void clear() {
		wrbits = txchar = xmitrate = rcvrate = invl = ctrl = 0;
		recvbps = xmitbps = 0;
		wrport = rdport = 0;
		regsel = 0xf;
		rs232.clear();
	}
	/**
	 * @return
	 */
	public short getRegisterSelect() {
		return regsel;
	}
	/**
	 * @param rcvrate
	 * @return
	 */
	private int calcBPSRate(short rate) {
		int bps;
		
		// BPS = 3 MHz / ((CLK4M ? 4 : 3) * 2 * rate x 8*DIV8)
		int         div;

		// input speed
		div = (((ctrl & CTRL_CLK4M) != 0 ? 4 : 3) * 2
			   * (rate & RATE_MASK) * ((rate & RATE_DIV8) != 0 ? 8 : 1));
		if (div == 0)
			bps = 50;
		else
			bps = (3000000 /*baseclockhz*/ / div);

		return bps;
	}
	

	public enum RegisterSelect {
		CONTROL,
		INTERVAL,
		RECVRATE,
		XMITRATE,
		EMIT
	}

	private RegisterSelect deriveRegisterSelect() {
		RegisterSelect sel = null;
		if ((regsel & 8) != 0) 
			sel = RegisterSelect.CONTROL;
		else if ((regsel & 4) != 0) 
			sel = RegisterSelect.INTERVAL;
		else if ((regsel & 2) != 0) 
			sel = RegisterSelect.RECVRATE;
		else if ((regsel & 1) != 0) 
			sel = RegisterSelect.XMITRATE;
		else
			sel = RegisterSelect.EMIT;
		return sel;
	}

	/*	
	 *	Flag bits for registers multiplexed through loadctrl
	 */

	int FLAG_TXCHAR() { return (1<<(getWordSize()-1)); }
	static final int FLAG_XMITRATE =	(1<<10);
	static final int FLAG_RCVRATE =	(1<<10);
	static final int FLAG_INVL =		(1<<7);
	static final int FLAG_CTRL =		(1<<7);
	
	/**
	 * Apply register changes once the last bit in a register is set.
	 */
	public void triggerChange(int dbit) {
		RegisterSelect sel = deriveRegisterSelect();
		
		boolean complete = false;
		switch (sel) {
		case CONTROL:
			if (dbit == FLAG_CTRL) { 
				this.ctrl = (short) (getWriteBits() & 0x7ff);
				dump();
				rs232.setCTRLRegister();
				rs232.flushBuffers();
				this.regsel = (byte) (regsel & ~8);
			}
			break;
		case INTERVAL:
			if (dbit == FLAG_INVL) { 
				this.invl = (short) (getWriteBits() & 0xff);
				dump();
				rs232.setINVLRegister();
				rs232.flushBuffers();
				this.regsel = (byte) (regsel & ~4);
			}
			break;
		case RECVRATE:
			if (dbit == FLAG_RCVRATE) { 
				rcvrate = ((short) (getWriteBits() & 0x7ff));
				recvbps = calcBPSRate(rcvrate);
				dump();
				rs232.setReceiveRate(recvbps);
				rs232.flushBuffers();
				this.regsel = (byte) (regsel & ~2);
				complete = (regsel & 1) != 0;
			}
			// fall through: can set both RECV and XMIT rate at same time
		case XMITRATE:
			if (sel == RegisterSelect.XMITRATE && dbit == FLAG_XMITRATE) 
				complete = true;
			if (complete) {
				xmitrate = ((short) (getWriteBits() & 0x7ff));
				xmitbps = calcBPSRate(xmitrate);
				dump();
				rs232.setTransmitRate(xmitbps);
				rs232.flushBuffers();
				this.regsel = (byte) (regsel & ~1);
			}
			break;
		case EMIT:
			if (dbit == FLAG_TXCHAR()) {
				this.txchar = (short) (getWriteBits() & (0xff >> (8 - getWordSize())));
				dump();
				rs232.transmitChar((byte) txchar);
			}
			break;
		}		
	}
	
	public void dump()
	{
		StringBuilder sb = new StringBuilder(); 
		sb.append(String.format(("* regsel=%s\t\n"), deriveRegisterSelect()));
		if (regsel >= 8) {
			sb.append("* ");
			sb.append(String.format(("ctrl=%02X\nrcl0=%b,rcl1=%b, clk4m=%b, Podd=%b,Penb=%b, SBS2=%b,SBS1=%b\t"),
				 ctrl, 
				 0 != (ctrl & RS232Constants.CTRL_RCL0), 
				 0 != (ctrl & RS232Constants.CTRL_RCL1),
				 0 != (ctrl & RS232Constants.CTRL_CLK4M), 
				 0 != (ctrl & RS232Constants.CTRL_Podd),
				 0 != (ctrl & RS232Constants.CTRL_Penb), 
				 0 != (ctrl & RS232Constants.CTRL_SBS2),
				 0 != (ctrl & RS232Constants.CTRL_SBS1)));
		}
		if (regsel < 8 && regsel >= 4) {
			sb.append("* ");
			sb.append(String.format(("invl=%03X\t"), invl));
		}
		if (regsel < 4 && regsel >= 2) {
			sb.append("* ");
			sb.append(String.format(("rcvrate=%03X\t"), rcvrate));
		}
		if (regsel < 2 && regsel >= 1) {
			sb.append("* ");
			sb.append(String.format(("xmitrate=%03X\t"), xmitrate));
		}
		if (regsel < 1) {
			sb.append("* ");
			sb.append(String.format(("txchar=%02X (%c)\t"), txchar, txchar));
		}
		sb.append(String.format(("recvbps=%d, xmitbps=%d\n"), recvbps, xmitbps));
	
	/*	module_logger(&realRS232DSR, _L | L_3,("rtson=%d, brkon=%d, rienb=%d, xbienb=%d, timenb=%d, dscenb=%d\n"),
			!!(wrport&RS_RTSON), !!(wrport&RS_BRKON), !!(wrport&RS_RIENB),
			!!(wrport&RS_XBIENB), !!(wrport&RS_TIMEMB), !!(wrport&RS_DSCENB));
	
		module_logger(&realRS232DSR, _L | L_3,("rcverr=%d,rper=%d,rover=%d,rfer=%d,rfbd=%d,rsbd=%d,rin=%d\n"),
			!!(rdport&RS_RCVERR), !!(rdport&RS_RPER), !!(rdport&RS_ROVER),
			!!(rdport&RS_RFER), !!(rdport&RS_RFBD), !!(rdport&RS_RSBD),
			!!(rdport&RS_RIN));
		module_logger(&realRS232DSR, _L | L_3,("rbint=%d,xbint=%d,timint=%d,dscint=%d,rbrl=%d,xbre=%d,xsre=%d\n"),
			!!(rdport&RS_RBINT), !!(rdport&RS_XBINT), !!(rdport&RS_TIMINT), !!(rdport&RS_DSCINT),
			!!(rdport&RS_RBRL), !!(rdport&RS_XBRE), !!(rdport&RS_XSRE));
		module_logger(&realRS232DSR, _L | L_3,("timerr=%d,timelp=%d,rts=%d,dsr=%d,cts=%d,dsch=%d,flag=%d,int=%d\n"),
			!!(rdport&RS_TIMERR), !!(rdport&RS_TIMELP), !!(rdport&RS_RTS),
			!!(rdport&RS_DSR), !!(rdport&RS_CTS), !!(rdport&RS_DSCH),
			!!(rdport&RS_FLAG), !!(rdport&RS_INT));
	*/
		dumper.info(sb.toString());
	}

	public void setReadPort(int rdport) {
		this.rdport = rdport;
	}
	public int getReadPort() {
		return rdport;
	}
	public void setWritePort(int flags) {
		wrport = flags;
	}
	public int getWritePort() {
		return wrport;
	}
	public void setWriteBits(int flags) {
		wrbits = (short) flags;
	}
	public int getWriteBits() {
		return wrbits;
	}
	public RS232 getRS232() {
		return rs232;
	}


	/**
	 * @param i
	 */
	public void setRegisterSelect(int i) {
		regsel = (byte) i;
	}


	/**
	 * @param data
	 * @param bit
	 */
	public void updateRegisterSelect(int data, int bit) {
		if (data != 0)
			regsel |= bit;
		else
			regsel &= ~bit;
				
	}


	/**
	 * @param data
	 * @param bit
	 */
	public void updateWriteBits(int data, int bit) {
		if (data != 0)
			wrbits |= bit;
		else
			wrbits &= ~bit;
	}


	/**
	 * @param data
	 * @param bit
	 */
	public void updateWritePort(int data, int bit) {
		if (data != 0)
			wrport |= bit;
		else
			wrport &= ~bit;
	}


}
