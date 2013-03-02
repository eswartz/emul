/*
  VdpTMS9918AConsts.java

  (c) 2011 Edward Swartz

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
package v9t9.common.hardware;


/**
 * @author ejs
 *
 */
public class VdpTMS9918AConsts {
	protected VdpTMS9918AConsts() { }
	
	public final static int VDP_INTERRUPT = 0x80;
	public final static int VDP_COINC = 0x40;
	public final static int VDP_FIVE_SPRITES = 0x20;
	public final static int VDP_FIFTH_SPRITE = 0x1f;
	public final static int R0_M3 = 0x2; // bitmap
	public final static int R0_EXTERNAL = 1;
	public final static int R1_RAMSIZE = 128;
	public final static int R1_NOBLANK = 64;
	public final static int R1_INT = 32;
	public final static int R1_M1 = 0x10; // text
	public final static int R1_M2 = 0x8; // multi
	public final static int R1_SPR4 = 2;
	public final static int R1_SPRMAG = 1;
	
	public final static int MODE_GRAPHICS = 0;
	public final static int MODE_TEXT = 1;
	public final static int MODE_MULTI = 2;
	public final static int MODE_BITMAP = 4;
	
	public final static int REG_ST = -1;

}
