/**
 * Mar 11, 2011
 */
package v9t9.gui.client.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


public interface ImageProvider {
	void drawImage(GC gc, Rectangle drawRect, Rectangle imgRect);
	Rectangle imageIndexToBounds(int iconIndex);
}