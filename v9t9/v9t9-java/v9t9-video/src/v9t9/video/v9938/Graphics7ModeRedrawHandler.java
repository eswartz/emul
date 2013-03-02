/*
  Graphics7ModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

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
package v9t9.video.v9938;

import v9t9.common.video.RedrawBlock;
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
	
	protected void drawBlock(RedrawBlock block, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapRGB332ColorBlock(
				block.c + (interlaced ? 256 : 0), block.r,
			 info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * block.r + block.c) ^ pageOffset),
			rowstride);
	}

	/** Backdrop isn't a normal color */
	public void clear() {
		byte [] rgb = { 0, 0, 0 };
		info.canvas.getColorMgr().getGRB332(rgb, (byte) info.canvas.getColorMgr().getClearColor());
		info.canvas.clear();
	}
	
}
