package v9t9.emulator.clients.builtin.swt;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.image.ImageImport;
import v9t9.emulator.clients.builtin.video.image.ImageImportOptions;
import v9t9.engine.VdpHandler;

public abstract class ImageImportHandler implements IImageImportHandler {

	private ImageImportOptions imageImportOptions;

	public ImageImportHandler() {
		super();
	}

	abstract protected VdpHandler getVdpHandler() ;

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