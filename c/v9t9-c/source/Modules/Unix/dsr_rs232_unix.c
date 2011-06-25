 
/*
  dsr_rs232_unix.c				-- V9t9 module backend for RS232 DSR module

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

#include <stdlib.h>
#include <unistd.h>
//#include <termios.h>
#include <linux/termios.h>
#include <sys/stat.h>
#include <sys/fcntl.h>
#include <sys/time.h>

#include "v9t9_common.h"
#include "emulate.h"
#include "timer.h"
#include "9901.h"
#include "dsr_rs232.h"

#define _L	LOG_RS232|LOG_INFO
#define _LD	_L|LOG_DEBUG

typedef struct RSDevice {
	// file descriptor
	int		handle;

	// true: is a TTY or serial port, false: is a file
	int		is_tty;

	// old attributes
	struct termios oldrsattrs;

	// new attributes
	struct termios rsattrs;
}	RSDevice;

static RSDevice devices[MAX_RS232_DEVS];

//  Open handles for serial port
//static int  rsh[MAX_RS232_DEVS];

#define	RS_TO_DEVICE(rs)	(devices[(rs)-rsregs])
#define	IDX_TO_DEVICE(idx)	(devices[idx])
#define	RSH(rs)				(RS_TO_DEVICE(rs).handle)

//  Last available port; remaining ones are files
static int  rsmax;

#define	RS_IS_TTY(idx)		(IDX_TO_DEVICE(idx).handle > 0 && IDX_TO_DEVICE(idx).is_tty)

//  Old attributes for ports
//static struct termios oldrsattrs[MAX_RS232_DEVS];

//  New attributes
//static struct termios rsattrs[MAX_RS232_DEVS];

#if !BUFFERED_RS232
static int  rs232_interrupt_tag;
#endif

/*
 *	Initialize system dependencies on RS232.
 *
 *	Assign up to MAX_RS232_DEVS devices from /dev/ttySxx,
 *	use files for remaining RS232 devices.
 */

int         
Init_RS232_SysDeps(void)
{
	return vmOk;
}

//	This can be at most 32
#define MAX_TTY	16

/*
 *	Find a TTY for the RS232 device.
 *	Return 0 iff no TTYs are available.
 */
static int 
Find_RS232_Device(int rs, int *ttys)
{
	int 		tty = 0;
	int         h;
	int         first_try;
	int			user_defined;

	user_defined = first_try && *rs232name[rs];

	if (!user_defined) {
		// look for available TTY
		while (tty < MAX_TTY) {
			if (!(*ttys & (1 << tty))) {
				sprintf(rs232name[rs], "/dev/ttyS%d", tty);
				h = open(rs232name[rs], O_RDWR | O_NONBLOCK);
				if (h >= 0) {
					close(h);

					module_logger(&realRS232DSR, _L|LOG_INFO|LOG_USER, 
								  _("RS232/%d is %s\n"), rs+1,
								  rs232name[rs]);
					*ttys |= 1 << tty;
					break;
				}
			}
			tty++;
		}
		if (tty >= MAX_TTY) {
			module_logger(&realRS232DSR, _L|LOG_ERROR|LOG_USER, 
						  _("RS232/%d cannot be assigned to a device\n"), rs+1);
			*rs232name[rs] = 0;
			return 0;
		}
	} else {
		h = open(rs232name[rs], O_RDWR | O_NONBLOCK);
		if (h >= 0) {
			close(h);

			module_logger(&realRS232DSR, _L|LOG_INFO|LOG_USER, 
						  _("RS232/%d is %s\n"), rs+1,
						  rs232name[rs]);
		} else {
			module_logger(&realRS232DSR, _L|LOG_ERROR|LOG_USER, 
						  _("RS232/%d cannot be used (%s)\n"), rs+1,
						  OS_GetErrText(errno));
		}
	}
	return 1;
}

static int
My_Init_RS232_SysDeps(void)
{
	int         rsidx;
	int			ttys = 0;

	memset(&devices, 0, sizeof(devices));

	rsmax = 0;
	while (rsmax < MAX_RS232_DEVS) {
		if (!Find_RS232_Device(rsmax, &ttys))
			break;
		rsmax++;
	}

	for (rsidx = rsmax; rsidx < MAX_RS232_DEVS; rsidx++) {
		if (*rs232name[rsidx])
			continue;
		sprintf(rs232name[rsidx], "serial%d.txt", rsidx);
		module_logger(&realRS232DSR, _L|LOG_INFO|LOG_USER, _("RS232/%d is %s\n"), rsidx + 1,
			 rs232name[rsidx]);
	}

	if (rsmax == 0) {
		module_logger(&realRS232DSR, _L|LOG_ERROR|LOG_USER,
			 _("Linux-RS232: could not assign any serial ports\n"));
		return vmNotAvailable;
	} else if (rsmax < MAX_RS232_DEVS) {
		module_logger(&realRS232DSR, _L|LOG_USER, _("could not assign %d serial ports, "
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
	vmResult    res;

	res = My_Init_RS232_SysDeps();
	if (res != vmOk)
		return res;

	for (rsidx = 0; rsidx < MAX_RS232_DEVS; rsidx++) {
		RSDevice *device = &IDX_TO_DEVICE(rsidx);
		if (strstr(rs232name[rsidx], "/dev/ttyS") != NULL) {
			device->handle = open(rs232name[rsidx], O_RDWR | O_NONBLOCK);
			device->is_tty = 1;
		} else {
			device->handle =
				open(rs232name[rsidx], O_RDWR | O_CREAT | O_TRUNC, 0666);
			device->is_tty = 0;
		}

		if (device->handle < 0) {
			module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER, _("Could not open %s\n"), rs232name[rsidx]);
			// assume user set the name
			*rs232name[rsidx] = 0;
			continue;
		}

		// only set attrs on real ports
		if (device->is_tty) {
			struct termios *ti;

			tcgetattr(device->handle, &device->oldrsattrs);

			device->rsattrs = device->oldrsattrs;
			ti = &device->rsattrs;

			// no control chars
			memset(ti->c_cc, 0, sizeof(ti->c_cc));
			// make a "raw" device
			ti->c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP
							 | INLCR | IGNCR | ICRNL | IXON);
			ti->c_oflag &= ~OPOST;
			ti->c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
			ti->c_cflag &= ~(CSIZE | PARENB);
			ti->c_cflag |= CS8 | CRTSCTS;

			tcsetattr(device->handle, TCSAFLUSH, ti);
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
		RSDevice *device = &IDX_TO_DEVICE(idx);
		if (device->handle > 0) {
			if (device->is_tty) {
				tcsetattr(device->handle, TCSADRAIN, &device->oldrsattrs);
			}
			close(device->handle);
		}
	}
	return vmOk;
}

int
Term_RS232_SysDeps(void)
{
	return vmOk;
}

/**********************************************************/

#if !BUFFERED_RS232

/*	Interrupt routine	*/

/*
    This routine executes throughout the lifetime of 
	the emulator, setting the RS232 status bits.
	When interrupts are enabled, it also triggers those.
	This interrupt only sets bits; reading them will reset them.
	
	NOTE: if this routine is made into a thread, we must
	move a cached copy of the status bits somewhere, since
	the RS232 interrupt handler can kill the status bits with
	the interrupt acknowledge.
*/
static void
My_RS232_Interrupt(void)
{
	rs232regs  *rs;
	fd_set      rd, wr, ex;
	int         maxfd;
	struct timeval tim;
	bool        interruptible = false;

	FD_ZERO(&rd);
	maxfd = 0;
	for (rs = &rsregs[0]; rs < &rsregs[MAX_RS232_DEVS]; rs++) {
		RSDevice *device = &RS_TO_DEVICE(rs);
		if (device->handle > 0 && device->is_tty) {
			FD_SET(device->handle, &rd);
			maxfd = device->handle + 1;
		}
	}
	wr = rd;
	ex = rd;
	tim.tv_sec = 0;
	tim.tv_usec = 0;

	if (select(maxfd, &rd, &wr, &ex, &tim) < 0) {
		//  nothing
		module_logger(&realRS232DSR, _L|LOG_ERROR,
			 _("failure in select() in My_RS232_Interrupt\n"));
	} else {
		for (rs = &rsregs[0]; rs < &rsregs[MAX_RS232_DEVS]; rs++) {
			int         modem;
			RSDevice 	*device = &RS_TO_DEVICE(rs);

			if (device->handle <= 0 || !device->is_tty)
				continue;

			rs->rdport &= ~RS_RBRL;

			// stuff to read?
			if (FD_ISSET(device->handle, &rd)) {
				module_logger(&realRS232DSR, _L|L_2, _("... data ready for reading\n"));
				rs->rdport |= RS_RBRL;
				//  rs->rdport |= RS_DSR;
				if ((rs->wrport & RS_RIENB) && !(rs->rdport & RS_RBINT)) {
					rs->rdport |= RS_RBINT | RS_INT;
					interruptible = true;
				}
			}

			rs->rdport &= ~(RS_XBRE | RS_XSRE);

			// space to write?
			if (FD_ISSET(device->handle, &wr)) {
				module_logger(&realRS232DSR, _L|L_2, _("... data ready to write\n"));
				rs->rdport |= RS_XBRE | RS_XSRE;
				//  rs->rdport |= RS_RTS | RS_CTS;
				if ((rs->wrport & RS_XBIENB) && !(rs->rdport & RS_XBINT)) {
					rs->rdport |= RS_XBINT | RS_INT;
					interruptible = true;
				}
			}
			// problems?
			if (FD_ISSET(device->handle, &ex)) {
				module_logger(&realRS232DSR, _L|L_2, _("... data exception\n"));
				rs->rdport |= RS_RCVERR | RS_RPER | RS_ROVER | RS_RFER;
			}

			rs->rdport &= ~(RS_DSR | RS_RTS | RS_CTS);
			if (ioctl(device->handle, TIOCMGET, &modem) == 0) {
				rs->rdport |= ((modem & TIOCM_RTS) ? RS_RTS : 0) |
					((modem & TIOCM_CTS) ? RS_CTS : 0) |
					((modem & TIOCM_DSR) ? RS_DSR : 0);
			} else {
				module_logger(&realRS232DSR, _L|L_2|LOG_ERROR,
					 _("cannot read modem lines\n"));
			}
		}
	}

	if (interruptible) {
		module_logger(&realRS232DSR, _L|L_2, _("** Interrupt **\n"));
		trigger9901int(M_INT_EXT);
//      debugger_enable(true);
	}
}

#else // BUFFERED_RS232

//  Try to read enough characters to ensure a
//  smooth data flow, typically representative of
//  the baud rate.
//
void
Receive_Chars(rs232regs * rs)
{
	int         most = BUF_SIZ - BUFFER_LEFT(rs, r), max, got, res;
	fd_set      fds;
	struct timeval tm;
	RSDevice	*device = &RS_TO_DEVICE(rs);

	if (BUFFER_FULL(rs, r))
		return;

	if (most > (rs->recvbps / 8))
		most = rs->recvbps / 8;

	if (most == 0)
		return;

	module_logger(&realRS232DSR, _LD|L_4, _("Trying to receive %d chars\n"), most);

	if (most > BUF_SIZ - rs->r_en)
		max = BUF_SIZ - rs->r_en;
	else
		max = most;

	module_logger(&realRS232DSR, _LD|L_3, _("most=%d, max=%d\n"), most, max);

	FD_ZERO(&fds);
	FD_SET(device->handle, &fds);
	tm.tv_sec = 0;
	tm.tv_usec = 0;

	if (select(device->handle + 1, &fds, 0L, 0L, &tm) > 0) {
		module_logger(&realRS232DSR, _LD|L_3, _("select passed, max = %d\n"), max);

		// read possibly to end of buffer...
		res = read(device->handle, rs->recv + rs->r_en, max);
		if (res > 0)
			got = res;
		else
			got = 0;

		// and then start from the beginning
		if (got != most) {
			max = most - max;
			res = read(device->handle, rs->recv, max);
			if (res > 0)
				got += res;
		}
		rs->r_en = (rs->r_en + got) & BUF_MASK;
	} else {
		got = 0;
	}

	module_logger(&realRS232DSR, _LD|(got ? L_1 : L_3), 
				  _("received %d chars of %d maximum\n"), got, most);
}

//  Try to send chars in the buffer
//  to the RS232.  To avoid taking too long,
//  only send as many characters as are representative
//  of the baud rate.
//
void
Transmit_Chars(rs232regs * rs)
{
	static int  prev;			// previous bytes sent
	int         most = BUFFER_LEFT(rs, t), sent, max, res;
	fd_set      fds;
	struct timeval tm;
	RSDevice	*device = &RS_TO_DEVICE(rs);

	// nothing to send?
	if (BUFFER_EMPTY(rs, t))
		return;

	if (most > (rs->xmitbps / 8))
		most = rs->xmitbps / 8;

	if (most + rs->t_st > BUF_SIZ)
		max = BUF_SIZ - rs->t_st;
	else
		max = most;

	FD_ZERO(&fds);
	FD_SET(device->handle, &fds);
	tm.tv_sec = 0;
	tm.tv_usec = 0;

	if (select(device->handle + 1, 0L, &fds, 0L, &tm) > 0) {
//  if (1) {
		// send possibly to end of buffer...
		res = write(device->handle, rs->xmit + rs->t_st, max);
		if (res > 0)
			sent = res;
		else
			sent = 0;

		// and then start from the beginning
		if (sent == max && max < most) {
			max = most - max;
			res = write(device->handle, rs->xmit, max);
			if (res > 0)
				sent += res;
		}
		rs->t_st = (rs->t_st + sent) & BUF_MASK;
	} else {
		sent = 0;
	}

	module_logger(&realRS232DSR, _LD|L_1, _("sent %d chars\n"), sent);
}

void
Update_Modem_Lines(rs232regs * rs)
{
	int         modem;

	if (!RS_IS_TTY(rs - rsregs))
		return;

	rs->rdport &= ~(RS_DSR | RS_RTS | RS_CTS);
	if (ioctl(RSH(rs), TIOCMGET, &modem) == 0) {
//		module_logger(&realRS232DSR, _LD|LOG_USER,_("modem = %x\n"), modem);
		rs->rdport |= ((modem & TIOCM_RTS) ? RS_RTS : 0) |
			((modem & TIOCM_CTS) ? RS_CTS : 0) |
			((modem & TIOCM_DSR) ? RS_DSR : 0);
	} else {
		module_logger(&realRS232DSR, _L|L_2|LOG_ERROR, _("cannot read modem lines\n"));
	}
}

#endif // BUFFERED_RS232



/**********************************************************/

static void
Change_RTS(rs232regs * rs)
{
	int         modem;

	if (ioctl(RSH(rs), TIOCMGET, &modem) == 0) {
		modem = (modem & ~TIOCM_RTS) |
			((rs->wrport & RS_RTSON) ? TIOCM_RTS : 0) | TIOCM_DSR;	// !!! an option may control this
		if (ioctl(RSH(rs), TIOCMSET, &modem) < 0) {
			module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
				 _("could not set RTS/DSR lines\n"));
		}
	}
}

static void
Commit_Changes(rs232regs * rs, struct termios *ti)
{
	unsigned int modem;

	if (tcsetattr(RSH(rs), TCSADRAIN, ti) < 0) {
		// this is a sign that the connection has the wrong rate
		//rs->rdport |= RS_RPER | RS_ROVER | RS_RFER;
		//rs->rdport &= ~(RS_RTS | RS_CTS | RS_DSR);
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
			 _("could not set attributes (%s)\n"), strerror(errno));
	}

}

/*
 *	Set modem clocks and RTS.
 *
 *	POSIX says that baud rate 0 means clear RTS.
 */
#define NEAR(x) ((x)+(x)/10)
#define RANGE(b,x) (((b) >= (x) - (x)/10) && ((b) < (x) + (x)/10))

static int
Map_Baud(int baud)
{
	if (baud == 0 || RANGE(baud, 50))
		return B50;
	else if (RANGE(baud, 75))
		return B75;
	else if (RANGE(baud, 110))
		return B110;
	else if (RANGE(baud, 134))
		return B134;
	else if (RANGE(baud, 150))
		return B150;
	else if (RANGE(baud, 200))
		return B200;
	else if (RANGE(baud, 300))
		return B300;
	else if (RANGE(baud, 600))
		return B600;
	else if (RANGE(baud, 1200))
		return B1200;
	else if (RANGE(baud, 1800))
		return B1800;
	else if (RANGE(baud, 2400))
		return B2400;
	else if (RANGE(baud, 4800))
		return B4800;
	else if (RANGE(baud, 9600))
		return B9600;
	else if (RANGE(baud, 19200))
		return B19200;
	else if (RANGE(baud, 38400))
		return B38400;
	else {
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
			 _("unsupported baud rate %d, using 50\n"), baud);
		return B50;
	} 
}

static void
Set_Clock(rs232regs * rs, struct termios *ti)
{
	if (cfsetispeed(ti, Map_Baud(rs->recvbps)) < 0)
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
			 _("could not set input baud rate of %d\n"),
			 rs->recvbps);
	else
		module_logger(&realRS232DSR, _LD|L_1, _("Input baud rate:  %d\n"), rs->recvbps);

	// output speed
	if (cfsetospeed(ti, Map_Baud(rs->xmitbps)) < 0)
		module_logger(&realRS232DSR, _L|LOG_ERROR | LOG_USER,
			 _("could not set output baud rate of %d\n"),
			 rs->xmitbps);
	else
		module_logger(&realRS232DSR, _LD|L_1, _("Output baud rate:  %d\n"), rs->xmitbps);
}

//  Use this macro to define a pointer to the attrs for the port,
//  or return immediately if we're talking to a file.
//
#define SETUP_TTY_ATTRS(ti, rs)	struct termios *ti = &RS_TO_DEVICE(rs).rsattrs; if (!RS_IS_TTY(rs - rsregs)) return

void
Reset_RS232_SysDeps(rs232regs * rs)
{
	SETUP_TTY_ATTRS(ti, rs);

	Set_Clock(rs, ti);
	tcsetattr(RSH(rs), TCSAFLUSH, ti);

#if !BUFFERED_RS232
	if (!rs232_interrupt_tag)
		rs232_interrupt_tag = TM_UniqueTag();
	TM_SetEvent(rs232_interrupt_tag, TM_HZ, 0, TM_FUNC | TM_REPEAT,
				My_RS232_Interrupt);
#endif
}

static int  CS_VALS[] = { CS5, CS6, CS7, CS8 };
void
Set_CTRL_Register(rs232regs * rs)
{
	struct termios oldti;

	SETUP_TTY_ATTRS(ti, rs);

	oldti = *ti;

	module_logger(&realRS232DSR, _L|L_1, _("Setting CTRL register to %04X\n"), rs->ctrl);

	// parity, stop bits, word size
	ti->c_cflag = (CS_VALS[(rs->ctrl & (CTRL_RCL0 + CTRL_RCL1))]) |
		((rs->ctrl & CTRL_Penb) ? PARENB : 0) |
		((rs->ctrl & CTRL_Podd) ? PARODD : 0) |
		// TI declares a 1.5 and a 1 stop bit code, which we coalesce into 1
		(!(rs->ctrl & CTRL_SBS1) && (rs->ctrl & CTRL_SBS2) ? CSTOPB : 0) |
		CRTSCTS | CREAD;

	// redundant parity selection?
	if (rs->ctrl & CTRL_Penb)
		ti->c_iflag |= INPCK;
	else
		ti->c_iflag &= ~INPCK;

	// clock multiplier
	Set_Clock(rs, ti);

	if (memcmp(&oldti, ti, sizeof(oldti)))
		Commit_Changes(rs, ti);
}

void
Set_INVL_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting INVL register to %04X\n"), rs->invl);
}

void
Set_RCVRATE_Register(rs232regs * rs)
{
	struct termios oldti;

	SETUP_TTY_ATTRS(ti, rs);

	oldti = *ti;
	module_logger(&realRS232DSR, _L|L_1, _("Setting RCVRATE register to %04X\n"), rs->rcvrate);

	Set_Clock(rs, ti);

	if (memcmp(&oldti, ti, sizeof(oldti)))
		Commit_Changes(rs, ti);
}

void
Set_XMITRATE_Register(rs232regs * rs)
{
	struct termios oldti;

	SETUP_TTY_ATTRS(ti, rs);

	oldti = *ti;
	module_logger(&realRS232DSR, _L|L_1, _("Setting XMITRATE register to %04X\n"), rs->xmitrate);

	Set_Clock(rs, ti);

	if (memcmp(&oldti, ti, sizeof(oldti)))
		Commit_Changes(rs, ti);
}

void
Set_Control_Bits(rs232regs * rs, u32 old, int bit)
{
	SETUP_TTY_ATTRS(ti, rs);

	module_logger(&realRS232DSR, _L|L_1, _("Setting control bits to %04X\n"), rs->wrport);

#if !BUFFERED_RS232
	//  RTS is handled
	if ((rs->wrport ^ old) & RS_RTSON)
		Change_RTS(rs);
#endif

	//  Break?  Only set at END of break.
	if ((old & RS_BRKON) && !(rs->wrport & RS_BRKON)) {
		tcsendbreak(RSH(rs), 0);
	}
#if !BUFFERED_RS232

	//  Interrupts?

	//  Writing a bit turns off its status bit
	if (bit & (RS_RIENB | RS_TIMENB | RS_DSCENB | RS_XBIENB)) {
		if (bit & RS_RIENB)
			rs->rdport &= ~RS_RBRL;
		if (bit & RS_TIMENB)
			rs->rdport &= ~RS_TIMELP;
		if (bit & RS_DSCENB)
			rs->rdport &= ~RS_DSCH;
//      if (bit & RS_XBIENB)    rs->rdport &= ~RS_XBRE;
		reset9901int(M_INT_EXT);
	}
#endif
}

#if !BUFFERED_RS232

void
Transmit_Char(rs232regs * rs)
{
	u8          dat;

	module_logger(&realRS232DSR, _L|L_1, _("Transmitting char %02X (%c)\n"), rs->txchar, rs->txchar);
	dat = rs->txchar;
	rs->rdport &= ~(RS_XBRE | RS_XSRE | RS_TIMERR);
	if (write(RSH(rs), &dat, 1) < 0) {
		module_logger(&realRS232DSR, _L|LOG_ERROR, _("RS232/%d write error\n"), rs - rsregs);
		rs->rdport |= RS_TIMERR;
	}
	rs->rdport |= RS_XBRE | RS_XSRE;
}

#endif

void
Read_Status_Bits(rs232regs * rs)
{
	//  The interrupt routine sets the status bits.

	module_logger(&realRS232DSR, _L|L_2, _("Reading status bits as %08X\n"), rs->rdport);
}

#if !BUFFERED_RS232

void
Receive_Data(rs232regs * rs)
{
	u8          dat;

	if (read(RSH(rs), &dat, 1) == 1) {
		rs->rdport = (rs->rdport & ~0xff) | dat;
	} else {
		rs->rdport |= RS_RCVERR | RS_RPER | RS_ROVER | RS_RFER;
	}
	module_logger(&realRS232DSR, _L|L_1, _("Receiving char %02X (%c)\n"), rs->rdport & 0xff, rs->rdport & 0xff);
}

#endif
