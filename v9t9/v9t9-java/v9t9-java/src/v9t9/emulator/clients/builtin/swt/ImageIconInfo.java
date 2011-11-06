/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Information describing an icon
 * @author ejs
 *
 */
public class ImageIconInfo {

	protected Rectangle bounds;
	protected final ImageProvider imageProvider;
	private int iconIndex;
	
	public ImageIconInfo(ImageProvider imageProvider, int iconIndex) {
		this.imageProvider = imageProvider;
		setIconIndex(iconIndex);
	}
	
	/**
	 * @param imageProvider2
	 */
	public ImageIconInfo(ImageProvider imageProvider) {
		this.imageProvider = imageProvider;
	}

	public void setIconIndex(int iconIndex) {
		this.iconIndex = iconIndex;
		if (imageProvider != null) {
			this.bounds = imageProvider.imageIndexToBounds(iconIndex);
		} else {
			bounds = null;
		}
	}
	/**
	 * @return the iconIndex
	 */
	public int getIconIndex() {
		return iconIndex;
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public ImageProvider getImageProvider() {
		return imageProvider;
	}
	
}
