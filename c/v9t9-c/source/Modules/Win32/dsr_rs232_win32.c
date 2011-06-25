/*
  dsr_rs232_win32.c				-- V9t9 module backend for RS232 DSR

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

/*
 *	Internal RS232 handler for Win32.
 *
 */

#include "winv9t9.h"

#include "v9t9_common.h"
#include "emulate.h"
#include "timer.h"
#include "9901.h"
#include "dsr_rs232.h"

#define _L LOG_RS232|LOG_INFO

#define ERROR_IF(s,ri,x,ex) do { if (x) { module_logger(&realRS232DSR, _L|LOG_USER|LOG_ERROR, "failed '%s' for %s (%s)\n", \
				s, rs232name[ri], OS_GetErrText(GetLastError())); ex; } } while(0)

struct RSInfo {
	HANDLE      rsh;		// open handle for serial device
	HANDLE      mutex;		// mutex for rsregs
//  DWORD   events; // current EV_xxx status of port (independent of CRU lines)
//  DWORD   modem;  // current MS_xxx modem line status (independent of CRU lines)
	u32         rdflags;	// real flags for port

	//  Interrupt thread (handles state)
	HANDLE      interrupt_thread;

	//  Old attributes for ports
	DCB         oldrsattrs;
	//  New attributes
	DCB         rsattrs;
} rsinfo[MAX_RS232_DEVS];

#define	RSH(rs)			(rsinfo[rs-rsregs].rsh)
#define	RSMUTEX(rs)		(rsinfo[rs-rsregs].mutex)
#define RSFLAGS(rs)		(rsinfo[rs-rsregs].flags)

//  Last available port; remaining ones are files
static int  rsmax;

#define	RS_IS_TTY(x)	(x < rsmax)

//static int rs232_interrupt_tag;

static DWORD WINAPI My_RS232_Interrupt(LPVOID arg);

static DWORD rs232_interrupt_thread_ID;

/*
 *	Initialize system dependencies on RS232.
 *
 *	Assign up to MAX_RS232_DEVS devices from COM1-4,
 *	use files for remaining RS232 devices.
 */

int         
Init_RS232_SysDeps(void)
{
	int         rsidx;
	int         tty = 1;
	int         first_try;
	HANDLE      h;

	rsmax = 0;
	first_try = 1;
	while (rsmax < MAX_RS232_DEVS && tty <= 4) {
		if (first_try && *rs232name[rsmax]) {
			if (strstr(rs232name[rsmax], "COM") != NULL ||
				strstr(rs232name[rsmax], "com") != NULL) {
				rsmax++;
				continue;
			} else {
				break;
			}
		}
		first_try = 0;

		sprintf(rs232name[rsmax], "COM%d", tty);
		h = CreateFile(rs232name[rsmax], GENERIC_READ | GENERIC_WRITE,
					   0 /* share */ , NULL /* security */ , OPEN_EXISTING,
					   FILE_ATTRIBUTE_NORMAL, NULL /* overlapped */ );
		if (h != INVALID_HANDLE_VALUE) {
			CloseHandle(h);
			tty++;
			rsmax++;
			first_try = 1;
			module_logger(&realRS232DSR, _L|LOG_USER, _("RS232/%d is %s\n"), rsmax,
				 rs232name[rsmax - 1]);
		} else {
			tty++;
		}
	}

	for (rsidx = rsmax; rsidx < MAX_RS232_DEVS; rsidx++) {
		if (*rs232name[rsmax])
			continue;
		sprintf(rs232name[rsmax], "serial%d.txt", rsidx);
		module_logger(&realRS232DSR, _L|LOG_USER, _("RS232/%d is %s\n"), rsidx + 1,
			 rs232name[rsidx]);
	}

	if (rsmax == 0) {
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
			 _("could not assign any serial ports\n"));
		return vmNotAvailable;
	} else if (rsmax < MAX_RS232_DEVS) {
		module_logger(&realRS232DSR, _L|LOG_USER, _("could not assign %d serial ports,\n"
			 "only RS232/1 through RS232/%d are available\n"),
			 MAX_RS232_DEVS, rsmax);
	} else {
		module_logger(&realRS232DSR, _L|LOG_USER, _("assigned %d serial ports\n"), rsmax);
	}

	return vmOk;
}

/*
 *	Enable system-dependent RS232 use 
 *
 *	Open all ports for the duration of the RS232 module's use;
 *	use files for extra ports.
 */
int
Enable_RS232_SysDeps(void)
{
	int         rsidx;
	struct RSInfo *rsi;

	for (rsidx = 0; rsidx < MAX_RS232_DEVS; rsidx++) {
		rsi = &rsinfo[rsidx];

		if (strstr(rs232name[rsidx], "COM") != NULL ||
			strstr(rs232name[rsidx], "com") != NULL)
			rsi->rsh =
				CreateFile(rs232name[rsidx], GENERIC_READ | GENERIC_WRITE, 0	/* share */
						   , NULL /* security */ , OPEN_EXISTING,
						   FILE_ATTRIBUTE_NORMAL, NULL /* overlapped */ );
		else
			rsi->rsh =
				CreateFile(rs232name[rsidx], GENERIC_READ | GENERIC_WRITE, 0	/* share */
						   , NULL /* security */ , CREATE_ALWAYS,
						   FILE_ATTRIBUTE_NORMAL, NULL /* overlapped */ );

		ERROR_IF(_("opening RS232 port"), rsidx,
				 (rsi->rsh == INVALID_HANDLE_VALUE), return vmNotAvailable);

		// only set attrs on real ports
		if (RS_IS_TTY(rsidx)) {
			DCB        *dcb;
			DWORD       events;
			COMMTIMEOUTS timeouts;

			// buffer input but not output
			ERROR_IF(_("setting comm buffers"), rsidx,
					 !SetupComm(rsi->rsh, 1024, 0), return vmInternalError);

			ERROR_IF(_("getting comm state"), rsidx,
					 !GetCommState(rsi->rsh, &rsi->oldrsattrs),
					 return vmInternalError);

			rsi->rsattrs = rsi->oldrsattrs;
			dcb = &rsi->rsattrs;

			dcb->fOutX = dcb->fInX = 0;	// no XON/XOFF
			dcb->fErrorChar = dcb->fNull = 0;	// no messing with me!
			dcb->fRtsControl = RTS_CONTROL_TOGGLE;
			dcb->fAbortOnError = 1;	// must use ClearCommError() to fix
			dcb->fOutxCtsFlow = 0;	// don't suspend
			dcb->fOutxDsrFlow = 0;	// don't suspend
			dcb->fDtrControl = DTR_CONTROL_ENABLE;
			dcb->fDsrSensitivity = 0;	// don't ignore stuff if DSR is low
			dcb->fBinary = 1;	// of course...

			ERROR_IF(_("setting comm state"), rsidx,
					 !SetCommState(rsi->rsh, dcb), return vmInternalError);

			events =
				EV_BREAK | EV_CTS | EV_DSR | EV_ERR | EV_RXCHAR | EV_TXEMPTY;
			ERROR_IF(_("setting comm mask"), rsidx,
					 !SetCommMask(rsi->rsh, events), return vmInternalError);

			// no timeouts at all
			timeouts.ReadIntervalTimeout = MAXDWORD;
			timeouts.ReadTotalTimeoutMultiplier = 0;
			timeouts.ReadTotalTimeoutConstant = 0;
			timeouts.WriteTotalTimeoutMultiplier = 0;
			timeouts.WriteTotalTimeoutConstant = 0;
			ERROR_IF(_("setting timeouts"), rsidx,
					 !SetCommTimeouts(rsi->rsh, &timeouts),
					 return vmInternalError);

			ERROR_IF(_("creating RS232 mutex"), rsidx, !(rsi->mutex = CreateMutex(NULL	/* security */
																			   , FALSE	/* owner */
																			   , NULL	/* name */
													  )),
					 return vmInternalError);

			rsi->interrupt_thread =
				CreateThread(NULL, 16384, My_RS232_Interrupt, (LPVOID) rsidx, 0	/* run */
							 , &rs232_interrupt_thread_ID);

			ERROR_IF(_("creating RS232 monitor thread"), rsidx,
					 rsi->interrupt_thread == INVALID_HANDLE_VALUE,
					 return vmInternalError);
		}
	}


	return vmOk;
}

/*
 *	Disable RS232 use:  close files and ports
 */
int
Disable_RS232_SysDeps(void)
{
	int         idx;


	for (idx = 0; idx < MAX_RS232_DEVS; idx++) {
		struct RSInfo *rsi = &rsinfo[idx];

		if (rsi->interrupt_thread)
		{
			CloseHandle(rsi->interrupt_thread);
			rsi->interrupt_thread = NULL;
		}
		if (RS_IS_TTY(idx)) {
			SetCommState(rsi->rsh, &rsi->oldrsattrs);
		}
		if (rsi->rsh) CloseHandle(rsi->rsh);
		if (rsi->mutex) CloseHandle(rsi->mutex);
	}
	return vmOk;
}

int
Term_RS232_SysDeps(void)
{
	return vmOk;
}

/**********************************************************/

static void
ClearError(rs232regs * rs, u32 * flags)
{
	DWORD       err;
	COMSTAT     stat;

	ClearCommError(RSH(rs), &err, &stat);

	*flags &= ~(RS_RCVERR | RS_RPER | RS_ROVER | RS_RFER);
	if (err & CE_FRAME)
		*flags |= RS_RFER;
	if (err & CE_RXPARITY)
		*flags |= RS_RPER;
	if (err & CE_RXOVER)
		*flags |= RS_ROVER;
	if (err & (CE_IOE | CE_MODE))
		*flags |= RS_RCVERR;

//  rlog2("... data exception gives %08X", *flags);

	//  Check for remaining chars.
	if (stat.cbInQue > 0) {
		*flags |= RS_RBRL;
		if ((rs->wrport & RS_RIENB) && !(*flags & RS_RBINT)) {
			*flags |= RS_RBINT | RS_INT;
		}
	}

	if (stat.cbOutQue == 0) {
		*flags |= RS_XBRE | RS_XSRE;
		if ((rs->wrport & RS_XBIENB) && !(*flags & RS_XBINT)) {
			*flags |= RS_XBINT | RS_INT;
		}
	}

}

/*	Interrupt thread	*/

/*
    This routine actually executes throughout the lifetime of 
	the emulator, setting the RS232 status bits.
	When RS232 interrupts are enabled, it also triggers those.

	This routine sets some of the CRU bits triggered by EV_xxxx 
	in the rsinfo[].flags field since this thread executes 
	simultaneously with the emulator.  If, for example, this routine 
	sees a new character and sets RS_RBRL while the RS232
	interrupt routine is reading the old one, the info will be lost
	since the RS232 handler acknowledges the interrupt by setting bit
	18, which turns off RS_RBRL.  When the next character arrives, this
	routine will NOT be called, since Win32 "knows" we have a character
	waiting and have not read it (since the flag is off).  Since Win32 
	operates with toggles, for some ungodly reason, we cannot lose this 
	information.
*/
static DWORD WINAPI
My_RS232_Interrupt(LPVOID arg)
{
	int         rsidx = (int) arg;
	struct RSInfo *rsi = &rsinfo[rsidx];
	rs232regs  *rs = &rsregs[rsidx];
	bool        interruptible;

//  over.hEvent = CreateEvent(NULL, /* no security attributes */
//      FALSE, /* auto reset event */
//      FALSE, /* not signaled */
//      NULL /* no name */
//      );

//  rs->rdport |= RS_DSR | RS_XBRE | RS_XSRE;   

	rsi->rdflags = RS_DSR | RS_XBRE | RS_XSRE;
//  GetCommModemStatus(rsi->rsh, &rsi->modem);

	while (1) {
		DWORD       events, modem, oldflags;

		interruptible = false;


		WaitCommEvent(rsi->rsh, &events, NULL);
		module_logger(&realRS232DSR, _L|L_1, "[.Inside My_RS232_Interrupt\n");


		WaitForSingleObject(rsi->mutex, INFINITE);
//      rsi->events = dummy;
		module_logger(&realRS232DSR, _L|L_1, "Inside My_RS232_Interrupt.]\n");

		oldflags = rsi->rdflags;
		ClearError(rs, &rsi->rdflags);
		if ((rsi->rdflags /*^ oldflags */ ) &
			(RS_RBINT | RS_DSCINT | RS_TIMINT | RS_XBINT))
			interruptible = true;

#if 0
		// stuff to read?
		if (events & EV_RXCHAR) {
			module_logger(&realRS232, _L|L_1, _("... interrupt: data ready for reading\n"));
			rsi->rdflags |= RS_RBRL;
//          rs->rdport |= RS_RBRL;
			if ((rs->wrport & RS_RIENB) && !(rs->rdport & RS_RBINT)) {
				rsi->rdflags |= RS_RBINT | RS_INT;
				interruptible = true;
			}
		}
		// space to write?
		if (events & EV_TXEMPTY) {
			module_logger(&realRS232, _L|L_1, _("... data ready to write\n"));
			rsi->rdflags |= RS_XBRE | RS_XSRE;
//          rs->rdport |= RS_XBRE | RS_XSRE; 
			if ((rs->wrport & RS_XBIENB) && !(rs->rdport & RS_XBINT)) {
				rsi->rdflags |= RS_XBINT | RS_INT;
				interruptible = true;
			}
		}
		// problems?
		if (events & EV_ERR) {
			module_logger(&realRS232, _L|L_1, _("... data exception\n"));
			ClearError(rs, &rsi->rdflags);
		}
#endif
		// line changes?
		if (events & (EV_CTS | EV_DSR | EV_RLSD)) {
			module_logger(&realRS232DSR, _L|L_1, _("... line change\n"));
			GetCommModemStatus(rsi->rsh, &modem);
			rsi->rdflags |= ((modem & MS_RLSD_ON) ? RS_RTS : 0) |	// !!!???
				((modem & MS_CTS_ON) ? RS_CTS : 0) |
				((modem & MS_DSR_ON) ? RS_DSR : 0);
			if ((rs->wrport & RS_DSCENB) && !(rs->rdport & RS_DSCINT)) {
				rsi->rdflags |= RS_DSCINT | RS_INT;
				interruptible = true;
			}
		}
//      rs->rdport &= ~(RS_DSR|RS_RTS|RS_CTS);
//      if (GetCommModemStatus(rsi->rsh, &rsi->modem)) {
//          module_logger(&realRS232, _L|L_1,"Comm modem status is %04X\n", rsi->modem);
//          rs->rdport |= ((rsi->modem & MS_RLSD_ON) ? RS_RTS : 0) |        // !!!???
//              ((rsi->modem & MS_CTS_ON) ? RS_CTS : 0) |
//              ((rsi->modem & MS_DSR_ON) ? RS_DSR : 0);
//      } else {
//          module_logger(&realRS232, _L|LOG_ERROR,"cannot read modem lines\n");
//      }

		if (interruptible) {
			module_logger(&realRS232DSR, _L|0, _("** Interrupt **\n"));
			trigger9901int(M_INT_EXT);
			//debugger_enable(true);
		}

		ReleaseMutex(rsi->mutex);
	}							/* while */
}

/**********************************************************/

static void
Commit_Changes(rs232regs * rs, DCB * dcb)
{
	module_logger(&realRS232DSR, _L|L_1,
		 _("Commit_Changes: BaudRate=%d, fParity=%d, ByteSize=%d, Parity=%d, StopBits=%d\n"),
		 dcb->BaudRate, dcb->fParity, dcb->ByteSize, dcb->Parity,
		 dcb->StopBits);

	ERROR_IF(_("committing RS232 state changes"), rs - rsregs,
			 !SetCommState(RSH(rs), dcb), 0);

	ERROR_IF(_("setting RTS"), rs - rsregs,
			 !EscapeCommFunction(RSH(rs),
								 (rs->wrport & RS_RTSON) ? SETRTS : CLRRTS),
			 0);

	ERROR_IF(_("setting break"), rs - rsregs,
			 !EscapeCommFunction(RSH(rs),
								 (rs->wrport & RS_BRKON) ? SETBREAK :
								 CLRBREAK), 0);
}

/*
 *	Set modem clocks
 */

static void
Set_Clock(rs232regs * rs, DCB * dcb)
{
	// Baud = 3 MHz / ((CLK4M ? 4 : 3) * 2 * xBAUD x 8*DIV8)
	u32         baud, div;
	u32         o_baud, i_baud;

	// input speed
	div = (((rs->ctrl & CTRL_CLK4M) ? 4 : 3) * 2
		   * (rs->rcvrate & RATE_MASK) * ((rs->rcvrate & RATE_DIV8) ? 8 : 1));
	if (div == 0)
		i_baud = 50;
	else
		i_baud = (baseclockhz / div);

	// output speed
	div = (((rs->ctrl & CTRL_CLK4M) ? 4 : 3) * 2
		   * (rs->xmitrate & RATE_MASK) *
		   ((rs->xmitrate & RATE_DIV8) ? 8 : 1));
	if (div == 0)
		o_baud = 50;
	else
		o_baud = (baseclockhz / div);

	if (i_baud > o_baud)
		baud = i_baud;
	else
		baud = o_baud;

	dcb->BaudRate = baud;
	module_logger(&realRS232DSR, _L|0, _("Baud rate:  %d\n"), baud);
}

//  Use this macro to define a pointer to the attrs for the port,
//  or return immediately if we're talking to a file.
//
#define SETUP_TTY_ATTRS(dcb, rs)	DCB *dcb = &rsinfo[rs - rsregs].rsattrs; if (!RS_IS_TTY(rs - rsregs)) return
#define GET_TTY_MUTEX(rs)			WaitForSingleObject(rsinfo[rs-rsregs].mutex, 1)
#define RELEASE_TTY_MUTEX(rs) 		ReleaseMutex(rsinfo[rs-rsregs].mutex)

void
Reset_RS232_SysDeps(rs232regs * rs)
{
	struct RSInfo *rsi = &rsinfo[rs - rsregs];

	SETUP_TTY_ATTRS(dcb, rs);
	GET_TTY_MUTEX(rs);

	rsi->rdflags = RS_DSR | RS_XBRE | RS_XSRE;
	Set_Clock(rs, dcb);


	SetCommState(RSH(rs), dcb);	// ignore error
	RELEASE_TTY_MUTEX(rs);

}

void
Set_CTRL_Register(rs232regs * rs)
{
	SETUP_TTY_ATTRS(dcb, rs);
	GET_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|L_1, _("Setting CTRL register to %04X\n"), rs->ctrl);

	dcb->fParity = !!(rs->ctrl & CTRL_Penb);
	dcb->Parity =
		dcb->fParity ? ((rs->ctrl & CTRL_Podd) ? ODDPARITY : EVENPARITY) :
		NOPARITY;
	dcb->ByteSize = (rs->ctrl & (CTRL_RCL0 + CTRL_RCL1)) + 5;
	// TI declares a 1.5 and a 1 stop bit code, which we coalesce into 1
	if (dcb->ByteSize == 5 && !(rs->ctrl & (CTRL_SBS1 | CTRL_SBS2)))
		dcb->StopBits = ONE5STOPBITS;
	else
		dcb->StopBits = (!(rs->ctrl & CTRL_SBS1)
						 && (rs->ctrl & CTRL_SBS2) ? 2 : 0);

	// clock multiplier
	Set_Clock(rs, dcb);

	RELEASE_TTY_MUTEX(rs);

	Commit_Changes(rs, dcb);
}

void
Set_INVL_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting INVL register to %04X\n"), rs->invl);
}

void
Set_RCVRATE_Register(rs232regs * rs)
{
	SETUP_TTY_ATTRS(dcb, rs);
	GET_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|L_1, _("Setting RCVRATE register to %04X\n"), rs->rcvrate);

	Set_Clock(rs, dcb);

	RELEASE_TTY_MUTEX(rs);

	Commit_Changes(rs, dcb);
}

void
Set_XMITRATE_Register(rs232regs * rs)
{
	SETUP_TTY_ATTRS(dcb, rs);
	GET_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|L_1, _("Setting XMITRATE register to %04X\n"), rs->xmitrate);

	Set_Clock(rs, dcb);

	RELEASE_TTY_MUTEX(rs);

	Commit_Changes(rs, dcb);
}

void
Set_Control_Bits(rs232regs * rs, u32 old, int bit)
{
	struct RSInfo *rsi = &rsinfo[rs - rsregs];

	SETUP_TTY_ATTRS(dcb, rs);
	GET_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|L_1, _("Setting control bits to %04X\n"), rs->wrport);

	//  RTS or BREAK?
	if ((rs->wrport ^ old) & (RS_RTSON | RS_BRKON))
		Commit_Changes(rs, dcb);

	//  Interrupts?

	//  Writing a bit turns off its status bit
	if (bit & (RS_RIENB | RS_TIMENB | RS_DSCENB | RS_XBIENB)) {

/*		if ((bit & RS_RIENB) && !(rsi->events & EV_RXCHAR)) 	
			rs->rdport &= ~(RS_RBRL | RS_RBINT);
		if ((bit & RS_TIMENB) && !0)							
			rs->rdport &= ~(RS_TIMELP | RS_TIMINT);
		if ((bit & RS_DSCENB) && !(rsi->events & (EV_CTS|EV_DSR|EV_RLSD))) 	
			rs->rdport &= ~(RS_DSCH | RS_DSCINT);
//		if (bit & RS_XBIENB)	rs->rdport &= ~RS_XBRE;
		if (!(rs->rdport & (RS_RBINT|RS_TIMINT|RS_DSCINT)))
			reset9901int(M_INT_EXT);*/

		u32         oldflags;

		if (bit & RS_RIENB)
			rsi->rdflags &= ~(RS_RBRL | RS_RBINT);
		if (bit & RS_TIMENB)
			rsi->rdflags &= ~(RS_TIMELP | RS_TIMINT);
		if (bit & RS_DSCENB)
			rsi->rdflags &= ~(RS_DSCH | RS_DSCINT);
		if (bit & RS_XBIENB)
			rsi->rdflags &= ~RS_XBINT;

		oldflags = rsi->rdflags;
		ClearError(rs, &rsi->rdflags);
		if (!
			((rsi->rdflags ^ oldflags) & (RS_RBINT | RS_TIMINT | RS_DSCINT |
										  RS_XBINT))) reset9901int(M_INT_EXT);
		else {
			rsi->rdflags |= RS_INT;
			module_logger(&realRS232DSR, _L|0, _("*- Interrupt -*\n"));
			trigger9901int(M_INT_EXT);
		}

		/*  rsi->rdflags = (rsi->rdflags & ~(RS_RBRL|RS_TIMELP|RS_DSCH|
		   RS_RBINT|RS_TIMINT|RS_DSCINT)) | 
		   (rs->rdport & (RS_RBRL|RS_TIMELP|RS_DSCH|
		   RS_RBINT|RS_TIMINT|RS_DSCINT)); */
	}
	RELEASE_TTY_MUTEX(rs);

}

void
Transmit_Char(rs232regs * rs)
{
	u8          dat;
	DWORD       written;

	module_logger(&realRS232DSR, _L|0, _("Transmitting char %02X (%c)\n"), rs->txchar, rs->txchar);

	GET_TTY_MUTEX(rs);

	dat = rs->txchar;
	rs->rdport &= ~(RS_XBRE | RS_XSRE | RS_TIMERR);

	if (!WriteFile(RSH(rs), &dat, 1, &written, 0L) || written == 0) {
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER, _("RS232/%d write error (%s)\n"),
			 rs - rsregs, OS_GetErrText(GetLastError()));
//      rs->rdport |= RS_TIMERR;
	}
//  FlushFileBuffers(RSH(rs));
	ClearError(rs, &rs->rdport);
//  rs->rdport |= RS_XBRE | RS_XSRE;

	RELEASE_TTY_MUTEX(rs);

}

void
Read_Status_Bits(rs232regs * rs)
{
	struct RSInfo *rsi = &rsinfo[rs - rsregs];

	GET_TTY_MUTEX(rs);

	//  Flags are cached by interrupt routine
	rs->rdport &= ~0xff;
	rs->rdport |= (rsi->rdflags & ~0xff);

	RELEASE_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|L_2, _("Reading status bits as %08X\n"), rs->rdport);
}

void
Receive_Data(rs232regs * rs)
{
	u8          dat;
	u32         read;

	GET_TTY_MUTEX(rs);

	if (!ReadFile(RSH(rs), &dat, 1, &read, 0L)) {
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER, _("RS232/%d read error (%s)\n"),
			 rs - rsregs, OS_GetErrText(GetLastError()));
	} else {
		rs->rdport = (rs->rdport & ~0xff) | dat;
	}
	ClearError(rs, &rs->rdport);
	RELEASE_TTY_MUTEX(rs);

	module_logger(&realRS232DSR, _L|0, _("Receiving char %02X (%c)\n"), rs->rdport & 0xff, rs->rdport & 0xff);
}
