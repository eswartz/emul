package v9t9.gui.client.swt;

import v9t9.canvas.video.ImageDataCanvas;
import v9t9.common.hardware.IVdpChip;
import v9t9.gui.image.ImageImport;
import v9t9.gui.image.ImageImportOptions;

public abstract class ImageImportHandler implements IImageImportHandler {

	private ImageImportOptions imageImportOptions;

	public ImageImportHandler() {
		super();
	}

	abstract protected IVdpChip getVdpHandler() ;

	abstract protected ImageDataCanvas getCanvas();


	@Override
	public ImageImport createImageImport() {
		return new ImageImport(getCanvas(), getVdpHandler(), getImageImportOptions());
	}
	
	@Override
	public ImageImportOptions getImageImportOptions() {
		if (imageImportOptions == null) {
			imageImportOptions = new ImageImportOptions();
			resetOptions();
		}
		return imageImportOptions;
	}
	
	@Override
	public void resetOptions() {
		imageImportOptions.resetOptions(getCanvas(), getVdpHandler());
	}
	
}