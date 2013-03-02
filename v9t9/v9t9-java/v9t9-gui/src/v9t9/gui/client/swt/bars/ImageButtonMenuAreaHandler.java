/*
  ImageButtonMenuAreaHandler.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author ejs
 *
 */
public class ImageButtonMenuAreaHandler extends BaseImageButtonAreaHandler {
 
	private Rectangle menuOverlayBounds;

	/**
	 * 
	 */
	public ImageButtonMenuAreaHandler(ImageProvider imageProvider) {
		menuOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#getBounds()
	 */
	@Override
	public Rectangle getBounds(Point size) {
		return new Rectangle(size.x - size.x/3, size.y - size.y / 3, 
				size.x / 3, size.y / 3);
	}
	
	@Override
	public boolean isMenu() {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#isActive()
	 */
	@Override
	public boolean isActive() {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageButtonAreaHandler#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return "Menu";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseEnter()
	 */
	@Override
	public void mouseEnter() {
		button.addImageOverlay(menuOverlayBounds);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#mouseExit()
	 */
	@Override
	public void mouseExit() {
		button.removeImageOverlay(menuOverlayBounds);
	}
}
