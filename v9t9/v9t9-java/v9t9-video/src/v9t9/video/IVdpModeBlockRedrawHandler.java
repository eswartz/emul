/*
  IVdpModeRedrawHandler.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import v9t9.common.video.RedrawBlock;

/**
 * The redraw handler manages the efficient update of the
 * VDP canvas ("background") given changes made to the VDP
 * memory.  
 * 
 * Any modifying write to VDP memory will be either
 * to one or more mapped areas (screen image table,
 * pattern table, sprite table, etc) or will be invisible
 * (i.e. not mapped to any memory that participates in drawing,
 * or e.g. in simpler graphics modes, affecting a pattern for 
 * which no character is in the screen image table).
 * 
 * The implementor of this interface then is responsible
 * for tracking what has changed on screen.  Effectively
 * this comes down to what screen blocks must be redrawn.
 * These may be directly obvious or may be propagated through
 * other changes (e.g. to the pattern table).
 * @author Ed
 *
 */
public interface IVdpModeBlockRedrawHandler extends IVdpModeRedrawHandler {
	/** 
	 * Given the touched memory, redraw the bitmap,
	 * generate the redraw blocks representing the changed bits of the bitmap,
	 * and return the number of blocks updated.
	 * @param blocks array of at most 1024 blocks, filled in by implementor
	 * @return number of blocks changed
	 */
	int updateCanvas(RedrawBlock[] blocks);

	/**
	 * Clear the canvas
	 */
	void clear();

}