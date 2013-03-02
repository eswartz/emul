/*
  VdpSprite.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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