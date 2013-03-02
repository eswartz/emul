/*
  ImageButtonMenuAreaHandler.java

  (c) 2012 Edward Swartz

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
