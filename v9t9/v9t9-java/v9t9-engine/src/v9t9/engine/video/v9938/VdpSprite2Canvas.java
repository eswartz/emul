/**
 * 
 */
package v9t9.engine.video.v9938;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ICanvas;
import v9t9.common.video.ISprite2Canvas;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpSprite;
import v9t9.engine.video.Sprite2Canvas;
import v9t9.engine.video.tms9918a.VdpSpriteCanvas;

/**
 * For Sprite 2 mode, we use a different strategy for managing sprites.
 * <p>
 * First, the "color + early clock" byte in the sprite attribute table
 * is not used.
 * <p>
 * Instead, a sprite color table is implicitly located
 * 512 bytes in front of the sprite attribute table.  Each entry is 16
 * bytes long and specifies attributes for each (non-magified) row of the sprite.
 * The color and early clock are moved here. 
 * A new flag allows for "canceling priority" on sprites, which effectively
 * means allowing them to OR with each other and not detect collisions.
 * <p>
 * The base {@link VdpSpriteCanvas} will draw sprites directly to the
 * final screen canvas, meaning we cannot do such OR'ing because we can't
 * distinguish existing screen pixels from sprite pixels (to avoid
 * OR'ing with the background).  Not to mention, the format of the 
 * screen canvas may have lost the original color code information.
 * Similarly, this makes collision detection difficult.
 * <p>
 * This sprite canvas, then, maintains its own independent rendering canvas
 * for the sprites.  All sprites' dirtyings and drawings are performed on
 * a static 256xN bitmap, where we can track the pixels properly.
 * <p>
 * In the final "drawing" call, we draw the sprites into their own canvas
 * and then blit this on top of the screen canvas.
 * <p>
 * This approach requires that we modify our interpretation of how dirtying
 * interacts.  Now, when a screen block changes, this doesn't mean the sprite
 * under it is dirty -- but it does mean that sprite's corresponding block
 * must be reblitted.  The other dirtying changes (sprite movement -> screen
 * change) still apply, though.
 * @author ejs
 *
 */
public class VdpSprite2Canvas extends VdpSpriteCanvas {

	public static boolean EARLY = true;
	public static boolean OR = true;
	
	private ISprite2Canvas spriteCanvas;
	/** which screen changes there were, requiring sprite reblits */
	private byte[] screenSpriteChanges;
	private final boolean evenOddColors;

	public VdpSprite2Canvas(IVdpCanvas canvas, int maxPerLine, boolean evenOddColors) {
		super(canvas, maxPerLine);
		this.evenOddColors = evenOddColors;
		this.spriteCanvas = new Sprite2Canvas();
		spriteCanvas.setClearColor(0);
		spriteCanvas.setSize(256, canvas.getHeight());
		screenSpriteChanges = null;
	}
	
	@Override
	protected void updateSpriteBitmapForScreenChanges(ICanvas screenCanvas,
			byte[] screenChanges) {
		screenSpriteChanges = screenChanges;
		// no changes to screen can affect sprites here
	}

	@Override
	public void drawSprites(IVdpCanvas canvas) {
		//spriteCanvas.clear(null);
		// clear the blocks where the sprites are moving
		//int cleared = 0;
		for (int i = 0; i < spritebitmap.length; i++) {
			if ((spritebitmap[i] & knowndirty) != 0) {
				int offset = spriteCanvas.getBitmapOffset(i % 32 * 8, i / 32 * 8);
				spriteCanvas.clear8x8Block(offset);
				//cleared++;
			}
		}
		//System.out.print(cleared +" cleared; ");
		super.drawSprites(canvas);
		
		blitSpriteCanvas(canvas, evenOddColors);
	}

	
	/**
	 * Draws an 8x8 sprite character
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param attr the row attribute table
	 * @param doubleWidth is the sprite drawn double-wide?
	 */
	private void drawUnmagnifiedSpriteChar(IVdpCanvas canvas, int y, int x, int rowbitmap, ByteMemoryAccess pattern,
			ByteMemoryAccess attr, boolean doubleWidth) {
		
		VdpColorManager colorMgr = canvas.getColorMgr();
		
		int pixy = 0;
		for (int yy = 0; yy < 8; yy++) {
			if (y >= canvas.getHeight())
				continue;
			
			byte attrb = attr.memory[attr.offset + yy];
			int shift = EARLY && (attrb & 0x80) != 0 ? -32 : 0 ;
			byte color = (byte) (attrb & 0xf);
			if (color == 0 && !colorMgr.isClearFromPalette())
				continue;
			
			byte bitmask = -1;
			if (x + shift + 8 <= 0)
				continue;
			if (x + shift < 0) {
				bitmask &= 0xff >> -(x + shift);
			} else if (x + shift + 8 > 256) {
				bitmask &= 0xff << ((x + shift + 8) - 256);
			}
			
			boolean isLogicalOr = OR && (attrb & 0x40) != 0;
			byte patt = 0;
			if ((rowbitmap & (1 << pixy)) != 0) {
				patt = pattern.memory[pattern.offset + yy];
				if (patt != 0) {
					int theX = (x + shift) * (doubleWidth ? 2 : 1);
					if (doubleWidth)
						canvas.drawEightMagnifiedSpritePixels(
								theX, y, patt, color,
								bitmask, isLogicalOr);
					else
						canvas.drawEightSpritePixels(
								theX, y, patt, color, 
								bitmask, isLogicalOr);
				}
				pixy++;
				y = (y + 1) & 0xff;
			}
		}
	}


	/**
	 * Draws an 8x8 sprite character
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param attr the row attribute table
	 * @param doubleWidth is the sprite drawn double-wide?
	 */
	private void drawMagnifiedSpriteChar(IVdpCanvas canvas, int y, int x, int rowbitmap, ByteMemoryAccess pattern,
			ByteMemoryAccess attr, boolean doubleWidth) {
		
		VdpColorManager colorMgr = canvas.getColorMgr();
		
		int pixy = 0;
		for (int yy = 0; yy < 8; yy++) {
			if (y >= canvas.getHeight())
				continue;
			
			byte attrb = attr.memory[attr.offset + yy];
			int shift = EARLY && (attrb & 0x80) != 0 ? -32 : 0;
			byte color = (byte) (attrb & 0xf);
			if (color == 0 && !colorMgr.isClearFromPalette())
				continue;
			
			short bitmask = -1;
			if (x + shift + 16 <= 0)
				continue;
			if (x + shift < 0) {
				bitmask &= 0xffff >> -(x + shift);
			} else if (x + shift + 16 > 256) {
				bitmask &= 0xffff << ((x + shift + 16) - 256);
			}
			
			boolean isLogicalOr = OR && (attrb & 0x40) != 0;
			byte patt = 0;
			for (int iy = 0; iy < 2; iy++) {
				if ((rowbitmap & (1 << pixy)) != 0) {
					patt = pattern.memory[pattern.offset + yy];
					if (patt != 0) {
						int theX = (x + shift) * (doubleWidth ? 2 : 1);
						if (doubleWidth)
							canvas.drawEightDoubleMagnifiedSpritePixels(
									theX, y, patt, color,
									bitmask, isLogicalOr);
						else
							canvas.drawEightMagnifiedSpritePixels(
									theX, y, patt, color, 
									bitmask, isLogicalOr);
					}
				}
				pixy++;
				y = (y + 1) & 0xff;
			}
		}
	}

	@Override
	protected void drawSprite(IVdpCanvas canvas, VdpSprite sprite, int sprrowbitmap) {
		// sprite color 0 does not imply invisibility since this only
		// applies to the color 0 in the color stripe
		
		boolean doubleWidth = canvas.getWidth() == 512;
		
		int x = sprite.getX();
		int y = sprite.getY();
		ByteMemoryAccess tmpPattern = new ByteMemoryAccess(sprite.getPattern());
		
		for (int c = 0; c < getNumSpriteChars(); c++) {
			int rowshift = charshifts[c*2];
			int colshift = charshifts[c*2+1];
			ByteMemoryAccess colors = new ByteMemoryAccess(sprite.getColorStripe());
			colors.offset += rowshift;
			if (isMagnified())
				drawMagnifiedSpriteChar(canvas, y + rowshift * 2, x + colshift * 2, 
						sprrowbitmap >> (rowshift * 2), tmpPattern, colors,
						doubleWidth);
			else
				drawUnmagnifiedSpriteChar(canvas, y + rowshift, x + colshift, 
					sprrowbitmap >> rowshift, tmpPattern, colors,
					doubleWidth);
			tmpPattern.offset += 8;
		}
	}
	
	protected void blitSpriteCanvas(IVdpCanvas canvas, boolean fourColorMode) {
		// where the screen changed, we need to draw our sprite blocks
		int blockStride = canvas.getVisibleWidth() / 8;
		// 1 or 2 if 256 or 512 mode
		int blockMag = blockStride / 32;
		int blockCount = canvas.getHeight() / 8;
		int screenOffs = 0;
		for (int yblock = 0; yblock < blockCount; yblock++) {
			for (int xblock = 0; xblock < 32; xblock++) {
				if (screenSpriteChanges[screenOffs + xblock * blockMag] != 0 
					|| (blockMag > 1 && screenSpriteChanges[screenOffs + xblock * blockMag + 1] != 0)) {
					
					if (!fourColorMode)
						canvas.blitSpriteBlock(spriteCanvas, xblock * 8, yblock * 8, blockMag);
					else
						canvas.blitFourColorSpriteBlock(spriteCanvas, xblock * 8, yblock * 8, blockMag);
				}
			}
			screenOffs += blockStride;
		}
	}
}