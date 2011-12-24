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
	
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */
		int count = 0;
		int screenBase = modeInfo.screen.base;
		
		for (int i = info.changes.screen.nextSetBit(0); 
			i >= 0 && i < modeInfo.screen.size; 
			i = info.changes.screen.nextSetBit(i+1)) 
		{
			int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;

			RedrawBlock block = blocks[count++];
			
			block.r = (i >> 5) << 3;	/* for graphics mode */
			block.c = (i & 31) << 3;
			byte color = (byte) info.vdp.readAbsoluteVdpMemory(modeInfo.color.base + (currchar >> 3));

			byte fg, bg;
			
			bg = (byte) (color & 0xf);
			fg = (byte) ((color >> 4) & 0xf);
			
			info.canvas.draw8x8TwoColorBlock(block.r, block.c, info.vdp.getByteReadMemoryAccess((modeInfo.patt.base + (currchar << 3))), fg, bg); 
		}
		return count;
	}

}
