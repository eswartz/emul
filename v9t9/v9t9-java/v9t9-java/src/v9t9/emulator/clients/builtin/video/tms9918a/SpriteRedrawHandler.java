/**
 * 
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BaseRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpRedrawInfo;
import v9t9.emulator.clients.builtin.video.VdpSprite;
import v9t9.emulator.clients.builtin.video.VdpTouchHandler;
import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class manages sprite redraws by maintaining a separate transparent
 * canvas for sprite graphics.  This canvas is blitted on top of the "normal" VDP
 * canvas ("background") for the current mode to create the full graphics + sprite
 * appearance of the screen.
 * 
 * The advantages of this are efficiency of redrawing.
 * 
 * 1) Changes in the background do not affect the sprite redraw -- when such
 * blocks are changed normally, then the correspoding blocks from the sprite
 * canvas are merely reblitted on top. 
 *
 * 2) Appropriate video renderers can avoid reblitting the background
 * when sprites change.  Otherwise, changes in sprites require redraws of
 * blocks in the background before the old sprite blocks and the new
 * sprite blocks are reblitted.
 * @author ejs
 * 
 */
public class SpriteRedrawHandler extends BaseRedrawHandler {

	protected VdpTouchHandler modify_sprite_default = new VdpTouchHandler() {

		public void modify(int offs) {
			info.changes.sprite |= (1<<(offs >> 2));
			info.changes.changed = true;
		}

	};
	protected VdpTouchHandler modify_sprpat_default = new VdpTouchHandler() {

		public void modify(int offs) {
			int patt;

			if ((info.vdpregs[1] & VdpTMS9918A.R1_SPR4) != 0) {
				patt = (offs >> 3) & 0xfc;
				Arrays.fill(info.changes.sprpat, patt, patt + 4, (byte) 1);
			} else {
				patt = offs >> 3;
				info.changes.sprpat[patt] = 1;
			}

			info.changes.changed = true;
		}

	};

	protected VdpSpriteCanvas spriteCanvas;
	
	/** Cache the offset of each sprite's pattern in memory
	 * to avoid recreating the VDP memory accessors
	 */
	protected int[] sprpatOffsMap = new int[32];
	{
		Arrays.fill(sprpatOffsMap, -1);
	}

	public SpriteRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);

		init();
	}

	protected void init() {
		info.touch.sprite = modify_sprite_default;
		info.touch.sprpat = modify_sprpat_default;
		
		spriteCanvas = new VdpSpriteCanvas(info.canvas, 4);
		
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		if (modeInfo.sprite.base <= addr
				&& addr < modeInfo.sprite.base + modeInfo.sprite.size) {
			info.touch.sprite.modify(addr - modeInfo.sprite.base);
			visible = true;
		}

		if (modeInfo.sprpat.base <= addr
				&& addr < modeInfo.sprpat.base + modeInfo.sprpat.size) {
			info.touch.sprpat.modify(addr - modeInfo.sprpat.base);
			visible = true;
		}

		return visible;
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
		boolean isMag = false;
		int size = 8;
		int numchars = 1;
		switch (info.vdpregs[1] & (VdpTMS9918A.R1_SPRMAG + VdpTMS9918A.R1_SPR4)) {
		case 0:
			size = 8;
			numchars = 1;
			isMag = false;
			break;
		case VdpTMS9918A.R1_SPRMAG:
			size = 16;
			numchars = 1;
			isMag = true;
			break;
		case VdpTMS9918A.R1_SPR4:
			size = 16;
			numchars = 4;
			isMag = false;
			break;
		case VdpTMS9918A.R1_SPR4 + VdpTMS9918A.R1_SPRMAG:
			size = 32;
			numchars = 4;
			isMag = true;
			break;
		}

		spriteCanvas.setNumSpriteChars(numchars);
		spriteCanvas.setMagnified(isMag);

		int sprbase = modeInfo.sprite.base;
		int sprpatbase = modeInfo.sprpat.base;
		
		ByteMemoryAccess access = info.vdp.getByteReadMemoryAccess(sprbase);
		boolean deleted = false;
		for (int i = 0; i < 32; i++) {
			VdpSprite sprite = sprites[i];
			int y = access.memory[access.offset++] & 0xff;
			int x = access.memory[access.offset++] & 0xff;
			int ch = access.memory[access.offset++] & 0xff;
			ch &= ~(numchars - 1);
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
				if ((info.vdpChanges.sprite & (1 << i)) != 0) {
					sprite.setBitmapDirty(true);
				}
				*/
				sprite.setDeleted(color == 0);
				sprite.move(x, y + 1);
				sprite.setColor(color);
				sprite.setShift(shift);
				int patOffs = sprpatbase + (ch << 3);
				if (sprpatOffsMap[i] != patOffs) {
					sprite.setPattern(info.vdp.getByteReadMemoryAccess(patOffs));
					sprpatOffsMap[i] = patOffs;
				}
				sprite.setSize(size);
				
				// also check whether the pattern content changed
				if (info.changes.sprpat[ch] != 0)
					sprite.setBitmapDirty(true);
				
				//vdpStatus |= VdpTMS9918A.VDP_COINC;
			}
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

	/**
	 * Draw the sprites
	 * @param force
	 */
	public void updateCanvas(boolean force) {
		spriteCanvas.drawSprites(info.canvas);
	}

	public void redrawCanvas() {
		VdpSprite[] sprites = spriteCanvas.getSprites();
		for (VdpSprite sprite : sprites) {
			sprite.setBitmapDirty(true);
		}
	}

	/* not done for sprites */
	public void propagateTouches() {
		
	}

	/* not done for sprites */
	public int updateCanvas(RedrawBlock[] blocks, boolean force) {
		return 0;
	}
	
}
