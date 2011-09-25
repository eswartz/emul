/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.Rectangle;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas {
	public enum Format {
		/** Text mode */
		TEXT,
		/** Graphics mode, one color set per 8x8 block */
		COLOR16_8x8,
		/** Bitmap mode, one color set per 8x1 block */
		COLOR16_8x1,
		/** Multicolor mode, one color set per 4x4 block */
		COLOR16_4x4,
		/** V9938 16-color mode */
		COLOR16_1x1,
		/** V9938 4-color mode */
		COLOR4_1x1,
		/** V9938 256-color mode */
		COLOR256_1x1,
	}
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

	/** width in pixels */
	protected int width;
	protected int bytesPerLine;
	
	/** height in pixels */
	protected int height;
	
	protected Format format;

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
			//if (val > 4)
			//	val8 |= 0x1f;
			val8 |= i * 0x1f / 7; 
			rgb3to8[i] = val8;
		}
		for (int i = 0; i < 4; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 6);
			val8 |= i * 0x3f / 3;
			rgb2to8[i] = val8;
		}
	}
	
	public VdpCanvas() {
		this(8);
	}
	private static byte[] fromRBG(String hex) {
		int r = Integer.parseInt(hex.substring(0, 1), 16);
		int b = Integer.parseInt(hex.substring(1, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 3), 16);
		return getGRB333(g, r, b);
	}
	

	
	protected static final byte[][] stockPaletteEd = {
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
	
	
	// "ColecoFan1981" http://www.atariage.com/forums/topic/155790-tms-9918a9928a9929a-colors/
	protected static final byte[][] stockPalette = {
		/* 0 */ { 0x00, 0x00, 0x00 }, 
		/* 1 */ { 0x00, 0x00, 0x00 },
		/* 2 */ { 70, (byte) 183, 62 }, 
		/* 3 */ { 124, (byte) 208, 108 },
		/* 4 */ { 99, 91, (byte) 169 }, 
		/* 5 */ { 127, 113, (byte) 255 },
		/* 6 */ { (byte) 183, 98, 73 }, 
		/* 7 */ { 92, (byte) 199, (byte) 239 },
		/* 8 */ { (byte) 217, 107, 73 }, 
		/* 9 */ { (byte) 253, (byte) 142, 108 },
		/* 10 */ { (byte) 195, (byte) 206, 66 },
		/* 11 */ { (byte) 211, (byte) 219, 117 }, 
		/* 12 */ { 61, (byte) 160, 47 },
		/* 13 */ { (byte) 183, 99, (byte) 199 },
		/* 14 */ { (byte) 204, (byte) 204, (byte) 204 },
		/* 15 */ { (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
			
	};
	protected static final byte[][] stockPaletteWashed = {
		/* 0 */ { 0x00, 0x00, 0x00 }, 
		/* 1 */ { 0x00, 0x00, 0x00 },
		/* 2 */ { 82, (byte) 190, 71 }, 
		/* 3 */ { (byte) 134, (byte) 213, 121 },
		/* 4 */ { 111, 103, (byte) 178 }, 
		/* 5 */ { (byte) 139, 123, (byte) 255 },
		/* 6 */ { (byte) 190, 110, 85 }, 
		/* 7 */ { 105, (byte) 205, (byte) 241 },
		/* 8 */ { (byte) 221, 118, 85 }, 
		/* 9 */ { (byte) 253, (byte) 153, 121 },
		/* 10 */ { (byte) 202, (byte) 212, 77 },
		/* 11 */ { (byte) 217, (byte) 222, (byte) 128 }, 
		/* 12 */ { 75, (byte) 171, 61 },
		/* 13 */ { (byte) 190, 110, (byte) 205 },
		/* 14 */ { (byte) 210, (byte) 210, (byte) 210 },
		/* 15 */ { (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
	};

	// VDP V9938 default
	protected static final byte[][] stockPaletteV9938 = {
		fromRBG("000"), // 0
	    fromRBG("000"),  // 1
	    fromRBG("116"),  // 2
	    fromRBG("337"),  // 3
	    fromRBG("171"),  // 4
	    fromRBG("273"),  // 5
	    fromRBG("511"),  // 6
	    fromRBG("276"),  // 7
	    fromRBG("711"),  // 8
	    fromRBG("733"),  // 9
	    fromRBG("616"),  // A
	    fromRBG("646"),  // B
	    fromRBG("114"),  // C
	    fromRBG("652"),  // D
	    fromRBG("555"),  // E
	    fromRBG("777"),  // F
	};

	public static final byte[][][] palettes() {
		return new byte[][][] {
			stockPaletteEd,
			stockPaletteV9938
		};
	};

	public static byte[][][] allPalettes() {
		return new byte[][][] {
			stockPalette,
			stockPaletteEd,
			stockPaletteV9938,
			stockPaletteWashed
		};
	}
	
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
	public VdpCanvas(int extraSpace) {
		
    	this.extraSpace = extraSpace;
		colorPalette = new byte[16][];
    	for (int i = 0; i < 16; i++)
    		colorPalette[i] = Arrays.copyOf(stockPalette[i], 3); 

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
    	
    	setSize(256, 192);
    }

	public void setFormat(Format format) {
		this.format = format;
		paletteMappingDirty = true;
		useAltSpritePalette = format == Format.COLOR256_1x1;
	}
	public Format getFormat() {
		return format;
	}
	
	public static byte[] rgbToGrey(byte[] rgb) {
		byte[] g = new byte[3];
		int lum = (299 * (rgb[0] & 0xff) + 587 * (rgb[1] & 0xff) + 114 * (rgb[2] & 0xff)) / 1000;
		g[0] = g[1] = g[2] = (byte) lum;
		return g;
	}

	public final void setSize(int x, int y) {
		setSize(x, y, false);
	}
	
	public final void setSize(int x, int y, boolean isInterlaced) {
		if (x != width || y != height || isInterlaced != this.isInterlacedEvenOdd) {
			this.isInterlacedEvenOdd = isInterlaced;
			this.width = visibleToActualWidth(x);
			this.height = y;
			markDirty();
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

	protected byte[][] thePalette;
	
	protected boolean paletteMappingDirty = true;

	private boolean clearFromPalette;

	private int xoffs;

	private int yoffs;

	private boolean useAltSpritePalette;

	private boolean isGreyscale;

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
		
		paletteMappingDirty = true;
	}
	
	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB333(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[b&0x7] };
	}
	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB332(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb2to8[b&0x3] };
	}

	/** Set the RGB triple for the palette entry, using 3-bit RGB (usually from a palette). */
	public void setGRB333(int idx, int g, int r, int b) {
		setRGB(idx, getGRB333(g, r, b));
	}
	
	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public void getGRB332(byte[] rgb, byte grb) {
		getGRB332(rgb, grb, isGreyscale);
	}

	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public static void getGRB332(byte[] rgb, byte grb, boolean isGreyscale) {
		rgb[0] = rgb3to8[(grb >> 2) & 0x7];
		rgb[1] = rgb3to8[(grb >> 5) & 0x7];
		rgb[2] = rgb2to8[grb & 0x3];
		if (isGreyscale) {
			// (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
			
			int l = ((rgb[0] & 0xff) * 299 + (rgb[1] & 0xff) * 587 + (rgb[2] & 0xff) * 114) / 1000;
			rgb[0] = (byte) l;
			rgb[1] = (byte) l;
			rgb[2] = (byte) l;
		}
	}
	public byte[] getStockRGB(int i) {
		return stockPalette[i];
	}
	
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

	/** Draw eight pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr); 
	/** Draw 16 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	/** Draw 32 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	public abstract void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	
	public boolean isBlank() {
		return isBlank;
	}


	public void setBlank(boolean b) {
		if (b != isBlank) {
			isBlank = b;
			markDirty();
		}
	}


	public void markDirty(RedrawBlock[] blocks, int count) {
		if (dx1 == 0 && dy1 == 0 && dx2 == width && dy2 == height) {
			// already dirty
			listener.canvasDirtied(this);
		} else {
			for (int i = 0; i < count; i++) {
				RedrawBlock block = blocks[i];
				//int y = (block.r / 8);
				//int x = block.c / blockWidth;
				//int idx = y * dirtyStride + x;
				//dirtyBlocks[idx] = true;
				if (block.c < dx1) dx1 = block.c;
				if (block.r < dy1) dy1 = block.r;
				if (block.c + 8 >= dx2) dx2 = block.c + 8;
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

		if (isInterlacedEvenOdd()) {
			return new Rectangle(dx1, dy1 * 2, (dx2 - dx1), (dy2 - dy1) * 2);
		}
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
		this.isGreyscale = b;
		thePalette = b ? greyPalette : colorPalette;
		paletteMappingDirty = true;
	}
	
	public boolean isGreyscale() {
		return isGreyscale;
	}
	
	public byte[][] getPalette() {
		return thePalette;
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


	abstract public void clearToEvenOddClearColors();

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

	/**
	 * Tell whether the color 0 is transparent or a color in the
	 * palette.
	 * @param b true: clear (color 0) is a palette color, false: transparent
	 */
	public void setClearFromPalette(boolean b) {
		clearFromPalette = b;
	}

	/**
	 * Tell whether the color 0 is transparent or a color in the
	 * palette.
	 * @return true: clear (color 0) is a palette color, false: transparent
	 */
	public boolean isClearFromPalette() {
		return clearFromPalette;
	}

	public void setInterlacedEvenOdd(boolean isInterlacedEvenOdd) {
		this.isInterlacedEvenOdd = isInterlacedEvenOdd;
	}

	public boolean isInterlacedEvenOdd() {
		return isInterlacedEvenOdd;
	}
	
	/**
	 * @return the extraSpace
	 */
	public int getExtraSpace() {
		return extraSpace;
	}
	
}
