/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.VdpHandler;

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

	public GraphicsModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, changed, vdpCanvas, modeInfo);
		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = modify_color_graphics;
		vdpTouchBlock.patt = modify_patt_default;
	}
		
	public void propagateTouches() {
		propagatePatternTouches();
	}
	
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw changed chars  */
		int count = 0;
		int screenBase = vdpModeInfo.screen.base;
		for (int i = 0; i < 768; i++) {
			byte changes = vdpChanges.screen[i];
			if (force || changes != 0) {			/* this screen pos updated? */
				//logger(_L|L_3, _("redrawing char %d\n"), i);
				int currchar = vdpMemory.readAbsoluteVdpMemory(screenBase + i) & 0xff;

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;	/* for graphics mode */
				block.c = (i & 31) << 3;

				redraw_graphics_block(
						block,
						vdpModeInfo.patt.base + (currchar << 3),
						(byte) vdpMemory.readAbsoluteVdpMemory(vdpModeInfo.color.base + (currchar >> 3))); 

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
		
		vdpCanvas.draw8x8TwoColorBlock(block.r, block.c, vdpMemory.getByteReadMemoryAccess(pattOffs), fg, bg);
	}


}
