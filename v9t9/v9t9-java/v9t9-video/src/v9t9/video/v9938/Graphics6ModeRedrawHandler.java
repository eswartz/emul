/*
  Graphics6ModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;

import v9t9.common.video.RedrawBlock;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * Redraw graphics 6 mode content (512x192x16)
 * <p>
 * Bitmapped mode where pattern table contains 2 pixels per byte.  Every row
 * is linear in memory and every row is adjacent to the next.  This is gonna be HARD!
 * @author ejs
 *
 */
public class Graphics6ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics6ModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

	}

	@Override
	protected void init() {
		rowstride = 256;
		blockshift = 2;
		blockstride = 64;
		blockcount = (info.vdpregs[9] & 0x80) != 0 ? 64*27 : 1536;
		colshift = 1;
	}
	
	protected void drawBlock(RedrawBlock block, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapTwoColorBlock(
				block.c + (interlaced ? 512 : 0), block.r,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * block.r + (block.c >> 1)) ^ pageOffset),
			rowstride);
	}
}
