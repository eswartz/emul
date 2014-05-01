/**
 * 
 */
package v9t9.common.dsr;

import java.awt.image.BufferedImage;

/**
 * @author ejs
 *
 */
public interface IPrinterImageListener {

	void updated();
	void newPage(BufferedImage image);
}
