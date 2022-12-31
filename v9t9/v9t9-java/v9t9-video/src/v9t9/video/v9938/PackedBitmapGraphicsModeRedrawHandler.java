/*
  PackedBitmapGraphicsModeRedrawHandler.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;

import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.RedrawBlock;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeBlockRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.VdpTouchHandler;
import v9t9.video.common.VdpModeInfo;

/**
 * Redraw graphics 4, 5, 6 mode content
 * <p>
 * Bitmapped mode where pattern table contains some number of pixels per byte.  
 * Every row is linear in memory and every row is adjacent to the next.  
 * This is gonna be HARD!
 * @author ejs
 *
 */
public abstract class PackedBitmapGraphicsModeRedrawHandler extends BaseRedrawHandler 
	implements IVdpModeBlockRedrawHandler, IVdpModeRowRedrawHandler {

	// shift of pixels per row
	protected int rowstrideshift;
	protected int blockshift;
	// shift of blocks per row
	protected int blockstrideshift;
	protected int blockcount;
	protected int colshift;
	protected int pageOffset;	
	protected int pattAddrMask;
	
	/**
	 * Touches to the pattern area (as we consider it, though it's really the screen area
	 * per the V9938 registers) modify the screen bitmap, so we know what blocks
	 * to update.  
	 * 
	 * This will modify the physical block that was apparently modified,
	 * while #updateCanvas will adapt this to the {@link #pattAddrMask}.
	 */
	protected class ScreenBitmapTouchHandler implements VdpTouchHandler {
		int rowstridemask = ~(~0 << rowstrideshift);
		public void modify(int offs) {
			int row = (offs >> rowstrideshift) >> 3;
			int col = (offs & rowstridemask) >> blockshift;
			int addr = (row << blockstrideshift) + col;
			info.changes.screen.set(addr);
			info.changes.changed = true;
		}
		
	}
	
	public PackedBitmapGraphicsModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		init();
		// the mask is on the screen register but we treat these as pattern changes
		pattAddrMask = ((IVdpV9938) info.vdp).getPackedModeScreenAddrMask();
		info.touch.patt = new ScreenBitmapTouchHandler();
	}
		
	protected abstract void init();
	
	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRedrawHandler#getCharsPerRow()
	 */
	@Override
	public int getCharsPerRow() {
		return 1 << blockstrideshift;
	}
	
	@Override
	public boolean touch(int addr) {
		boolean visible = false;
		if (info.vdp.isInterlacedEvenOdd()) {
			int pageSize = info.vdp.getGraphicsPageSize();
			int pattBase = modeInfo.patt.base ^ pageSize;
			if (pattBase <= addr && addr < pattBase + modeInfo.patt.size) {
				// trigger our listener for the other page
	    		info.touch.patt.modify(addr - pattBase);
	    		visible = true;
	    	}
		}
			
		return super.touch(addr) | visible;
	}
	
	public void prepareUpdate() {
		// The pattAddrMask determines which address lines
		// are available -- this means changes to patterns might
		// actually affect multiple blocks (repeated) on screen.
		
		// the mask is on the screen register but we treat these as pattern changes
		pattAddrMask = ((IVdpV9938) info.vdp).getPackedModeScreenAddrMask();
		
		int minAddr = (info.canvas.getMinY() / 8) << blockstrideshift;
		int maxAddr = ((info.canvas.getMaxY() + 7) / 8) << blockstrideshift;
		
		int bpm = (pattAddrMask >> blockshift);
		for (int addr = minAddr; addr < maxAddr; addr++) {
//			int row = (addr / rowstride) >> 3;
//			int col = (addr % rowstride) >> blockshift;
//			int screenAddr = row * blockstride + col;
			if ((addr & bpm) != addr && info.changes.screen.get(addr & bpm)) {
				info.changes.screen.set(addr);
			}
		}
//		
//		
//		int row = (offs / rowstride) >> 3;
//		int col = (offs % rowstride) >> blockshift;
//		info.changes.screen.set(row * blockstride + col);
//		info.changes.changed = true;
		
	}
	
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw 8x8 blocks where pixels changed */
		IVdpV9938 vdp9938 = (IVdpV9938)info.vdp;
		boolean interlacedEvenOdd = vdp9938.isInterlacedEvenOdd();
		int graphicsPageSize = vdp9938.getGraphicsPageSize();
		
		int minY = info.canvas.getMinY();
		int maxY = info.canvas.getMaxY();
		
		int blockstridemask = ~(~0 << blockstrideshift);
		int count = 0;
		int screenSize = blockcount;
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < screenSize; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{
			RedrawBlock block = blocks[count++];
			
			block.r = (i >> blockstrideshift) << 3;
			if (block.r + 8 < minY || block.r >= maxY)
				continue;
			
			block.c = (i & blockstridemask) << 3;

			drawBlock(block.r, block.c, 0, false);
			
			// when interlacing, each row is technically twice as wide
			// and the interlaced rows are on the "right" side of the bitmap
			if (interlacedEvenOdd) {
				drawBlock(block.r, block.c, pageOffset ^ graphicsPageSize, true);
			}
		}
		return count;
	}

	abstract protected void drawBlock(int r, int c, int pageOffset, boolean interlaced);
	
	
	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvas(int, int)
	 */
	@Override
	public void updateCanvasRow(int row, int col) {
		/*  Redraw 8x8 blocks where pixels changed */
		IVdpV9938 vdp9938 = (IVdpV9938)info.vdp;
		boolean interlacedEvenOdd = vdp9938.isInterlacedEvenOdd();
		int graphicsPageSize = vdp9938.getGraphicsPageSize();
		
//		System.out.println("packed: " + prevScanline + " - " + currentScanline);
		
		int roffs = (row >> 3) << blockstrideshift;
		for (int c = 1 << blockstrideshift; --c >= 0; ) {
			int i = roffs + c;
			if (!info.changes.screen.get(i)) 
				continue;
			
			drawPixels(c * 8, row, 0, false);
			
			// when interlacing, each row is technically twice as wide
			// and the interlaced rows are on the "right" side of the bitmap
			if (interlacedEvenOdd) {
				drawPixels(c * 8, row, pageOffset ^ graphicsPageSize, true);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvasBlock(int, int, int)
	 */
	@Override
	public void updateCanvasBlock(int screenOffs, int col, int row) {
		IVdpV9938 vdp9938 = (IVdpV9938)info.vdp;
		boolean interlacedEvenOdd = vdp9938.isInterlacedEvenOdd();
		int graphicsPageSize = vdp9938.getGraphicsPageSize();
		
		drawBlock(row, col, 0, false);
		
		// when interlacing, each row is technically twice as wide
		// and the interlaced rows are on the "right" side of the bitmap
		if (interlacedEvenOdd) {
			drawBlock(row, col, pageOffset ^ graphicsPageSize, true);
		}
	}
	
	abstract protected void drawPixels(int x, int y, int pageOffset, boolean interlaced);

	/**
	 * @param pageOffset the pageOffset to set
	 */
	public void setPageOffset(int pageOffset) {
		this.pageOffset = pageOffset;
	}

}
