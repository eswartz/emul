/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.MemoryCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpSpriteCanvas;

/**
 * For Sprite 2 mode, we use a different strategy for managing sprites.
 * <p>
 * Sprite 2 allows for "canceling priority" on sprites, which effectively
 * means allowing them to OR with each other and not detect collisions.
 * <p>
 * The base {@link VdpSpriteCanvas} will draw sprites directly to the
 * final screen canvas, meaning we cannot do such OR'ing because we can't
 * distinguish existing screen pixels from sprite pixels (to avoid
 * OR'ing with the background).  Not to mention, the format of the 
 * screen canvas may have lost the original color code information.
 * Similarly, this makes collision detection
 * difficult.
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
 * over it is dirty -- but it does mean that sprite's corresponding block
 * must be reblitted.  The other dirtying changes (sprite movement -> screen
 * change) still apply, though.
 * @author ejs
 *
 */
public class VdpSprite2Canvas extends VdpSpriteCanvas {

	private MemoryCanvas spriteCanvas;
	/** which screen changes there were, requiring sprite reblits */
	private byte[] screenSpriteChanges;

	public VdpSprite2Canvas(VdpCanvas canvas, int maxPerLine) {
		super(canvas, maxPerLine);
		this.spriteCanvas = new MemoryCanvas();
		spriteCanvas.setClearColor(0);
		spriteCanvas.setSize(256, canvas.getHeight());
		screenSpriteChanges = null;
	}
	
	@Override
	protected void updateSpriteBitmapForScreenChanges(VdpCanvas screenCanvas,
			byte[] screenChanges) {
		screenSpriteChanges = screenChanges;
		
		// no changes to screen can affect sprites here
	}

	@Override
	public void drawSprites(VdpCanvas canvas) {
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
		super.drawSprites(spriteCanvas);
		blitSpriteCanvas(canvas);
	}

	protected void blitSpriteCanvas(VdpCanvas screenCanvas) {
		// where the screen changed, we need to draw our sprite blocks
		int blockStride = screenCanvas.getWidth() / 8;
		int blockMag = blockStride / 32;
		int blockCount = 32 * screenCanvas.getHeight() / 8;
		int screenOffs = 0;
		//RedrawBlock[] blocks = new RedrawBlock[screenCanvas.getBlockCount()];
		//int blockidx = 0;
		for (int i = 0; i < blockCount; i += 32) {
			for (int j = 0; j < 32; j++) {
				if (screenSpriteChanges[(i + j) * blockMag] != 0 
					|| (blockMag > 1 && screenSpriteChanges[(i + j) * blockMag + 1] != 0)) {
					/*
					blocks[blockidx++] = block;
					block.r = i / 32 * 8;
					block.c = j * blockMag;
					block.w = blockMag * 8;
					block.h = 8;
					 */
					screenCanvas.blitSpriteBlock(spriteCanvas, 
							j * 8, i / 32 * 8, blockMag);
				}
			}
			screenOffs += blockStride;
		}
		//System.out.println(blockidx + " dirty from sprites");
		//screenCanvas.markDirty(blocks, blockidx);
	}
}