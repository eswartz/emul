/**
 * 
 */
package v9t9.common.video;

import v9t9.common.memory.ByteMemoryAccess;


/**
 * @author ejs
 *
 */
public interface IVdpCanvas extends ICanvas {

	VdpFormat getFormat();

	boolean isInterlacedEvenOdd();

	/** Clear the canvas to the clear color, if the rgb is not used.  
	 */
	void clear();
	

	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 */
	abstract public void draw8x8TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg);

	/**
	 * Blit an 8x6 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 */
	abstract public void draw8x6TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg);

	/**
	 * Blit an 8x8 block defined by a pattern and colors to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param colors array of 0x&lt;fg&gt;&lt;bg&gt; pixels; bg may be 0 for vdpreg[7] bg
	 */
	abstract public void draw8x8MultiColorBlock(int r, int c,
			ByteMemoryAccess pattern, ByteMemoryAccess colors);


	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * &lt;color;&gt;&lt;color&gt; in nybbles. 
	 * @param offs
	 * @param offs
	 * @param access
	 * @param rowstride access stride between rows
	 */
	public abstract void draw8x8BitmapTwoColorBlock(
			int x, int y,
			ByteMemoryAccess access,
			int rowstride);

	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * &lt;color;&gt;&lt;color&gt;&lt;color;&gt;&lt;color&gt; in two-bit pieces. 
	 * @param offs
	 * @param access
	 * @param rowstride access stride between rows
	 */
	public abstract void draw8x8BitmapFourColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride);

	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * RGB 3-3-2 pixels. 
	 * @param offset
	 * @param rowstride access stride between rows
	 * @param access
	 */
	public abstract void draw8x8BitmapRGB332ColorBlock(int x, int y,
			ByteMemoryAccess byteReadMemoryAccess, int rowstride);


	/** Draw eight pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr); 
	/** Draw 16 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	/** Draw 32 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);

	abstract public void clearToEvenOddClearColors();

	/**
	 * @return
	 */
	int getBlockCount();

	/**
	 * @return
	 */
	boolean isMono();

	/**
	 * @param color168x8
	 */
	void setFormat(VdpFormat color168x8);

	/**
	 * @param mono
	 */
	void setMono(boolean mono);

	/**
	 * @param b
	 */
	void setBlank(boolean b);

	/**
	 * 
	 */
	void syncColors();

	/**
	 * 
	 */
	void markDirty();

	/**
	 * @param blocks
	 * @param count
	 */
	void markDirty(RedrawBlock[] blocks, int count);

	/**
	 * @param xoffs
	 * @param yoffs
	 */
	void setOffset(int xoffs, int yoffs);

	/**
	 * @param width
	 * @param videoHeight
	 * @param interlacedEvenOdd
	 */
	void setSize(int width, int height, boolean interlacedEvenOdd);

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	int getBitmapOffset(int i, int j);

	/**
	 * @param spriteCanvas
	 * @param i
	 * @param j
	 * @param blockMag
	 */
	void blitSpriteBlock(ISpriteVdpCanvas spriteCanvas, int i, int j, int blockMag);

	/**
	 * @param spriteCanvas
	 * @param i
	 * @param j
	 * @param blockMag
	 */
	void blitFourColorSpriteBlock(ISpriteVdpCanvas spriteCanvas, int i, int j,
			int blockMag);


	VdpColorManager getColorMgr();
	void setClearColor(int c);

}