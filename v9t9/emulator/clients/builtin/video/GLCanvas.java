/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.jni.v9t9render.SWIGTYPE_p_OpenGL;
import v9t9.jni.v9t9render.V9t9Render;

/**
 * @author ejs
 *
 */
public class GLCanvas extends ImageDataCanvas24Bit {
	SWIGTYPE_p_OpenGL ogl;
	
	
	public GLCanvas(SWIGTYPE_p_OpenGL ogl) {
		this.ogl = ogl;
	}

	protected int getNumBlocks() {
		return 32 * 27;
	}

	protected void updateBlock(int x, int y) {
		int bwidth = width / 32;
		x /= bwidth;
		y /= 8;
		int offset = getBitmapOffset(x * bwidth, y * 8);
		int block = y * 32 + x;
		if (offset >= imageData.data.length)
			return;
		
		byte[] data = new byte[bwidth * 8 * 3];
		int didx = 0;
		for (int iy = 0; iy < 8; iy++) {
			for (int ix = 0; ix < 8 * 3; ix++) {
				data[didx++] = imageData.data[offset++];
			}
			offset += getLineStride() - 8 * 3;
		}
		V9t9Render.updateBlockTexture(ogl, block, bwidth, 8, data, 0);
	}
	
	@Override
	public void clear(byte[] rgb) {
		super.clear(rgb);
		int bwidth = width / 32;
		int nblocks = getNumBlocks();
		for (int  i = 0; i < nblocks; i++) {
			int y = (i / 32) * 8;
			int x = (i % 32) * bwidth;
			updateBlock(x, y);
		}
	}

	@Override
	public void draw8x8BitmapTwoColorBlock(int c, int r,
			ByteMemoryAccess access, int rowstride) {
		super.draw8x8BitmapTwoColorBlock(c, r, access, rowstride);
		updateBlock(c, r);
	}

	@Override
	public void draw8x8BitmapFourColorBlock(int c,
			int r, ByteMemoryAccess access, int rowstride) {
		super.draw8x8BitmapFourColorBlock(c, r, access, rowstride);
		updateBlock(c, r);
	}

	@Override
	public void draw8x8BitmapRGB332ColorBlock(int c,
			int r, ByteMemoryAccess byteReadMemoryAccess, int rowstride) {
		super.draw8x8BitmapRGB332ColorBlock(c, r, byteReadMemoryAccess, rowstride);
		updateBlock(c, r);
	}

	@Override
	public void blitSpriteBlock(MemoryCanvas spriteCanvas, int x, int y,
			int blockMag) {
		super.blitSpriteBlock(spriteCanvas, x, y, blockMag);
		updateBlock(x, y);
	}
}

