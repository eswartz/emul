/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpSprite;
import v9t9.emulator.clients.builtin.video.VdpSpriteCanvas;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * @author ejs
 * 
 */
public class SpriteRedrawHandler extends BaseRedrawHandler {

	static boolean five_sprites_on_a_line = true;
	protected VdpTouchHandler modify_sprite_default = new VdpTouchHandler() {

		public void modify(int offs) {
			vdpChanges.sprite |= (1<<(offs >> 2));
			vdpchanged = 1;
		}

	};
	protected VdpTouchHandler modify_sprpat_default = new VdpTouchHandler() {

		public void modify(int offs) {
			int patt;

			if ((vdpregs[1] & VdpTMS9918A.R1_SPR4) != 0) {
				patt = (offs >> 3) & 0xfc;
				Arrays.fill(vdpChanges.sprpat, patt, patt + 4, (byte) 1);
			} else {
				patt = offs >> 3;
				vdpChanges.sprpat[patt] = 1;
			}

			vdpchanged = 1;
		}

	};

	private VdpSpriteCanvas spriteCanvas;

	public SpriteRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);

		vdpTouchBlock.sprite = modify_sprite_default;
		vdpTouchBlock.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSpriteCanvas(vdpCanvas);
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		if (vdpModeInfo.sprite.base <= addr
				&& addr < vdpModeInfo.sprite.base + vdpModeInfo.sprite.size) {
			vdpTouchBlock.sprite.modify(addr - vdpModeInfo.sprite.base);
			visible = true;
		}

		if (vdpModeInfo.sprpat.base <= addr
				&& addr < vdpModeInfo.sprpat.base + vdpModeInfo.sprpat.size) {
			vdpTouchBlock.sprpat.modify(addr - vdpModeInfo.sprpat.base);
			visible = true;
		}

		return visible;
	}

	/**
	 * This function does the mammoth tasks of:
	 * 
	 * 0) Change sprites under whom chars changed 1) See if sprite pattern
	 * change forces a sprite update. 2) See if position change forces a sprite
	 * update. 3) See if deleted-ptr changes, forcing sprite updates. 4) Update
	 * vdp_changes.screen where sprites dance. 5) Force updates to sprites under
	 * changed ones. 6) Weed out blank sprites. 7) Set VDP status flags for
	 * coincidence and five sprites on a line (hackish)
	 * 
	 * @return the updated VDP status bits
	 */
	public byte updateSpriteCoverage(byte vdpStatus) {
		
		// Update changes when sprite patterns change
		VdpSprite[] sprites = spriteCanvas.getSprites();
		
		// figure sprite size/mag
		int size = 8;
		int numchars = 1;
		switch (vdpregs[1] & (VdpTMS9918A.R1_SPRMAG + VdpTMS9918A.R1_SPR4)) {
		case 0:
			size = 8;
			numchars = 1;
			break;
		case VdpTMS9918A.R1_SPRMAG:
			size = 16;
			numchars = 1;
			break;
		case VdpTMS9918A.R1_SPR4:
			size = 16;
			numchars = 4;
			break;
		case VdpTMS9918A.R1_SPR4 + VdpTMS9918A.R1_SPRMAG:
			size = 32;
			numchars = 4;
			break;
		}
		
		int sprbase = vdpModeInfo.sprite.base;
		int sprpatbase = vdpModeInfo.sprpat.base;
		
		ByteMemoryAccess access = vdpMemory.getByteReadMemoryAccess(sprbase);
		boolean deleted = false;
		for (int i = 0; i < 32; i++) {
			VdpSprite sprite = sprites[i];
			int y = access.memory[access.offset++] & 0xff;
			int x = access.memory[access.offset++] & 0xff;
			int ch = access.memory[access.offset++] & 0xff;
			int color = access.memory[access.offset++] & 0xff;
			int shift = (color & 0x80) != 0 ? -32 : 0;
			color &= 0xf;
			
			if (y == 0xd0) {
				deleted = true;
			}
			if (deleted) {
				sprite.setDeleted(true);
			} else {
				/*  just trigger dirty by looking at the following stuff
				if ((vdpChanges.sprite & (1 << i)) != 0) {
					sprite.setBitmapDirty(true);
				}
				*/
				sprite.setDeleted(color == 0);
				sprite.move(x, y);
				sprite.setColor(color);
				sprite.setShift(shift);
				sprite.setPattern(vdpMemory.getByteReadMemoryAccess(sprpatbase + (ch << 3)));
				sprite.setSize(size);
				sprite.setNumchars(numchars);
				
				// also check whether the pattern content changed
				if (vdpChanges.sprpat[ch] != 0)
					sprite.setBitmapDirty(true);
			}
		}

		// TODO: move the VDP status logic
		int fifth_sprite = spriteCanvas.updateSpriteCoverage(vdpChanges.screen);

		if (fifth_sprite != -1) {
			vdpStatus = (byte) (vdpStatus
					& ~(VdpTMS9918A.VDP_FIVE_SPRITES | VdpTMS9918A.VDP_FIFTH_SPRITE) 
					| (VdpTMS9918A.VDP_FIVE_SPRITES | fifth_sprite));
		} else {
			vdpStatus &= ~(VdpTMS9918A.VDP_FIVE_SPRITES | VdpTMS9918A.VDP_FIFTH_SPRITE);
		}


		return vdpStatus;
	}

	/**
	 * Draw the sprites
	 * @param force
	 */
	public void updateCanvas(boolean force) {
		spriteCanvas.drawSprites();
	}

	public void redrawCanvas() {
		VdpSprite[] sprites = spriteCanvas.getSprites();
		for (VdpSprite sprite : sprites) {
			sprite.setBitmapDirty(true);
		}
	}
}
