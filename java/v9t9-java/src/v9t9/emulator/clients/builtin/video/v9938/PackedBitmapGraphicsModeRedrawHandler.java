/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.VdpHandler;

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
			vdpChanges.screen[row * blockstride + col] = 1;
			vdpChanges.changed = true;
		}
		
	}
	
	public PackedBitmapGraphicsModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
		init();
		vdpTouchBlock.patt = new ScreenBitmapTouchHandler();
	}
		
	protected abstract void init();
	
	@Override
	public boolean touch(int addr) {
		boolean visible = false;
		if (((VdpV9938)vdp).isInterlacedEvenOdd()) {
			int pageSize = ((VdpV9938) vdp).getGraphicsPageSize();
			int pattBase = vdpModeInfo.patt.base ^ pageSize;
			if (pattBase <= addr && addr < pattBase + vdpModeInfo.patt.size) {
	    		vdpTouchBlock.patt.modify(addr - pattBase);
	    		visible = true;
	    	}
		}
			
		return super.touch(addr) | visible;
	}
	public void propagateTouches() {
		// we directly detect screen changes already
	}
	
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw 8x8 blocks where pixels changed */
		VdpV9938 vdpV9938 = (VdpV9938)vdp;
		int pageOffset = vdpV9938.getGraphicsPageOffset();
		boolean interlacedEvenOdd = vdpV9938.isInterlacedEvenOdd();
		int graphicsPageSize = vdpV9938.getGraphicsPageSize();
		
		// for interlacing?
		int halfRowStride = vdpCanvas.getLineStride() / 2;
		int rowStrideOffset = 0; // pageOffset != 0 ? halfRowStride : 0;
		
		//System.out.println(pageOffset);
		int count = 0;
		int screenSize = blockcount;
		for (int i = 0; i < screenSize; i++) {
			byte changes = vdpChanges.screen[i];
			if (force || changes != 0) {		
				RedrawBlock block = blocks[count++];
				
				block.r = (i / blockstride) << 3;
				block.c = (i % blockstride) << 3;

				drawBlock(block, 0, rowStrideOffset);
				if (interlacedEvenOdd) {
					drawBlock(block, pageOffset ^ graphicsPageSize, rowStrideOffset ^ halfRowStride);
				}
					
			}
		}
		return count;
	}

	abstract protected void drawBlock(RedrawBlock block, int pageOffset, int interlaceOffset);

}
