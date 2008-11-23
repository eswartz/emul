package v9t9.tests.video;

import v9t9.emulator.clients.builtin.video.VdpCanvas;

/**
 * Base class for a sprite
 * @author ejs
 *
 */
public abstract class SpriteBase {
	protected int x = -1;
	protected int y = -1;
	protected int size;
	protected int shift = -1;
	protected boolean deleted;
	protected boolean changed;

	public SpriteBase() {
		deleted = true;
	}
	public void move(int x, int y) {
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			setBitmapDirty(true);
		}
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		if (size != this.size) {
			this.size = size;
			setBitmapDirty(true);
		}
	}
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getShift() {
		return shift;
	}
	public void setShift(int shift) {
		if (shift != this.shift) {
			this.shift = shift;
			setBitmapDirty(true);
		}
	}
	
	public void setDeleted(boolean deleted) {
		if (deleted != this.deleted) {
			this.deleted = deleted;
			setBitmapDirty(true);
		}
	}
	public boolean isDeleted() {
		return deleted;
	}
	
	/** Tell if the bitmap under the sprite is dirty,
	 * either due to the bitmap changing or the sprite changing */
	public boolean isBitmapDirty() {
		return changed;
	}

	public void setBitmapDirty(boolean flag) {
		this.changed = flag;
	}
	
	/** Draw the sprite on the canvas */
	abstract public void draw(VdpCanvas canvas);
	
}