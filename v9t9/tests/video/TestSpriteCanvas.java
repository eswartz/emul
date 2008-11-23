/**
 * 
 */
package v9t9.tests.video;


import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpSprite;
import v9t9.emulator.clients.builtin.video.VdpSpriteCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.engine.memory.ByteMemoryAccess;


/**
 * @author ejs
 *
 */
public class TestSpriteCanvas extends TestCase {

	protected VdpCanvas canvas;
	interface ITimeable {
		void run() throws Exception;
	}
	protected VideoRenderer videoRenderer;

	protected VideoRenderer createVideoRenderer() {
		return new SwtVideoRenderer(display, new ImageDataCanvas24Bit());
	}
	
	protected Display display;
	private VdpSpriteCanvas sprCanvas;
	private RedrawBlock[] redrawBlocks;
	private VdpSprite[] sprites;
	private byte[] screenChanges;
	
	@Override
	protected void setUp() throws Exception {
		display = new Display();
		handleEvents();
		
		videoRenderer = createVideoRenderer();
		canvas = videoRenderer.getCanvas();
		videoRenderer.setZoom(2);
		
		sprCanvas = new VdpSpriteCanvas(canvas);
		sprites = sprCanvas.getSprites();
		
		redrawBlocks = new RedrawBlock[768];
		for (int i = 0; i < 768; i++) {
			redrawBlocks[i] = new RedrawBlock();
			redrawBlocks[i].w = 8;
			redrawBlocks[i].h = 8;
		}

		screenChanges = new byte[768];
	}

	private void setupSprites(int size, int numchars, ByteMemoryAccess pattern) {
		for (int n = 0; n < 32; n++) {
			VdpSprite sprite = sprites[n];
			sprite.setSize(size);
			sprite.setColor(n & 15);
			sprite.setNumchars(numchars);
			sprite.setPattern(pattern);
		}
		
	}

	@Override
	protected void tearDown() throws Exception {
		handleEvents();
		if (!display.isDisposed()) display.dispose();
	}
	
	protected void handleEvents() {
		while (display.readAndDispatch()) /**/ ;
	}
	
	protected void updateAllAndWait() {
		videoRenderer.redraw();
		videoRenderer.sync();
		handleEvents();
	}

	protected void updateAndWait(RedrawBlock[] blocks, int count) {
		canvas.markDirty(blocks, count);
		videoRenderer.sync();
		handleEvents();
	}
	
	final static byte[] fuzzy = new byte[] { (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55 };
	final static ByteMemoryAccess fuzzyPattern = new ByteMemoryAccess(fuzzy, 0);
	
	final static byte[] diamondBytes= {
			0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, (byte) 0xff,
			 (byte) 0xff,0x7f,0x3f, 0x1f,0x0f,0x07, 0x03,  0x01,  
			 0x08, 0x0c, 0x0e, 0x0f, (byte) 0xf8, (byte) 0xfc, (byte) 0xfe, (byte) 0xff,
			 (byte) 0xff,(byte) 0xfe,(byte) 0xfc, (byte) 0xf8, 0x0f, 0x0e, 0x0c, 0x08,  
		};
	final static ByteMemoryAccess diamondPattern = new ByteMemoryAccess(diamondBytes, 0);
	
	public void testNoSprites() throws Exception {
		setupSprites(0, 0, null);
		
		clearChanges();
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertDirty(new int[] { });
	}

	private void assertDirty(int[] changes) {
		for (int i = 0; i < changes.length; i++) {
			if (screenChanges[changes[i]] == 0)
				fail("expected dirty " + changes[i]);
		}
		for (int i = 0; i < 768; i++) {
			if (screenChanges[i] != 0) {
				boolean found = false;
				for (int j = 0; !found && j < changes.length; j++)
					if (changes[j] == i)
						found = true;
			
				if (!found)
						fail("did not expect dirty block " + i);
			}
		}
	}

	public void testAllDeletedSprites() throws Exception {
		setupSprites(8, 1, fuzzyPattern);
		for (int i = 0; i < 32; i++)
			sprites[i].setDeleted(true);
		
		clearChanges();
		sprCanvas.updateSpriteCoverage(screenChanges);
		
		assertDirty(new int[] { });
	}
	public void testOneSprite() throws Exception {
		setupSprites(8, 1, fuzzyPattern);
		for (int i = 1; i < 32; i++)
			sprites[i].setDeleted(true);
		
		clearChanges();
		sprites[0].move(8, 8);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 1 * 32 + 1});
		
		// move
		clearChanges();
		sprites[0].move(0, 0);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 1 * 32 + 1, 0 * 32 + 0 });

		// delete
		clearChanges();
		sprites[0].setDeleted(true);
		sprites[0].move(80, 80);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 0 * 32 + 0 });

		// move
		clearChanges();
		sprites[0].move(8, 8);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { });
		
		// undelete
		clearChanges();
		sprites[0].setDeleted(false);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 1 * 32 + 1 });
	}

	private void clearChanges() {
		Arrays.fill(screenChanges, 0, screenChanges.length, (byte)0);
	}

	public void testOneSpriteCrossing() throws Exception {
		setupSprites(8, 1, fuzzyPattern);
		for (int i = 1; i < 32; i++)
			sprites[i].setDeleted(true);
		
		// straddles 4 blocks
		clearChanges();
		sprites[0].move(4, 4);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 0 * 32 + 0, 0 * 32 + 1, 1 * 32 + 0, 1 * 32 + 1 });

		// move into block; will redraw 3 blocks we exited and the new block
		clearChanges();
		sprites[0].move(8, 8);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 0 * 32 + 0, 0 * 32 + 1, 1 * 32 + 0, 1 * 32 + 1 });

		// no movement -> no changes
		clearChanges();
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertFalse(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { });

	}


	public void testChangeBlockUnderSprite() throws Exception {
		setupSprites(16, 1, fuzzyPattern);
		for (int i = 1; i < 32; i++)
			sprites[i].setDeleted(true);
		
		// in one block
		clearChanges();
		sprites[0].move(128, 96);
		sprCanvas.updateSpriteCoverage(screenChanges);
		sprCanvas.drawSprites();
		assertDirty(new int[] { 12 * 32 + 16, 12 * 32 + 17, 13 * 32 + 16, 13 * 32 + 17 });

		// screen changes underneath; full sprite redrawn
		clearChanges();
		assertFalse(sprites[0].isBitmapDirty());
		screenChanges[12 * 32 + 16] = 1;
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 12 * 32 + 16, 12 * 32 + 17, 13 * 32 + 16, 13 * 32 + 17 });

	}

	public void testCascadingSpriteUpdate() throws Exception {
		setupSprites(8, 1, fuzzyPattern);
		
		for (int n = 0; n < 32; n++) {
			if (n < 4) {
				sprites[n].move(128 + n * 4, 96 + n * 4);
				sprites[n].setDeleted(false);
			} else {
				sprites[n].setDeleted(true);
			}
		}
		
		// first time, all will be redrawn
		clearChanges();
		sprites[0].move(128, 96);
		sprCanvas.updateSpriteCoverage(screenChanges);
		sprCanvas.drawSprites();
		assertDirty(new int[] { 
				12 * 32 + 16, // 0, 1 
				12 * 32 + 17, // 1
				13 * 32 + 16, // 1
				13 * 32 + 17, // 1, 2, 3
				13 * 32 + 18, // 3
				14 * 32 + 17, // 3
				14 * 32 + 18, // 3
			});

		// now, move sprite 3: sprites 1 and 2 (due to sharing blocks)
		// and 0 (due to sharing blocks with 1) should also be redrawn
		clearChanges();
		sprites[3].setBitmapDirty(true);
		sprCanvas.updateSpriteCoverage(screenChanges);
		assertTrue(sprites[1].isBitmapDirty());
		assertTrue(sprites[2].isBitmapDirty());
		assertTrue(sprites[0].isBitmapDirty());
		sprCanvas.drawSprites();
		assertDirty(new int[] { 
				12 * 32 + 16, // 0, 1 
				12 * 32 + 17, // 1
				13 * 32 + 16, // 1
				13 * 32 + 17, // 1, 2, 3
				13 * 32 + 18, // 3
				14 * 32 + 17, // 3
				14 * 32 + 18, // 3
			});

	}

}
