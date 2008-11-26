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
 * Redraw graphics 4, 5, 6 mode content
 * <p>
 * Bitmapped mode where pattern table contains some number of pixels per byte.  
 * Every row is linear in memory and every row is adjacent to the next.  
 * This is gonna be HARD!
 * @author ejs
 *
 */
public abstract class PackedBitmapGraphicsModeRedrawHandler extends BaseRedrawHandler implements VdpModeRedrawHandler {

	protected int rowstride;
	protected int blockshift;
	protected int blockstride;
	protected int blockcount;
	protected class ScreenBitmapTouchHandler implements VdpTouchHandler {
		public void modify(int offs) {
			int row = (offs / rowstride) >> 3;
			int col = (offs % rowstride) >> blockshift;
			vdpChanges.screen[row * blockstride + col] = vdpchanged = 1;
		}
		
	}
	
	public PackedBitmapGraphicsModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
		init();
		vdpTouchBlock.patt = new ScreenBitmapTouchHandler();
	}
		
	protected abstract void init();
	
	public void propagateTouches() {
	}
	
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw 8x8 blocks where pixels changed */
		int count = 0;
		int screenSize = blockcount;
		for (int i = 0; i < screenSize; i++) {
			byte changes = vdpChanges.screen[i];
			if (force || changes != 0) {			/* this screen pos updated? */
				RedrawBlock block = blocks[count++];
				
				block.r = (i / blockstride) << 3;	/* for graphics mode */
				block.c = (i % blockstride) << 3;

				drawBlock(block);
			}
		}
		return count;
	}

	abstract protected void drawBlock(RedrawBlock block);

}
