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
		void canvasResized(VdpCanvas canvas);
	}

	// record dirty state in terms of 8x8 or 8x6 blocks
	//private boolean[] dirtyBlocks;
	//private int dirtyStride;
	private int dx1, dy1, dx2, dy2;
	
    protected int clearColor;
    protected int clearColor1;

    /** fundamental block width in pixels, which impacts how dirtyBlocks, dirtyStride are interpreted */
	private int blockWidth;

	/** width in pixels */
	protected int width;
	protected int bytesPerLine;
	
	/** height in pixels */
	protected int height;

	protected static final byte[][] stockPalette = {
		/* 0 */ { 0x00, 0x00, 0x00 }, 
		/* 1 */ { 0x00, 0x00, 0x00 },
		/* 2 */ { 0x40, (byte) 0xb0, 0x40 }, 
		/* 3 */ { 0x60, (byte) 0xc0, 0x60 },
		/* 4 */ { 0x40, 0x40, (byte) 0xc0 }, 
		/* 5 */ { 0x60, 0x60, (byte) 0xf0 },
		/* 6 */ { (byte) 0xc0, 0x40, 0x40 }, 
		/* 7 */ { 0x40, (byte) 0xf0, (byte) 0xf0 },
		/* 8 */ { (byte) 0xf0, 0x40, 0x40 }, 
		/* 9 */ { (byte) 0xff, (byte) 0x80, 0x60 },
		/* 10 */ { (byte) 0xf0, (byte) 0xc0, 0x40 },
		/* 11 */ { (byte) 0xff, (byte) 0xe0, 0x60 }, 
		/* 12 */ { 0x40, (byte) 0x80, 0x40 },
		/* 13 */ { (byte) 0xc0, 0x40, (byte) 0xc0 },
		/* 14 */ { (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
		/* 15 */ { (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
	};
	
	protected static final byte[][] altSpritePaletteGBR = {
		{ 0, 0, 0 },
		{ 0, 0, 2 },
		{ 0, 3, 0 },
		{ 0, 3, 2 },
		{ 3, 0, 0 },
		{ 3, 0, 2 },
		{ 3, 3, 0 },
		{ 3, 3, 2 },
		{ 4, 7, 2 },
		{ 0, 0, 7 },
		{ 0, 7, 0 },
		{ 0, 7, 7 },
		{ 7, 0, 0 },
		{ 7, 0, 7 },
		{ 7, 7, 0 },
		{ 7, 7, 7 }
	};
	protected byte colorPalette[][];
	protected byte greyPalette[][];
	protected byte altSpritePalette[][];

	private boolean isInterlacedEvenOdd;
	
	protected static byte[] rgb3to8 = new byte[8];
	protected static byte[] rgb2to8 = new byte[4];

	protected final int extraSpace;
	static {
		for (int i = 0; i < 8; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 5);
			if (val > 4)
				val8 |= 0x1f;
			rgb3to8[i] = val8;
		}
		for (int i = 0; i < 4; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 6);
			if (val > 2)
				val8 |= 0x3f;
			rgb2to8[i] = val8;
		}
	}
	
	public VdpCanvas() {
		this(8);
	}
	public VdpCanvas(int extraSpace) {
		
    	this.extraSpace = extraSpace;
		colorPalette = new byte[16][];
    	for (int i = 0; i < 16; i++)
    		colorPalette[i] = stockPalette[i]; 

    	greyPalette = new byte[16][];
    	for (int i = 0; i < 16; i++)
    		greyPalette[i] = rgbToGrey(stockPalette[i]);
    	
    	altSpritePalette = new byte[16][];
    	for (int i = 0; i < 16; i++) {
    		altSpritePalette[i] = getGRB333(
    				altSpritePaletteGBR[i][0], altSpritePaletteGBR[i][1], altSpritePaletteGBR[i][2]);
    	}
    	
    	setGreyscale(false);
    	
    	/*
    	for (int i = 16;  i < 256; i++) {
    		setRGB(i, new byte[] { 0, 0, 0 });
    	}*/
    	
		setBlockWidth(8);
    	setSize(256, 192);
    }

	private byte[] rgbToGrey(byte[] rgb) {
		byte[] g = new byte[3];
		int lum = (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
		g[0] = g[1] = g[2] = (byte) lum;
		return g;
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
		setSize(x, y, false);
	}
	
	public final void setSize(int x, int y, boolean isInterlaced) {
		if (x != width || y != height || isInterlaced != this.isInterlacedEvenOdd) {
			this.isInterlacedEvenOdd = isInterlaced;
			this.width = visibleToActualWidth(x);
			this.height = y;
			updateDirtyBuffer();
			doChangeSize();
			if (listener != null)
				listener.canvasResized(this);
		}
	}

	/** Convert the width displayed with the width in the canvas.
	 * We have 16 extra pixels for V9938 panning.  */
	protected int visibleToActualWidth(int x) {
		return x + extraSpace;
	}
	/** Convert the width in the canvas to the width displayed.  We have 16 extra
	 * pixels for V9938 panning. */
	protected int actualToVisibleWidth(int x) {
		return x - extraSpace;
	}

	public abstract void doChangeSize();

	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent, or some other value if supported 
	 */
	public void setClearColor(int c) {
		this.clearColor = c;
		this.clearColor1 = c;
		markDirty();
	}
	
	/**
	 * Set the real color for the "clear" color
	 * in an even-odd tiling mode
	 * @param c
	 */
	public void setClearColor1(int c) {
		this.clearColor1 = c;
		markDirty();
	}

	/** Clear the canvas to the clear color, if the rgb is not used.  
	 * @param rgb preferred color
	 */
	public abstract void clear(byte[] rgb);
	protected byte[] clearRGB;
	public byte[] getClearRGB() { return clearRGB; } 

	private boolean isBlank;

	private ICanvasListener listener;

	private byte[][] thePalette;

	private boolean clearFromPalette;

	private int xoffs;

	private int yoffs;

	private boolean useAltSpritePalette;

	/** Get the RGB triple for the palette entry. */
	public byte[] getRGB(int idx) {
		if (idx == 0 && !clearFromPalette)
			idx = clearColor;
		return thePalette[idx];
	}
	
	/** Get the RGB triple for the palette entry (odd fields). */
	public byte[] getRGB1(int idx) {
		if (idx == 0 && !clearFromPalette)
			idx = clearColor1;
		return thePalette[idx];
	}
	
	/** Get the RGB triple for the palette entry for a sprite. */
	public byte[] getSpriteRGB(int idx) {
		if (idx == 0 && !clearFromPalette)
			idx = clearColor;
		if (useAltSpritePalette)
			return altSpritePalette[idx];
		else
			return thePalette[idx];
	}
	
	
	
	/** Set the RGB triple for the palette entry. */
	public void setRGB(int idx, byte[] rgb) {
		if (colorPalette[idx] == null)
			colorPalette[idx] = new byte[3];
		colorPalette[idx][0] = rgb[0];
		colorPalette[idx][1] = rgb[1];
		colorPalette[idx][2] = rgb[2];
		
		greyPalette[idx] = rgbToGrey(rgb);
	}
	
	/** Get the RGB triple for the 3-bit GRB. */
	public byte[] getGRB333(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[b&0x7] };
	}

	/** Set the RGB triple for the palette entry, using 3-bit RGB (usually from a palette). */
	public void setGRB333(int idx, int g, int r, int b) {
		setRGB(idx, getGRB333(g, r, b));
	}
	
	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public void getGRB332(byte[] rgb, byte grb) {
		rgb[0] = rgb3to8[(grb >> 2) & 0x7];
		rgb[1] = rgb3to8[(grb >> 5) & 0x7];
		rgb[2] = rgb2to8[grb & 0x3];
	}
	public byte[] getStockRGB(int i) {
		return stockPalette[i];
	}
	/** Get an implementation-defined offset into the bitmap */ 
	public abstract int getBitmapOffset(int x, int y);

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

	/** Draw eight pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightSpritePixels(int offs, byte mem, byte fg, byte bitmask, boolean isLogicalOr); 
	/** Draw 16 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	/** Draw 32 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightDoubleMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	
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
	
	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	public int getWidth() {
		return width;
	}
	
	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	public int getVisibleWidth() {
		return actualToVisibleWidth(width);
	}
	
	
	/** Get the nominal screen height. This does not count interlacing. */
	public int getHeight() {
		return height;
	}
	
	public int getVisibleHeight() {
		return height * (isInterlacedEvenOdd ? 2 : 1);
	}

	public void setGreyscale(boolean b) {
		thePalette = b ? greyPalette : colorPalette;
	}

	public void setClearFromPalette(boolean b) {
		clearFromPalette = b;
	}

	/**
	 * Set adjustment offset 
	 * @param i
	 * @param j
	 */
	public void setOffset(int x, int y) {
		xoffs = x;
		yoffs = y;
	}
	
	public int getXOffset() { 
		return xoffs;
	}
	public int getYOffset() {
		return yoffs;
	}

	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * &lt;color;&gt;&lt;color&gt; in nybbles. 
	 * @param offs
	 * @param offs
	 * @param access
	 * @param rowstride access stride between rows
	 */
	public abstract void draw8x8BitmapTwoColorBlock(
			int offs,
			ByteMemoryAccess access,
			int rowstride);

	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * &lt;color;&gt;&lt;color&gt;&lt;color;&gt;&lt;color&gt; in two-bit pieces. 
	 * @param offs
	 * @param access
	 * @param rowstride access stride between rows
	 */
	public abstract void draw8x8BitmapFourColorBlock(int offs,
			ByteMemoryAccess access, int rowstride);

	/**
	 * Draw an 8x8 block of pixels from the given memory, arranged as
	 * RGB 3-3-2 pixels. 
	 * @param offset
	 * @param rowstride access stride between rows
	 * @param access
	 */
	public abstract void draw8x8BitmapRGB332ColorBlock(int offset,
			ByteMemoryAccess byteReadMemoryAccess, int rowstride);


	public void clearToEvenOddClearColors() {
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c += 8) {
				drawEightPixels(getBitmapOffset(c, r), (byte)0xaa, (byte)clearColor, (byte)clearColor1);
			}
		}
	}

	public int getClearColor() {
		return clearColor;
	}

	public int getBlockCount() {
		return (getVisibleWidth() / 8) * ((height + 7) / 8);
	}

	/** 
	 * Compose the block from the sprite canvas onto your canvas. 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitSpriteBlock(MemoryCanvas spriteCanvas, int x, int y,
			int blockMag);

	/** 
	 * Compose the block from the sprite canvas onto your canvas, in four-color mode 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitFourColorSpriteBlock(MemoryCanvas spriteCanvas, int x,
			int y, int blockMag);

	public boolean isClearFromPalette() {
		return clearFromPalette;
	}

	public boolean isUseAltSpritePalette() {
		return useAltSpritePalette;
	}

	public void setUseAltSpritePalette(boolean useAltSpritePalette) {
		this.useAltSpritePalette = useAltSpritePalette;
	}

	/** Map the image generated by ImageData to something renderable */
	public Rectangle mapVisible(Rectangle logical) {
		return new Rectangle(logical.x, logical.y, logical.width, logical.height);
	}

	public void setInterlacedEvenOdd(boolean isInterlacedEvenOdd) {
		this.isInterlacedEvenOdd = isInterlacedEvenOdd;
	}

	public boolean isInterlacedEvenOdd() {
		return isInterlacedEvenOdd;
	}


}
