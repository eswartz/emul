/*
  InstTableCommon.java

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
package v9t9.common.asm;

/**
 * @author Ed
 *
 */
public class InstTableCommon {

	public static final int Idata = 0;
	public static final int Idsr = -2;
	public static final int Iticks = -3;
	public static final int Iemitchar = -4;
	public static final int Idbg = -5;
	public static final int Idbgf = -6;
	public static final int Ikysl = -7;
	public static final int Ibyte = -1;
	public static final int Idelete = -999;	// noop
	/** user instructions */
	public static final int Iuser = 100;

}
