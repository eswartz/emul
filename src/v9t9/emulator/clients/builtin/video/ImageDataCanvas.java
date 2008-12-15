package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.graphics.ImageData;

public abstract class ImageDataCanvas extends VdpCanvas {

	protected ImageData imageData;

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

}