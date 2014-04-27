/**
 * 
 */
package v9t9.engine.dsr.rs232;

import v9t9.engine.Dumper;
import v9t9.engine.dsr.rs232.RS232Regs.RegisterSelect;
import static v9t9.engine.dsr.rs232.RS232Constants.*;
/**
 * This maintains the state of RS232 registers and manages high-level emulation.
 * @author ejs
 *
 */
public class RS232 {
	private RS232Regs regs = new RS232Regs();
	private Dumper dumper;
	
	public RS232(Dumper dumper) {
		this.dumper = dumper;
		
	}
	
	public void dump()
	{
		StringBuilder sb = new StringBuilder(); 
		sb.append(String.format(("* regsel=%s\t\n"), deriveRegisterSelect()));
		if (regs.regsel >= 8) {
			sb.append("* ");
			sb.append(String.format(("ctrl=%02X\nrcl0=%b,rcl1=%b, clk4m=%b, Podd=%b,Penb=%b, SBS2=%b,SBS1=%b\t"),
				 regs.ctrl, 
				 0 != (regs.ctrl & RS232Constants.CTRL_RCL0), 
				 0 != (regs.ctrl & RS232Constants.CTRL_RCL1),
				 0 != (regs.ctrl & RS232Constants.CTRL_CLK4M), 
				 0 != (regs.ctrl & RS232Constants.CTRL_Podd),
				 0 != (regs.ctrl & RS232Constants.CTRL_Penb), 
				 0 != (regs.ctrl & RS232Constants.CTRL_SBS2),
				 0 != (regs.ctrl & RS232Constants.CTRL_SBS1)));
		}
		if (regs.regsel < 8 && regs.regsel >= 4) {
			sb.append("* ");
			sb.append(String.format(("invl=%03X\t"), regs.invl));
		}
		if (regs.regsel < 4 && regs.regsel >= 2) {
			sb.append("* ");
			sb.append(String.format(("rcvrate=%03X\t"), regs.rcvrate));
		}
		if (regs.regsel < 2 && regs.regsel >= 1) {
			sb.append("* ");
			sb.append(String.format(("xmitrate=%03X\t"), regs.xmitrate));
		}
		if (regs.regsel < 1) {
			sb.append("* ");
			sb.append(String.format(("txchar=%02X (%c)\t"), regs.txchar, regs.txchar));
		}
		sb.append(String.format(("recvbps=%d, xmitbps=%d\n"), regs.recvbps, regs.xmitbps));
	
		sb.append(String.format(("Write buffer: %d/%d  Read buffer: %d/%d\n"),
			 regs.t_st, regs.t_en, regs.r_st, regs.r_en));
	
	/*	module_logger(&realRS232DSR, _L | L_3,("rtson=%d, brkon=%d, rienb=%d, xbienb=%d, timenb=%d, dscenb=%d\n"),
			!!(regs.wrport&RS_RTSON), !!(regs.wrport&RS_BRKON), !!(regs.wrport&RS_RIENB),
			!!(regs.wrport&RS_XBIENB), !!(regs.wrport&RS_TIMEMB), !!(regs.wrport&RS_DSCENB));
	
		module_logger(&realRS232DSR, _L | L_3,("rcverr=%d,rper=%d,rover=%d,rfer=%d,rfbd=%d,rsbd=%d,rin=%d\n"),
			!!(regs.rdport&RS_RCVERR), !!(regs.rdport&RS_RPER), !!(regs.rdport&RS_ROVER),
			!!(regs.rdport&RS_RFER), !!(regs.rdport&RS_RFBD), !!(regs.rdport&RS_RSBD),
			!!(regs.rdport&RS_RIN));
		module_logger(&realRS232DSR, _L | L_3,("rbint=%d,xbint=%d,timint=%d,dscint=%d,rbrl=%d,xbre=%d,xsre=%d\n"),
			!!(regs.rdport&RS_RBINT), !!(regs.rdport&RS_XBINT), !!(regs.rdport&RS_TIMINT), !!(regs.rdport&RS_DSCINT),
			!!(regs.rdport&RS_RBRL), !!(regs.rdport&RS_XBRE), !!(regs.rdport&RS_XSRE));
		module_logger(&realRS232DSR, _L | L_3,("timerr=%d,timelp=%d,rts=%d,dsr=%d,cts=%d,dsch=%d,flag=%d,int=%d\n"),
			!!(regs.rdport&RS_TIMERR), !!(regs.rdport&RS_TIMELP), !!(regs.rdport&RS_RTS),
			!!(regs.rdport&RS_DSR), !!(regs.rdport&RS_CTS), !!(regs.rdport&RS_DSCH),
			!!(regs.rdport&RS_FLAG), !!(regs.rdport&RS_INT));
	*/
		dumper.info(sb.toString());
	}

	public void setReadPort(int flags) {
		regs.rdport = flags;
	}
	public int getReadPort() {
		return regs.rdport;
	}
	public void setWritePort(int flags) {
		regs.wrport = flags;
	}
	public int getWritePort() {
		return regs.wrport;
	}
	public void setWriteBits(int flags) {
		regs.wrbits = (short) flags;
	}
	public int getWriteBits() {
		return regs.wrbits;
	}
	/*	
	 *	Flag bits for registers multiplexed through loadctrl
	 */

	int FLAG_TXCHAR() { return (1<<(regs.getWordSize()-1)); }
	static final int FLAG_XMITRATE =	(1<<10);
	static final int FLAG_RCVRATE =	(1<<10);
	static final int FLAG_INVL =		(1<<7);
	static final int FLAG_CTRL =		(1<<7);

	/**
	 * Propagate register changes once (1) the first or last 
	 * control bit is set or (2) data is set.
	 * 
	 * @param i
	 * @param bit
	 */
	public void triggerChange(int cbit, int dbit) {
		RegisterSelect sel = deriveRegisterSelect();
		
		boolean complete = false;
		switch (sel) {
		case CONTROL:
			if (dbit == FLAG_CTRL) { 
				regs.ctrl = (short) (regs.wrbits & 0x7ff);
				dump();
				setCTRLRegister();
				flushBuffers();
				regs.regsel &= ~8;
			}
			break;
		case INTERVAL:
			if (dbit == FLAG_INVL) { 
				regs.invl = (short) (regs.wrbits & 0xff);
				dump();
				setINVLRegister();
				flushBuffers();
				regs.regsel &= ~4;
			}
			break;
		case RECVRATE:
			if (dbit == FLAG_RCVRATE) { 
				regs.rcvrate = (short) (regs.wrbits & 0x7ff);
				regs.recvbps = calcBPSRate(regs.rcvrate);
				dump();
				setRCVRATERegister();
				flushBuffers();
				regs.regsel &= ~2;
				complete = (regs.regsel & 1) != 0;
			}
			// fall through: can set both RECV and XMIT rate at same time
		case XMITRATE:
			if (sel == RegisterSelect.XMITRATE && dbit == FLAG_XMITRATE) 
				complete = true;
			if (complete) {
				regs.xmitrate = (short) (regs.wrbits & 0x7ff);
				regs.xmitbps = calcBPSRate(regs.xmitrate);
				dump();
				setXMITRATERegister();
				flushBuffers();
				regs.regsel &= ~1;
			}
			break;
		case EMIT:
			if (dbit == FLAG_TXCHAR()) {
				regs.txchar = (short) (regs.wrbits & (0xff >> (8 - regs.getWordSize())));
				dump();
				transmitChar();
			}
			break;
		}		
	}

	/**
	 * @return
	 */
	protected RegisterSelect deriveRegisterSelect() {
		RegisterSelect sel = null;
		if ((regs.regsel & 8) != 0) 
			sel = RegisterSelect.CONTROL;
		else if ((regs.regsel & 4) != 0) 
			sel = RegisterSelect.INTERVAL;
		else if ((regs.regsel & 2) != 0) 
			sel = RegisterSelect.RECVRATE;
		else if ((regs.regsel & 1) != 0) 
			sel = RegisterSelect.XMITRATE;
		else
			sel = RegisterSelect.EMIT;
		return sel;
	}

	/**
	 * 
	 */
	private void transmitChar() {
		// TODO Auto-generated method stub
		
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
		div = (((regs.ctrl & CTRL_CLK4M) != 0 ? 4 : 3) * 2
			   * (rate & RATE_MASK) * ((rate & RATE_DIV8) != 0 ? 8 : 1));
		if (div == 0)
			bps = 50;
		else
			bps = (3000000 /*baseclockhz*/ / div);

		dumper.info(String.format("Calc_BPS_Rate:  *bps = %d", bps));
		return bps;
	}

	/**
	 * 
	 */
	private void flushBuffers() {
		dumper.info("*** Flushing buffers");
		
		// lose all readable data
		regs.r_st = regs.r_en = 0;

		// send all writeable data
		// (don't loop here!)
		transmitChars();

		if (!regs.isXmitBufferEmpty()) { 
			dumper.info("*** Transmit buffer was not empty");
		}
		regs.t_st = regs.t_en = 0;
		dump();		
	}

	/**
	 * 
	 */
	private void transmitChars() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public short getRegisterSelect() {
		return regs.regsel;
	}
	public void setRegisterSelect(int regsel) {
		regs.regsel = (short) regsel;
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
	 * @return
	 */
	public RS232Regs getRegisters() {
		return regs;
	}

	/**
	 * 
	 */
	public void readStatusBits() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void clear() {
		regs.clear();
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

}
