package v9t9.video;

import java.nio.Buffer;

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
	public Buffer copy(Buffer buffer) {
		return copyBytes(buffer, imageData.data, imageData.bytesPerLine, imageData.bytesPerLine / imageData.width);
	}
	
	
}