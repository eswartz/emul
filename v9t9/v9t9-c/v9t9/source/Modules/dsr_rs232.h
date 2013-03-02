/*
  (c) 1994-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.  

*/

/*
  $Id$
 */

/*
 *	RS232 definitions.
 *
 */

#ifndef __DSR_RS232_H__
#define __DSR_RS232_H__

#include "OSLib.h"

#if UNDER_WIN32
#define BUFFERED_RS232	0
#else
#define BUFFERED_RS232 1
#endif

#define MAX_RS232_DEVS	2
#define RS232_CRU_BASE	0x1300

extern	char		realrs232filename[OS_NAMESIZE];
extern	char		rs232name[MAX_RS232_DEVS][OS_PATHSIZE];

/* The RS232 device is characterized by a dense packing of registers
	into a small number of bits.  Depending on the settings of CRU
	bits 11-14, a different register set is selected.  We use an array
	to store these registers.  Also, certain bits are called "flag"
	bits since writing their value instantiates the changes made to
	the register.  This saves us a bit of time. */

#define uint unsigned int

typedef struct {
	//	WRITE:
	u16	loadctrl;		// 	load control register: selects active register (0-15)
	u16	wrbits;			//	bits written to 0...10

	//	wrbits copied here
	u16	txchar;			//	[loadctrl=0] transmitted char
	u16	xmitrate;		//	[loadctrl>=1] transmit rate register: timing
	u16	rcvrate;		//	[loadctrl>=2] receive rate register: timing
	#define RATE_DIV8	0x400
	#define RATE_MASK	0x3ff

	u32	recvbps, xmitbps;	//	cached calculation for baud rate

	u16	invl;			//	[loadctrl>=4] interval register: ???
	u16	ctrl;			//	[loadctrl>=8] control register: clock, parity, stop
	#define CTRL_RCL0	1
	#define CTRL_RCL1	2
	#define CTRL_CLK4M	8
	#define CTRL_Podd	16
	#define CTRL_Penb	32
	#define CTRL_SBS2	64		//	00 = 1.5, 01 = 2, 1X = 1
	#define CTRL_SBS1	128		//

	u32		wrport;		//	bitmap for remaining write registers
	#define	RS_RTSON	(1<<16)	//	[16] request to send
	#define RS_BRKON	(1<<17)	//	[17] break on
	#define RS_RIENB	(1<<18)	//	[18] receive interrupt enable
	#define RS_XBIENB	(1<<19)	//	[19] xmit buffer interrupt enable
	#define RS_TIMENB	(1<<20)	//	[20] timer interrupt enable
	#define	RS_DSCENB	(1<<21)	//	[21] data status change interrupt enable
	//	[22-30] unused

	#define	RS_RESET	(1<<31)	//	[31] reset

	//	READ:
	u32	rdport;			//	bitmap for read registers

	#define	RS_RXCHAR	(0xff)	//	[0-7] received char

	//	[8] always zero
	#define	RS_RCVERR	(1<<9)	//	[9] receive error
	#define	RS_RPER		(1<<10)	//	[10] receive parity error
	#define	RS_ROVER	(1<<11)	//	[11] receive overrun error
	#define	RS_RFER		(1<<12)	//	[12] receive framing error
	#define	RS_RFBD		(1<<13)	//	[13] receive full bit detect
	#define	RS_RSBD		(1<<14)	//	[14] receive start bit detect
	#define	RS_RIN		(1<<15)	//	[15] receive input
	#define	RS_RBINT	(1<<16)	//	[16] receiver interrupt (rbrl - rienb)
	#define	RS_XBINT	(1<<17)	//	[17] transmitter interrupt (xbre - xbienb)
	//	[18] always zero
	#define	RS_TIMINT	(1<<19)	//	[19] timer interrupt (timelp - timenb)
	#define	RS_DSCINT	(1<<20)	//	[20] data set status change interrupt (dsch - dscenb)
	#define	RS_RBRL		(1<<21)	//	[21] receive buffer register loaded
	#define	RS_XBRE		(1<<22)	//	[22] transmit buffer register empty
	#define	RS_XSRE		(1<<23)	//	[23] transmit shift register empty
	#define	RS_TIMERR	(1<<24)	//	[24] time error
	#define	RS_TIMELP	(1<<25)	//	[25] timer elapsed
	#define	RS_RTS		(1<<26)	//	[26] request to send
	#define	RS_DSR		(1<<27)	//	[27] data set ready
	#define	RS_CTS		(1<<28)	//	[28] clear to send
	#define	RS_DSCH		(1<<29)	//	[29] data set status change
	#define	RS_FLAG		(1<<30)	//	[30] register load control flag set
	#define	RS_INT		(1<<31)	//	[31] interrupt

#if BUFFERED_RS232
// (500000/5/TM_HZ)		// max possible rate to deal with 
#define BUF_SIZ 	1024
#define BUF_MASK 	1023

#define BUFFER_EMPTY(rs,l)	(rs->l##_st == rs->l##_en)
#define BUFFER_FULL(rs,l)	(rs->l##_st == ((rs->l##_en+1)&BUF_MASK))

#define BUFFER_LEFT(rs,l)	((BUF_SIZ - rs->l##_st + rs->l##_en) & BUF_MASK)

	u8	xmit[BUF_SIZ], recv[BUF_SIZ];
	u32	t_st,t_en, r_st, r_en;			// pointers to ring
#endif

}	rs232regs;

#define RS_WORDSIZE(rs)		((rs->ctrl & (CTRL_RCL0+CTRL_RCL1)) + 5)


extern	rs232regs	rsregs[MAX_RS232_DEVS];

#undef uint

//	Returning vmXXX codes
int		Init_RS232_SysDeps(void);
int		Enable_RS232_SysDeps(void);
int		Disable_RS232_SysDeps(void);
int   	Term_RS232_SysDeps(void);

void	Reset_RS232_SysDeps(rs232regs *rs);
void	Set_CTRL_Register(rs232regs *rs);
void	Set_INVL_Register(rs232regs *rs);
void	Set_RCVRATE_Register(rs232regs *rs);
void	Set_XMITRATE_Register(rs232regs *rs);

//	called for any change
void	Set_Control_Bits(rs232regs *rs, u32 old, int bit);		// bits 16 to 21

#if !BUFFERED_RS232
//	called once per character
void	Transmit_Char(rs232regs *rs);
#else
//	called TM_HZ times per second
void	Transmit_Chars(rs232regs *rs);
#endif

//	called for any bit
void	Read_Status_Bits(rs232regs *rs);

#if !BUFFERED_RS232
//	called once at zero bit
void	Receive_Data(rs232regs *rs);
#else
//	called TM_HZ times per second
void	Receive_Chars(rs232regs *rs);
#endif

#if BUFFERED_RS232
//	called TM_HZ times per second
void	Update_Modem_Lines(rs232regs *rs);
#endif

#endif
