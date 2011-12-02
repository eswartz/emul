/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import v9t9.emulator.clients.builtin.video.image.ImageImport;
import v9t9.emulator.clients.builtin.video.image.ImageImportOptions;

/**
 * @author ejs
 *
 */
public interface IImageImportHandler {
	ImageImport createImageImport();
	ImageImportOptions getImageImportOptions();
	void resetOptions();
}
