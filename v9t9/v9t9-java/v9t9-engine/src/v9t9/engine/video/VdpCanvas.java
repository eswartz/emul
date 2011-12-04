/**
 * 
 */
package v9t9.engine.video;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas extends BaseVdpCanvas implements ISpriteCanvas {
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
	

	protected Format format;

	boolean isInterlacedEvenOdd;
	private int xoffs;
	private int yoffs;
	
	protected byte[] colorMap;
	protected byte[] spriteColorMap;
	protected byte[][] fourColorMap;

	public VdpCanvas() {
    	setSize(256, 192);
    }

	public void setFormat(Format format) {
		this.format = format;
		getColorMgr().useAltSpritePalette(format == Format.COLOR256_1x1);
	}
	public Format getFormat() {
		return format;
	}
	
	/** Clear the canvas to the clear color, if the rgb is not used.  
	 */
	public abstract void clear();
	private boolean isBlank;


	public final void setSize(int x, int y) {
		setSize(x, y, false);
	}

	public int getVisibleHeight() {
		return height * (isInterlacedEvenOdd ? 2 : 1);
	}
	

	public abstract void doChangeSize();

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

	public boolean isBlank() {
		return isBlank;
	}


	public void setBlank(boolean b) {
		if (b != isBlank) {
			isBlank = b;
			markDirty();
		}
	}

	public void setListener(ICanvasListener listener) {
		this.listener = listener;
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

	/** 
	 * Compose the block from the sprite canvas onto your canvas. 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitSpriteBlock(Sprite2Canvas spriteCanvas, int x, int y,
			int blockMag);

	/** 
	 * Compose the block from the sprite canvas onto your canvas, in four-color mode 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitFourColorSpriteBlock(Sprite2Canvas spriteCanvas, int x,
			int y, int blockMag);

	public void setInterlacedEvenOdd(boolean isInterlacedEvenOdd) {
		this.isInterlacedEvenOdd = isInterlacedEvenOdd;
	}

	public boolean isInterlacedEvenOdd() {
		return isInterlacedEvenOdd;
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

	public byte[] getRGB(int idx) {
		return getColorMgr().getRGB(idx);
	}

	/**
	 * 
	 */
	public void syncColors() {
		if (colorMap == null) {
			colorMap = new byte[16];
			spriteColorMap = new byte[16];
			fourColorMap = new byte[2][];
			fourColorMap[0] = new byte[16];
			fourColorMap[1] = new byte[16];
		}
		VdpColorManager cc = getColorMgr();
		if (cc.isClearFromPalette()) {
			colorMap[0] = 0;
			spriteColorMap[0] = 0;
		} else {
			colorMap[0] = (byte) cc.getClearColor();
			spriteColorMap[0] = (byte) cc.getClearColor();
		}
		fourColorMap[0][0] = (byte) cc.getFourColorModeColor(0, true);
		fourColorMap[1][0] = (byte) cc.getFourColorModeColor(0, false);
			
		for (int i = 1; i < 16; i++) {
			colorMap[i] = (byte) i;
			spriteColorMap[i] = (byte) i;
			fourColorMap[0][i] = (byte) cc.getFourColorModeColor(i, true);
			fourColorMap[1][i] = (byte) cc.getFourColorModeColor(i, false);
		}
	}
}
