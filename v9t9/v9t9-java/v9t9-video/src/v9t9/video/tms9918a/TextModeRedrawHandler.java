/*
  TextModeRedrawHandler.java

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

import v9t9.common.video.RedrawBlock;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;

/**
 * @author ejs
 *
 */
public class TextModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

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

		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{

			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i / 40) << 3;	
			block.c = (i % 40) * 6 + (256 - 240) / 2;

			int pattOffs = pattBase + (currchar << 3);
			info.canvas.draw8x6TwoColorBlock(block.r, block.c, 
					info.vdp.getByteReadMemoryAccess(pattOffs), fg, bg);
		}

		return count;
	}

}
