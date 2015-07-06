/*
  RS232Regs.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.rs232;

import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.common.machine.IMachine;
import v9t9.engine.Dumper;
import v9t9.engine.dsr.rs232.RS232;
import v9t9.machine.ti99.cpu.Cpu9900;
import ejs.base.properties.IProperty;

/** The RS232 device is characterized by a dense packing of registers
	into a small number of bits.  Depending on the settings of CRU
	bits 11-14, a different register set is selected.  We use an array
	to store these registers.  Also, certain bits are called "flag"
	bits since writing their value instantiates the changes made to
	the register.  This saves us a bit of time. */
public class RS232Regs {

	static final public short CRU_BASE = 0x1300;

	static final public int RATE_DIV8 = 0x400;

	static final public int RATE_MASK = 0x3ff;

	static final public int CTRL_RCL0 = 1;

	static final public int CTRL_RCL1 = 2;

	static final public int CTRL_CLK4M = 8;

	static final public int CTRL_Podd = 16;

	static final public int CTRL_Penb = 32;

	static final public int CTRL_SBS2 = 64;		//	00 = 1.5, 01 = 2, 1X = 1

	static final public int CTRL_SBS1 = 128;		//

	static final public int RS_RTSON = (1<<16);	//	[16] request to send

	static final public int RS_BRKON = (1<<17);	//	[17] break on

	static final public int RS_RIENB = (1<<18);	//	[18] receive interrupt enable

	static final public int RS_XBIENB = (1<<19);	//	[19] xmit buffer interrupt enable

	static final public int RS_TIMENB = (1<<20);	//	[20] timer interrupt enable

	static final public int RS_DSCENB = (1<<21);	//	[21] data status change interrupt enable
	//	[22-30] unused

	static final public int RS_RESET = (1<<31);	//	[31] reset

	static final public int RS_RXCHAR = (0xff);	//	[0-7] received char

	//	[8] always zero
	static final public int RS_RCVERR = (1<<9);	//	[9] receive error

	static final public int RS_RPER = (1<<10);	//	[10] receive parity error

	static final public int RS_ROVER = (1<<11);	//	[11] receive overrun error

	static final public int RS_RFER = (1<<12);	//	[12] receive framing error

	static final public int RS_RFBD = (1<<13);	//	[13] receive full bit detect

	static final public int RS_RSBD = (1<<14);	//	[14] receive start bit detect

	static final public int RS_RIN = (1<<15);	//	[15] receive input

	static final public int RS_RBINT = (1<<16);	//	[16] receiver interrupt (rbrl - rienb)

	static final public int RS_XBINT = (1<<17);	//	[17] transmitter interrupt (xbre - xbienb)

	//	[18] always zero
	static final public int RS_TIMINT = (1<<19);	//	[19] timer interrupt (timelp - timenb)

	static final public int RS_DSCINT = (1<<20);	//	[20] data set status change interrupt (dsch - dscenb)

	static final public int RS_RBRL = (1<<21);	//	[21] receive buffer register loaded

	static final public int RS_XBRE = (1<<22);	//	[22] transmit buffer register empty

	static final public int RS_XSRE = (1<<23);	//	[23] transmit shift register empty

	static final public int RS_TIMERR = (1<<24);	//	[24] time error

	static final public int RS_TIMELP = (1<<25);	//	[25] timer elapsed

	static final public int RS_RTS = (1<<26);	//	[26] request to send

	static final public int RS_DSR = (1<<27);	//	[27] data set ready

	static final public int RS_CTS = (1<<28);	//	[28] clear to send

	static final public int RS_DSCH = (1<<29);	//	[29] data set status change

	static final public int RS_FLAG = (1<<30);	//	[30] register load control flag set

	static final public int RS_INT = (1<<31);	//	[31] interrupt
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

	private IMachine machine;

	private IProperty cyclesPerSecond;

	
	/**
	 * 
	 */
	public RS232Regs(IMachine machine, RS232 rs232, Dumper dumper) {
		this.machine = machine;
		this.rs232 = rs232;
		this.dumper = dumper;
		cyclesPerSecond = machine.getSettings().get(ICpu.settingCyclesPerSecond);
	}
	

	public int getWordSize() {
		return ((ctrl & (RS232Regs.CTRL_RCL0 + RS232Regs.CTRL_RCL1)) + 5);
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
	private int calcBPSRate(short rate) {
		int bps;
		
		// BPS = 3 MHz / ((CLK4M ? 4 : 3) * 2 * rate x 8*DIV8)
		int         div;

		// input speed
		div = (((ctrl & RS232Regs.CTRL_CLK4M) != 0 ? 4 : 3) * 2
			   * (rate & RS232Regs.RATE_MASK) * ((rate & RS232Regs.RATE_DIV8) != 0 ? 8 : 1));
		if (div == 0)
			bps = 50;
		else
			bps = (cyclesPerSecond.getInt() / div);

		return bps;
	}
	private int calcIntervalRate(short rate) {
		int hz;
		
		// BPS = 3 MHz / rate
		int         div;

		// input speed
		div = ((ctrl & RS232Regs.CTRL_CLK4M) != 0 ? 4 : 3) * 64
				* (rate & 0xff);
		if (div == 0)
			hz = 0;
		else
			hz = (cyclesPerSecond.getInt() / div);

		return hz;
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
	 * Apply register changes once the last bit in a register is set (dbit == 1<<bit)
	 * or when a register select bit is changed (dbit == 0).
	 */
	public void triggerChange(int dbit) {
		RegisterSelect sel = deriveRegisterSelect();
		
		if (sel == RegisterSelect.CONTROL) {
			if (dbit == 0 || dbit == FLAG_CTRL) { 
				this.ctrl = (short) (getWriteBits() & 0x7ff);
				dump();
				
				rs232.setControlBits(getDataSize(), getParity(), getStop());
				rs232.flushBuffers();
				if (dbit != 0) {
					this.regsel = (byte) ((regsel & ~8) | 4);
				}
			}
		}
		if (sel == RegisterSelect.INTERVAL) {
			if (dbit == 0 || dbit == FLAG_INVL) { 
				this.invl = (short) (getWriteBits() & 0xff);
				dump();
				int rate = calcIntervalRate(invl);
				rs232.setIntervalRate(rate);
				rs232.flushBuffers();
				if (dbit != 0) {
					this.regsel = (byte) ((regsel & ~4) | 2);
				}
			}
		}
		if (sel == RegisterSelect.RECVRATE) {
			if (dbit == 0 || dbit == FLAG_RCVRATE) { 
				rcvrate = ((short) (getWriteBits() & 0x7ff));
				recvbps = calcBPSRate(rcvrate);
				dump();
				rs232.setReceiveRate(recvbps);
				rs232.flushBuffers();
				if (dbit != 0) {
					if ((regsel & 1) != 0)
						sel = RegisterSelect.XMITRATE;	// auto-set next
					this.regsel = (byte) ((regsel & ~2) | 1);
				}
			}
		}
		if (sel == RegisterSelect.XMITRATE) {
			if (dbit == 0 || dbit == FLAG_XMITRATE) { 
				xmitrate = ((short) (getWriteBits() & 0x7ff));
				xmitbps = calcBPSRate(xmitrate);
				dump();
				rs232.setTransmitRate(xmitbps);
				rs232.flushBuffers();
				if (dbit != 0)
					this.regsel = (byte) (regsel & ~1);	
			}
		}
		if (sel == RegisterSelect.EMIT) {
			if (dbit == FLAG_TXCHAR()) {
				this.txchar = (short) (getWriteBits() & (0xff >> (8 - getWordSize())));
				dump();
				rs232.transmitChar((byte) txchar);
				updateFlagsAndInts();
			}
		}		
	}
	

	//  Update status bits when buffer state changes,
	//  possibly triggering an interrupt.
	public void updateFlagsAndInts()
	{
		boolean        interruptible = false;
	
		// all buffered chars are immediately
		// available to be read.
		//
		if (!rs232.getRecvBuffer().isEmpty()) {
			rdport |= RS_RBRL;
	
			// trigger interrupt?
			if ((wrport & RS_RIENB) != 0) {
				rdport |= RS_RBINT;
				interruptible = true;
			}
		} else {
			rdport &= ~(RS_RBRL | RS_RBINT);
		}
	
		// contrary to popular opinion, we will set RS_XBRE
		// here if the buffer is NOT FULL, so the 99/4A
		// can continue to stuff the buffer.
		//
		if (!rs232.getXmitBuffer().isFull()) {
			rdport |= RS_XBRE;
	
			// trigger interrupt?
			if ((wrport & RS_XBIENB) != 0) {
				rdport |= RS_XBINT;
				interruptible = true;
			}
		} else {
			rdport &= ~(RS_XBRE | RS_XBINT);
		}

		// TODO: data lines
		
		//  NOTE:  when buffering, the DSR state can change physically
		//  while interrupts should still be generated logically.
		//  Be sure to bias the DSR state by the buffer state.
		//
		//  The typical routine for RS_RIENB goes like this:
		//  
		//  <interrupt>
		//  test bit 31     -- must be 1 else RESET
		//  test bit RBINT  -- must be 1 else RESET
		//  test bit DSR    -- must be 1 else RESET
		//  test bit RBRL   -- must be 1 else RESET
		//  read char
		//  set bit RIENB   -- ACK; this resets RBRL
		// 
		//  We will immediately set RBRL and RBINT again if needed.
		//  We won't reset the interrupt state until all the bits
		//  are off.
	
		if (interruptible) {
			dumper.info("RS232: *** Interrupt ***");
			rdport |= RS_INT;
			
			machine.getCru().triggerInterrupt(Cpu9900.INTLEVEL_INTREQ);
		} else {
			rdport &= ~RS_INT;
			machine.getCru().acknowledgeInterrupt(Cpu9900.INTLEVEL_INTREQ);
		}
	}

	public void dump()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("RS232:\n");
		sb.append(String.format(("* regsel=%s\t\n"), deriveRegisterSelect()));
		if (regsel >= 8) {
			sb.append("* ");
			sb.append(String.format(("ctrl=%02X\nrcl0=%b,rcl1=%b, clk4m=%b, Podd=%b,Penb=%b, SBS2=%b,SBS1=%b\t"),
				 ctrl, 
				 0 != (ctrl & RS232Regs.CTRL_RCL0), 
				 0 != (ctrl & RS232Regs.CTRL_RCL1),
				 0 != (ctrl & RS232Regs.CTRL_CLK4M), 
				 0 != (ctrl & RS232Regs.CTRL_Podd),
				 0 != (ctrl & RS232Regs.CTRL_Penb), 
				 0 != (ctrl & RS232Regs.CTRL_SBS2),
				 0 != (ctrl & RS232Regs.CTRL_SBS1)));
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


	/**
	 * @return
	 */
	public DataSize getDataSize() {
		switch ((ctrl & CTRL_RCL0 + CTRL_RCL1) >> 0) {
		case 0:
			return DataSize.FIVE;
		case 1:
			return DataSize.SIX;
		case 2:
			return DataSize.SEVEN;
		case 3:
		default:
			return DataSize.EIGHT;
		}
	}


	/**
	 * @return
	 */
	public Parity getParity() {
		switch ((ctrl & CTRL_Penb + CTRL_Podd) >> 4) {
		default:
		case 0:
		case 1:
			return Parity.NONE;
		case 2:
			return Parity.EVEN;
		case 3:
			return Parity.ODD;
		}
	}


	/**
	 * @return
	 */
	public Stop getStop() {
		switch ((ctrl & CTRL_SBS1 + CTRL_SBS2) >> 6) {
		case 0:
			return Stop.STOP_1_5;
		case 1:
			return Stop.STOP_2; 
		case 2:
		case 3:
		default:
			return Stop.STOP_1;
		}
	}


}
