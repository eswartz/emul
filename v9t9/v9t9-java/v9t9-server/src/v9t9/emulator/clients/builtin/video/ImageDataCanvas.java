package v9t9.emulator.clients.builtin.video;

import java.nio.ByteBuffer;

import org.eclipse.swt.graphics.ImageData;

public abstract class ImageDataCanvas extends BitmapVdpCanvas {

	protected ImageData imageData;

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



	@Override
	public void doChangeSize() {
		imageData = createImageData();
	}

	abstract protected ImageData createImageData();

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

}