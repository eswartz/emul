/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpRedrawInfo;
import v9t9.engine.memory.ByteMemoryAccess;

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


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.BaseRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData(IBitmapPixelAccess access) {
		ByteMemoryAccess patt = info.vdp.getByteReadMemoryAccess(modeInfo.patt.base);
		
		int my =  (info.vdpregs[9] & 0x80) != 0 ? 212 : 192;
		for (int y = 0; y < my; y++) {
			for (int x = 0; x < 512; x += 4) {
				
				byte p = 0;
				for (int xo = 0; xo < 4; xo++) {
					byte c = access.getPixel(x + xo, y);
					p |= c << ((3 - xo) * 2);
				}
				
				int poffs = y * rowstride + (x >> 2); 
				patt.memory[patt.offset + poffs] = p;
				touch(patt.offset + poffs);
			}
		}
		
	}
}
