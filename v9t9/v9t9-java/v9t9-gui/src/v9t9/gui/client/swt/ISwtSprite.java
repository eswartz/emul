/**
 * 
 */
package v9t9.gui.client.swt;

import org.eclipse.swt.graphics.ImageData;

/**
 * @author ejs
 *
 */
public interface ISwtSprite {

	/** get image for sprite */
	ImageData getImageData();
	/** position, 0=center,  -1=left edge, 1=right edge */
	float getXPos();
	/** position, 0=center,  -1=top edge, 1=bottom edge */
	float getYPos();
	/** size, proportion of width */
	float getXSize();
	/** size, proportion of height */
	float getYSize();
}
