/**
 * 
 */
package v9t9.engine.video.v9938;

import v9t9.engine.video.IBitmapPixelAccess;
import v9t9.engine.video.RedrawBlock;
import v9t9.engine.video.VdpModeInfo;
import v9t9.engine.video.VdpRedrawInfo;

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

	@Override
	protected byte createImageDataByte(IBitmapPixelAccess access, int x, int y) {
		byte f = access.getPixel(x, y);
		byte b = access.getPixel(x + 1, y);
		
		return (byte) ((f << 4) | b);
	}
}
