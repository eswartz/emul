/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.swt.ImageIconCanvas.IImageBar;

/**
 * Get an image which are available in multiple sizes
 * @author ejs
 *
 */
public class MultiImageSizeProvider implements ImageProvider {
	protected TreeMap<Integer, Image> iconMap;
	
	/**
	 * 
	 */
	public MultiImageSizeProvider(TreeMap<Integer, Image> iconMap) {
		this.iconMap = iconMap;
	}
	/**
	 * @param imageBar  
	 */
	public Pair<Double, Image> getImage(Point size, IImageBar imageBar) {
		SortedMap<Integer, Image> tailMap = iconMap.tailMap(size.x);
		Image icon;
		if (tailMap.isEmpty())
			icon = iconMap.lastEntry().getValue();
		else
			icon = tailMap.values().iterator().next();
		int min = iconMap.values().iterator().next().getBounds().width;
		double ratio = (double) icon.getBounds().width / min;
		return new Pair<Double, Image>(ratio, icon);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ImageButton.ImageProvider#drawImage(org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle, int, int, int)
	 */
	@Override
	public void drawImage(GC gc, Point size, Rectangle bounds,
			int xoffset, int yoffset, IImageBar imageBar) {
		double ratio;
		Pair<Double, Image> iconInfo = getImage(size, imageBar);
		ratio = iconInfo.first;
		Image icon = iconInfo.second;
		gc.drawImage(icon, (int)(bounds.x * ratio), (int)(bounds.y * ratio), 
				(int)(bounds.width * ratio), (int) (bounds.height * ratio), 
				xoffset, yoffset, size.x, size.y);
	}

	/**
	 * @param i
	 * @return
	 */
	public Rectangle imageIndexToBounds(int iconIndex) {
		Rectangle bounds = iconMap.values().iterator().next().getBounds();
		int unit = bounds.width;
		return new Rectangle(0, unit * iconIndex, unit, unit); 
	}
}
