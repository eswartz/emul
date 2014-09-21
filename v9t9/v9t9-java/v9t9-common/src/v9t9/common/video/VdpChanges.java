/*
  VdpChanges.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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

//	public BitSet	touchedRows;
	
	public VdpChanges(int maxRedrawblocks) {
		screen = new BitSet(maxRedrawblocks);
//		touchedRows = new BitSet(512);
	}
}