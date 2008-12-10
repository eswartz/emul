/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.engine.VdpHandler;

/**
 * Redraw graphics 4 mode content (256x192x16)
 * <p>
 * Bitmapped mode where pattern table contains 2 pixels per byte.  Every row
 * is linear in memory and every row is adjacent to the next.  This is gonna be HARD!
 * @author ejs
 *
 */
public class Graphics4ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics4ModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 128;
		blockshift = 2;
		blockstride = 32;
		blockcount = (vdpregs[9] & 0x80) != 0 ? 32*27 : 768;
	}
	
	protected void drawBlock(RedrawBlock block) {
		vdpCanvas.draw8x8BitmapTwoColorBlock(
				block.c,
				 block.r,
						vdp.getByteReadMemoryAccess(
								vdpModeInfo.patt.base + rowstride * block.r + (block.c >> 1) + pageOffset), rowstride);
	}



}
