/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpRedrawInfo;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Redraw graphics mode content
 * @author ejs
 *
 */
public class GraphicsModeRedrawHandler extends BaseRedrawHandler implements VdpModeRedrawHandler {

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
		
	public void propagateTouches() {
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

	@Override
	public void importImageData(IBitmapPixelAccess access) {
		ByteMemoryAccess screen = info.vdp.getByteReadMemoryAccess(modeInfo.screen.base);
		ByteMemoryAccess patt = info.vdp.getByteReadMemoryAccess(modeInfo.patt.base);
		ByteMemoryAccess color = info.vdp.getByteReadMemoryAccess(modeInfo.color.base);
		
		// assume char 255 is not used
		Arrays.fill(screen.memory, screen.offset, screen.offset + 768, (byte) 0xff);
		for (int i = 0; i < 768; i++)
			touch(screen.offset + i);

		Arrays.fill(patt.memory, patt.offset + 255 * 8, patt.offset + 256 * 8, (byte) 0x0);
		for (int i = 0; i < 8; i++)
			touch(patt.offset + 255*8 + i);

		byte b = 0;
		
		byte cb = (byte) info.vdp.readVdpReg(7);
		cb = (byte) ((cb & 0xf) | 0x10);
		
		b = (byte) ((cb >> 0) & 0xf);

		Arrays.fill(color.memory, color.offset, color.offset + 32, cb);
		for (int i = 0; i < 32; i++)
			touch(color.offset + i);

		int width = access.getWidth();
		int height = access.getHeight();
		
		int yoffs = ((192 - height) / 2) & ~0x7;
		int xoffs = ((256 - width) / 2) & ~0x7;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x+= 8) {
				int ch = ((y >> 3) * ((width + 7) >> 3)) + (x >> 3);
				if (ch > 0xff)
					throw new IllegalStateException();
				int choffs = (((y + yoffs) >> 3) << 5) + ((x + xoffs) >> 3);
				
				screen.memory[screen.offset + choffs] = (byte) ch;
				touch(screen.offset + choffs);
				
				int poffs = (ch << 3) + (y & 7);
				
				byte p = 0;
				
				for (int xo = 0; xo < 8; xo++) {
					byte c = access.getPixel(x + xo + xoffs, y + yoffs);
					if (c != b) {
						p |= 0x80 >> xo;
					}
				}

				patt.memory[patt.offset + poffs] = p;
				touch(patt.offset + poffs);
			}
		}
		
	}


}
