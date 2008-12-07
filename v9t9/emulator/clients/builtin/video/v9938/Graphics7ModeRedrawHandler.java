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
 * Redraw graphics 7 mode content (256x192x256)
 * <p>
 * Bitmapped mode where pattern table contains one pixel per byte in RGB 3-3-2 format.  
 * Every row is linear in memory and every row is adjacent to the next.  
 * @author ejs
 *
 */
public class Graphics7ModeRedrawHandler extends PackedBitmapGraphicsModeRedrawHandler {

	public Graphics7ModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		rowstride = 256;
		blockshift = 3;
		blockstride = 32;
		blockcount = (vdpregs[9] & 0x80) != 0 ? 32*27 : 768;		
	}
	
	protected void drawBlock(RedrawBlock block) {
		vdpCanvas.draw8x8BitmapRGB332ColorBlock(
				block.c,
				 block.r,
						vdpMemory.getByteReadMemoryAccess(
								vdpModeInfo.patt.base + rowstride * block.r + block.c + pageOffset), rowstride);
	}

	/** Backdrop isn't a normal color */
	public void clear() {
		vdpCanvas.clear(vdpCanvas.getGRB332((byte) vdpCanvas.getClearColor()));
	}
}
