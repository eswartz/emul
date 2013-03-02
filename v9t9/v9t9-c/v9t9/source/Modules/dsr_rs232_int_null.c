/*
  dsr_rs232_int_null.c			-- V9t9 module backend for null RS232

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
 *	Internal RS232 handler.
 *
 */

#include "v9t9_common.h"
#include "dsr_rs232.h"

#define _L 	LOG_RS232|LOG_INFO

int         
Init_RS232_SysDeps(void)
{
	return vmOk;
}

int
Enable_RS232_SysDeps(void)
{
	return vmOk;
}

int
Disable_RS232_SysDeps(void)
{
	return vmOk;
}

int
Term_RS232_SysDeps(void)
{
	return vmOk;
}

void
Reset_RS232_SysDeps(rs232regs * rs)
{
}


void
Set_CTRL_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting CTRL register to %04X\n"), rs->ctrl);
}

void
Set_INVL_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting INVL register to %04X\n"), rs->invl);
}

void
Set_RCVRATE_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting RCVRATE register to %04X\n"), rs->rcvrate);
}

void
Set_XMITRATE_Register(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting XMITRATE register to %04X\n"), rs->xmitrate);

}

void
Set_Control_Bits(rs232regs * rs, u32 old, int bit)
{
	module_logger(&realRS232DSR, _L|L_1, _("Setting control bits to %04X\n"), rs->wrport);
}

void
Transmit_Char(rs232regs * rs)
{
	module_logger(&realRS232DSR, _L|L_1, _("Transmitting char %02X (%c)\n"), rs->txchar, rs->txchar);
}

void
Read_Status_Bits(rs232regs * rs)
{
	rs->rdport |= RS_RTS | RS_DSR | RS_CTS | RS_XBRE;
	module_logger(&realRS232DSR, _L|L_2, _("Reading status bits as %08X\n"), rs->rdport);
}

void
Receive_Data(rs232regs * rs)
{
	rs->rdport = (rs->rdport & ~0xff) | '*';
	module_logger(&realRS232DSR, _L|0, _("Receiving char %02X (%c)\n"), rs->rdport, rs->rdport);
}
