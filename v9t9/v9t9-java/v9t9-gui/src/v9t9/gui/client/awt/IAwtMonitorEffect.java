/**
 * 
 */
package v9t9.gui.client.awt;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import v9t9.common.client.IMonitorEffect;

/**
 * @author ejs
 *
 */
public interface IAwtMonitorEffect extends IMonitorEffect {

	/**
	 * @param destWidth
	 * @param destHeight
	 * @param surface
	 * @param logRect
	 * @param physRect
	 * @return
	 */
	BufferedImage applyEffect(int destWidth, int destHeight,
			BufferedImage surface, Rectangle logRect, Rectangle physRect);

}
