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
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Text 2 mode
 * @author ejs
 *
 */
public class Text2ModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_text2 = new VdpTouchHandler() {
		
		public void modify(int offs) {
			vdpChanges.color[offs] = 1;
			vdpChanges.changed = true;
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
		
		// propagate blink changes
		int size = vdpModeInfo.screen.size;
		for (int i = 0; i < size; i++) {
			if ((vdpChanges.color[i >> 3]) != 0) { 	/* this position changed? */
				vdpChanges.screen[i] = VdpChanges.SC_BACKGROUND;	/* then this char changed */
			}
		}
		
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
		if ((vdpregs[9] & VdpV9938.R9_LN) == 0)
			size = 80 * 24;
		
		tbg = (byte) (vdpregs[7] & 0xf);
		tfg = (byte) ((vdpregs[7] >> 4) & 0xf);
		
		bbg = (byte) (vdpregs[12] & 0xf);
		bfg = (byte) ((vdpregs[12] >> 4) & 0xf);

		boolean blinkOn = ((VdpV9938)vdp).blinkOn;
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

				if (blinkOn) {
					byte blinkMap = vdp.readAbsoluteVdpMemory(colorBase + (i >> 3));
					
					if ((blinkMap & (0x80 >> (i & 7))) != 0) {
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

	/**
	 * See if a blinking toggle really affects anything on screen
	 */
	public void updateForBlink() {
		// is there even a distinct blink color?
		if (vdpregs[12] == vdpregs[7])
			 return;
		
		// update any blink entries which are on
		int screenBase = vdpModeInfo.screen.base;
		int colorBase = vdpModeInfo.color.base;
		ByteMemoryAccess access = vdp.getByteReadMemoryAccess(colorBase);
		int size = vdpModeInfo.color.size;
		for (int i = 0; i < size; i++) {
			byte cur = access.memory[access.offset + i];
			for (int j = 0; j < 8; j++) {
				if ((cur & (0x80 >> j)) != 0) {
					int screenOffs = (i << 3) + j;
					vdpChanges.changed |= touch(screenBase + screenOffs);
				}
			}
		}
	}

}
