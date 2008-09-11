/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import v9t9.engine.memory.MemoryDomain;

/**
 * Redraw graphics mode content
 * @author ejs
 *
 */
public class GraphicsModeRedrawHandler extends BaseRedrawHandler implements VdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_graphics = new VdpTouchHandler() {
	
		public void modify(int offs) {
			int ptr = offs << 3;
			Arrays.fill(vdpChanges.patt, ptr, ptr + 8, (byte)1);
	    	vdpchanged = 1;			
		}
		
	};

	public GraphicsModeRedrawHandler(byte[] vdpregs, MemoryDomain vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, changed, vdpCanvas);
		
		int         ramsize = (vdpregs[1] & InternalVdp.R1_RAMSIZE) != 0 ? 0x3fff : 0xfff;

		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = (vdpregs[3] * 0x40) & ramsize;
		vdpModeInfo.color.size = 32;
		vdpModeInfo.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		vdpCanvas.setSize(256, 192);
		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = modify_color_graphics;
		vdpTouchBlock.patt = modify_patt_default;
	}
		
	public void propagateTouches() {
		propagatePatternTouches();
	}
	
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */
		int count = 0;
		int screenBase = vdpModeInfo.screen.base;
		for (int i = 0; i < 768; i++) {
			byte changes = vdpChanges.screen[i];
			if (changes != 0) {			/* this screen pos updated? */
				//logger(_L|L_3, _("redrawing char %d\n"), i);
				int currchar = vdpMemory.flatReadByte(screenBase + i) & 0xff;

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;	/* for graphics mode */
				block.c = (i & 31) << 3;

				redraw_graphics_block(
						block,
						vdpModeInfo.patt.base + (currchar << 3),
						(byte) vdpMemory.flatReadByte(vdpModeInfo.color.base + (currchar >> 3))); 

					/* can't redraw easily */
				//if (changes == SC_SPRITE_COVERING)
				//	ull->pattern = ull->colors = NULL;
			}
		}
		return count;
	}

	private void redraw_graphics_block(RedrawBlock block, int pattOffs, byte color) {
		byte fg, bg;
		
		bg = (byte) (color & 0xf);
		fg = (byte) ((color >> 4) & 0xf);
		
		vdpCanvas.draw8x8TwoColorBlock(block.r, block.c, readEightBytes(pattOffs), fg, bg);
	}


}
