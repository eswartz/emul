/**
 * Mar 11, 2011
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import v9t9.emulator.clients.builtin.swt.ImageIconCanvas.IImageBar;

public interface ImageProvider {
	void drawImage(GC gc, Point size, Rectangle bounds, int xoffset, int yoffset, IImageBar imageBar);
	Rectangle imageIndexToBounds(int iconIndex);
}