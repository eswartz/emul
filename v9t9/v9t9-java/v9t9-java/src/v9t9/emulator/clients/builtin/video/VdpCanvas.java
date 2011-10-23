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
	
	public VdpCanvas() {
		
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
		setGreyscale(isGreyscale());	// reset sprite palette
	}
	public Format getFormat() {
		return format;
	}
	
	/** Clear the canvas to the clear color, if the rgb is not used.  
	 * @param rgb preferred color
	 */
	public abstract void clear(byte[] rgb);
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


	/** Get the dirty rectangle in pixels */
	public Rectangle getDirtyRect() {
		if (dx1 >= dx2 || dy1 >= dy2)
			return null;

		if (isInterlacedEvenOdd()) {
			return new Rectangle(dx1, dy1 * 2, (dx2 - dx1), (dy2 - dy1) * 2);
		}
		return new Rectangle(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
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
}
