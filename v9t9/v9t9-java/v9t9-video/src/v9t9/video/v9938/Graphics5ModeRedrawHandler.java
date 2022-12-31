/*
  Graphics5ModeRedrawHandler.java

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
		rowstrideshift = 7; // 128
		blockshift = 1;		// byte 2 -> block 1
		blockstrideshift = 6;
		blockcount = (info.vdpregs[9] & 0x80) != 0 ? 64*27 : 1536;
		
		colshift = 2; 
	}
	
	protected void drawBlock(int r, int c, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapFourColorBlock(
				c + (interlaced ? 512 : 0), r,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base 
					+ ((r << rowstrideshift) + (c >> 2)) & pattAddrMask)  ^ pageOffset),
			1 << rowstrideshift);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.v9938.PackedBitmapGraphicsModeRedrawHandler#drawPixels(int, int, int, boolean)
	 */
	@Override
	protected void drawPixels(int x, int y, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapFourColorByte(
				x + (interlaced ? 512 : 0), y,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base 
					+ ((y << rowstrideshift) + (x >> 2)) & pattAddrMask) ^ pageOffset));		
	}
	
	@Override
	public void clear() {
		info.canvas.clearToEvenOddClearColors();
	}

}
