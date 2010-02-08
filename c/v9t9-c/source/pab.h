/*
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

#ifndef __PAB_H__
#define __PAB_H__

/*
	Defines to handle the PAB (peripheral access block) by 
	emulated device DSRS.  This is based on the 99/4A way of doing
	things.
	
	The PAB holds information sent to the DSR describing the
	device, filename, record, operation, etc., used in an
	I/O request.
	
	PABs are stored in the VDP RAM, and the emulated device code accesses
	the VDP RAM directly.  We must synchronize the VDP with the changes we
	make to it, so that the screen will be updated as necessary.
*/

/*	File operations (byte 0, opcode)*/
enum
{
	f_open,
	f_close,
	f_read,
	f_write,
	f_seek,
	f_load,
	f_save,
	f_delete,
	f_scratch,
	f_status
};

/*	File open codes (byte 1, pflags) */
enum
{
	m_openmode = 3 << 1,
	m_update = 0 << 1,
	m_output = 1 << 1,
	m_input = 2 << 1,
	m_append = 3 << 1
};

/*	Flags set for types of file access (byte 1, pflags)
	(0x80 never really used) */
enum
{
	fp_program = 0x80,
	fp_variable = 0x10,
	fp_internal = 0x8,
	fp_relative = 0x1
};

/*	Error codes (byte 1, pflags)*/
enum
{
	m_error	= 0x7 << 5,

	e_baddevice = 0x0,
	e_readonly = 1 << 5,
	e_badopenmode = 2 << 5,
	e_illegal = 3 << 5,
	e_outofspace = 4 << 5,
	e_endoffile	= 5 << 5,
	e_hardwarefailure = 6 << 5,
	e_badfiletype = 7 << 5
};

/*	Status codes */
enum
{
	st_filenotfound = 0x80,
	st_readonly = 0x40,
	st_internal = 0x10,
	st_program = 8,
	st_variable = 4,
	st_diskfull = 2,
	st_eof = 1
};


/*	This maps directly onto the VDP */
#define PABREC_SIZE 10

typedef struct
{
	u8	opcode;		/* file operation (f_xxx) */
	u8	pflags;		/* file access code (fp_xxx) + open mode (m_xxx) [IN]*/
					/* error code [OUT] */
	u16	addr;		/* VDP record address */
	u8	preclen;	/* file record length */
	u8	charcount;	/* characters used in record */
	u16	recnum;		/* current record (seek position) */
	u8	scrnoffs;	/* screen offset (for CSx) */
	u8	namelen;	/* length of filename following */
	u8	name[0];	/* filename */
}	pabrec;

//extern	pabrec	*pabaddr;	/* VDP location of current PAB */

/*
  Set error directly to PAB in VDP; this allows short-circuit
  subroutine exit without storing PAB back to VDP.
 */

#define VDP_PTR(x)      FLAT_MEMORY_PTR(md_video, (x)&0x3fff)
#define VDP_ADDR(p)     ((p) - VDP_PTR(0))


static void
pab_set_vdp_error(u8 *fnptr, u8 paberror)
{
	u16 addr = VDP_ADDR(fnptr) - 9 + 1;
	u8 byt = domain_read_byte(md_video, addr);
	domain_write_byte(md_video, addr, (byt & ~m_error) | (paberror & m_error));
}

#endif
