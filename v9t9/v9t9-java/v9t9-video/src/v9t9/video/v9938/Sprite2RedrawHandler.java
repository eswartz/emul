/*
  Sprite2RedrawHandler.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.video.v9938;

import java.util.Arrays;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.VdpSprite;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;
import v9t9.video.tms9918a.SpriteRedrawHandler;

import static v9t9.common.hardware.VdpV9938Consts.*;

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
	/** Cache the offset of each sprite's color pattern in memory
	 * to avoid recreating the VDP memory accessors
	 */
	protected int[] sprcolOffsMap = new int[32];
	{
		Arrays.fill(sprcolOffsMap, -1);
	}


	public Sprite2RedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

	}

	@Override
	protected void init() {
		info.touch.sprite = modify_sprite_default;
		info.touch.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSprite2Canvas(info.canvas, 8,  info.vdp.getModeNumber() == MODE_GRAPHICS5);
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
	 * <p>T
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
		boolean isMag = false;
		int size = 8;
		int numchars = 1;
		switch (info.vdpregs[1] & (R1_SPRMAG + R1_SPR4)) {
		case 0:
			size = 8;
			numchars = 1;
			isMag = false;
			break;
		case R1_SPRMAG:
			size = 16;
			numchars = 1;
			isMag = true;
			break;
		case R1_SPR4:
			size = 16;
			numchars = 4;
			isMag = false;
			break;
		case R1_SPR4 + R1_SPRMAG:
			size = 32;
			numchars = 4;
			isMag = true;
			break;
		}
		
		int sprbase = modeInfo.sprite.base;
		int sprpatbase = modeInfo.sprpat.base;
		
		ByteMemoryAccess access = info.vdp.getByteReadMemoryAccess(sprbase);
		ByteMemoryAccess colorAccess = info.vdp.getByteReadMemoryAccess((sprbase - 0x200) & 0x1ffff);
		
		spriteCanvas.setNumSpriteChars(numchars);
		spriteCanvas.setMagnified(isMag);
		
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
				sprite.move(x, (y + 1) & 0xff);
				int patOffs = sprpatbase + (ch << 3);
				if (sprpatOffsMap[i] != patOffs) {
					sprite.setPattern(info.vdp.getByteReadMemoryAccess(patOffs));
					sprpatOffsMap[i] = patOffs;
				}

				if (sprcolOffsMap[i] != colorAccess.offset) {
					ByteMemoryAccess colorStripe = new ByteMemoryAccess(colorAccess);
					sprite.setColorStripe(colorStripe);
					sprcolOffsMap[i] = colorAccess.offset;
				}
				
				int sizeX = size;
				int sizeY = size;
				int origClock = -1;
				
				// a sprite counts as double-wide if it has two
				// different early clock settings
				for (int offs = 0; offs < 16; offs++) {
					byte attr = colorAccess.memory[colorAccess.offset + offs];
					int clock = VdpSprite2Canvas.EARLY && (attr & 0x80) != 0 ? -32 : 0;
					if (origClock == -1)
						origClock = clock;
					else if (clock != origClock) {
						if (clock < origClock)
							origClock = clock;
						sizeX += 32;
						break;
					}
				}
				sprite.setShift(origClock);
				sprite.setSize(sizeX, sizeY);
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
					& ~(VDP_FIVE_SPRITES | VDP_FIFTH_SPRITE) 
					| (VDP_FIVE_SPRITES | nth_sprite));
		} else {
			vdpStatus &= ~(VDP_FIVE_SPRITES | VDP_FIFTH_SPRITE);
		}


		return vdpStatus;
	}

}
