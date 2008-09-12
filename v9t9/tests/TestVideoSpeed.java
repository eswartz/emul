/**
 * 
 */
package v9t9.tests;

import junit.framework.TestCase;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.engine.memory.ByteMemoryAccess;


/**
 * @author ejs
 *
 */
public class TestVideoSpeed extends TestCase {

	private SwtVideoRenderer videoRenderer;
	private Display display;
	private VdpCanvas canvas;

	interface ITimeable {
		void run() throws Exception;
	}
	
	@Override
	protected void setUp() throws Exception {
		display = new Display();
		videoRenderer = new SwtVideoRenderer(display);
		videoRenderer.setBlank(false);
		videoRenderer.resize(256, 192);
		canvas = videoRenderer.getCanvas();
	}
	
	@Override
	protected void tearDown() throws Exception {
		display.dispose();
	}
	
	protected void time(String label, int iterations, ITimeable timeable) throws Exception {
		timeable.run();
		long start = System.currentTimeMillis();
		int cnt = 0;
		while (cnt++ < iterations) {
			timeable.run();
		}
		long end = System.currentTimeMillis();
		System.out.println(label + ": " + iterations + " iterations took " + (end-start) + " ms @ "
					+ (double)iterations * 1000.0 / (end-start)  + " per second");
	}
	
	protected void updateAllAndWait() {
		videoRenderer.redraw();
		videoRenderer.sync();
		while (display.readAndDispatch()) /**/ ;
	}

	protected void updateAndWait(RedrawBlock[] blocks, int count) {
		videoRenderer.updateList(blocks, count);
		videoRenderer.sync();
		while (display.readAndDispatch()) /**/ ;
	}
	
	final int COUNT = 768;

	public void _testClear() throws Exception {
		for (int zoom = 1; zoom <= 2; zoom++) {
			videoRenderer.setZoom(zoom);
			time("clearing canvas zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					canvas.clear();
					updateAllAndWait();
				}
				
			});
		}		
	}
	
	public void _testFlip() throws Exception {
		final int[] color = { 1 };
		for (int zoom = 1; zoom <= 2; zoom++) {
			videoRenderer.setZoom(zoom);
			time("flipping canvas zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					canvas.setClearColor(color[0]);
					canvas.clear();
					updateAllAndWait();
					color[0] = color[0] == 1 ? 15 : 1;
				}
				
			});
		}
	}
	final static byte[] fuzzy = new byte[] { (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55, (byte) 0xaa, 0x55 };
	final static ByteMemoryAccess fuzzyPattern = new ByteMemoryAccess(fuzzy, 0);
	
	public void testNoDraw() throws Exception {
		final RedrawBlock[] blocks = new RedrawBlock[] {  };
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			final Point pt  = new Point(0, 0);
			videoRenderer.setZoom(zoom);
			time("nothing drawn zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					updateAndWait(blocks, 0);
					pt.x += 8;
					if (pt.x >= 256) {
						colors[0] += 41;
						colors[1] += 17;
						pt.x = 0;
						pt.y += 8;
						if (pt.y >= 192)
							pt.y = 0;
					}
				}
				
			});
		}
	}
	
	public void _testFullUpdateNoDraw() throws Exception {
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			final Point pt  = new Point(0, 0);
			videoRenderer.setZoom(zoom);
			time("nothing drawn but full repaint zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					updateAllAndWait();
					pt.x += 8;
					if (pt.x >= 256) {
						colors[0] += 41;
						colors[1] += 17;
						pt.x = 0;
						pt.y += 8;
						if (pt.y >= 192)
							pt.y = 0;
					}
				}
				
			});
		}
	}
	public void testSingleCharDraw() throws Exception {
		final RedrawBlock block = new RedrawBlock();
		final RedrawBlock[] blocks = new RedrawBlock[] { block };
		block.w = 8;
		block.h = 8;
		
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			final Point pt  = new Point(0, 0);
			videoRenderer.setZoom(zoom);
			time("char-by-char 2-color fill zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					block.r = pt.y;
					block.c = pt.x;
					colors[0] += 1 + pt.x;
					colors[1] += 3 + pt.y;
					canvas.draw8x8TwoColorBlock(block.r, block.c, fuzzyPattern, 
							(byte)((colors[0]/5)&15), (byte)((colors[1]/5)&15));
					updateAndWait(blocks, 1);
					pt.x += 8;
					if (pt.x >= 256) {
						colors[0] += 41;
						colors[1] += 17;
						pt.x = 0;
						pt.y += 8;
						if (pt.y >= 192)
							pt.y = 0;
					}
				}
				
			});
		}
	}
	
	public void testRowCharDraw() throws Exception {
		final RedrawBlock[] blocks = new RedrawBlock[32];
		for (int i = 0; i < 32; i++) {
			blocks[i] = new RedrawBlock();
			blocks[i].w = 8;
			blocks[i].h = 8;
			
		}
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			final Point pt  = new Point(0, 0);
			videoRenderer.setZoom(zoom);
			time("row-by-row 2-color fill zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					pt.x = 0;
					for (int i = 0; i < 32; i++) {
						RedrawBlock block = blocks[i];
						block.r = pt.y;
						block.c = pt.x;
						colors[0] += 1 + pt.x;
						colors[1] += 3 + pt.y;
						canvas.draw8x8TwoColorBlock(block.r, block.c, fuzzyPattern, 
								(byte)((colors[0]/5)&15), (byte)((colors[1]/5)&15));
						pt.x += 8;
					}
					updateAndWait(blocks, 32);
					colors[0] += 41;
					colors[1] += 17;
					pt.x = 0;
					pt.y += 8;
					if (pt.y >= 192)
						pt.y = 0;
				}
				
			});
		}
	}
	
	public void testHalfCharDraw() throws Exception {
		final RedrawBlock[] blocks = new RedrawBlock[768];
		for (int i = 0; i < 768; i++) {
			blocks[i] = new RedrawBlock();
			blocks[i].w = 8;
			blocks[i].h = 8;
			
		}
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			videoRenderer.setZoom(zoom);
			time("half-screen 2-color fill zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					for (int i = 0; i < 768; i+=2) {
						RedrawBlock block = blocks[i/2];
						block.r = 8*(i / 32);
						block.c = 8*((i % 32) + (block.r % 16 == 0 ? 0 : 1));
						colors[0] += 1 + block.r;
						colors[1] += 3 + block.c;
						canvas.draw8x8TwoColorBlock(block.r, block.c, fuzzyPattern, 
								(byte)((colors[0]/5)&15), (byte)((colors[1]/5)&15));
					}
					updateAndWait(blocks, 384);
					colors[0] += 41;
					colors[1] += 17;
				}
				
			});
		}
	}
	
	public void testFullCharDraw() throws Exception {
		final RedrawBlock[] blocks = new RedrawBlock[768];
		for (int i = 0; i < 768; i++) {
			blocks[i] = new RedrawBlock();
			blocks[i].w = 8;
			blocks[i].h = 8;
			
		}
		final int[] colors = { 0, 0 };

		for (int zoom = 1; zoom <= 2; zoom++) {
			videoRenderer.setZoom(zoom);
			time("full-screen 2-color fill zoom " + zoom, COUNT, new ITimeable() {
	
				public void run() throws Exception {
					for (int i = 0; i < 768; i++) {
						RedrawBlock block = blocks[i];
						block.r = 8*(i / 32);
						block.c = 8*(i % 32);
						colors[0] += 1 + block.r;
						colors[1] += 3 + block.c;
						canvas.draw8x8TwoColorBlock(block.r, block.c, fuzzyPattern, 
								(byte)((colors[0]/5)&15), (byte)((colors[1]/5)&15));
					}
					updateAndWait(blocks, 768);
					colors[0] += 41;
					colors[1] += 17;
				}
				
			});
		}
	}
}
