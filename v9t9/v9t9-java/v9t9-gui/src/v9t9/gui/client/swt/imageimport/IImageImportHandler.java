/**
 * 
 */
package v9t9.gui.client.swt.imageimport;

import java.util.Collection;

import v9t9.video.imageimport.ImageFrame;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportDialogOptions;

/**
 * @author ejs
 *
 */
public interface IImageImportHandler {
	ImageImport createImageImport();
	ImageImportDialogOptions getImageImportOptions();
	void resetOptions();
	void stopRendering() ;
	void importImage(ImageFrame[] frames);
	
	Collection<String> getHistory();
}
