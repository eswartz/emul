package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.graphics.ImageData;

public abstract class ImageDataCanvas extends VdpCanvas {

	protected ImageData imageData;

	public ImageDataCanvas() {
		super();
	}

	public ImageData getImageData() {
		return imageData;
	}

	@Override
	public int getLineStride() {
		return imageData.bytesPerLine;
	}



	@Override
	public void doChangeSize() {
		imageData = createImageData();
	}

	abstract protected ImageData createImageData();

}