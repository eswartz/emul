/*
  VdpSprite.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.common.video;

import v9t9.common.memory.ByteMemoryAccess;

public class VdpSprite extends SpriteBase {
	private byte color;
	private ByteMemoryAccess pattern;
	
	private ByteMemoryAccess colorStripe;
	private final int n;
	
	public VdpSprite(int n) {
		this.n = n;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.SpriteBase#toString()
	 */
	@Override
	public String toString() {
		if (color == 0 && colorStripe == null)
			return "VdpSprite #" + n;
		else
			return super.toString() + " #" + n;
	}
	
	public byte getColor() {
		return color;
	}
	public void setColor(int color) {
		if (color != this.color || colorStripe != null) {
			this.color = (byte) color;
			colorStripe = null;
			setBitmapDirty(true);
		}
	}
	
	public void setColorStripe(ByteMemoryAccess colorStripe) {
		if (colorStripe == null || !colorStripe.equals(this.colorStripe)) {
			this.colorStripe = colorStripe;
			setBitmapDirty(true);
		}
	}
	public ByteMemoryAccess getColorStripe() {
		return colorStripe;
	}

	public ByteMemoryAccess getPattern() {
		return pattern;
	}
	
	public void setPattern(ByteMemoryAccess pattern) {
		if (this.pattern == null || !this.pattern.equals(pattern)) {
			this.pattern = pattern;
			setBitmapDirty(true);
		}
	}

	
	/** Update "last" values with current values. */
	public void finishDraw() {
		setBitmapDirty(false);
	}

}