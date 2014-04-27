/*
  dsr_rs232.c					-- V9t9 module for RS232 DSR 

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
#define BUFFERED_RS232 1

#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <stddef.h>

#include "v9t9_common.h"
#include "v9t9.h"
#include "cru.h"
#include "command.h"
#include "9900.h"
#include "memory.h"
#include "dsr.h"
#include "timer.h"

#include "dsr_rs232.h"

extern vmModule realRS232DSR;

char        realrs232filename[OS_NAMESIZE] = "rs232.bin";
u8          realrs232dsr[8192];
char        rs232name[MAX_RS232_DEVS][OS_PATHSIZE] = { "", "" };

rs232regs   rsregs[MAX_RS232_DEVS];

#define _L	 LOG_RS232 | LOG_INFO
#if BUFFERED_RS232
static int  rs232_interrupt_tag;
#endif

/********************************************************************/


static void 
dumprs232(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L | L_3, _("loadctrl=%1X\t\n"), rs->loadctrl);
	if (rs->loadctrl >= 8) {
		module_logger(&realRS232DSR, _L | L_3, "* ");
		logger(_L | L_3,
			 _("ctrl=%02X\nrcl0=%d,rcl1=%d, clk4m=%d, Podd=%d,Penb=%d, SBS2=%d,SBS1=%d\t"),
			 rs->ctrl, !!(rs->ctrl & CTRL_RCL0), !!(rs->ctrl & CTRL_RCL1),
			 !!(rs->ctrl & CTRL_CLK4M), !!(rs->ctrl & CTRL_Podd),
			 !!(rs->ctrl & CTRL_Penb), !!(rs->ctrl & CTRL_SBS2),
			 !!(rs->ctrl & CTRL_SBS1));
	}
	if (rs->loadctrl < 8 && rs->loadctrl >= 4) {
		module_logger(&realRS232DSR, _L | L_3 |  0, "* ");
		logger(_L | L_3 |  0, _("invl=%03X\t"), rs->invl);
	}
	if (rs->loadctrl < 4 && rs->loadctrl >= 2) {
		module_logger(&realRS232DSR, _L | L_3, "* ");
		logger(_L | L_3 |  0, _("rcvrate=%03X\t"), rs->rcvrate);
	}
	if (rs->loadctrl < 2 && rs->loadctrl >= 1) {
		module_logger(&realRS232DSR, _L | L_3, "* ");
		logger(_L | L_3 |  0, _("xmitrate=%03X\t"), rs->xmitrate);
	}
	if (rs->loadctrl < 1) {
		module_logger(&realRS232DSR, _L | L_3, "* ");
		logger(_L | L_3 |  0, _("txchar=%02X (%c)\t"), rs->txchar, rs->txchar);
	}
	module_logger(&realRS232DSR, _L | L_3, _("recvbps=%d, xmitbps=%d\n"), rs->recvbps, rs->xmitbps);

#if BUFFERED_RS232
	module_logger(&realRS232DSR, _L | L_3, _("Write buffer: %d/%d  Read buffer: %d/%d\n"),
		 rs->t_st, rs->t_en, rs->r_st, rs->r_en);
#endif

/*	module_logger(&realRS232DSR, _L | L_3,_("rtson=%d, brkon=%d, rienb=%d, xbienb=%d, timenb=%d, dscenb=%d\n"),
		!!(rs->wrport&RS_RTSON), !!(rs->wrport&RS_BRKON), !!(rs->wrport&RS_RIENB),
		!!(rs->wrport&RS_XBIENB), !!(rs->wrport&RS_TIMEMB), !!(rs->wrport&RS_DSCENB));

	module_logger(&realRS232DSR, _L | L_3,_("rcverr=%d,rper=%d,rover=%d,rfer=%d,rfbd=%d,rsbd=%d,rin=%d\n"),
		!!(rs->rdport&RS_RCVERR), !!(rs->rdport&RS_RPER), !!(rs->rdport&RS_ROVER),
		!!(rs->rdport&RS_RFER), !!(rs->rdport&RS_RFBD), !!(rs->rdport&RS_RSBD),
		!!(rs->rdport&RS_RIN));
	module_logger(&realRS232DSR, _L | L_3,_("rbint=%d,xbint=%d,timint=%d,dscint=%d,rbrl=%d,xbre=%d,xsre=%d\n"),
		!!(rs->rdport&RS_RBINT), !!(rs->rdport&RS_XBINT), !!(rs->rdport&RS_TIMINT), !!(rs->rdport&RS_DSCINT),
		!!(rs->rdport&RS_RBRL), !!(rs->rdport&RS_XBRE), !!(rs->rdport&RS_XSRE));
	module_logger(&realRS232DSR, _L | L_3,_("timerr=%d,timelp=%d,rts=%d,dsr=%d,cts=%d,dsch=%d,flag=%d,int=%d\n"),
		!!(rs->rdport&RS_TIMERR), !!(rs->rdport&RS_TIMELP), !!(rs->rdport&RS_RTS),
		!!(rs->rdport&RS_DSR), !!(rs->rdport&RS_CTS), !!(rs->rdport&RS_DSCH),
		!!(rs->rdport&RS_FLAG), !!(rs->rdport&RS_INT));
*/
}

#define SETUP_RS232REGS(rs,addr)	rs232regs *rs = &rsregs[(addr - RS232_CRU_BASE) / 0x40 - 1]

/********************************************************************/

#if BUFFERED_RS232

/*	BUFFER STUFFERS	*/

//  Update status bits when buffer state changes,
//  possibly triggering an interrupt.
static void
Update_Flags_And_Ints(rs232regs * rs)
{
	bool        interruptible = false;

	// all buffered chars are immediately
	// available to be read.
	//
	if (!BUFFER_EMPTY(rs, r)) {
		rs->rdport |= RS_RBRL;

		// trigger interrupt?
		if (rs->wrport & RS_RIENB) {
			rs->rdport |= RS_RBINT;
			interruptible = true;
		}
	} else {
		rs->rdport &= ~(RS_RBRL | RS_RBINT);
	}

	// contrary to popular opinion, we will set RS_XBRE
	// here if the buffer is NOT FULL, so the 99/4A
	// can continue to stuff the buffer.
	//
	if (!BUFFER_FULL(rs, t)) {
		rs->rdport |= RS_XBRE;

		// trigger interrupt?
		if (rs->wrport & RS_XBIENB) {
			rs->rdport |= RS_XBINT;
			interruptible = true;
		}
	} else {
		rs->rdport &= ~(RS_XBRE | RS_XBINT);
	}

#warning data lines

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
		module_logger(&realRS232DSR, _L | L_2, _("*** Interrupt ***\n"));
		rs->rdport |= RS_INT;
		trigger9901int(M_INT_EXT);
	} else {
		rs->rdport &= ~RS_INT;
		reset9901int(M_INT_EXT);
	}
}


static void
Transmit_Char(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L | L_1, _("Buffering char %02X (%c)\n\n"), rs->txchar, rs->txchar);

	// Put char in buffer.  Kill most recent char if full.
	if (!BUFFER_FULL(rs, t)) {
		rs->xmit[rs->t_en] = rs->txchar;
		rs->t_en = (rs->t_en + 1) & BUF_MASK;
	} else {
		rs->xmit[(rs->t_en - 1 + BUF_SIZ) & BUF_MASK] = rs->txchar;
	}
	Update_Flags_And_Ints(rs);
}

static void
Receive_Data(rs232regs * rs)
{
	// Get char from buffer, or last one if empty.
	if (!BUFFER_EMPTY(rs, r)) {
		rs->rdport = (rs->rdport & ~0xff) | rs->recv[rs->r_st];
		rs->r_st = (rs->r_st + 1) & BUF_MASK;
	} else {
		rs->rdport = (rs->rdport & ~0xff) |
			rs->recv[(rs->r_st - 1 + BUF_SIZ) & BUF_MASK];
	}
	Update_Flags_And_Ints(rs);
	module_logger(&realRS232DSR, _L | L_1, _("Received char %02X (%c)\n"), rs->rdport & 0xff, rs->rdport & 0xff);
}

/*	Timer event which periodically checks the RS232 devices for
	activity, reading into buffers and writing from buffers.
	It is not efficient anywhere to call the OS for each status bit
	read, which is why we eat up the buffers the OS is storing for us. 	*/

static void
RS232_Monitor(void)
{
	int         rsidx;

	for (rsidx = 0; rsidx < MAX_RS232_DEVS; rsidx++) {
		rs232regs  *rs = &rsregs[rsidx];

		// transmit buffered chars.
		Transmit_Chars(rs);

		// receive characters from the OS.
		// these are available for reading immediately.
		Receive_Chars(rs);

		// update modem lines
		Update_Modem_Lines(rs);

		// update status flags and interrupt if needed
		Update_Flags_And_Ints(rs);
	}
}

static void
Flush_Buffers(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L | L_1, _("*** Flushing buffers"));
	// lose all readable data
	rs->r_st = rs->r_en = 0;

	// send all writeable data
	// (don't loop here!)
	Transmit_Chars(rs);

	if (!BUFFER_EMPTY(rs, t)) {
		module_logger(&realRS232DSR, _L | L_1, _("*** Transmit buffer was not empty\n"));
	}
	rs->t_st = rs->t_en = 0;
	dumprs232(rs);
}

#else

#define Flush_Buffers(x)

#endif

/********************************************************************/

/*	WRITE BITS  */

/*	Load control register handler  
 *
 *	The highest bit set in loadctrl determines which 
 *	write register is active.
 */

static int  wrt_reg_offs[] = {
	offsetof(rs232regs, txchar),	//  0
	offsetof(rs232regs, xmitrate),	//  1
	offsetof(rs232regs, rcvrate),	//  2
	offsetof(rs232regs, rcvrate),	//  3
	offsetof(rs232regs, invl),	//  4
	offsetof(rs232regs, invl),	//  5
	offsetof(rs232regs, invl),	//  6
	offsetof(rs232regs, invl),	//  7
	offsetof(rs232regs, ctrl),	//  8
	offsetof(rs232regs, ctrl),	//  9
	offsetof(rs232regs, ctrl),	//  10
	offsetof(rs232regs, ctrl),	//  11
	offsetof(rs232regs, ctrl),	//  12
	offsetof(rs232regs, ctrl),	//  13
	offsetof(rs232regs, ctrl),	//  14
	offsetof(rs232regs, ctrl)	//  15
};

#define WRT_REG(rs)	(*(u16 *)((u8 *)rs + wrt_reg_offs[rs->loadctrl&0xf]))

/*	
 *	Flag bits for registers multiplexed through loadctrl
 */

#define FLAG_TXCHAR(rs)		(1<<(RS_WORDSIZE(rs)-1))
#define FLAG_XMITRATE(rs)	(1<<10)
#define FLAG_RCVRATE(rs)	(1<<10)
#define FLAG_INVL(rs)		(1<<7)
#define FLAG_CTRL(rs)		(1<<7)

static void
Calc_BPS_Rate(rs232regs * rs, u16 rate, u32 * bps)
{
	// BPS = 3 MHz / ((CLK4M ? 4 : 3) * 2 * rate x 8*DIV8)
	u32         div;

	// input speed
	div = (((rs->ctrl & CTRL_CLK4M) ? 4 : 3) * 2
		   * (rate & RATE_MASK) * ((rate & RATE_DIV8) ? 8 : 1));
	if (div == 0)
		*bps = 50;
	else
		*bps = (3000000 /*baseclockhz*/ / div);

	module_logger(&realRS232DSR, _L | L_1, _("Calc_BPS_Rate:  *bps = %d\n\n"), *bps);
}

/*
 *	Multiplexer for registers handled by loadctrl
 *
 */

static void
Trigger_Change(rs232regs * rs, int cbit, int dbit)
{
	if ((cbit == 8) || (rs->loadctrl & 8)) {
		if (!dbit || dbit == FLAG_CTRL(rs)) {
			rs->ctrl = rs->wrbits & 0x7ff;
			dumprs232(rs);
			Set_CTRL_Register(rs);
			Flush_Buffers(rs);
			rs->loadctrl &= ~8;
		}
	} else if ((cbit == 4) || (rs->loadctrl & 4)) {
		if (!dbit || dbit == FLAG_INVL(rs)) {
			rs->invl = rs->wrbits & 0xff;
			dumprs232(rs);
			Set_INVL_Register(rs);
			Flush_Buffers(rs);
			rs->loadctrl &= ~4;
		}
	} else if (cbit == 1 || cbit == 2) {
		if ((cbit == 2) || (rs->loadctrl & 2)) {
			if (!dbit || dbit == FLAG_RCVRATE(rs)) {
				rs->rcvrate = rs->wrbits & 0x7ff;
				Calc_BPS_Rate(rs, rs->rcvrate, &rs->recvbps);
				dumprs232(rs);
				Set_RCVRATE_Register(rs);
				Flush_Buffers(rs);
				rs->loadctrl &= ~2;
			}
		}
		if ((cbit == 1) || (rs->loadctrl & 1)) {
			if (!dbit || dbit == FLAG_XMITRATE(rs)) {
				rs->xmitrate = rs->wrbits & 0x7ff;
				Calc_BPS_Rate(rs, rs->xmitrate, &rs->xmitbps);
				dumprs232(rs);
				Set_XMITRATE_Register(rs);
				Flush_Buffers(rs);
				rs->loadctrl &= ~1;
			}
		}
	} else if (!dbit || dbit == FLAG_TXCHAR(rs)) {
		rs->txchar = rs->wrbits & (0xff >> (8 - RS_WORDSIZE(rs)));
		dumprs232(rs);
		Transmit_Char(rs);
	}
}

/* 
 *	Writes of low register bits
 */
static      u32
RealRS232_0_10_w(u32 addr, u32 data, u32 num)
{
	u32         bit = 1 << ((addr & 0x1f) >> 1);

	SETUP_RS232REGS(rs, addr);

	if (bit >= 0x100) {
		module_logger(&realRS232DSR, _L | L_3, _("RealRS232_0_10_w: %d / %d\n"), (addr & 0x3f) / 2, data);
	}

	rs->rdport &= ~RS_FLAG;

	if (data)
		rs->wrbits |= bit;
	else
		rs->wrbits &= ~bit;

/*
	if (bit == FLAG_TXCHAR(rs) && rs->loadctrl == 0) {
		rs->txchar = rs->wrbits & (0xff >> (8 - RS_WORDSIZE(rs)));
		dumprs232(rs);
		Transmit_Char(rs);
	}
*/
	Trigger_Change(rs, 0, bit);

	return 0;
}

/*
 *	Load control register bits
 */
static      u32
RealRS232_11_14_w(u32 addr, u32 data, u32 num)
{
	int         bit = 1 << (((addr & 0x1f) >> 1) - 11);
	u16         old;

	SETUP_RS232REGS(rs, addr);

	module_logger(&realRS232DSR, _L | L_3, _("RealRS232_11_14_w: %d / %d\n"), (addr & 0x3f) / 2, data);

	rs->rdport |= RS_FLAG;

	old = rs->loadctrl;
	if (data)
		rs->loadctrl |= bit;
	else
		rs->loadctrl &= ~bit;

	//  set this register
	Trigger_Change(rs, bit, 0);
	return 0;
}


/*
 *	Remaining write ports
 *
 */
static      u32
RealRS232_16_21_w(u32 addr, u32 data, u32 num)
{
	u16         old;
	u32         bit = 1 << ((addr & 0x3f) >> 1);

	SETUP_RS232REGS(rs, addr);

	module_logger(&realRS232DSR, _L | L_3, _("RealRS232_16_21_w: %d / %d\n"), (addr & 0x3f) / 2, data);

	old = rs->wrport;
	if (data)
		rs->wrport |= bit;
	else
		rs->wrport &= ~bit;

	dumprs232(rs);
	Set_Control_Bits(rs, old, bit);
	return 0;
}

/*
 *	Read character
 *
 */
static      u32
RealRS232_0_7_r(u32 addr, u32 data, u32 num)
{
	u32         bit = 1 << ((addr & 0xf) >> 1);
	u32         ret;

	SETUP_RS232REGS(rs, addr);

	if (bit == 1) {
		Receive_Data(rs);
		dumprs232(rs);
	}

	ret = !!(rs->rdport & bit);

	if (bit >= 0x100) {
		module_logger(&realRS232DSR, _L | L_3, _("RealRS232_0_7_r: %04X / %d\n"), addr, ret);
	}

	return ret;
}

/*
 *	Read status bit
 *
 */
static      u32
RealRS232_9_31_r(u32 addr, u32 data, u32 num)
{
	u32         bit = 1 << ((addr & 0x3f) >> 1);
	u32         ret;

	SETUP_RS232REGS(rs, addr);

	Read_Status_Bits(rs);

#if BUFFERED_RS232
	// force DSR while buffer is nonempty
	if (!BUFFER_EMPTY(rs, r)) {
		rs->rdport |= RS_DSR;
	}
#endif

	dumprs232(rs);

	ret = !!(rs->rdport & bit);

	//  turn off bit once read
//  if (bit == RS_INT || bit == RS_RBINT || bit == RS_XBINT || 
//      bit == RS_TIMINT || bit == RS_DSCINT) 
//      rs->rdport &= ~bit;

	module_logger(&realRS232DSR, _L | L_3, _("RealRS232_9_31_r: %d / %d\n"), (addr & 0x3f) / 2, ret);
	return ret;
}

static      u32
RealRS232_Reset_w(u32 addr, u32 data, u32 base)
{
	SETUP_RS232REGS(rs, addr);

	module_logger(&realRS232DSR, _L | L_3, _("RealRS232_Reset_w: %d\n"), data);

	if (data) {
#if BUFFERED_RS232
		if (rs232_interrupt_tag)
			TM_ResetEvent(rs232_interrupt_tag);
#endif
		memset(rs, 0, sizeof(rs232regs));
		rs->loadctrl = 0xf;
		Set_CTRL_Register(rs);
		Set_INVL_Register(rs);
		Set_RCVRATE_Register(rs);
		Set_XMITRATE_Register(rs);
		Reset_RS232_SysDeps(rs);
		dumprs232(rs);

#if BUFFERED_RS232
		if (!rs232_interrupt_tag)
			rs232_interrupt_tag = TM_UniqueTag();

		TM_SetEvent(rs232_interrupt_tag, TM_HZ, 0, TM_FUNC | TM_REPEAT,
					RS232_Monitor);
#endif
	}
	return 0;
}

/********************************************************************/

mrstruct    dsr_rom_realrs232_handler = {
	realrs232dsr, realrs232dsr, NULL,			/* ROM */
	NULL,
	NULL,
	NULL,
	NULL
};

static      u32
RealRS232ROM_w(u32 addr, u32 data, u32 num)
{
	if (data) {
//		module_logger(&realRS232DSR, _L | L_1, _("real RS232 DSR on\n"));
		report_status(STATUS_RS232_ACCESS, 0, true);
//      debugger_enable(true);
		dsr_set_active(&realRS232DSR);

		SET_AREA_HANDLER(0x4000, 0x2000, &dsr_rom_realrs232_handler);

	} else {
//		module_logger(&realRS232DSR, _L | 0, _("real RS232 DSR off\n"));
		report_status(STATUS_RS232_ACCESS, 0, false);
//      if (pc<0x2000)
//      debugger_enable(false);
		dsr_set_active(NULL);
		SET_AREA_HANDLER(0x4000, 0x2000, &zero_memory_handler);
	}
	return 0;
}


/************************************/


static      vmResult
realrs232_getcrubase(u32 * base)
{
	*base = RS232_CRU_BASE;
	return vmOk;
}

static      vmResult
realrs232_filehandler(u32 code)
{
	return vmOk;
}

static      vmResult
realrs232_detect(void)
{
	return vmOk;
}

static      vmResult
realrs232_init(void)
{
	command_symbol_table *realrs232commands =
		command_symbol_table_new(_("TI RS232 DSR Options"),
								 _("These commands control the TI RS232 emulation"),

		 command_symbol_new("RS232_1",
							_("Give local name for first RS232 port"),
							c_DONT_SAVE,
							NULL /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_string
							(_("filename"),
							 _("filename or device for current operating system"),
							 NULL /* action */ ,
							 ARG_STR(rs232name[0]),
							 NULL /* next */ )
							,

		  command_symbol_new("RS232_2",
							 _("Give local name for second RS232 port"),
							 c_DONT_SAVE,
							 NULL /* action */ ,
							 RET_FIRST_ARG,
							 command_arg_new_string
							 (_("filename"),
							  _("filename or device for current operating system"),
							  NULL /* action */ ,
							  ARG_STR(rs232name[1]),
							  NULL /* next */ )
							 ,

		   command_symbol_new("RS232DSRFileName",
							  _("Name of RS232 DSR ROM image which fits in the CPU address space >4000...>5FFF"),
							  c_STATIC,
							  NULL /* action */ ,
							  RET_FIRST_ARG,
							  command_arg_new_string
							  (_("file"),
							   _("name of binary image"),
							   NULL /* action */ ,
							   ARG_STR
							   (realrs232filename),
							   NULL /* next */ )
							  ,

		  NULL /* next */ ))),

		 NULL /* sub */ ,

		 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, realrs232commands);

	return Init_RS232_SysDeps();
}

static struct {
	int         bit;
	crufunc    *func;
} rs232_writers[] = {
	0, RealRS232_0_10_w,
		1, RealRS232_0_10_w,
		2, RealRS232_0_10_w,
		3, RealRS232_0_10_w,
		4, RealRS232_0_10_w,
		5, RealRS232_0_10_w,
		6, RealRS232_0_10_w,
		7, RealRS232_0_10_w,
		8, RealRS232_0_10_w,
		9, RealRS232_0_10_w,
		10, RealRS232_0_10_w,
		11, RealRS232_11_14_w,
		12, RealRS232_11_14_w,
		13, RealRS232_11_14_w,
		14, RealRS232_11_14_w,
		16, RealRS232_16_21_w,
		17, RealRS232_16_21_w,
		18, RealRS232_16_21_w,
		19, RealRS232_16_21_w,
		20, RealRS232_16_21_w,
		21, RealRS232_16_21_w, 31, RealRS232_Reset_w, -1, 0};

static struct {
	int         bit;
	crufunc    *func;
} rs232_readers[] = {
	0, RealRS232_0_7_r,
		1, RealRS232_0_7_r,
		2, RealRS232_0_7_r,
		3, RealRS232_0_7_r,
		4, RealRS232_0_7_r,
		5, RealRS232_0_7_r,
		6, RealRS232_0_7_r,
		7, RealRS232_0_7_r,
		9, RealRS232_9_31_r,
		10, RealRS232_9_31_r,
		11, RealRS232_9_31_r,
		12, RealRS232_9_31_r,
		13, RealRS232_9_31_r,
		14, RealRS232_9_31_r,
		15, RealRS232_9_31_r,
		16, RealRS232_9_31_r,
		17, RealRS232_9_31_r,
		19, RealRS232_9_31_r,
		20, RealRS232_9_31_r,
		21, RealRS232_9_31_r,
		22, RealRS232_9_31_r,
		23, RealRS232_9_31_r,
		24, RealRS232_9_31_r,
		25, RealRS232_9_31_r,
		26, RealRS232_9_31_r,
		27, RealRS232_9_31_r,
		28, RealRS232_9_31_r,
		29, RealRS232_9_31_r,
		30, RealRS232_9_31_r, 31, RealRS232_9_31_r, -1, 0};

static int
add_rs232_device(int base)
{
	int         idx;

	for (idx = 0; rs232_writers[idx].bit >= 0; idx++) {
		if (!cruadddevice(CRU_WRITE,
						  RS232_CRU_BASE + base + rs232_writers[idx].bit * 2,
						  1, rs232_writers[idx].func))
			return 0;
	}
	for (idx = 0; rs232_readers[idx].bit >= 0; idx++) {
		if (!cruadddevice(CRU_READ,
						  RS232_CRU_BASE + base + rs232_readers[idx].bit * 2,
						  1, rs232_readers[idx].func))
			return 0;
	}
	return 1;
}

static int
del_rs232_device(int base)
{
	int         idx;

	for (idx = 0; rs232_writers[idx].bit >= 0; idx++) {
		if (!crudeldevice(CRU_WRITE,
						  RS232_CRU_BASE + base + rs232_writers[idx].bit * 2,
						  1, rs232_writers[idx].func))
			return 0;
	}
	for (idx = 0; rs232_readers[idx].bit >= 0; idx++) {
		if (!crudeldevice(CRU_READ,
						  RS232_CRU_BASE + base + rs232_readers[idx].bit * 2,
						  1, rs232_readers[idx].func))
			return 0;
	}
	return 1;
}

static      vmResult
realrs232_enable(void)
{
	vmResult    res;

	module_logger(&realRS232DSR, _L | L_1, _("setting up TI RS232 DSR ROM\n"));
/*
	if (0 == data_load_dsr(romspath, realrs232filename,
					 _("TI RS232 DSR ROM"), realrs232dsr)) 
		return vmNotAvailable;
*/

	res = Enable_RS232_SysDeps();
	if (res != vmOk)
		return vmNotAvailable;

	if (cruadddevice(CRU_WRITE, RS232_CRU_BASE, 1, RealRS232ROM_w) &&
		add_rs232_device(0x40) && add_rs232_device(0x80)) {
		return vmOk;
	} else
		return vmNotAvailable;
}

static      vmResult
realrs232_disable(void)
{
	Disable_RS232_SysDeps();
	crudeldevice(CRU_WRITE, RS232_CRU_BASE, 1, RealRS232ROM_w);
	del_rs232_device(0x40);
	del_rs232_device(0x80);
	return vmOk;
}

static      vmResult
realrs232_restart(void)
{
	if (0 == data_load_dsr(romspath, realrs232filename,
					 _("TI RS232 DSR ROM"), realrs232dsr)) 
		return vmNotAvailable;
	else
		return vmOk;
}

static      vmResult
realrs232_restop(void)
{
	return vmOk;
}

static      vmResult
realrs232_term(void)
{
	Term_RS232_SysDeps();
	return vmOk;
}

static vmDSRModule realRS232Module = {
	1,
	realrs232_getcrubase,
	realrs232_filehandler
};

vmModule    realRS232DSR = {
	3,
	"TI real-RS232 DSR",
	"dsrRealRS232",

	vmTypeDSR,
	vmFlagsNone,

	realrs232_detect,
	realrs232_init,
	realrs232_term,
	realrs232_enable,
	realrs232_disable,
	realrs232_restart,
	realrs232_restop,
	{(vmGenericModule *) & realRS232Module}
};
