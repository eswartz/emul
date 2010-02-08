/*
  9901.c						-- 9901 CRU I/O functionality 
									(timer, interrupts, keyboard, cassette)

  (c) 1994-2001 Edward Swartz

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

#include "v9t9_common.h"
#include "v9t9.h"
#include "cru.h"
#include "9900.h"

#include "keyboard.h"
#include "emulate.h"
#include "sound.h"
#include "vdp.h"
#include "timer.h"

#define _L	 LOG_CRU | LOG_INFO

hw9901state hw9901;
AudioGate audiogate;

/***************************************************/

void        
reset9901()
{
	memset(crukeyboardmap, 0, sizeof(crukeyboardmap));
	memset((void *)&hw9901, 0, sizeof(hw9901));
}

/*
	handle9901() is called when something about the 
	interrupt state changes (processor interrupt mask,
	pending interrupt mask, etc).  If an interrupt is needed,
	this triggers the INTPIN_INTREQ pin on the 9900, and sets the 
	ST_INTERRUPT flag in stateflag.
*/
static void
handle9901(void)
{
	// any of these interrupts enabled?  [optimization]
	if (hw9901.currentints & hw9901.int9901) {
		// There are 16 levels, and intlevel is 16 bits.
		// When it goes from 0x8000 to 0, we will see there
		// are no interrupts that can be passed.
		hw9901.intlevel = M_INT_EXT;
		while (hw9901.intlevel && ((hw9901.currentints & hw9901.int9901) & hw9901.intlevel) == 0)
			hw9901.intlevel =
				(hw9901.intlevel << 1) & (M_INT_EXT | M_INT_VDP | M_INT_CLOCK);

		if (hw9901.intlevel) {
			logger(_L | L_1, _("Triggering interrupt... %04X/%04X/%04X\n"), hw9901.intlevel,
				 hw9901.currentints, hw9901.int9901);

			intpins9900 |= INTPIN_INTREQ;
			stateflag |= ST_INTERRUPT;
		}
	}
}

/*
	Trigger an interrupt, via hardware.
*/
void
trigger9901int(u16 mask)
{
	if (hw9901.int9901 & mask) {
		hw9901.currentints |= mask;

		// see if it applies
		handle9901();
	}
}


/*
	Reset an interrupt, via hardware.
*/
void
reset9901int(u16 mask)
{
	if (hw9901.currentints & mask) {
		hw9901.currentints &= ~mask;

		// take care of pending interrupts
		handle9901();
	}
}

u16 read9901int(void)
{
	return hw9901.intlevel;
}

/***************************************************/

static int clock9901tag;

// if TM_HZ < 46875, we must pretend
// to have skipped this many clicks
// for every call to clock_9901_event().
static	u32  clockdelta;

static void
clock_9901_event(int tag)
{
	logger(_L | L_2, "**** clock_9901_event %d/%d ****\n", hw9901.timer, clockdelta);
	// '<' prevents timer interrupts
	// when clockinvl (thus clockdelta) is zero
	if (hw9901.timer < clockdelta) {
		trigger9901int(M_INT_CLOCK);

		// if resolution is too low, at least
		// keep the wavelength the same
		if (hw9901.clockinvl < clockdelta)
			hw9901.timer += clockdelta + hw9901.clockinvl;
		else
			hw9901.timer += hw9901.clockinvl;
	}

	hw9901.timer -= clockdelta;
}

/*
 *	Called from emulate() to mark time.
 */
void
handle9901tick(void)
{
	if (!hw9901.clockmode && hw9901.clockinvl && hw9901.timer-- == 0) {
		logger(_L | L_2, "**** handle9901tick ****");
		trigger9901int(M_INT_CLOCK);
		hw9901.timer = hw9901.clockinvl;

	}
}

void
setclockmode9901(u8 onoff)
{
	// SBO 0 turns on clock mode, SBZ 0 turns on I/O mode
	if (!onoff) {
		// set new speed
		hw9901.clockinvl = hw9901.latchedclockinvl;
		hw9901.timer = hw9901.clockinvl;

//      TM_ResetEvent(clock9901tag);
		if (hw9901.clockinvl) {
			logger(_L | 0, "*** Setting clock to %5.2f Hz\n\n",
				 3000000.0 / 64 / hw9901.clockinvl);
//          #warning main clock hertz
//          clockdelta = 3000000 / 64 / TM_HZ;
//          TM_SetEvent(clock9901tag, TM_HZ * 100 / 100, 0, 
//                      TM_FUNC|TM_REPEAT, clock_9901_event);
		} else {
//          clockdelta = 0;
		}

		hw9901.clockmode = 0;
	} else {
		// setting clock mode freezes read register
		// (not to mention allowing access to the read register!)
		// but the decrementer continues    

		if (hw9901.clockinvl)
			hw9901.latchedtimer = hw9901.timer % hw9901.clockinvl;
		else
			hw9901.latchedtimer = 0;

		hw9901.clockmode = 1;
	}
}

/******************************************************/

static      u32
crur9901_0(u32 addr, u32 data, u32 num)
{
	return 1;
}

/*	Read INT_EXT status or lowest bit of timer.  */
static      u32
crur9901_1(u32 addr, u32 data, u32 num)
{
	if (hw9901.clockmode)
		return (hw9901.latchedtimer & 1);
	else if (hw9901.int9901 & M_INT_EXT) {
		logger(_L | L_1, "crur9901_1: currentints=%04X\n", hw9901.currentints);
		return !(hw9901.currentints & M_INT_EXT);
	} else
		return 0;
}

/*	Read INT_VDP status or 2nd bit of timer.  
	Don't return INT_VDP status if INT1 is still waiting.
*/
static      u32
crur9901_2(u32 addr, u32 data, u32 num)
{
	if (hw9901.clockmode)
		return (hw9901.latchedtimer >> 1) & 1;
	else if (hw9901.int9901 & M_INT_VDP) {
		logger(_L | L_1, "crur9901_2: currentints=%04X\n", hw9901.currentints);
		//return !(hw9901.currentints&M_INT_EXT) && !!(hw9901.currentints & M_INT_VDP);
		return !(hw9901.currentints & M_INT_VDP);
	} else
		return 0;
}

static      u32
crur9901_LS(u32 addr, u32 data, u32 num)
{
	return (hw9901.clockmode ? 0 : 1);
}

/*	Note:  the keyboard module is only allowed to
	set the crukeyboardmap[], not take over the I/O for it. */

static      u32
crur9901_KS(u32 addr, u32 data, u32 num)
{
	int         mask;
	u32         bit = addr / 2;

	mask = 1 << (bit - 3);

	if (hw9901.clockmode)
		return (hw9901.latchedtimer >> (bit - 1)) & 0x1;
	else if (hw9901.int9901 & (1 << bit))
		return !(hw9901.currentints & ~(~0 << bit)) && !!(hw9901.currentints & (1 << bit));
	else {
		u32         alphamask =
			((bit - 3) == 4) ? ((AlphaLock || !caps) ? 0 : 0x10) : 0;

		logger(_L | L_2, "crukeyboardcol=%X, mask=%X, addr=%2X\n", crukeyboardcol,
			 mask, addr);
		return !(((crukeyboardmap[crukeyboardcol] & mask) | alphamask));
	}
}

static      u32
cruralpha(u32 addr, u32 data, u32 num)
{
	return caps & 1;

}

static      u32
crurCSIn(u32 addr, u32 data, u32 num)
{
	return cassette_read();
}

/**********************************************/

static      u32
cruw9901_0(u32 addr, u32 data, u32 num)
{
	setclockmode9901(data);
	return 0;
}

/*
	Change an interrupt enable, or change bit in clock interval
*/
static      u32
cruw9901_S(u32 addr, u32 data, u32 num)
{
	u32         bit = addr / 2;

	if (hw9901.clockmode) {
		hw9901.latchedclockinvl =
			(hw9901.latchedclockinvl & ~(1 << bit)) | (data << bit);
		logger(_L | L_2, "cruw9901_S:  hw9901.latchedclockinvl=%04X\n",
			 hw9901.latchedclockinvl);
	} else {
		u32         mask = (~((~0) << num)) << bit;

/*		if (addr == 0x1a) {
			cruwAudioGate(addr, data, num);
			return;
		} else if (addr == 0x1c) {
			cruwCS2Motor(addr, data, num);
			return;
		}*/

		logger(_L | L_2, _("Altering 9901 bit... addr=%04X, data=%04X,mask=%04X\n"), addr,
			 data, mask);

		//  First, writing a 0 will disable the interrupt,
		//  and writing a 1 will enable it, or acknowledge it.
		hw9901.int9901 = (hw9901.int9901 & ~mask) | (data << bit);
		logger(_L | L_2, _("before reset: int9901 = %04X, currentints = %04X\n"), hw9901.int9901,
			 hw9901.currentints);

		//  This will acknowledge the interrupt, and possibly
		//  trigger a lower-level pending interrupt.
		reset9901int(data << bit);
		logger(_L | L_2, _("after reset: int9901 = %04X, currentints = %04X\n"), hw9901.int9901,
			 hw9901.currentints);

//      if (bit == 3)
//          if (data) debugger_enable(); else debugger_disable();

		/*  int9901change();
		   handle9901(); */
	}
	return 0;
}

static      u32
cruwCS1Motor(u32 addr, u32 data, u32 num)
{
	cassette_set_motor(0, data);
	return 0;
}

static      u32
cruwCS2Motor(u32 addr, u32 data, u32 num)
{
	cassette_set_motor(1, data);
	return 0;
}

/*	Enable/disable audio gate */
static      u32
cruwAudioGate(u32 addr, u32 data, u32 num)
{
	audiogate.play = data;
	logger(_L | L_2, _("audiogate.play=%d\n"), data);
	return 0;
}

/*	Send bit to cassette  */
static      u32
cruwCSOut(u32 addr, u32 data, u32 num)
{
	/*  purposefully avoid gathering more than 1/100 second
	   of info at a time */
	if (audiogate.latch != data || currenttime < audiogate.base_time)	// "a lot of" time passed
	{
		audiogate.latch = data;
		audiogate.hertz = baseclockhz / 64 / BASE_EMULATOR_HZ;

		logger(_L | L_2, "currenttime=%d, base_time=%d\n", currenttime,
			 audiogate.base_time);

		audiogate.length = currenttime - audiogate.base_time;
		if (currenttime < audiogate.base_time) {
			// flush buffer -- we will generate data too fast to play it!
			//SOUNDPLAY(vms_AG, NULL, 0, 0);
			audiogate.length += audiogate.hertz;
		}

		/*  write to "cassette"... */
		cassette_write(audiogate.length, baseclockhz / 64);

		/*  also play  */
		SOUNDPLAY(vms_AGw, NULL, audiogate.length, baseclockhz / 64);

		audiogate.base_time = currenttime;
	}

	audiogate.last_time = currenttime;
	return 0;
}

/***********************************************/

/*	Note:  the keyboard module is only allowed to
	set the crukeyboardmap[], not take over the I/O for it. */

static      u32
cruwkeyboard_0(u32 addr, u32 data, u32 num)
{
	crukeyboardcol = (crukeyboardcol & 3) | (data << 2);
	return 0;
}

static      u32
cruwkeyboard_1(u32 addr, u32 data, u32 num)
{
	crukeyboardcol = (crukeyboardcol & 5) | (data << 1);
	return 0;
}

static      u32
cruwkeyboard_2(u32 addr, u32 data, u32 num)
{
	crukeyboardcol = (crukeyboardcol & 6) | (data);
	return 0;
}

static      u32
cruwAlpha(u32 addr, u32 data, u32 num)
{
	AlphaLock = data;
	return 0;
}

/*****************************************/

int
setup_9901(void)
{
	clock9901tag = TM_UniqueTag();

	if (cruadddevice(CRU_WRITE, 0x0, 1, cruw9901_0) &&
		cruadddevice(CRU_WRITE, 0x2, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x4, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x6, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x8, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0xa, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0xc, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0xe, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x10, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x12, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x14, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x16, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x18, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x1a, 1, cruw9901_S) &&
		cruadddevice(CRU_WRITE, 0x1c, 1, cruw9901_S) &&
//      cruadddevice(CRU_WRITE,0x1a,1,cruwAudioGate) &&     // primary use as timer bit
//      cruadddevice(CRU_WRITE,0x1c,1,cruwCS2Motor) &&      // primary use as timer bit
//      cruadddevice(CRU_WRITE,0x1e,1,cruwCS1Motor) &&
		cruadddevice(CRU_WRITE, 0x24, 1, cruwkeyboard_2) &&
		cruadddevice(CRU_WRITE, 0x26, 1, cruwkeyboard_1) &&
		cruadddevice(CRU_WRITE, 0x28, 1, cruwkeyboard_0) &&
		cruadddevice(CRU_WRITE, 0x2A, 1, cruwAlpha) &&
		cruadddevice(CRU_WRITE, 0x2c, 1, cruwCS1Motor) &&
		cruadddevice(CRU_WRITE, 0x2e, 1, cruwCS2Motor) &&
		cruadddevice(CRU_WRITE, 0x30, 1, cruwAudioGate) &&
		cruadddevice(CRU_WRITE, 0x32, 1, cruwCSOut) &&
		cruadddevice(CRU_READ, 0x0, 1, crur9901_0) &&
		cruadddevice(CRU_READ, 0x2, 1, crur9901_1) &&
		cruadddevice(CRU_READ, 0x4, 1, crur9901_2) &&
		cruadddevice(CRU_READ, 0x6, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0x8, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0xa, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0xc, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0xe, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0x10, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0x12, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0x14, 1, crur9901_KS) &&
		cruadddevice(CRU_READ, 0x18, 1, crur9901_LS) &&	// "Pull up 10K to +5V"
//      cruadddevice(CRU_READ,0x1a,1,crur9901_LS) &&    // output only
//      cruadddevice(CRU_READ,0x1c,1,crur9901_LS) &&    // output only
//      cruadddevice(CRU_READ,0x1e,1,crur9901_LS) &&    // output only
		cruadddevice(CRU_READ, 0x20, 1, crur9901_LS) &&	// not connected
		cruadddevice(CRU_READ, 0x22, 1, crur9901_LS) &&	// not connected
		cruadddevice(CRU_READ, 0x2a, 1, cruralpha) &&
		cruadddevice(CRU_READ, 0x36, 1, crurCSIn)) {
		return 1;
	} else
		return 0;
}

DECL_SYMBOL_ACTION(hw9901_machine_state)
{
	char *str;
	if (task == csa_READ) {
		char tmp[(sizeof(hw9901)+sizeof(audiogate))*2+1];

		if (iter)
			return 0;

		emulate_bin2hex((u8 *)&hw9901, tmp, sizeof(hw9901));
		command_arg_set_string(sym->args, tmp);
		emulate_bin2hex((u8 *)&audiogate, tmp, sizeof(audiogate));
		command_arg_set_string(sym->args->next, tmp);
		
		return 1;
	}
	command_arg_get_string(sym->args, &str);
	emulate_hex2bin(str, (u8 *)&hw9901, sizeof(hw9901));
	command_arg_get_string(sym->args->next, &str);
	emulate_hex2bin(str, (u8 *)&audiogate, sizeof(audiogate));

	setclockmode9901(hw9901.clockmode);
	return 1;
}
