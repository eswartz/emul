/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.swt.ImageButton.ImageProvider;

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
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ImageButton.ImageProvider#getImage(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public Pair<Double, Image> getImage(Point size) {
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

}
