/**
 * 
 */
package v9t9.engine.video.tms9918a;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpChanges;
import v9t9.common.video.VdpModeInfo;
import v9t9.engine.video.BaseRedrawHandler;
import v9t9.engine.video.IBitmapPixelAccess;
import v9t9.engine.video.IVdpModeRedrawHandler;
import v9t9.engine.video.VdpRedrawInfo;

/**
 * @author ejs
 *
 */
public class MulticolorModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

	public MulticolorModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
		info.touch.screen = modify_screen_default;
		info.touch.color = null;
		info.touch.patt = modify_patt_default;
	}

	static final byte stockMultiBlockPattern[] = { 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0, 
		(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0 
	};
	
	static final ByteMemoryAccess multiBlockPattern = 
		new ByteMemoryAccess(stockMultiBlockPattern, 0);

	public void prepareUpdate() {
		propagatePatternTouches();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.info.vdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;

		for (int i = 0; i < 768; i++) {
			if (force || info.changes.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;
				block.c = (i & 31) << 3;

				int pattOffs = pattBase + (currchar << 3) + ((i >> 5) & 3) * 2;
				
				byte mem1 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs);
				byte mem2 = (byte) info.vdp.readAbsoluteVdpMemory(pattOffs + 1);
				
				byte[] colors = { mem1, mem1, mem1, mem1, mem2, mem2, mem2, mem2 }; 

				info.canvas.draw8x8MultiColorBlock(block.r, block.c, 
						multiBlockPattern,
						new ByteMemoryAccess(colors, 0));
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.BaseRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData(IBitmapPixelAccess access) {
		ByteMemoryAccess patt = info.vdp.getByteReadMemoryAccess(modeInfo.patt.base);
		
		for (int y = 0; y < 48; y++) {
			for (int x = 0; x < 64; x += 2) {
				
				byte f = access.getPixel(x, y);
				byte b = access.getPixel(x + 1, y);
				
				int poffs = ((y >> 3) << 8) + (y & 7) + ((x >> 1) << 3);  
				//System.out.println("("+y+","+x+") = "+ poffs);
				patt.memory[patt.offset + poffs] = (byte) ((f << 4) | b);
				touch(patt.offset + poffs);
			}
		}
	}
}
