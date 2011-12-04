/**
 * 
 */
package v9t9.engine.video.v9938;

import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpModeInfo;
import v9t9.engine.video.IBitmapPixelAccess;
import v9t9.engine.video.VdpRedrawInfo;

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
	
	@Override
	protected byte createImageDataByte(IBitmapPixelAccess access, int x, int y) {
		return access.getPixel(x, y);
	}
}
