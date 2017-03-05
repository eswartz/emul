/*
  Graphics4ModeRedrawHandler.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;

import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * Redraw graphics 4 mode content (256x192x16)
 * <p>
 * Bitmapped mode where pattern table contains 2 pixels per byte.  Every row
 * is linear in memory and every row is adjacent to the next.  This is gonna be HARD!
 * @author ejs
 *
 */
public class Graphics4ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics4ModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 128;
		blockshift = 2;
		blockstride = 32;
		blockcount = (info.vdpregs[9] & 0x80) != 0 ? 32*27 : 768;
		colshift = 1;
	}
	
	protected void drawBlock(int r, int c, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapTwoColorBlock(
			c + (interlaced ? 256 : 0), 
			r,
			info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * r + (c >> 1)) ^ pageOffset),
			rowstride);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.v9938.PackedBitmapGraphicsModeRedrawHandler#drawPixels(int, int, int, boolean)
	 */
	@Override
	protected void drawPixels(int x, int y, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapTwoColorByte(
				x + (interlaced ? 256 : 0), y, 
				info.vdp.getByteReadMemoryAccess(
						(modeInfo.patt.base + rowstride * y + (x >> 1)) ^ pageOffset));		
	}

}
