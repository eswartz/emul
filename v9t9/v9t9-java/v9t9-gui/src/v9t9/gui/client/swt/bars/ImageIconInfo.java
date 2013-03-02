/*
  ImageIconInfo.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
