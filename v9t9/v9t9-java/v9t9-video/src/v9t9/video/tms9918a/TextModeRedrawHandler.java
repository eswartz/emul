/*
  TextModeRedrawHandler.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.tms9918a;

import v9t9.common.video.RedrawBlock;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeBlockRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * @author ejs
 *
 */
public class TextModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeBlockRedrawHandler, IVdpModeRowRedrawHandler {

	public TextModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		info.touch.patt = modify_patt_default;
		info.touch.sprite = info.touch.sprpat = null;
		info.touch.screen = modify_screen_default;
		info.touch.color = null;
	}

	public void prepareUpdate() {
		propagatePatternTouches();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		
		byte fg, bg;
		
		bg = (byte) (info.vdpregs[7] & 0xf);
		fg = (byte) ((info.vdpregs[7] >> 4) & 0xf);

		int minY = info.canvas.getMinY();
		int maxY = info.canvas.getMaxY();
		
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{

			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i / 40) << 3;	
			if (block.r + 8 < minY || block.r >= maxY)
				continue;
			
			block.c = (i % 40) * 6 + (256 - 240) / 2;

			int pattOffs = pattBase + (currchar << 3);
			info.canvas.draw8x6TwoColorBlock(block.r, block.c, 
					info.vdp.getByteReadMemoryAccess(pattOffs), fg, bg);
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRedrawHandler#getCharsPerRow()
	 */
	@Override
	public int getCharsPerRow() {
		return 40;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvas(int, int)
	 */
	@Override
	public void updateCanvasRow(int row, int col) {
//		System.out.println("text: " + prevScanline + " - " + currentScanline);
		int screenBase = modeInfo.screen.base;
		
		byte fg, bg;
		
		bg = (byte) (info.vdpregs[7] & 0xf);
		fg = (byte) ((info.vdpregs[7] >> 4) & 0xf);
		
		int centerOffs = (256 - 240) / 2;
		int roffs = (row >> 3) * 40;
		boolean anyOnRow = false;
		for (int c = 0; c < 40; c++) {
			int i = roffs + c;
			if (!info.changes.screen.get(i)) 
				continue;
			
			if (!anyOnRow) {
				// draw backdrop
				anyOnRow = true;
				info.canvas.drawEightPixels(
						info.canvas.getBitmapOffset(0, row),
						(byte) 0x00, 
						fg, bg);
				info.canvas.drawEightPixels(
						info.canvas.getBitmapOffset(centerOffs + 240, row),
						(byte) 0x00, 
						fg, bg);
			}
			
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;
			
			int offs = info.canvas.getBitmapOffset(c * 6 + centerOffs, row);
			int pattAddr = (row & 7) + (modeInfo.patt.base + (currchar << 3));
			info.canvas.drawSixPixels(
					offs, info.vdp.readAbsoluteVdpMemory(pattAddr), 
					fg, bg);
		}
	}
	

	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvasBlockRow(int)
	 */
	@Override
	public void updateCanvasBlock(int screenOffs, int col, int row) {
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		
		byte fg, bg;
		
		bg = (byte) (info.vdpregs[7] & 0xf);
		fg = (byte) ((info.vdpregs[7] >> 4) & 0xf);
		
//		int centerOffs = (256 - 240) / 2;
//		
//		// draw backdrop
//		if (col == 0)
//			info.canvas.draw8x8TwoColorBlock(row, 0, solidBlockPattern, bg, bg);
//		if (col == 240 - 6)
//			info.canvas.draw8x8TwoColorBlock(row, centerOffs + 240, solidBlockPattern, bg, bg);
//		
		int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + screenOffs) & 0xff;
		
		int pattOffs = pattBase + (currchar << 3);
		info.canvas.draw8x6TwoColorBlock(row, col, 
				info.vdp.getByteReadMemoryAccess(pattOffs), fg, bg);
	}
}
