/*
  MulticolorModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.video.tms9918a;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.RedrawBlock;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * @author ejs
 *
 */
public class MulticolorModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

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

		
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i >> 5) << 3;
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
}
