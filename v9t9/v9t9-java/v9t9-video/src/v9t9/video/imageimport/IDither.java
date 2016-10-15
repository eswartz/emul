/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.IPaletteMapper;

/**
 * @author ejs
 *
 */
public interface IDither {

	void run(BufferedImage img, IPaletteMapper mapColor);

}
