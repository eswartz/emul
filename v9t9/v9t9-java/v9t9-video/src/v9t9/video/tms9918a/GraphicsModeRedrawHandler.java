/**
 * 
 */
package v9t9.video.tms9918a;

import java.util.Arrays;

import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpModeInfo;
import v9t9.video.BaseRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.VdpTouchHandler;

/**
 * Redraw graphics mode content
 * @author ejs
 *
 */
public class GraphicsModeRedrawHandler extends BaseRedrawHandler implements IVdpModeRedrawHandler {

	protected VdpTouchHandler modify_color_graphics = new VdpTouchHandler() {
	
		public void modify(int offs) {
			int ptr = offs << 3;
			Arrays.fill(info.changes.patt, ptr, ptr + 8, (byte)1);
			info.changes.changed = true;			
		}
		
	};

	public GraphicsModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
		info.touch.screen = modify_screen_default;
		info.touch.color = modify_color_graphics;
		info.touch.patt = modify_patt_default;
	}
		
	public void prepareUpdate() {
		propagatePatternTouches();
	}
	
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw changed chars  */
		int count = 0;
		int screenBase = modeInfo.screen.base;
		for (int i = 0; i < 768; i++) {
			byte changes = info.changes.screen[i];
			if (force || changes != 0) {			/* this screen pos updated? */
				int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;	/* for graphics mode */
				block.c = (i & 31) << 3;

				redraw_graphics_block(
						block,
						modeInfo.patt.base + (currchar << 3),
						(byte) info.vdp.readAbsoluteVdpMemory(modeInfo.color.base + (currchar >> 3))); 
			}
		}
		return count;
	}

	private void redraw_graphics_block(RedrawBlock block, int pattOffs, byte color) {
		byte fg, bg;
		
		bg = (byte) (color & 0xf);
		fg = (byte) ((color >> 4) & 0xf);
		
		info.canvas.draw8x8TwoColorBlock(block.r, block.c, info.vdp.getByteReadMemoryAccess(pattOffs), fg, bg);
	}

}
