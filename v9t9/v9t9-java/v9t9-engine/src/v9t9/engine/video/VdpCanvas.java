/**
 * 
 */
package v9t9.engine.video;

import v9t9.common.video.ICanvasListener;
import v9t9.common.video.ISprite2Canvas;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas extends BaseVdpCanvas implements IVdpCanvas {
	protected VdpFormat format;

	boolean isInterlacedEvenOdd;
	private int xoffs;
	private int yoffs;
	
	protected byte[] colorMap;
	protected byte[] spriteColorMap;
	protected byte[][] fourColorMap;

	public VdpCanvas() {
    	setSize(256, 192);
    }

	public void setFormat(VdpFormat format) {
		this.format = format;
		getColorMgr().useAltSpritePalette(format == VdpFormat.COLOR256_1x1);
		setMono(false);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.video.IVdpCanvas#getFormat()
	 */
	@Override
	public VdpFormat getFormat() {
		return format;
	}
	
	private boolean isBlank;

	private boolean isMono;


	public final void setSize(int x, int y) {
		setSize(x, y, false);
	}

	public int getVisibleHeight() {
		return height * (isInterlacedEvenOdd ? 2 : 1);
	}
	

	public abstract void doChangeSize();

	public final void setSize(int x, int y, boolean isInterlaced) {
		if (x != width || y != height || isInterlaced != this.isInterlacedEvenOdd) {
			this.isInterlacedEvenOdd = isInterlaced;
			this.width = visibleToActualWidth(x);
			this.height = y;
			markDirty();
			doChangeSize();
			if (listener != null)
				listener.canvasResized(this);
		}
	}
	
	public boolean isBlank() {
		return isBlank;
	}


	public void setBlank(boolean b) {
		if (b != isBlank) {
			isBlank = b;
			markDirty();
		}
	}


	/** 
	 * Compose the block from the sprite canvas onto your canvas. 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitSpriteBlock(ISprite2Canvas spriteCanvas, int x, int y,
			int blockMag);

	/** 
	 * Compose the block from the sprite canvas onto your canvas, in four-color mode 
	 * @param spriteCanvas
	 * @param x the sprite canvas X position
	 * @param y the sprite canvas Y position
	 * @param blockMag if 1, x/y map to the receiver, else x is doubled in the receiver
	 * and the block is magnified 2x horizontally
	 */
	abstract public void blitFourColorSpriteBlock(ISprite2Canvas spriteCanvas, int x,
			int y, int blockMag);

	public void setListener(ICanvasListener listener) {
		this.listener = listener;
	}
	
	public void setInterlacedEvenOdd(boolean isInterlacedEvenOdd) {
		this.isInterlacedEvenOdd = isInterlacedEvenOdd;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.video.IVdpCanvas#isInterlacedEvenOdd()
	 */
	@Override
	public boolean isInterlacedEvenOdd() {
		return isInterlacedEvenOdd;
	}
	

	/**
	 * Set adjustment offset 
	 * @param i
	 * @param j
	 */
	public void setOffset(int x, int y) {
		xoffs = x;
		yoffs = y;
	}

	public int getXOffset() { 
		return xoffs;
	}

	public int getYOffset() {
		return yoffs;
	}

	public byte[] getRGB(int idx) {
		return getColorMgr().getRGB(idx);
	}

	/**
	 * 
	 */
	public void syncColors() {
		if (colorMap == null) {
			colorMap = new byte[16];
			spriteColorMap = new byte[16];
			fourColorMap = new byte[2][];
			fourColorMap[0] = new byte[16];
			fourColorMap[1] = new byte[16];
		}
		VdpColorManager cc = getColorMgr();
		if (cc.isClearFromPalette()) {
			colorMap[0] = 0;
			spriteColorMap[0] = 0;
		} else {
			colorMap[0] = (byte) cc.getClearColor();
			spriteColorMap[0] = (byte) cc.getClearColor();
		}
		fourColorMap[0][0] = (byte) cc.getFourColorModeColor(0, true);
		fourColorMap[1][0] = (byte) cc.getFourColorModeColor(0, false);
			
		for (int i = 1; i < 16; i++) {
			colorMap[i] = (byte) i;
			spriteColorMap[i] = (byte) i;
			fourColorMap[0][i] = (byte) cc.getFourColorModeColor(i, true);
			fourColorMap[1][i] = (byte) cc.getFourColorModeColor(i, false);
		}
	}

	/**
	 * @return
	 */
	public boolean isMono() {
		return isMono;
	}
	
	/**
	 * @param isMono the isMono to set
	 */
	public void setMono(boolean isMono) {
		this.isMono = isMono;
	}
}
