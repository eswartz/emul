package v9t9.video;

import java.nio.ByteBuffer;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.common.video.BitmapVdpCanvas;


public abstract class ImageDataCanvas extends BitmapVdpCanvas {

	protected ImageData imageData;
	protected int bytesPerLine;

	protected int pixSize;
	
	public ImageDataCanvas() {
		super();
	}

	public ImageData getImageData() {
		return imageData;
	}

	@Override
	final public int getLineStride() {
		return bytesPerLine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.ICanvas#getPixelStride()
	 */
	@Override
	final public int getPixelStride() {
		return pixSize;
	}


	@Override
	public void doChangeSize() {
		createImageData();
	}

	/** Create imageData and set bytesPerLine */
	abstract protected void createImageData();

	public int getDisplayAdjustOffset() {
		int displayAdjust = getYOffset() * getLineStride() + getXOffset() * getPixelStride();
		return displayAdjust;
	}


	/**
	 * @param buffer
	 * @return 
	 */
	public ByteBuffer copy(ByteBuffer buffer) {
		if (buffer.capacity() < imageData.bytesPerLine * getVisibleHeight())
			buffer = ByteBuffer.allocateDirect(imageData.bytesPerLine * getVisibleHeight());

		buffer.rewind();
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		int offs = getBitmapOffset(0, 0);
		int bpp = imageData.bytesPerLine / imageData.width;
		if (imageData.bytesPerLine == bpp * vw) {
			buffer.put(imageData.data, offs, bpp * vw * vh);
		} else {
			for (int r = 0; r < vh; r++) {
				buffer.put(imageData.data, offs, bpp * vw);
				offs += imageData.bytesPerLine;
			}
		}
		buffer.rewind();
		
		return buffer;
	}


	/** Get the dirty rectangle in pixels */
	public synchronized Rectangle getDirtyRect() {
		if (dx1 >= dx2 || dy1 >= dy2)
			return null;

		return new Rectangle(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
	}
	
}