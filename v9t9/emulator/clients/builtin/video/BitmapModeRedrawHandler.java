/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.MemoryDomain;

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
			vdpChanges.color[offs >> 3] = vdpchanged = 1;			
		}
		
	};
	protected VdpTouchHandler modify_patt_bitmap = new VdpTouchHandler() {
	
		public void modify(int offs) {
			vdpChanges.patt[offs >> 3] = vdpchanged = 1;			
		}
		
	};

	public BitmapModeRedrawHandler(byte[] vdpregs, MemoryDomain vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);
		
		int         ramsize = (vdpregs[1] & InternalVdp.R1_RAMSIZE) != 0 ? 0x3fff : 0xfff;

		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		vdpCanvas.setSize(256, 192);

		vdpTouchBlock.screen = modify_screen_default;
		vdpTouchBlock.color = modify_color_bitmap;
		vdpTouchBlock.patt = modify_patt_bitmap;

		vdpModeInfo.color.base = (vdpregs[3] & 0x80) != 0 ? 0x2000 : 0;
		vdpModeInfo.color.size = 6144;
		bitcolormask = (short) ((((short) (vdpregs[3] & 0x7f)) << 6) | 0x3f);

		vdpModeInfo.patt.base = (vdpregs[4] & 0x4) != 0 ? 0x2000 : 0;
		vdpModeInfo.patt.size = 6144;

		// thanks, Thierry!
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
		
		for (int i = 0; i < 768; i++) {
			int sector =  (i & 0x300);
			int currchar = vdpMemory.flatReadByte(vdpModeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (vdpChanges.patt[currchar + sector] != 0
					|| vdpChanges.color[currchar + sector] != 0) { /* if color or pattern changed */
				vdpChanges.screen[i] = VdpChanges.SC_BACKGROUND;	/* then this char changed */
			}
		}
		

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*  Redraw changed chars  */

		int count = 0;
		int screenBase = vdpModeInfo.screen.base;
		int pattBase = vdpModeInfo.patt.base;
		int colorBase = vdpModeInfo.color.base;

		for (int i = 0; i < 768; i++) {
			if (vdpChanges.screen[i] != VdpChanges.SC_UNTOUCHED) {			/* this screen pos updated? */
				int currchar = vdpMemory.flatReadByte(screenBase + i) & 0xff;	/* char # to update */

				RedrawBlock block = blocks[count++];
				
				block.r = (i >> 5) << 3;
				block.c = (i & 31) << 3;

				int pp, cp;
				
				pp = cp = (currchar + (i & 0x300)) << 3;
				pp &= bitpattmask;
				cp &= bitcolormask;
				
				vdpCanvas.draw8x8MultiColorBlock(block.r, block.c, 
						vdpMemory.getByteReadMemoryAccess(pattBase + pp),
						vdpMemory.getByteReadMemoryAccess(colorBase + cp));
			}
		}

		return count;
	}

}
