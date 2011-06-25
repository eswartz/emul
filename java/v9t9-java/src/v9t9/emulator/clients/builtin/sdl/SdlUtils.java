/**
 * 
 */
package v9t9.emulator.clients.builtin.sdl;

import org.eclipse.swt.graphics.Rectangle;

import sdljava.video.SDLRect;

/**
 * @author Ed
 *
 */
public class SdlUtils {

	public static boolean pointInRect(SDLRect rect, int x, int y) {
		return rect.getX() <= x && x < rect.getWidth() + rect.getX()
		&& rect.getY() <= y && y < rect.getHeight() + rect.getY();
	}

	public static SDLRect convertRect(Rectangle rect) {
		if (rect == null)
			return new SDLRect(0, 0, 0, 0 );
		return new SDLRect(rect.x, rect.y, rect.width, rect.height);
	}

	public static SDLRect copyRect(SDLRect rect) {
		return new SDLRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static void addRect(SDLRect d, SDLRect rect) {
		if (rect.x < d.x)
			rect.x = d.x;
		if (rect.y < d.y)
			rect.y = d.y;
		if (rect.x + rect.width > d.x + d.width)
			d.width = rect.x + rect.width - d.x;
		if (rect.y + rect.height > d.y + d.height)
			d.height = rect.y + rect.height - d.y;
	}

	public static boolean rectIntersectsRect(SDLRect a, SDLRect b) {
		return a.x < b.x + b.width && a.x + a.width > b.x
		&& a.y < b.y + b.height && a.y + a.height > b.y;
	}
}
