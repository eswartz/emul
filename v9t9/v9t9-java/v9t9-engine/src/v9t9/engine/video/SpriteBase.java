package v9t9.engine.video;


/**
 * Base class for a sprite
 * @author ejs
 *
 */
public abstract class SpriteBase {
	protected int x = -1;
	protected int y = -1;
	protected int sizeY, sizeX;
	protected int shift = -1;
	protected boolean deleted;
	protected boolean changed;

	public SpriteBase() {
		deleted = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return deleted ? "<<Sprite>>" :
			"Sprite @" + x + "," + y + " sz " + sizeX + "x" + sizeY;
	}
	public void move(int x, int y) {
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			setBitmapDirty(true);
		}
	}
	public int getSizeY() {
		return sizeY;
	}
	public int getSizeX() {
		return sizeX;
	}
	public void setSize(int size) {
		if (size != this.sizeY || size != this.sizeX) {
			this.sizeY = this.sizeX = size;
			setBitmapDirty(true);
		}
	}
	public void setSize(int sizeX, int sizeY) {
		if (sizeY != this.sizeY || sizeX != this.sizeX) {
			this.sizeY = sizeY;
			this.sizeX = sizeX;
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
	
}