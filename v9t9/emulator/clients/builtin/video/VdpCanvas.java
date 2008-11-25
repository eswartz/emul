/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.graphics.Rectangle;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas {
	public interface ICanvasListener {
		void canvasDirtied(VdpCanvas canvas);
	}
	protected static final int X_PADDING = 32;

	// record dirty state in terms of 8x8 or 8x6 blocks
	//private boolean[] dirtyBlocks;
	//private int dirtyStride;
	private int dx1, dy1, dx2, dy2;
	
    protected int clearColor;

    /** fundamental block width in pixels, which impacts how dirtyBlocks, dirtyStride are interpreted */
	private int blockWidth;

	/** width in pixels */
	protected int width;

	/** height in pixels */
	protected int height;

	public VdpCanvas() {
		setBlockWidth(8);
    	setSize(256, 192);
    }

	public void setBlockWidth(int width) {
		blockWidth = width;
		updateDirtyBuffer();
	}

	private void updateDirtyBuffer() {
		//dirtyStride = width / blockWidth;
		//dirtyBlocks = new boolean[(height / 8) * dirtyStride];
		markDirty();
	}

	public final void setSize(int x, int y) {
		if (x != width || y != height) {
			this.width = x;
			this.height = y;
			updateDirtyBuffer();
			doChangeSize();
		}
	}
	public abstract void doChangeSize();

	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent
	 */
	public void setClearColor(int c) {
		this.clearColor = c;
		markDirty();
	}

	public abstract void clear();

	protected static byte vdp_palette[][] = {
		{ 0x00, 0x00, 0x00 }, 
		{ 0x00, 0x00, 0x00 },
			{ 0x40, (byte) 0xb0, 0x40 }, 
			{ 0x60, (byte) 0xc0, 0x60 },
			{ 0x40, 0x40, (byte) 0xc0 }, 
			{ 0x60, 0x60, (byte) 0xf0 },
			{ (byte) 0xc0, 0x40, 0x40 }, 
			{ 0x40, (byte) 0xf0, (byte) 0xf0 },
			{ (byte) 0xf0, 0x40, 0x40 }, 
			{ (byte) 0xff, (byte) 0x80, 0x60 },
			{ (byte) 0xf0, (byte) 0xc0, 0x40 },
			{ (byte) 0xff, (byte) 0xe0, 0x60 }, 
			{ 0x40, (byte) 0x80, 0x40 },
			{ (byte) 0xc0, 0x40, (byte) 0xc0 },
			{ (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
			{ (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
			
			// color #16 reflects the vdpfg  
			{ 0x00, 0x00, 0x00 } };

	protected static float yiq[][] = {
			{ 0.00f, 0.47f, 0.47f },
			{ 0.00f, 0.47f, 0.47f },
			{ 0.53f, 0.07f, 0.20f },
			{ 0.67f, 0.17f, 0.27f },
			{ 0.40f, 0.40f, 1.00f },
			{ 0.53f, 0.43f, 0.93f },
			{ 0.47f, 0.83f, 0.30f },
			{ 0.73f, 0.00f, 0.70f },
			{ 0.53f, 0.93f, 0.27f },
			{ 0.67f, 0.93f, 0.27f },
			{ 0.73f, 0.57f, 0.07f },
			{ 0.80f, 0.57f, 0.17f },
			{ 0.47f, 0.13f, 0.23f },
			{ 0.53f, 0.73f, 0.67f },
			{ 0.80f, 0.47f, 0.47f },
			{ 1.00f, 0.47f, 0.47f }
	};
	
	protected static float yiqrgb[][] = {
		{ 1f, 	0.9563f,	0.6210f },
		{ 1f,	-.2721f,	-.6474f },
		{ 1f,	-1.1070f,	1.7046f }
	};
	
	static {
		// EJS clearly has no idea what he's doing
		for (int i = 0; i < 16; i++) {
			/*
			float[] mm = { 0, 0, 0 };
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					mm[r] += yiqrgb[r][c] * yiq[i][c];
				}
			}
			*/
			/*
			
			float y = yiq[i][0];
			float rmy = yiq[i][1];
			float bmy = yiq[i][2];
			mm[0] = y + rmy;
			mm[1] = (float) (y - 0.51*rmy - 0.186*bmy);
			mm[2] = y + bmy;
			*/
			/*
			vdp_palette[i][0] = (byte)(255 * mm[0]);
			vdp_palette[i][1] = (byte)(255 * mm[1]);
			vdp_palette[i][2] = (byte)(255 * mm[2]);
			*/
		}
	
	}

	private boolean isBlank;

	private ICanvasListener listener;

	/** Get the RGB triple for the palette entry. */
	public byte[] getColorRGB(int idx) {
		if (idx == 0)
			idx = clearColor;
		return vdp_palette[idx];
	}
	
	/** Get an implementation-defined offset into the bitmap */ 
	public abstract int getBitmapOffset(int x, int y);

	/** Set the color at the offset
	 * @see #getBitmapOffset(int, int) */ 
	public abstract void setColorAtOffset(int offset, byte color);
	
	/** Get the delta for one pixel, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	public abstract int getPixelStride();
	
	/** Get the delta for one row, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	public abstract int getLineStride();

	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 */
	public void draw8x8TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			drawEightPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}

	}

	/**
	 * Blit an 8x6 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 */
	public void draw8x6TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			drawSixPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}
	}

	/**
	 * Blit an 8x8 block defined by a pattern and colors to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param colors array of 0x&lt;fg&gt;&lt;bg&gt; pixels; bg may be 0 for vdpreg[7] bg
	 */
	public void draw8x8MultiColorBlock(int r, int c,
			ByteMemoryAccess pattern, ByteMemoryAccess colors) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			byte color = colors.memory[colors.offset + i];
			byte fg = (byte) ((color >> 4) & 0xf);
			byte bg = (byte) (color & 0xf);
			drawEightPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}
	}

	/**
	 * Draw eight pixels in a row, where pixels corresponding to an "on"
	 * bit in "mem" are painted with the "fg" color, otherwise with the "bg" color.
	 * @param offs
	 * @param mem
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 * @see #getBitmapOffset(int, int)
	 */
	abstract protected void drawEightPixels(int offs, byte mem, byte fg, byte bg); 
	/**
	 * Draw six pixels in a row, where pixels corresponding to an "on"
	 * bit in "mem" are painted with the "fg" color, otherwise with the "bg" color.
	 * @param offs
	 * @param mem
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 * @see #getBitmapOffset(int, int)
	 */
	abstract protected void drawSixPixels(int offs, byte mem, byte fg, byte bg); 

	/**
	 * Draws an 8x8 sprite character
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param color the color for "on" bits on the sprite; will not be 0
	 */
	public void drawUnmagnifiedSpriteChar(int y, int x, int shift, int rowbitmap, ByteMemoryAccess pattern,
			byte color) {
		if (x + shift + 8 <= 0)
			return;
		
		byte bitmask = -1;
		if (x + shift < 0) {
			bitmask &= 0xff >> (x + shift);
		} else if (x + shift + 8 > 256) {
			bitmask &= 0xff << ((x + shift + 8) - 256);
		}
		
		for (int yy = 0; yy < 8; yy++) {
			if (y >= height)
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy];
				if (patt != 0) {
					int block = getBitmapOffset(x + shift, y);
					drawEightSpritePixels(block, patt, color, bitmask);
				}
			}
			y = (y + 1) & 0xff;
		}
	}

	/**
	 * Draws an 16x16 sprite character from an 8x8 pattern
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param color the color for "on" bits on the sprite; will not be 0
	 */
	public void drawMagnifiedSpriteChar(int y, int x, int shift, int rowbitmap, ByteMemoryAccess pattern,
			byte color) {
		if (x + shift + 16 <= 0)
			return;

		short bitmask = -1;
		if (x + shift < 0) {
			bitmask &= 0xffff >> (x + shift);
		} else if (x + shift + 16 > 256) {
			bitmask &= 0xffff << ((x + shift + 16) - 256);
		}
		
		for (int yy = 0; yy < 16; yy++) {
			if (y >= height)
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy / 2];
				if (patt != 0) {
					int block = getBitmapOffset(x + shift, y);
					drawEightMagnifiedSpritePixels(block, patt, color, bitmask);
				}
			}
			y = (y + 1) & 0xff;
		}
	}
	
	/** Draw eight pixels of an 8x1 row. 
	 * @param bitmask TODO*/
	abstract protected void drawEightSpritePixels(int offs, byte mem, byte fg, byte bitmask); 
	/** Draw 16 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask TODO*/
	abstract protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask);
	
	public boolean isBlank() {
		return isBlank;
	}


	public void setBlank(boolean b) {
		isBlank = b;
	}


	public void markDirty(RedrawBlock[] blocks, int count) {
		if (dx1 == 0 && dy1 == 0 && dx2 == width && dy2 == height) {
			// already dirty
		} else {
			for (int i = 0; i < count; i++) {
				RedrawBlock block = blocks[i];
				//int y = (block.r / 8);
				//int x = block.c / blockWidth;
				//int idx = y * dirtyStride + x;
				//dirtyBlocks[idx] = true;
				if (block.c < dx1) dx1 = block.c;
				if (block.r < dy1) dy1 = block.r;
				if (block.c + blockWidth >= dx2) dx2 = block.c + blockWidth;
				if (block.r + 8 >= dy2) dy2 = block.r + 8;
			}
			if (count > 0 && listener != null)
				listener.canvasDirtied(this);
		}
	}
	
	public void markDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, true);
		dx1 = dy1 = 0;
		dx2 = width;
		dy2 = height;
		if (listener != null)
			listener.canvasDirtied(this);
	}
	
	public void clearDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, false);
		dx1 = width;
		dy1 = height; 
		dx2 = dy2 = 0;
	}

	/** Get the dirty rectangle in pixels */
	public Rectangle getDirtyRect() {
		if (dx1 >= dx2 || dy1 >= dy2)
			return null;
		return new Rectangle(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
	}
	
	public void setListener(ICanvasListener listener) {
		this.listener = listener;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

}
