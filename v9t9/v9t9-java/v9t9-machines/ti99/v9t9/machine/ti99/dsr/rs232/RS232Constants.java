/**
 * 
 */
package v9t9.machine.ti99.dsr.rs232;

/**
 * @author ejs
 *
 */
public class RS232Constants {

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

	//	READ:

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

}
