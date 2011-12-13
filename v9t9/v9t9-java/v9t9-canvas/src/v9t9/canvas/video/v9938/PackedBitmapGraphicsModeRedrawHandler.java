/**
 * 
 */
package v9t9.canvas.video.v9938;

import v9t9.canvas.video.BaseRedrawHandler;
import v9t9.canvas.video.IVdpModeRedrawHandler;
import v9t9.canvas.video.VdpRedrawInfo;
import v9t9.canvas.video.VdpTouchHandler;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpModeInfo;

/**
 * Redraw graphics 4, 5, 6 mode content
 * <p>
 * Bitmapped mode where pattern table contains some number of pixels per byte.  
 * Every row is linear in memory and every row is adjacent to the next.  
 * This is gonna be HARD!
 * @author ejs
 *
 */
public abstract class PackedBitmapGraphicsModeRedrawHandler extends BaseRedrawHandler implements IVdpModeRedrawHandler {

	protected int rowstride;
	protected int blockshift;
	protected int blockstride;
	protected int blockcount;
	protected int colshift;	
	
	protected class ScreenBitmapTouchHandler implements VdpTouchHandler {
		public void modify(int offs) {
			int row = (offs / rowstride) >> 3;
			int col = (offs % rowstride) >> blockshift;
			info.changes.screen[row * blockstride + col] = 1;
			info.changes.changed = true;
		}
		
	}
	
	public PackedBitmapGraphicsModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		init();
		info.touch.patt = new ScreenBitmapTouchHandler();
	}
		
	protected abstract void init();
	
	@Override
	public boolean touch(int addr) {
		boolean visible = false;
		if (info.vdp.isInterlacedEvenOdd()) {
			int pageSize = info.vdp.getGraphicsPageSize();
			int pattBase = modeInfo.patt.base ^ pageSize;
			if (pattBase <= addr && addr < pattBase + modeInfo.patt.size) {
	    		info.touch.patt.modify(addr - pattBase);
	    		visible = true;
	    	}
		}
			
		return super.touch(addr) | visible;
	}
	public void prepareUpdate() {
		// we directly detect screen changes already
	}
	
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw 8x8 blocks where pixels changed */
		IVdpV9938 vdp9938 = (IVdpV9938)info.vdp;
		int pageOffset = vdp9938.getGraphicsPageOffset();
		boolean interlacedEvenOdd = vdp9938.isInterlacedEvenOdd();
		int graphicsPageSize = vdp9938.getGraphicsPageSize();
		
		//System.out.println(pageOffset);
		int count = 0;
		int screenSize = blockcount;
		for (int i = 0; i < screenSize; i++) {
			byte changes = info.changes.screen[i];
			if (force || changes != 0) {		
				RedrawBlock block = blocks[count++];
				
				block.r = (i / blockstride) << 3;
				block.c = (i % blockstride) << 3;

				drawBlock(block, 0, false);
				
				// when interlacing, each row is technically twice as wide
				// and the interlaced rows are on the "right" side of the bitmap
				if (interlacedEvenOdd) {
					drawBlock(block, pageOffset ^ graphicsPageSize, true);
				}
					
			}
		}
		return count;
	}

	abstract protected void drawBlock(RedrawBlock block, int pageOffset, boolean interlaced);


}
