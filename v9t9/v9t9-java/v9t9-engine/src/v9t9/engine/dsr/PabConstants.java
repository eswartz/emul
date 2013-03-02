/*
  PabConstants.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.engine.dsr;

/**
 * @author ejs
 *
 */
public class PabConstants {
	private PabConstants() { }
	
	/*	Error codes (byte 1, pflags)*/
	public final static int
		e_baddevice = 0 << 5,
		e_readonly = 1 << 5,
		e_badopenmode = 2 << 5,
		e_illegal = 3 << 5,
		e_outofspace = 4 << 5,
		e_endoffile	= 5 << 5,
		e_hardwarefailure = 6 << 5,
		e_badfiletype = 7 << 5
	;
	public final static int e_pab_mask = 0x7 << 5;
	

	/*	File open codes (byte 1, pflags) */
	public final static int
		m_update = 0 << 1,
		m_output = 1 << 1,
		m_input = 2 << 1,
		m_append = 3 << 1
	;
	public final static int m_mode_mask = 3 << 1;

	/*	Flags set for types of file access (byte 1, pflags)
		(0x80 never really used) */
	public final static int
		fp_program = 0x80,
		fp_variable = 0x10,
		fp_internal = 0x8,
		fp_relative = 0x1
		;
	public final static int fp_access_mask = 0x99;
	
	public static final int op_load = 5;
	public static final int op_save = 6;
	public static final int op_open = 0;
	public static final int op_write = 3;
	public static final int op_read = 2;
	public static final int op_close = 1;
	public static final int op_restore = 4;
	public static final int op_scratch = 8;
	public static final int op_status = 9;
	public static final int op_delete = 7;
	
	public final static int 
		st_noexist = 0x80,
		st_protected = 0x40,
		st_reserved20 = 0x20,
		st_internal = 0x10,
		st_program = 0x08,
		st_variable = 0x04,
		st_endofspace = 0x02,
		st_endoffile = 0x01
		;
}
