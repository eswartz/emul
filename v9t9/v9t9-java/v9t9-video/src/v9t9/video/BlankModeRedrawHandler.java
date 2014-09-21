/*
  BlankModeRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import v9t9.common.video.RedrawBlock;
import v9t9.video.common.VdpModeInfo;



/**
 * @author ejs
 *
 */
public class BlankModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeBlockRedrawHandler, IVdpModeRowRedrawHandler {

	public BlankModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
		
		info.touch.patt = null;
		info.touch.sprite = info.touch.sprpat = null;
		info.touch.screen = null;
		info.touch.color = null;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void prepareUpdate() {
		propagatePatternTouches();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		return 0;
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
	public void updateCanvas(int prevScanline, int currentScanline) {
//		System.out.println("blank: " + prevScanline + " - " + currentScanline);
		
		byte bg = (byte) ((info.vdpregs[7] ) & 0xf);
		for (int line = prevScanline; line < currentScanline; line++) {
			int rowOffs = info.canvas.getBitmapOffset(0, line); 
			for (int x = info.canvas.getWidth() - 8; x >= 0; x -= 8) {
				info.canvas.drawEightPixels(rowOffs + x, (byte) 0xff, bg, bg);
			}
		}
	}
}
