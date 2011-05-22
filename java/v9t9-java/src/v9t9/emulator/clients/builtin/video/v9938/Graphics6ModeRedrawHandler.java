/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.IVdpPixelAccess;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Redraw graphics 6 mode content (512x192x16)
 * <p>
 * Bitmapped mode where pattern table contains 2 pixels per byte.  Every row
 * is linear in memory and every row is adjacent to the next.  This is gonna be HARD!
 * @author ejs
 *
 */
public class Graphics6ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics6ModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 256;
		blockshift = 2;
		blockstride = 64;
		blockcount = (vdpregs[9] & 0x80) != 0 ? 64*27 : 1536;		
	}
	
	protected void drawBlock(RedrawBlock block, int pageOffset, int interlaceOffset) {
		int rowOffs = interlaceOffset / vdpCanvas.getLineStride();			
		vdpCanvas.draw8x8BitmapTwoColorBlock(
				block.c, block.r + rowOffs,
			 vdp.getByteReadMemoryAccess(
					(vdpModeInfo.patt.base + rowstride * block.r + (block.c >> 1)) ^ pageOffset),
			rowstride);
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.BaseRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData(IVdpPixelAccess access) {
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdpModeInfo.patt.base);
		
		int my =  (vdpregs[9] & 0x80) != 0 ? 212 : 192;
		for (int y = 0; y < my; y++) {
			for (int x = 0; x < 512; x += 2) {
				
				byte f = access.getPixel(x, y);
				byte b = access.getPixel(x + 1, y);
				
				int poffs = y * rowstride + (x >> 1); 
				patt.memory[patt.offset + poffs] = (byte) ((f << 4) | b);
				touch(patt.offset + poffs);
			}
		}
		
	}
}
