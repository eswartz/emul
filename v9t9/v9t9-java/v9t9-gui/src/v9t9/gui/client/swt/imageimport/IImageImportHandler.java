/**
 * 
 */
package v9t9.gui.client.swt.imageimport;

import java.awt.image.BufferedImage;
import java.util.Collection;

import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportOptions;

/**
 * @author ejs
 *
 */
public interface IImageImportHandler {
	ImageImport createImageImport();
	ImageImportOptions getImageImportOptions();
	void resetOptions();
	
	void importImage(BufferedImage image, boolean scaleSmooth);
	
	Collection<String> getHistory();
}
