/**
 * 
 */
package v9t9.gui.client.swt;

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
}
