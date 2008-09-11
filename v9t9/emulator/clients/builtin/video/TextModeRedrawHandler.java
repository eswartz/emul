/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public class TextModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	public TextModeRedrawHandler(byte[] vdpregs, MemoryDomain vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);
		
		int         ramsize = (vdpregs[1] & InternalVdp.R1_RAMSIZE) != 0 ? 0x3fff : 0xfff;

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
		vdpCanvas.setSize(256, 192);
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
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = vdpModeInfo.screen.base;
		int pattBase = vdpModeInfo.patt.base;

		for (int i = 0; i < 960; i++) {
			if (vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = vdpMemory.flatReadByte(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i / 40) << 3;	
				block.c = (i % 40) * 6 + (256 - 240) / 2;

				int pattOffs = pattBase + (currchar << 3);
				vdpCanvas.draw8x6TwoColorBlock(block.r, block.c, 
						readEightBytes(pattOffs), (byte)16, (byte)0);
			}
		}


		return count;
	}

}
