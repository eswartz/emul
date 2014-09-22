/*
  Graphics7ModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;

import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * Redraw graphics 7 mode content (256x192x256)
 * <p>
 * Bitmapped mode where pattern table contains one pixel per byte in RGB 3-3-2 format.  
 * Every row is linear in memory and every row is adjacent to the next.  
 * @author ejs
 *
 */
public class Graphics7ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

	public Graphics7ModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 256;
		blockshift = 3;
		blockstride = 32;
		blockcount = (info.vdpregs[9] & 0x80) != 0 ? 32*27 : 768;
		colshift = 0;
	}
	
	protected void drawBlock(int r, int c, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapRGB332ColorBlock(
				c + (interlaced ? 256 : 0), r,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * r + c) ^ pageOffset),
			rowstride);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.v9938.PackedBitmapGraphicsModeRedrawHandler#drawPixels(int, int, int, boolean)
	 */
	@Override
	protected void drawPixels(int x, int y, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapRGB332ColorByte(
				x + (interlaced ? 256 : 0), y,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * y + x) ^ pageOffset));		
	}

	/** Backdrop isn't a normal color */
	public void clear() {
		byte [] rgb = { 0, 0, 0 };
		info.canvas.getColorMgr().getGRB332(rgb, (byte) info.canvas.getColorMgr().getClearColor());
		info.canvas.clear();
	}
	
}
