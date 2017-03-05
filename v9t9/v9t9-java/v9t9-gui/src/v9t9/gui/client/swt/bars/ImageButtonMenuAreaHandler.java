/*
  ImageButtonMenuAreaHandler.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.ejs.gui.common.SwtUtils;

/**
 * @author ejs
 *
 */
public class ImageButtonMenuAreaHandler extends BaseImageButtonAreaHandler {
 
	private Rectangle menuOverlayBounds;
	private MenuDetectListener menuDetectListener;

	/**
	 * 
	 */
	public ImageButtonMenuAreaHandler(IImageProvider imageProvider, final IMenuHandler menuHandler) {
		menuOverlayBounds = imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY);
		
		menuDetectListener = new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				Control parent = (Control) e.widget;
				Menu menu = new Menu(parent);
				menuHandler.fillMenu(menu);
				if (!menu.isDisposed()) {
					if (menu.getItemCount() > 0) {
						SwtUtils.runMenu(null, e.x, e.y, menu);
					}
					menu.dispose();		
				}
			}
		};

	}

	@Override
	public boolean isInBounds(int x, int y, Point size) {
		return x >= size.x - size.x / 3 && y >= size.y - size.y / 3; 
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
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.BaseImageButtonAreaHandler#attach(v9t9.gui.client.swt.bars.ImageButton)
	 */
	@Override
	public void attach(ImageButton button) {
		super.attach(button);
		button.addMenuDetectListener(menuDetectListener);
	}
}
