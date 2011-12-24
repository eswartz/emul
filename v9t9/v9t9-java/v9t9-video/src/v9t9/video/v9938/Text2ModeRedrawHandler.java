/**
 * 
 */
package v9t9.video.v9938;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpModeInfo;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.VdpTouchHandler;

import static v9t9.common.hardware.VdpV9938Consts.*;

/**
 * Text 2 mode
 * @author ejs
 *
 */
public class Text2ModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_text2 = new VdpTouchHandler() {
		
		public void modify(int offs) {
			info.changes.color[offs] = 1;
			info.changes.changed = true;
		}
		
	};
	
	public Text2ModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);


		info.touch.patt = modify_patt_default;
		info.touch.sprite = info.touch.sprpat = null;
		info.touch.screen = modify_screen_default;
		info.touch.color = modify_color_text2;
	}

	public void prepareUpdate() {
		propagatePatternTouches();
		
		// propagate blink changes
		int size = modeInfo.screen.size;
		for (int i = 0; i < size; i++) {
			if ((info.changes.color[i >> 3]) != 0) { 	/* this position changed? */
				info.changes.screen.set(i);	/* then this char changed */
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		int colorBase = modeInfo.color.base;
		
		// normal text colors
		byte tfg, tbg;
		// blinky colors
		byte bfg, bbg;
		
		int size = modeInfo.screen.size;
		if ((info.vdpregs[9] & R9_LN) == 0)
			size = 80 * 24;
		
		tbg = (byte) (info.vdpregs[7] & 0xf);
		tfg = (byte) ((info.vdpregs[7] >> 4) & 0xf);
		
		bbg = (byte) (info.vdpregs[12] & 0xf);
		bfg = (byte) ((info.vdpregs[12] >> 4) & 0xf);

		boolean blinkOn = ((VdpV9938CanvasRenderer) info.renderer).isBlinkOn();
		
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

			RedrawBlock block = blocks[count++];
			
			block.r = (i / 80) << 3;	
			block.c = (i % 80) * 6 + (512 - 480) / 2;

			int pattOffs = pattBase + (currchar << 3);
			
			byte fg, bg;
			fg = tfg; bg = tbg;

			if (blinkOn) {
				byte blinkMap = info.vdp.readAbsoluteVdpMemory(colorBase + (i >> 3));
				
				if ((blinkMap & (0x80 >> (i & 7))) != 0) {
					fg = bfg; bg = bbg;
				}
			}
			info.canvas.draw8x6TwoColorBlock(block.r, block.c, 
					info.vdp.getByteReadMemoryAccess(pattOffs), 
					fg, bg);
		}


		return count;
	}

	/**
	 * See if a blinking toggle really affects anything on screen
	 */
	public void updateForBlink() {
		// is there even a distinct blink color?
		if (info.vdpregs[12] == info.vdpregs[7])
			 return;
		
		// update any blink entries which are on
		int screenBase = modeInfo.screen.base;
		int colorBase = modeInfo.color.base;
		ByteMemoryAccess access = info.vdp.getByteReadMemoryAccess(colorBase);
		int size = modeInfo.color.size;
		for (int i = 0; i < size; i++) {
			byte cur = access.memory[access.offset + i];
			for (int j = 0; j < 8; j++) {
				if ((cur & (0x80 >> j)) != 0) {
					int screenOffs = (i << 3) + j;
					info.changes.changed |= touch(screenBase + screenOffs);
				}
			}
		}
	}

}
