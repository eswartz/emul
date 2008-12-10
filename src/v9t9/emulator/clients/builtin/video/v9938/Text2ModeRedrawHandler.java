/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.VdpHandler;

/**
 * Text 2 mode
 * @author ejs
 *
 */
public class Text2ModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_text2 = new VdpTouchHandler() {
		
		public void modify(int offs) {
			vdpChanges.color[offs] = vdpchanged = 1;			
		}
		
	};
	
	public Text2ModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);

		vdpTouchBlock.patt = modify_patt_default;
		vdpTouchBlock.sprite = vdpTouchBlock.sprpat = null;
		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = modify_color_text2;
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
		int colorBase = vdpModeInfo.color.base;
		
		// normal text colors
		byte tfg, tbg;
		// blinky colors
		byte bfg, bbg;
		
		int size = vdpModeInfo.screen.size;
		if ((vdpregs[8] & VdpV9938.R9_LN) == 0)
			size = 80 * 24;
		
		tbg = (byte) (vdpregs[7] & 0xf);
		tfg = (byte) ((vdpregs[7] >> 4) & 0xf);
		
		bbg = (byte) (vdpregs[12] & 0xf);
		bfg = (byte) ((vdpregs[12] >> 4) & 0xf);

		for (int i = 0; i < size; i++) {
			if (force 
					|| vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED			/* this screen pos updated? */
					|| vdpChanges.color[i >> 3] != 0) 
			{
				int currchar = vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i / 80) << 3;	
				block.c = (i % 80) * 6 + (512 - 480) / 2;

				int pattOffs = pattBase + (currchar << 3);
				
				byte fg, bg;
				fg = tfg; bg = tbg;

				if (((VdpV9938)vdp).blinkOn) {
					byte blinkMap = vdp.readAbsoluteVdpMemory(colorBase + i >> 3);
					
					if ((blinkMap & (i >> 3)) != 0) {
						fg = bfg; bg = bbg;
					}
				}
				vdpCanvas.draw8x6TwoColorBlock(block.r, block.c, 
						vdp.getByteReadMemoryAccess(pattOffs), 
						fg, bg);
			}
		}


		return count;
	}

}
