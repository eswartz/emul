/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpSprite;
import v9t9.emulator.clients.builtin.video.tms9918a.SpriteRedrawHandler;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Sprite mode 2
 * <p>
 * -- Separate color table -- 512 bytes before sprite attribute table
 * -- Color in sprite is ignored
 * -- 16 colors per sprite, one per line
 * -- EC | CC | IC | 0 | color
 * -- early clock, priority enable, collision detect
 * @author ejs
 * 
 */
public class Sprite2RedrawHandler extends SpriteRedrawHandler {

	public Sprite2RedrawHandler(byte[] vdpregs, VdpHandler vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas, modeInfo);
	}

	@Override
	protected void init() {
		vdpTouchBlock.sprite = modify_sprite_default;
		vdpTouchBlock.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSprite2Canvas(vdpCanvas, 8);
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		// sprite color table
		int sprcolbase = (vdpModeInfo.sprite.base - 0x200) & 0x1ffff;
		if (sprcolbase <= addr
				&& addr < vdpModeInfo.sprite.base) {
			
			vdpChanges.sprite |= (1<< ((addr - sprcolbase) >> 4));
			vdpchanged = 1;
			
			visible = true;
		}

		return super.touch(addr) || visible;
	}

	/**
	 * This function does the mammoth tasks of:
	 * 
	 * <p>
	 * 0) Change sprites under whom chars changed
	 * <p>
	 * 1) See if sprite pattern change forces a sprite update.
	 * <p>
	 * 2) See if position change forces a sprite update.
	 * <p>
	 * 3) See if deleted-ptr changes, forcing sprite updates.
	 * <p>
	 * 4) Update vdp_changes.screen where sprites dance.
	 * <p>
	 * 5) Force updates to sprites under changed ones.
	 * <p>
	 * 6) Weed out blank sprites.
	 * <p>
	 * 7) Set VDP status flags for coincidence and N sprites on a line
	 * (hackish)
	 * @param forceRedraw 
	 * 
	 * @return the updated VDP status bits
	 */
	public byte updateSpriteCoverage(byte vdpStatus, boolean forceRedraw) {
		
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
		ByteMemoryAccess colorAccess = vdpMemory.getByteReadMemoryAccess((sprbase - 0x200) & 0x1ffff);
		
		boolean deleted = false;
		for (int i = 0; i < 32; i++) {
			VdpSprite sprite = sprites[i];
			int y = access.memory[access.offset++] & 0xff;
			int x = access.memory[access.offset++] & 0xff;
			int ch = access.memory[access.offset++] & 0xff;
			access.offset++;
			
			if (y == 0xd8) {	// 216
				deleted = true;
			}
			if (deleted) {
				sprite.setDeleted(true);
			} else {
				sprite.setDeleted(false);
				sprite.move(x, y);
				sprite.setPattern(vdpMemory.getByteReadMemoryAccess(sprpatbase + ((ch & 0xfc) << 3)));
				sprite.setSize(size);
				sprite.setNumchars(numchars);
				sprite.setColorStripe(new ByteMemoryAccess(colorAccess));
				// also check whether the pattern content changed
				if (vdpChanges.sprpat[ch] != 0)
					sprite.setBitmapDirty(true);
			}
			
			colorAccess.offset += 16;
		}

		// TODO: move the VDP status logic
		int nth_sprite = spriteCanvas.updateSpriteCoverage(vdpCanvas, vdpChanges.screen, forceRedraw);

		if (nth_sprite != -1) {
			vdpStatus = (byte) (vdpStatus
					& ~(VdpTMS9918A.VDP_FIVE_SPRITES | VdpTMS9918A.VDP_FIFTH_SPRITE) 
					| (VdpTMS9918A.VDP_FIVE_SPRITES | nth_sprite));
		} else {
			vdpStatus &= ~(VdpTMS9918A.VDP_FIVE_SPRITES | VdpTMS9918A.VDP_FIFTH_SPRITE);
		}


		return vdpStatus;
	}

}
