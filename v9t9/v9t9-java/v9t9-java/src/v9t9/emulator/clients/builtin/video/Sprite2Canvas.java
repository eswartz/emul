/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

/**
 * This class is used for sprite 2 sprite drawing (since due to the
 * per-row early clock, a sprite would dirty so much, and vice versa
 * for underlying screen changes, we draw sprites separately then
 * blit then changes
 * @author ejs
 *
 */
public class Sprite2Canvas extends BaseVdpCanvas implements ISpriteCanvas {
	private final int UPDATEBLOCK_ROW_STRIDE = (256+64);
	private final byte[] bitmap = new byte[UPDATEBLOCK_ROW_STRIDE * 256];
	private final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }

    public Sprite2Canvas() {
    	setSize(256, 192);
    }

	public void doChangeSize() {
	}

	public int getLineStride() {
		return UPDATEBLOCK_ROW_STRIDE;
	}
	
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		if (isLogicalOr) {
			for (int i = 0; i < 8; i++) {
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] |= fg;
				}
				mem <<= 1;
			}
		} else {
			for (int i = 0; i < 8; i++) {
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] = fg;
				}
				mem <<= 1;
			}
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i * 2] |= fg;
					bitmap[offs + i * 2 + 1] |= fg;
				} else {
					bitmap[offs + i * 2] = fg;
					bitmap[offs + i * 2 + 1] = fg;
				}
			}
			mem <<= 1;
		}
	}
	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i * 4] |= fg;
					bitmap[offs + i * 4 + 1] |= fg;
					bitmap[offs + i * 4 + 2] |= fg;
					bitmap[offs + i * 4 + 3] |= fg;
				} else {
					bitmap[offs + i * 4] = fg;
					bitmap[offs + i * 4 + 1] = fg;
					bitmap[offs + i * 4 + 2] = fg;
					bitmap[offs + i * 4 + 3] = fg;
				}
			}
			mem <<= 1;
		}
	}

	public void clear8x8Block(int offset) {
		for (int i = 0; i < 8; i++) {
			Arrays.fill(bitmap, offset, offset + 8, (byte) 0);
			offset += getLineStride();
		}
	}

	public byte getColorAtOffset(int offset) {
		return bitmap[offset];
	}

	public int getBitmapOffset(int x, int y) {
		return UPDPTR(y, x);
	}
}
