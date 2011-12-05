/**
 * 
 */
package v9t9.engine.video.v9938;

import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpModeInfo;
import v9t9.engine.video.VdpRedrawInfo;

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
	
	protected void drawBlock(RedrawBlock block, int pageOffset, boolean interlaced) {
		info.canvas.draw8x8BitmapTwoColorBlock(
			block.c + (interlaced ? 256 : 0), 
			block.r,
			info.vdp.getByteReadMemoryAccess(
					(modeInfo.patt.base + rowstride * block.r + (block.c >> 1)) ^ pageOffset),
			rowstride);
	}


}
