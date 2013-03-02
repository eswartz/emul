/*
  VdpChanges.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.common.video;

import java.util.BitSet;

public class VdpChanges
{
	// if anything changed in the tables
	public boolean 	changed;
	// if set, just redraw it all
	public boolean	fullRedraw;
	
	/** Tell if the block at the given screen offset was changed 
	 */
	public BitSet	screen;					// 1: block changed
	/** Tell if the pattern (8x8) at the given table offset was changed */
	public byte	patt[] = new byte[768];			// 1: pattern changed
	/** Tell if the color (8x8) at the given table offset was changed */
	public byte	color[] = new byte[768];			// 1: color changed
	/** Tell if the sprite (bitmask 1<<#) was changed */
	public int 	sprite;				// (1<<x): sprite #x changed
	/** Tell if the sprite pattern (8x8) was changed */
	public byte	sprpat[] = new byte[256];		// 1: sprite pattern changed
	
	public VdpChanges(int maxRedrawblocks) {
		screen = new BitSet(maxRedrawblocks);
	}
}