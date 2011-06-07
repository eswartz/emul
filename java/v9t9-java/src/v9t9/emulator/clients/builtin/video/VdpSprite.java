/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;

public class VdpSprite extends SpriteBase {
	private byte color;
	private ByteMemoryAccess pattern;
	
	private ByteMemoryAccess colorStripe;
	
	public VdpSprite() {
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
		this.colorStripe = colorStripe;
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