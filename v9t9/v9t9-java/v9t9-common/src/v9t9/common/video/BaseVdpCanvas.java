/**
 * 
 */
package v9t9.common.video;



/**
 * @author ejs
 *
 */
public abstract class BaseVdpCanvas implements ICanvas {
	protected int dx1;
	protected int dy1;
	protected int dx2;
	protected int dy2;
	/** width in pixels */
	protected int width;
	protected int bytesPerLine;
	/** height in pixels */
	protected int height;
	protected ICanvasListener listener;
	private VdpColorManager colorMgr;
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#setSize(int, int)
	 */
	@Override
	public void setSize(int x, int y) {
		if (x != width || y != height) {
			this.width = visibleToActualWidth(x);
			this.height = y;
			if (listener != null)
				listener.canvasResized(this);
		}
	}

	/** Convert the width displayed with the width in the canvas.
	 * We have 16 extra pixels for V9938 panning.  */
	protected int visibleToActualWidth(int x) {
		return x;
	}

	/** Convert the width in the canvas to the width displayed.  We have 16 extra
	 * pixels for V9938 panning. */
	protected int actualToVisibleWidth(int x) {
		return x;
	}

	public void markDirty(RedrawBlock[] blocks, int count) {
		if (dx1 == 0 && dy1 == 0 && dx2 == width && dy2 == height) {
			// already dirty
			listener.canvasDirtied(this);
		} else {
			for (int i = 0; i < count; i++) {
				RedrawBlock block = blocks[i];
				//int y = (block.r / 8);
				//int x = block.c / blockWidth;
				//int idx = y * dirtyStride + x;
				//dirtyBlocks[idx] = true;
				if (block.c < dx1) dx1 = block.c;
				if (block.r < dy1) dy1 = block.r;
				if (block.c + 8 >= dx2) dx2 = block.c + 8;
				if (block.r + 8 >= dy2) dy2 = block.r + 8;
			}
			if (count > 0 && listener != null)
				listener.canvasDirtied(this);
		}
	}
	
	public void markDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, true);
		dx1 = dy1 = 0;
		dx2 = width;
		dy2 = height;
		if (listener != null)
			listener.canvasDirtied(this);
	}
	
	public void clearDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, false);
		dx1 = width;
		dy1 = height; 
		dx2 = dy2 = 0;
	}
	
	public BaseVdpCanvas() {
		colorMgr = new VdpColorManager();
    	
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#getVisibleWidth()
	 */
	@Override
	public int getVisibleWidth() {
		return actualToVisibleWidth(width);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#getVisibleHeight()
	 */
	@Override
	public int getVisibleHeight() {
		return height;
	}

	public int getBlockCount() {
		return (getVisibleWidth() / 8) * ((height + 7) / 8);
	}

	/**
	 * @return the colorMgr
	 */
	public VdpColorManager getColorMgr() {
		return colorMgr;
	}
	
	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent, or some other value if supported 
	 */
	public void setClearColor(int c) {
		colorMgr.setClearColor(c);
		markDirty();
	}


}