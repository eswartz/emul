/*
  Sprite2VdpCanvas.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.video;

import java.util.Arrays;

import v9t9.common.video.BaseVdpCanvas;
import v9t9.common.video.ISpriteVdpCanvas;

/**
 * This class is used for sprite 2 sprite drawing (since due to the
 * per-row early clock, a sprite would dirty so much, and vice versa
 * for underlying screen changes, we draw sprites separately then
 * blit then changes
 * @author ejs
 *
 */
public class Sprite2VdpCanvas extends BaseVdpCanvas implements ISpriteVdpCanvas {
	private final int UPDATEBLOCK_ROW_STRIDE = (256+64);
	private final byte[] bitmap = new byte[UPDATEBLOCK_ROW_STRIDE * 256];
	private final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }

    public Sprite2VdpCanvas() {
    	setSize(256, 212);
    	setClearColor(0);
    }

	public void doChangeSize() {
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#getLineStride()
	 */
	@Override
	public int getLineStride() {
		return UPDATEBLOCK_ROW_STRIDE;
	}
	
	@Override
	public int getPixelStride() {
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#drawEightSpritePixels(int, int, byte, byte, byte, boolean)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#drawEightMagnifiedSpritePixels(int, int, byte, byte, short, boolean)
	 */
	@Override
	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 16; i += 2) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i] |= fg;
					bitmap[offs + i + 1] |= fg;
				} else {
					bitmap[offs + i] = fg;
					bitmap[offs + i + 1] = fg;
				}
			}
			mem <<= 1;
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#drawEightDoubleMagnifiedSpritePixels(int, int, byte, byte, short, boolean)
	 */
	@Override
	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 32; i += 4) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i] |= fg;
					bitmap[offs + i + 1] |= fg;
					bitmap[offs + i + 2] |= fg;
					bitmap[offs + i + 3] |= fg;
				} else {
					bitmap[offs + i] = fg;
					bitmap[offs + i + 1] = fg;
					bitmap[offs + i + 2] = fg;
					bitmap[offs + i + 3] = fg;
				}
			}
			mem <<= 1;
		}
	}

	public void clear8x8Block(int offset) {
		for (int i = 0; i < 8; i++) {
			if (offset < bitmap.length)
				Arrays.fill(bitmap, offset, offset + 8, (byte) 0);
			offset += getLineStride();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#getColorAtOffset(int)
	 */
	@Override
	public byte getColorAtOffset(int offset) {
		return bitmap[offset];
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.ISprite2Canvas#getBitmapOffset(int, int)
	 */
	@Override
	public int getBitmapOffset(int x, int y) {
		return UPDPTR(y, x);
	}


}
