/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author ejs
 *
 */
public interface ISVGLoader {
	boolean isSlow();

	/**
	 * Transcode and create an image from the SVG.
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	ImageData getImageData(Point size) throws SVGException;

	/**
	 * Transcode and create an image from the SVG.
	 * @param aoi area of interest
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	ImageData getImageData(Rectangle aoi, Point size) throws SVGException;

	Point getSize();

	/**
	 * @return
	 */
	String getURI();

	/**
	 * @return
	 */
	boolean isValid();

}