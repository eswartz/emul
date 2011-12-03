/**
 * 
 */
package v9t9.gui.client.swt;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.base.utils.Pair;


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
	 */
	public Pair<Double, Image> getImage(final int sx, final int sy) {
		int sz = Math.max(sx, sy);
		SortedMap<Integer, Image> tailMap = iconMap.tailMap(sz);
		Image icon;
		if (tailMap.isEmpty())
			icon = iconMap.lastEntry().getValue();
		else
			icon = tailMap.values().iterator().next();
		int min = iconMap.values().iterator().next().getBounds().width;
		double ratio = (double) icon.getBounds().width / min;
		return new Pair<Double, Image>(ratio, icon);
	}

	@Override
	public void drawImage(GC gc, Rectangle drawRect, Rectangle imgRect) {
		double ratio;
		Pair<Double, Image> iconInfo = getImage(drawRect.width, drawRect.height);
		ratio = iconInfo.first;
		Image icon = iconInfo.second;
		gc.drawImage(icon, (int)(imgRect.x * ratio), (int)(imgRect.y * ratio), 
				(int)(imgRect.width * ratio), (int) (imgRect.height * ratio), 
				drawRect.x, drawRect.y, drawRect.width, drawRect.height);
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
