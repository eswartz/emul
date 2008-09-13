/**
 * 
 */
package v9t9.tests;


import junit.framework.TestCase;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.engine.memory.ByteMemoryAccess;


/**
 * @author ejs
 *
 */
public abstract class TestVideoSpriteSpeedBase extends TestCase {

	protected VdpCanvas canvas;
	interface ITimeable {
		void run(int iter) throws Exception;
	}
	protected VideoRenderer videoRenderer;

	abstract protected VideoRenderer createVideoRenderer();
	
	protected void time(String label, int iterations, ITimeable timeable) throws Exception {
		long start = System.currentTimeMillis();
		int cnt = 0;
		while (cnt++ < iterations) {
			timeable.run(cnt);
		}
		long end = System.currentTimeMillis();
		System.out.println(label + ": " + iterations + " iterations took " + (end-start) + " ms @ "
					+ (double)iterations * 1000.0 / (end-start)  + " per second");
	}
	
	protected void updateAllAndWait() {
		videoRenderer.redraw();
		videoRenderer.sync();
		handleEvents();
	}

	abstract protected void handleEvents();

	protected void updateAndWait(RedrawBlock[] blocks, int count) {
		videoRenderer.updateList(blocks, count);
		videoRenderer.sync();
		handleEvents();
	}
	
	final int COUNT = 500;

	final static byte[] fuzzy = new byte[] { (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55 };
	final static ByteMemoryAccess fuzzyPattern = new ByteMemoryAccess(fuzzy, 0);
	final static byte[] diamondBytes= {
			0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, (byte) 0xff,
			 (byte) 0xff,0x7f,0x3f, 0x1f,0x0f,0x07, 0x03,  0x01,  
			 (byte) 0x80, (byte) 0xc0, (byte) 0xe0, (byte) 0xf0, (byte) 0xf8, (byte) 0xfc, (byte) 0xfe, (byte) 0xff,
			 (byte) 0xff,(byte) 0xfe,(byte) 0xfc, (byte) 0xf8, (byte) 0xf0, (byte) 0xe0, (byte) 0xc0, (byte) 0x80,  
		};
	final static ByteMemoryAccess diamondPattern = new ByteMemoryAccess(diamondBytes, 0);
	private void runSpriteTest(String label, int size, int numchars, final int shift, ByteMemoryAccess pattern, int count) throws Exception {
		final VdpSpriteCanvas sprCanvas = new VdpSpriteCanvas(canvas);
		final double[] xs = new double[32]; 
		final double[] ys = new double[32];
		final double[] dxs = new double[32];
		final double[] dys = new double[32];
		for (int n = 0; n < 32; n++) {
			dxs[n] = Math.cos(n / 32.0 * 2 * Math.PI) / 4.;
			dys[n] = Math.sin(n / 32.0 * 2 * Math.PI) / 16.;
			xs[n] = 128;
			ys[n] = 96;
			VdpSprite sprite = sprCanvas.getSprites()[n];
			sprite.setDeleted(false);
			sprite.setSize(size);
			sprite.setShift(shift);
			sprite.setColor(n & 15);
			sprite.setNumchars(numchars);
			sprite.setPattern(pattern);
		}
		final byte[] screenChanges = new byte[768];
		final RedrawBlock[] blocks = new RedrawBlock[768];
		for (int i = 0; i < 768; i++) {
			blocks[i] = new RedrawBlock();
			blocks[i].w = 8;
			blocks[i].h = 8;
			
		}
		
		for (int zoom = 1; zoom <= 2; zoom++) {
			videoRenderer.setZoom(zoom);
			time("sprite drawing " + label + " zoom " + zoom, count, new ITimeable() {
	
				public void run(int iter) throws Exception {
					for (int n = 0; n < 32; n++) {
						SpriteBase sprite = sprCanvas.getSprites()[n];
						xs[n] = 128 + dxs[n] * iter;
						ys[n] = 96 + dys[n] * iter;
						sprite.move(((int) xs[n]) & 0xff, ((int) ys[n]) & 0xff);
					}
					
					sprCanvas.updateSpriteCoverage(screenChanges);
					int count = 0;
					for (int i = 0; i < 768; i++) {
						if (screenChanges[i] != 0) {
							RedrawBlock block = blocks[count++];
							block.r = 8*(i / 32);
							block.c = 8*(i % 32);
							canvas.draw8x8TwoColorBlock(block.r, block.c, fuzzyPattern, 
									(byte)7, (byte)7);
						}
					}
					
					sprCanvas.drawSprites();
					
					updateAndWait(blocks, count);
				}
				
			});
		}
		
	}

	public void testSpriteDrawMagnified16x16() throws Exception {
		runSpriteTest("mag 16x16", 32, 4, 0, diamondPattern, 1500);
	}
	public void testSpriteDrawMagnified8x8() throws Exception {
		runSpriteTest("mag 8x8", 16, 1, 0, fuzzyPattern, 1500);
	}

	public void testSpriteDrawUnmagnified8x8() throws Exception {
		runSpriteTest("unmag 8x8", 8, 1, 0, fuzzyPattern, 700);
	}
	public void testSpriteDrawUnmagnified8x8Early() throws Exception {
		runSpriteTest("unmag 8x8", 8, 1, -32, fuzzyPattern, 700);
	}

	public void testSpriteDrawUnmagnified16x16() throws Exception {
		runSpriteTest("unmag 16x16", 16, 4, 0, diamondPattern, 700);
	}

	public void testSpriteDrawUnmagnified16x16Early() throws Exception {
		runSpriteTest("unmag 16x16", 16, 4, -32, diamondPattern, 700);
	}

}
