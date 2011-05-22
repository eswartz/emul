/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * @author ejs
 *
 */
public class BitmapModeRedrawHandler extends BaseRedrawHandler implements
		VdpModeRedrawHandler {

	short bitpattmask;
	short bitcolormask;
	protected VdpTouchHandler modify_color_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			vdpChanges.color[offs >> 3] = 1;
			vdpChanges.changed = true;
		}
		
	};
	protected VdpTouchHandler modify_patt_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			vdpChanges.patt[offs >> 3] = 1;
			vdpChanges.changed = true;
		}
		
	};

	public BitmapModeRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);
		

		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = modify_color_bitmap;
		vdpTouchBlock.patt = modify_patt_bitmap;

		bitcolormask = (short) ((((short) (vdpregs[3] & 0x7f)) << 6) | 0x3f);

		// thanks, Thierry!
		// in "bitmap text" mode, the full pattern table is always addressed,
		// otherwise, the color bits are used in the pattern masking
		if ((vdpregs[1] & 0x10) != 0)
			bitpattmask = (short) ((((short) (vdpregs[4] & 0x03) << 11)) | 0x7ff);
		else
			bitpattmask =
				(short) ((((short) (vdpregs[4] & 0x03) << 11)) | (bitcolormask & 0x7ff));

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void propagateTouches() {
		/*  Set pattern or color changes in chars */
		
		int bpm = bitpattmask >> 3;
		int bcm = bitcolormask >> 3;
		for (int i = 0; i < 768; i++) {
			int sector =  (i & 0x300);
			int currchar = vdp.readAbsoluteVdpMemory(vdpModeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (vdpChanges.patt[(currchar + sector) & bpm] != 0
					|| vdpChanges.color[(currchar + sector) & bcm] != 0) { /* if color or pattern changed */
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

		for (int i = 0; i < 768; i++) {
			if (force || vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;
				block.c = (i & 31) << 3;

				int pp, cp;
				
				pp = cp = (currchar + (i & 0x300)) << 3;
				pp &= bitpattmask;
				cp &= bitcolormask;
				
				vdpCanvas.draw8x8MultiColorBlock(block.r, block.c, 
						vdp.getByteReadMemoryAccess(pattBase + pp),
						vdp.getByteReadMemoryAccess(colorBase + cp));
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.BaseRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData(IBitmapPixelAccess access) {
		boolean isMono = bitcolormask != 0x1fff;
		
		ByteMemoryAccess screen = vdp.getByteReadMemoryAccess(vdpModeInfo.screen.base);
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdpModeInfo.patt.base);
		ByteMemoryAccess color = vdp.getByteReadMemoryAccess(vdpModeInfo.color.base);
		
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x += 8) {
				byte p = 0, f = 0, b = 0;
				
				// for the benefit of mono pictures or mono modes, 
				// always select the lesser color as foreground.
				f = access.getPixel(x, y);
				p = (byte) 0x80;
				
				boolean gotBG = false;
				for (int xo = 1; xo < 8; xo++) {
					byte c = access.getPixel(x + xo, y);
					if (c == f) {
						p |= 0x80 >> xo;
					} else {
						if (!gotBG) {
							gotBG = true;
							b = c;
							if (b < f) {
								b = f;
								f = c;
								p ^= 0xff << (8 - xo);
								p |= 0x80 >> xo;
							}
						}
					}
				}

				int choffs = ((y >> 6) << 8) + ((y & 0x3f) >> 3) * 32 + (x >> 3);
				int ch = screen.memory[screen.offset + choffs] & 0xff;

				int poffs = (y >> 6) * 0x800 + (ch << 3) + (y & 7);
				
				patt.memory[patt.offset + poffs] = p;
				touch(patt.offset + poffs);
				
				if (!isMono) {
					color.memory[color.offset + poffs] = (byte) ((f << 4) | (b));
					touch(color.offset + poffs);
				}
			}
		}
		
	}
}
