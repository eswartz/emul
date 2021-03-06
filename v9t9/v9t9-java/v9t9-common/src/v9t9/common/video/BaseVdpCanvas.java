/*
  BaseVdpCanvas.java

  (c) 2011-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public abstract class BaseVdpCanvas implements ICanvas {
	public static class Rect {
		public Rect(int x, int y, int dx, int dy) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
		}

		public final int x, y, dx, dy;
	}
	protected int dx1;
	protected int dy1;
	protected int dx2;
	protected int dy2;
	/** width in pixels */
	protected int width;
	/** height in pixels */
	protected int height;
	protected ICanvasListener listener;
	private VdpColorManager colorMgr;
	private int minY;
	private int maxY;
	
	public void setListener(ICanvasListener listener) {
		this.listener = listener;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ICanvas#setSize(int, int)
	 */
	@Override
	public void setSize(int x, int y) {
		if (x != width || y != height) {
			this.width = visibleToActualWidth(x);
			this.height = y;
			this.minY = 0;
			this.maxY = height;
			if (listener != null)
				listener.canvasResized(this);
		}
	}


	@Override
	public int getMinY() {
		return minY;
	}

	@Override
	public void setMinY(int minY) {
		this.minY = minY;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	/** Convert the width displayed with the width in the canvas.
	 * We have 16 extra pixels for V9938 panning.  */
	protected int visibleToActualWidth(int x) {
		return x;
	}

	public synchronized void markDirty(RedrawBlock[] blocks, int count) {
		if (count == 0)
			return;
		if (dx1 == 0 && dy1 == 0 && dx2 == width && dy2 == height) {
			// already dirty
		} else {
			for (int i = 0; i < count; i++) {
				RedrawBlock block = blocks[i];
				if (block.c < dx1) dx1 = block.c;
				if (block.r < dy1) dy1 = Math.max(block.r, minY);
				if (block.c + 8 >= dx2) dx2 = block.c + 8;
				if (block.r + 8 >= dy2) dy2 = Math.min(block.r + 8, maxY);
			}
			if (listener != null)
				listener.canvasDirtied(this);
		}
	}

	public synchronized void markDirtyRows(int from, int to) {
		dx1 = 0;
		dx2 = width;
		if (dy1 <= from && dy2 >= to) {
			// already dirty
		} else {
			dy1 = Math.min(from, dy1);
			dy2 = Math.max(to, dy2);
			if (listener != null)
				listener.canvasDirtied(this);
		}
	}
	
	public synchronized void markDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, true);
		dx1 = 0;
		dy1 = minY;
		dx2 = width;
		dy2 = maxY;
		if (listener != null)
			listener.canvasDirtied(this);
	}
	
	public synchronized void clearDirty() {
		//Arrays.fill(dirtyBlocks, 0, dirtyBlocks.length, false);
		dx1 = width;
		dy1 = height; 
		dx2 = dy2 = minY;
	}

	public int getBlockCount() {
		return (getVisibleWidth() / 8) * ((height + 7) / 8);
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
		return width;
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


	/** Get the dirty rectangle in pixels */
	public synchronized Rect getDirtyRect() {
		if (dx1 >= dx2 || dy1 >= dy2)
			return null;

		return new Rect(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
	}
}