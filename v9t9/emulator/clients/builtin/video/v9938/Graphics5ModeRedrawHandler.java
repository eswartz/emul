/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandlerBlock;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Redraw graphics 5 mode content (512x192x4)
 * <p>
 * Bitmapped mode where pattern table contains 4 pixels per byte.  Every row
 * is linear in memory and every row is adjacent to the next.  This is gonna be HARD!
 * @author ejs
 *
 */
public class Graphics5ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

		
	public Graphics5ModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 128;
		blockshift = 1;		// byte 2 -> block 1
		blockstride = 64;
		blockcount = (vdpregs[9] & 0x80) != 0 ? 64*27 : 1536;
	}
	
	protected void drawBlock(RedrawBlock block) {
		vdpCanvas.draw8x8BitmapFourColorBlock(
				vdpCanvas.getBitmapOffset(block.c, block.r),
				 vdpMemory.getByteReadMemoryAccess(
						vdpModeInfo.patt.base + rowstride * block.r + (block.c >> 2)),
						rowstride);
	}

}
