/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas {
	
    protected int clearColor;

	public VdpCanvas() {
    	setSize(256, 192);
    }


	public abstract void setSize(int x, int y);

	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent
	 */
	public void setClearColor(int c) {
		this.clearColor = c;
	}

	public abstract int getWidth() ;


	public abstract int getHeight();

	public abstract void clear();


	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg
	 * @param bg
	 */
	public abstract void draw8x8TwoColorBlock(int r, int c, ByteMemoryAccess pattern, byte fg,
			byte bg);

	/**
	 * Blit an 8x6 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg
	 * @param bg
	 */
	public abstract void draw8x6TwoColorBlock(int r, int c, ByteMemoryAccess pattern, byte fg,
			byte bg);

	/**
	 * Blit an 8x8 block defined by a pattern and colors to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param colors
	 */
	public abstract void draw8x8MultiColorBlock(int r, int c,
			ByteMemoryAccess pattern, ByteMemoryAccess colors);

	protected byte vdp_palette[][] = { { 0x00, 0x00, 0x00 }, { 0x00, 0x00, 0x00 },
			{ 0x40, (byte) 0xb0, 0x40 }, { 0x60, (byte) 0xc0, 0x60 },
			{ 0x40, 0x40, (byte) 0xc0 }, { 0x60, 0x60, (byte) 0xf0 },
			{ (byte) 0xc0, 0x40, 0x40 }, { 0x40, (byte) 0xf0, (byte) 0xf0 },
			{ (byte) 0xf0, 0x40, 0x40 }, { (byte) 0xff, (byte) 0x80, 0x60 },
			{ (byte) 0xf0, (byte) 0xc0, 0x40 },
			{ (byte) 0xff, (byte) 0xe0, 0x60 }, { 0x40, (byte) 0x80, 0x40 },
			{ (byte) 0xc0, 0x40, (byte) 0xc0 },
			{ (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
			{ (byte) 0xff, (byte) 0xff, (byte) 0xff }, { 0x00, 0x00, 0x00 } };

	/** Get the RGB triple for the palette entry. */
	public byte[] getColorRGB(int idx) {
		if (idx == 0)
			idx = clearColor;
		return vdp_palette[idx];
	}
	
	public abstract int getBitmapOffset(int x, int y);

	public abstract void setColorAtOffset(int offset, byte color);
	
	public abstract int getPixelStride();
}
