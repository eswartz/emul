/*
  BitmapModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.tms9918a;

import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.video.RedrawBlock;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.VdpTouchHandler;
import v9t9.video.common.VdpModeInfo;

/**
 * @author ejs
 *
 */
public class BitmapModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			info.changes.color[offs >> 3] = 1;
			info.changes.changed = true;
		}
		
	};
	protected VdpTouchHandler modify_patt_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			info.changes.patt[offs >> 3] = 1;
			info.changes.changed = true;
		}
		
	};
	private int bitcolormask;
	private int bitpattmask;

	public BitmapModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		info.touch.screen = modify_screen_default;
		info.touch.color = modify_color_bitmap;
		info.touch.patt = modify_patt_bitmap;

		/*
		bitcolormask = (short) ((((short) (info.vdpregs[3] & 0x7f)) << 6) | 0x3f);

		// thanks, Thierry!
		// in "bitmap text" mode, the full pattern table is always addressed,
		// otherwise, the color bits are used in the pattern masking
		if ((info.vdpregs[1] & 0x10) != 0)
			bitpattmask = (short) ((((short) (info.vdpregs[4] & 0x03) << 11)) | 0x7ff);
		else
			bitpattmask =
				(short) ((((short) (info.vdpregs[4] & 0x03) << 11)) | (bitcolormask & 0x7ff));
		 */
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void prepareUpdate() {
		/*  Set pattern or color changes in chars */

		bitcolormask = ((IVdpTMS9918A) info.vdp).getBitmapModeColorMask();
		bitpattmask = ((IVdpTMS9918A) info.vdp).getBitmapModePatternMask();

		int bpm = bitpattmask >> 3;
		int bcm = bitcolormask >> 3;
		
		int minY = (info.canvas.getMinY() / 8) * 32;
		int maxY = ((info.canvas.getMaxY() + 7) / 8) * 32;
		
		for (int i = minY; i < maxY; i++) {
			int sector = (i & 0x300);
			int currchar = info.vdp.readAbsoluteVdpMemory(modeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (info.changes.patt[(currchar + sector) & bpm] != 0
					|| info.changes.color[(currchar + sector) & bcm] != 0) { /* if color or pattern changed */
				info.changes.screen.set(i);	/* then this char changed */
			}
		}
		

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.info.vdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		int colorBase = modeInfo.color.base;

		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{

			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i >> 5) << 3;
			block.c = (i & 31) << 3;

			int pp, cp;
			
			pp = cp = (currchar + (i & 0x300)) << 3;
			pp &= bitpattmask;
			cp &= bitcolormask;
			
			info.canvas.draw8x8MultiColorBlock(block.r, block.c, 
					info.vdp.getByteReadMemoryAccess(pattBase + pp),
					info.vdp.getByteReadMemoryAccess(colorBase + cp));
		}

		return count;
	}
}
