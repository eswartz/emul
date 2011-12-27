package v9t9.gui.client.swt.imageimport;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashSet;

import v9t9.common.hardware.IVdpChip;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportOptions;

public abstract class ImageImportHandler implements IImageImportHandler {

	private ImageImportOptions imageImportOptions;
	private Collection<String> urlHistory = new LinkedHashSet<String>();

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
	
	public void importImage(BufferedImage image, boolean scaleSmooth) {
		ImageImport importer = createImageImport();
		ImageImportOptions imageImportOptions = getImageImportOptions();
		imageImportOptions.updateFrom(image);
		imageImportOptions.setScaleSmooth(scaleSmooth);
		importer.importImage();
	}
	
	public Collection<String> getHistory() {
		return urlHistory;
	}
	
}