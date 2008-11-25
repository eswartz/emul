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
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * @author ejs
 *
 */
public class MulticolorModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	public MulticolorModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);
		
		int         ramsize = (vdpregs[1] & VdpConstants.R1_RAMSIZE) != 0 ? 0x3fff : 0xfff;

		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdpModeInfo.patt.size = 1536;
		vdpCanvas.setSize(256, 192);
		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = null;
		vdpTouchBlock.patt = modify_patt_default;
	}

	static final byte stockMultiBlockPattern[] = { 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0, 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0 
	};
	
	static final ByteMemoryAccess multiBlockPattern = 
		new ByteMemoryAccess(stockMultiBlockPattern, 0);

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

		for (int i = 0; i < 768; i++) {
			if (force || vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = vdpMemory.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;
				block.c = (i & 31) << 3;

				int pattOffs = pattBase + (currchar << 3) + ((i >> 5) & 3) * 2;
				
				byte mem1 = (byte) vdpMemory.readAbsoluteVdpMemory(pattOffs);
				byte mem2 = (byte) vdpMemory.readAbsoluteVdpMemory(pattOffs + 1);
				
				byte[] colors = { mem1, mem1, mem1, mem1, mem2, mem2, mem2, mem2 }; 

				vdpCanvas.draw8x8MultiColorBlock(block.r, block.c, 
						multiBlockPattern,
						new ByteMemoryAccess(colors, 0));
			}
		}

		return count;
	}

}
