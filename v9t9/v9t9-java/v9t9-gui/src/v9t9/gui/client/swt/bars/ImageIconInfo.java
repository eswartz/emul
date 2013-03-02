/*
  ImageIconInfo.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt.bars;

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
