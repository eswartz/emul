/*
  MulticolorModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.tms9918a;

import v9t9.common.memory.ByteMemoryAccess;
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
public class MulticolorModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeBlockRedrawHandler, IVdpModeRowRedrawHandler {

	public MulticolorModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
		info.touch.screen = modify_screen_default;
		info.touch.color = null;
		info.touch.patt = modify_patt_default;
	}

	static final byte stockMultiBlockPattern[] = { 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0, 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0 
	};
	
	static final ByteMemoryAccess multiBlockPattern = 
		new ByteMemoryAccess(stockMultiBlockPattern, 0);

	public void prepareUpdate() {
		propagatePatternTouches();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.info.vdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;

		int minY = info.canvas.getMinY();
		int maxY = info.canvas.getMaxY();
		
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i >> 5) << 3;
			if (block.r + 8 < minY || block.r >= maxY)
				continue;

			block.c = (i & 31) << 3;

			int pattOffs = pattBase + (currchar << 3) + ((i >> 5) & 3) * 2;
			
			byte mem1 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs);
			byte mem2 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs + 1);
			
			byte[] colors = { mem1, mem1, mem1, mem1, mem2, mem2, mem2, mem2 }; 

			info.canvas.draw8x8MultiColorBlock(block.r, block.c, 
					multiBlockPattern,
					new ByteMemoryAccess(colors, 0));
		}

		return count;
	}


	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#getCharsPerRow()
	 */
	@Override
	public int getCharsPerRow() {
		return 32;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvas(int, int)
	 */
	@Override
	public void updateCanvasRow(int row, int col) {
//		System.out.println("multi: " + prevScanline + " - " + currentScanline);
		
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		
		int roffs = (row >> 3) << 5;
		for (int c = 0; c < 32; c++) {
			int i = roffs + c;
			if (!info.changes.screen.get(i)) 
				continue;
			
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */
			int pattOffs = pattBase + (currchar << 3) + ((i >> 5) & 3) * 2;
			
			byte mem = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs + ((row & 7) >= 4 ? 1 : 0));
			
			int offs = info.canvas.getBitmapOffset(c * 8, row);
			info.canvas.drawEightPixels(
					offs, (byte) 0xf0, 
					(byte) ((mem >> 4) & 0xf), (byte) (mem & 0xf));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IVdpModeRowRedrawHandler#updateCanvasBlockRow(int)
	 */
	@Override
	public void updateCanvasBlock(int screenOffs, int col, int row) {

		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		
		byte[] colors = new byte[8]; 
		int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + screenOffs) & 0xff;	/* char # to update */

		int pattOffs = pattBase + (currchar << 3) + ((screenOffs >> 5) & 3) * 2;
		
		byte mem1 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs);
		byte mem2 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs + 1);
		
		colors[0] = colors[1] = colors[2] = colors[3] = mem1;
		colors[4] = colors[5] = colors[6] = colors[7] = mem2;
		info.canvas.draw8x8MultiColorBlock(row, col, 
				multiBlockPattern,
				new ByteMemoryAccess(colors, 0));

	}
}
