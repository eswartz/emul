/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpRedrawInfo;
import v9t9.emulator.clients.builtin.video.VdpSprite;
import v9t9.emulator.clients.builtin.video.tms9918a.SpriteRedrawHandler;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
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

	public Sprite2RedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

	}

	@Override
	protected void init() {
		info.touch.sprite = modify_sprite_default;
		info.touch.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSprite2Canvas(info.canvas, 8, (((VdpV9938)info.vdp).getModeNumber() == VdpV9938.MODE_GRAPHICS5));
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		// sprite color table
		int sprcolbase = (modeInfo.sprite.base - 0x200) & 0x1ffff;
		if (sprcolbase <= addr
				&& addr < modeInfo.sprite.base) {
			
			info.changes.sprite |= (1<< ((addr - sprcolbase) >> 4));
			info.changes.changed = true;
			
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
		switch (info.vdpregs[1] & (VdpTMS9918A.R1_SPRMAG + VdpTMS9918A.R1_SPR4)) {
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
		
		int sprbase = modeInfo.sprite.base;
		int sprpatbase = modeInfo.sprpat.base;
		
		ByteMemoryAccess access = info.vdp.getByteReadMemoryAccess(sprbase);
		ByteMemoryAccess colorAccess = info.vdp.getByteReadMemoryAccess((sprbase - 0x200) & 0x1ffff);
		
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
				sprite.setPattern(info.vdp.getByteReadMemoryAccess(sprpatbase + ((ch & 0xfc) << 3)));
				
				ByteMemoryAccess colorStripe = new ByteMemoryAccess(colorAccess);
				sprite.setColorStripe(colorStripe);
				
				int sizeX = size;
				int sizeY = size;
				int origClock = -1;
				
				// a sprite counts as double-wide if it has 
				for (int offs = 0; offs < 16; offs++) {
					byte attr =colorStripe.memory[colorStripe.offset + offs];
					int clock = (attr & 0x80) != 0 ? -32 : 0;
					if (origClock == -1)
						origClock = clock;
					else if (clock != origClock) {
						if (clock < origClock)
							origClock = clock;
						sizeX *= 2;
						break;
					}
				}
				sprite.setShift(origClock);
				sprite.setSize(sizeX, sizeY);
				sprite.setNumchars(numchars);
				// also check whether the pattern content changed
				if (info.changes.sprpat[ch] != 0)
					sprite.setBitmapDirty(true);
			}
			
			colorAccess.offset += 16;
		}

		// TODO: move the VDP status logic
		int nth_sprite = spriteCanvas.updateSpriteCoverage(info.canvas, info.changes.screen, forceRedraw);

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
