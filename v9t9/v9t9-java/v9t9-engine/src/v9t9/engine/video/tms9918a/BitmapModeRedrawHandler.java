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
import v9t9.engine.video.VdpTouchHandler;

/**
 * @author ejs
 *
 */
public class BitmapModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

	short bitpattmask;
	short bitcolormask;
	protected VdpTouchHandler modify_color_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			info.changes.color[offs >> 3] = 1;
			info.changes.changed = true;
		}
		
	};
	protected VdpTouchHandler modify_patt_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			info.changes.patt[offs >> 3] = 1;
			info.changes.changed = true;
		}
		
	};

	public BitmapModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		info.touch.screen = modify_screen_default;
		info.touch.color = modify_color_bitmap;
		info.touch.patt = modify_patt_bitmap;

		bitcolormask = (short) ((((short) (info.vdpregs[3] & 0x7f)) << 6) | 0x3f);

		// thanks, Thierry!
		// in "bitmap text" mode, the full pattern table is always addressed,
		// otherwise, the color bits are used in the pattern masking
		if ((info.vdpregs[1] & 0x10) != 0)
			bitpattmask = (short) ((((short) (info.vdpregs[4] & 0x03) << 11)) | 0x7ff);
		else
			bitpattmask =
				(short) ((((short) (info.vdpregs[4] & 0x03) << 11)) | (bitcolormask & 0x7ff));

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void prepareUpdate() {
		/*  Set pattern or color changes in chars */
		
		int bpm = bitpattmask >> 3;
		int bcm = bitcolormask >> 3;
		for (int i = 0; i < 768; i++) {
			int sector =  (i & 0x300);
			int currchar = info.vdp.readAbsoluteVdpMemory(modeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (info.changes.patt[(currchar + sector) & bpm] != 0
					|| info.changes.color[(currchar + sector) & bcm] != 0) { /* if color or pattern changed */
				info.changes.screen[i] = VdpChanges.SC_BACKGROUND;	/* then this char changed */
			}
		}
		

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.info.vdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = modeInfo.screen.base;
		int pattBase = modeInfo.patt.base;
		int colorBase = modeInfo.color.base;

		for (int i = 0; i < 768; i++) {
			if (force || info.changes.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = info.vdp.readAbsoluteVdpMemory(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;
				block.c = (i & 31) << 3;

				int pp, cp;
				
				pp = cp = (currchar + (i & 0x300)) << 3;
				pp &= bitpattmask;
				cp &= bitcolormask;
				
				info.canvas.draw8x8MultiColorBlock(block.r, block.c, 
						info.vdp.getByteReadMemoryAccess(pattBase + pp),
						info.vdp.getByteReadMemoryAccess(colorBase + cp));
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.BaseRedrawHandler#importImageData()
	 */
	@Override
	public void importImageData(IBitmapPixelAccess access) {
		boolean isMono = isMono();
		
		ByteMemoryAccess screen = info.vdp.getByteReadMemoryAccess(modeInfo.screen.base);
		ByteMemoryAccess patt = info.vdp.getByteReadMemoryAccess(modeInfo.patt.base);
		ByteMemoryAccess color = info.vdp.getByteReadMemoryAccess(modeInfo.color.base);
		
		byte f = 0, b = 0;
		
		if (isMono) {
			f = (byte) ((info.vdp.readVdpReg(7) >> 4) & 0xf);
			b = (byte) ((info.vdp.readVdpReg(7) >> 0) & 0xf);
		}

		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x += 8) {
				
				int choffs = ((y >> 6) << 8) + ((y & 0x3f) >> 3) * 32 + (x >> 3);
				int ch = choffs & 0xff;
				
				if ((y & 7) == 0) {
					screen.memory[screen.offset + choffs] = (byte) ch;
					touch(screen.offset + choffs);
				}

				int poffs = (y >> 6) * 0x800 + (ch << 3) + (y & 7);
				
				byte p = 0;
				
				if (!isMono) {
					// in color mode, by convention keep the foreground color
					// as the lesser color.
					f = access.getPixel(x, y);
					p = (byte) 0x80;
				
					boolean gotBG = false;
					for (int xo = 1; xo < 8; xo++) {
						byte c = access.getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						} else {
							if (!gotBG) {
								if (c < f) {
									b = f;
									f = c;
									p ^= (0xff << (8 - xo));
									p |= 0x80 >> xo;
								} else {
									b = c;
								}
								gotBG = true;
							}
						}
					}
					
					color.memory[color.offset + poffs] = (byte) ((f << 4) | (b));
					touch(color.offset + poffs);
				} else {
					// in mono mode, mapper has matched with fg and bg from vr7
					for (int xo = 0; xo < 8; xo++) {
						byte c = access.getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						}
					}
				}

				patt.memory[patt.offset + poffs] = p;
				touch(patt.offset + poffs);
			}
		}
		
	}

	public boolean isMono() {
		boolean isMono = bitcolormask != 0x1fff;
		return isMono;
	}
}
