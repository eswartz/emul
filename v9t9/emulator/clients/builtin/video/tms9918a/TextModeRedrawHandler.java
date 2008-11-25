/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpConstants;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.engine.VdpHandler;

/**
 * @author ejs
 *
 */
public class TextModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	public TextModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);
		
		int         ramsize = (vdpregs[1] & VdpConstants.R1_RAMSIZE) != 0 ? 0x3fff : 0xfff;

		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 960;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = 0;
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = 0;
		vdpModeInfo.sprpat.size = 0;
//		screenxsize = 240;
		vdpCanvas.setSize(256, 192);	// keep the full res but only draw in center
		vdpTouchBlock.patt = modify_patt_default;
		vdpTouchBlock.sprite = vdpTouchBlock.sprpat = null;
		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = null;

		vdpCanvas.clear();
	}

	public void propagateTouches() {
		propagatePatternTouches();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = vdpModeInfo.screen.base;
		int pattBase = vdpModeInfo.patt.base;

		for (int i = 0; i < 960; i++) {
			if (force || vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = vdpMemory.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i / 40) << 3;	
				block.c = (i % 40) * 6 + (256 - 240) / 2;

				int pattOffs = pattBase + (currchar << 3);
				vdpCanvas.draw8x6TwoColorBlock(block.r, block.c, 
						vdpMemory.getByteReadMemoryAccess(pattOffs), (byte)16, (byte)0);
			}
		}


		return count;
	}

}
