/*
  Graphics5ModeRedrawHandler.java

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
 * Redraw graphics 5 mode content (512x192x4)
 * <p>
 * Bitmapped mode where pattern table contains 4 pixels per byte.  
 * The backdrop is rendered in even-odd stripes.
 *
 * @author ejs
 *
 */
public class Graphics5ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics5ModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 128;
		blockshift = 1;		// byte 2 -> block 1
		blockstride = 64;
		blockcount = (info.vdpregs[9] & 0x80) != 0 ? 64*27 : 1536;
		
		colshift = 2; 
	}
	
	protected void drawBlock(RedrawBlock block, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapFourColorBlock(
				block.c + (interlaced ? 512 : 0), block.r,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base 
					+ rowstride * block.r + (block.c >> 2)) ^ pageOffset),
			rowstride);
	}
	
	@Override
	public void clear() {
		info.canvas.clearToEvenOddClearColors();
	}

}
