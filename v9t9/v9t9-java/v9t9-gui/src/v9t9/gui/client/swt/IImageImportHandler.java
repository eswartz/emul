/**
 * 
 */
package v9t9.gui.client.swt;

import v9t9.gui.image.ImageImport;
import v9t9.gui.image.ImageImportOptions;

/**
 * @author ejs
 *
 */
public interface IImageImportHandler {
	ImageImport createImageImport();
	ImageImportOptions getImageImportOptions();
	void resetOptions();
}
