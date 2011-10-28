/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.image.BufferedImage;

/**
 * @author ejs
 *
 */
public interface IImageImportHandler {
	void importImage(BufferedImage image, boolean isLowColor);
}
